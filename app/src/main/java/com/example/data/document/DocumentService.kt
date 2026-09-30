package com.example.data.document

import android.content.Context
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
import java.util.concurrent.TimeUnit

sealed class DocumentServiceResult {
    data class Success(
        val responseText: String,
        val documentName: String,
        val promptUsed: String
    ) : DocumentServiceResult()

    data class Error(
        val errorMessage: String,
        val isNetworkError: Boolean = false,
        val canRetry: Boolean = true
    ) : DocumentServiceResult()
}

/**
 * Intelligent Document & PDF Analysis Service using Gemini Multimodal API.
 * Supports PDF documents via inline_data and text documents via embedded content.
 */
class DocumentService(
    private val context: Context,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val docModels = listOf(
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
     * Analyzes a document with a user prompt (or default summarization).
     */
    suspend fun analyzeDocument(
        document: DocumentAttachment,
        userPrompt: String,
        dialect: RegionalDialect
    ): DocumentServiceResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext DocumentServiceResult.Error(
                errorMessage = "Gemini API key is not configured. Please add GEMINI_API_KEY in the AI Studio Secrets panel.",
                canRetry = false
            )
        }

        val promptText = if (userPrompt.isNotBlank()) userPrompt else "Summarize this document clearly and highlight the most important points."

        val systemInstruction = """
            You are a helpful regional AI assistant speaking with ${dialect.dialectName} tone (${dialect.cityOrArea}, ${dialect.region}).
            Communicate naturally in ${dialect.language} with a friendly, regional perspective.
            Analyze the attached document carefully and answer the user's question accurately.
            Format your response cleanly with headings or bullet points where appropriate.
        """.trimIndent()

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray()
            val contentObj = JSONObject().apply {
                val partsArray = JSONArray()

                // Add instruction & prompt
                partsArray.put(JSONObject().apply {
                    put("text", "$systemInstruction\n\nDocument Name: ${document.fileName}\nUser Request: $promptText")
                })

                // Add document content
                if (document.textContent != null) {
                    // Plain text / markdown content
                    partsArray.put(JSONObject().apply {
                        put("text", "--- DOCUMENT CONTENT ---\n${document.textContent}\n--- END DOCUMENT CONTENT ---")
                    })
                } else if (document.isPdf) {
                    // PDF encoded as inline_data
                    val base64Pair = DocumentProcessingManager.getDocumentBase64(document.localPath)
                    if (base64Pair != null) {
                        partsArray.put(JSONObject().apply {
                            put("inline_data", JSONObject().apply {
                                put("mime_type", base64Pair.second)
                                put("data", base64Pair.first)
                            })
                        })
                    } else {
                        partsArray.put(JSONObject().apply {
                            put("text", "[Document was attached: ${document.fileName}, size: ${document.fileSizeFormatted}]")
                        })
                    }
                }

                put("parts", partsArray)
            }
            contentsArray.put(contentObj)
            put("contents", contentsArray)
        }

        var lastError: String? = null
        for (model in docModels) {
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
                        return@withContext DocumentServiceResult.Success(
                            responseText = text.trim(),
                            documentName = document.fileName,
                            promptUsed = promptText
                        )
                    }
                } else if (response.code == 429) {
                    lastError = "API quota exceeded. Please wait a moment and try again."
                    continue
                } else {
                    lastError = "Document analysis failed (status ${response.code})."
                }
            } catch (e: java.net.UnknownHostException) {
                return@withContext DocumentServiceResult.Error(
                    errorMessage = "Document analysis requires an active internet connection.",
                    isNetworkError = true,
                    canRetry = true
                )
            } catch (e: Exception) {
                Log.e("DocumentService", "Error analyzing document with $model", e)
                lastError = e.localizedMessage ?: "Failed to process document"
            }
        }

        DocumentServiceResult.Error(
            errorMessage = lastError ?: "Could not analyze the document. Please try again.",
            canRetry = true
        )
    }
}
