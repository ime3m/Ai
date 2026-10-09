package com.example.data.image

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.data.ai.AIApiKeyProvider
import com.example.data.model.RegionalDialect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

sealed class ImageServiceResult {
    data class Success(
        val imagePath: String? = null,
        val textResponse: String = "",
        val isImageGenerated: Boolean = false,
        val promptUsed: String = ""
    ) : ImageServiceResult()

    data class Error(
        val errorTitle: String = "Image generation unavailable",
        val errorMessage: String,
        val technicalDetails: String? = null,
        val isQuotaError: Boolean = false,
        val isNetworkError: Boolean = false,
        val canRetry: Boolean = true
    ) : ImageServiceResult()
}

/**
 * Clean Image & Multimodal AI Service.
 *
 * Integrates with Google Generative AI / Gemini backend:
 * - Image Generation via `gemini-2.5-flash-image`, `gemini-3.1-flash-image-preview`
 * - Multimodal Vision / Image Understanding via `gemini-3.5-flash` and `gemini-2.5-flash-lite`
 * - Image-to-Image editing / transformation
 * - Secure API key access via AIApiKeyProvider (BuildConfig or custom key in Settings)
 * - Single-request lock to prevent duplicate concurrent submissions
 * - Production quota error diagnosis (identifies free-tier zero quota limit vs rate limits)
 */
class ImageGenerationService(
    private val context: Context,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // Primary image generation candidate models in order of preference
    private val imageGenerationModels = listOf(
        "gemini-2.5-flash-image",
        "gemini-3.1-flash-image-preview",
        "gemini-3.1-flash-image",
        "gemini-3.1-flash-lite-image"
    )

    // Supported multimodal vision models for photo understanding
    private val visionModels = listOf(
        "gemini-3.5-flash",
        "gemini-2.5-flash-lite"
    )

    // In-flight mutex to prevent duplicate simultaneous generation requests
    private val generationMutex = Mutex()

    private fun getApiKey(): String {
        return AIApiKeyProvider.getApiKey(context)
    }

    /**
     * Sanitizes strings (error bodies, URLs, exception traces) to guarantee
     * no secrets, API keys, or credentials are exposed in logs or UI.
     */
    private fun sanitizeSecrets(input: String?, secret: String?): String {
        if (input == null) return ""
        var sanitized = input
        if (!secret.isNullOrBlank() && secret.length >= 8) {
            sanitized = sanitized.replace(secret, "[REDACTED_API_KEY]")
        }
        // General regex to scrub any key=AIza... or AIza... or similar keys
        sanitized = sanitized.replace(Regex("(?i)key=([a-zA-Z0-9_-]{12,})"), "key=[REDACTED]")
        sanitized = sanitized.replace(Regex("AIza[0-9A-Za-z-_]{35}"), "[REDACTED_API_KEY]")
        return sanitized
    }

    /**
     * Parsed quota error representation
     */
    private data class ParsedQuotaError(
        val status: String,
        val message: String,
        val quotaId: String?,
        val quotaMetric: String?,
        val limit: String?,
        val retryDelay: String?,
        val isFreeTierZeroLimit: Boolean
    )

    private fun parseQuotaError(responseBody: String, apiKey: String): ParsedQuotaError {
        val sanitized = sanitizeSecrets(responseBody, apiKey)
        var status = "RESOURCE_EXHAUSTED"
        var message = "You exceeded your current quota."
        var quotaId: String? = null
        var quotaMetric: String? = null
        var limit: String? = null
        var retryDelay: String? = null
        var isFreeTierZero = sanitized.contains("limit: 0", ignoreCase = true) ||
                sanitized.contains("FreeTier", ignoreCase = true)

        try {
            val root = JSONObject(responseBody)
            val errObj = root.optJSONObject("error")
            if (errObj != null) {
                status = errObj.optString("status", status)
                message = sanitizeSecrets(errObj.optString("message", message), apiKey)

                val details = errObj.optJSONArray("details")
                if (details != null) {
                    for (i in 0 until details.length()) {
                        val d = details.optJSONObject(i) ?: continue
                        val type = d.optString("@type")
                        if (type.contains("QuotaFailure")) {
                            val violations = d.optJSONArray("violations")
                            if (violations != null && violations.length() > 0) {
                                val firstV = violations.getJSONObject(0)
                                quotaMetric = firstV.optString("quotaMetric")
                                quotaId = firstV.optString("quotaId")
                            }
                        } else if (type.contains("RetryInfo")) {
                            retryDelay = d.optString("retryDelay")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("ImageGenerationService", "Error parsing quota JSON details", e)
        }

        if (sanitized.contains("limit: 0")) {
            limit = "0"
            isFreeTierZero = true
        }

        return ParsedQuotaError(
            status = status,
            message = message,
            quotaId = quotaId,
            quotaMetric = quotaMetric,
            limit = limit,
            retryDelay = retryDelay,
            isFreeTierZeroLimit = isFreeTierZero
        )
    }

    /**
     * Generates a new image from a text prompt.
     * Prevents duplicate requests with a lock and handles quota limits gracefully.
     */
    suspend fun generateImage(
        prompt: String,
        dialect: RegionalDialect
    ): ImageServiceResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            Log.e("ImageGenerationService", "API key missing or empty")
            return@withContext ImageServiceResult.Error(
                errorTitle = "Image generation unavailable",
                errorMessage = "Image generation isn't available with the current AI configuration.",
                technicalDetails = "API key is not configured. Add GEMINI_API_KEY in AI Studio or enter your key in app Settings.",
                canRetry = false
            )
        }

        // Acquire lock to guarantee one request per user action
        if (!generationMutex.tryLock()) {
            Log.w("ImageGenerationService", "Duplicate image generation request dropped; request already running.")
            return@withContext ImageServiceResult.Error(
                errorTitle = "Request in progress",
                errorMessage = "An image request is already processing. Please wait...",
                canRetry = false
            )
        }

        try {
            val visualPrompt = ImageIntentDetector.extractImagePrompt(prompt)
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray()
                    partsArray.put(JSONObject().apply {
                        put("text", visualPrompt)
                    })
                    put("parts", partsArray)
                }
                contentsArray.put(contentObj)
                put("contents", contentsArray)

                val configObj = JSONObject().apply {
                    val modalities = JSONArray().apply {
                        put("TEXT")
                        put("IMAGE")
                    }
                    put("responseModalities", modalities)
                }
                put("generationConfig", configObj)
            }

            var lastTechnicalError: String? = null
            var isQuotaExceeded = false

            for (model in imageGenerationModels) {
                try {
                    // Send API key via x-goog-api-key header to prevent exposing key in URL
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("x-goog-api-key", apiKey)
                        .addHeader("Content-Type", "application/json")
                        .post(requestJson.toString().toRequestBody(jsonMediaType))
                        .build()

                    Log.d("ImageGenerationService", "Sending image generation request for prompt to model: $model")
                    val response = okHttpClient.newCall(request).execute()
                    val responseBodyStr = response.body?.string() ?: ""

                    if (response.isSuccessful && responseBodyStr.isNotBlank()) {
                        val root = JSONObject(responseBodyStr)
                        val candidates = root.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val firstCandidate = candidates.getJSONObject(0)
                            val content = firstCandidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")

                            var savedImagePath: String? = null
                            val textBuilder = StringBuilder()

                            if (parts != null) {
                                for (i in 0 until parts.length()) {
                                    val part = parts.getJSONObject(i)
                                    val inlineData = part.optJSONObject("inlineData") ?: part.optJSONObject("inline_data")
                                    if (inlineData != null) {
                                        val base64Data = inlineData.optString("data")
                                        val mimeType = inlineData.optString("mimeType", inlineData.optString("mime_type", "image/png"))
                                        if (base64Data.isNotBlank()) {
                                            savedImagePath = ImageStorageManager.saveGeneratedImageLocally(context, base64Data, mimeType)
                                        }
                                    }

                                    val textPart = part.optString("text")
                                    if (textPart.isNotBlank()) {
                                        textBuilder.append(textPart)
                                    }
                                }
                            }

                            if (savedImagePath != null) {
                                val companionText = if (textBuilder.isNotBlank()) {
                                    textBuilder.toString().trim()
                                } else {
                                    when (dialect.language.lowercase()) {
                                        "malayalam" -> "നിങ്ങൾ ആവശ്യപ്പെട്ട ചിത്രം ഇതാ തയ്യാറാണ്! 🎨"
                                        "tamil" -> "நீங்கள் கேட்ட படம் இதோ தயாராக உள்ளது! 🎨"
                                        "hindi" -> "आपकी बनाई गई तस्वीर यहाँ तैयार है! 🎨"
                                        else -> "Here is the image you requested! 🎨"
                                    }
                                }

                                return@withContext ImageServiceResult.Success(
                                    imagePath = savedImagePath,
                                    textResponse = companionText,
                                    isImageGenerated = true,
                                    promptUsed = visualPrompt
                                )
                            }
                        }
                    } else if (response.code == 429) {
                        isQuotaExceeded = true
                        val quotaDetails = parseQuotaError(responseBodyStr, apiKey)

                        // Log specific API error details safely without leaking credentials
                        Log.w(
                            "ImageGenerationService",
                            "Model $model HTTP 429 Quota Exhausted: " +
                                    "status=${quotaDetails.status}, " +
                                    "quotaId=${quotaDetails.quotaId ?: "none"}, " +
                                    "metric=${quotaDetails.quotaMetric ?: "none"}, " +
                                    "limit=${quotaDetails.limit ?: "unspecified"}, " +
                                    "isFreeTierZero=${quotaDetails.isFreeTierZeroLimit}"
                        )

                        lastTechnicalError = if (quotaDetails.isFreeTierZeroLimit) {
                            "HTTP 429 RESOURCE_EXHAUSTED ($model)\n" +
                            "• Metric: ${quotaDetails.quotaMetric ?: "generate_content_free_tier_requests"}\n" +
                            "• Policy: ${quotaDetails.quotaId ?: "GenerateRequestsPerDayPerProjectPerModel-FreeTier"}\n" +
                            "• Daily Limit: 0 (Free Tier does not include image generation quota)\n" +
                            "• Requirement: Requires a Google AI Studio pay-as-you-go project key (configurable in Settings)."
                        } else {
                            "HTTP 429 RATE_LIMIT_EXCEEDED ($model)\n" +
                            "• Details: ${quotaDetails.message}"
                        }
                        // Stop checking subsequent models if the project has 0 quota
                        break
                    } else {
                        val sanitizedErr = sanitizeSecrets(responseBodyStr, apiKey)
                        Log.w("ImageGenerationService", "HTTP ${response.code} on $model: $sanitizedErr")
                        lastTechnicalError = "HTTP ${response.code} ($model): $sanitizedErr"
                    }
                } catch (e: java.net.UnknownHostException) {
                    return@withContext ImageServiceResult.Error(
                        errorTitle = "Network connection required",
                        errorMessage = "Image generation needs an internet connection. Please check your network.",
                        technicalDetails = "UnknownHostException: Unable to resolve generativelanguage.googleapis.com",
                        isNetworkError = true,
                        canRetry = true
                    )
                } catch (e: Exception) {
                    val sanitizedEx = sanitizeSecrets(e.localizedMessage ?: e.message, apiKey)
                    Log.w("ImageGenerationService", "Model $model request failed: $sanitizedEx")
                    lastTechnicalError = sanitizedEx
                }
            }

            if (isQuotaExceeded) {
                return@withContext ImageServiceResult.Error(
                    errorTitle = "Image generation unavailable",
                    errorMessage = "The image service has reached its current API limit.",
                    technicalDetails = lastTechnicalError ?: "HTTP 429 RESOURCE_EXHAUSTED: Daily free-tier image limit is 0 for this project.",
                    isQuotaError = true,
                    canRetry = true
                )
            }

            return@withContext ImageServiceResult.Error(
                errorTitle = "Image generation unavailable",
                errorMessage = "Image generation isn't available with the current AI configuration.",
                technicalDetails = lastTechnicalError ?: "Selected image generation models are not enabled for this key.",
                canRetry = true
            )
        } finally {
            generationMutex.unlock()
        }
    }

    /**
     * Multimodal Image Understanding / Vision Analysis.
     * Uses multimodal Gemini models (gemini-3.5-flash / gemini-2.5-flash-lite).
     */
    suspend fun analyzeImage(
        imagePath: String,
        userPrompt: String,
        dialect: RegionalDialect
    ): ImageServiceResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext ImageServiceResult.Error(
                errorTitle = "Vision AI unavailable",
                errorMessage = "Gemini API key is not configured.",
                technicalDetails = "Missing GEMINI_API_KEY.",
                canRetry = false
            )
        }

        val base64Pair = ImageStorageManager.getBase64ImageData(context, imagePath)
            ?: return@withContext ImageServiceResult.Error(
                errorTitle = "Image error",
                errorMessage = "Could not process attached image.",
                canRetry = false
            )

        val promptText = if (userPrompt.isNotBlank()) userPrompt else "Describe what you see in this image in detail."

        val systemInstruction = """
            You are a helpful AI assistant speaking in ${dialect.dialectName} (${dialect.cityOrArea}, ${dialect.region}).
            Communicate naturally in ${dialect.language} with a friendly, regional tone.
            Analyze the attached image and answer the user's question accurately.
        """.trimIndent()

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray()
            val contentObj = JSONObject().apply {
                val partsArray = JSONArray()
                partsArray.put(JSONObject().apply {
                    put("text", "$systemInstruction\n\nUser Question: $promptText")
                })
                partsArray.put(JSONObject().apply {
                    put("inline_data", JSONObject().apply {
                        put("mime_type", base64Pair.second)
                        put("data", base64Pair.first)
                    })
                })
                put("parts", partsArray)
            }
            contentsArray.put(contentObj)
            put("contents", contentsArray)
        }

        for (model in visionModels) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("x-goog-api-key", apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBodyStr = response.body?.string() ?: ""

                if (response.isSuccessful && responseBodyStr.isNotBlank()) {
                    val root = JSONObject(responseBodyStr)
                    val text = root.optJSONArray("candidates")?.optJSONObject(0)
                        ?.optJSONObject("content")?.optJSONArray("parts")
                        ?.optJSONObject(0)?.optString("text") ?: ""

                    if (text.isNotBlank()) {
                        return@withContext ImageServiceResult.Success(
                            imagePath = imagePath,
                            textResponse = text.trim(),
                            isImageGenerated = false,
                            promptUsed = promptText
                        )
                    }
                } else {
                    val sanitizedErr = sanitizeSecrets(responseBodyStr, apiKey)
                    Log.w("ImageGenerationService", "Vision error on model $model: HTTP ${response.code} $sanitizedErr")
                }
            } catch (e: java.net.UnknownHostException) {
                return@withContext ImageServiceResult.Error(
                    errorTitle = "Network connection required",
                    errorMessage = "Image analysis needs an internet connection.",
                    isNetworkError = true,
                    canRetry = true
                )
            } catch (e: Exception) {
                val sanitizedEx = sanitizeSecrets(e.localizedMessage ?: e.message, apiKey)
                Log.e("ImageGenerationService", "Vision analysis error with $model: $sanitizedEx")
            }
        }

        ImageServiceResult.Error(
            errorTitle = "Vision AI error",
            errorMessage = "Could not analyze the image. Please try again.",
            canRetry = true
        )
    }

    /**
     * Image-to-Image editing / transformation.
     */
    suspend fun editImage(
        imagePath: String,
        transformationPrompt: String,
        dialect: RegionalDialect
    ): ImageServiceResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext ImageServiceResult.Error(
                errorTitle = "Image editing unavailable",
                errorMessage = "Gemini API key is not configured.",
                canRetry = false
            )
        }

        val base64Pair = ImageStorageManager.getBase64ImageData(context, imagePath)
            ?: return@withContext ImageServiceResult.Error(
                errorTitle = "Image error",
                errorMessage = "Could not read original image for editing.",
                canRetry = false
            )

        val visualPrompt = ImageIntentDetector.extractImagePrompt(transformationPrompt)
        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray()
            val contentObj = JSONObject().apply {
                val partsArray = JSONArray()
                partsArray.put(JSONObject().apply {
                    put("text", "Transform and edit this image based on the following instructions: $visualPrompt")
                })
                partsArray.put(JSONObject().apply {
                    put("inline_data", JSONObject().apply {
                        put("mime_type", base64Pair.second)
                        put("data", base64Pair.first)
                    })
                })
                put("parts", partsArray)
            }
            contentsArray.put(contentObj)
            put("contents", contentsArray)

            val configObj = JSONObject().apply {
                val modalities = JSONArray().apply {
                    put("TEXT")
                    put("IMAGE")
                }
                put("responseModalities", modalities)
            }
            put("generationConfig", configObj)
        }

        for (model in imageGenerationModels) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("x-goog-api-key", apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBodyStr = response.body?.string() ?: ""

                if (response.isSuccessful && responseBodyStr.isNotBlank()) {
                    val root = JSONObject(responseBodyStr)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val parts = firstCandidate.optJSONObject("content")?.optJSONArray("parts")

                        var savedImagePath: String? = null
                        val textBuilder = StringBuilder()

                        if (parts != null) {
                            for (i in 0 until parts.length()) {
                                val part = parts.getJSONObject(i)
                                val inlineData = part.optJSONObject("inlineData") ?: part.optJSONObject("inline_data")
                                if (inlineData != null) {
                                    val base64Data = inlineData.optString("data")
                                    val mimeType = inlineData.optString("mimeType", inlineData.optString("mime_type", "image/png"))
                                    if (base64Data.isNotBlank()) {
                                        savedImagePath = ImageStorageManager.saveGeneratedImageLocally(context, base64Data, mimeType)
                                    }
                                }

                                val textPart = part.optString("text")
                                if (textPart.isNotBlank()) {
                                    textBuilder.append(textPart)
                                }
                            }
                        }

                        if (savedImagePath != null) {
                            return@withContext ImageServiceResult.Success(
                                imagePath = savedImagePath,
                                textResponse = if (textBuilder.isNotBlank()) textBuilder.toString().trim() else "Here is your edited image! 🎨",
                                isImageGenerated = true,
                                promptUsed = visualPrompt
                            )
                        }
                    }
                } else if (response.code == 429) {
                    val quotaDetails = parseQuotaError(responseBodyStr, apiKey)
                    Log.w("ImageGenerationService", "Edit image HTTP 429: ${quotaDetails.status} ${quotaDetails.quotaId}")
                    return@withContext ImageServiceResult.Error(
                        errorTitle = "Image editing unavailable",
                        errorMessage = "The image service has reached its current API limit.",
                        technicalDetails = "HTTP 429 RESOURCE_EXHAUSTED ($model): Free tier daily quota limit is 0.",
                        isQuotaError = true,
                        canRetry = true
                    )
                }
            } catch (e: Exception) {
                val sanitizedEx = sanitizeSecrets(e.localizedMessage ?: e.message, apiKey)
                Log.e("ImageGenerationService", "Edit image error on $model: $sanitizedEx")
            }
        }

        ImageServiceResult.Error(
            errorTitle = "Image editing unavailable",
            errorMessage = "Image editing isn't available with the current AI configuration.",
            technicalDetails = "HTTP error on image editing endpoints.",
            canRetry = true
        )
    }
}
