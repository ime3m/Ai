package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
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
import java.util.regex.Pattern

/**
 * Robust Voice Interaction State Machine:
 * IDLE -> Ready ("Tap to speak")
 * LISTENING -> Audio capture & streaming active ("Listening…")
 * SPEECH_DETECTED -> Voice Activity Detected, live words appearing
 * PROCESSING -> End-of-speech detected, transcript finalized, AI generating
 * SPEAKING -> AI answering with regional voice
 * ERROR -> Recoverable error with clear Try Again action
 */
enum class VoiceState {
    IDLE,
    LISTENING,
    SPEECH_DETECTED,
    PROCESSING,
    SPEAKING,
    ERROR;

    val isRecording: Boolean get() = this == LISTENING || this == SPEECH_DETECTED
}

class VoiceSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

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

    // VAD & Turn Management State
    private var recordingStartTimeMs: Long = 0L
    private var lastSpeechDetectedTimeMs: Long = 0L
    private var hasDetectedSpeech: Boolean = false
    private var lastRecognizedText: String = ""
    private val isFinalizing = AtomicBoolean(false)

    // Track active speaking utterance IDs for seamless multi-chunk progress
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

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        VoicePipelineLogger.log(VoicePipelineLogger.Event.MIC_STARTED, "Ready for speech")
                        if (_voiceState.value != VoiceState.SPEECH_DETECTED) {
                            _voiceState.value = VoiceState.LISTENING
                        }
                    }

                    override fun onBeginningOfSpeech() {
                        VoicePipelineLogger.log(VoicePipelineLogger.Event.SPEECH_DETECTED, "Beginning of speech")
                        // Barge-in: immediate stop of AI speech if running
                        interruptAi()
                        hasDetectedSpeech = true
                        lastSpeechDetectedTimeMs = System.currentTimeMillis()
                        _voiceState.value = VoiceState.SPEECH_DETECTED
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.0f, 1.0f)
                        _soundLevel.value = normalized

                        // Real-time Voice Activity Detection from audio power level
                        if (normalized > 0.18f && _voiceState.value.isRecording) {
                            hasDetectedSpeech = true
                            lastSpeechDetectedTimeMs = System.currentTimeMillis()
                            if (_voiceState.value == VoiceState.LISTENING) {
                                _voiceState.value = VoiceState.SPEECH_DETECTED
                            }
                        }
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        VoicePipelineLogger.log(VoicePipelineLogger.Event.RECORDING_STOPPED, "Recognizer onEndOfSpeech")
                        _soundLevel.value = 0f
                        finalizeTurn(reason = "RECOGNIZER_END_OF_SPEECH")
                    }

                    override fun onError(error: Int) {
                        VoicePipelineLogger.log(VoicePipelineLogger.Event.RECOGNITION_ERROR, "Recognizer error code: $error")
                        _soundLevel.value = 0f

                        // CRITICAL RESILIENCE: If we already captured a partial transcript before the recognizer
                        // triggered a timeout or no_match, do NOT discard the user's speech!
                        if (lastRecognizedText.isNotBlank() || _partialTranscript.value.isNotBlank()) {
                            finalizeTurn(reason = "ERROR_RECOVERY_WITH_TRANSCRIPT")
                            return
                        }

                        silenceMonitorJob?.cancel()

                        val inputError = when (error) {
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                                VoicePipelineLogger.log(VoicePipelineLogger.Event.PERMISSION_ERROR, "Missing mic permission")
                                VoiceInputError.PermissionDenied
                            }
                            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                                VoiceInputError.NoSpeech
                            }
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                                VoicePipelineLogger.log(VoicePipelineLogger.Event.NETWORK_ERROR, "Network timeout/error")
                                VoiceInputError.NetworkError
                            }
                            SpeechRecognizer.ERROR_AUDIO -> {
                                VoiceInputError.AudioError
                            }
                            else -> {
                                VoiceInputError.General("Recognition error ($error)")
                            }
                        }

                        _voiceState.value = VoiceState.ERROR
                        _voiceError.value = inputError
                        _partialTranscript.value = ""
                        onError?.invoke(inputError.message)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        VoicePipelineLogger.log(VoicePipelineLogger.Event.FINAL_TRANSCRIPT, "Results received: '$text'")
                        if (text.isNotBlank()) {
                            lastRecognizedText = text
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
                            VoicePipelineLogger.log(VoicePipelineLogger.Event.PARTIAL_TRANSCRIPT, text)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _voiceState.value = VoiceState.SPEAKING
                    VoicePipelineLogger.log(VoicePipelineLogger.Event.TTS_STARTED, utteranceId ?: "")
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId == lastQueuedUtteranceId) {
                        _voiceState.value = VoiceState.IDLE
                        VoicePipelineLogger.log(VoicePipelineLogger.Event.TTS_COMPLETED, utteranceId ?: "")
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (utteranceId == lastQueuedUtteranceId) {
                        _voiceState.value = VoiceState.IDLE
                        VoicePipelineLogger.log(VoicePipelineLogger.Event.RECOGNITION_ERROR, "TTS error on $utteranceId")
                    }
                }
            })
        } else {
            Log.e("VoiceSpeechManager", "TTS initialization failed: $status")
        }
    }

    /**
     * Starts listening with regional speech recognition locale and configurable VAD.
     */
    fun startListening(localeCode: String = "en-US") {
        // Barge-in: flush any AI speech immediately
        interruptAi()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            val err = VoiceInputError.General("Speech recognition is not available on this device.")
            _voiceState.value = VoiceState.ERROR
            _voiceError.value = err
            onError?.invoke(err.message)
            return
        }

        try {
            isFinalizing.set(false)
            _voiceError.value = null
            _partialTranscript.value = ""
            lastRecognizedText = ""
            hasDetectedSpeech = false
            recordingStartTimeMs = System.currentTimeMillis()
            lastSpeechDetectedTimeMs = 0L

            VoicePipelineLogger.log(VoicePipelineLogger.Event.MIC_STARTED, "Locale: $localeCode")

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeCode)
                // Support multi-language hints for natural code-switching (e.g. Malayalam + English)
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf(localeCode, "en-IN", "en-US"))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                // Hints for platform recognizer silence detection
                val timeout = _voiceSettings.value.silenceTimeoutMs
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, timeout)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, timeout)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)
            }

            speechRecognizer?.startListening(intent)
            _voiceState.value = VoiceState.LISTENING

            startSilenceAndSafetyWatcher()
        } catch (e: Exception) {
            Log.e("VoiceSpeechManager", "Failed to start listening", e)
            _voiceState.value = VoiceState.ERROR
            val err = VoiceInputError.General("Could not start microphone: ${e.message}")
            _voiceError.value = err
            onError?.invoke(err.message)
        }
    }

    /**
     * Active background watcher that enforces:
     * 1. Configurable silence detection (Voice Activity Detection): automatically ends turn after sustained silence.
     * 2. Maximum recording safety limit (60s default): prevents infinite background recordings.
     */
    private fun startSilenceAndSafetyWatcher() {
        silenceMonitorJob?.cancel()
        silenceMonitorJob = scope.launch {
            while (isActive && _voiceState.value.isRecording) {
                delay(120)
                val now = System.currentTimeMillis()
                val currentSettings = _voiceSettings.value

                // 1. Maximum Duration Safety Limit
                val elapsed = now - recordingStartTimeMs
                if (elapsed >= currentSettings.maxDurationSeconds * 1000L) {
                    VoicePipelineLogger.log(
                        VoicePipelineLogger.Event.MAX_DURATION_REACHED,
                        "Reached ${currentSettings.maxDurationSeconds}s safety limit"
                    )
                    finalizeTurn(reason = "MAX_SAFETY_LIMIT_REACHED")
                    break
                }

                // 2. Silence Detection in AUTO mode
                if (currentSettings.mode == VoiceInputMode.AUTO && hasDetectedSpeech && lastSpeechDetectedTimeMs > 0L) {
                    val silenceDuration = now - lastSpeechDetectedTimeMs
                    if (silenceDuration >= currentSettings.silenceTimeoutMs) {
                        VoicePipelineLogger.log(
                            VoicePipelineLogger.Event.SILENCE_DETECTED,
                            "Silence threshold met: ${silenceDuration}ms >= ${currentSettings.silenceTimeoutMs}ms"
                        )
                        finalizeTurn(reason = "SUSTAINED_SILENCE_DETECTED")
                        break
                    }
                }
            }
        }
    }

    /**
     * Stops listening manually (e.g. User taps Stop in MANUAL mode or interrupts).
     */
    fun stopListening() {
        VoicePipelineLogger.log(VoicePipelineLogger.Event.RECORDING_STOPPED, "Manual stop invoked")
        finalizeTurn(reason = "USER_MANUAL_STOP")
    }

    /**
     * Finalizes the current utterance:
     * Extracts best transcript, stops recognizer, cancels timers, and delivers result.
     */
    private fun finalizeTurn(reason: String) {
        if (!isFinalizing.compareAndSet(false, true)) {
            return
        }

        silenceMonitorJob?.cancel()

        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.d("VoiceSpeechManager", "Error stopping recognizer", e)
        }

        _soundLevel.value = 0f

        val finalText = lastRecognizedText.ifBlank { _partialTranscript.value }.trim()

        if (finalText.isNotBlank()) {
            VoicePipelineLogger.log(VoicePipelineLogger.Event.FINAL_TRANSCRIPT, "Finalized ($reason): '$finalText'")
            _voiceState.value = VoiceState.PROCESSING
            _lastFinalTranscript.value = finalText
            _partialTranscript.value = ""

            // Deliver finalized transcript to consumer
            onSpeechRecognized?.invoke(finalText)
        } else {
            // No speech was detected before end of turn
            VoicePipelineLogger.log(VoicePipelineLogger.Event.RECOGNITION_ERROR, "No speech detected on finalize ($reason)")
            val err = VoiceInputError.NoSpeech
            _voiceState.value = VoiceState.ERROR
            _voiceError.value = err
            _partialTranscript.value = ""
            onError?.invoke(err.message)
        }
    }

    /**
     * Clears error state and restarts listening.
     */
    fun retryListening(localeCode: String = "en-US") {
        _voiceError.value = null
        _voiceState.value = VoiceState.IDLE
        startListening(localeCode)
    }

    fun interruptAi() {
        try {
            lastQueuedUtteranceId = null
            if (_voiceState.value == VoiceState.SPEAKING || textToSpeech?.isSpeaking == true) {
                textToSpeech?.stop()
                VoicePipelineLogger.log(VoicePipelineLogger.Event.USER_INTERRUPTION, "Interrupted AI speech")
            }
        } catch (e: Exception) {
            Log.e("VoiceSpeechManager", "Error interrupting AI", e)
        } finally {
            if (_voiceState.value == VoiceState.SPEAKING) {
                _voiceState.value = VoiceState.IDLE
            }
        }
    }

    /**
     * Speaks text using a natural adult human conversational voice profile.
     */
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

            selectBestAdultVoice(locale)

            val calibratedPitch = when (personalityName.uppercase()) {
                "CALM", "PROFESSIONAL" -> 0.89f
                "FUNNY", "ENERGETIC" -> 0.95f
                "TEACHER" -> 0.91f
                "STORYTELLER" -> 0.90f
                "LOCAL_FRIEND" -> 0.92f
                else -> basePitch.coerceIn(0.88f, 0.95f)
            }

            val chunks = splitIntoConversationalChunks(cleanText)
            if (chunks.isEmpty()) return

            val sessionTimestamp = System.currentTimeMillis()
            val totalChunks = chunks.size
            lastQueuedUtteranceId = "utt_${sessionTimestamp}_${totalChunks - 1}"

            _voiceState.value = VoiceState.SPEAKING

            chunks.forEachIndexed { index, chunk ->
                val utteranceId = "utt_${sessionTimestamp}_$index"
                val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD

                val dynamicRate = calculateDynamicPacing(chunk, speechPacing, personalityName)
                val dynamicPitch = calculateDynamicPitch(chunk, calibratedPitch)

                textToSpeech?.setPitch(dynamicPitch)
                textToSpeech?.setSpeechRate(dynamicRate)
                textToSpeech?.speak(chunk.text, queueMode, null, utteranceId)

                if (index < totalChunks - 1 && chunk.pauseAfterMs > 0) {
                    val pauseId = "pause_${sessionTimestamp}_$index"
                    textToSpeech?.playSilentUtterance(chunk.pauseAfterMs.toLong(), TextToSpeech.QUEUE_ADD, pauseId)
                }
            }
        } catch (e: Exception) {
            Log.e("VoiceSpeechManager", "Failed speaking", e)
            _voiceState.value = VoiceState.IDLE
        }
    }

    private fun selectBestAdultVoice(targetLocale: Locale) {
        try {
            val allVoices = textToSpeech?.voices ?: return
            val matching = allVoices.filter { voice ->
                val notInstalled = voice.features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) == true
                voice.locale.language.equals(targetLocale.language, ignoreCase = true) && !notInstalled
            }

            if (matching.isNotEmpty()) {
                val best = matching.sortedWith(
                    compareByDescending<Voice> { it.quality }
                        .thenByDescending { !it.isNetworkConnectionRequired }
                        .thenBy { voice ->
                            val name = voice.name.lowercase()
                            if (name.contains("child") || name.contains("kid")) 1 else 0
                        }
                ).firstOrNull()

                if (best != null) {
                    textToSpeech?.voice = best
                }
            }
        } catch (e: Exception) {
            Log.d("VoiceSpeechManager", "Voice selection fallback", e)
        }
    }

    private fun calculateDynamicPacing(
        chunk: SpokenChunk,
        pacingSetting: String,
        personalityName: String
    ): Float {
        val base = when (pacingSetting.lowercase()) {
            "slow" -> 0.85f
            "fast" -> 1.06f
            "normal" -> 0.94f
            else -> 0.93f
        }

        if (pacingSetting.equals("Natural", ignoreCase = true)) {
            return when {
                chunk.isBriefAcknowledgement -> base + 0.05f
                chunk.isQuestion || chunk.text.length > 60 -> (base - 0.04f).coerceAtLeast(0.86f)
                personalityName.equals("STORYTELLER", true) || personalityName.equals("CALM", true) -> base - 0.03f
                personalityName.equals("ENERGETIC", true) || personalityName.equals("FUNNY", true) -> base + 0.04f
                else -> base
            }
        }
        return base
    }

    private fun calculateDynamicPitch(chunk: SpokenChunk, baseAdultPitch: Float): Float {
        return when {
            chunk.isQuestion -> (baseAdultPitch + 0.03f).coerceAtMost(0.97f)
            chunk.isEnding -> (baseAdultPitch - 0.02f).coerceAtLeast(0.87f)
            else -> baseAdultPitch
        }
    }

    private fun cleanTextForSpokenDelivery(raw: String): String {
        return raw
            .replace(Regex("[\\p{So}\\p{Cn}]"), "")
            .replace(Regex("[*#_~`>]"), "")
            .replace(Regex("\\[.*?\\]|\\(.*?\\)"), "")
            .replace(Regex("\\?{2,}"), "?")
            .replace(Regex("!{2,}"), "!")
            .replace(Regex("\\.{4,}"), "...")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun splitIntoConversationalChunks(text: String): List<SpokenChunk> {
        val list = mutableListOf<SpokenChunk>()
        val pattern = Pattern.compile("(?<=[.!?…;])\\s+|(?<=[,])\\s+(?=[A-Z|\\p{IsMalayalam}|\\p{IsArabic}])")
        val tokens = text.split(pattern).filter { it.isNotBlank() }

        if (tokens.isEmpty()) {
            return listOf(SpokenChunk(text, isQuestion = text.endsWith("?"), pauseAfterMs = 0, isEnding = true))
        }

        tokens.forEachIndexed { i, token ->
            val trimmed = token.trim()
            val isLast = i == tokens.lastIndex
            val isQ = trimmed.endsWith("?")

            val pauseMs = when {
                isLast -> 0
                trimmed.endsWith("...") || trimmed.endsWith("…") -> 280
                trimmed.endsWith("?") || trimmed.endsWith("!") -> 220
                trimmed.endsWith(".") -> 180
                trimmed.endsWith(",") -> 110
                else -> 150
            }

            val isAck = trimmed.length < 15 && (
                    trimmed.startsWith("Hey", true) || trimmed.startsWith("Yeah", true) ||
                            trimmed.startsWith("ശരി", true) || trimmed.startsWith("അതെ", true) ||
                            trimmed.startsWith("പിന്നെന്താ", true) || trimmed.startsWith("ഹലോ", true)
                    )

            list.add(
                SpokenChunk(
                    text = trimmed,
                    isQuestion = isQ,
                    pauseAfterMs = pauseMs,
                    isBriefAcknowledgement = isAck,
                    isEnding = isLast
                )
            )
        }
        return list
    }

    private data class SpokenChunk(
        val text: String,
        val isQuestion: Boolean = false,
        val pauseAfterMs: Int = 180,
        val isBriefAcknowledgement: Boolean = false,
        val isEnding: Boolean = false
    )

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            interruptAi()
        }
    }

    private fun parseLocale(code: String): Locale {
        return try {
            Locale.forLanguageTag(code.replace('_', '-'))
        } catch (_: Exception) {
            Locale.ENGLISH
        }
    }

    fun destroy() {
        try {
            silenceMonitorJob?.cancel()
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceSpeechManager", "Cleanup error", e)
        }
    }
}
