package com.example.data.repository

import com.example.data.local.VoiceDao
import com.example.data.model.ConversationMessageEntity
import com.example.data.model.DictionaryEntryEntity
import com.example.data.model.PronunciationFeedback
import com.example.data.model.RegionalDialect
import com.example.data.model.RegionalExpression
import com.example.data.model.SlangTranslationResult
import com.example.data.model.SpeakingStylePreferenceEntity
import com.example.data.model.VoicePersonality
import com.example.data.model.VoiceProfileEntity
import com.example.data.remote.GeminiVoiceService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class VoiceRepository(
    private val voiceDao: VoiceDao,
    private val geminiService: GeminiVoiceService
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
                            region = expr.region,
                            exampleSentence = expr.exampleSentence,
                            formalEquivalent = expr.formalEquivalent,
                            category = expr.toneCategory,
                            isUserContributed = false,
                            status = "Verified"
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

    fun getMessagesForDialect(dialectId: String): Flow<List<ConversationMessageEntity>> {
        return voiceDao.getMessagesForDialect(dialectId)
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
        slangEnabled: Boolean
    ): String {
        val currentStyle = voiceDao.getSpeakingStyleSync()
        return geminiService.generateVoiceResponse(
            userMessage = userMessage,
            conversationHistory = history,
            dialect = dialect,
            regionalStrength = strength,
            personality = personality,
            speakingStyle = currentStyle,
            slangEnabled = slangEnabled
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
}
