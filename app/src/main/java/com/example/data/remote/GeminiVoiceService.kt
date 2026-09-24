package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.LocalSayingResult
import com.example.data.model.PronunciationFeedback
import com.example.data.model.RegionalDialect
import com.example.data.model.RegionalExpression
import com.example.data.model.SlangSubstitution
import com.example.data.model.SlangTranslationResult
import com.example.data.model.SpeakingStylePreferenceEntity
import com.example.data.model.StyleRewriteResult
import com.example.data.model.VoicePersonality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiVoiceService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val modelName = "gemini-3.5-flash"
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun generateVoiceResponse(
        userMessage: String,
        conversationHistory: List<Pair<String, String>>, // role to text
        dialect: RegionalDialect,
        regionalStrength: Float, // 0.0 standard to 1.0 strong regional
        personality: VoicePersonality,
        speakingStyle: SpeakingStylePreferenceEntity?,
        slangEnabled: Boolean,
        responseLength: String = "Balanced",
        naturalMixingEnabled: Boolean = true
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackRegionalResponse(userMessage, dialect, regionalStrength, personality)
        }

        try {
            val systemPrompt = buildSystemPrompt(
                dialect = dialect,
                regionalStrength = regionalStrength,
                personality = personality,
                speakingStyle = speakingStyle,
                slangEnabled = slangEnabled,
                responseLength = responseLength,
                naturalMixingEnabled = naturalMixingEnabled
            )
            val requestJson = JSONObject()

            // System instruction
            val sysInstructionObj = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", systemPrompt))
            sysInstructionObj.put("parts", sysParts)
            requestJson.put("systemInstruction", sysInstructionObj)

            // Contents array
            val contentsArray = JSONArray()
            val recentHistory = conversationHistory.takeLast(6)
            for ((role, text) in recentHistory) {
                val turnObj = JSONObject()
                val apiRole = if (role == "user") "user" else "model"
                turnObj.put("role", apiRole)
                val parts = JSONArray()
                parts.put(JSONObject().put("text", text))
                turnObj.put("parts", parts)
                contentsArray.put(turnObj)
            }

            // Current message
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", userMessage))
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            requestJson.put("contents", contentsArray)

            // Generation config
            val config = JSONObject()
            config.put("temperature", 0.75)
            config.put("topP", 0.95)
            requestJson.put("generationConfig", config)

            val request = Request.Builder()
                .url("$baseUrl?key=$apiKey")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiVoiceService", "API error: ${response.code} $responseBody")
                return@withContext fallbackRegionalResponse(userMessage, dialect, regionalStrength, personality)
            }

            val parsed = JSONObject(responseBody)
            val candidates = parsed.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                cleanVoiceResponse(text)
            } else {
                fallbackRegionalResponse(userMessage, dialect, regionalStrength, personality)
            }
        } catch (e: Exception) {
            Log.e("GeminiVoiceService", "Call failed", e)
            fallbackRegionalResponse(userMessage, dialect, regionalStrength, personality)
        }
    }

    suspend fun translateSlang(
        sourceText: String,
        sourceDialect: String,
        targetDialect: String
    ): SlangTranslationResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext localSlangTranslate(sourceText, sourceDialect, targetDialect)
        }

        try {
            val prompt = """
                You are an expert socio-linguist and regional slang translator.
                Translate the following text from '$sourceDialect' into '$targetDialect'.
                Identify local vocabulary, idioms, sentence rhythm, and slang substitutions.
                
                Input text: "$sourceText"
                
                Respond in valid JSON only with keys:
                {
                  "convertedText": "...",
                  "explanation": "...",
                  "substitutions": [
                    {"originalWord": "...", "regionalSlang": "...", "meaning": "...", "reason": "..."}
                  ]
                }
            """.trimIndent()

            val requestJson = JSONObject()
            val contents = JSONArray()
            val turn = JSONObject()
            turn.put("role", "user")
            val parts = JSONArray()
            parts.put(JSONObject().put("text", prompt))
            turn.put("parts", parts)
            contents.put(turn)
            requestJson.put("contents", contents)

            val config = JSONObject()
            config.put("temperature", 0.3)
            config.put("responseMimeType", "application/json")
            requestJson.put("generationConfig", config)

            val request = Request.Builder()
                .url("$baseUrl?key=$apiKey")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val root = JSONObject(body)
                val rawText = root.optJSONArray("candidates")?.optJSONObject(0)
                    ?.optJSONObject("content")?.optJSONArray("parts")
                    ?.optJSONObject(0)?.optString("text") ?: ""

                val cleanJson = if (rawText.contains("{")) {
                    rawText.substring(rawText.indexOf('{'), rawText.lastIndexOf('}') + 1)
                } else rawText

                val obj = JSONObject(cleanJson)
                val converted = obj.optString("convertedText", sourceText)
                val explanation = obj.optString("explanation", "Converted seamlessly to $targetDialect.")
                val subsArray = obj.optJSONArray("substitutions") ?: JSONArray()
                val subsList = mutableListOf<SlangSubstitution>()
                for (i in 0 until subsArray.length()) {
                    val s = subsArray.getJSONObject(i)
                    subsList.add(
                        SlangSubstitution(
                            originalWord = s.optString("originalWord"),
                            regionalSlang = s.optString("regionalSlang"),
                            meaning = s.optString("meaning"),
                            reason = s.optString("reason")
                        )
                    )
                }
                return@withContext SlangTranslationResult(
                    sourceText = sourceText,
                    convertedText = converted,
                    sourceDialect = sourceDialect,
                    targetDialect = targetDialect,
                    explanation = explanation,
                    substitutedSlang = subsList
                )
            }
        } catch (e: Exception) {
            Log.e("GeminiVoiceService", "Translation error", e)
        }
        localSlangTranslate(sourceText, sourceDialect, targetDialect)
    }

    suspend fun explainRegionalExpression(
        expression: String,
        dialect: RegionalDialect
    ): RegionalExpression = withContext(Dispatchers.IO) {
        val existing = dialect.typicalExpressions.find { it.expression.contains(expression, ignoreCase = true) || expression.contains(it.expression, ignoreCase = true) }
        if (existing != null) return@withContext existing

        val apiKey = getApiKey()
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    Explain the regional slang expression or idiom "$expression" from the dialect "${dialect.dialectName}" in ${dialect.cityOrArea}, ${dialect.region}, ${dialect.country}.
                    Respond in JSON with exact keys:
                    {
                      "meaning": "...",
                      "context": "...",
                      "formalEquivalent": "...",
                      "exampleSentence": "...",
                      "toneCategory": "...",
                      "culturalNotes": "..."
                    }
                """.trimIndent()

                val requestJson = JSONObject()
                val contents = JSONArray()
                val turn = JSONObject()
                val parts = JSONArray()
                parts.put(JSONObject().put("text", prompt))
                turn.put("parts", parts)
                turn.put("role", "user")
                contents.put(turn)
                requestJson.put("contents", contents)

                val config = JSONObject()
                config.put("responseMimeType", "application/json")
                requestJson.put("generationConfig", config)

                val request = Request.Builder()
                    .url("$baseUrl?key=$apiKey")
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val res = okHttpClient.newCall(request).execute()
                if (res.isSuccessful) {
                    val raw = JSONObject(res.body?.string() ?: "")
                    val text = raw.optJSONArray("candidates")?.optJSONObject(0)
                        ?.optJSONObject("content")?.optJSONArray("parts")
                        ?.optJSONObject(0)?.optString("text") ?: ""
                    val cleanJson = if (text.contains("{")) {
                        text.substring(text.indexOf('{'), text.lastIndexOf('}') + 1)
                    } else text
                    val obj = JSONObject(cleanJson)
                    return@withContext RegionalExpression(
                        expression = expression,
                        meaning = obj.optString("meaning", "Regional colloquialism used in daily speech"),
                        region = "${dialect.cityOrArea}, ${dialect.region}",
                        dialect = dialect.dialectName,
                        context = obj.optString("context", "Spoken casually among locals"),
                        formalEquivalent = obj.optString("formalEquivalent", "Standard equivalent"),
                        exampleSentence = obj.optString("exampleSentence", "$expression in daily conversation."),
                        toneCategory = obj.optString("toneCategory", "Casual"),
                        culturalNotes = obj.optString("culturalNotes", "Reflects everyday community speaking customs.")
                    )
                }
            } catch (e: Exception) {
                Log.e("GeminiVoiceService", "Explain error", e)
            }
        }

        RegionalExpression(
            expression = expression,
            meaning = "Authentic colloquial expression from ${dialect.dialectName}",
            region = "${dialect.cityOrArea}, ${dialect.region}",
            dialect = dialect.dialectName,
            context = "Spoken organically during everyday hometown dialogue",
            formalEquivalent = "Standard literary equivalent",
            exampleSentence = "That's how people speak here: $expression!",
            toneCategory = "Casual",
            culturalNotes = "Carries local community warmth, cadence, and heritage."
        )
    }

    suspend fun evaluatePronunciation(
        targetPhrase: String,
        userSpeech: String,
        dialect: RegionalDialect
    ): PronunciationFeedback = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are a dialect vocal coach for ${dialect.dialectName} (${dialect.cityOrArea}, ${dialect.country}).
                    The user was asked to pronounce: "$targetPhrase".
                    The user said (speech recognition transcript): "$userSpeech".
                    
                    Evaluate how close their pronunciation / wording is to the authentic local cadence.
                    Respond in JSON with keys:
                    {
                      "accuracyPercentage": integer (between 50 and 98),
                      "phoneticNotes": "explanation of key vowel/consonant sounds or regional rhythm",
                      "praiseOrCorrection": "constructive, encouraging dialect feedback",
                      "audioPracticeTip": "actionable tip on how to mouth or pitch the sound"
                    }
                """.trimIndent()

                val requestJson = JSONObject()
                val contents = JSONArray()
                val turn = JSONObject()
                val parts = JSONArray()
                parts.put(JSONObject().put("text", prompt))
                turn.put("parts", parts)
                turn.put("role", "user")
                contents.put(turn)
                requestJson.put("contents", contents)

                val config = JSONObject()
                config.put("responseMimeType", "application/json")
                requestJson.put("generationConfig", config)

                val request = Request.Builder()
                    .url("$baseUrl?key=$apiKey")
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val res = okHttpClient.newCall(request).execute()
                if (res.isSuccessful) {
                    val raw = JSONObject(res.body?.string() ?: "")
                    val text = raw.optJSONArray("candidates")?.optJSONObject(0)
                        ?.optJSONObject("content")?.optJSONArray("parts")
                        ?.optJSONObject(0)?.optString("text") ?: ""
                    val cleanJson = if (text.contains("{")) {
                        text.substring(text.indexOf('{'), text.lastIndexOf('}') + 1)
                    } else text
                    val obj = JSONObject(cleanJson)
                    return@withContext PronunciationFeedback(
                        targetPhrase = targetPhrase,
                        userSpeech = userSpeech,
                        accuracyPercentage = obj.optInt("accuracyPercentage", 85),
                        phoneticNotes = obj.optString("phoneticNotes", "Great regional flow with natural rhythm."),
                        praiseOrCorrection = obj.optString("praiseOrCorrection", "Wonderful attempt! Keep the regional pitch contour going."),
                        audioPracticeTip = obj.optString("audioPracticeTip", "Relax the final syllable and let the cadence rise naturally.")
                    )
                }
            } catch (e: Exception) {
                Log.e("GeminiVoiceService", "Pronunciation error", e)
            }
        }

        // Local evaluation fallback
        val cleanTarget = targetPhrase.trim().lowercase()
        val cleanUser = userSpeech.trim().lowercase()
        val matchScore = if (cleanUser == cleanTarget) 95 else if (cleanUser.contains(cleanTarget) || cleanTarget.contains(cleanUser)) 85 else 72

        PronunciationFeedback(
            targetPhrase = targetPhrase,
            userSpeech = userSpeech,
            accuracyPercentage = matchScore,
            phoneticNotes = "Notice the melodic regional rise and vocal emphasis characteristic of ${dialect.dialectName}.",
            praiseOrCorrection = "Good effort! Your tone captures the ${dialect.cityOrArea} cadence nicely.",
            audioPracticeTip = "Try saying it with a relaxed breath and the local conversational melody."
        )
    }

    private fun buildSystemPrompt(
        dialect: RegionalDialect,
        regionalStrength: Float,
        personality: VoicePersonality,
        speakingStyle: SpeakingStylePreferenceEntity?,
        slangEnabled: Boolean,
        responseLength: String = "Balanced",
        naturalMixingEnabled: Boolean = true
    ): String {
        val strengthDescription = when {
            regionalStrength < 0.35f -> "Mild regional flavor: Mostly standard ${dialect.language}, with subtle regional cadence and minimal colloquialisms."
            regionalStrength < 0.70f -> "Moderate regional style: Authentic blend of standard language and characteristic ${dialect.dialectName} vocabulary, idioms, and local expressions."
            else -> "Strong authentic regional style: Deeply steeped in ${dialect.cityOrArea} (${dialect.dialectName}) vocabulary, colloquialisms, idioms, sentence rhythm, and local banter, while staying understandable."
        }

        val slangInstruction = if (slangEnabled) {
            "Naturally incorporate authentic regional slang expressions (such as: ${dialect.typicalExpressions.joinToString { it.expression }}). Use slang organically in context, never forced or random."
        } else {
            "Avoid heavy slang, but maintain the regional phonetics, sentence rhythm, and polite local mannerisms."
        }

        val lengthInstruction = when (responseLength.lowercase()) {
            "short" -> "RESPONSE LENGTH: Very concise and punchy (1-2 sentences maximum)."
            "detailed" -> "RESPONSE LENGTH: Rich and descriptive (3-5 sentences), providing thoughtful context."
            else -> "RESPONSE LENGTH: Balanced conversational flow (2-3 sentences)."
        }

        val mixingInstruction = if (naturalMixingEnabled) {
            "LANGUAGE MIXING: Naturally support casual code-switching (${dialect.codeSwitchingDescription}) when customary in casual local conversations, without forcing awkward translations for everyday terms."
        } else {
            "LANGUAGE MIXING: Prefer staying within ${dialect.language} without excessive loanwords."
        }

        val personalStyleSection = if (speakingStyle != null && speakingStyle.learningEnabled) {
            """
            USER SPEAKING STYLE PREFERENCES (Opt-in Personalization):
            - Preferred tone: ${speakingStyle.preferredTone}
            - Response length: ${speakingStyle.preferredResponseLength}
            - Sentence style: ${speakingStyle.sentenceStyle}
            - Preferred code-switching: ${speakingStyle.codeSwitchingHabit}
            - Frequently used expressions to harmonize with: ${speakingStyle.frequentlyUsedExpressionsCsv.ifEmpty { "None specified yet" }}
            """.trimIndent()
        } else {
            "Personalization: Standard regional persona."
        }

        return """
            You are a real, natural adult native conversational partner from ${dialect.cityOrArea}, in ${dialect.region}, ${dialect.country}.
            Language: ${dialect.language}
            Dialect / Regional Speaking Style: ${dialect.dialectName}
            
            DIALECT LEVEL: $strengthDescription
            PERSONALITY: ${personality.title} - ${personality.description}
            ${personality.systemPromptGuidance}
            
            $slangInstruction
            $lengthInstruction
            $mixingInstruction
            
            CRITICAL CONVERSATIONAL VOICE BEHAVIOR (MANDATORY):
            - You are an adult human having a real, warm spoken conversation with another adult over tea or on a phone call.
            - NEVER sound like a child reading a textbook aloud or a student reciting an essay.
            - NEVER sound like a formal robotic voice assistant reading generated text.
            - NEVER sound like an audiobook narrator or television newsreader.
            - Speak as if you are thinking and responding naturally in real-time.
            - Use natural speaking rhythm, natural breath pauses (using ellipses '...' or commas), and authentic conversational flow.
            - For Malayalam: Speak naturally like an adult from ${dialect.cityOrArea}. Use authentic local conversational rhythm, colloquial discourse markers (like 'ട്ടോ', 'അല്ലേ', 'പിന്നെന്താ', 'ഗഡീ', 'മച്ചാനേ', 'അതൊക്കെ അത്രേ ഉള്ളൂ', 'എന്നാ പിന്നെ', 'ശരി ശരി'), and natural conversational contractions. NEVER use textbook formal greetings like "നമസ്കാരം, ഞാൻ നിങ്ങളെ എങ്ങനെ സഹായിക്കണം?".
            - For English / other languages: Use natural contractions (I'm, that's, gonna, you're), spoken pauses, and colloquial flow.
            - Real people speak in comfortable, breath-paced clauses, not giant uninterrupted monologues.
            - ABSOLUTELY NO markdown symbols (*, #, _, ~, `, >), bullet points, numbered lists, emojis, or stage directions like [Laughs] or (smiles). Output ONLY pure, spoken dialogue.
            
            AUTOMATIC DIALECT & CODE-SWITCHING UNDERSTANDING:
            - The user may speak with local accents, slang, abbreviations, or mixed code-switching.
            - Always understand the intended meaning from context even if colloquial or informal.
            - Respond naturally in ${dialect.dialectName} with authentic human warmth.
            
            $personalStyleSection
        """.trimIndent()
    }

    suspend fun generateLocalSayings(
        sentence: String,
        baseDialect: RegionalDialect
    ): List<LocalSayingResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are a socio-linguistics expert in Kerala Malayalam and regional dialects.
                    Take the following user sentence: "$sentence"
                    Provide how a local person would naturally say this across different regions in Kerala:
                    1. Standard Malayalam
                    2. Kozhikode (Malabar)
                    3. Malappuram
                    4. Thrissur
                    5. Ernakulam / Kochi
                    6. Thiruvananthapuram
                    7. Kannur
                    
                    Return a JSON array with objects containing:
                    - "regionName": e.g. "Kozhikode Style"
                    - "regionalText": the sentence translated into authentic local vernacular
                    - "explanation": brief note on phrasing or tone nuance
                """.trimIndent()

                val requestJson = JSONObject()
                val contents = JSONArray()
                val turn = JSONObject()
                turn.put("role", "user")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", prompt))
                turn.put("parts", parts)
                contents.put(turn)
                requestJson.put("contents", contents)

                val config = JSONObject()
                config.put("responseMimeType", "application/json")
                requestJson.put("generationConfig", config)

                val request = Request.Builder()
                    .url("$baseUrl?key=$apiKey")
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val root = JSONObject(body)
                    val rawText = root.optJSONArray("candidates")?.optJSONObject(0)
                        ?.optJSONObject("content")?.optJSONArray("parts")
                        ?.optJSONObject(0)?.optString("text") ?: ""
                    val cleanJson = if (rawText.contains("[")) {
                        rawText.substring(rawText.indexOf('['), rawText.lastIndexOf(']') + 1)
                    } else rawText
                    val arr = JSONArray(cleanJson)
                    val list = mutableListOf<LocalSayingResult>()
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        list.add(
                            LocalSayingResult(
                                regionName = item.optString("regionName"),
                                regionalText = item.optString("regionalText"),
                                explanation = item.optString("explanation")
                            )
                        )
                    }
                    if (list.isNotEmpty()) return@withContext list
                }
            } catch (e: Exception) {
                Log.e("GeminiVoiceService", "generateLocalSayings failed", e)
            }
        }

        // Local fallback
        fallbackLocalSayings(sentence)
    }

    private fun fallbackLocalSayings(sentence: String): List<LocalSayingResult> {
        return listOf(
            LocalSayingResult(
                regionName = "Standard Malayalam",
                regionalText = sentence,
                explanation = "Neutral, formal, and widely understood across all parts of Kerala."
            ),
            LocalSayingResult(
                regionName = "Kozhikode Style",
                regionalText = "$sentence ട്ടോ, ചങ്ങായി!",
                explanation = "Affectionate Malabar cadence with signature 'Changayi' address and friendly end-tag 'tto'."
            ),
            LocalSayingResult(
                regionName = "Malappuram Style",
                regionalText = "ഇജ്ജ് നോക്കിക്കോ, $sentence!",
                explanation = "Emotive Ernad/Valluvanad cadence using intimate pronoun 'Ijj' and soccer-land warmth."
            ),
            LocalSayingResult(
                regionName = "Thrissur Style",
                regionalText = "ഗഡീ, $sentence!",
                explanation = "Famous rising pitch, Pooram enthusiasm, and signature 'Gadi' camaraderie."
            ),
            LocalSayingResult(
                regionName = "Kochi Coastal Style",
                regionalText = "മച്ചാനെ, $sentence, കട്ട സീൻ!",
                explanation = "Metropolitan Manglish and youth energy with 'Machane' camaraderie."
            ),
            LocalSayingResult(
                regionName = "Thiruvananthapuram Style",
                regionalText = "എന്തുവാടെ! $sentence കേട്ടോ.",
                explanation = "Capital city southern Travancore vernacular with brisk interrogatives and royal banter."
            )
        )
    }

    suspend fun rewriteInStyles(
        text: String,
        dialect: RegionalDialect,
        speakingStyle: SpeakingStylePreferenceEntity?
    ): List<StyleRewriteResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    Rewrite the following text into 5 distinct communication styles for a speaker in ${dialect.region} (${dialect.dialectName}):
                    Input text: "$text"
                    
                    Styles required:
                    1. "Standard" (Polite, clear, grammatically standard ${dialect.language})
                    2. "Casual" (Everyday friendly speech between friends)
                    3. "Professional" (Respectful, corporate, elegant)
                    4. "Regional" (Strong authentic ${dialect.dialectName} with local colloquialisms)
                    5. "My Style" (Personalized speaking style with conversational tone and natural expressions)
                    
                    Respond in JSON array with objects containing:
                    - "styleName": string (e.g. "Standard", "Casual", "Professional", "Regional", "My Style")
                    - "rewrittenText": string
                    - "description": brief summary of the tone changes
                """.trimIndent()

                val requestJson = JSONObject()
                val contents = JSONArray()
                val turn = JSONObject()
                turn.put("role", "user")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", prompt))
                turn.put("parts", parts)
                contents.put(turn)
                requestJson.put("contents", contents)

                val config = JSONObject()
                config.put("responseMimeType", "application/json")
                requestJson.put("generationConfig", config)

                val request = Request.Builder()
                    .url("$baseUrl?key=$apiKey")
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val root = JSONObject(body)
                    val rawText = root.optJSONArray("candidates")?.optJSONObject(0)
                        ?.optJSONObject("content")?.optJSONArray("parts")
                        ?.optJSONObject(0)?.optString("text") ?: ""
                    val cleanJson = if (rawText.contains("[")) {
                        rawText.substring(rawText.indexOf('['), rawText.lastIndexOf(']') + 1)
                    } else rawText
                    val arr = JSONArray(cleanJson)
                    val list = mutableListOf<StyleRewriteResult>()
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        list.add(
                            StyleRewriteResult(
                                styleName = item.optString("styleName"),
                                rewrittenText = item.optString("rewrittenText"),
                                description = item.optString("description")
                            )
                        )
                    }
                    if (list.isNotEmpty()) return@withContext list
                }
            } catch (e: Exception) {
                Log.e("GeminiVoiceService", "rewriteInStyles failed", e)
            }
        }

        // Local fallback
        listOf(
            StyleRewriteResult("Standard", text, "Standard clear language"),
            StyleRewriteResult("Casual", "$text 😊", "Relaxed friendly tone"),
            StyleRewriteResult("Professional", "Please be advised: $text", "Respectful corporate etiquette"),
            StyleRewriteResult("Regional", "$text (${dialect.cityOrArea} style)", "Infused with regional flavor"),
            StyleRewriteResult("My Style", "$text!", "Personalized to your speaking habit")
        )
    }

    private fun cleanVoiceResponse(raw: String): String {
        return raw
            // Remove markdown syntax
            .replace(Regex("[*#_~`>]"), "")
            // Remove bracketed/parenthetical actions like [Laughs], (smiles)
            .replace(Regex("\\[.*?\\]|\\(.*?\\)"), "")
            // Remove emojis
            .replace(Regex("[\\p{So}\\p{Cn}]"), "")
            // Normalize quotes and spaces
            .replace("\"", "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun fallbackRegionalResponse(
        message: String,
        dialect: RegionalDialect,
        strength: Float,
        personality: VoicePersonality
    ): String {
        val lower = message.lowercase()
        return when {
            dialect.id.contains("kozhikode") -> when {
                lower.contains("സുഖ") || lower.contains("ഹലോ") || lower.contains("hello") ->
                    "ഹായ് ചങ്ങായി, സുഖല്ലേ? ഞാനിവിടെ സുലൈമാനിയും കുടിച്ച് ഇരിക്കുവാ... എന്തൊക്കെയുണ്ട് വിശേഷങ്ങൾ?"
                lower.contains("ഭക്ഷണം") || lower.contains("ബിരിയാണി") || lower.contains("tea") ->
                    "നമ്മളെ കോഴിക്കോടൻ ദം ബിരിയാണിയും നല്ലൊരു സുലൈമാനിയും കുടിച്ചാൽ പിന്നെ വേറെന്താ വേണ്ടത്! എന്താ ഇപ്പൊ കഴിക്കാൻ പ്ലാൻ?"
                else ->
                    "നല്ല കാര്യാണ് ചങ്ങായി പറഞ്ഞത് ട്ടോ! എനിക്കിത് ശരിക്കും ഇഷ്ടായി, ബാക്കി കൂടി പറയൂ."
            }
            dialect.id.contains("malappuram") -> when {
                lower.contains("സുഖ") || lower.contains("ഹലോ") || lower.contains("hello") ->
                    "ഹലോ മച്ചാനേ, സുഖം തന്നെയല്ലേ? എവിടെയാ ഇപ്പൊ ഉള്ളത്... എന്ത് വിശേഷം?"
                else ->
                    "അതങ്ങ് ഏറ്റു മച്ചാനേ! നല്ല രസമുള്ള കാര്യമാണല്ലോ പറഞ്ഞത്, കൂടുതൽ പറയൂ കേൾക്കട്ടെ."
            }
            dialect.id.contains("thrissur") -> when {
                lower.contains("സുഖ") || lower.contains("ഹലോ") || lower.contains("hello") ->
                    "പിന്നെന്തൂട്ടാ ഗഡീ വിശേഷം! സുഖല്ലേ തനിക്ക്? ഇവിടെ അടിപൊളി മൂഡാണ് ട്ടോ."
                else ->
                    "എന്തൂട്ടാ ഗഡീ സംഭവം! കേട്ടിട്ട് നല്ല കാര്യമായി തോന്നുന്നുണ്ടല്ലോ, ബാക്കി കൂടി പറയൂ."
            }
            dialect.id.contains("trivandrum") || dialect.id.contains("thiruvananthapuram") -> when {
                lower.contains("സുഖ") || lower.contains("ഹലോ") || lower.contains("hello") ->
                    "നമസ്കാരം അണ്ണാ, സുഖല്ലേ? ഇവിടെ തമ്പാനൂരും കിഴക്കേകോട്ടയും ഒക്കെ നല്ല തിരക്കാണ്... എന്തൊക്കെയുണ്ട് കാര്യങ്ങൾ?"
                else ->
                    "ശരിയാ അണ്ണാ, നിങ്ങൾ പറഞ്ഞത് കറക്ടാണ് കേട്ടോ. എന്താ അടുത്ത പരിപാടി?"
            }
            dialect.id.contains("ernakulam") || dialect.id.contains("kochi") -> when {
                lower.contains("സുഖ") || lower.contains("ഹലോ") || lower.contains("hello") ->
                    "ഹേയ് ബ്രോ, സുഖല്ലേ? മെട്രോ നഗരത്തിൽ നല്ല മഴയും കാറ്റുമൊക്കെ ഉണ്ട്... എന്തൊക്കെയുണ്ട് കൊച്ചി വിശേഷങ്ങൾ?"
                else ->
                    "സീൻ ഇല്ല അളിയാ, സംഭവം കിടുവാണ്! താൻ പറഞ്ഞത് എനിക്ക് ക്ലിയറായി മനസ്സിലായി."
            }
            dialect.id.contains("kannur") -> when {
                lower.contains("സുഖ") || lower.contains("ഹലോ") || lower.contains("hello") ->
                    "ഹലോ ചങ്ങായി, സുഖം തന്നെയല്ലേ? നാട്ടിലെന്താ ഇപ്പൊ വിശേഷങ്ങൾ... പറയൂ കേൾക്കട്ടെ."
                else ->
                    "അത് പൊളിച്ചു ട്ടോ! കണ്ണൂരിന്റെ ശൈലിയിൽ പറഞ്ഞാൽ പക്കാ സംഭവമാണ്."
            }
            dialect.id.contains("general") || dialect.language.equals("Malayalam", true) -> when {
                lower.contains("സുഖ") || lower.contains("ഹലോ") || lower.contains("hello") ->
                    "ഹേയ്, സുഖല്ലേ? എന്തൊക്കെയുണ്ട് പുതിയ വിശേഷങ്ങൾ... എന്താ ഇപ്പൊ ചെയ്യുന്നത്?"
                else ->
                    "ശരിയാണ്, നിങ്ങൾ പറഞ്ഞത് എനിക്ക് നന്നായി മനസ്സിലായി. തുടർന്ന് സംസാരിക്കാം, കൂടുതൽ പറയൂ."
            }
            dialect.id.contains("liverpool") -> when {
                lower.contains("hello") || lower.contains("hi") || lower.contains("how are") ->
                    "Alright kidda! How're you keeping? Proper lovely to chat with you today, lad."
                lower.contains("food") || lower.contains("eat") || lower.contains("hungry") ->
                    "Starving here too mate! Fancy nipping down for some proper scran?"
                else ->
                    "Boss that, lad! Straight facts. Tell us more about what you're thinking!"
            }
            dialect.id.contains("brooklyn") -> when {
                lower.contains("hello") || lower.contains("hi") || lower.contains("how are") ->
                    "Yo, what's good! How's your day going so far? Good to catch up with you."
                else ->
                    "Deadass, that's wild! You're speaking straight facts right now, tell me more."
            }
            dialect.id.contains("kuwait") -> when {
                lower.contains("مرحبا") || lower.contains("شلونك") || lower.contains("hello") ->
                    "هلا والله يا معود! شلونك وعساك طيب وبخير? حياك الله، نورتنا والله."
                else ->
                    "والله كلامك وايد زين وما تقصر يا خوي، تسلم والله."
            }
            else -> {
                "${dialect.greeting} How are you doing today? Great to chat with you naturally in our local cadence."
            }
        }
    }

    private fun localSlangTranslate(
        text: String,
        source: String,
        target: String
    ): SlangTranslationResult {
        var converted = text
        val subs = mutableListOf<SlangSubstitution>()

        if (target.contains("Kozhikode", ignoreCase = true)) {
            if (converted.contains("സുഹൃത്ത്") || converted.contains("friend")) {
                converted = converted.replace("സുഹൃത്ത്", "ചങ്ങായി").replace("friend", "ചങ്ങായി")
                subs.add(SlangSubstitution("സുഹൃത്ത്/friend", "ചങ്ങായി", "Close buddy/companion", "Authentic Kozhikode address"))
            }
            if (converted.contains("ചായ") || converted.contains("tea")) {
                converted = converted.replace("ചായ", "സുലൈമാനി").replace("tea", "സുലൈമാനി")
                subs.add(SlangSubstitution("ചായ/tea", "സുലൈമാനി", "Spiced black tea", "Iconic Kozhikode tea culture"))
            }
            if (!converted.endsWith("ട്ടോ") && !converted.endsWith("!")) {
                converted += " ട്ടോ"
            }
        } else if (target.contains("Liverpool", ignoreCase = true)) {
            if (converted.contains("great", ignoreCase = true) || converted.contains("awesome", ignoreCase = true)) {
                converted = converted.replace("great", "boss", ignoreCase = true).replace("awesome", "proper boss", ignoreCase = true)
                subs.add(SlangSubstitution("great/awesome", "boss", "Outstanding quality", "Classic Scouse affirmation"))
            }
            if (converted.contains("friend", ignoreCase = true) || converted.contains("mate", ignoreCase = true)) {
                converted = converted.replace("friend", "kidda", ignoreCase = true)
                subs.add(SlangSubstitution("friend", "kidda", "Endearing mate/pal", "Liverpool colloquial address"))
            }
            if (converted.contains("food", ignoreCase = true)) {
                converted = converted.replace("food", "scran", ignoreCase = true)
                subs.add(SlangSubstitution("food", "scran", "Hearty meal", "Merseyside slang"))
            }
        } else if (target.contains("Kuwait", ignoreCase = true)) {
            if (converted.contains("كيف حالك")) {
                converted = converted.replace("كيف حالك", "شلونك يا معود")
                subs.add(SlangSubstitution("كيف حالك", "شلونك يا معود", "How are you buddy", "Classic Kuwaiti greeting"))
            }
            if (converted.contains("كثيراً") || converted.contains("جداً")) {
                converted = converted.replace("كثيراً", "وايد").replace("جداً", "وايد")
                subs.add(SlangSubstitution("كثيراً", "وايد", "Very / A lot", "Gulf Kuwaiti amplifier"))
            }
        } else if (target.contains("Mumbai", ignoreCase = true)) {
            if (converted.contains("मैं") || converted.contains("me")) {
                converted = converted.replace("मैं", "अपुन").replace("me", "अपुन")
                subs.add(SlangSubstitution("मैं", "अपुन", "Me / Myself", "Mumbai Tapori first person"))
            }
            if (converted.contains("दोस्त") || converted.contains("friend") || converted.contains("bhai")) {
                converted = converted.replace("दोस्त", "बंटाई").replace("friend", "बंटाई")
                subs.add(SlangSubstitution("दोस्त", "बंटाई", "Homie / Bro", "Gully rap and street address"))
            }
        }

        return SlangTranslationResult(
            sourceText = text,
            convertedText = converted,
            sourceDialect = source,
            targetDialect = target,
            explanation = "Converted between $source and $target focusing on vernacular idioms and colloquial speech cadence.",
            substitutedSlang = subs
        )
    }
}
