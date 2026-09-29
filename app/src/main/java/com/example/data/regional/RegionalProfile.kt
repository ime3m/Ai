package com.example.data.regional

import com.example.data.model.RegionalDialect
import com.example.data.repository.DialectCatalog

/**
 * Unified RegionalProfile:
 * Single source of truth for:
 * 1. Default geographical location
 * 2. Response language & code
 * 3. Regional dialect & style
 * 4. Voice language & locale
 * 5. Regional AI personality, greetings, and expressions
 */
data class RegionalProfile(
    val id: String,
    val name: String,
    val flagEmoji: String,
    val country: String,
    val defaultLocation: String,
    val language: String,
    val languageCode: String,
    val localeCode: String,
    val dialectName: String,
    val dialectId: String,
    val voiceLanguage: String,
    val voiceLocaleCode: String,
    val conversationalPersona: String,
    val culturalContext: String,
    val commonExpressions: List<String>,
    val greeting: String,
    val samplePhrases: List<String> = emptyList(),
    val systemStyle: String = "natural",
    val dateFormat: String = "dd/MM/yyyy",
    val measurementSystem: String = "Metric",
    val languages: List<String> = listOf(language),
    val style: String = systemStyle,
    val defaultLanguage: String = languageCode,
    val defaultLocaleCode: String = localeCode,
    val defaultDialectId: String = dialectId
) {
    val dialect: String get() = dialectId
    val voice: String get() = dialectId
    val displayName: String get() = name

    fun toRegionalDialect(): RegionalDialect {
        return DialectCatalog.dialects.find { it.id == dialectId } ?: RegionalDialect(
            id = dialectId,
            language = language,
            country = country,
            stateOrProvince = if (country == "India") "Kerala" else country,
            region = name,
            cityOrArea = name,
            dialectName = dialectName,
            flagEmoji = flagEmoji,
            samplePhrases = samplePhrases,
            greeting = greeting,
            localeCode = voiceLocaleCode,
            description = "$name regional profile with authentic $language phrasing and cadence.",
            codeSwitchingDescription = "Authentic $language communication.",
            tendenciesNotes = conversationalPersona,
            typicalExpressions = emptyList()
        )
    }
}

/**
 * Registry of Standard Pre-configured Regional Profiles.
 * Defaults to Kerala on first launch.
 */
object RegionalProfileRegistry {

    // 1. JAPAN
    val JAPAN = RegionalProfile(
        id = "japan",
        name = "Japan",
        flagEmoji = "🇯🇵",
        country = "Japan",
        defaultLocation = "Japan",
        language = "Japanese",
        languageCode = "ja",
        localeCode = "ja-JP",
        dialectName = "Japanese",
        dialectId = "ja_jp_tokyo",
        voiceLanguage = "Japanese",
        voiceLocaleCode = "ja-JP",
        conversationalPersona = "Thoughtful, polite, articulate Japanese companion with natural respectful phrasing",
        culturalContext = "Japanese society, omotenashi hospitality, seasonal appreciation, tradition and technology",
        commonExpressions = listOf("こんにちは (Konnichiwa)", "大丈夫 (Daijoubu)", "よろしく (Yoroshiku)", "ありがとう (Arigatou)", "お疲れ様です (Otsukaresama)"),
        greeting = "こんにちは！お元気ですか？今日はどのようなお手伝いをしましょうか？",
        samplePhrases = listOf("こんにちは、元気ですか？", "今日の東京の天気はどうですか？", "おすすめの観光地を教えてください"),
        systemStyle = "polite_japanese",
        dateFormat = "yyyy/MM/dd",
        measurementSystem = "Metric"
    )

    // 2. KERALA
    val KERALA = RegionalProfile(
        id = "kerala",
        name = "Kerala",
        flagEmoji = "🇮🇳",
        country = "India",
        defaultLocation = "Kerala, India",
        language = "Malayalam",
        languageCode = "ml",
        localeCode = "ml-IN",
        dialectName = "Kerala Malayalam",
        dialectId = "ml_in_kl_general",
        voiceLanguage = "Malayalam",
        voiceLocaleCode = "ml-IN",
        conversationalPersona = "Warm, knowledgeable, friendly local partner from Kerala with natural conversational flow",
        culturalContext = "Kerala culture, local nuances, tea shop banter, contemporary life in Kerala",
        commonExpressions = listOf("എന്താ വിശേഷം", "സാരമില്ല", "പിന്നെന്താ", "അതങ്ങനെയാണ്", "കാര്യം എളുപ്പമാണ്"),
        greeting = "നമസ്കാരം! എന്തുണ്ട് വിശേഷം? സുഖമായിരിക്കുന്നോ? നമുക്ക് സംസാരിക്കാം.",
        samplePhrases = listOf("എന്തുണ്ട് വിശേഷം?", "ഇന്നത്തെ കാലാവസ്ഥ എങ്ങനെയാണ്?", "കേരളത്തിലെ വിശേഷങ്ങൾ പറയൂ"),
        systemStyle = "natural_kerala_malayalam",
        dateFormat = "dd/MM/yyyy",
        measurementSystem = "Metric"
    )

    // 3. MALAPPURAM
    val MALAPPURAM = RegionalProfile(
        id = "malappuram",
        name = "Malappuram",
        flagEmoji = "🇮🇳",
        country = "India",
        defaultLocation = "Malappuram, Kerala, India",
        language = "Malayalam",
        languageCode = "ml",
        localeCode = "ml-IN",
        dialectName = "Malappuram Malayalam",
        dialectId = "ml_in_kl_malappuram",
        voiceLanguage = "Malayalam",
        voiceLocaleCode = "ml-IN",
        conversationalPersona = "Warm, emotive Ernad/Valluvanad camaraderie with authentic Malappuram slang and football warmth",
        culturalContext = "Malappuram culture, football love, sulaimani, Ernad dialect idioms",
        commonExpressions = listOf("ഇജ്ജ് എവിടെയാ", "അസ്സലാമു അലൈക്കും", "ആട്ടെ", "കട്ടക്ക് കൂടിക്കോ", "തീർച്ചയായും"),
        greeting = "അസ്സലാമു അലൈക്കും! ഇജ്ജ് എവിടെയായിരുന്നു? എന്തൊക്കെയുണ്ട് മലപ്പുറത്തെ വർത്തമാനം?",
        samplePhrases = listOf("ഇജ്ജ് എന്തെടുക്കുവാണ്?", "മലപ്പുറത്ത് ഇന്ന് മഴയുണ്ടോ?", "എന്തൊക്കെയുണ്ട് വിശേഷങ്ങൾ?"),
        systemStyle = "ernad_malappuram_malayalam",
        dateFormat = "dd/MM/yyyy",
        measurementSystem = "Metric"
    )

    // 4. KOZHIKODE
    val KOZHIKODE = RegionalProfile(
        id = "kozhikode",
        name = "Kozhikode",
        flagEmoji = "🇮🇳",
        country = "India",
        defaultLocation = "Kozhikode, Kerala, India",
        language = "Malayalam",
        languageCode = "ml",
        localeCode = "ml-IN",
        dialectName = "Kozhikode Malayalam",
        dialectId = "ml_in_kl_kozhikode",
        voiceLanguage = "Malayalam",
        voiceLocaleCode = "ml-IN",
        conversationalPersona = "Affectionate Malabar cadence with signature Changayi address, culinary passion, and genuine warmth",
        culturalContext = "Kozhikode culinary heritage, beach walks, SM Street, hospitality",
        commonExpressions = listOf("ചങ്ങായി", "ട്ടോ", "ഉഷാറല്ലേ", "എന്താപ്പാ ഇത്", "കട്ടക്ക് നിക്കാം"),
        greeting = "നമസ്കാരം ചങ്ങായി! എന്തൊക്കെയുണ്ട് കോഴിക്കോട്ടെ വിശേഷങ്ങൾ? സുഖമല്ലേ?",
        samplePhrases = listOf("എന്താ ചങ്ങായി വിശേഷം?", "കോഴിക്കോട്ട് ഇന്ന് മഴയുണ്ടോ?", "മിഠായിത്തെരുവിൽ നല്ല തിരക്കുണ്ടോ?"),
        systemStyle = "malabar_kozhikode_malayalam",
        dateFormat = "dd/MM/yyyy",
        measurementSystem = "Metric"
    )

    // 5. THIRUVANANTHAPURAM
    val THIRUVANANTHAPURAM = RegionalProfile(
        id = "thiruvananthapuram",
        name = "Thiruvananthapuram",
        flagEmoji = "🇮🇳",
        country = "India",
        defaultLocation = "Thiruvananthapuram, Kerala, India",
        language = "Malayalam",
        languageCode = "ml",
        localeCode = "ml-IN",
        dialectName = "Thiruvananthapuram Malayalam",
        dialectId = "ml_in_kl_thiruvananthapuram",
        voiceLanguage = "Malayalam",
        voiceLocaleCode = "ml-IN",
        conversationalPersona = "Capital city southern Travancore vernacular with brisk interrogatives, royal banter, and witty rhythm",
        culturalContext = "Thiruvananthapuram culture, Secretariat, Kovalam, southern Travancore idioms",
        commonExpressions = listOf("എന്തുവാടെ", "അണ്ണാ", "കേട്ടോ", "എന്തര്", "പിന്നല്ലാതെ"),
        greeting = "നമസ്കാരം! എന്തുവാടെ വിശേഷം? കാര്യങ്ങളൊക്കെ സുഖമായി നടക്കുന്നുണ്ടോ?",
        samplePhrases = listOf("എന്തുവാടെ വിശേഷം?", "തിരുവനന്തപുരത്ത് ഇന്ന് മഴയുണ്ടോ?", "കാര്യങ്ങൾ എങ്ങനെയുണ്ട്?"),
        systemStyle = "travancore_trivandrum_malayalam",
        dateFormat = "dd/MM/yyyy",
        measurementSystem = "Metric"
    )

    // 6. KUWAIT
    val KUWAIT = RegionalProfile(
        id = "kuwait",
        name = "Kuwait",
        flagEmoji = "🇰🇼",
        country = "Kuwait",
        defaultLocation = "Kuwait",
        language = "Arabic",
        languageCode = "ar",
        localeCode = "ar-KW",
        dialectName = "Gulf Arabic",
        dialectId = "ar_kw_kuwait",
        voiceLanguage = "Arabic",
        voiceLocaleCode = "ar-KW",
        conversationalPersona = "Generous, polite, articulate Gulf companion with authentic Diwaniya warmth",
        culturalContext = "Kuwaiti and GCC society, maritime trade, hospitality, modern developments",
        commonExpressions = listOf("Shlonik", "Hala wallah", "Abshir", "Inshallah"),
        greeting = "هلا والله! شلونك؟ عساك بخير؟ شلون أقدر أساعدك اليوم؟",
        samplePhrases = listOf("شلونك اليوم؟", "كم درجة الحرارة في الكويت؟", "أبي مطعم حلو بالكويت"),
        systemStyle = "gulf_arabic",
        dateFormat = "dd/MM/yyyy",
        measurementSystem = "Metric"
    )

    // 7. UNITED STATES
    val UNITED_STATES = RegionalProfile(
        id = "united_states",
        name = "United States",
        flagEmoji = "🇺🇸",
        country = "United States",
        defaultLocation = "United States",
        language = "English",
        languageCode = "en",
        localeCode = "en-US",
        dialectName = "American English",
        dialectId = "en_us_ny_brooklyn",
        voiceLanguage = "English",
        voiceLocaleCode = "en-US",
        conversationalPersona = "Casual, direct, clear, modern American conversational companion",
        culturalContext = "US culture, technology, sports, arts, straightforward communication",
        commonExpressions = listOf("What's up", "Sounds good", "No worries", "Got it"),
        greeting = "Hey there! How's it going today? What can I help you explore?",
        samplePhrases = listOf("What's up?", "How is the weather today?", "Tell me something interesting"),
        systemStyle = "american_english",
        dateFormat = "MM/dd/yyyy",
        measurementSystem = "Imperial"
    )

    // 8. UNITED KINGDOM
    val UNITED_KINGDOM = RegionalProfile(
        id = "united_kingdom",
        name = "United Kingdom",
        flagEmoji = "🇬🇧",
        country = "United Kingdom",
        defaultLocation = "United Kingdom",
        language = "English",
        languageCode = "en",
        localeCode = "en-GB",
        dialectName = "British English",
        dialectId = "en_gb_eng_london",
        voiceLanguage = "English",
        voiceLocaleCode = "en-GB",
        conversationalPersona = "Polite, witty, thoughtful British conversational companion",
        culturalContext = "British society, understated humor, tea culture, current affairs",
        commonExpressions = listOf("Cheers", "Brilliant", "Spot on", "Right then"),
        greeting = "Hello there! How are things with you today? Fancy a chat?",
        samplePhrases = listOf("Alright mate?", "What's the weather in London?", "Tell me about British history"),
        systemStyle = "british_english",
        dateFormat = "dd/MM/yyyy",
        measurementSystem = "Metric"
    )

    // 9. INDIA GENERAL
    val INDIA_GENERAL = RegionalProfile(
        id = "india",
        name = "India",
        flagEmoji = "🇮🇳",
        country = "India",
        defaultLocation = "India",
        language = "English",
        languageCode = "en",
        localeCode = "en-IN",
        dialectName = "Indian English",
        dialectId = "en_in_general",
        voiceLanguage = "English",
        voiceLocaleCode = "en-IN",
        conversationalPersona = "Respectful, warm, articulate Pan-Indian conversational assistant",
        culturalContext = "Indian diversity, contemporary tech, pan-Indian festivals, sports, geography, and general knowledge",
        commonExpressions = listOf("No problem", "Tell me", "Let's see", "Sure thing"),
        greeting = "Namaste! How are you doing today? What can I assist you with?",
        samplePhrases = listOf("Namaste!", "What's the latest in India?", "Tell me a good recipe"),
        systemStyle = "indian_english_multilingual",
        dateFormat = "dd/MM/yyyy",
        measurementSystem = "Metric"
    )

    // 10. TAMIL NADU
    val TAMIL_NADU = RegionalProfile(
        id = "tamil_nadu",
        name = "Tamil Nadu",
        flagEmoji = "🇮🇳",
        country = "India",
        defaultLocation = "Tamil Nadu, India",
        language = "Tamil",
        languageCode = "ta",
        localeCode = "ta-IN",
        dialectName = "Tamil",
        dialectId = "ta_in_chennai",
        voiceLanguage = "Tamil",
        voiceLocaleCode = "ta-IN",
        conversationalPersona = "Vibrant, friendly, knowledgeable Tamil Nadu native with authentic local phrasing",
        culturalContext = "Tamil Nadu, Chennai & Madurai local expressions, food, tech, cinema, and history",
        commonExpressions = listOf("Enna vishayam", "Kandippa", "Solranga", "Paravala"),
        greeting = "Vanakkam! Eppadi irukkeenga? Namma pesalama?",
        samplePhrases = listOf("Vanakkam!", "Chennai la innaiku mazhai irukka?", "Enna vishayam?"),
        systemStyle = "natural_tamil",
        dateFormat = "dd/MM/yyyy",
        measurementSystem = "Metric"
    )

    // 11. KARNATAKA
    val KARNATAKA = RegionalProfile(
        id = "karnataka",
        name = "Karnataka",
        flagEmoji = "🇮🇳",
        country = "India",
        defaultLocation = "Karnataka, India",
        language = "Kannada",
        languageCode = "kn",
        localeCode = "kn-IN",
        dialectName = "Kannada",
        dialectId = "kn_in_bangalore",
        voiceLanguage = "Kannada",
        voiceLocaleCode = "kn-IN",
        conversationalPersona = "Articulate, polite, modern Bangalore & Karnataka companion",
        culturalContext = "Karnataka heritage, tech culture, coffee tradition, Kannada literature",
        commonExpressions = listOf("En samachara", "Houda", "Thumbha chennagide", "Banni"),
        greeting = "Namaskara! Hegiddheera? Enu samachara?",
        samplePhrases = listOf("Namaskara!", "Bangalore weather hegidhe?", "Enu vishesha?"),
        systemStyle = "natural_kannada",
        dateFormat = "dd/MM/yyyy",
        measurementSystem = "Metric"
    )

    // 12. PHILIPPINES
    val PHILIPPINES = RegionalProfile(
        id = "philippines",
        name = "Philippines",
        flagEmoji = "🇵🇭",
        country = "Philippines",
        defaultLocation = "Philippines",
        language = "Filipino",
        languageCode = "fil",
        localeCode = "fil-PH",
        dialectName = "Filipino Tagalog",
        dialectId = "fil_ph_manila",
        voiceLanguage = "Filipino",
        voiceLocaleCode = "fil-PH",
        conversationalPersona = "Exceptionally warm, cheerful, and helpful Filipino companion",
        culturalContext = "Bayanihan spirit, Filipino humor, Taglish code-switching, Philippine archipelago",
        commonExpressions = listOf("Kumusta ka", "Walang anuman", "Tara", "Ayos lang"),
        greeting = "Kumusta ka! Masaya akong makasama ka ngayon. Ano ang maitutulong ko?",
        samplePhrases = listOf("Kumusta!", "Anong balita?", "Tara, usap tayo"),
        systemStyle = "filipino_taglish",
        dateFormat = "MM/dd/yyyy",
        measurementSystem = "Metric"
    )

    val allProfiles: List<RegionalProfile> = listOf(
        KERALA,
        MALAPPURAM,
        KOZHIKODE,
        THIRUVANANTHAPURAM,
        JAPAN,
        KUWAIT,
        UNITED_STATES,
        UNITED_KINGDOM,
        INDIA_GENERAL,
        TAMIL_NADU,
        KARNATAKA,
        PHILIPPINES
    )

    fun getProfileById(id: String): RegionalProfile {
        return allProfiles.find { it.id.equals(id, ignoreCase = true) } ?: KERALA
    }
}
