package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.TranscriptConfirmationMode
import com.example.audio.VoiceInputError
import com.example.audio.VoiceInputMode
import com.example.audio.VoiceInputSettings
import com.example.audio.VoiceSpeechManager
import com.example.audio.VoiceState
import com.example.data.local.AppDatabase
import com.example.data.model.ConversationMessageEntity
import com.example.data.model.ConversationMode
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
import com.example.data.repository.DialectCatalog
import com.example.data.repository.VoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class InputMode {
    VOICE, WRITE
}

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

data class LocalSayingsUiState(
    val inputText: String = "",
    val results: List<LocalSayingResult> = emptyList(),
    val isLoading: Boolean = false
)

data class StyleRewriteUiState(
    val inputText: String = "",
    val results: List<StyleRewriteResult> = emptyList(),
    val isRewriting: Boolean = false
)

class VoiceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val geminiService = GeminiVoiceService()
    val repository = VoiceRepository(database.voiceDao(), geminiService)
    val speechManager = VoiceSpeechManager(application)

    val voiceState: StateFlow<VoiceState> = speechManager.voiceState
    val soundLevel: StateFlow<Float> = speechManager.soundLevel
    val partialTranscript: StateFlow<String> = speechManager.partialTranscript
    val lastFinalTranscript: StateFlow<String> = speechManager.lastFinalTranscript
    val voiceError: StateFlow<VoiceInputError?> = speechManager.voiceError
    val voiceSettings: StateFlow<VoiceInputSettings> = speechManager.voiceSettings
    val isMuted: StateFlow<Boolean> = speechManager.isMuted

    private val _pendingReviewTranscript = MutableStateFlow<String?>(null)
    val pendingReviewTranscript: StateFlow<String?> = _pendingReviewTranscript.asStateFlow()

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

    private val _inputMode = MutableStateFlow(InputMode.VOICE)
    val inputMode: StateFlow<InputMode> = _inputMode.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _localSayingsState = MutableStateFlow(
        LocalSayingsUiState(inputText = "നമുക്ക് ചായ കുടിക്കാൻ പോകാം")
    )
    val localSayingsState: StateFlow<LocalSayingsUiState> = _localSayingsState.asStateFlow()

    private val _styleRewriteState = MutableStateFlow(
        StyleRewriteUiState(inputText = "നാളെ കാണാം")
    )
    val styleRewriteState: StateFlow<StyleRewriteUiState> = _styleRewriteState.asStateFlow()

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
            if (speechManager.voiceSettings.value.confirmationMode == TranscriptConfirmationMode.REVIEW_BEFORE_SEND) {
                _pendingReviewTranscript.value = recognizedText
            } else {
                handleUserInput(recognizedText)
            }
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

    fun updateResponseStyle(style: String) {
        val profile = _currentProfile.value ?: return
        val updated = profile.copy(responseStyle = style)
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
        if (speechManager.voiceState.value.isRecording) {
            speechManager.stopListening()
        } else {
            _pendingReviewTranscript.value = null
            speechManager.startListening(_currentDialect.value.localeCode)
        }
    }

    fun retryVoiceInput() {
        _pendingReviewTranscript.value = null
        speechManager.retryListening(_currentDialect.value.localeCode)
    }

    fun clearPendingReviewTranscript() {
        _pendingReviewTranscript.value = null
    }

    fun updateVoiceInputSettings(settings: VoiceInputSettings) {
        speechManager.updateSettings(settings)
    }

    fun setVoiceInputMode(mode: VoiceInputMode) {
        speechManager.setInputMode(mode)
    }

    fun setSilenceTimeout(timeoutMs: Long) {
        speechManager.setSilenceTimeout(timeoutMs)
    }

    fun setMaxRecordingDuration(seconds: Int) {
        speechManager.setMaxDuration(seconds)
    }

    fun setConfirmationMode(mode: TranscriptConfirmationMode) {
        speechManager.setConfirmationMode(mode)
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

    fun setInputMode(mode: InputMode) {
        _inputMode.value = mode
    }

    fun updateResponseLength(length: String) {
        val profile = _currentProfile.value ?: return
        val updated = profile.copy(responseLength = length)
        _currentProfile.value = updated
        viewModelScope.launch {
            repository.saveProfile(updated)
        }
    }

    fun toggleNaturalMixing(enabled: Boolean) {
        val profile = _currentProfile.value ?: return
        val updated = profile.copy(naturalMixingEnabled = enabled)
        _currentProfile.value = updated
        viewModelScope.launch {
            repository.saveProfile(updated)
        }
    }

    fun sendChatMessage(userText: String, speakResponse: Boolean = (_inputMode.value == InputMode.VOICE)) {
        val dialect = _currentDialect.value
        val profile = _currentProfile.value
        val strength = profile?.regionalStrength ?: 0.75f
        val personalityStr = profile?.personality ?: VoicePersonality.FRIENDLY.name
        val personality = VoicePersonality.entries.find { it.name == personalityStr } ?: VoicePersonality.FRIENDLY
        val slangEnabled = profile?.slangEnabled ?: true
        val responseLength = profile?.responseLength ?: "Balanced"
        val naturalMixing = profile?.naturalMixingEnabled ?: true

        viewModelScope.launch {
            _isAiThinking.value = true
            try {
                // Save user message to DB
                repository.saveMessage(dialect.id, role = "user", text = userText)

                val history = _messages.value.takeLast(6).map { it.role to it.text }
                val aiResponse = repository.getAiVoiceResponse(
                    userMessage = userText,
                    history = history,
                    dialect = dialect,
                    strength = strength,
                    personality = personality,
                    slangEnabled = slangEnabled,
                    responseLength = responseLength,
                    naturalMixingEnabled = naturalMixing
                )

                // Detect dialect slang used in AI message for quick tapping
                val detectedSlang = dialect.typicalExpressions.filter {
                    aiResponse.contains(it.expression, ignoreCase = true)
                }.map { it.expression }

                // Save AI message to DB
                repository.saveMessage(dialect.id, role = "assistant", text = aiResponse, highlightedSlang = detectedSlang)

                // Speak response via dynamic natural adult voice if speakResponse is requested
                if (speakResponse && !isMuted.value) {
                    // Natural processing delay (Requirement 46: short natural response start pause)
                    kotlinx.coroutines.delay(380)
                    val pacing = profile?.voiceSpeed ?: "Natural"
                    speechManager.speak(
                        text = aiResponse,
                        localeCode = dialect.localeCode,
                        speechPacing = pacing,
                        personalityName = personality.name
                    )
                }
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun updateVoiceSpeed(speed: String) {
        val profile = _currentProfile.value ?: return
        val updated = profile.copy(voiceSpeed = speed)
        _currentProfile.value = updated
        viewModelScope.launch {
            repository.saveProfile(updated)
        }
    }

    fun regenerateLastMessage() {
        val lastUserMessage = _messages.value.lastOrNull { it.role == "user" }
        if (lastUserMessage != null) {
            sendChatMessage(lastUserMessage.text, speakResponse = (_inputMode.value == InputMode.VOICE))
        }
    }

    fun replayMessage(text: String) {
        speakText(text)
    }

    fun compareLocalSayings(sentence: String) {
        if (sentence.isBlank()) return
        _localSayingsState.value = _localSayingsState.value.copy(inputText = sentence, isLoading = true)
        viewModelScope.launch {
            val results = repository.generateLocalSayings(sentence, _currentDialect.value)
            _localSayingsState.value = _localSayingsState.value.copy(
                inputText = sentence,
                results = results,
                isLoading = false
            )
        }
    }

    fun rewriteInVariousStyles(text: String) {
        if (text.isBlank()) return
        _styleRewriteState.value = _styleRewriteState.value.copy(inputText = text, isRewriting = true)
        viewModelScope.launch {
            val results = repository.rewriteInStyles(text, _currentDialect.value)
            _styleRewriteState.value = _styleRewriteState.value.copy(
                inputText = text,
                results = results,
                isRewriting = false
            )
        }
    }

    fun toggleSpeakingStyleLearning(enabled: Boolean) {
        val current = speakingStyle.value ?: SpeakingStylePreferenceEntity()
        updateSpeakingStylePreferences(current.copy(learningEnabled = enabled))
    }

    fun togglePauseSpeakingStyle(paused: Boolean) {
        val current = speakingStyle.value ?: SpeakingStylePreferenceEntity()
        updateSpeakingStylePreferences(current.copy(isPaused = paused))
    }

    fun selectVoiceProfile(profile: VoiceProfileEntity) {
        _currentProfile.value = profile
        val dialect = DialectCatalog.getDialectById(profile.dialectId)
        _currentDialect.value = dialect
        loadMessages(dialect.id)
        updatePracticePhrase(dialect)
    }

    fun saveVoiceProfile(profile: VoiceProfileEntity) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            selectVoiceProfile(profile)
            _userFeedbackNotice.value = "Profile saved: ${profile.profileName}"
        }
    }

    fun updateSpeakingStyle(updated: SpeakingStylePreferenceEntity) {
        updateSpeakingStylePreferences(updated)
    }

    fun resetSpeakingStyle() {
        resetSpeakingStylePreferences()
    }

    fun inspectExpression(expression: RegionalExpression) {
        _selectedExpressionForDetails.value = expression
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
        val profile = _currentProfile.value
        val pacing = profile?.voiceSpeed ?: "Natural"
        val personalityStr = profile?.personality ?: VoicePersonality.FRIENDLY.name
        speechManager.speak(
            text = text,
            localeCode = dialect.localeCode,
            speechPacing = pacing,
            personalityName = personalityStr
        )
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

    fun updateLocalSayingsInput(text: String) {
        _localSayingsState.value = _localSayingsState.value.copy(inputText = text)
    }

    fun generateLocalSayings() {
        val state = _localSayingsState.value
        if (state.inputText.isBlank()) return
        _localSayingsState.value = state.copy(isLoading = true)
        viewModelScope.launch {
            val results = repository.generateLocalSayings(state.inputText, _currentDialect.value)
            _localSayingsState.value = _localSayingsState.value.copy(
                results = results,
                isLoading = false
            )
        }
    }

    fun updateStyleRewriteInput(text: String) {
        _styleRewriteState.value = _styleRewriteState.value.copy(inputText = text)
    }

    fun rewriteInStyles() {
        val state = _styleRewriteState.value
        if (state.inputText.isBlank()) return
        _styleRewriteState.value = state.copy(isRewriting = true)
        viewModelScope.launch {
            val results = repository.rewriteInStyles(state.inputText, _currentDialect.value)
            _styleRewriteState.value = _styleRewriteState.value.copy(
                results = results,
                isRewriting = false
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
        category: String,
        englishMeaning: String = "",
        district: String = "",
        pronunciation: String = "",
        similarExpressionsCsv: String = "",
        status: String = "Community submitted"
    ) {
        viewModelScope.launch {
            repository.addDictionaryEntry(
                DictionaryEntryEntity(
                    expression = word,
                    meaning = meaning,
                    englishMeaning = englishMeaning,
                    region = region,
                    district = district,
                    exampleSentence = exampleSentence,
                    formalEquivalent = formalEquivalent,
                    category = category,
                    pronunciation = pronunciation,
                    similarExpressionsCsv = similarExpressionsCsv,
                    isUserContributed = true,
                    status = status
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
