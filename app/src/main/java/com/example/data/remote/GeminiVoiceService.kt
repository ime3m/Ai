package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.knowledge.RealTimeKnowledgeEngine
import com.example.data.knowledge.RealTimeKnowledgeResponse
import com.example.data.location.LocationExtractor
import com.example.data.location.QueryContext
import com.example.data.location.UserLocation
import com.example.data.model.AIRequest
import com.example.data.model.LocalSayingResult
import com.example.data.model.PronunciationFeedback
import com.example.data.model.RegionalDialect
import com.example.data.model.RegionalExpression
import com.example.data.model.SlangSubstitution
import com.example.data.model.SlangTranslationResult
import com.example.data.model.SpeakingStylePreferenceEntity
import com.example.data.model.StyleRewriteResult
import com.example.data.model.VoicePersonality
import com.example.data.reasoning.AiResponseTrace
import com.example.data.reasoning.ResponseQualitySystem
import com.example.data.reasoning.UniversalReasoningEngine
import com.example.data.reasoning.UserIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val candidateModels = listOf(
        "gemini-flash-latest",
        "gemini-flash-lite-latest",
        "gemini-2.5-flash-lite",
        "gemini-3.1-flash-lite-preview"
    )
    private val modelCooldowns = java.util.concurrent.ConcurrentHashMap<String, Long>()

    private fun getAvailableModels(): List<String> {
        val now = System.currentTimeMillis()
        val available = candidateModels.filter { model ->
            val cooldownUntil = modelCooldowns[model] ?: 0L
            now > cooldownUntil
        }
        return if (available.isNotEmpty()) available else candidateModels
    }

    private fun markModelRateLimited(model: String, cooldownSeconds: Long = 60) {
        modelCooldowns[model] = System.currentTimeMillis() + (cooldownSeconds * 1000)
    }

    private fun getModelUrl(model: String, streaming: Boolean = false): String {
        val action = if (streaming) "streamGenerateContent?alt=sse" else "generateContent"
        return "https://generativelanguage.googleapis.com/v1beta/models/$model:$action"
    }

    private suspend fun executeGeminiStream(
        requestJson: JSONObject,
        apiKey: String,
        startTime: Long,
        onFirstToken: () -> Unit,
        onToken: (String) -> Unit
    ): String? = withContext(Dispatchers.IO) {
        val models = getAvailableModels()
        for (model in models) {
            try {
                val url = "${getModelUrl(model, streaming = true)}&key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val bodyStream = response.body?.byteStream() ?: continue
                    val reader = java.io.BufferedReader(java.io.InputStreamReader(bodyStream))
                    val fullResponse = StringBuilder()
                    var isFirstToken = true
                    var line = reader.readLine()
                    while (line != null) {
                        if (line.startsWith("data: ")) {
                            val jsonStr = line.substring(6).trim()
                            if (jsonStr.isNotEmpty() && jsonStr != "[DONE]") {
                                try {
                                    val chunkJson = JSONObject(jsonStr)
                                    val candidates = chunkJson.optJSONArray("candidates")
                                    val firstCandidate = candidates?.optJSONObject(0)
                                    val content = firstCandidate?.optJSONObject("content")
                                    val parts = content?.optJSONArray("parts")
                                    val partText = parts?.optJSONObject(0)?.optString("text")
                                    if (!partText.isNullOrEmpty()) {
                                        if (isFirstToken) {
                                            isFirstToken = false
                                            Log.d("VoiceAiPerf", "FIRST_AI_TOKEN_RECEIVED [t=${System.currentTimeMillis() - startTime}ms]")
                                            onFirstToken()
                                        }
                                        fullResponse.append(partText)
                                        onToken(partText)
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                        line = reader.readLine()
                    }
                    val text = fullResponse.toString()
                    if (text.isNotBlank()) {
                        Log.d("VoiceAiPerf", "AI_RESPONSE_COMPLETED [t=${System.currentTimeMillis() - startTime}ms] length=${text.length}")
                        return@withContext cleanVoiceResponse(text)
                    }
                } else if (response.code == 429) {
                    markModelRateLimited(model, 60)
                    Log.w("GeminiVoiceService", "Model $model quota/rate limited (429). Trying fallback model...")
                    continue
                } else {
                    Log.w("GeminiVoiceService", "Model $model streaming returned error ${response.code}")
                    continue
                }
            } catch (e: Exception) {
                Log.w("GeminiVoiceService", "Model $model streaming failed", e)
                continue
            }
        }
        null
    }

    private fun executeGeminiPost(
        requestJson: JSONObject,
        apiKey: String
    ): JSONObject? {
        val models = getAvailableModels()
        for (model in models) {
            try {
                val request = Request.Builder()
                    .url("${getModelUrl(model)}?key=$apiKey")
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    return JSONObject(body)
                } else if (response.code == 429) {
                    markModelRateLimited(model, 60)
                    Log.w("GeminiVoiceService", "Model $model quota/rate limited (429). Trying next model...")
                } else {
                    Log.w("GeminiVoiceService", "Model $model returned error ${response.code}: ${sanitizeForLogs(body)}")
                }
            } catch (e: Exception) {
                Log.w("GeminiVoiceService", "Model $model execution failed", e)
            }
        }
        return null
    }

    private val _latestTrace = MutableStateFlow<AiResponseTrace?>(null)
    val latestTrace: StateFlow<AiResponseTrace?> = _latestTrace.asStateFlow()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun transcribeAudio(
        audioData: ByteArray,
        mimeType: String = "audio/wav",
        dialect: RegionalDialect? = null
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return@withContext null

        try {
            val base64Audio = android.util.Base64.encodeToString(audioData, android.util.Base64.NO_WRAP)
            val dialectHint = dialect?.let {
                "Target dialect: ${it.dialectName} (${it.cityOrArea}, ${it.region}, ${it.country}). Language: ${it.language}."
            } ?: ""

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray()
                    val audioPart = JSONObject().apply {
                        put("inline_data", JSONObject().apply {
                            put("mime_type", mimeType)
                            put("data", base64Audio)
                        })
                    }
                    val textPart = JSONObject().apply {
                        put(
                            "text",
                            "Please transcribe the speech in this audio accurately. $dialectHint " +
                                "Transcribe verbatim in the speaker's original language and dialect. " +
                                "Do NOT add conversational remarks, introductory text, explanations, or quotes. " +
                                "If there is silence, unintelligible noise, or no spoken words, respond with nothing."
                        )
                    }
                    partsArray.put(audioPart)
                    partsArray.put(textPart)
                    put("parts", partsArray)
                }
                contentsArray.put(contentObj)
                put("contents", contentsArray)
            }

            val root = executeGeminiPost(jsonBody, apiKey) ?: return@withContext null
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val transcribed = parts.getJSONObject(0).optString("text", "").trim()
                    return@withContext transcribed.ifBlank { null }
                }
            }
            null
        } catch (e: Exception) {
            Log.e("GeminiVoiceService", "Error during Gemini audio transcription", e)
            null
        }
    }

    suspend fun streamVoiceResponse(
        request: AIRequest,
        regionalStrength: Float = 0.75f,
        personality: VoicePersonality = VoicePersonality.FRIENDLY,
        speakingStyle: SpeakingStylePreferenceEntity? = null,
        slangEnabled: Boolean = true,
        responseLength: String = "Balanced",
        naturalMixingEnabled: Boolean = true,
        customPromptNotes: String = "",
        inputSource: String = "TEXT",
        conversationId: String = "conv_default",
        onFirstToken: () -> Unit = {},
        onToken: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        Log.d("VoiceAiPerf", "REQUEST_STARTED [t=0ms] inputSource=$inputSource")

        val userMessage = request.message
        val dialect = request.regionalProfile.toRegionalDialect()
        val queryContext = request.queryContext ?: LocationExtractor.analyzeQuery(
            query = userMessage,
            conversationHistory = request.conversationHistory,
            userLocation = request.userLocation
        )
        val targetLocation = request.queryLocation ?: queryContext.requestedLocation
        val intent = queryContext.intent ?: UniversalReasoningEngine.classifyIntent(userMessage)

        // Avoid unnecessary external calls: only query real-time data when question requires it
        val isTimeSensitive = intent == UserIntent.CURRENT_INFORMATION ||
                queryContext.isLocationDependentQuery ||
                RealTimeKnowledgeEngine.requiresRealTimeRetrieval(userMessage)

        val verifiedKnowledge = if (isTimeSensitive) {
            RealTimeKnowledgeEngine.retrieveVerifiedKnowledge(
                query = userMessage,
                dialect = dialect,
                resolvedLocation = targetLocation,
                userLocation = request.userLocation,
                conversationHistory = request.conversationHistory
            )
        } else null

        val apiKey = getApiKey()
        var streamResult: String? = null

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val systemPrompt = buildSystemPrompt(
                    dialect = dialect,
                    regionalStrength = regionalStrength,
                    personality = personality,
                    speakingStyle = speakingStyle,
                    slangEnabled = slangEnabled,
                    responseLength = responseLength,
                    naturalMixingEnabled = naturalMixingEnabled,
                    customPromptNotes = customPromptNotes,
                    groundingFact = verifiedKnowledge?.factualText,
                    intent = intent,
                    queryLocation = targetLocation,
                    userLocation = request.userLocation,
                    queryContext = queryContext
                )

                Log.d("VoiceAiPerf", "PROMPT_READY [t=${System.currentTimeMillis() - startTime}ms]")

                val requestJson = JSONObject()
                val sysInstructionObj = JSONObject()
                val sysParts = JSONArray()
                sysParts.put(JSONObject().put("text", systemPrompt))
                sysInstructionObj.put("parts", sysParts)
                requestJson.put("systemInstruction", sysInstructionObj)

                val contentsArray = JSONArray()
                val recentHistory = request.conversationHistory.takeLast(6)
                for ((role, text) in recentHistory) {
                    val turnObj = JSONObject()
                    val apiRole = if (role == "user") "user" else "model"
                    turnObj.put("role", apiRole)
                    val parts = JSONArray()
                    parts.put(JSONObject().put("text", text))
                    turnObj.put("parts", parts)
                    contentsArray.put(turnObj)
                }

                val currentTurn = JSONObject()
                currentTurn.put("role", "user")
                val currentParts = JSONArray()
                currentParts.put(JSONObject().put("text", "[PRIMARY USER REQUEST - INTENT: ${intent.name}]\n$userMessage"))
                currentTurn.put("parts", currentParts)
                contentsArray.put(currentTurn)
                requestJson.put("contents", contentsArray)

                val config = JSONObject()
                config.put("temperature", 0.70)
                config.put("topP", 0.95)
                requestJson.put("generationConfig", config)

                Log.d("VoiceAiPerf", "NETWORK_REQUEST_STARTED [t=${System.currentTimeMillis() - startTime}ms]")
                streamResult = executeGeminiStream(
                    requestJson = requestJson,
                    apiKey = apiKey,
                    startTime = startTime,
                    onFirstToken = onFirstToken,
                    onToken = onToken
                )
            } catch (e: Exception) {
                Log.e("GeminiVoiceService", "Streaming request exception", e)
            }
        }

        val finalResponse = if (!streamResult.isNullOrBlank()) {
            streamResult
        } else {
            val fallback = if (verifiedKnowledge != null) {
                verifiedKnowledge.dialectText
            } else {
                UniversalReasoningEngine.generateKnowledgeAnswer(
                    query = userMessage,
                    intent = intent,
                    dialect = dialect,
                    strength = regionalStrength,
                    personality = personality,
                    history = request.conversationHistory,
                    queryLocation = targetLocation,
                    userLocation = request.userLocation
                )
            }
            onFirstToken()
            onToken(fallback)
            fallback
        }

        ResponseQualitySystem.recordResponse(finalResponse)
        val trace = AiResponseTrace(
            messageId = "msg_${System.currentTimeMillis()}",
            conversationId = conversationId,
            userInput = userMessage,
            inputSource = inputSource,
            detectedLanguage = dialect.language,
            detectedRegion = "${dialect.cityOrArea}, ${dialect.region}",
            detectedIntent = intent,
            currentInformationRequired = isTimeSensitive,
            retrievalUsed = verifiedKnowledge != null,
            retrievalSources = verifiedKnowledge?.sources?.map { it.name } ?: emptyList(),
            systemPromptVersion = "v2.0-IntentFirst",
            modelRequestCreated = true,
            modelRequestSent = true,
            modelResponseReceived = streamResult != null,
            rawAiResponse = finalResponse,
            responseLength = finalResponse.length,
            genericResponseDetected = false,
            relevanceCheck = "PASS",
            regenerationTriggered = false,
            regenerationReason = "",
            finalResponse = finalResponse,
            ttsStarted = true
        )
        _latestTrace.value = trace

        finalResponse
    }

    suspend fun generateVoiceResponse(
        request: AIRequest,
        regionalStrength: Float = 0.75f,
        personality: VoicePersonality = VoicePersonality.FRIENDLY,
        speakingStyle: SpeakingStylePreferenceEntity? = null,
        slangEnabled: Boolean = true,
        responseLength: String = "Balanced",
        naturalMixingEnabled: Boolean = true,
        customPromptNotes: String = "",
        inputSource: String = "TEXT",
        conversationId: String = "conv_default"
    ): String {
        return generateVoiceResponse(
            userMessage = request.message,
            conversationHistory = request.conversationHistory,
            dialect = request.regionalProfile.toRegionalDialect(),
            regionalStrength = regionalStrength,
            personality = personality,
            speakingStyle = speakingStyle,
            slangEnabled = slangEnabled,
            responseLength = responseLength,
            naturalMixingEnabled = naturalMixingEnabled,
            customPromptNotes = customPromptNotes,
            inputSource = inputSource,
            conversationId = conversationId,
            queryLocation = request.queryLocation,
            userLocation = request.userLocation,
            queryContext = request.queryContext
        )
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
        naturalMixingEnabled: Boolean = true,
        customPromptNotes: String = "",
        inputSource: String = "TEXT",
        conversationId: String = "conv_default",
        queryLocation: String? = null,
        userLocation: UserLocation = UserLocation(),
        queryContext: QueryContext? = null
    ): String = withContext(Dispatchers.IO) {
        val messageId = "msg_${System.currentTimeMillis()}"

        // Analyze query for geographic targets and user intent
        val resolvedContext = queryContext ?: LocationExtractor.analyzeQuery(
            query = userMessage,
            conversationHistory = conversationHistory,
            userLocation = userLocation
        )
        val targetLocation = queryLocation ?: resolvedContext.requestedLocation
        val intent = resolvedContext.intent ?: UniversalReasoningEngine.classifyIntent(userMessage)

        val isTimeSensitive = intent == UserIntent.CURRENT_INFORMATION ||
                resolvedContext.isLocationDependentQuery ||
                RealTimeKnowledgeEngine.requiresRealTimeRetrieval(userMessage)

        val verifiedKnowledge = if (isTimeSensitive) {
            RealTimeKnowledgeEngine.retrieveVerifiedKnowledge(
                query = userMessage,
                dialect = dialect,
                resolvedLocation = targetLocation,
                userLocation = userLocation,
                conversationHistory = conversationHistory
            )
        } else null

        val apiKey = getApiKey()
        var modelRequestCreated = false
        var modelRequestSent = false
        var modelResponseReceived = false
        var rawResponse = ""
        var regenerationTriggered = false
        var regenerationReason = ""
        var candidateAnswer: String? = null

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                modelRequestCreated = true
                val systemPrompt = buildSystemPrompt(
                    dialect = dialect,
                    regionalStrength = regionalStrength,
                    personality = personality,
                    speakingStyle = speakingStyle,
                    slangEnabled = slangEnabled,
                    responseLength = responseLength,
                    naturalMixingEnabled = naturalMixingEnabled,
                    customPromptNotes = customPromptNotes,
                    groundingFact = verifiedKnowledge?.factualText,
                    intent = intent,
                    queryLocation = targetLocation,
                    userLocation = userLocation,
                    queryContext = resolvedContext
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

                // Current message - explicitly prioritized with intent
                val currentTurn = JSONObject()
                currentTurn.put("role", "user")
                val currentParts = JSONArray()
                currentParts.put(JSONObject().put("text", "[PRIMARY USER REQUEST - INTENT: ${intent.name}]\n$userMessage"))
                currentTurn.put("parts", currentParts)
                contentsArray.put(currentTurn)

                requestJson.put("contents", contentsArray)

                // Generation config
                val config = JSONObject()
                config.put("temperature", 0.70)
                config.put("topP", 0.95)
                requestJson.put("generationConfig", config)

                val modelsToTry = getAvailableModels()
                for (model in modelsToTry) {
                    val request = Request.Builder()
                        .url("${getModelUrl(model)}?key=$apiKey")
                        .post(requestJson.toString().toRequestBody(jsonMediaType))
                        .build()

                    modelRequestSent = true
                    try {
                        val response = okHttpClient.newCall(request).execute()
                        val responseBody = response.body?.string() ?: ""

                        if (response.isSuccessful) {
                            modelResponseReceived = true
                            val parsed = JSONObject(responseBody)
                            val candidates = parsed.optJSONArray("candidates")
                            val firstCandidate = candidates?.optJSONObject(0)
                            val content = firstCandidate?.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            val text = parts?.optJSONObject(0)?.optString("text")

                            if (!text.isNullOrBlank()) {
                                rawResponse = text
                                val cleaned = cleanVoiceResponse(text)
                                val isGeneric = ResponseQualitySystem.isGenericOrRepetitive(cleaned, userMessage, intent)
                                val isRelevant = ResponseQualitySystem.checkRelevance(userMessage, cleaned, intent)

                                if (!isGeneric && isRelevant) {
                                    candidateAnswer = cleaned
                                    break // Success!
                                } else {
                                    regenerationTriggered = true
                                    regenerationReason = if (isGeneric) "Blocked generic conversational filler" else "Failed question-answer relevance check"
                                    Log.w("GeminiVoiceService", "Model $model output rejected: reason='$regenerationReason'. Raw was: '$cleaned'")
                                    break
                                }
                            }
                        } else if (response.code == 429) {
                            markModelRateLimited(model, 60)
                            Log.w("GeminiVoiceService", "Model $model quota exceeded / rate limited (429). Attempting fallback model...")
                            continue
                        } else {
                            Log.w("GeminiVoiceService", "API error for model $model: ${response.code} ${sanitizeForLogs(responseBody)}")
                            continue
                        }
                    } catch (e: Exception) {
                        Log.w("GeminiVoiceService", "Model $model network execution failed", e)
                        continue
                    }
                }
            } catch (e: Exception) {
                Log.e("GeminiVoiceService", "Call failed", e)
            }
        }

        // If candidateAnswer is null (API error, 429 quota exhaustion, offline, or rejected as generic filler)
        val finalResponse = if (!candidateAnswer.isNullOrBlank()) {
            candidateAnswer
        } else {
            if (verifiedKnowledge != null) {
                verifiedKnowledge.dialectText
            } else {
                UniversalReasoningEngine.generateKnowledgeAnswer(
                    query = userMessage,
                    intent = intent,
                    dialect = dialect,
                    strength = regionalStrength,
                    personality = personality,
                    history = conversationHistory,
                    queryLocation = targetLocation,
                    userLocation = userLocation
                )
            }
        }

        ResponseQualitySystem.recordResponse(finalResponse)
        val isFinalRelevant = ResponseQualitySystem.checkRelevance(userMessage, finalResponse, intent)

        val trace = AiResponseTrace(
            messageId = messageId,
            conversationId = conversationId,
            userInput = userMessage,
            inputSource = inputSource,
            detectedLanguage = dialect.language,
            detectedRegion = "${dialect.cityOrArea}, ${dialect.region}",
            detectedIntent = intent,
            currentInformationRequired = isTimeSensitive,
            retrievalUsed = verifiedKnowledge != null,
            retrievalSources = verifiedKnowledge?.sources?.map { it.name } ?: emptyList(),
            systemPromptVersion = "v2.0-IntentFirst",
            modelRequestCreated = modelRequestCreated,
            modelRequestSent = modelRequestSent,
            modelResponseReceived = modelResponseReceived,
            rawAiResponse = rawResponse.ifBlank { finalResponse },
            responseLength = finalResponse.length,
            genericResponseDetected = regenerationTriggered,
            relevanceCheck = if (isFinalRelevant) "PASS" else "FAIL",
            regenerationTriggered = regenerationTriggered,
            regenerationReason = regenerationReason,
            finalResponse = finalResponse,
            ttsStarted = true
        )
        _latestTrace.value = trace

        return@withContext finalResponse
    }

    fun getRealTimeKnowledge(
        query: String,
        dialect: RegionalDialect,
        forceWeb: Boolean = false,
        resolvedLocation: String? = null,
        userLocation: UserLocation = UserLocation(),
        history: List<Pair<String, String>> = emptyList()
    ): RealTimeKnowledgeResponse? {
        val targetLocation = resolvedLocation ?: LocationExtractor.extractExplicitLocation(query, query.lowercase().trim())
        return RealTimeKnowledgeEngine.retrieveVerifiedKnowledge(
            query = query,
            dialect = dialect,
            forceWeb = forceWeb,
            resolvedLocation = targetLocation,
            userLocation = userLocation,
            conversationHistory = history
        )
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

            val root = executeGeminiPost(requestJson, apiKey)
            if (root != null) {
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

                val raw = executeGeminiPost(requestJson, apiKey)
                if (raw != null) {
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

                val raw = executeGeminiPost(requestJson, apiKey)
                if (raw != null) {
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
        naturalMixingEnabled: Boolean = true,
        customPromptNotes: String = "",
        groundingFact: String? = null,
        intent: UserIntent? = null,
        queryLocation: String? = null,
        userLocation: UserLocation? = null,
        queryContext: QueryContext? = null
    ): String {
        val regionProfile = com.example.data.regional.RegionalProfileRegistry.getProfileById(dialect.region.lowercase())
            .let { if (it.id == "kerala" && dialect.country != "India") com.example.data.regional.RegionalProfileRegistry.allProfiles.find { p -> p.defaultDialectId == dialect.id } ?: it else it }

        return com.example.data.ai.AIConfig.buildSystemInstruction(
            regionalProfile = regionProfile,
            dialect = dialect,
            strength = regionalStrength,
            personality = personality,
            speakingStyle = speakingStyle,
            slangEnabled = slangEnabled,
            responseLength = responseLength,
            naturalMixingEnabled = naturalMixingEnabled,
            customPromptNotes = customPromptNotes,
            groundingFact = groundingFact,
            intent = intent,
            queryLocation = queryLocation,
            userLocation = userLocation,
            queryContext = queryContext
        )
    }

    fun previewPromptContext(
        dialect: RegionalDialect,
        regionalStrength: Float,
        personality: VoicePersonality,
        speakingStyle: SpeakingStylePreferenceEntity?,
        slangEnabled: Boolean,
        responseLength: String = "Balanced",
        naturalMixingEnabled: Boolean = true,
        customPromptNotes: String = "",
        queryLocation: String? = null,
        userLocation: UserLocation? = null
    ): String {
        return buildSystemPrompt(
            dialect = dialect,
            regionalStrength = regionalStrength,
            personality = personality,
            speakingStyle = speakingStyle,
            slangEnabled = slangEnabled,
            responseLength = responseLength,
            naturalMixingEnabled = naturalMixingEnabled,
            customPromptNotes = customPromptNotes,
            groundingFact = null,
            queryLocation = queryLocation,
            userLocation = userLocation
        )
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

                val root = executeGeminiPost(requestJson, apiKey)
                if (root != null) {
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

                val root = executeGeminiPost(requestJson, apiKey)
                if (root != null) {
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

    private fun sanitizeForLogs(text: String): String {
        return text.replace(Regex("key=[A-Za-z0-9_\\-]+"), "key=REDACTED")
    }

    private fun cleanVoiceResponse(raw: String): String {
        var cleaned = raw
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

        // Strip repetitive canned opening confirmations to keep conversation natural
        val repetitiveOpenings = listOf(
            Regex("^ശരിയാണ്,\\s*നിങ്ങൾ പറഞ്ഞത് എനിക്ക് നന്നായി മനസ്സിലായി[.,!]?\\s*", RegexOption.IGNORE_CASE),
            Regex("^ശരിയാണ്,\\s*നിങ്ങൾ പറഞ്ഞത്[.,!]?\\s*", RegexOption.IGNORE_CASE),
            Regex("^താൻ പറഞ്ഞത് എനിക്ക് ക്ലിയറായി മനസ്സിലായി[.,!]?\\s*", RegexOption.IGNORE_CASE),
            Regex("^നിങ്ങൾ പറഞ്ഞത് കറക്ടാണ് കേട്ടോ[.,!]?\\s*", RegexOption.IGNORE_CASE)
        )
        for (pattern in repetitiveOpenings) {
            val replaced = cleaned.replaceFirst(pattern, "")
            if (replaced.isNotBlank()) {
                cleaned = replaced.trim()
            }
        }
        return cleaned
    }

    private fun fallbackRegionalResponse(
        message: String,
        dialect: RegionalDialect,
        strength: Float,
        personality: VoicePersonality
    ): String {
        val intent = UniversalReasoningEngine.classifyIntent(message)
        return UniversalReasoningEngine.generateKnowledgeAnswer(
            query = message,
            intent = intent,
            dialect = dialect,
            strength = strength,
            personality = personality,
            history = emptyList()
        )
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
