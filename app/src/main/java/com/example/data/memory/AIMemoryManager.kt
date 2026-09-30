package com.example.data.memory

import com.example.data.model.MemoryItemEntity

object AIMemoryManager {

    private val rememberPatterns = listOf(
        Regex("(?i)^(?:please\\s+)?remember(?:\\s+that)?\\s+(.+)$"),
        Regex("(?i)^ഓർത്തു\\s*വെക്കൂ\\s+(.+)$"),
        Regex("(?i)^ഓർമ്മയിൽ\\s*വെക്കൂ\\s+(.+)$"),
        Regex("(?i)^don't\\s+forget(?:\\s+that)?\\s+(.+)$")
    )

    /**
     * Inspects if user message is an explicit command to save something to AI memory.
     */
    fun extractMemoryIntent(userMessage: String): Pair<String, String>? {
        val trimmed = userMessage.trim()
        for (pattern in rememberPatterns) {
            val match = pattern.find(trimmed)
            if (match != null && match.groupValues.size > 1) {
                val statement = match.groupValues[1].trim()
                if (statement.length >= 3) {
                    val key = when {
                        statement.contains("name", ignoreCase = true) || statement.contains("പേര്") -> "Name"
                        statement.contains("prefer", ignoreCase = true) || statement.contains("ഇഷ്ടം") -> "Preference"
                        statement.contains("work", ignoreCase = true) || statement.contains("ജോലി") || statement.contains("business") -> "Work"
                        statement.contains("live", ignoreCase = true) || statement.contains("താമസം") || statement.contains("location") -> "Location"
                        else -> "User Note"
                    }
                    return Pair(key, statement)
                }
            }
        }
        return null
    }

    /**
     * Formats active memories into a clean string for Gemini system instruction.
     */
    fun formatMemoriesForPrompt(memories: List<MemoryItemEntity>): String {
        val active = memories.filter { it.isEnabled }
        if (active.isEmpty()) return ""

        val sb = StringBuilder("\n--- USER-CONTROLLED PERSONAL MEMORY ---\n")
        sb.append("The user has explicitly asked you to remember the following preferences:\n")
        active.forEach { mem ->
            sb.append("• [${mem.key}]: ${mem.value}\n")
        }
        sb.append("Honor these preferences naturally in your responses.\n")
        sb.append("--- END PERSONAL MEMORY ---\n")
        return sb.toString()
    }
}
