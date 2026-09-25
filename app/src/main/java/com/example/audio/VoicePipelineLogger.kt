package com.example.audio

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * Diagnostic logger & session monitor tracking every stage of the voice interaction pipeline
 * (Requirements 126, 128, 147).
 */
object VoicePipelineLogger {
    private const val TAG = "VoicePipelineDebug"

    enum class Event {
        SESSION_STARTED,
        PERMISSION_CHECK,
        PERMISSION_GRANTED,
        PERMISSION_DENIED,
        AUDIO_INITIALIZED,
        MIC_STARTED,
        AUDIO_STREAM_VERIFIED,
        SPEECH_DETECTED,
        PARTIAL_TRANSCRIPT,
        SILENCE_DETECTED,
        FINAL_TRANSCRIPT,
        RECORDING_STOPPED,
        MAX_DURATION_REACHED,
        AI_REQUEST_STARTED,
        AI_RESPONSE_RECEIVED,
        TTS_STARTED,
        TTS_COMPLETED,
        USER_INTERRUPTION,
        RECOGNITION_ERROR,
        AUDIO_ERROR,
        NETWORK_ERROR,
        PERMISSION_ERROR,
        SESSION_ENDED
    }

    private val sessionCounter = AtomicInteger(1)
    private var currentSessionId: String = ""

    private val logHistory = mutableListOf<String>()

    private val _liveDiagnostics = MutableStateFlow(VoiceDiagnostics())
    val liveDiagnostics: StateFlow<VoiceDiagnostics> = _liveDiagnostics.asStateFlow()

    fun createNewSession(): String {
        val datePrefix = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val index = sessionCounter.getAndIncrement()
        val id = "voice_session_${datePrefix}_%03d".format(index)
        currentSessionId = id
        _liveDiagnostics.value = _liveDiagnostics.value.copy(
            sessionId = id,
            audioFrames = 0L,
            nonZeroSamples = 0L,
            speechDetected = false,
            partialTranscript = "",
            finalTranscript = "",
            lastError = null,
            sttConnection = "INITIALIZING"
        )
        log(Event.SESSION_STARTED, "Session: $id")
        return id
    }

    fun getCurrentSessionId(): String = currentSessionId.ifBlank { createNewSession() }

    fun updateDiagnostics(update: (VoiceDiagnostics) -> VoiceDiagnostics) {
        val updated = update(_liveDiagnostics.value)
        _liveDiagnostics.value = updated
    }

    fun log(event: Event, details: String = "", sessionId: String? = null) {
        val sId = sessionId ?: currentSessionId
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        val entry = "[$time][$sId] ${event.name}: $details"
        Log.d(TAG, entry)
        synchronized(logHistory) {
            logHistory.add(entry)
            if (logHistory.size > 250) {
                logHistory.removeAt(0)
            }
        }
    }

    fun getLogs(): List<String> = synchronized(logHistory) { logHistory.toList() }

    fun clear() = synchronized(logHistory) { logHistory.clear() }
}
