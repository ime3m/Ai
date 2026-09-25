package com.example.data.repository

import com.example.data.local.VoiceDao
import com.example.data.model.ConversationMessageEntity
import com.example.data.model.DictionaryEntryEntity
import com.example.data.model.LocalSayingResult
import com.example.data.model.PronunciationFeedback
import com.example.data.model.RegionalDialect
import com.example.data.model.RegionalExpression
import com.example.data.model.SlangTranslationResult
import com.example.data.model.SpeakingStylePreferenceEntity
import com.example.data.model.StyleRewriteResult
import com.example.data.model.VoicePersonality
import com.example.data.model.VoiceProfileEntity
import com.example.data.remote.GeminiVoiceService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class VoiceRepository(
    val voiceDao: VoiceDao,
    val geminiService: GeminiVoiceService
) {

    val allProfiles: Flow<List<VoiceProfileEntity>> = voiceDao.getAllProfiles()
    val allDictionaryEntries: Flow<List<DictionaryEntryEntity>> = voiceDao.getAllDictionaryEntries()
    val speakingStyle: Flow<SpeakingStylePreferenceEntity?> = voiceDao.getSpeakingStyle()

    suspend fun initializeDefaultsIfNeeded() {
        val existingProfiles = voiceDao.getAllProfiles().firstOrNull()
        if (existingProfiles.isNullOrEmpty()) {
            // Seed default Kozhikode profile
            val defaultDialect = DialectCatalog.getDialectById("ml_in_kl_kozhikode")
            voiceDao.insertProfile(
                VoiceProfileEntity(
                    profileName = "My Kozhikode Mate",
                    dialectId = defaultDialect.id,
                    language = defaultDialect.language,
                    country = defaultDialect.country,
                    region = defaultDialect.region,
                    cityOrArea = defaultDialect.cityOrArea,
                    dialectName = defaultDialect.dialectName,
                    regionalStrength = 0.80f,
                    personality = VoicePersonality.FRIENDLY.name,
                    responseStyle = "Casual",
                    slangEnabled = true,
                    isDefault = true
                )
            )

            // Seed Liverpool Scouse profile
            val liverpoolDialect = DialectCatalog.getDialectById("en_gb_eng_liverpool")
            voiceDao.insertProfile(
                VoiceProfileEntity(
                    profileName = "Liverpool Footy Chum",
                    dialectId = liverpoolDialect.id,
                    language = liverpoolDialect.language,
                    country = liverpoolDialect.country,
                    region = liverpoolDialect.region,
                    cityOrArea = liverpoolDialect.cityOrArea,
                    dialectName = liverpoolDialect.dialectName,
                    regionalStrength = 0.85f,
                    personality = VoicePersonality.FUNNY.name,
                    responseStyle = "Casual",
                    slangEnabled = true,
                    isDefault = false
                )
            )

            // Seed Kuwaiti Arabic profile
            val kuwaitDialect = DialectCatalog.getDialectById("ar_kw_kuwait")
            voiceDao.insertProfile(
                VoiceProfileEntity(
                    profileName = "Kuwaiti Diwaniya Voice",
                    dialectId = kuwaitDialect.id,
                    language = kuwaitDialect.language,
                    country = kuwaitDialect.country,
                    region = kuwaitDialect.region,
                    cityOrArea = kuwaitDialect.cityOrArea,
                    dialectName = kuwaitDialect.dialectName,
                    regionalStrength = 0.75f,
                    personality = VoicePersonality.PROFESSIONAL.name,
                    responseStyle = "Casual",
                    slangEnabled = true,
                    isDefault = false
                )
            )
        }

        // Populate initial dictionary entries from DialectCatalog if empty
        val existingDict = voiceDao.getAllDictionaryEntries().firstOrNull()
        if (existingDict.isNullOrEmpty()) {
            for (dialect in DialectCatalog.dialects) {
                for (expr in dialect.typicalExpressions) {
                    voiceDao.insertDictionaryEntry(
                        DictionaryEntryEntity(
                            expression = expr.expression,
                            meaning = expr.meaning,
                            englishMeaning = expr.englishMeaning,
                            region = expr.region,
                            district = expr.district,
                            exampleSentence = expr.exampleSentence,
                            formalEquivalent = expr.formalEquivalent,
                            category = expr.category,
                            pronunciation = expr.pronunciation,
                            similarExpressionsCsv = expr.similarExpressions.joinToString(", "),
                            isUserContributed = false,
                            status = expr.verificationStatus
                        )
                    )
                }
            }
        }

        // Initialize default speaking style preference row if null
        val existingStyle = voiceDao.getSpeakingStyleSync()
        if (existingStyle == null) {
            voiceDao.setSpeakingStyle(
                SpeakingStylePreferenceEntity(
                    id = 1,
                    learningEnabled = false,
                    frequentlyUsedExpressionsCsv = "Good vibe, Nice one, Exactly",
                    preferredSlangCsv = "",
                    formalityLevel = 0.3f,
                    sentenceStyle = "Conversational & rhythmic",
                    preferredResponseLength = "Medium",
                    preferredTone = "Warm & banter",
                    frequentlyUsedWordsCsv = "",
                    codeSwitchingHabit = "Natural mixing with English"
                )
            )
        }
    }

    val allActiveSessions: Flow<List<com.example.data.model.ConversationSessionEntity>> = voiceDao.getAllActiveSessions()
    val pinnedSessions: Flow<List<com.example.data.model.ConversationSessionEntity>> = voiceDao.getPinnedSessions()
    val archivedSessions: Flow<List<com.example.data.model.ConversationSessionEntity>> = voiceDao.getArchivedSessions()

    fun searchSessions(query: String): Flow<List<com.example.data.model.ConversationSessionEntity>> {
        return voiceDao.searchSessions(query)
    }

    suspend fun getSessionById(sessionId: String): com.example.data.model.ConversationSessionEntity? {
        return voiceDao.getSessionById(sessionId)
    }

    suspend fun createNewSession(title: String, dialectId: String): com.example.data.model.ConversationSessionEntity {
        val session = com.example.data.model.ConversationSessionEntity(
            title = title,
            dialectId = dialectId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        voiceDao.insertSession(session)
        return session
    }

    suspend fun pinSession(sessionId: String, isPinned: Boolean) {
        voiceDao.setSessionPinned(sessionId, isPinned)
    }

    suspend fun archiveSession(sessionId: String, isArchived: Boolean) {
        voiceDao.setSessionArchived(sessionId, isArchived)
    }

    suspend fun renameSession(sessionId: String, title: String) {
        voiceDao.renameSession(sessionId, title)
    }

    suspend fun deleteSession(sessionId: String) {
        voiceDao.deleteMessagesForConversation(sessionId)
        voiceDao.deleteSession(sessionId)
    }

    val allMessagesDesc: Flow<List<ConversationMessageEntity>> = voiceDao.getAllMessagesDesc()
    val allMessages: Flow<List<ConversationMessageEntity>> = voiceDao.getAllMessages()
    val messageCount: Flow<Int> = voiceDao.getMessageCount()

    fun getMessagesForConversation(conversationId: String): Flow<List<ConversationMessageEntity>> {
        return voiceDao.getMessagesForConversation(conversationId)
    }

    fun getRecentMessagesForConversation(conversationId: String, limit: Int = 30): Flow<List<ConversationMessageEntity>> {
        return voiceDao.getRecentMessagesForConversation(conversationId, limit)
    }

    fun searchMessages(query: String): Flow<List<ConversationMessageEntity>> {
        return voiceDao.searchAllMessages(query)
    }

    suspend fun deleteMessage(messageId: Long) {
        voiceDao.deleteMessageById(messageId)
    }

    suspend fun setMessagePinned(messageId: Long, isPinned: Boolean) {
        voiceDao.setMessagePinned(messageId, isPinned)
    }

    suspend fun clearMessagesForConversation(conversationId: String) {
        voiceDao.deleteMessagesForConversation(conversationId)
    }

    fun getMessagesForDialect(dialectId: String): Flow<List<ConversationMessageEntity>> {
        return voiceDao.getMessagesForDialect(dialectId)
    }

    suspend fun saveConversationMessage(
        conversationId: String,
        dialectId: String,
        role: String,
        text: String,
        highlightedSlang: List<String> = emptyList(),
        knowledge: com.example.data.knowledge.RealTimeKnowledgeResponse? = null
    ) {
        val sourcesStr = knowledge?.sources?.joinToString("|") { "${it.name} (${it.url})" } ?: ""
        voiceDao.insertMessage(
            ConversationMessageEntity(
                conversationId = conversationId,
                role = role,
                text = text,
                dialectId = dialectId,
                highlightedSlangCsv = highlightedSlang.joinToString(","),
                isRealTimeKnowledge = knowledge != null,
                sourcesCsv = sourcesStr,
                knowledgeFreshness = knowledge?.freshnessCategory?.name ?: "",
                knowledgeTimestamp = knowledge?.timestamp ?: ""
            )
        )

        // Update session preview & timestamp
        val session = voiceDao.getSessionById(conversationId)
        if (session != null) {
            val preview = if (text.length > 60) text.take(60) + "..." else text
            voiceDao.updateSession(
                session.copy(
                    updatedAt = System.currentTimeMillis(),
                    lastMessagePreview = preview,
                    messageCount = session.messageCount + 1
                )
            )
        }
    }

    suspend fun saveMessage(dialectId: String, role: String, text: String, highlightedSlang: List<String> = emptyList()) {
        voiceDao.insertMessage(
            ConversationMessageEntity(
                role = role,
                text = text,
                dialectId = dialectId,
                highlightedSlangCsv = highlightedSlang.joinToString(",")
            )
        )
    }

    fun checkRealTimeKnowledge(
        query: String,
        dialect: RegionalDialect,
        forceWeb: Boolean = false
    ): com.example.data.knowledge.RealTimeKnowledgeResponse? {
        return geminiService.getRealTimeKnowledge(query, dialect, forceWeb)
    }

    suspend fun clearMessagesForDialect(dialectId: String) {
        voiceDao.deleteMessagesForDialect(dialectId)
    }

    suspend fun clearAllConversationHistory() {
        voiceDao.clearAllMessages()
    }

    suspend fun saveProfile(profile: VoiceProfileEntity): Long {
        if (profile.isDefault) {
            voiceDao.clearDefaultProfiles()
        }
        return if (profile.id == 0L) {
            voiceDao.insertProfile(profile)
        } else {
            voiceDao.updateProfile(profile)
            profile.id
        }
    }

    suspend fun setDefaultProfile(profileId: Long) {
        voiceDao.clearDefaultProfiles()
        voiceDao.setDefaultProfile(profileId)
    }

    suspend fun deleteProfile(profile: VoiceProfileEntity) {
        voiceDao.deleteProfile(profile)
    }

    // Dictionary operations
    suspend fun addDictionaryEntry(entry: DictionaryEntryEntity): Long {
        return voiceDao.insertDictionaryEntry(entry)
    }

    suspend fun updateDictionaryEntry(entry: DictionaryEntryEntity) {
        voiceDao.updateDictionaryEntry(entry)
    }

    suspend fun deleteDictionaryEntry(entry: DictionaryEntryEntity) {
        voiceDao.deleteDictionaryEntry(entry)
    }

    fun searchDictionary(query: String): Flow<List<DictionaryEntryEntity>> {
        return voiceDao.searchDictionary(query)
    }

    // Speaking Style & Privacy Controls
    suspend fun updateSpeakingStyle(style: SpeakingStylePreferenceEntity) {
        voiceDao.setSpeakingStyle(style)
    }

    suspend fun resetSpeakingStyle() {
        voiceDao.setSpeakingStyle(
            SpeakingStylePreferenceEntity(
                id = 1,
                learningEnabled = false,
                frequentlyUsedExpressionsCsv = "",
                preferredSlangCsv = "",
                formalityLevel = 0.3f,
                sentenceStyle = "Conversational & rhythmic",
                preferredResponseLength = "Medium",
                preferredTone = "Warm & friendly",
                frequentlyUsedWordsCsv = "",
                codeSwitchingHabit = "Natural mixing with English"
            )
        )
    }

    // Gemini Voice Actions
    suspend fun getAiVoiceResponse(
        userMessage: String,
        history: List<Pair<String, String>>,
        dialect: RegionalDialect,
        strength: Float,
        personality: VoicePersonality,
        slangEnabled: Boolean,
        responseLength: String = "Balanced",
        naturalMixingEnabled: Boolean = true,
        customPromptNotes: String = ""
    ): String {
        val currentStyle = voiceDao.getSpeakingStyleSync()
        return geminiService.generateVoiceResponse(
            userMessage = userMessage,
            conversationHistory = history,
            dialect = dialect,
            regionalStrength = strength,
            personality = personality,
            speakingStyle = currentStyle,
            slangEnabled = slangEnabled,
            responseLength = responseLength,
            naturalMixingEnabled = naturalMixingEnabled,
            customPromptNotes = customPromptNotes
        )
    }

    suspend fun previewPromptContext(
        dialect: RegionalDialect,
        strength: Float,
        personality: VoicePersonality,
        slangEnabled: Boolean,
        responseLength: String = "Balanced",
        naturalMixingEnabled: Boolean = true,
        customPromptNotes: String = ""
    ): String {
        val currentStyle = voiceDao.getSpeakingStyleSync()
        return geminiService.previewPromptContext(
            dialect = dialect,
            regionalStrength = strength,
            personality = personality,
            speakingStyle = currentStyle,
            slangEnabled = slangEnabled,
            responseLength = responseLength,
            naturalMixingEnabled = naturalMixingEnabled,
            customPromptNotes = customPromptNotes
        )
    }

    suspend fun translateSlang(
        text: String,
        sourceDialect: String,
        targetDialect: String
    ): SlangTranslationResult {
        return geminiService.translateSlang(text, sourceDialect, targetDialect)
    }

    suspend fun explainExpression(
        expression: String,
        dialect: RegionalDialect
    ): RegionalExpression {
        return geminiService.explainRegionalExpression(expression, dialect)
    }

    suspend fun evaluatePronunciation(
        targetPhrase: String,
        userSpeech: String,
        dialect: RegionalDialect
    ): PronunciationFeedback {
        return geminiService.evaluatePronunciation(targetPhrase, userSpeech, dialect)
    }

    suspend fun generateLocalSayings(
        sentence: String,
        baseDialect: RegionalDialect
    ): List<LocalSayingResult> {
        return geminiService.generateLocalSayings(sentence, baseDialect)
    }

    suspend fun rewriteInStyles(
        text: String,
        dialect: RegionalDialect
    ): List<StyleRewriteResult> {
        val currentStyle = voiceDao.getSpeakingStyleSync()
        return geminiService.rewriteInStyles(text, dialect, currentStyle)
    }
}
