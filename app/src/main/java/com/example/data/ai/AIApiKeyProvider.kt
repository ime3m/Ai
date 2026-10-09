package com.example.data.ai

import android.content.Context
import com.example.BuildConfig
import com.example.data.preferences.PreferencesRepository

/**
 * Centralized API key resolution helper for Gemini API services.
 * Prefers custom API key configured in app Settings if present;
 * otherwise uses the build configuration key (from AI Studio Secrets).
 */
object AIApiKeyProvider {

    fun getApiKey(context: Context? = null): String {
        if (context != null) {
            try {
                val customKey = PreferencesRepository(context).customApiKey
                if (!customKey.isNullOrBlank() && customKey.trim().length > 10) {
                    return customKey.trim()
                }
            } catch (_: Exception) {}
        }
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotBlank() && key != "MY_GEMINI_API_KEY") key.trim() else ""
        } catch (_: Exception) {
            ""
        }
    }

    fun isConfigured(context: Context? = null): Boolean {
        return getApiKey(context).isNotBlank()
    }

    fun isCustomKeyActive(context: Context): Boolean {
        val customKey = PreferencesRepository(context).customApiKey
        return !customKey.isNullOrBlank() && customKey.trim().length > 10
    }
}
