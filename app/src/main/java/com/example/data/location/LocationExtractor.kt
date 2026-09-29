package com.example.data.location

import com.example.data.reasoning.UniversalReasoningEngine
import com.example.data.reasoning.UserIntent
import java.util.Locale

/**
 * Robust Location Extraction & Resolution Engine.
 *
 * Enforces the CORE RULE:
 * - "Selected Region" (styling, language, voice) and "Requested Location" (geographic query)
 *   are completely SEPARATE concepts.
 * - Selected regional profile (e.g., Kerala) MUST NEVER automatically determine geographic location!
 *
 * Priority for geographic queries:
 * 1. Explicit location in current user message
 * 2. Location resolved from conversation context (e.g., "there", "അവിടെ")
 * 3. User's chosen/default location
 * 4. Ask user if location is required (e.g. "What is the weather?" -> "Which location should I check?")
 */
object LocationExtractor {

    // Common known cities, states, and countries with normalized display names
    private val KNOWN_LOCATIONS = mapOf(
        // Middle East
        "kuwait" to "Kuwait",
        "kuwait city" to "Kuwait City",
        "dubai" to "Dubai",
        "abu dhabi" to "Abu Dhabi",
        "sharjah" to "Sharjah",
        "doha" to "Doha",
        "qatar" to "Qatar",
        "riyadh" to "Riyadh",
        "jeddah" to "Jeddah",
        "saudi arabia" to "Saudi Arabia",
        "muscat" to "Muscat",
        "oman" to "Oman",
        "manama" to "Manama",
        "bahrain" to "Bahrain",

        // Global Hubs
        "london" to "London",
        "tokyo" to "Tokyo",
        "paris" to "Paris",
        "new york" to "New York",
        "nyc" to "New York",
        "singapore" to "Singapore",
        "berlin" to "Berlin",
        "sydney" to "Sydney",
        "toronto" to "Toronto",
        "chicago" to "Chicago",
        "san francisco" to "San Francisco",
        "los angeles" to "Los Angeles",

        // India / Kerala Cities
        "kochi" to "Kochi",
        "cochin" to "Kochi",
        "ernakulam" to "Ernakulam",
        "kozhikode" to "Kozhikode",
        "calicut" to "Kozhikode",
        "thiruvananthapuram" to "Thiruvananthapuram",
        "trivandrum" to "Thiruvananthapuram",
        "thrissur" to "Thrissur",
        "trichur" to "Thrissur",
        "malappuram" to "Malappuram",
        "palakkad" to "Palakkad",
        "kannur" to "Kannur",
        "kollam" to "Kollam",
        "alappuzha" to "Alappuzha",
        "alleppey" to "Alappuzha",
        "kottayam" to "Kottayam",
        "wayanad" to "Wayanad",
        "idukki" to "Idukki",
        "kasaragod" to "Kasaragod",
        "pathanamthitta" to "Pathanamthitta",

        // India Metros
        "bengaluru" to "Bangalore",
        "bangalore" to "Bangalore",
        "chennai" to "Chennai",
        "madras" to "Chennai",
        "delhi" to "Delhi",
        "new delhi" to "New Delhi",
        "mumbai" to "Mumbai",
        "bombay" to "Mumbai",
        "hyderabad" to "Hyderabad",
        "kolkata" to "Kolkata",
        "pune" to "Pune",
        "ahmedabad" to "Ahmedabad",
        "goa" to "Goa",
        "farwaniya" to "Farwaniya",
        "kasaragod" to "Kasaragod",
        "kasargod" to "Kasaragod",

        // States & Countries
        "kerala" to "Kerala",
        "india" to "India",
        "tamil nadu" to "Tamil Nadu",
        "karnataka" to "Karnataka",
        "philippines" to "Philippines",
        "japan" to "Japan",
        "united states" to "United States",
        "usa" to "United States",
        "united kingdom" to "United Kingdom",
        "uk" to "United Kingdom"
    )

    // Malayalam words mapped to normalized English location names
    private val MALAYALAM_LOCATIONS = mapOf(
        // Kuwait & Gulf Cities
        "കുവൈറ്റ് സിറ്റി" to "Kuwait City",
        "കുവൈത്ത് സിറ്റി" to "Kuwait City",
        "കുവൈറ്റിലെ" to "Kuwait",
        "കുവൈറ്റിൽ" to "Kuwait",
        "കുവൈറ്റിലോ" to "Kuwait",
        "കുവൈറ്റിന്റെ" to "Kuwait",
        "കുവൈറ്റിലേക്ക്" to "Kuwait",
        "കുവൈറ്റ്" to "Kuwait",
        "കുവൈത്തിലെ" to "Kuwait",
        "കുവൈത്തിൽ" to "Kuwait",
        "കുവൈത്ത്" to "Kuwait",
        "ഫർവാനിയയിലെ" to "Farwaniya",
        "ഫർവാനിയയിൽ" to "Farwaniya",
        "ഫർവാനിയയിലോ" to "Farwaniya",
        "ഫർവാനിയ" to "Farwaniya",
        "ദുബായിലെ" to "Dubai",
        "ദുബായിൽ" to "Dubai",
        "ദുബായിലോ" to "Dubai",
        "ദുബായ്" to "Dubai",
        "ദുബൈയിലെ" to "Dubai",
        "ദുബൈയിൽ" to "Dubai",
        "ദുബൈലോ" to "Dubai",
        "ദുബൈ" to "Dubai",
        "അബുദാബിയിലെ" to "Abu Dhabi",
        "അബുദാബിയിൽ" to "Abu Dhabi",
        "അബുദാബിയിലോ" to "Abu Dhabi",
        "അബുദാബി" to "Abu Dhabi",
        "ഷാർജയിലെ" to "Sharjah",
        "ഷാർജയിൽ" to "Sharjah",
        "ഷാർജ" to "Sharjah",
        "ദോഹയിലെ" to "Doha",
        "ദോഹയിൽ" to "Doha",
        "ദോഹ" to "Doha",
        "ഖത്തറിലെ" to "Qatar",
        "ഖത്തറിൽ" to "Qatar",
        "ഖത്തർ" to "Qatar",
        "റിയാദിലെ" to "Riyadh",
        "റിയാദിൽ" to "Riyadh",
        "റിയാദ്" to "Riyadh",
        "സൗദി അറേബ്യയിലെ" to "Saudi Arabia",
        "സൗദി അറേബ്യയിൽ" to "Saudi Arabia",
        "സൗദി അറേബ്യ" to "Saudi Arabia",
        "സൗദിയിലെ" to "Saudi Arabia",
        "സൗദിയിൽ" to "Saudi Arabia",
        "സൗദി" to "Saudi Arabia",
        "ബഹ്റൈനിലെ" to "Bahrain",
        "ബഹ്റൈനിൽ" to "Bahrain",
        "ബഹ്റൈൻ" to "Bahrain",
        "മനാമ" to "Manama",
        "ഒമാനിലെ" to "Oman",
        "ഒമാനിൽ" to "Oman",
        "ഒമാൻ" to "Oman",
        "മസ്കറ്റിലെ" to "Muscat",
        "മസ്കറ്റിൽ" to "Muscat",
        "മസ്കറ്റ്" to "Muscat",

        // Global Hubs
        "ലണ്ടനിലെ" to "London",
        "ലണ്ടനിൽ" to "London",
        "ലണ്ടനിലോ" to "London",
        "ലണ്ടൻ" to "London",
        "ടോക്കിയോയിലെ" to "Tokyo",
        "ടോക്കിയോയിൽ" to "Tokyo",
        "ടോക്കിയോ" to "Tokyo",
        "പാരീസിലെ" to "Paris",
        "പാരീസിൽ" to "Paris",
        "പാരീസ്" to "Paris",
        "ഫ്രാൻസ്" to "France",
        "ന്യൂയോർക്കിലെ" to "New York",
        "ന്യൂയോർക്കിൽ" to "New York",
        "ന്യൂയോർക്ക്" to "New York",
        "സിംഗപ്പൂരിലെ" to "Singapore",
        "സിംഗപ്പൂരിൽ" to "Singapore",
        "സിംഗപ്പൂർ" to "Singapore",
        "ജപ്പാനിലെ" to "Japan",
        "ജപ്പാനിൽ" to "Japan",
        "ജപ്പാൻ" to "Japan",

        // Kerala Districts & Cities
        "കൊച്ചിയിലെ" to "Kochi",
        "കൊച്ചിയിൽ" to "Kochi",
        "കൊച്ചിയിലോ" to "Kochi",
        "കൊച്ചിയുടെ" to "Kochi",
        "കൊച്ചി" to "Kochi",
        "എറണാകുളത്തെ" to "Ernakulam",
        "എറണാകുളത്ത്" to "Ernakulam",
        "എറണാകുളത്തോ" to "Ernakulam",
        "എറണാകുളം" to "Ernakulam",
        "കോഴിക്കോട്ടിലെ" to "Kozhikode",
        "കോഴിക്കോട്ടെ" to "Kozhikode",
        "കോഴിക്കോട്ടിൽ" to "Kozhikode",
        "കോഴിക്കോട്ട്" to "Kozhikode",
        "കോഴിക്കോടോ" to "Kozhikode",
        "കോഴിക്കോട്ടോ" to "Kozhikode",
        "കോഴിക്കോട്" to "Kozhikode",
        "കാലിക്കറ്റ്" to "Kozhikode",
        "കാസർഗോഡിലെ" to "Kasaragod",
        "കാസർഗോഡിൽ" to "Kasaragod",
        "കാസർഗോഡോ" to "Kasaragod",
        "കാസർഗോഡ്" to "Kasaragod",
        "കാസർകോട്ടിലെ" to "Kasaragod",
        "കാസർകോട്ടെ" to "Kasaragod",
        "കാസർകോട്ടിൽ" to "Kasaragod",
        "കാസർകോട്ടോ" to "Kasaragod",
        "കാസർകോട്" to "Kasaragod",
        "തിരുവനന്തപുരത്തെ" to "Thiruvananthapuram",
        "തിരുവനന്തപുരത്ത്" to "Thiruvananthapuram",
        "തിരുവനന്തപുരത്തോ" to "Thiruvananthapuram",
        "തിരുവനന്തപുരം" to "Thiruvananthapuram",
        "ട്രിവാൻഡ്രം" to "Thiruvananthapuram",
        "തൃശ്ശൂരിലെ" to "Thrissur",
        "തൃശ്ശൂരിൽ" to "Thrissur",
        "തൃശ്ശൂരിലോ" to "Thrissur",
        "തൃശ്ശൂർ" to "Thrissur",
        "തൃശൂരിലെ" to "Thrissur",
        "തൃശൂരിൽ" to "Thrissur",
        "തൃശൂരിലോ" to "Thrissur",
        "തൃശൂർ" to "Thrissur",
        "മലപ്പുറത്തെ" to "Malappuram",
        "മലപ്പുറത്ത്" to "Malappuram",
        "മലപ്പുറത്തോ" to "Malappuram",
        "മലപ്പുറം" to "Malappuram",
        "പാലക്കാട്ടെ" to "Palakkad",
        "പാലക്കാട്ട്" to "Palakkad",
        "പാലക്കാട്ടിൽ" to "Palakkad",
        "പാലക്കാട്ടോ" to "Palakkad",
        "പാലക്കാട്" to "Palakkad",
        "കണ്ണൂരിലെ" to "Kannur",
        "കണ്ണൂരിൽ" to "Kannur",
        "കണ്ണൂരിലോ" to "Kannur",
        "കണ്ണൂർ" to "Kannur",
        "വയനാട്ടിലെ" to "Wayanad",
        "വയനാട്ടിൽ" to "Wayanad",
        "വയനാട്ടിലോ" to "Wayanad",
        "വയനാട്" to "Wayanad",
        "ഇടുക്കിയിലെ" to "Idukki",
        "ഇടുക്കിയിൽ" to "Idukki",
        "ഇടുക്കിയിലോ" to "Idukki",
        "ഇടുക്കി" to "Idukki",
        "കൊല്ലത്തെ" to "Kollam",
        "കൊല്ലത്ത്" to "Kollam",
        "കൊല്ലത്തോ" to "Kollam",
        "കൊല്ലം" to "Kollam",
        "ആലപ്പുഴയിലെ" to "Alappuzha",
        "ആലപ്പുഴയിൽ" to "Alappuzha",
        "ആലപ്പുഴയിലോ" to "Alappuzha",
        "ആലപ്പുഴ" to "Alappuzha",
        "കോട്ടയത്തെ" to "Kottayam",
        "കോട്ടയത്ത്" to "Kottayam",
        "കോട്ടയത്തോ" to "Kottayam",
        "കോട്ടയം" to "Kottayam",
        "പത്തനംതിട്ടയിലെ" to "Pathanamthitta",
        "പത്തനംതിട്ടയിൽ" to "Pathanamthitta",
        "പത്തനംതിട്ട" to "Pathanamthitta",

        // India Metros & States
        "ബാംഗ്ലൂരിലെ" to "Bangalore",
        "ബാംഗ്ലൂരിൽ" to "Bangalore",
        "ബാംഗ്ലൂർ" to "Bangalore",
        "ബംഗളൂരു" to "Bangalore",
        "ചെന്നൈയിലെ" to "Chennai",
        "ചെന്നൈയിൽ" to "Chennai",
        "ചെന്നൈ" to "Chennai",
        "ഡൽഹിയിലെ" to "Delhi",
        "ഡൽഹിയിൽ" to "Delhi",
        "ഡൽഹി" to "Delhi",
        "മുംബൈയിലെ" to "Mumbai",
        "മുംബൈയിൽ" to "Mumbai",
        "മുംബൈ" to "Mumbai",
        "ഹൈദരാബാദ്" to "Hyderabad",
        "കൊൽക്കത്ത" to "Kolkata",
        "കേരളത്തിലെ" to "Kerala",
        "കേരളത്തിൽ" to "Kerala",
        "കേരളത്തിലോ" to "Kerala",
        "കേരളത്തെ" to "Kerala",
        "കേരളത്തിന്റെ" to "Kerala",
        "കേരളം" to "Kerala",
        "ഇന്ത്യയിലെ" to "India",
        "ഇന്ത്യയിൽ" to "India",
        "ഇന്ത്യയിലോ" to "India",
        "ഇന്ത്യ" to "India",
        "അമേരിക്കയിലെ" to "United States",
        "അമേരിക്കയിൽ" to "United States",
        "അമേരിക്ക" to "United States"
    )

    /**
     * Resolves the complete QueryContext from user query, history, and user's default location.
     */
    fun analyzeQuery(
        query: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        userLocation: UserLocation = UserLocation(),
        regionalProfile: com.example.data.regional.RegionalProfile? = null
    ): QueryContext {
        return LocationResolver.resolveQueryContext(query, conversationHistory, userLocation, regionalProfile)
    }

    /**
     * Extracts an explicit location from the text (English & Malayalam).
     */
    fun extractExplicitLocation(originalText: String, lowerText: String): String? {
        // A. Check Malayalam dictionary matches first (longer phrases first)
        val sortedMal = MALAYALAM_LOCATIONS.keys.sortedByDescending { it.length }
        for (malKey in sortedMal) {
            if (originalText.contains(malKey)) {
                return MALAYALAM_LOCATIONS[malKey]
            }
        }

        // B. Check known English location mapping (longer phrases first, e.g. "Kuwait City" before "Kuwait")
        val sortedKnown = KNOWN_LOCATIONS.keys.sortedByDescending { it.length }
        for (known in sortedKnown) {
            val regex = Regex("\\b${Regex.escape(known)}\\b", RegexOption.IGNORE_CASE)
            if (regex.containsMatchIn(lowerText)) {
                return KNOWN_LOCATIONS[known]
            }
        }

        // C. Pattern matches for English:
        // "in [Location]", "at [Location]", "for [Location]", "weather in [Location]", "hot is [Location]"
        val patterns = listOf(
            Regex("(?:in|at|for|around|near)\\s+([A-Za-z\\s]{2,20})(?:\\s+tomorrow|\\s+today|\\s+tonight|\\s*\\?|$)", RegexOption.IGNORE_CASE),
            Regex("(?:weather|temperature|forecast|rain|time|restaurants|hotels)\\s+(?:in|of|at|for|around)\\s+([A-Za-z\\s]{2,20})(?:\\s+tomorrow|\\s+today|\\s*\\?|$)", RegexOption.IGNORE_CASE),
            Regex("(?:how\\s+hot\\s+is|how\\s+cold\\s+is)\\s+([A-Za-z\\s]{2,20})(?:\\s+tomorrow|\\s+today|\\s*\\?|$)", RegexOption.IGNORE_CASE),
            Regex("\\b([A-Z][a-zA-Z]{1,15}(?:\\s+[A-Z][a-zA-Z]{1,15})?)\\s+(?:weather|temperature|forecast|time|restaurants)(?:\\s+tomorrow|\\s+today|\\s*\\?|$)", RegexOption.IGNORE_CASE),
            Regex("(?:what's happening in|happening in|news in|events in)\\s+([A-Za-z\\s]{2,20})(?:\\s*\\?|$)", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(originalText)
            if (match != null) {
                val candidate = match.groupValues[1].trim()
                    .replace(Regex("(?i)\\b(tomorrow|today|tonight|right now|currently|please|now)\\b"), "")
                    .trim()
                if (isValidLocationName(candidate)) {
                    // Normalize if known
                    return KNOWN_LOCATIONS[candidate.lowercase(Locale.ROOT)] ?: capitalizeLocation(candidate)
                }
            }
        }

        return null
    }

    /**
     * Determines whether the query refers to a contextual place ("there", "അവിടെ", "അവിടുത്തെ").
     */
    fun isContextualLocationReference(lowerText: String): Boolean {
        return lowerText.contains("there") ||
                lowerText.contains("that place") ||
                lowerText.contains("same place") ||
                lowerText.contains("അവിടെ") ||
                lowerText.contains("അവിടുത്തെ") ||
                lowerText.contains("അവിടത്തെ")
    }

    /**
     * Determines whether the query refers to current/here device location.
     */
    fun isDeviceLocationReference(lowerText: String): Boolean {
        return lowerText.contains("here") ||
                lowerText.contains("around here") ||
                lowerText.contains("right here") ||
                lowerText.contains("my location") ||
                lowerText.contains("current location") ||
                lowerText.contains("ഇവിടെ") ||
                lowerText.contains("ഇവിടുത്തെ") ||
                lowerText.contains("ഇവിടത്തെ") ||
                lowerText.contains("എന്റെ സ്ഥലം") ||
                lowerText.contains("ഞാനിരിക്കുന്ന സ്ഥലം")
    }

    /**
     * Scans recent conversation history in reverse to find the most recent geographic location.
     */
    fun findRecentLocationInHistory(history: List<Pair<String, String>>): String? {
        for (i in history.indices.reversed()) {
            val (_, text) = history[i]
            val found = extractExplicitLocation(text, text.lowercase(Locale.ROOT))
            if (found != null) {
                return found
            }
        }
        return null
    }

    /**
     * Checks if a query is dependent on geographic location.
     */
    fun isLocationDependentQuery(lowerText: String): Boolean {
        // Weather
        if (lowerText.contains("weather") || lowerText.contains("temperature") ||
            lowerText.contains("rain") || lowerText.contains("forecast") ||
            lowerText.contains("hot") || lowerText.contains("cold") ||
            lowerText.contains("കാലാവസ്ഥ") || lowerText.contains("മഴ") ||
            lowerText.contains("ചൂട്") || lowerText.contains("തണുപ്പ്")) {
            return true
        }

        // Restaurants & Food
        if (lowerText.contains("restaurant") || lowerText.contains("restaurants") ||
            lowerText.contains("cafe") || lowerText.contains("dining") ||
            lowerText.contains("റെസ്റ്റോറന്റ്") || lowerText.contains("ഹോട്ടൽ") ||
            lowerText.contains("ഭക്ഷണം")) {
            return true
        }

        // Hotels / Stays
        if (lowerText.contains("hotel") || lowerText.contains("hotels") ||
            lowerText.contains("resort") || lowerText.contains("stay") ||
            lowerText.contains("താമസം")) {
            return true
        }

        // Flights / Airports
        if (lowerText.contains("flight") || lowerText.contains("airport") ||
            lowerText.contains("വിമാനം") || lowerText.contains("എയർപോർട്ട്")) {
            return true
        }

        // Traffic / Transit
        if (lowerText.contains("traffic") || lowerText.contains("metro") ||
            lowerText.contains("transit") || lowerText.contains("ഗതാഗതം")) {
            return true
        }

        // News & Local Events
        if (lowerText.contains("happening in") || lowerText.contains("news in") ||
            lowerText.contains("events in") || lowerText.contains("വാർത്തകൾ") ||
            lowerText.contains("വിശേഷങ്ങൾ")) {
            return true
        }

        // Time
        if (lowerText.contains("time in") || lowerText.contains("what's the time in") ||
            lowerText.contains("current time in") || lowerText.contains("സമയം എത്ര")) {
            return true
        }

        // Travel
        if (lowerText.contains("travel to") || lowerText.contains("trip to") ||
            lowerText.contains("visit") || lowerText.contains("യാത്ര")) {
            return true
        }

        return false
    }

    private val STOP_WORDS = setOf(
        "the", "a", "an", "is", "it", "will", "does", "what", "whats", "what's", "how", "when",
        "where", "why", "me", "you", "him", "her", "us", "them", "good",
        "bad", "nice", "hello", "hi", "hey", "like", "today", "tomorrow", "tonight",
        "there", "here", "somewhere", "anywhere", "place", "city", "country",
        "weather", "forecast", "time", "temperature", "news", "restaurant", "restaurants"
    )

    private fun isValidLocationName(candidate: String): Boolean {
        val trimmed = candidate.trim().lowercase(Locale.ROOT)
        if (trimmed.length < 2 || trimmed.length > 30) return false
        val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return false
        if (words.all { STOP_WORDS.contains(it) }) return false
        if (STOP_WORDS.contains(trimmed)) return false
        if (trimmed == "there" || trimmed == "here" || trimmed.endsWith(" there")) return false
        return true
    }

    private fun capitalizeLocation(str: String): String {
        return str.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }
    }
}
