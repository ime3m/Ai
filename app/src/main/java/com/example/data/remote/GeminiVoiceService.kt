package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.PronunciationFeedback
import com.example.data.model.RegionalDialect
import com.example.data.model.RegionalExpression
import com.example.data.model.SlangSubstitution
import com.example.data.model.SlangTranslationResult
import com.example.data.model.SpeakingStylePreferenceEntity
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
        slangEnabled: Boolean
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackRegionalResponse(userMessage, dialect, regionalStrength, personality)
        }

        try {
            val systemPrompt = buildSystemPrompt(dialect, regionalStrength, personality, speakingStyle, slangEnabled)
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
        slangEnabled: Boolean
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
            You are a real, friendly native conversational partner from ${dialect.cityOrArea}, in ${dialect.region}, ${dialect.country}.
            Language: ${dialect.language}
            Dialect / Speaking Style: ${dialect.dialectName}
            
            DIALECT LEVEL: $strengthDescription
            PERSONALITY: ${personality.title} - ${personality.description}
            ${personality.systemPromptGuidance}
            
            $slangInstruction
            
            AUTOMATIC DIALECT & CODE-SWITCHING UNDERSTANDING:
            - The user may speak with local accents, slang, abbreviations, or mixed code-switching (${dialect.codeSwitchingDescription}).
            - Always understand the intended meaning from context even if grammar or spelling is colloquial.
            - Respond in ${dialect.dialectName} naturally matching the conversational rhythm.
            
            CONVERSATIONAL VOICE RULES:
            - This is a spoken voice conversation. Keep answers concise, natural, and conversational (1-3 sentences typically).
            - Avoid markdown headers, asterisks, bullet lists, or robotic greetings.
            - Speak directly as a human from ${dialect.cityOrArea}.
            
            $personalStyleSection
        """.trimIndent()
    }

    private fun cleanVoiceResponse(raw: String): String {
        return raw.replace(Regex("[*#_~`>]"), "")
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
        return when (dialect.id) {
            "ml_in_kl_kozhikode" -> when {
                lower.contains("സുഖ") || lower.contains("ഹലോ") || lower.contains("hello") ->
                    "സുഖം തന്നെ ചങ്ങായി! ഒരു സുലൈമാനി കുടിച്ചാലോ? നിങ്ങളുടെ വിശേഷങ്ങൾ പറയൂ."
                lower.contains("ഭക്ഷണം") || lower.contains("ബിരിയാണി") || lower.contains("tea") ->
                    "കോഴിക്കോട് ബിരിയാണിയും നൈസ് ആയിട്ടൊരു സുലൈമാനിയും കിട്ടിയാൽ പിന്നെ വേറെന്താ വേണ്ടത് ഓൻ!"
                else ->
                    "നല്ല കാര്യാണ് ചങ്ങായി പറഞ്ഞത്! അതങ്ങ് ഏറ്റു, കൂടുതൽ വിശേഷങ്ങൾ പറയൂ."
            }
            "ml_in_kl_thrissur" -> when {
                lower.contains("സുഖ") || lower.contains("ഹലോ") || lower.contains("hello") ->
                    "പിന്നെന്തൂട്ടാ ഗഡീ വിശേഷം! തകർപ്പൻ മൂഡിലാണല്ലോ നമ്മൾ."
                else ->
                    "എന്തൂട്ടാ ഗഡീ സംഭവം! കേൾക്കാൻ നല്ല രസണ്ട് ട്ടോ, ബാക്കി കൂടി പറയൂ."
            }
            "en_gb_eng_liverpool" -> when {
                lower.contains("hello") || lower.contains("hi") || lower.contains("how are") ->
                    "Alright kidda! You sound? Proper good to have a chinwag with ya today, lad."
                lower.contains("food") || lower.contains("eat") || lower.contains("hungry") ->
                    "Starving here too, mate! Fancy nipping down for some proper scran?"
                else ->
                    "Boss that, lad! Straight facts. Tell us more about it!"
            }
            "en_us_ny_brooklyn" -> when {
                lower.contains("hello") || lower.contains("hi") || lower.contains("how are") ->
                    "Yo, what's good! Deadass happy to talk to you. How's the borough treating you?"
                else ->
                    "Deadass, that's wild! You're speaking straight facts right now."
            }
            "ar_kw_kuwait" -> when {
                lower.contains("مرحبا") || lower.contains("شلونك") || lower.contains("hello") ->
                    "هلا والله يا معود! عساك طيب وبخير؟ حياك الله في ديوانيتنا."
                else ->
                    "والله كلامك وايد زين وما تقصر يا خوي، تسلم والله."
            }
            "es_es_andalusia_seville" -> when {
                lower.contains("hola") || lower.contains("hello") || lower.contains("qué tal") ->
                    "¡Qué pasa quillo! Qué alegría más grande verte por aquí, miarma."
                else ->
                    "¡No ni ná! Tienes más arte que nadie, cuéntame más cosas."
            }
            "hi_in_mh_mumbai" -> when {
                lower.contains("hello") || lower.contains("kya") || lower.contains("kaise") ->
                    "अरे क्या बोलते बंटाई! सब एकदम झकास? अपुन एकदम रेडी है बात करने को."
                else ->
                    "एक नंबर बात बोला भाई! अपुन को पूरा समझ आया, लोड नहीं लेने का!"
            }
            else -> {
                "${dialect.greeting} I hear you loud and clear in our ${dialect.dialectName} rhythm!"
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
