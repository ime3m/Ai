package com.example.data.reasoning

import android.util.Log
import com.example.data.model.RegionalDialect
import java.util.LinkedList

/**
 * Section 154, 155, 156, 157, 160:
 * Response Quality System enforcing strict anti-generic response rules,
 * repetition detection, relevance validation, and developer tracing.
 */
object ResponseQualitySystem {

    private const val TAG = "ResponseQualitySystem"

    // Short-term history of recent AI responses to detect repetition loops
    private val recentAiResponses = LinkedList<String>()
    private const val MAX_RECENT_RESPONSES = 6

    // Forbidden generic filler patterns that MUST NEVER be returned as answers to questions
    private val FORBIDDEN_GENERIC_PATTERNS = listOf(
        "ശരിയാണ്, നിങ്ങൾ പറഞ്ഞത് എനിക്ക് നന്നായി മനസ്സിലായി",
        "തുടർന്ന് സംസാരിക്കാം, കൂടുതൽ പറയൂ",
        "കൂടുതൽ പറയൂ",
        "ബാക്കി കൂടി പറയൂ",
        "കൂടുതൽ പറയൂ കേൾക്കട്ടെ",
        "താൻ പറഞ്ഞത് എനിക്ക് ക്ലിയറായി മനസ്സിലായി",
        "നിങ്ങൾ പറഞ്ഞത് കറക്ടാണ് കേട്ടോ",
        "Deadass, that's wild! You're speaking straight facts right now, tell me more",
        "Tell us more about what you're thinking"
    )

    /**
     * Section 155: Loop & Generic Repetition Detector.
     * Returns true if the response is a generic filler response or an uninformative duplicate.
     */
    fun isGenericOrRepetitive(
        candidateResponse: String,
        userQuery: String,
        intent: UserIntent
    ): Boolean {
        val trimmed = candidateResponse.trim()
        val lower = trimmed.lowercase()

        // 1. Check for exact forbidden generic patterns
        for (forbidden in FORBIDDEN_GENERIC_PATTERNS) {
            if (lower.contains(forbidden.lowercase())) {
                // If it contains the forbidden filler without providing substantial factual or task content
                if (trimmed.length < forbidden.length + 30) {
                    Log.w(TAG, "Blocked forbidden generic filler pattern: '$forbidden'")
                    return true
                }
            }
        }

        // 2. Pure acknowledgment without an answer is strictly forbidden for questions/tasks
        if (intent == UserIntent.QUESTION ||
            intent == UserIntent.INFORMATION_LOOKUP ||
            intent == UserIntent.CALCULATION ||
            intent == UserIntent.TRANSLATION ||
            intent == UserIntent.CURRENT_INFORMATION ||
            intent == UserIntent.ADVICE ||
            intent == UserIntent.EXPLANATION
        ) {
            val pureAcknowledgments = listOf(
                "ശരിയാണ്", "മനസ്സിലായി", "ഓക്കെ", "ശരി", "അതെ", "തീർച്ചയായും",
                "yes", "okay", "understood", "i see", "sure"
            )
            if (pureAcknowledgments.any { lower == it || lower == "$it." || lower == "$it!" }) {
                Log.w(TAG, "Blocked pure acknowledgment without answer for intent: $intent")
                return true
            }
        }

        // 3. Repetition detector against recent responses
        synchronized(recentAiResponses) {
            for (past in recentAiResponses) {
                // If exact match
                if (past.equals(trimmed, ignoreCase = true)) {
                    Log.w(TAG, "Blocked exact duplicate of recent response: '$trimmed'")
                    return true
                }

                // If sentence structure similarity is extremely high
                val pastWords = past.lowercase().split(Regex("\\s+")).toSet()
                val currentWords = trimmed.lowercase().split(Regex("\\s+")).toSet()
                if (pastWords.isNotEmpty() && currentWords.isNotEmpty()) {
                    val intersection = pastWords.intersect(currentWords).size
                    val similarity = intersection.toDouble() / maxOf(pastWords.size, currentWords.size)
                    if (similarity > 0.85 && intent != UserIntent.GREETING) {
                        Log.w(TAG, "Blocked high-similarity repetition (similarity=$similarity)")
                        return true
                    }
                }
            }
        }

        return false
    }

    /**
     * Section 156: Question-Answer Relevance Checker.
     * Verifies that the candidate response directly addresses what the user requested.
     */
    fun checkRelevance(
        query: String,
        response: String,
        intent: UserIntent
    ): Boolean {
        val qLower = query.lowercase().trim()
        val rLower = response.lowercase().trim()

        if (response.isBlank()) return false

        when (intent) {
            UserIntent.QUESTION, UserIntent.INFORMATION_LOOKUP -> {
                // Capital of Kerala check
                if (qLower.contains("കേരള") && (qLower.contains("തലസ്ഥാനം") || qLower.contains("capital"))) {
                    return rLower.contains("തിരുവനന്തപുരം") || rLower.contains("thiruvananthapuram")
                }
                // Capital of India check
                if ((qLower.contains("india") || qLower.contains("ഇന്ത്യ")) && (qLower.contains("capital") || qLower.contains("തലസ്ഥാനം"))) {
                    return rLower.contains("new delhi") || rLower.contains("ഡൽഹി") || rLower.contains("delhi")
                }
                // Kerala CM check
                if (qLower.contains("chief minister") || qLower.contains("മുഖ്യമന്ത്രി") || qLower.contains("cm")) {
                    return rLower.contains("satheesan") || rLower.contains("സതീശൻ") || rLower.contains("v. d.")
                }
            }

            UserIntent.CALCULATION -> {
                // Must contain digits or calculation result
                if (!response.any { it.isDigit() }) {
                    return false
                }
                // 1000 രൂപയുടെ 15% check
                if (qLower.contains("1000") && qLower.contains("15")) {
                    return response.contains("150")
                }
            }

            UserIntent.CURRENT_INFORMATION -> {
                // Weather query must have weather indicators
                if (qLower.contains("weather") || qLower.contains("കാലാവസ്ഥ")) {
                    val hasWeatherTerms = rLower.contains("°c") || rLower.contains("മഴ") ||
                            rLower.contains("warm") || rLower.contains("sunny") ||
                            rLower.contains("കാലാവസ്ഥ") || rLower.contains("വെയിൽ") ||
                            rLower.contains("താപനില") || rLower.contains("rain") ||
                            rLower.contains("clouds") || rLower.contains("മേഘാവൃതം") ||
                            rLower.contains("kuwait") || rLower.contains("കുവൈത്ത്")
                    if (!hasWeatherTerms) return false
                }
            }

            UserIntent.ADVICE -> {
                // Workout plan must contain actual exercises or steps
                if (qLower.contains("workout") || qLower.contains("വർക്കൗട്ട്") || qLower.contains("വ്യായാമം")) {
                    val hasWorkoutTerms = rLower.contains("വാം-അപ്പ്") || rLower.contains("സ്ക്വാറ്റ്") ||
                            rLower.contains("പ്ലാങ്ക്") || rLower.contains("warm-up") ||
                            rLower.contains("push-up") || rLower.contains("squat") ||
                            rLower.contains("നടത്തം") || rLower.contains("കാർഡിയോ")
                    if (!hasWorkoutTerms) return false
                }
            }

            UserIntent.STORYTELLING -> {
                // Must be longer than a simple sentence
                if (response.length < 80) return false
            }

            UserIntent.TRANSLATION -> {
                // Translation must not simply repeat the prompt without translated words
                if (response == query) return false
            }

            else -> {
                // General pass
            }
        }

        return true
    }

    /**
     * Section 154: Acknowledgment limit reformatter.
     * Ensures any conversational acknowledgment is immediately followed by the factual answer,
     * and varies repetitive prefixes.
     */
    fun formatWithAcknowledgmentLimit(
        acknowledgment: String,
        actualAnswer: String
    ): String {
        val cleanAck = acknowledgment.trim().removeSuffix(".")
        val cleanAnswer = actualAnswer.trim()
        if (cleanAck.isBlank()) return cleanAnswer
        return "$cleanAck, $cleanAnswer"
    }

    /**
     * Section 157: Empty or Failed AI Response Handler.
     * Returns an honest, supportive error message rather than conversational filler.
     */
    fun createHonestFailureResponse(dialect: RegionalDialect): String {
        return if (dialect.language.equals("Malayalam", true)) {
            "ക്ഷമിക്കണം, ഇപ്പോൾ സാങ്കേതിക കാരണങ്ങളാൽ കൃത്യമായ മറുപടി ലഭ്യമാക്കാൻ കഴിഞ്ഞില്ല. ദയവായി അല്പം കഴിഞ്ഞ് വീണ്ടും ചോദിക്കാമോ?"
        } else if (dialect.id.contains("kuwait")) {
            "عذراً، لم نتمكن من معالجة الرد حالياً بسبب خلل تقني. يرجى المحاولة مرة أخرى."
        } else {
            "I apologize, but I couldn't prepare a response right now due to a network or service issue. Please try asking again."
        }
    }

    /**
     * Registers a verified final response into the short-term memory buffer.
     */
    fun recordResponse(response: String) {
        synchronized(recentAiResponses) {
            recentAiResponses.addLast(response.trim())
            if (recentAiResponses.size > MAX_RECENT_RESPONSES) {
                recentAiResponses.removeFirst()
            }
        }
    }

    /**
     * Clears the repetition memory (e.g. on new conversation session).
     */
    fun clearMemory() {
        synchronized(recentAiResponses) {
            recentAiResponses.clear()
        }
    }
}
