package com.example.data.suggestions

import com.example.data.location.LocationExtractor

/**
 * Intelligent Conversation Title Generator.
 *
 * Generates concise, meaningful titles from the user's first prompt
 * rather than displaying generic "New Chat".
 *
 * Examples:
 * - "What's the weather in Kuwait?" -> "Weather in Kuwait"
 * - "Tell me about Kerala travel" -> "Kerala Travel & Places"
 * - "Help me with Excel formulas" -> "Excel & Spreadsheet Help"
 * - "How do I build an AI app?" -> "AI App Development"
 */
object ConversationTitleGenerator {

    fun generateTitle(query: String, dialectName: String = "Kerala"): String {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return "Chat with $dialectName"

        val lower = trimmed.lowercase()

        // 0. Image Generation
        if (com.example.data.image.ImageIntentDetector.isImageGenerationRequest(trimmed) ||
            lower.contains("image") || lower.contains("ചിത്രം") || lower.contains("picture")
        ) {
            val visualPrompt = com.example.data.image.ImageIntentDetector.extractImagePrompt(trimmed)
            val shortSubject = if (visualPrompt.length > 25) visualPrompt.take(23).trimEnd() + "..." else visualPrompt
            return "$shortSubject Image"
        }

        // 1. Weather / Climate
        if (lower.contains("weather") || lower.contains("കാലാവസ്ഥ") ||
            lower.contains("rain") || lower.contains("mazha") ||
            lower.contains("climate") || lower.contains("തണുപ്പ്") || lower.contains("ചൂട്")
        ) {
            val city = LocationExtractor.extractExplicitLocation(trimmed, lower)
            return if (!city.isNullOrBlank()) {
                "Weather in ${city.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
            } else {
                "Weather Information"
            }
        }

        // 2. Kerala & Travel
        if (lower.contains("kerala") || lower.contains("കേരള") ||
            lower.contains("tourist") || lower.contains("visit") ||
            lower.contains("travel") || lower.contains("യാത്ര")
        ) {
            return "Kerala Travel & Places"
        }

        // 3. Technical / Coding / Excel / Software
        if (lower.contains("excel") || lower.contains("spreadsheet") || lower.contains("formula") || lower.contains("ഫോർമുല")) {
            return "Excel & Spreadsheet Help"
        }
        if (lower.contains("code") || lower.contains("kotlin") || lower.contains("android") ||
            lower.contains("python") || lower.contains("programming") || lower.contains("app development")
        ) {
            return "App Development & Coding"
        }

        // 4. Fitness / Health
        if (lower.contains("fitness") || lower.contains("gym") || lower.contains("workout") ||
            lower.contains("health") || lower.contains("exercise") || lower.contains("വ്യായാമം") || lower.contains("ആരോഗ്യം")
        ) {
            return "Fitness & Health Guide"
        }

        // 5. Food & Cuisine
        if (lower.contains("food") || lower.contains("recipe") || lower.contains("ഭക്ഷണം") ||
            lower.contains("cooking") || lower.contains("ചായ") || lower.contains("പാചകം")
        ) {
            return "Food & Recipes"
        }

        // 6. Time & Date
        if (lower.contains("time") || lower.contains("സമയം") || lower.contains("date") || lower.contains("തീയതി")) {
            val city = LocationExtractor.extractExplicitLocation(trimmed, lower)
            return if (!city.isNullOrBlank()) {
                "Time in ${city.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
            } else {
                "Time & Date"
            }
        }

        // 7. Language & Slang
        if (lower.contains("malayalam") || lower.contains("മലയാളം") ||
            lower.contains("slang") || lower.contains("dialect") || lower.contains("ചൊല്ല്")
        ) {
            return "$dialectName Language & Slang"
        }

        // 8. General question cleanup: remove question starters
        var clean = trimmed
        val starters = listOf(
            "what is the", "what is", "what's the", "what's", "whats the", "whats",
            "tell me about", "tell me", "can you tell me about", "can you tell me",
            "can you explain", "explain to me", "explain", "how to", "how do i", "how can i",
            "എന്താണ്", "എന്താണ് എന്ന്", "എങ്ങനെയാണ്", "എനിക്ക് പറഞ്ഞു തരൂ", "പറഞ്ഞു തരൂ",
            "വിശദീകരിക്കൂ", "വിവരിക്കൂ"
        )
        for (s in starters) {
            if (clean.lowercase().startsWith(s)) {
                clean = clean.substring(s.length).trim(' ', '?', ':', ',', '.', '-')
                break
            }
        }

        if (clean.length > 36) {
            clean = clean.take(34).trimEnd() + "..."
        }

        return if (clean.isNotBlank()) {
            clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } else {
            "Chat with $dialectName"
        }
    }
}
