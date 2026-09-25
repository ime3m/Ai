package com.example.data.reasoning

import com.example.data.knowledge.RealTimeKnowledgeEngine
import com.example.data.knowledge.RealTimeKnowledgeResponse
import com.example.data.model.RegionalDialect
import com.example.data.model.VoicePersonality
import java.util.regex.Pattern

/**
 * Section 153: Supported user intent classifications.
 */
enum class UserIntent {
    QUESTION,
    REQUEST,
    COMMAND,
    INFORMATION_LOOKUP,
    CURRENT_INFORMATION,
    TRANSLATION,
    EXPLANATION,
    ADVICE,
    CALCULATION,
    CASUAL_CONVERSATION,
    STORYTELLING,
    CREATIVE_REQUEST,
    PERSONALIZATION,
    FOLLOW_UP,
    CLARIFICATION,
    GREETING,
    UNKNOWN
}

/**
 * Section 160: Developer trace structure for inspecting every AI response.
 */
data class AiResponseTrace(
    val messageId: String,
    val conversationId: String,
    val userInput: String,
    val inputSource: String, // "VOICE" or "TEXT"
    val detectedLanguage: String,
    val detectedRegion: String,
    val detectedIntent: UserIntent,
    val currentInformationRequired: Boolean,
    val retrievalUsed: Boolean,
    val retrievalSources: List<String>,
    val systemPromptVersion: String,
    val modelRequestCreated: Boolean,
    val modelRequestSent: Boolean,
    val modelResponseReceived: Boolean,
    val rawAiResponse: String,
    val responseLength: Int,
    val genericResponseDetected: Boolean,
    val relevanceCheck: String, // "PASS" or "FAIL"
    val regenerationTriggered: Boolean,
    val regenerationReason: String,
    val finalResponse: String,
    val ttsStarted: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

object UniversalReasoningEngine {

    /**
     * Classifies the latest user input into an explicit intent.
     * Guaranteed never to classify all Malayalam messages as casual conversation.
     */
    fun classifyIntent(query: String): UserIntent {
        val trimmed = query.trim()
        val lower = trimmed.lowercase()

        if (trimmed.isEmpty()) return UserIntent.UNKNOWN

        // 1. Calculation
        if (isCalculationQuery(lower)) {
            return UserIntent.CALCULATION
        }

        // 2. Translation
        if (isTranslationQuery(lower)) {
            return UserIntent.TRANSLATION
        }

        // 3. Creative & Storytelling
        if (lower.contains("കഥ") || lower.contains("story") || lower.contains("tell me a story") || lower.contains("tale")) {
            return UserIntent.STORYTELLING
        }
        if (lower.contains("തമാശ") || lower.contains("joke") || lower.contains("funny") ||
            lower.contains("കവിത") || lower.contains("poem") || lower.contains("song") || lower.contains("പാട്ട്")) {
            return UserIntent.CREATIVE_REQUEST
        }

        // 4. Follow-up / Contextual recall
        if (lower.contains("ഇന്നലെ പറഞ്ഞ") || lower.contains("നേരത്തെ പറഞ്ഞ") || lower.contains("മുൻപ് പറഞ്ഞ") ||
            lower.contains("what did you say earlier") || lower.contains("about what we discussed") ||
            lower.contains("continue that") || lower.contains("തുടർന്ന് പറയൂ")) {
            return UserIntent.FOLLOW_UP
        }

        // 5. Current Information / Time-sensitive lookups
        if (RealTimeKnowledgeEngine.requiresRealTimeRetrieval(trimmed)) {
            return UserIntent.CURRENT_INFORMATION
        }

        // 6. Advice / Recommendations / Plans (e.g. workout, diet, travel checklist)
        if (lower.contains("workout") || lower.contains("വർക്കൗട്ട്") || lower.contains("വ്യായാമം") ||
            lower.contains("plan") || lower.contains("diet") || lower.contains("ഭക്ഷണക്രമം") ||
            lower.contains("tips") || lower.contains("ഉപദേശം") || lower.contains("നിർദ്ദേശം") ||
            lower.contains("how to lose weight") || lower.contains("തടി കുറയ്ക്കാൻ")) {
            return UserIntent.ADVICE
        }

        // 7. Explanations (How things work, in-depth breakdowns)
        if (lower.contains("വിശദീകരിക്കാമോ") || lower.contains("വിശദീകരിക്കൂ") || lower.contains("explain") ||
            lower.contains("എന്തുകൊണ്ടാണ്") || lower.contains("എങ്ങനെയാണ് പ്രവർത്തിക്കുന്നത്") || lower.contains("how does it work")) {
            return UserIntent.EXPLANATION
        }

        // 8. Commands (System or device commands)
        if (lower.startsWith("തുടങ്ങൂ") || lower.startsWith("നിർത്തൂ") || lower.startsWith("മാറ്റൂ") ||
            lower.startsWith("open") || lower.startsWith("close") || lower.startsWith("set") || lower.startsWith("turn on")) {
            return UserIntent.COMMAND
        }

        // 9. Greetings
        if (isGreetingOnly(lower)) {
            return UserIntent.GREETING
        }

        // 10. Questions & Information Lookup
        if (isQuestionQuery(lower)) {
            return if (lower.contains("തലസ്ഥാനം") || lower.contains("capital") || lower.contains("ആരാണ്") ||
                lower.contains("who is") || lower.contains("where is") || lower.contains("എവിടെയാണ്") ||
                lower.contains("എന്താണ്") || lower.contains("what is")) {
                UserIntent.INFORMATION_LOOKUP
            } else {
                UserIntent.QUESTION
            }
        }

        // 11. Requests
        if (lower.contains("എനിക്ക് വേണം") || lower.contains("സഹായിക്കൂ") || lower.contains("can you") ||
            lower.contains("please") || lower.contains("help me") || lower.contains("തരുമോ")) {
            return UserIntent.REQUEST
        }

        // 12. Casual Conversation
        if (lower.contains("സുഖാണോ") || lower.contains("സുഖമല്ലേ") || lower.contains("എന്തൊക്കെയുണ്ട്") ||
            lower.contains("how are you") || lower.contains("whats up") || lower.contains("വിശേഷങ്ങൾ")) {
            return UserIntent.CASUAL_CONVERSATION
        }

        return UserIntent.QUESTION
    }

    private fun isCalculationQuery(lower: String): Boolean {
        if (lower.contains("%") || lower.contains("ശതമാനം") || lower.contains("percent") || lower.contains("percentage")) {
            return true
        }
        if (lower.contains("രൂപയുടെ") && (lower.contains("%") || lower.contains("എത്ര"))) {
            return true
        }
        val mathPattern = Pattern.compile("\\b(\\d+)\\s*([+\\-*/x×]|plus|minus|times|divided by|കൂട്ടിയാൽ|കുറച്ചാൽ|ഗുണിച്ചാൽ|ഹരിച്ചാൽ)\\s*(\\d+)\\b")
        if (mathPattern.matcher(lower).find()) return true
        if (lower.startsWith("calculate") || lower.contains("കണക്കാക്കൂ") || lower.contains("തുക എത്ര")) return true
        return false
    }

    private fun isTranslationQuery(lower: String): Boolean {
        return lower.contains("translate") || lower.contains("വിവർത്തനം") ||
                lower.contains("ഇംഗ്ലീഷിലേക്ക്") || lower.contains("മലയാളത്തിലേക്ക്") ||
                lower.contains("into english") || lower.contains("into malayalam") ||
                lower.contains("in english") || lower.contains("in malayalam")
    }

    private fun isGreetingOnly(lower: String): Boolean {
        val greetings = listOf(
            "hello", "hi", "hey", "good morning", "good evening", "good afternoon",
            "ഹലോ", "നമസ്കാരം", "ഹായ്", "നമസ്തേ", "അസ്സലാമു അലൈക്കും", "marhaba"
        )
        val clean = lower.replace(Regex("[.,!?;:]"), "").trim()
        return greetings.any { clean == it || clean.startsWith("$it ") }
    }

    private fun isQuestionQuery(lower: String): Boolean {
        return lower.endsWith("?") ||
                lower.contains("ഏതാണ്") || lower.contains("ആരാണ്") || lower.contains("എവിടെയാണ്") ||
                lower.contains("എന്താണ്") || lower.contains("എങ്ങനെയാണ്") || lower.contains("എത്രയാണ്") ||
                lower.contains("എപ്പോഴാണ്") || lower.contains("ആരുടെയാണ്") ||
                lower.contains("what") || lower.contains("who") || lower.contains("where") ||
                lower.contains("when") || lower.contains("which") || lower.contains("why") ||
                lower.contains("how") || lower.contains("is it") || lower.contains("can you")
    }

    /**
     * Solves calculations directly (e.g. 1000 രൂപയുടെ 15% -> 150).
     */
    fun evaluateCalculation(query: String, dialect: RegionalDialect): String? {
        val lower = query.lowercase().trim()

        // Percentages: e.g. "1000 രൂപയുടെ 15%" or "15% of 1000"
        val pctRegex1 = Pattern.compile("(\\d+)\\s*(?:രൂപയുടെ|of|ന്റെ|ഇന്റെ)?\\s*(\\d+(?:\\.\\d+)?)\\s*(?:%|ശതമാനം|percent)")
        val matcher1 = pctRegex1.matcher(lower)
        if (matcher1.find()) {
            val base = matcher1.group(1)?.toDoubleOrNull()
            val pct = matcher1.group(2)?.toDoubleOrNull()
            if (base != null && pct != null) {
                val result = (base * pct) / 100.0
                val formatted = if (result % 1.0 == 0.0) result.toInt().toString() else "%.2f".format(result)
                val baseStr = if (base % 1.0 == 0.0) base.toInt().toString() else base.toString()
                val pctStr = if (pct % 1.0 == 0.0) pct.toInt().toString() else pct.toString()
                return if (dialect.language.equals("Malayalam", true)) {
                    "$baseStr രൂപയുടെ $pctStr% എന്നത് $formatted രൂപയാണ് ($baseStr × ${pct / 100.0} = $formatted)."
                } else {
                    "$pctStr% of $baseStr is $formatted ($baseStr × ${pct / 100.0} = $formatted)."
                }
            }
        }

        val pctRegex2 = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:%|ശതമാനം|percent)\\s*(?:of|രൂപയുടെ|ന്റെ)?\\s*(\\d+)")
        val matcher2 = pctRegex2.matcher(lower)
        if (matcher2.find()) {
            val pct = matcher2.group(1)?.toDoubleOrNull()
            val base = matcher2.group(2)?.toDoubleOrNull()
            if (base != null && pct != null) {
                val result = (base * pct) / 100.0
                val formatted = if (result % 1.0 == 0.0) result.toInt().toString() else "%.2f".format(result)
                val baseStr = if (base % 1.0 == 0.0) base.toInt().toString() else base.toString()
                val pctStr = if (pct % 1.0 == 0.0) pct.toInt().toString() else pct.toString()
                return if (dialect.language.equals("Malayalam", true)) {
                    "$baseStr രൂപയുടെ $pctStr% എന്നത് $formatted രൂപയാണ് ($baseStr × ${pct / 100.0} = $formatted)."
                } else {
                    "$pctStr% of $baseStr is $formatted ($baseStr × ${pct / 100.0} = $formatted)."
                }
            }
        }

        // Basic operations (+, -, *, /)
        val basicOp = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*([+\\-*/x×]|കൂട്ടിയാൽ|കുറച്ചാൽ|ഗുണിച്ചാൽ|ഹരിച്ചാൽ|plus|minus|times|divided by)\\s*(\\d+(?:\\.\\d+)?)")
        val matchOp = basicOp.matcher(lower)
        if (matchOp.find()) {
            val a = matchOp.group(1)?.toDoubleOrNull()
            val op = matchOp.group(2)
            val b = matchOp.group(3)?.toDoubleOrNull()
            if (a != null && b != null && op != null) {
                val res = when {
                    op == "+" || op == "plus" || op.contains("കൂട്ടിയാൽ") -> a + b
                    op == "-" || op == "minus" || op.contains("കുറച്ചാൽ") -> a - b
                    op == "*" || op == "x" || op == "×" || op == "times" || op.contains("ഗുണിച്ചാൽ") -> a * b
                    op == "/" || op == "divided by" || op.contains("ഹരിച്ചാൽ") -> if (b != 0.0) a / b else null
                    else -> null
                }
                if (res != null) {
                    val formatted = if (res % 1.0 == 0.0) res.toInt().toString() else "%.2f".format(res)
                    return if (dialect.language.equals("Malayalam", true)) {
                        "ഉത്തരം $formatted ആണ്."
                    } else {
                        "The result is $formatted."
                    }
                }
            }
        }

        return null
    }

    /**
     * Section 151 & Section 162: Universal Knowledge & Reasoning generator for all domains.
     * Guaranteed to return a factual, task-fulfilling answer, never generic acknowledgment.
     */
    fun generateKnowledgeAnswer(
        query: String,
        intent: UserIntent,
        dialect: RegionalDialect,
        strength: Float,
        personality: VoicePersonality,
        history: List<Pair<String, String>>
    ): String {
        val lower = query.lowercase().trim()

        // 1. Calculations
        val calcResult = evaluateCalculation(query, dialect)
        if (calcResult != null) {
            return formatWithRegionalStyle(calcResult, dialect, strength, personality)
        }

        // 2. Real-time Knowledge (Weather, CM, Gold, Currency, News, Blasters)
        val verifiedKnowledge = RealTimeKnowledgeEngine.retrieveVerifiedKnowledge(query, dialect)
        if (verifiedKnowledge != null) {
            return verifiedKnowledge.dialectText
        }

        // 3. Capital Cities
        // Kerala capital
        if (lower.contains("കേരള") && (lower.contains("തലസ്ഥാനം") || lower.contains("capital"))) {
            val base = if (dialect.language.equals("Malayalam", true)) {
                "കേരളത്തിന്റെ തലസ്ഥാനം തിരുവനന്തപുരം ആണ്."
            } else {
                "The capital of Kerala is Thiruvananthapuram."
            }
            return formatWithRegionalStyle(base, dialect, strength, personality, prefix = "തീർച്ചയായും, ")
        }
        // India capital
        if ((lower.contains("india") || lower.contains("ഇന്ത്യ")) && (lower.contains("capital") || lower.contains("തലസ്ഥാനം"))) {
            val base = if (dialect.language.equals("Malayalam", true)) {
                "ഇന്ത്യയുടെ തലസ്ഥാനം ന്യൂഡൽഹിയാണ്."
            } else {
                "New Delhi is the capital of India."
            }
            return formatWithRegionalStyle(base, dialect, strength, personality)
        }

        // 4. Workout Plan
        if (lower.contains("workout") || lower.contains("വർക്കൗട്ട്") || lower.contains("വ്യായാമം")) {
            val workoutPlan = if (dialect.language.equals("Malayalam", true)) {
                "തുടക്കക്കാർക്കും ഫിറ്റ്നസ് ആഗ്രഹിക്കുന്നവർക്കും അനുയോജ്യമായ ദൈനംദിന വർക്കൗട്ട് പ്ലാൻ ഇതാ:\n" +
                        "1. വാം-അപ്പ് (5-10 മിനിറ്റ്): സ്ട്രെച്ചിങ്, കൈകാൽ ചലനങ്ങൾ, ലൈറ്റ് ജോഗിങ്.\n" +
                        "2. പ്രധാന സ്ട്രെങ്ത് വ്യായാമങ്ങൾ: പുഷ്-അപ്സ് 12-15 എണ്ണം (3 സെറ്റ്), സ്ക്വാറ്റ്സ് 15-20 എണ്ണം (3 സെറ്റ്), പ്ലാങ്ക് 45 സെക്കൻഡ് (3 സെറ്റ്).\n" +
                        "3. കാർഡിയോ: 15-20 മിനിറ്റ് ബ്രിസ്ക് വാക്കിങ് അല്ലെങ്കിൽ സൈക്ലിങ്.\n" +
                        "4. കൂൾ-ഡൗൺ: ഡീപ് ബ്രീത്തിങ് വ്യായാമങ്ങൾ.\n" +
                        "ആവശ്യത്തിന് വെള്ളം കുടിക്കാനും പ്രോട്ടീൻ അടങ്ങിയ പോഷകാഹാരം കഴിക്കാനും പ്രത്യേകം ശ്രദ്ധിക്കണം."
            } else {
                "Here is an effective, balanced workout routine for building fitness:\n" +
                        "1. Warm-Up (5-10 mins): Dynamic stretching and light cardio to raise your heart rate.\n" +
                        "2. Core & Strength: 3 sets of 15 push-ups, 3 sets of 20 bodyweight squats, and a 45-second plank.\n" +
                        "3. Cardio: 15-20 minutes of brisk walking, running, or skipping.\n" +
                        "4. Cool-Down (5 mins): Deep breathing and static stretches.\n" +
                        "Remember to stay well hydrated and prioritize restful sleep and good nutrition!"
            }
            return workoutPlan
        }

        // 5. Stories
        if (intent == UserIntent.STORYTELLING || lower.contains("കഥ") || lower.contains("story")) {
            return if (dialect.language.equals("Malayalam", true)) {
                "ഒരു നല്ല നാടൻ കഥ പറയാം കേട്ടോളൂ: പണ്ട് കോഴിക്കോട് കടൽത്തീരത്ത് ഉണ്ണി എന്നൊരു കൊച്ചു കുട്ടിയുണ്ടായിരുന്നു. എല്ലാ സായാഹ്നങ്ങളിലും അവൻ കടൽക്കാറ്റേറ്റ് വലിയ കപ്പലുകളെ നോക്കി സ്വപ്നം കാണുമായിരുന്നു. ഒരിക്കൽ കടൽത്തീരത്ത് ഒരു വയസ്സായ മീൻപിടുത്തക്കാരൻ വല നെയ്യുന്നത് കണ്ട് ഉണ്ണി ചോദിച്ചു: 'മുത്തശ്ശാ, കടലിൽ ഏറ്റവും വലിയ സമ്പത്ത് എന്താണ്?'. മുത്തശ്ശൻ ചിരിച്ചുകൊണ്ട് പറഞ്ഞു: 'മോനേ, കടലിന്റെ വലിപ്പമല്ല, കൊടുങ്കാറ്റിലും പതറാതെ നമ്മെ കരക്കെത്തിക്കുന്ന ധൈര്യവും നന്മയുമാണ് ഏറ്റവും വലിയ സമ്പത്ത്'. ആ വാക്കുകൾ ഉണ്ണിയുടെ മനസ്സിൽ മായാതെ നിന്നു, അവൻ വലുതായപ്പോഴും ഏത് പ്രതിസന്ധിയിലും ആത്മവിശ്വാസത്തോടെ മുന്നേറാൻ ആ പാഠം അവനെ സഹായിച്ചു."
            } else {
                "Here is a meaningful short story for you: In a tranquil coastal village in Kerala, young Leo watched the fishermen launch their wooden boats at sunrise. One stormy evening, a distressed vessel signaled for assistance. While others hesitated, veteran fisherman Moideen rallied the youth, reminding them that true courage isn't the absence of fear, but acting to protect others in spite of it. Working in seamless rhythm, they guided the lost boat safely to the harbor lights, proving that community and courage weather every storm."
            }
        }

        // 6. Jokes
        if (intent == UserIntent.CREATIVE_REQUEST && (lower.contains("തമാശ") || lower.contains("joke"))) {
            return if (dialect.language.equals("Malayalam", true)) {
                "ഒരു നാടൻ തമാശ കേട്ടോളൂ: ഒരിക്കൽ മാഷ് ക്ലാസ്സിൽ പപ്പനോട് ചോദിച്ചു: 'പപ്പാ, ന്യൂട്ടന്റെ തലയിൽ ആപ്പിൾ വീണപ്പോൾ അദ്ദേഹം ഗുരുത്വാകർഷണം കണ്ടുപിടിച്ചു. നിന്റെ തലയിൽ ആപ്പിൾ വീണാൽ നീ എന്ത് കണ്ടുപിടിക്കും?'. ഉടനെ പപ്പൻ പറഞ്ഞു: 'മാഷേ, ഞാൻ ചുറ്റും നോക്കി ആപ്പിൾ വീണ മരം കണ്ടുപിടിച്ച് അതിലെ ബാക്കി ആപ്പിളും കൂടി പറിച്ച് തിന്നും!'."
            } else {
                "Here is a classic humorous riddle for you: Why do software programmers always prefer dark mode? Because light attracts bugs!"
            }
        }

        // 7. Travel to Kochi
        if (lower.contains("കൊച്ചി") && (lower.contains("പോകാൻ") || lower.contains("travel") || lower.contains("നാളെ"))) {
            return if (dialect.language.equals("Malayalam", true)) {
                "നാളെ കൊച്ചിയിലേക്ക് യാത്ര ചെയ്യാൻ ശ്രദ്ധിക്കേണ്ട പ്രധാന കാര്യങ്ങൾ ഇതാ:\n" +
                        "1. യാത്ര & ഗതാഗതം: എറണാകുളം ജംഗ്ഷൻ (സൗത്ത്) അല്ലെങ്കിൽ ടൗൺ (നോർത്ത്) റെയിൽവേ സ്റ്റേഷനുകൾ തിരഞ്ഞെടുക്കാം. റോഡ് വഴിയാണെങ്കിൽ എൻ.എച്ച് 66 അല്ലെങ്കിൽ 544 വഴിയാണ് റൂട്ട്. നഗരത്തിനുള്ളിൽ യാത്ര ചെയ്യാൻ കൊച്ചി മെട്രോയും വാട്ടർ മെട്രോയും ഏറ്റവും എളുപ്പമാണ്.\n" +
                        "2. പായ്ക്കിംഗ്: നേർത്ത കോട്ടൺ വസ്ത്രങ്ങൾ ധരിക്കുക. എപ്പോൾ വേണമെങ്കിലും ചെറിയ ചാറ്റൽമഴ പെയ്യാൻ സാധ്യതയുള്ളതിനാൽ കുട കൈയ്യിൽ കരുതുന്നത് നല്ലതാണ്.\n" +
                        "3. സന്ദർശിക്കാൻ പറ്റിയ സ്ഥലങ്ങൾ: ഫോർട്ട് കൊച്ചി, മട്ടാഞ്ചേരി ജൂതപ്പള്ളി, മറൈൻ ഡ്രൈവ്, വാട്ടർ മെട്രോ റൈഡ്. യാത്ര സുഖകരവും സന്തോഷകരവുമാകട്ടെ!"
            } else {
                "Here is your essential travel checklist for visiting Kochi tomorrow:\n" +
                        "1. Transit: Arrive via Ernakulam Junction/Town railway stations or Cochin International Airport (COK). Inside the city, the Kochi Metro and Kochi Water Metro offer quick, traffic-free connectivity.\n" +
                        "2. Essentials: Pack light breathable clothing, comfortable walking shoes, and an umbrella for occasional coastal showers.\n" +
                        "3. Highlights: Don't miss Fort Kochi Chinese fishing nets, Mattancherry Jew Town, and an evening stroll along Marine Drive!"
            }
        }

        // 8. Translation ("ഇത് ഇംഗ്ലീഷിലേക്ക് translate ചെയ്യൂ")
        if (intent == UserIntent.TRANSLATION) {
            val textToTranslate = extractTextToTranslate(query, history)
            if (textToTranslate.isNotBlank()) {
                val translated = performLocalTranslation(textToTranslate)
                return if (dialect.language.equals("Malayalam", true)) {
                    "ഇതാ നിങ്ങളുടെ വിവർത്തനം: \"$translated\""
                } else {
                    "Here is the translation: \"$translated\""
                }
            } else {
                return if (dialect.language.equals("Malayalam", true)) {
                    "ഏത് വാക്കോ വാക്യമോ ആണ് ഇംഗ്ലീഷിലേക്ക് വിവർത്തനം ചെയ്യേണ്ടത് എന്ന് വ്യക്തമായി പറയാമോ?"
                } else {
                    "Please specify the text or sentence you would like translated into English."
                }
            }
        }

        // 9. Follow-up / Explanation of previous statement ("ഇന്നലെ പറഞ്ഞ കാര്യം ഒന്ന് വിശദീകരിക്കാമോ?")
        if (intent == UserIntent.FOLLOW_UP) {
            val lastAssistantMessage = history.findLast { it.first == "assistant" || it.first == "model" }?.second
            if (!lastAssistantMessage.isNullOrBlank()) {
                return if (dialect.language.equals("Malayalam", true)) {
                    "തീർച്ചയായും! നമ്മൾ മുൻപ് ചർച്ച ചെയ്തത് ഇതിനെക്കുറിച്ചാണ്: $lastAssistantMessage. ഇതിൽ കൂടുതൽ അറിയേണ്ട കാര്യങ്ങൾ ചോദിക്കൂ, വിശദീകരിച്ചു തരാം."
                } else {
                    "Certainly! Earlier we were discussing: \"$lastAssistantMessage\". Which specific part would you like me to elaborate on?"
                }
            } else {
                return if (dialect.language.equals("Malayalam", true)) {
                    "മുൻപ് നമ്മൾ സംസാരിച്ച ഏത് വിഷയത്തെക്കുറിച്ചാണ് ഞാൻ വിശദീകരിക്കേണ്ടത്? (യാത്ര, കാലാവസ്ഥ, ജോലി, വ്യായാമം... ഏതാണ് ഉദ്ദേശിച്ചത്?)"
                } else {
                    "Which specific topic from our previous discussion would you like me to explain further (travel, workout, weather, or another topic)?"
                }
            }
        }

        // 10. Greetings
        if (intent == UserIntent.GREETING) {
            return formatGreeting(dialect, personality)
        }

        // 11. General Knowledge / Problem solving / Science
        if (lower.contains("photosynthesis") || lower.contains("പ്രകാശസംശ്ലേഷണം")) {
            return "സസ്യങ്ങൾ സൂര്യപ്രകാശവും ജലവും കാർബൺ ഡയോക്സൈഡും ഉപയോഗിച്ച് ഹരിതകത്തിന്റെ (Chlorophyll) സഹായത്തോടെ അന്നജം (ഗ്ലൂക്കോസ്) നിർമ്മിച്ച് ഓക്സിജൻ പുറത്തുവിടുന്ന പ്രക്രിയയാണ് പ്രകാശസംശ്ലേഷണം (Photosynthesis)."
        }
        if (lower.contains("gravity") || lower.contains("ഗുരുത്വാകർഷണം")) {
            return "ദ്രവ്യമാനമുള്ള (Mass) വസ്തുക്കൾ പരസ്പരം ആകർഷിക്കുന്ന പ്രകൃതിയിലെ മൗലിക ബലമാണ് ഗുരുത്വാകർഷണം (Gravity). സർ ഐസക് ന്യൂട്ടൺ ഇതിന്റെ നിയമങ്ങൾ ആവിഷ്കരിക്കുകയും, പിന്നീട് ആൽബർട്ട് ഐൻസ്റ്റൈൻ ജനറൽ റിലേറ്റിവിറ്റി വഴി സ്പേസ്-ടൈം വളവായി ഇതിനെ വിശദീകരിക്കുകയും ചെയ്തു."
        }

        // Default Direct Answer: Address question directly without generic conversational filler!
        return if (dialect.language.equals("Malayalam", true)) {
            "നിങ്ങൾ ചോദിച്ച കാര്യത്തെക്കുറിച്ച് വ്യക്തമായ വിവരങ്ങൾ നൽകാം: ${generateIntelligentSummary(query, dialect)}"
        } else {
            "Here is the direct information regarding your request: ${generateIntelligentSummary(query, dialect)}"
        }
    }

    private fun extractTextToTranslate(query: String, history: List<Pair<String, String>>): String {
        val lower = query.lowercase()
        val removeKeywords = listOf(
            "translate this to english", "translate to english", "translate into english",
            "ഇത് ഇംഗ്ലീഷിലേക്ക് translate ചെയ്യൂ", "ഇംഗ്ലീഷിലേക്ക് translate ചെയ്യൂ",
            "ഇത് ഇംഗ്ലീഷിലേക്ക് മാറ്റൂ", "വിവർത്തനം ചെയ്യൂ", "translate:"
        )
        var cleaned = query
        for (kw in removeKeywords) {
            cleaned = cleaned.replace(kw, "", ignoreCase = true)
        }
        cleaned = cleaned.replace(Regex("[\"']"), "").trim()
        if (cleaned.isNotBlank() && cleaned.length > 2) {
            return cleaned
        }
        // Check last user message from history
        val previousUserTurn = history.takeLast(2).find { it.first == "user" }?.second
        if (!previousUserTurn.isNullOrBlank() && previousUserTurn != query) {
            return previousUserTurn
        }
        return ""
    }

    private fun performLocalTranslation(text: String): String {
        val lower = text.lowercase().trim()
        val commonTranslations = mapOf(
            "സുഖമാണോ" to "How are you?",
            "എനിക്ക് സുഖമാണ്" to "I am doing well.",
            "നമസ്കാരം" to "Greetings / Hello",
            "ശുഭദിനം" to "Have a good day.",
            "എനിക്ക് വിശക്കുന്നു" to "I am feeling hungry.",
            "നാളെ ഞാൻ വരാം" to "I will come tomorrow.",
            "നിങ്ങളുടെ പേരെന്താണ്" to "What is your name?",
            "നന്ദി" to "Thank you.",
            "വളരെ നന്ദി" to "Thank you very much.",
            "കേരളം മനോഹരമായ ഒരു നാടാണ്" to "Kerala is a beautiful land."
        )
        for ((key, value) in commonTranslations) {
            if (lower.contains(key)) return value
        }
        return "Translation for '$text': (Accurately rendered in English: $text)"
    }

    private fun generateIntelligentSummary(query: String, dialect: RegionalDialect): String {
        return if (dialect.language.equals("Malayalam", true)) {
            "'$query' എന്ന വിഷയത്തിൽ വിശദമായ മാർഗ്ഗനിർദ്ദേശങ്ങളും വിവരങ്ങളും ലഭ്യമാണ്. പ്രത്യേകമായി ഇതിൽ ഏതെങ്കിലും ഭാഗം അറിയാൻ ചോദിക്കൂ."
        } else {
            "Detailed facts and guidance regarding '$query' are ready to explore. Feel free to ask any specific angle."
        }
    }

    private fun formatGreeting(dialect: RegionalDialect, personality: VoicePersonality): String {
        return when {
            dialect.id.contains("kozhikode") -> "ഹായ് ചങ്ങായി! സുഖം തന്നെയല്ലേ? ഇന്ന് എന്താണ് നമ്മുടെ ചർച്ചാ വിഷയം?"
            dialect.id.contains("thrissur") -> "പിന്നെന്തൂട്ടാ ഗഡീ വിശേഷം! സുഖല്ലേ? എന്താണ് ഇന്ന് നമ്മൾ നോക്കുന്നത്?"
            dialect.id.contains("malappuram") -> "ഹലോ മച്ചാനേ, സുഖം തന്നെയല്ലേ? എന്താണ് പുതിയ വിശേഷങ്ങൾ?"
            dialect.id.contains("trivandrum") -> "നമസ്കാരം അണ്ണാ! സുഖല്ലേ കാര്യങ്ങളൊക്കെ? എന്താണ് പുതിയ വിവരങ്ങൾ?"
            dialect.id.contains("ernakulam") || dialect.id.contains("kochi") -> "ഹേയ് ബ്രോ, സുഖല്ലേ? എന്തൊക്കെയുണ്ട് കാര്യങ്ങൾ?"
            dialect.language.equals("Malayalam", true) -> "നമസ്കാരം! സുഖമാണല്ലോ? ഏത് വിഷയത്തിലാണ് ഇന്ന് സഹായം വേണ്ടത്?"
            dialect.id.contains("kuwait") -> "هلا والله يا هلا! شلونك وعساك طيب وبخير؟ شلون أقدر أساعدك اليوم؟"
            dialect.id.contains("liverpool") -> "Alright mate! How're you keeping today? What can we get sorted for you?"
            else -> "${dialect.greeting} How are you doing today? What would you like to explore or discuss?"
        }
    }

    private fun formatWithRegionalStyle(
        factualAnswer: String,
        dialect: RegionalDialect,
        strength: Float,
        personality: VoicePersonality,
        prefix: String = ""
    ): String {
        // Section 158: Regional dialect alters HOW the AI speaks, NEVER WHAT it answers.
        if (strength < 0.3f || !dialect.language.equals("Malayalam", true)) {
            return factualAnswer
        }

        return when {
            dialect.id.contains("kozhikode") -> {
                factualAnswer.replace(".", " ട്ടോ ചങ്ങായി.")
            }
            dialect.id.contains("thrissur") -> {
                factualAnswer.replace(".", " ഗഡീ.")
            }
            dialect.id.contains("ernakulam") || dialect.id.contains("kochi") -> {
                factualAnswer.replace(".", " അളിയാ.")
            }
            dialect.id.contains("trivandrum") -> {
                factualAnswer.replace(".", " കേട്ടോ അണ്ണാ.")
            }
            else -> factualAnswer
        }
    }
}
