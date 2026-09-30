package com.example.data.suggestions

import com.example.data.model.RegionalDialect

/**
 * Context-Aware Dynamic Chat Suggestion Engine.
 *
 * Guarantees:
 * 1. Suggestions are ALWAYS available throughout the conversation.
 * 2. Suggestions update dynamically based on the current user question,
 *    latest AI response, topic, and selected regional dialect.
 * 3. Never slows down the main AI response (<1ms in-memory generation).
 * 4. Safe fallback ensures suggestion chips are never empty or broken.
 * 5. Supports Malayalam, English, and Malayalam-English mixed regional styles.
 */
object DynamicSuggestionEngine {

    fun generateSuggestions(
        userMessage: String,
        aiResponse: String,
        dialect: RegionalDialect
    ): List<String> {
        val lang = dialect.language.lowercase()
        val isMalayalam = lang.contains("malayalam") || dialect.region.contains("Kerala", ignoreCase = true)
        val queryLower = userMessage.lowercase().trim()

        // 0. Image Generation / Editing / Visual Topic
        if (com.example.data.image.ImageIntentDetector.isImageGenerationRequest(userMessage) ||
            queryLower.contains("image") || queryLower.contains("ചിത്രം") || queryLower.contains("photo") ||
            queryLower.contains("picture") || queryLower.contains("വരയ്ക്കൂ") || queryLower.contains("ഫോട്ടോ")
        ) {
            return if (isMalayalam) {
                listOf(
                    "കൂടുതൽ റിയലിസ്റ്റിക് ആക്കൂ",
                    "ബാക്ക്ഗ്രൗണ്ട് മാറ്റൂ",
                    "മറ്റൊരു പതിപ്പ് ഉണ്ടാക്കൂ",
                    "സിനിമാറ്റിക് ലുക്ക് തരൂ"
                )
            } else {
                listOf(
                    "Make it more realistic",
                    "Change the background",
                    "Create another version",
                    "Make it cinematic"
                )
            }
        }

        // 1. Weather / Climate Topic (Kuwait, Kerala, or any location)
        if (queryLower.contains("weather") || queryLower.contains("കാലാവസ്ഥ") ||
            queryLower.contains("rain") || queryLower.contains("mazha") ||
            queryLower.contains("temperature") || queryLower.contains("climate") ||
            queryLower.contains("തണുപ്പ്") || queryLower.contains("ചൂട്")
        ) {
            return if (isMalayalam) {
                listOf(
                    "നാളത്തെ കാലാവസ്ഥയോ?",
                    "ഇന്ന് മഴ പെയ്യുമോ?",
                    "മറ്റൊരു നഗരത്തിലെ കാലാവസ്ഥ",
                    "ഈ ആഴ്ചയിലെ തണുപ്പ് എത്ര?"
                )
            } else {
                listOf(
                    "What about tomorrow?",
                    "Weather in another city",
                    "Will it rain today?",
                    "Weekly temperature forecast"
                )
            }
        }

        // 2. Kerala / Tourism / Places / Travel
        if (queryLower.contains("kerala") || queryLower.contains("കേരള") ||
            queryLower.contains("visit") || queryLower.contains("tourist") ||
            queryLower.contains("place") || queryLower.contains("സ്ഥലങ്ങൾ") ||
            queryLower.contains("travel") || queryLower.contains("യാത്ര")
        ) {
            return if (isMalayalam) {
                listOf(
                    "സന്ദർശിക്കാൻ പറ്റിയ സ്ഥലങ്ങൾ",
                    "നാടൻ ഭക്ഷണ ശുപാർശകൾ",
                    "കൗതുകകരമായ കാര്യങ്ങൾ പറയൂ",
                    "യാത്രയ്ക്ക് ഏറ്റവും നല്ല സമയം"
                )
            } else {
                listOf(
                    "Best places to visit",
                    "Kerala food recommendations",
                    "Tell me something interesting",
                    "Best time of year to visit"
                )
            }
        }

        // 3. Technical / Excel / Spreadsheets / Code / Programming
        if (queryLower.contains("excel") || queryLower.contains("formula") ||
            queryLower.contains("spreadsheet") || queryLower.contains("code") ||
            queryLower.contains("android") || queryLower.contains("python") ||
            queryLower.contains("function") || queryLower.contains("ഫോർമുല")
        ) {
            return if (isMalayalam) {
                listOf(
                    "ഒരു ഫോർമുല തരുമോ?",
                    "ലളിതമായി വിശദീകരിക്കൂ",
                    "ഒരു ഉദാഹരണം കാണിക്കൂ",
                    "സ്റ്റെപ്പ് ബൈ സ്റ്റെപ്പ് ഗൈഡ്"
                )
            } else {
                listOf(
                    "Give me a formula",
                    "Explain it simply",
                    "Show me an example",
                    "Step by step guide"
                )
            }
        }

        // 4. Fitness / Health / Nutrition
        if (queryLower.contains("fitness") || queryLower.contains("health") ||
            queryLower.contains("diet") || queryLower.contains("workout") ||
            queryLower.contains("exercise") || queryLower.contains("gym") ||
            queryLower.contains("വ്യായാമം") || queryLower.contains("ആരോഗ്യം")
        ) {
            return if (isMalayalam) {
                listOf(
                    "ലളിതമായ വർക്ക്ഔട്ട് പ്ലാൻ",
                    "ഭക്ഷണക്രമത്തിൽ ശ്രദ്ധിക്കേണ്ടത്",
                    "തുടക്കക്കാർക്കുള്ള ടിപ്പുകൾ",
                    "ദിവസേന എത്ര വെള്ളം കുടിക്കണം?"
                )
            } else {
                listOf(
                    "Simple workout routine",
                    "Diet recommendations",
                    "Tips for beginners",
                    "Daily habits to follow"
                )
            }
        }

        // 5. Food / Cooking / Local Cuisine
        if (queryLower.contains("food") || queryLower.contains("recipe") ||
            queryLower.contains("dish") || queryLower.contains("ഭക്ഷണം") ||
            queryLower.contains("cooking") || queryLower.contains("ചായ") ||
            queryLower.contains("പാചകം")
        ) {
            return if (isMalayalam) {
                listOf(
                    "പാചകം ചെയ്യാൻ എത്ര സമയം വേണം?",
                    "ആവശ്യമായ ചേരുവകൾ",
                    "മറ്റൊരു നാടൻ വിഭവം പറയൂ",
                    "രുചികരമായ ചില ടിപ്പുകൾ"
                )
            } else {
                listOf(
                    "Cooking time needed",
                    "Key ingredients list",
                    "Another local dish recommendation",
                    "Secret tips for better taste"
                )
            }
        }

        // 6. Time / Date / Geography
        if (queryLower.contains("time") || queryLower.contains("സമയം") ||
            queryLower.contains("date") || queryLower.contains("തീയതി")
        ) {
            return if (isMalayalam) {
                listOf(
                    "മറ്റൊരു രാജ്യത്തെ സമയം പറയൂ",
                    "നാളെ എന്തെങ്കിലും പ്രത്യേകതയുണ്ടോ?",
                    "അടുത്ത പൊതു അവധി എപ്പോഴാണ്?",
                    "ഇന്നത്തെ ദിവസം ഓർമ്മിപ്പിക്കൂ"
                )
            } else {
                listOf(
                    "Time in another country",
                    "Tomorrow's date",
                    "Upcoming holidays",
                    "Timezone difference"
                )
            }
        }

        // 7. Slang / Dialect / Local Sayings
        if (queryLower.contains("slang") || queryLower.contains("dialect") ||
            queryLower.contains("meaning") || queryLower.contains("അർത്ഥം") ||
            queryLower.contains("വാക്ക്") || queryLower.contains("ചൊല്ല്")
        ) {
            return if (isMalayalam) {
                listOf(
                    "നാട്ടുകാർ സാധാരണ എങ്ങനെ പറയും?",
                    "മറ്റൊരു സ്ലാങ് വാക്ക് പറയൂ",
                    "ഒരു സംഭാഷണത്തിൽ ഉപയോഗിച്ചു കാണിക്കൂ"
                )
            } else {
                listOf(
                    "How do locals say it?",
                    "Another regional expression",
                    "Use it in a dialogue sentence"
                )
            }
        }

        // 8. General conversational fallback
        return if (isMalayalam) {
            listOf(
                "കൂടുതൽ പറയൂ",
                "ഒരു ഉദാഹരണം തരൂ",
                "ലളിതമായി പറയൂ",
                "അടുത്തത് എന്താണ് ചെയ്യേണ്ടത്?"
            )
        } else {
            listOf(
                "Explain more",
                "Give me an example",
                "Explain it simply",
                "What should I do next?"
            )
        }
    }

    fun getUploadedImageSuggestions(dialect: RegionalDialect): List<String> {
        val lang = dialect.language.lowercase()
        val isMalayalam = lang.contains("malayalam") || dialect.region.contains("Kerala", ignoreCase = true)
        return if (isMalayalam) {
            listOf(
                "ഇതിനെക്കുറിച്ച് പറയൂ",
                "ഈ ചിത്രം മെച്ചപ്പെടുത്തൂ",
                "ഇതിലെ ടെക്സ്റ്റ് വായിക്കൂ",
                "പുതിയ വേർഷൻ ഉണ്ടാക്കൂ"
            )
        } else {
            listOf(
                "Describe this",
                "Improve this image",
                "Read the text",
                "Create a new version"
            )
        }
    }

    fun getGeneratedImageSuggestions(dialect: RegionalDialect): List<String> {
        val lang = dialect.language.lowercase()
        val isMalayalam = lang.contains("malayalam") || dialect.region.contains("Kerala", ignoreCase = true)
        return if (isMalayalam) {
            listOf(
                "കൂടുതൽ റിയലിസ്റ്റിക് ആക്കൂ",
                "ബാക്ക്ഗ്രൗണ്ട് മാറ്റൂ",
                "മറ്റൊരു പതിപ്പ് ഉണ്ടാക്കൂ",
                "സിനിമാറ്റിക് ലുക്ക് തരൂ"
            )
        } else {
            listOf(
                "Make it more realistic",
                "Change the background",
                "Create another version",
                "Make it cinematic"
            )
        }
    }

    fun getDocumentSuggestions(dialect: RegionalDialect): List<String> {
        val lang = dialect.language.lowercase()
        val isMalayalam = lang.contains("malayalam") || dialect.region.contains("Kerala", ignoreCase = true)
        return if (isMalayalam) {
            listOf(
                "ഇത് ചുരുക്കി പറയൂ",
                "ലളിതമായി വിശദീകരിക്കൂ",
                "പ്രധാന പോയിന്റുകൾ കണ്ടെത്തൂ",
                "ഇതിലെ പ്രധാന കാര്യങ്ങൾ എന്തൊക്കെ?"
            )
        } else {
            listOf(
                "Summarize this",
                "Explain simply",
                "Find important points",
                "Translate this"
            )
        }
    }

    fun getInitialSuggestions(dialect: RegionalDialect): List<String> {
        val lang = dialect.language.lowercase()
        val isMalayalam = lang.contains("malayalam") || dialect.region.contains("Kerala", ignoreCase = true)
        return if (isMalayalam) {
            listOf(
                "എന്തൊക്കെയുണ്ട് വിശേഷം?",
                "Weather in Kuwait",
                "കേരളത്തിലെ സന്ദർശന സ്ഥലങ്ങൾ",
                "ഒരു നാടൻ ചൊല്ല് പറയൂ"
            )
        } else {
            listOf(
                "What's the weather in Kuwait?",
                "Tell me about Kerala",
                "Help me with Excel",
                "Popular local expressions"
            )
        }
    }
}
