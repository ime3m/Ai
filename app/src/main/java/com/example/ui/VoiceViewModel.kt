package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.VoiceSpeechManager
import com.example.audio.VoiceState
import com.example.data.local.AppDatabase
import com.example.data.model.ConversationMessageEntity
import com.example.data.model.ConversationMode
import com.example.data.model.DictionaryEntryEntity
import com.example.data.model.PronunciationFeedback
import com.example.data.model.RegionalDialect
import com.example.data.model.RegionalExpression
import com.example.data.model.SlangTranslationResult
import com.example.data.model.SpeakingStylePreferenceEntity
import com.example.data.model.VoicePersonality
import com.example.data.model.VoiceProfileEntity
import com.example.data.remote.GeminiVoiceService
import com.example.data.repository.DialectCatalog
import com.example.data.repository.VoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SlangTranslationUiState(
    val inputText: String = "",
    val sourceDialect: String = "Standard Malayalam",
    val targetDialect: String = "Kozhikode Slang",
    val result: SlangTranslationResult? = null,
    val isTranslating: Boolean = false
)

data class PracticeUiState(
    val currentPhraseIndex: Int = 0,
    val targetPhrase: String = "",
    val userSpeech: String = "",
    val feedback: PronunciationFeedback? = null,
    val isEvaluating: Boolean = false
)

class VoiceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val geminiService = GeminiVoiceService()
    val repository = VoiceRepository(database.voiceDao(), geminiService)
    val speechManager = VoiceSpeechManager(application)

    val voiceState: StateFlow<VoiceState> = speechManager.voiceState
    val soundLevel: StateFlow<Float> = speechManager.soundLevel
    val partialTranscript: StateFlow<String> = speechManager.partialTranscript
    val isMuted: StateFlow<Boolean> = speechManager.isMuted

    val allProfiles: StateFlow<List<VoiceProfileEntity>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dictionaryEntries: StateFlow<List<DictionaryEntryEntity>> = repository.allDictionaryEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val speakingStyle: StateFlow<SpeakingStylePreferenceEntity?> = repository.speakingStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentProfile = MutableStateFlow<VoiceProfileEntity?>(null)
    val currentProfile: StateFlow<VoiceProfileEntity?> = _currentProfile.asStateFlow()

    private val _currentDialect = MutableStateFlow(DialectCatalog.dialects.first())
    val currentDialect: StateFlow<RegionalDialect> = _currentDialect.asStateFlow()

    private val _currentMode = MutableStateFlow(ConversationMode.FREE_CONVERSATION)
    val currentMode: StateFlow<ConversationMode> = _currentMode.asStateFlow()

    private val _messages = MutableStateFlow<List<ConversationMessageEntity>>(emptyList())
    val messages: StateFlow<List<ConversationMessageEntity>> = _messages.asStateFlow()

    private val _selectedExpressionForDetails = MutableStateFlow<RegionalExpression?>(null)
    val selectedExpressionForDetails: StateFlow<RegionalExpression?> = _selectedExpressionForDetails.asStateFlow()

    private val _userFeedbackNotice = MutableStateFlow<String?>(null)
    val userFeedbackNotice: StateFlow<String?> = _userFeedbackNotice.asStateFlow()

    private val _slangTranslationState = MutableStateFlow(
        SlangTranslationUiState(
            inputText = "നമുക്ക് വൈകുന്നേരം ചായ കുടിക്കാൻ പോകാം",
            sourceDialect = "Standard Malayalam",
            targetDialect = "Kozhikode Slang"
        )
    )
    val slangTranslationState: StateFlow<SlangTranslationUiState> = _slangTranslationState.asStateFlow()

    private val _practiceState = MutableStateFlow(PracticeUiState())
    val practiceState: StateFlow<PracticeUiState> = _practiceState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            // Observe profiles and choose active
            repository.allProfiles.collect { profiles ->
                if (profiles.isNotEmpty()) {
                    val defaultOne = profiles.find { it.isDefault } ?: profiles.first()
                    _currentProfile.value = defaultOne
                    val dialect = DialectCatalog.getDialectById(defaultOne.dialectId)
                    _currentDialect.value = dialect
                    loadMessages(dialect.id)
                    updatePracticePhrase(dialect)
                }
            }
        }

        // Connect speech recognizer
        speechManager.onSpeechRecognized = { recognizedText ->
            handleUserInput(recognizedText)
        }

        speechManager.onError = { errorMsg ->
            _userFeedbackNotice.value = errorMsg
        }
    }

    private fun loadMessages(dialectId: String) {
        viewModelScope.launch {
            repository.getMessagesForDialect(dialectId).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    private fun updatePracticePhrase(dialect: RegionalDialect) {
        val expressions = dialect.typicalExpressions
        if (expressions.isNotEmpty()) {
            val expr = expressions[_practiceState.value.currentPhraseIndex % expressions.size]
            _practiceState.value = _practiceState.value.copy(
                targetPhrase = expr.expression,
                feedback = null,
                userSpeech = ""
            )
        }
    }

    fun setConversationMode(mode: ConversationMode) {
        _currentMode.value = mode
        speechManager.interruptAi()
        if (mode == ConversationMode.DIALECT_PRACTICE) {
            updatePracticePhrase(_currentDialect.value)
        }
    }

    fun selectDialect(dialect: RegionalDialect) {
        _currentDialect.value = dialect
        val profile = _currentProfile.value
        if (profile != null) {
            val updated = profile.copy(
                dialectId = dialect.id,
                language = dialect.language,
                country = dialect.country,
                region = dialect.region,
                cityOrArea = dialect.cityOrArea,
                dialectName = dialect.dialectName
            )
            _currentProfile.value = updated
            viewModelScope.launch {
                repository.saveProfile(updated)
            }
        }
        loadMessages(dialect.id)
        updatePracticePhrase(dialect)
    }

    fun updateRegionalStrength(newStrength: Float) {
        val profile = _currentProfile.value ?: return
        val updated = profile.copy(regionalStrength = newStrength)
        _currentProfile.value = updated
        viewModelScope.launch {
            repository.saveProfile(updated)
        }
    }

    fun updatePersonality(personality: VoicePersonality) {
        val profile = _currentProfile.value ?: return
        val updated = profile.copy(personality = personality.name)
        _currentProfile.value = updated
        viewModelScope.launch {
            repository.saveProfile(updated)
        }
    }

    fun toggleSlang(enabled: Boolean) {
        val profile = _currentProfile.value ?: return
        val updated = profile.copy(slangEnabled = enabled)
        _currentProfile.value = updated
        viewModelScope.launch {
            repository.saveProfile(updated)
        }
    }

    fun switchProfile(profile: VoiceProfileEntity) {
        _currentProfile.value = profile
        val dialect = DialectCatalog.getDialectById(profile.dialectId)
        _currentDialect.value = dialect
        loadMessages(dialect.id)
        viewModelScope.launch {
            repository.setDefaultProfile(profile.id)
        }
    }

    fun createOrUpdateProfile(profile: VoiceProfileEntity) {
        viewModelScope.launch {
            val id = repository.saveProfile(profile)
            if (profile.isDefault || _currentProfile.value == null) {
                _currentProfile.value = profile.copy(id = id)
                _currentDialect.value = DialectCatalog.getDialectById(profile.dialectId)
            }
        }
    }

    fun deleteProfile(profile: VoiceProfileEntity) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
        }
    }

    fun toggleMic() {
        if (speechManager.voiceState.value == VoiceState.LISTENING) {
            speechManager.stopListening()
        } else {
            speechManager.startListening(_currentDialect.value.localeCode)
        }
    }

    fun interruptAi() {
        speechManager.interruptAi()
    }

    fun toggleMute() {
        speechManager.toggleMute()
    }

    fun handleUserInput(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        when (_currentMode.value) {
            ConversationMode.FREE_CONVERSATION,
            ConversationMode.MY_STYLE,
            ConversationMode.LEARN_THE_REGION -> {
                sendChatMessage(trimmed)
            }
            ConversationMode.DIALECT_PRACTICE -> {
                evaluateUserPractice(trimmed)
            }
            ConversationMode.SLANG_TRANSLATOR -> {
                updateTranslationInput(trimmed)
                translateCurrentInput()
            }
        }
    }

    fun sendChatMessage(userText: String) {
        val dialect = _currentDialect.value
        val profile = _currentProfile.value
        val strength = profile?.regionalStrength ?: 0.75f
        val personalityStr = profile?.personality ?: VoicePersonality.FRIENDLY.name
        val personality = VoicePersonality.entries.find { it.name == personalityStr } ?: VoicePersonality.FRIENDLY
        val slangEnabled = profile?.slangEnabled ?: true

        viewModelScope.launch {
            // Save user message to DB
            repository.saveMessage(dialect.id, role = "user", text = userText)

            val history = _messages.value.takeLast(6).map { it.role to it.text }
            val aiResponse = repository.getAiVoiceResponse(
                userMessage = userText,
                history = history,
                dialect = dialect,
                strength = strength,
                personality = personality,
                slangEnabled = slangEnabled
            )

            // Detect dialect slang used in AI message for quick tapping
            val detectedSlang = dialect.typicalExpressions.filter {
                aiResponse.contains(it.expression, ignoreCase = true)
            }.map { it.expression }

            // Save AI message to DB
            repository.saveMessage(dialect.id, role = "assistant", text = aiResponse, highlightedSlang = detectedSlang)

            // Speak response via dynamic TTS
            val pitch = when (personality) {
                VoicePersonality.FUNNY, VoicePersonality.ENERGETIC -> 1.15f
                VoicePersonality.CALM -> 0.90f
                else -> 1.0f
            }
            val rate = when (personality) {
                VoicePersonality.ENERGETIC -> 1.1f
                VoicePersonality.CALM, VoicePersonality.STORYTELLER -> 0.92f
                else -> 1.0f
            }
            speechManager.speak(aiResponse, localeCode = dialect.localeCode, pitch = pitch, speechRate = rate)
        }
    }

    // Explain Regional Expression bottom sheet
    fun inspectExpression(expressionText: String) {
        viewModelScope.launch {
            val dialect = _currentDialect.value
            val explanation = repository.explainExpression(expressionText, dialect)
            _selectedExpressionForDetails.value = explanation
        }
    }

    fun dismissExpressionDetails() {
        _selectedExpressionForDetails.value = null
    }

    fun speakText(text: String) {
        val dialect = _currentDialect.value
        speechManager.speak(text, dialect.localeCode)
    }

    // Dialect Practice (Voice Learning Mode)
    fun nextPracticePhrase() {
        val expressions = _currentDialect.value.typicalExpressions
        if (expressions.isNotEmpty()) {
            val nextIndex = (_practiceState.value.currentPhraseIndex + 1) % expressions.size
            val nextExpr = expressions[nextIndex]
            _practiceState.value = PracticeUiState(
                currentPhraseIndex = nextIndex,
                targetPhrase = nextExpr.expression,
                feedback = null,
                userSpeech = ""
            )
            // AI says the regional expression
            speechManager.speak(nextExpr.expression, _currentDialect.value.localeCode)
        }
    }

    fun playCurrentPracticePhrase() {
        speechManager.speak(_practiceState.value.targetPhrase, _currentDialect.value.localeCode)
    }

    private fun evaluateUserPractice(userSpeech: String) {
        _practiceState.value = _practiceState.value.copy(
            userSpeech = userSpeech,
            isEvaluating = true
        )
        viewModelScope.launch {
            val dialect = _currentDialect.value
            val target = _practiceState.value.targetPhrase
            val feedback = repository.evaluatePronunciation(target, userSpeech, dialect)
            _practiceState.value = _practiceState.value.copy(
                feedback = feedback,
                isEvaluating = false
            )
            // Speak encouraging feedback tip
            speechManager.speak(feedback.praiseOrCorrection, dialect.localeCode)
        }
    }

    // Slang Translator methods
    fun updateTranslationInput(text: String) {
        _slangTranslationState.value = _slangTranslationState.value.copy(inputText = text)
    }

    fun setTranslationDialects(source: String, target: String) {
        _slangTranslationState.value = _slangTranslationState.value.copy(
            sourceDialect = source,
            targetDialect = target
        )
    }

    fun swapTranslationDialects() {
        val current = _slangTranslationState.value
        _slangTranslationState.value = current.copy(
            sourceDialect = current.targetDialect,
            targetDialect = current.sourceDialect
        )
    }

    fun translateCurrentInput() {
        val state = _slangTranslationState.value
        if (state.inputText.isBlank()) return

        _slangTranslationState.value = state.copy(isTranslating = true)
        viewModelScope.launch {
            val result = repository.translateSlang(
                text = state.inputText,
                sourceDialect = state.sourceDialect,
                targetDialect = state.targetDialect
            )
            _slangTranslationState.value = _slangTranslationState.value.copy(
                result = result,
                isTranslating = false
            )
        }
    }

    // Dictionary Management
    fun addDictionaryEntry(
        word: String,
        meaning: String,
        region: String,
        exampleSentence: String,
        formalEquivalent: String,
        category: String
    ) {
        viewModelScope.launch {
            repository.addDictionaryEntry(
                DictionaryEntryEntity(
                    expression = word,
                    meaning = meaning,
                    region = region,
                    exampleSentence = exampleSentence,
                    formalEquivalent = formalEquivalent,
                    category = category,
                    isUserContributed = true,
                    status = "Verified"
                )
            )
            _userFeedbackNotice.value = "Added \"$word\" to regional dictionary!"
        }
    }

    fun deleteDictionaryEntry(entry: DictionaryEntryEntity) {
        viewModelScope.launch {
            repository.deleteDictionaryEntry(entry)
            _userFeedbackNotice.value = "Removed \"${entry.expression}\""
        }
    }

    // Privacy & "Learn My Speaking Style" Controls
    fun updateSpeakingStylePreferences(updated: SpeakingStylePreferenceEntity) {
        viewModelScope.launch {
            repository.updateSpeakingStyle(updated)
            _userFeedbackNotice.value = "Speaking style preferences updated."
        }
    }

    fun resetSpeakingStylePreferences() {
        viewModelScope.launch {
            repository.resetSpeakingStyle()
            _userFeedbackNotice.value = "Personal speaking style has been reset to defaults."
        }
    }

    fun clearConversationHistory() {
        viewModelScope.launch {
            val dialect = _currentDialect.value
            repository.clearMessagesForDialect(dialect.id)
            _userFeedbackNotice.value = "Cleared conversation history for ${dialect.dialectName}."
        }
    }

    fun clearAllUserData() {
        viewModelScope.launch {
            repository.clearAllConversationHistory()
            repository.resetSpeakingStyle()
            _userFeedbackNotice.value = "All conversation history & personalized habits erased."
        }
    }

    fun dismissNotice() {
        _userFeedbackNotice.value = null
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }
}
