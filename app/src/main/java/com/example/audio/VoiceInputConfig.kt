package com.example.audio

enum class VoiceInputMode {
    AUTO,   // Automatically detects when the user stops speaking
    MANUAL  // User taps microphone to start, user taps stop to finish
}

enum class TranscriptConfirmationMode {
    AUTO_SEND,           // Automatically process & send the final transcript
    REVIEW_BEFORE_SEND   // Places final transcript in input composer for review & edit before sending
}

data class VoiceInputSettings(
    val mode: VoiceInputMode = VoiceInputMode.AUTO,
    val silenceTimeoutMs: Long = 1800L,        // Generous silence threshold to prevent cutting off regional accents/speech
    val startupGracePeriodMs: Long = 6000L,    // 6.0s STT startup grace period before silence check
    val maxDurationSeconds: Int = 60,          // Configurable maximum duration (default 60s)
    val confirmationMode: TranscriptConfirmationMode = TranscriptConfirmationMode.AUTO_SEND,
    val autoSendDelayMs: Long = 500L,
    val disableVad: Boolean = false            // Raw STT test mode without custom silence cutoff
)

/**
 * Strict internal error states mapped to simple, friendly user-facing messages
 * (Requirements 142 & 143).
 */
sealed class VoiceInputError(
    val message: String,
    val technicalCode: String,
    val canRetry: Boolean = true
) {
    object PermissionDenied : VoiceInputError(
        message = "Microphone permission is required.",
        technicalCode = "MICROPHONE_PERMISSION_DENIED"
    )

    object MicUnavailable : VoiceInputError(
        message = "I can't access your microphone. Check your microphone settings and try again.",
        technicalCode = "MICROPHONE_UNAVAILABLE"
    )

    object AudioCaptureFailed : VoiceInputError(
        message = "I can't access your microphone. Check your microphone settings and try again.",
        technicalCode = "AUDIO_CAPTURE_FAILED"
    )

    object SttConnectionFailed : VoiceInputError(
        message = "Speech service connection failed. Please try again.",
        technicalCode = "STT_CONNECTION_FAILED"
    )

    object SttLanguageUnsupported : VoiceInputError(
        message = "Voice input isn't currently available for this language.",
        technicalCode = "STT_LANGUAGE_UNSUPPORTED"
    )

    object NoSpeech : VoiceInputError(
        message = "I didn't hear any speech. Try speaking again.",
        technicalCode = "STT_NO_SPEECH"
    )

    object NoTranscript : VoiceInputError(
        message = "I couldn't understand that. Please try again.",
        technicalCode = "STT_NO_TRANSCRIPT"
    )

    object Timeout : VoiceInputError(
        message = "I didn't hear any speech. Try speaking again.",
        technicalCode = "STT_TIMEOUT"
    )

    object NetworkError : VoiceInputError(
        message = "Connection problem. Please try again.",
        technicalCode = "NETWORK_ERROR"
    )

    data class General(val details: String) : VoiceInputError(
        message = "I couldn't understand that. Please try again.",
        technicalCode = "UNKNOWN_VOICE_ERROR"
    )

    // Compatibility aliases
    companion object {
        val AudioError get() = AudioCaptureFailed
    }
}

/**
 * Real-time diagnostic statistics for developer visibility and testing (Requirement 128, 129, 147).
 */
data class VoiceDiagnostics(
    val sessionId: String = "",
    val permissionGranted: Boolean = false,
    val audioCaptureRunning: Boolean = false,
    val audioFrames: Long = 0L,
    val nonZeroSamples: Long = 0L,
    val inputLevelDb: Float = -96f,
    val inputLevelNormalized: Float = 0f,
    val speechDetected: Boolean = false,
    val sttConnection: String = "DISCONNECTED", // DISCONNECTED, CONNECTING, CONNECTED, TRANSCRIBING, DONE, ERROR
    val partialTranscript: String = "",
    val finalTranscript: String = "",
    val sampleRate: Int = 16000,
    val channels: Int = 1,
    val captureDurationMs: Long = 0L,
    val lastError: String? = null,
    val vadEnabled: Boolean = true
)
