package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Strict Voice State Machine (Requirement 146).
 */
enum class VoiceState {
    IDLE,
    REQUESTING_PERMISSION,
    INITIALIZING_AUDIO,
    LISTENING,              // Backward-compatibility alias for LISTENING_FOR_SPEECH
    LISTENING_FOR_SPEECH,
    SPEECH_DETECTED,
    TRANSCRIBING,
    END_OF_SPEECH,
    FINALIZING_TRANSCRIPT,
    READY_TO_SEND,
    PROCESSING,             // Backward-compatibility alias for PROCESSING_AI
    PROCESSING_AI,
    SPEAKING,               // Backward-compatibility alias for AI_SPEAKING
    AI_SPEAKING,
    MIC_PERMISSION_ERROR,
    AUDIO_ERROR,
    STT_ERROR,
    NETWORK_ERROR,
    ERROR;                  // General fallback error

    val isRecording: Boolean get() = this == INITIALIZING_AUDIO ||
            this == LISTENING ||
            this == LISTENING_FOR_SPEECH ||
            this == SPEECH_DETECTED ||
            this == TRANSCRIBING

    val isSpeaking: Boolean get() = this == SPEAKING || this == AI_SPEAKING

    val isProcessing: Boolean get() = this == PROCESSING ||
            this == PROCESSING_AI ||
            this == END_OF_SPEECH ||
            this == FINALIZING_TRANSCRIPT ||
            this == READY_TO_SEND ||
            this == TRANSCRIBING

    val isError: Boolean get() = this == MIC_PERMISSION_ERROR ||
            this == AUDIO_ERROR ||
            this == STT_ERROR ||
            this == NETWORK_ERROR ||
            this == ERROR
}

/**
 * Production-Grade Voice Interaction Pipeline Manager
 * Traces and coordinates:
 * MIC BUTTON -> PERMISSION -> HARDWARE ACCESS -> AUDIO CAPTURE -> VAD -> STT -> TRANSCRIPT -> AI
 */
class VoiceSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val tag = "VoiceSpeechManager"

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val audioRecordHelper = AudioRecordHelper(context)
    var onTranscribeAudio: (suspend (ByteArray, String) -> String?)? = null
    var onVirtualModeUtteranceRequested: (() -> String)? = null
    private var isUsingDirectAudioRecord = false
    private var activeLocaleCode = "en-US"

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var silenceMonitorJob: Job? = null

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private val _lastFinalTranscript = MutableStateFlow("")
    val lastFinalTranscript: StateFlow<String> = _lastFinalTranscript.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _soundLevel = MutableStateFlow(0f)
    val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

    private val _voiceError = MutableStateFlow<VoiceInputError?>(null)
    val voiceError: StateFlow<VoiceInputError?> = _voiceError.asStateFlow()

    private val _voiceSettings = MutableStateFlow(VoiceInputSettings())
    val voiceSettings: StateFlow<VoiceInputSettings> = _voiceSettings.asStateFlow()

    var onSpeechRecognized: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    // VAD & Turn Management State (Requirements 128, 130, 131, 147)
    private var currentSessionId: String = ""
    private var recordingStartTimeMs: Long = 0L
    private var lastSpeechDetectedTimeMs: Long = 0L
    private var hasDetectedSpeech: Boolean = false
    private var lastRecognizedText: String = ""
    private val isFinalizing = AtomicBoolean(false)

    private var lastQueuedUtteranceId: String? = null

    init {
        textToSpeech = TextToSpeech(context, this)
        initSpeechRecognizer()
    }

    fun updateSettings(settings: VoiceInputSettings) {
        _voiceSettings.value = settings
    }

    fun setInputMode(mode: VoiceInputMode) {
        _voiceSettings.value = _voiceSettings.value.copy(mode = mode)
    }

    fun setSilenceTimeout(timeoutMs: Long) {
        _voiceSettings.value = _voiceSettings.value.copy(silenceTimeoutMs = timeoutMs)
    }

    fun setMaxDuration(seconds: Int) {
        _voiceSettings.value = _voiceSettings.value.copy(maxDurationSeconds = seconds)
    }

    fun setConfirmationMode(mode: TranscriptConfirmationMode) {
        _voiceSettings.value = _voiceSettings.value.copy(confirmationMode = mode)
    }

    fun setDisableVad(disable: Boolean) {
        _voiceSettings.value = _voiceSettings.value.copy(disableVad = disable)
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                VoicePipelineLogger.log(
                    VoicePipelineLogger.Event.MIC_STARTED,
                    "SpeechRecognizer ready for speech",
                    currentSessionId
                )
                VoicePipelineLogger.updateDiagnostics { diag ->
                    diag.copy(sttConnection = "CONNECTED")
                }
                if (_voiceState.value != VoiceState.SPEECH_DETECTED) {
                    _voiceState.value = VoiceState.LISTENING_FOR_SPEECH
                }
            }

            override fun onBeginningOfSpeech() {
                VoicePipelineLogger.log(
                    VoicePipelineLogger.Event.SPEECH_DETECTED,
                    "Beginning of speech detected by recognizer",
                    currentSessionId
                )
                interruptAi()
                hasDetectedSpeech = true
                lastSpeechDetectedTimeMs = System.currentTimeMillis()
                _voiceState.value = VoiceState.SPEECH_DETECTED
                VoicePipelineLogger.updateDiagnostics { diag ->
                    diag.copy(speechDetected = true, sttConnection = "TRANSCRIBING")
                }
            }

            override fun onRmsChanged(rmsdB: Float) {
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.0f, 1.0f)
                _soundLevel.value = normalized

                if (normalized > 0.14f && _voiceState.value.isRecording) {
                    hasDetectedSpeech = true
                    lastSpeechDetectedTimeMs = System.currentTimeMillis()
                    if (_voiceState.value == VoiceState.LISTENING || _voiceState.value == VoiceState.LISTENING_FOR_SPEECH) {
                        _voiceState.value = VoiceState.SPEECH_DETECTED
                    }
                    VoicePipelineLogger.updateDiagnostics { diag ->
                        diag.copy(
                            speechDetected = true,
                            inputLevelDb = rmsdB,
                            inputLevelNormalized = normalized
                        )
                    }
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                if (buffer != null && buffer.isNotEmpty()) {
                    VoicePipelineLogger.updateDiagnostics { diag ->
                        diag.copy(
                            audioFrames = diag.audioFrames + (buffer.size / 2),
                            nonZeroSamples = diag.nonZeroSamples + (buffer.size / 2)
                        )
                    }
                }
            }

            override fun onEndOfSpeech() {
                VoicePipelineLogger.log(
                    VoicePipelineLogger.Event.RECORDING_STOPPED,
                    "Recognizer onEndOfSpeech",
                    currentSessionId
                )
                _soundLevel.value = 0f
                _voiceState.value = VoiceState.END_OF_SPEECH
                finalizeTurn(reason = "RECOGNIZER_END_OF_SPEECH")
            }

            override fun onError(error: Int) {
                VoicePipelineLogger.log(
                    VoicePipelineLogger.Event.RECOGNITION_ERROR,
                    "SpeechRecognizer error code: $error",
                    currentSessionId
                )
                _soundLevel.value = 0f

                // If error is client or busy, re-create recognizer
                if (error == SpeechRecognizer.ERROR_CLIENT || error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    try {
                        speechRecognizer?.destroy()
                        speechRecognizer = null
                    } catch (_: Exception) {}
                }

                // If we captured partial transcript, finalize it successfully
                if (lastRecognizedText.isNotBlank() || _partialTranscript.value.isNotBlank()) {
                    finalizeTurn(reason = "ERROR_RECOVERY_WITH_TRANSCRIPT")
                    return
                }

                silenceMonitorJob?.cancel()

                // Check if audio frames actually arrived (Requirement 128)
                val diag = VoicePipelineLogger.liveDiagnostics.value
                val hasAudioArrived = diag.audioFrames > 0L || hasDetectedSpeech

                // If recognizer failed due to audio/client/server or no-match after speech, fall back to direct audio recording
                if (error == SpeechRecognizer.ERROR_AUDIO ||
                    error == SpeechRecognizer.ERROR_CLIENT ||
                    error == SpeechRecognizer.ERROR_SERVER ||
                    (error == SpeechRecognizer.ERROR_NO_MATCH && hasAudioArrived)
                ) {
                    Log.w(tag, "SpeechRecognizer failed ($error), seamless fallback to direct AudioRecord engine")
                    try {
                        speechRecognizer?.cancel()
                    } catch (_: Exception) {}
                    startDirectAudioRecording(activeLocaleCode)
                    return
                }

                val (inputError, targetState) = when (error) {
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        VoiceInputError.PermissionDenied to VoiceState.MIC_PERMISSION_ERROR
                    }
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                        VoiceInputError.NetworkError to VoiceState.NETWORK_ERROR
                    }
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        if (!hasAudioArrived) {
                            VoiceInputError.NoSpeech to VoiceState.IDLE
                        } else {
                            VoiceInputError.NoTranscript to VoiceState.STT_ERROR
                        }
                    }
                    else -> {
                        VoiceInputError.General("Recognition error ($error)") to VoiceState.STT_ERROR
                    }
                }

                _voiceState.value = targetState
                _voiceError.value = inputError
                _partialTranscript.value = ""
                VoicePipelineLogger.updateDiagnostics { it.copy(lastError = inputError.technicalCode, sttConnection = "ERROR") }
                onError?.invoke(inputError.message)

                scope.launch {
                    delay(2000)
                    if (_voiceState.value.isError) {
                        _voiceState.value = VoiceState.IDLE
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.trim() ?: ""
                VoicePipelineLogger.log(
                    VoicePipelineLogger.Event.FINAL_TRANSCRIPT,
                    "Results received: '$text'",
                    currentSessionId
                )
                if (text.isNotBlank()) {
                    lastRecognizedText = text
                    VoicePipelineLogger.updateDiagnostics { diag ->
                        diag.copy(finalTranscript = text, sttConnection = "DONE")
                    }
                }
                finalizeTurn(reason = "ON_RESULTS")
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.trim() ?: ""
                if (text.isNotBlank()) {
                    _partialTranscript.value = text
                    lastRecognizedText = text
                    hasDetectedSpeech = true
                    lastSpeechDetectedTimeMs = System.currentTimeMillis()
                    _voiceState.value = VoiceState.SPEECH_DETECTED
                    VoicePipelineLogger.log(
                        VoicePipelineLogger.Event.PARTIAL_TRANSCRIPT,
                        text,
                        currentSessionId
                    )
                    VoicePipelineLogger.updateDiagnostics { diag ->
                        diag.copy(
                            partialTranscript = text,
                            speechDetected = true,
                            sttConnection = "TRANSCRIBING"
                        )
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun initSpeechRecognizer(): Boolean {
        return try {
            speechRecognizer?.destroy()
            speechRecognizer = null

            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createRecognitionListener())
                }
                true
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S &&
                SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
            ) {
                speechRecognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context).apply {
                    setRecognitionListener(createRecognitionListener())
                }
                true
            } else {
                try {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createRecognitionListener())
                    }
                    speechRecognizer != null
                } catch (_: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize SpeechRecognizer", e)
            false
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _voiceState.value = VoiceState.AI_SPEAKING
                    VoicePipelineLogger.log(VoicePipelineLogger.Event.TTS_STARTED, utteranceId ?: "", currentSessionId)
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId == lastQueuedUtteranceId) {
                        _voiceState.value = VoiceState.IDLE
                        VoicePipelineLogger.log(VoicePipelineLogger.Event.TTS_COMPLETED, utteranceId ?: "", currentSessionId)
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (utteranceId == lastQueuedUtteranceId) {
                        _voiceState.value = VoiceState.IDLE
                        VoicePipelineLogger.log(VoicePipelineLogger.Event.RECOGNITION_ERROR, "TTS error on $utteranceId", currentSessionId)
                    }
                }
            })
        }
    }

    /**
     * Complete Microphone Pipeline Execution (Requirements 126, 127, 131, 136, 139, 146, 147).
     */
    fun startListening(localeCode: String = "en-US") {
        // 1. Interrupt any active AI TTS speech immediately (Requirement 139)
        interruptAi()

        // 2. Start a fresh Voice Session with unique ID (Requirement 147)
        currentSessionId = VoicePipelineLogger.createNewSession()
        activeLocaleCode = localeCode

        isFinalizing.set(false)
        _voiceError.value = null
        _partialTranscript.value = ""
        lastRecognizedText = ""
        hasDetectedSpeech = false
        recordingStartTimeMs = System.currentTimeMillis()
        lastSpeechDetectedTimeMs = 0L
        isUsingDirectAudioRecord = false

        VoicePipelineLogger.log(
            VoicePipelineLogger.Event.PERMISSION_CHECK,
            "Verifying permission for $localeCode",
            currentSessionId
        )

        // 3. Microphone Permission Check (Requirement 127)
        if (!audioRecordHelper.hasMicPermission()) {
            VoicePipelineLogger.log(
                VoicePipelineLogger.Event.PERMISSION_DENIED,
                "RECORD_AUDIO permission missing",
                currentSessionId
            )
            val err = VoiceInputError.PermissionDenied
            _voiceState.value = VoiceState.MIC_PERMISSION_ERROR
            _voiceError.value = err
            VoicePipelineLogger.updateDiagnostics {
                it.copy(permissionGranted = false, lastError = err.technicalCode)
            }
            onError?.invoke(err.message)
            return
        }

        VoicePipelineLogger.updateDiagnostics { it.copy(permissionGranted = true) }
        _voiceState.value = VoiceState.INITIALIZING_AUDIO

        // 4. Verify microphone access and start SpeechRecognizer or direct AudioRecord
        val recognizerReady = speechRecognizer != null || initSpeechRecognizer()

        if (recognizerReady && speechRecognizer != null) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeCode)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeCode)

                    // Multi-language code-switching hints (Requirement 136)
                    val additionalLangs = when {
                        localeCode.startsWith("ml") -> arrayOf("ml-IN", "en-IN", "en-US")
                        localeCode.startsWith("ta") -> arrayOf("ta-IN", "en-IN", "en-US")
                        localeCode.startsWith("hi") -> arrayOf("hi-IN", "en-IN", "en-US")
                        localeCode.startsWith("kn") -> arrayOf("kn-IN", "en-IN", "en-US")
                        localeCode.startsWith("te") -> arrayOf("te-IN", "en-IN", "en-US")
                        localeCode.startsWith("bn") -> arrayOf("bn-IN", "en-IN", "en-US")
                        localeCode.startsWith("pa") -> arrayOf("pa-IN", "en-IN", "en-US")
                        else -> arrayOf(localeCode, "en-IN", "en-US")
                    }
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", additionalLangs)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)

                    // Provide generous silence timeouts to prevent premature STT cutoff (Requirement 130)
                    val timeout = _voiceSettings.value.silenceTimeoutMs.coerceAtLeast(1800L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, timeout)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, timeout)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000L)
                }

                speechRecognizer?.startListening(intent)
                _voiceState.value = VoiceState.LISTENING_FOR_SPEECH
                startSilenceAndSafetyWatcher()
                return
            } catch (e: Exception) {
                Log.w(tag, "SpeechRecognizer startListening failed, falling back to direct AudioRecord: ${e.message}")
            }
        }

        // Direct AudioRecord engine fallback
        startDirectAudioRecording(localeCode)
    }

    private fun startDirectAudioRecording(localeCode: String) {
        isUsingDirectAudioRecord = true
        VoicePipelineLogger.log(
            VoicePipelineLogger.Event.AUDIO_INITIALIZED,
            "Direct AudioRecord engine active",
            currentSessionId
        )

        val started = audioRecordHelper.startRecording(scope) { amplitude, rmsDb ->
            _soundLevel.value = amplitude
            if (amplitude > 0.08f) {
                hasDetectedSpeech = true
                lastSpeechDetectedTimeMs = System.currentTimeMillis()
                if (_voiceState.value == VoiceState.LISTENING || _voiceState.value == VoiceState.LISTENING_FOR_SPEECH) {
                    _voiceState.value = VoiceState.SPEECH_DETECTED
                }
            }
        }

        if (started) {
            _voiceState.value = VoiceState.LISTENING_FOR_SPEECH
            _partialTranscript.value = "Listening to your voice…"
            VoicePipelineLogger.updateDiagnostics { diag ->
                diag.copy(sttConnection = "CONNECTED")
            }
            startSilenceAndSafetyWatcher()
        } else {
            val err = VoiceInputError.MicUnavailable
            _voiceState.value = VoiceState.AUDIO_ERROR
            _voiceError.value = err
            VoicePipelineLogger.updateDiagnostics { diag ->
                diag.copy(lastError = err.technicalCode, sttConnection = "ERROR")
            }
            onError?.invoke(err.message)

            scope.launch {
                delay(2500)
                if (_voiceState.value.isError) {
                    _voiceState.value = VoiceState.IDLE
                }
            }
        }
    }

    /**
     * Continuous background watcher enforcing:
     * 1. Startup grace period (Requirement 131): Does NOT trigger silence detection prematurely.
     * 2. Auto silence detection (Requirement 130): Evaluates silence ONLY after speech has been detected.
     * 3. Max safety limit: Automatically finalizes long recordings.
     */
    private fun startSilenceAndSafetyWatcher() {
        silenceMonitorJob?.cancel()
        silenceMonitorJob = scope.launch {
            while (isActive && _voiceState.value.isRecording) {
                delay(100)
                val now = System.currentTimeMillis()
                val currentSettings = _voiceSettings.value
                val elapsedSinceStart = now - recordingStartTimeMs

                // 1. Maximum Duration Safety Limit
                if (elapsedSinceStart >= currentSettings.maxDurationSeconds * 1000L) {
                    VoicePipelineLogger.log(
                        VoicePipelineLogger.Event.MAX_DURATION_REACHED,
                        "Max limit reached (${currentSettings.maxDurationSeconds}s)",
                        currentSessionId
                    )
                    finalizeTurn(reason = "MAX_SAFETY_LIMIT_REACHED")
                    break
                }

                // 2. Bypass VAD if developer test mode is active (Requirement 133 «Raw STT Test»)
                if (currentSettings.disableVad) {
                    continue
                }

                // 3. Startup Grace Period (Requirement 131):
                // Do NOT check silence during the initial grace period (e.g. first 6s)
                if (elapsedSinceStart < currentSettings.startupGracePeriodMs) {
                    continue
                }

                // 4. Silence Detection in AUTO mode:
                // Only triggers after speech was actually detected!
                if (currentSettings.mode == VoiceInputMode.AUTO && hasDetectedSpeech && lastSpeechDetectedTimeMs > 0L) {
                    val silenceDuration = now - lastSpeechDetectedTimeMs
                    if (silenceDuration >= currentSettings.silenceTimeoutMs) {
                        VoicePipelineLogger.log(
                            VoicePipelineLogger.Event.SILENCE_DETECTED,
                            "End of speech detected: ${silenceDuration}ms >= ${currentSettings.silenceTimeoutMs}ms",
                            currentSessionId
                        )
                        _voiceState.value = VoiceState.END_OF_SPEECH
                        finalizeTurn(reason = "SUSTAINED_SILENCE_DETECTED")
                        break
                    }
                }
            }
        }
    }

    fun stopListening() {
        VoicePipelineLogger.log(VoicePipelineLogger.Event.RECORDING_STOPPED, "User manually stopped voice input", currentSessionId)
        _voiceState.value = VoiceState.END_OF_SPEECH
        finalizeTurn(reason = "USER_MANUAL_STOP")
    }

    /**
     * Finalizes current voice turn with real audio verification (Requirement 128, 144, 145).
     */
    private fun finalizeTurn(reason: String) {
        if (!isFinalizing.compareAndSet(false, true)) {
            return
        }

        silenceMonitorJob?.cancel()
        _voiceState.value = VoiceState.FINALIZING_TRANSCRIPT

        if (isUsingDirectAudioRecord) {
            val isVirtual = audioRecordHelper.isVirtualMode
            val audioBytes = audioRecordHelper.stopRecording()
            _soundLevel.value = 0f

            // Virtual audio mode for emulators
            if (isVirtual) {
                _voiceState.value = VoiceState.PROCESSING_AI
                _partialTranscript.value = "Generating regional voice response…"
                scope.launch {
                    val prompt = onVirtualModeUtteranceRequested?.invoke()
                    if (!prompt.isNullOrBlank()) {
                        VoicePipelineLogger.log(VoicePipelineLogger.Event.FINAL_TRANSCRIPT, "Virtual prompt: '$prompt'", currentSessionId)
                        _lastFinalTranscript.value = prompt
                        _partialTranscript.value = ""
                        _voiceState.value = VoiceState.IDLE
                        onSpeechRecognized?.invoke(prompt)
                    } else {
                        _voiceState.value = VoiceState.IDLE
                        _partialTranscript.value = ""
                        onError?.invoke("Select a dialect phrase or write your message to hear the AI speak.")
                    }
                }
                return
            }

            // Real Audio Verification (Requirement 128)
            val framesRead = audioRecordHelper.totalFramesRead
            val nonZero = audioRecordHelper.nonZeroSamples

            if (framesRead == 0L || (audioBytes == null || audioBytes.isEmpty())) {
                // Audio capture failed to receive frames (Requirement 128, 142)
                VoicePipelineLogger.log(VoicePipelineLogger.Event.AUDIO_ERROR, "Zero audio frames received", currentSessionId)
                val err = VoiceInputError.AudioCaptureFailed
                _voiceState.value = VoiceState.AUDIO_ERROR
                _voiceError.value = err
                _partialTranscript.value = ""
                onError?.invoke(err.message)
                return
            }

            if (audioBytes.isNotEmpty()) {
                _voiceState.value = VoiceState.TRANSCRIBING
                _partialTranscript.value = "Transcribing with regional speech AI…"
                scope.launch {
                    try {
                        val transcribed = onTranscribeAudio?.invoke(audioBytes, activeLocaleCode)?.trim()
                        if (!transcribed.isNullOrBlank()) {
                            VoicePipelineLogger.log(VoicePipelineLogger.Event.FINAL_TRANSCRIPT, "Transcribed: '$transcribed'", currentSessionId)
                            _lastFinalTranscript.value = transcribed
                            _partialTranscript.value = ""
                            _voiceState.value = VoiceState.IDLE
                            onSpeechRecognized?.invoke(transcribed)
                        } else {
                            if (!hasDetectedSpeech && nonZero < 100L) {
                                val err = VoiceInputError.NoSpeech
                                _voiceState.value = VoiceState.IDLE
                                _voiceError.value = err
                                _partialTranscript.value = ""
                                onError?.invoke(err.message)
                            } else {
                                val err = VoiceInputError.NoTranscript
                                _voiceState.value = VoiceState.STT_ERROR
                                _voiceError.value = err
                                _partialTranscript.value = ""
                                onError?.invoke(err.message)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Transcription error", e)
                        val err = VoiceInputError.General("Could not transcribe speech: ${e.message}")
                        _voiceState.value = VoiceState.STT_ERROR
                        _voiceError.value = err
                        _partialTranscript.value = ""
                        onError?.invoke(err.message)
                    }
                }
                return
            }
        }

        // Standard Android SpeechRecognizer turn finalization
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}

        _soundLevel.value = 0f
        val finalText = lastRecognizedText.ifBlank { _partialTranscript.value }.trim()

        if (finalText.isNotBlank()) {
            VoicePipelineLogger.log(VoicePipelineLogger.Event.FINAL_TRANSCRIPT, "Finalized ($reason): '$finalText'", currentSessionId)
            _voiceState.value = VoiceState.READY_TO_SEND
            _lastFinalTranscript.value = finalText
            _partialTranscript.value = ""

            // Deliver transcript
            onSpeechRecognized?.invoke(finalText)
        } else {
            val diag = VoicePipelineLogger.liveDiagnostics.value
            val err = if (!hasDetectedSpeech && diag.audioFrames < 50L) {
                VoiceInputError.NoSpeech
            } else {
                VoiceInputError.NoTranscript
            }
            VoicePipelineLogger.log(VoicePipelineLogger.Event.RECOGNITION_ERROR, "No transcript generated on finalize ($reason)", currentSessionId)
            _voiceState.value = VoiceState.IDLE
            _voiceError.value = err
            _partialTranscript.value = ""
            onError?.invoke(err.message)
        }
    }

    /**
     * Complete Audio Session Reset & Retry (Requirement 144).
     * FULLY resets audio, focus, VAD, STT state, and starts fresh session.
     */
    fun retryListening(localeCode: String = "en-US") {
        VoicePipelineLogger.log(VoicePipelineLogger.Event.SESSION_STARTED, "Full reset & retry requested", currentSessionId)

        // 1. Stop and cancel STT
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}

        // 2. Stop and release AudioRecord
        audioRecordHelper.stopRecording()
        audioRecordHelper.release()

        // 3. Clear transcripts and errors
        _voiceError.value = null
        _partialTranscript.value = ""
        lastRecognizedText = ""
        hasDetectedSpeech = false
        _soundLevel.value = 0f
        _voiceState.value = VoiceState.IDLE

        // 4. Start fresh voice session
        startListening(localeCode)
    }

    fun interruptAi() {
        try {
            lastQueuedUtteranceId = null
            if (_voiceState.value.isSpeaking || textToSpeech?.isSpeaking == true) {
                textToSpeech?.stop()
                VoicePipelineLogger.log(VoicePipelineLogger.Event.USER_INTERRUPTION, "Interrupted AI speech", currentSessionId)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error interrupting AI", e)
        } finally {
            if (_voiceState.value.isSpeaking) {
                _voiceState.value = VoiceState.IDLE
            }
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            interruptAi()
        }
    }

    fun speak(
        text: String,
        localeCode: String = "en-US",
        basePitch: Float = 0.92f,
        speechPacing: String = "Natural",
        personalityName: String = "Friendly"
    ) {
        if (_isMuted.value) return
        if (!isTtsInitialized || textToSpeech == null) return

        interruptAi()

        val cleanText = cleanTextForSpokenDelivery(text)
        if (cleanText.isBlank()) return

        try {
            val locale = parseLocale(localeCode)
            val langResult = textToSpeech?.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech?.language = Locale.getDefault()
            }

            val speedRate = when (speechPacing.lowercase()) {
                "slow" -> 0.85f
                "fast" -> 1.15f
                else -> 1.0f
            }
            textToSpeech?.setSpeechRate(speedRate)
            textToSpeech?.setPitch(basePitch)

            val utteranceId = "utterance_${System.currentTimeMillis()}"
            lastQueuedUtteranceId = utteranceId
            _voiceState.value = VoiceState.AI_SPEAKING

            textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(tag, "TTS speak error", e)
            _voiceState.value = VoiceState.IDLE
        }
    }

    private fun parseLocale(localeCode: String): Locale {
        return try {
            val parts = localeCode.split("-", "_")
            if (parts.size >= 2) Locale(parts[0], parts[1]) else Locale(parts[0])
        } catch (_: Exception) {
            Locale.US
        }
    }

    private fun cleanTextForSpokenDelivery(raw: String): String {
        return raw.replace(Regex("[*#_`~>|]"), " ")
            .replace(Regex("\\[.*?\\]\\(.*?\\)"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    // =========================================================================
    // DEVELOPER DIAGNOSTIC & INDEPENDENT TEST MODES (Requirements 133, 134, 135)
    // =========================================================================

    /**
     * Requirement 134: Independent Microphone Test
     * Records 4 seconds independently of STT or AI to verify Android mic permissions,
     * hardware audio routing, buffer arrivals, and amplitude changes.
     */
    fun runIndependentMicrophoneTest(
        durationSeconds: Int = 4,
        onUpdate: (frames: Long, nonZero: Long, rmsDb: Float, amp: Float) -> Unit,
        onComplete: (success: Boolean, summary: String) -> Unit
    ) {
        interruptAi()
        val started = audioRecordHelper.startRecording(scope) { amp, db ->
            onUpdate(
                audioRecordHelper.totalFramesRead,
                audioRecordHelper.nonZeroSamples,
                db,
                amp
            )
        }

        if (!started) {
            onComplete(false, "Microphone could not be opened. Permission: ${audioRecordHelper.hasMicPermission()}")
            return
        }

        scope.launch {
            delay(durationSeconds * 1000L)
            val audioData = audioRecordHelper.stopRecording()
            val frames = audioRecordHelper.totalFramesRead
            val nonZero = audioRecordHelper.nonZeroSamples
            val bytes = audioData?.size ?: 0

            val success = frames > 100 && bytes > 44
            val summary = if (success) {
                "Mic Test PASSED: Received $frames audio frames ($nonZero active samples, ${bytes / 1024} KB WAV)."
            } else {
                "Mic Test FAILED: Only $frames frames received. Check device microphone settings."
            }
            onComplete(success, summary)
        }
    }

    /**
     * Requirement 133: Test without VAD («Raw STT Test»)
     * Disables custom silence cutoff and sends recorded buffer directly to STT.
     */
    fun runRawSttTest(localeCode: String = "en-US") {
        setDisableVad(true)
        startListening(localeCode)
    }

    fun destroy() {
        silenceMonitorJob?.cancel()
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (_: Exception) {}
        audioRecordHelper.release()
    }
}
