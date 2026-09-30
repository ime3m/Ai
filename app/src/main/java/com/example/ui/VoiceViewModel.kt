package com.example.ui

import android.app.Application
import android.util.Log
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
import com.example.data.location.LocationExtractor
import com.example.data.location.QueryContext
import com.example.data.location.UserLocation
import com.example.data.model.AIRequest
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

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
    val preferencesRepository = com.example.data.preferences.PreferencesRepository(application)

    private val _selectedRegionProfile = MutableStateFlow(
        com.example.data.regional.RegionalProfileRegistry.getProfileById(preferencesRepository.selectedRegionId)
    )
    val selectedRegionProfile: StateFlow<com.example.data.regional.RegionalProfile> = _selectedRegionProfile.asStateFlow()

    private val _userLocation = MutableStateFlow(preferencesRepository.getUserLocation())
    val userLocation: StateFlow<UserLocation> = _userLocation.asStateFlow()

    fun setUserLocation(city: String?, country: String?) {
        val updated = UserLocation(
            city = city,
            country = country,
            source = UserLocation.SOURCE_USER_CONFIGURED
        )
        preferencesRepository.saveUserLocation(updated)
        _userLocation.value = updated
    }

    private val _showFirstLaunchWelcome = MutableStateFlow(false)
    val showFirstLaunchWelcome: StateFlow<Boolean> = _showFirstLaunchWelcome.asStateFlow()

    val voiceState: StateFlow<VoiceState> = speechManager.voiceState
    val soundLevel: StateFlow<Float> = speechManager.soundLevel
    val rmsDb: StateFlow<Float> = speechManager.rmsDb
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

    private val _streamingAiText = MutableStateFlow<String?>(null)
    val streamingAiText: StateFlow<String?> = _streamingAiText.asStateFlow()

    private val _lastFailedMessage = MutableStateFlow<String?>(null)
    val lastFailedMessage: StateFlow<String?> = _lastFailedMessage.asStateFlow()

    private val _requestError = MutableStateFlow<String?>(null)
    val requestError: StateFlow<String?> = _requestError.asStateFlow()

    private val _localSayingsState = MutableStateFlow(
        LocalSayingsUiState(inputText = "നമുക്ക് ചായ കുടിക്കാൻ പോകാം")
    )
    val localSayingsState: StateFlow<LocalSayingsUiState> = _localSayingsState.asStateFlow()

    private val _styleRewriteState = MutableStateFlow(
        StyleRewriteUiState(inputText = "നാളെ കാണാം")
    )
    val styleRewriteState: StateFlow<StyleRewriteUiState> = _styleRewriteState.asStateFlow()

    private val _isChatWindowOpen = MutableStateFlow(false)
    val isChatWindowOpen: StateFlow<Boolean> = _isChatWindowOpen.asStateFlow()

    private val _chatSuggestions = MutableStateFlow<List<String>>(
        com.example.data.suggestions.DynamicSuggestionEngine.getInitialSuggestions(_currentDialect.value)
    )
    val chatSuggestions: StateFlow<List<String>> = _chatSuggestions.asStateFlow()

    val imageService = com.example.data.image.ImageGenerationService(application)
    val documentService = com.example.data.document.DocumentService(application)
    val writingService = com.example.data.writing.WritingService(application)

    private val _attachedImageUri = MutableStateFlow<String?>(null)
    val attachedImageUri: StateFlow<String?> = _attachedImageUri.asStateFlow()

    private val _attachedDocument = MutableStateFlow<com.example.data.document.DocumentAttachment?>(null)
    val attachedDocument: StateFlow<com.example.data.document.DocumentAttachment?> = _attachedDocument.asStateFlow()

    private val _isImageGenerating = MutableStateFlow(false)
    val isImageGenerating: StateFlow<Boolean> = _isImageGenerating.asStateFlow()

    private val _imageLoadingMessage = MutableStateFlow("Creating your image...")
    val imageLoadingMessage: StateFlow<String> = _imageLoadingMessage.asStateFlow()

    // --- Saved Items (Section 11) ---
    val allSavedItems: StateFlow<List<com.example.data.model.SavedItemEntity>> = repository.allSavedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveItem(
        title: String,
        content: String,
        itemType: String = "ANSWER",
        mediaUri: String? = null,
        prompt: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveItem(
                com.example.data.model.SavedItemEntity(
                    title = title.ifBlank { "Saved Item" },
                    content = content,
                    itemType = itemType,
                    mediaUri = mediaUri,
                    prompt = prompt,
                    dialectId = _currentDialect.value.id
                )
            )
        }
    }

    fun deleteSavedItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSavedItem(id)
        }
    }

    // --- AI Memory (Section 22) ---
    private val _isMemoryEnabled = MutableStateFlow(true)
    val isMemoryEnabled: StateFlow<Boolean> = _isMemoryEnabled.asStateFlow()

    val allMemories: StateFlow<List<com.example.data.model.MemoryItemEntity>> = repository.allMemoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMemory(key: String, value: String, category: String = "General") {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveMemory(key = key, value = value, category = category)
        }
    }

    fun toggleMemory(id: Long, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleMemoryEnabled(id, enabled)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllMemories()
        }
    }

    fun toggleMasterMemory(enabled: Boolean) {
        _isMemoryEnabled.value = enabled
    }

    // --- Image & Document Attachment Actions ---
    fun attachImage(pathOrUri: String) {
        _attachedImageUri.value = pathOrUri
        _attachedDocument.value = null // clear document if image attached
        _chatSuggestions.value = com.example.data.suggestions.DynamicSuggestionEngine.getUploadedImageSuggestions(_currentDialect.value)
    }

    fun clearAttachedImage() {
        _attachedImageUri.value = null
        _chatSuggestions.value = com.example.data.suggestions.DynamicSuggestionEngine.getInitialSuggestions(_currentDialect.value)
    }

    fun attachDocument(doc: com.example.data.document.DocumentAttachment) {
        _attachedDocument.value = doc
        _attachedImageUri.value = null // clear image if doc attached
        _chatSuggestions.value = com.example.data.suggestions.DynamicSuggestionEngine.getDocumentSuggestions(_currentDialect.value)
    }

    fun clearAttachedDocument() {
        _attachedDocument.value = null
        _chatSuggestions.value = com.example.data.suggestions.DynamicSuggestionEngine.getInitialSuggestions(_currentDialect.value)
    }

    fun saveImageToGallery(imagePath: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = com.example.data.image.ImageStorageManager.saveImageToPublicGallery(getApplication(), imagePath)
            onResult(success)
        }
    }

    fun shareImage(context: android.content.Context, imagePath: String) {
        val shareIntent = com.example.data.image.ImageStorageManager.createShareImageIntent(context, imagePath)
        if (shareIntent != null) {
            val chooser = android.content.Intent.createChooser(shareIntent, "Share Image")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }

    fun regenerateImage(prompt: String) {
        sendChatMessage(prompt, speakResponse = !isMuted.value)
    }

    fun openChatWindow() {
        _isChatWindowOpen.value = true
    }

    fun closeChatWindow() {
        _isChatWindowOpen.value = false
    }

    fun onSuggestionClicked(suggestion: String) {
        sendChatMessage(suggestion, speakResponse = !isMuted.value)
    }

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            // Observe profiles and choose active based on persisted region preference
            repository.allProfiles.collect { profiles ->
                if (profiles.isNotEmpty()) {
                    val savedRegion = com.example.data.regional.RegionalProfileRegistry.getProfileById(preferencesRepository.selectedRegionId)
                    _selectedRegionProfile.value = savedRegion

                    val matchedProfile = profiles.find { it.dialectId == savedRegion.defaultDialectId }
                        ?: profiles.find { it.isDefault }
                        ?: profiles.first()

                    _currentProfile.value = matchedProfile
                    val dialect = DialectCatalog.getDialectById(matchedProfile.dialectId)
                    _currentDialect.value = dialect
                    updatePracticePhrase(dialect)
                    _chatSuggestions.value = com.example.data.suggestions.DynamicSuggestionEngine.getInitialSuggestions(dialect)
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

    fun completeFirstLaunch() {
        preferencesRepository.hasCompletedFirstLaunch = true
        _showFirstLaunchWelcome.value = false
    }

    fun selectRegionProfile(profile: com.example.data.regional.RegionalProfile) {
        _selectedRegionProfile.value = profile
        preferencesRepository.selectedRegionId = profile.id
        preferencesRepository.selectedLanguage = profile.defaultLanguage
        preferencesRepository.selectedVoiceId = profile.defaultDialectId

        val targetDialect = profile.toRegionalDialect()
        selectDialect(targetDialect)
        _userFeedbackNotice.value = "Region & Voice set to ${profile.flagEmoji} ${profile.name}"
    }

    fun showNotice(message: String) {
        _userFeedbackNotice.value = message
    }

    fun loadMessagesForSession(sessionId: String) {
        messageJob?.cancel()
        messageJob = viewModelScope.launch {
            repository.getMessagesForConversation(sessionId).collect { msgList ->
                if (msgList.isNotEmpty() || (!_isAiThinking.value && _streamingAiText.value == null)) {
                    _messages.value = msgList
                }
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
            _isChatWindowOpen.value = true
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

    fun retryLastFailedMessage() {
        val failed = _lastFailedMessage.value ?: return
        if (_isAiThinking.value) return
        _requestError.value = null
        _lastFailedMessage.value = null
        sendChatMessage(failed)
    }

    fun dismissRequestError() {
        _requestError.value = null
    }

    fun sendChatMessage(userText: String, speakResponse: Boolean = (_inputMode.value == InputMode.VOICE)) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _isAiThinking.value) return

        val startTime = System.currentTimeMillis()
        Log.d("VoiceAiPerf", "REQUEST_STARTED [t=0ms]")

        val dialect = _currentDialect.value
        val profile = _currentProfile.value
        val strength = profile?.regionalStrength ?: 0.75f
        val personalityStr = profile?.personality ?: VoicePersonality.FRIENDLY.name
        val personality = VoicePersonality.entries.find { it.name == personalityStr } ?: VoicePersonality.FRIENDLY
        val slangEnabled = profile?.slangEnabled ?: true
        val responseLength = profile?.responseLength ?: "Balanced"
        val naturalMixing = profile?.naturalMixingEnabled ?: true
        val customPromptNotes = profile?.customPromptNotes ?: ""
        val userLoc = _userLocation.value

        _isAiThinking.value = true
        _streamingAiText.value = null
        _requestError.value = null
        _isChatWindowOpen.value = true

        // 1. Instant session resolution with meaningful title generation
        var currentSession = _activeSession.value
        val sessionId = currentSession?.id ?: "conv_${System.currentTimeMillis()}"
        val meaningfulTitle = com.example.data.suggestions.ConversationTitleGenerator.generateTitle(trimmed, dialect.dialectName)
        if (currentSession == null) {
            val newSession = ConversationSessionEntity(
                id = sessionId,
                title = meaningfulTitle,
                dialectId = dialect.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            _activeSession.value = newSession
            currentSession = newSession
            viewModelScope.launch(Dispatchers.IO) {
                repository.createNewSession(meaningfulTitle, dialect.id)
                loadMessagesForSession(sessionId)
            }
        } else if (currentSession.title.startsWith("New Chat") || currentSession.title.startsWith("Chat with")) {
            val updatedSession = currentSession.copy(title = meaningfulTitle, updatedAt = System.currentTimeMillis())
            _activeSession.value = updatedSession
            viewModelScope.launch(Dispatchers.IO) {
                repository.renameSession(sessionId, meaningfulTitle)
            }
        }

        // 2. Instant user message rendering (zero UI delay)
        val userTimestamp = System.currentTimeMillis()
        val attachedImage = _attachedImageUri.value
        _attachedImageUri.value = null // consume attachment
        val attachedDoc = _attachedDocument.value
        _attachedDocument.value = null // consume attachment

        val tempUserMsg = ConversationMessageEntity(
            id = userTimestamp,
            conversationId = sessionId,
            role = "user",
            text = trimmed,
            timestamp = userTimestamp,
            dialectId = dialect.id,
            attachedImageUri = attachedImage,
            documentUri = attachedDoc?.localPath,
            documentName = attachedDoc?.fileName
        )
        _messages.value = _messages.value + tempUserMsg
        Log.d("VoiceAiPerf", "UI_UPDATED [t=${System.currentTimeMillis() - startTime}ms] user message rendered")

        // 3. Asynchronously persist user message in background
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveConversationMessage(
                    conversationId = sessionId,
                    dialectId = dialect.id,
                    role = "user",
                    text = trimmed,
                    attachedImageUri = attachedImage,
                    documentUri = attachedDoc?.localPath,
                    documentName = attachedDoc?.fileName
                )
            } catch (e: Exception) {
                Log.e("VoiceViewModel", "Failed to persist user message", e)
            }
        }

        // 3b. Optional transparent AI memory detection (Section 22)
        if (_isMemoryEnabled.value) {
            val memoryIntent = com.example.data.memory.AIMemoryManager.extractMemoryIntent(trimmed)
            if (memoryIntent != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.saveMemory(key = memoryIntent.first, value = memoryIntent.second)
                }
            }
        }

        // 4a. Handle Document Analysis (Section 18 & 40)
        if (attachedDoc != null) {
            viewModelScope.launch {
                _isAiThinking.value = true
                _streamingAiText.value = null
                try {
                    val docResult = documentService.analyzeDocument(attachedDoc, trimmed, dialect)
                    when (docResult) {
                        is com.example.data.document.DocumentServiceResult.Success -> {
                            val assistantTimestamp = System.currentTimeMillis()
                            val assistantMsg = ConversationMessageEntity(
                                id = assistantTimestamp,
                                conversationId = sessionId,
                                role = "assistant",
                                text = docResult.responseText,
                                timestamp = assistantTimestamp,
                                dialectId = dialect.id,
                                documentName = docResult.documentName
                            )
                            _messages.value = _messages.value + assistantMsg
                            _chatSuggestions.value = com.example.data.suggestions.DynamicSuggestionEngine.getDocumentSuggestions(dialect)

                            viewModelScope.launch(Dispatchers.IO) {
                                try {
                                    repository.saveConversationMessage(
                                        conversationId = sessionId,
                                        dialectId = dialect.id,
                                        role = "assistant",
                                        text = docResult.responseText,
                                        documentName = docResult.documentName
                                    )
                                } catch (e: Exception) {
                                    Log.e("VoiceViewModel", "Failed to persist assistant doc message", e)
                                }
                            }

                            if (speakResponse && !isMuted.value && docResult.responseText.isNotBlank()) {
                                speechManager.speak(
                                    text = docResult.responseText,
                                    localeCode = dialect.localeCode,
                                    speechPacing = profile?.voiceSpeed ?: "Natural",
                                    personalityName = personality.name
                                )
                            }
                        }
                        is com.example.data.document.DocumentServiceResult.Error -> {
                            _lastFailedMessage.value = trimmed
                            _requestError.value = docResult.errorMessage
                        }
                    }
                } catch (e: Exception) {
                    Log.e("VoiceViewModel", "Document analysis failed", e)
                    _lastFailedMessage.value = trimmed
                    _requestError.value = "Document analysis failed. Please try again."
                } finally {
                    _isAiThinking.value = false
                }
            }
            return
        }

        // 4b. Handle Image Generation, Vision Analysis, or Normal Text/Voice AI
        val isImageGen = attachedImage == null && com.example.data.image.ImageIntentDetector.isImageGenerationRequest(trimmed)
        val isImageEdit = attachedImage != null && com.example.data.image.ImageIntentDetector.isImageEditRequest(trimmed, true)
        val isVisionAnalysis = attachedImage != null && !isImageEdit

        if (isImageGen || isImageEdit || isVisionAnalysis) {
            viewModelScope.launch {
                _isImageGenerating.value = true
                _imageLoadingMessage.value = when {
                    isImageEdit -> "Editing your image..."
                    isImageGen -> "Creating your image..."
                    else -> "Analyzing your image..."
                }

                try {
                    val result = when {
                        isImageEdit -> imageService.editImage(attachedImage!!, trimmed, dialect)
                        isImageGen -> imageService.generateImage(trimmed, dialect)
                        else -> imageService.analyzeImage(attachedImage!!, trimmed, dialect)
                    }

                    when (result) {
                        is com.example.data.image.ImageServiceResult.Success -> {
                            val assistantTimestamp = System.currentTimeMillis()
                            val assistantMsg = ConversationMessageEntity(
                                id = assistantTimestamp,
                                conversationId = sessionId,
                                role = "assistant",
                                text = result.textResponse,
                                timestamp = assistantTimestamp,
                                dialectId = dialect.id,
                                imageUri = result.imagePath,
                                isImageGeneration = result.isImageGenerated,
                                imagePrompt = result.promptUsed
                            )
                            _messages.value = _messages.value + assistantMsg
                            _lastFailedMessage.value = null

                            viewModelScope.launch(Dispatchers.IO) {
                                try {
                                    repository.saveConversationMessage(
                                        conversationId = sessionId,
                                        dialectId = dialect.id,
                                        role = "assistant",
                                        text = result.textResponse,
                                        imageUri = result.imagePath,
                                        isImageGeneration = result.isImageGenerated,
                                        imagePrompt = result.promptUsed
                                    )
                                } catch (e: Exception) {
                                    Log.e("VoiceViewModel", "Failed to persist assistant image message", e)
                                }
                            }

                            if (result.isImageGenerated) {
                                _chatSuggestions.value = com.example.data.suggestions.DynamicSuggestionEngine.getGeneratedImageSuggestions(dialect)
                            } else {
                                _chatSuggestions.value = com.example.data.suggestions.DynamicSuggestionEngine.generateSuggestions(
                                    userMessage = trimmed,
                                    aiResponse = result.textResponse,
                                    dialect = dialect
                                )
                            }

                            if (speakResponse && !isMuted.value && result.textResponse.isNotBlank()) {
                                speechManager.speak(
                                    text = result.textResponse,
                                    localeCode = dialect.localeCode,
                                    speechPacing = profile?.voiceSpeed ?: "Natural",
                                    personalityName = personality.name
                                )
                            }
                        }
                        is com.example.data.image.ImageServiceResult.Error -> {
                            _lastFailedMessage.value = trimmed
                            _requestError.value = result.errorMessage
                        }
                    }
                } catch (e: Exception) {
                    Log.e("VoiceViewModel", "Image operation failed", e)
                    _lastFailedMessage.value = trimmed
                    _requestError.value = "Image operation failed. Please try again."
                } finally {
                    _isImageGenerating.value = false
                    _isAiThinking.value = false
                }
            }
            return
        }

        // 5. Normal text/voice AI streaming request with timeout
        viewModelScope.launch {
            try {
                val history = _messages.value.filter { it.role != "system" }.takeLast(6).map { it.role to it.text }
                val queryContext = LocationExtractor.analyzeQuery(
                    query = trimmed,
                    conversationHistory = history,
                    userLocation = userLoc
                )
                val targetLocation = queryContext.requestedLocation

                val aiRequest = AIRequest(
                    message = trimmed,
                    regionalProfile = _selectedRegionProfile.value,
                    queryLocation = targetLocation,
                    userLocation = userLoc,
                    queryContext = queryContext,
                    conversationHistory = history
                )

                val fullStreamBuilder = StringBuilder()

                val activeMemories = if (_isMemoryEnabled.value) {
                    repository.getActiveMemoriesList()
                } else emptyList()
                val memoryDirectives = com.example.data.memory.AIMemoryManager.formatMemoriesForPrompt(activeMemories)
                val effectiveCustomPromptNotes = if (memoryDirectives.isNotBlank()) {
                    "$customPromptNotes\n$memoryDirectives".trim()
                } else customPromptNotes

                val aiResponse = withTimeout(25000L) {
                    repository.streamAiVoiceResponse(
                        request = aiRequest,
                        strength = strength,
                        personality = personality,
                        slangEnabled = slangEnabled,
                        responseLength = responseLength,
                        naturalMixingEnabled = naturalMixing,
                        customPromptNotes = effectiveCustomPromptNotes,
                        inputSource = if (speakResponse) "VOICE" else "TEXT",
                        conversationId = sessionId,
                        onFirstToken = {
                            _isAiThinking.value = false
                            Log.d("VoiceAiPerf", "UI_UPDATED [t=${System.currentTimeMillis() - startTime}ms] first token received")
                        },
                        onToken = { token ->
                            fullStreamBuilder.append(token)
                            _streamingAiText.value = fullStreamBuilder.toString()
                        }
                    )
                }

                Log.d("VoiceAiPerf", "AI_RESPONSE_COMPLETED [t=${System.currentTimeMillis() - startTime}ms] length=${aiResponse.length}")

                val detectedSlang = dialect.typicalExpressions.filter {
                    aiResponse.contains(it.expression, ignoreCase = true)
                }.map { it.expression }

                // Save assistant message to DB asynchronously
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        repository.saveConversationMessage(
                            conversationId = sessionId,
                            dialectId = dialect.id,
                            role = "assistant",
                            text = aiResponse,
                            highlightedSlang = detectedSlang
                        )
                    } catch (e: Exception) {
                        Log.e("VoiceViewModel", "Failed to persist assistant message", e)
                    }
                }

                // Append assistant message in UI state
                val assistantTimestamp = System.currentTimeMillis()
                val tempAssistantMsg = ConversationMessageEntity(
                    id = assistantTimestamp,
                    conversationId = sessionId,
                    role = "assistant",
                    text = aiResponse,
                    timestamp = assistantTimestamp,
                    dialectId = dialect.id,
                    highlightedSlangCsv = detectedSlang.joinToString(",")
                )
                _messages.value = _messages.value + tempAssistantMsg
                _streamingAiText.value = null
                _isAiThinking.value = false
                _lastFailedMessage.value = null
                _chatSuggestions.value = com.example.data.suggestions.DynamicSuggestionEngine.generateSuggestions(
                    userMessage = trimmed,
                    aiResponse = aiResponse,
                    dialect = dialect
                )
                Log.d("VoiceAiPerf", "UI_UPDATED [t=${System.currentTimeMillis() - startTime}ms] final response rendered & suggestions updated")

                if (speakResponse && !isMuted.value) {
                    val pacing = profile?.voiceSpeed ?: "Natural"
                    speechManager.speak(
                        text = aiResponse,
                        localeCode = dialect.localeCode,
                        speechPacing = pacing,
                        personalityName = personality.name
                    )
                }
            } catch (te: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e("VoiceViewModel", "AI request timed out", te)
                _isAiThinking.value = false
                _streamingAiText.value = null
                _lastFailedMessage.value = trimmed
                _requestError.value = "Request timed out. Tap Retry to try again."
            } catch (e: Exception) {
                Log.e("VoiceViewModel", "AI request failed", e)
                _isAiThinking.value = false
                _streamingAiText.value = null
                _lastFailedMessage.value = trimmed
                _requestError.value = "Unable to connect. Tap Retry to try again."
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun startNewConversation() {
        val dialect = _currentDialect.value
        _messages.value = emptyList()
        _streamingAiText.value = null
        _isAiThinking.value = false
        _requestError.value = null
        _lastFailedMessage.value = null
        _activeSession.value = null
        _attachedImageUri.value = null
        _isImageGenerating.value = false
        _chatSuggestions.value = com.example.data.suggestions.DynamicSuggestionEngine.getInitialSuggestions(dialect)
        _isChatWindowOpen.value = true
    }

    fun selectConversation(session: ConversationSessionEntity) {
        _activeSession.value = session
        val dialect = DialectCatalog.getDialectById(session.dialectId)
        _currentDialect.value = dialect
        loadMessagesForSession(session.id)
        _isChatWindowOpen.value = true
        viewModelScope.launch {
            repository.getMessagesForConversation(session.id).collect { msgList ->
                val lastUser = msgList.lastOrNull { it.role == "user" }?.text ?: ""
                val lastAi = msgList.lastOrNull { it.role == "assistant" }?.text ?: ""
                if (lastUser.isNotBlank()) {
                    _chatSuggestions.value = com.example.data.suggestions.DynamicSuggestionEngine.generateSuggestions(
                        userMessage = lastUser,
                        aiResponse = lastAi,
                        dialect = dialect
                    )
                } else {
                    _chatSuggestions.value = com.example.data.suggestions.DynamicSuggestionEngine.getInitialSuggestions(dialect)
                }
            }
        }
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
