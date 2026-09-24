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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING
}

class VoiceSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _soundLevel = MutableStateFlow(0f)
    val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

    var onSpeechRecognized: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    init {
        textToSpeech = TextToSpeech(context, this)
        initSpeechRecognizer()
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = VoiceState.LISTENING
                    }

                    override fun onBeginningOfSpeech() {
                        _voiceState.value = VoiceState.LISTENING
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize -2dB..10dB into 0f..1f
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1.0f)
                        _soundLevel.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _voiceState.value = VoiceState.PROCESSING
                        _soundLevel.value = 0f
                    }

                    override fun onError(error: Int) {
                        _voiceState.value = VoiceState.IDLE
                        _soundLevel.value = 0f
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Tap mic to try again."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                            SpeechRecognizer.ERROR_NETWORK -> "Network issue for speech recognition."
                            else -> "Speech recognition paused."
                        }
                        Log.d("VoiceSpeechManager", "Speech error: $error ($errorMsg)")
                        // Don't disturb user for gentle timeouts
                        if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                            onError?.invoke(errorMsg)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        _voiceState.value = VoiceState.IDLE
                        _partialTranscript.value = ""
                        _soundLevel.value = 0f
                        if (text.isNotBlank()) {
                            onSpeechRecognized?.invoke(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        _partialTranscript.value = text
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
                }

                override fun onDone(utteranceId: String?) {
                    _voiceState.value = VoiceState.IDLE
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _voiceState.value = VoiceState.IDLE
                }
            })
        } else {
            Log.e("VoiceSpeechManager", "TTS initialization failed: $status")
        }
    }

    fun startListening(localeCode: String = "en-US") {
        // If AI is speaking, interrupt it immediately!
        interruptAi()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError?.invoke("Speech recognition is not available on this device. You can type your message below.")
            return
        }

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
            _voiceState.value = VoiceState.LISTENING
            _partialTranscript.value = ""
        } catch (e: Exception) {
            Log.e("VoiceSpeechManager", "Failed to start listening", e)
            _voiceState.value = VoiceState.IDLE
            onError?.invoke("Could not start microphone: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("VoiceSpeechManager", "Error stopping listening", e)
        }
        _voiceState.value = VoiceState.IDLE
        _soundLevel.value = 0f
    }

    fun interruptAi() {
        if (_voiceState.value == VoiceState.SPEAKING || textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
        if (_voiceState.value == VoiceState.SPEAKING) {
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun speak(text: String, localeCode: String = "en-US", pitch: Float = 1.0f, speechRate: Float = 1.0f) {
        if (_isMuted.value) return
        if (!isTtsInitialized || textToSpeech == null) return

        interruptAi()

        try {
            val locale = parseLocale(localeCode)
            val result = textToSpeech?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default locale
                textToSpeech?.language = Locale.getDefault()
            }
            textToSpeech?.setPitch(pitch)
            textToSpeech?.setSpeechRate(speechRate)

            val utteranceId = "utterance_${System.currentTimeMillis()}"
            _voiceState.value = VoiceState.SPEAKING
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e("VoiceSpeechManager", "Failed speaking", e)
            _voiceState.value = VoiceState.IDLE
        }
    }

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
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceSpeechManager", "Cleanup error", e)
        }
    }
}
