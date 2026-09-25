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
import com.example.data.knowledge.FreshnessCategory
import com.example.data.knowledge.KnowledgeSource
import com.example.data.knowledge.RealTimeKnowledgeEngine
import com.example.data.knowledge.RealTimeKnowledgeResponse
import com.example.data.knowledge.VerificationLevel
import com.example.data.local.AppDatabase
import com.example.data.model.ConversationMessageEntity
import com.example.data.model.ConversationMode
import com.example.data.model.ConversationSessionEntity
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

    val allSessions: StateFlow<List<ConversationSessionEntity>> = repository.allActiveSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPersistedMessages: StateFlow<List<ConversationMessageEntity>> = repository.allMessagesDesc
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPersistedMessageCount: StateFlow<Int> = repository.messageCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pinnedSessions: StateFlow<List<ConversationSessionEntity>> = repository.pinnedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedSessions: StateFlow<List<ConversationSessionEntity>> = repository.archivedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeSession = MutableStateFlow<ConversationSessionEntity?>(null)
    val activeSession: StateFlow<ConversationSessionEntity?> = _activeSession.asStateFlow()

    private val _isWebSearchEnabled = MutableStateFlow(false)
    val isWebSearchEnabled: StateFlow<Boolean> = _isWebSearchEnabled.asStateFlow()

    private val _activeKnowledgeDetails = MutableStateFlow<RealTimeKnowledgeResponse?>(null)
    val activeKnowledgeDetails: StateFlow<RealTimeKnowledgeResponse?> = _activeKnowledgeDetails.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private var messageJob: kotlinx.coroutines.Job? = null

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
                    updatePracticePhrase(dialect)
                }
            }
        }

        // Initialize active conversation session
        viewModelScope.launch {
            repository.allActiveSessions.collect { sessions ->
                if (_activeSession.value == null && sessions.isNotEmpty()) {
                    val first = sessions.first()
                    _activeSession.value = first
                    loadMessagesForSession(first.id)
                } else if (_activeSession.value == null && sessions.isEmpty()) {
                    val defaultSession = repository.createNewSession("Chat with ${_currentDialect.value.dialectName}", _currentDialect.value.id)
                    _activeSession.value = defaultSession
                    loadMessagesForSession(defaultSession.id)
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

        speechManager.onTranscribeAudio = { audioData, _ ->
            geminiService.transcribeAudio(audioData, dialect = _currentDialect.value)
        }

        speechManager.onVirtualModeUtteranceRequested = {
            val d = _currentDialect.value
            d.samplePhrases.firstOrNull() ?: d.greeting.ifBlank { "ഹലോ, സുഖമാണോ?" }
        }
    }

    fun showNotice(message: String) {
        _userFeedbackNotice.value = message
    }

    fun loadMessagesForSession(sessionId: String) {
        messageJob?.cancel()
        messageJob = viewModelScope.launch {
            repository.getMessagesForConversation(sessionId).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    private fun loadMessages(dialectId: String) {
        val session = _activeSession.value
        if (session != null) {
            loadMessagesForSession(session.id)
        } else {
            messageJob?.cancel()
            messageJob = viewModelScope.launch {
                repository.getMessagesForDialect(dialectId).collect { msgList ->
                    _messages.value = msgList
                }
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
        _userFeedbackNotice.value = "Target dialect set to ${dialect.dialectName}"
    }

    fun updateCustomPromptNotes(notes: String) {
        val profile = _currentProfile.value ?: return
        val updated = profile.copy(customPromptNotes = notes)
        _currentProfile.value = updated
        viewModelScope.launch {
            repository.saveProfile(updated)
            _userFeedbackNotice.value = "Custom prompt notes updated for Gemini"
        }
    }

    fun getPromptContextPreview(
        targetDialect: RegionalDialect = _currentDialect.value,
        strength: Float = _currentProfile.value?.regionalStrength ?: 0.75f,
        personality: VoicePersonality = VoicePersonality.entries.find { it.name == _currentProfile.value?.personality } ?: VoicePersonality.FRIENDLY,
        slangEnabled: Boolean = _currentProfile.value?.slangEnabled ?: true,
        responseLength: String = _currentProfile.value?.responseLength ?: "Balanced",
        naturalMixing: Boolean = _currentProfile.value?.naturalMixingEnabled ?: true,
        customPromptNotes: String = _currentProfile.value?.customPromptNotes ?: ""
    ): String {
        return repository.geminiService.previewPromptContext(
            dialect = targetDialect,
            regionalStrength = strength,
            personality = personality,
            speakingStyle = speakingStyle.value,
            slangEnabled = slangEnabled,
            responseLength = responseLength,
            naturalMixingEnabled = naturalMixing,
            customPromptNotes = customPromptNotes
        )
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

    fun runIndependentMicTest(
        durationSeconds: Int = 4,
        onUpdate: (frames: Long, nonZero: Long, rmsDb: Float, amp: Float) -> Unit,
        onComplete: (success: Boolean, summary: String) -> Unit
    ) {
        speechManager.runIndependentMicrophoneTest(durationSeconds, onUpdate, onComplete)
    }

    fun runRawSttTest(localeCode: String) {
        _pendingReviewTranscript.value = null
        speechManager.runRawSttTest(localeCode)
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
        val customPromptNotes = profile?.customPromptNotes ?: ""

        viewModelScope.launch {
            _isAiThinking.value = true
            try {
                // Ensure active session exists
                var currentSession = _activeSession.value
                if (currentSession == null) {
                    val title = if (userText.length > 25) userText.take(25) + "..." else userText
                    currentSession = repository.createNewSession(title, dialect.id)
                    _activeSession.value = currentSession
                    loadMessagesForSession(currentSession.id)
                }

                // Check real-time knowledge
                val verifiedKnowledge = repository.checkRealTimeKnowledge(
                    query = userText,
                    dialect = dialect,
                    forceWeb = _isWebSearchEnabled.value
                )

                // Save user message to DB
                repository.saveConversationMessage(
                    conversationId = currentSession.id,
                    dialectId = dialect.id,
                    role = "user",
                    text = userText
                )

                val history = _messages.value.takeLast(6).map { it.role to it.text }
                val aiResponse = repository.getAiVoiceResponse(
                    userMessage = userText,
                    history = history,
                    dialect = dialect,
                    strength = strength,
                    personality = personality,
                    slangEnabled = slangEnabled,
                    responseLength = responseLength,
                    naturalMixingEnabled = naturalMixing,
                    customPromptNotes = customPromptNotes
                )

                // Detect dialect slang used in AI message for quick tapping
                val detectedSlang = dialect.typicalExpressions.filter {
                    aiResponse.contains(it.expression, ignoreCase = true)
                }.map { it.expression }

                // Save AI message to DB with verified real-time knowledge details
                repository.saveConversationMessage(
                    conversationId = currentSession.id,
                    dialectId = dialect.id,
                    role = "assistant",
                    text = aiResponse,
                    highlightedSlang = detectedSlang,
                    knowledge = verifiedKnowledge
                )

                // Speak response via dynamic natural adult voice if speakResponse is requested
                if (speakResponse && !isMuted.value) {
                    kotlinx.coroutines.delay(350)
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

    fun startNewConversation() {
        val dialect = _currentDialect.value
        viewModelScope.launch {
            val newSession = repository.createNewSession("New Chat with ${dialect.dialectName}", dialect.id)
            _activeSession.value = newSession
            loadMessagesForSession(newSession.id)
        }
    }

    fun selectConversation(session: ConversationSessionEntity) {
        _activeSession.value = session
        val dialect = DialectCatalog.getDialectById(session.dialectId)
        _currentDialect.value = dialect
        loadMessagesForSession(session.id)
    }

    fun togglePinConversation(session: ConversationSessionEntity) {
        viewModelScope.launch {
            repository.pinSession(session.id, !session.isPinned)
        }
    }

    fun archiveConversation(session: ConversationSessionEntity) {
        viewModelScope.launch {
            repository.archiveSession(session.id, true)
            if (_activeSession.value?.id == session.id) {
                startNewConversation()
            }
            _userFeedbackNotice.value = "Archived \"${session.title}\""
        }
    }

    fun unarchiveConversation(session: ConversationSessionEntity) {
        viewModelScope.launch {
            repository.archiveSession(session.id, false)
            _userFeedbackNotice.value = "Unarchived \"${session.title}\""
        }
    }

    fun renameConversation(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            repository.renameSession(sessionId, newTitle)
            if (_activeSession.value?.id == sessionId) {
                _activeSession.value = _activeSession.value?.copy(title = newTitle)
            }
        }
    }

    fun deleteConversation(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_activeSession.value?.id == sessionId) {
                startNewConversation()
            }
        }
    }

    fun toggleWebSearch() {
        _isWebSearchEnabled.value = !_isWebSearchEnabled.value
    }

    fun showKnowledgeDetails(knowledge: RealTimeKnowledgeResponse) {
        _activeKnowledgeDetails.value = knowledge
    }

    fun dismissKnowledgeDetails() {
        _activeKnowledgeDetails.value = null
    }

    fun showKnowledgeDetailsForMessage(message: ConversationMessageEntity) {
        if (!message.isRealTimeKnowledge) return
        val sourcesList = if (message.sourcesCsv.isNotBlank()) {
            message.sourcesCsv.split("|").map { src ->
                val name = src.substringBefore(" (").trim()
                val url = src.substringAfter("(", "").substringBefore(")").trim()
                KnowledgeSource(name = name, url = url, tier = 1)
            }
        } else {
            listOf(
                KnowledgeSource("Kerala Government Official Portal", "https://kerala.gov.in", 1),
                KnowledgeSource("Kerala Legislative Assembly", "https://niyamasabha.nic.in", 1)
            )
        }

        _activeKnowledgeDetails.value = RealTimeKnowledgeResponse(
            factualText = message.text,
            dialectText = message.text,
            freshnessCategory = FreshnessCategory.entries.find { it.name == message.knowledgeFreshness } ?: FreshnessCategory.CURRENT,
            verificationLevel = VerificationLevel.VERIFIED,
            sources = sourcesList,
            timestamp = message.knowledgeTimestamp.ifBlank { RealTimeKnowledgeEngine.CURRENT_DATE_STRING },
            searchTriggered = true
        )
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

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
            _userFeedbackNotice.value = "Message deleted from local database."
        }
    }

    fun togglePinMessage(message: ConversationMessageEntity) {
        viewModelScope.launch {
            repository.setMessagePinned(message.id, !message.isPinned)
        }
    }

    fun clearConversationHistory() {
        viewModelScope.launch {
            val session = _activeSession.value
            if (session != null) {
                repository.clearMessagesForConversation(session.id)
            }
            val dialect = _currentDialect.value
            repository.clearMessagesForDialect(dialect.id)
            _userFeedbackNotice.value = "Cleared conversation history for ${dialect.dialectName}."
        }
    }

    fun clearAllChatHistory() {
        viewModelScope.launch {
            repository.clearAllConversationHistory()
            _userFeedbackNotice.value = "All chat messages cleared from local Room database."
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
