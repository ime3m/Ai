package com.example.data.model

data class RegionalDialect(
    val id: String,
    val language: String,
    val country: String,
    val stateOrProvince: String = "Kerala",
    val region: String,
    val cityOrArea: String,
    val dialectName: String,
    val flagEmoji: String,
    val samplePhrases: List<String>,
    val greeting: String,
    val localeCode: String,
    val description: String,
    val typicalExpressions: List<RegionalExpression>,
    val codeSwitchingDescription: String,
    val tendenciesNotes: String = ""
)

data class RegionalExpression(
    val expression: String,
    val meaning: String,
    val region: String,
    val dialect: String,
    val context: String,
    val formalEquivalent: String,
    val exampleSentence: String,
    val toneCategory: String = "Casual",
    val culturalNotes: String = "",
    val englishMeaning: String = "",
    val district: String = "",
    val category: String = "Everyday conversation",
    val pronunciation: String = "",
    val similarExpressions: List<String> = emptyList(),
    val casualEquivalent: String = "",
    val verificationStatus: String = "Verified", // "Verified", "Community submitted", "AI suggested", "Unverified"
    val usageNotes: String = "",
    val ageGenerationNotes: String = ""
)

enum class VoicePersonality(
    val title: String,
    val emoji: String,
    val description: String,
    val systemPromptGuidance: String
) {
    FRIENDLY(
        title = "Friendly",
        emoji = "😊",
        description = "Warm, welcoming, smiles in speech, supportive listener",
        systemPromptGuidance = "Adopt a warm, highly welcoming and supportive tone with friendly regional warmth."
    ),
    FUNNY(
        title = "Funny",
        emoji = "😄",
        description = "Witty, playful banter, dry humor, humorous expressions",
        systemPromptGuidance = "Include lighthearted humor, witty remarks, witty regional banter, and clever comedic timing."
    ),
    PROFESSIONAL(
        title = "Professional",
        emoji = "💼",
        description = "Polite, articulate, respectful yet locally authentic",
        systemPromptGuidance = "Maintain respectful, articulate professionalism while seamlessly weaving appropriate local regional etiquette."
    ),
    CALM(
        title = "Calm",
        emoji = "🌿",
        description = "Peaceful, grounded, gentle speaking pace, soothing rhythm",
        systemPromptGuidance = "Speak in a calm, grounded, serene manner with gentle pauses and soothing regional cadence."
    ),
    ENERGETIC(
        title = "Energetic",
        emoji = "⚡",
        description = "High energy, enthusiastic, punchy responses, lively rhythm",
        systemPromptGuidance = "Radiate excitement and high energy, speaking dynamically with vibrant regional exclamations."
    ),
    STORYTELLER(
        title = "Storyteller",
        emoji = "📖",
        description = "Expressive, paints pictures with local idioms and vivid tales",
        systemPromptGuidance = "Use vivid descriptive language, classic regional storytelling hooks, idioms, and colorful narrative rhythm."
    ),
    TEACHER(
        title = "Teacher",
        emoji = "🧑‍🏫",
        description = "Patient, encouraging, explains local dialect origins and vocabulary",
        systemPromptGuidance = "Be patient, encouraging, and informative, gently explaining regional vocabulary context when helpful."
    ),
    LOCAL_FRIEND(
        title = "Local Friend",
        emoji = "🤝",
        description = "Hometown buddy, authentic street banter, close camaraderie",
        systemPromptGuidance = "Speak like a lifelong hometown friend, using authentic colloquial banter, effortless slang, and genuine warmth."
    ),
    CASUAL(
        title = "Casual",
        emoji = "☕",
        description = "Relaxed buddy vibe, natural colloquial shortcuts, low pressure",
        systemPromptGuidance = "Keep it totally relaxed, like two close hometown friends catching up over local tea or coffee."
    ),
    SERIOUS(
        title = "Serious",
        emoji = "🎯",
        description = "Direct, focused, no fluff, honest and thoughtful",
        systemPromptGuidance = "Be direct, sincere, and thoughtful, using grounded regional phrasing with clarity and substance."
    )
}

enum class ConversationMode(
    val title: String,
    val iconName: String,
    val description: String
) {
    FREE_CONVERSATION(
        title = "Free Chat",
        iconName = "chat",
        description = "Fluid regional conversation with natural voice pauses"
    ),
    DIALECT_PRACTICE(
        title = "Dialect Practice",
        iconName = "mic",
        description = "Listen, repeat, and get pronunciation feedback"
    ),
    SLANG_TRANSLATOR(
        title = "Slang Translator",
        iconName = "translate",
        description = "Convert text or speech between standard & regional dialects"
    ),
    LEARN_THE_REGION(
        title = "Learn the Region",
        iconName = "school",
        description = "Master local cultural idioms, etiquette & slang stories"
    ),
    MY_STYLE(
        title = "My Style",
        iconName = "person",
        description = "Conversations tailored to your personal learned speaking habits"
    )
}

data class SlangTranslationResult(
    val sourceText: String,
    val convertedText: String,
    val sourceDialect: String,
    val targetDialect: String,
    val explanation: String,
    val substitutedSlang: List<SlangSubstitution> = emptyList()
)

data class SlangSubstitution(
    val originalWord: String,
    val regionalSlang: String,
    val meaning: String,
    val reason: String
)

data class PronunciationFeedback(
    val targetPhrase: String,
    val userSpeech: String,
    val accuracyPercentage: Int,
    val phoneticNotes: String,
    val praiseOrCorrection: String,
    val audioPracticeTip: String
)

data class LocalSayingResult(
    val regionName: String,
    val regionalText: String,
    val explanation: String = ""
)

data class StyleRewriteResult(
    val styleName: String,
    val rewrittenText: String,
    val description: String = ""
)
