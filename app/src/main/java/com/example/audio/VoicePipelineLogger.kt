package com.example.audio

import android.util.Log

/**
 * Diagnostic logger for tracking each stage of the voice interaction pipeline:
 * Audio capture -> VAD -> Speech-to-Text -> Silence Detection -> Final transcript -> AI -> TTS.
 */
object VoicePipelineLogger {
    private const val TAG = "VoicePipelineDebug"

    enum class Event {
        MIC_STARTED,
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
        NETWORK_ERROR,
        PERMISSION_ERROR
    }

    private val logHistory = mutableListOf<String>()

    fun log(event: Event, details: String = "") {
        val entry = "[${System.currentTimeMillis()}] ${event.name}: $details"
        Log.d(TAG, entry)
        synchronized(logHistory) {
            logHistory.add(entry)
            if (logHistory.size > 200) {
                logHistory.removeAt(0)
            }
        }
    }

    fun getLogs(): List<String> = synchronized(logHistory) { logHistory.toList() }

    fun clear() = synchronized(logHistory) { logHistory.clear() }
}
