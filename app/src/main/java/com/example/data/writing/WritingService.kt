package com.example.data.writing

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

enum class WritingType(val title: String, val iconEmoji: String, val placeholder: String) {
    EMAIL("Email", "✉️", "e.g. Leave request for 2 days due to personal emergency"),
    WHATSAPP("WhatsApp Message", "💬", "e.g. Casual invite to friends for dinner this weekend"),
    CV("CV / Resume Summary", "📄", "e.g. Android developer with 3 years experience in Kotlin & Compose"),
    COVER_LETTER("Cover Letter", "📝", "e.g. Application for Mobile App Engineer at a tech startup"),
    CAPTION("Instagram Caption", "📱", "e.g. Sunset view from tea plantation in Munnar with friends"),
    YOUTUBE("YouTube Description", "▶️", "e.g. Cooking authentic Kerala sadhya recipe step-by-step"),
    FORMAL_LETTER("Formal Letter", "📜", "e.g. Request to bank manager for address update"),
    TRANSLATION("Translation", "🌐", "e.g. Translate this paragraph into natural regional Malayalam"),
    REWRITE("Rewrite & Polish", "✏️", "e.g. Make this rough draft sound confident and professional"),
    SUMMARIZE("Summarize", "📚", "e.g. Key takeaways from this meeting notes or article"),
    GRAMMAR("Grammar Correction", "✅", "e.g. Correct grammar and flow of this message")
}

enum class WritingTone(val title: String) {
    FORMAL("Formal"),
    FRIENDLY("Friendly"),
    SHORT("Short & Crisp"),
    PROFESSIONAL("Professional"),
    REGIONAL("Regional Kerala Style"),
    CREATIVE("Creative")
}

sealed class WritingResult {
    data class Success(
        val outputText: String,
        val type: WritingType,
        val tone: WritingTone
    ) : WritingResult()

    data class Error(val message: String) : WritingResult()
}

class WritingService(
    private val context: Context,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()
) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val models = listOf("gemini-flash-latest", "gemini-2.5-flash-lite", "gemini-3.1-flash-lite-preview")

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun generateWriting(
        type: WritingType,
        tone: WritingTone,
        userPrompt: String,
        dialect: RegionalDialect
    ): WritingResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext WritingResult.Error("Gemini API key is not configured. Please add GEMINI_API_KEY in AI Studio.")
        }

        val regionalHint = if (tone == WritingTone.REGIONAL) {
            "Use natural ${dialect.dialectName} (${dialect.cityOrArea}, ${dialect.language}) colloquial expressions and tone where appropriate."
        } else {
            "Ensure the output language naturally honors the user's request (English, Malayalam, or bilingual if requested)."
        }

        val promptText = """
            You are an expert AI Writing Assistant.
            Task: Write a high quality ${type.title}.
            Desired Tone: ${tone.title}.
            $regionalHint

            User Requirements:
            $userPrompt

            Output only the finalized written content ready to copy, share, and use, without unnecessary conversational filler before or after.
        """.trimIndent()

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray()
            val contentObj = JSONObject().apply {
                val partsArray = JSONArray()
                partsArray.put(JSONObject().apply {
                    put("text", promptText)
                })
                put("parts", partsArray)
            }
            contentsArray.put(contentObj)
            put("contents", contentsArray)
        }

        for (model in models) {
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
                        return@withContext WritingResult.Success(
                            outputText = text.trim(),
                            type = type,
                            tone = tone
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("WritingService", "Error generating writing with $model", e)
            }
        }

        WritingResult.Error("Could not generate writing content. Please check your connection and try again.")
    }
}
