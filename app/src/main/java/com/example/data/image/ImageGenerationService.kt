package com.example.data.image

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.RegionalDialect
import kotlinx.coroutines.Dispatchers
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
        val errorMessage: String,
        val isNetworkError: Boolean = false,
        val canRetry: Boolean = true
    ) : ImageServiceResult()
}

/**
 * Clean Image & Multimodal AI Service.
 *
 * Integrates with the existing Google Generative AI / Gemini backend:
 * - Image Generation via `gemini-2.5-flash-image` and `gemini-3.1-flash-image-preview`
 * - Multimodal Vision / Image Understanding via `gemini-flash-latest` and `gemini-2.5-flash-lite`
 * - Image-to-Image editing / transformation
 * - Secure API key access via BuildConfig (AI Studio Secrets panel)
 * - Safe internal caching and persistence
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
    private val imageGenerationModels = listOf(
        "gemini-2.5-flash-image",
        "gemini-3.1-flash-image-preview"
    )
    private val visionModels = listOf(
        "gemini-flash-latest",
        "gemini-2.5-flash-lite",
        "gemini-3.1-flash-lite-preview"
    )

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Generates a new image from a text prompt.
     */
    suspend fun generateImage(
        prompt: String,
        dialect: RegionalDialect
    ): ImageServiceResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext ImageServiceResult.Error(
                errorMessage = "Gemini API key is not configured. Please add GEMINI_API_KEY in the AI Studio Secrets panel.",
                canRetry = false
            )
        }

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

        var lastError: String? = null
        for (model in imageGenerationModels) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

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
                    lastError = "API quota exceeded. Please wait a moment and try again."
                    continue
                } else if (response.code == 404 || response.code == 400) {
                    lastError = "Image model returned status ${response.code}: $responseBodyStr"
                    continue
                } else {
                    lastError = "Could not generate image (status ${response.code})."
                }
            } catch (e: java.net.UnknownHostException) {
                return@withContext ImageServiceResult.Error(
                    errorMessage = "Image generation needs an internet connection. Please check your network.",
                    isNetworkError = true,
                    canRetry = true
                )
            } catch (e: Exception) {
                Log.e("ImageGenerationService", "Failed with model $model", e)
                lastError = e.localizedMessage ?: "Network error during image generation"
            }
        }

        ImageServiceResult.Error(
            errorMessage = lastError ?: "Couldn't create the image. Please try again.",
            canRetry = true
        )
    }

    /**
     * Multimodal Image Understanding / Vision Analysis.
     */
    suspend fun analyzeImage(
        imagePath: String,
        userPrompt: String,
        dialect: RegionalDialect
    ): ImageServiceResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext ImageServiceResult.Error(
                errorMessage = "Gemini API key is not configured. Please add GEMINI_API_KEY in the AI Studio Secrets panel.",
                canRetry = false
            )
        }

        val base64Pair = ImageStorageManager.getBase64ImageData(context, imagePath)
            ?: return@withContext ImageServiceResult.Error("Could not process attached image.", canRetry = false)

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
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
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
                }
            } catch (e: java.net.UnknownHostException) {
                return@withContext ImageServiceResult.Error(
                    errorMessage = "Image analysis needs an internet connection.",
                    isNetworkError = true,
                    canRetry = true
                )
            } catch (e: Exception) {
                Log.e("ImageGenerationService", "Vision analysis error with $model", e)
            }
        }

        ImageServiceResult.Error("Could not analyze the image. Please try again.", canRetry = true)
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
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext ImageServiceResult.Error(
                errorMessage = "Gemini API key is not configured.",
                canRetry = false
            )
        }

        val base64Pair = ImageStorageManager.getBase64ImageData(context, imagePath)
            ?: return@withContext ImageServiceResult.Error("Could not read original image for editing.", canRetry = false)

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
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
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
                }
            } catch (e: Exception) {
                Log.e("ImageGenerationService", "Edit image error", e)
            }
        }

        // If direct image editing is not supported on this endpoint, fall back to vision description + image generation
        generateImage(visualPrompt, dialect)
    }
}
