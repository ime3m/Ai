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
    val silenceTimeoutMs: Long = 1200L,        // 800 - 1500 ms configurable silence detection
    val maxDurationSeconds: Int = 60,          // Configurable maximum duration (default 60s)
    val confirmationMode: TranscriptConfirmationMode = TranscriptConfirmationMode.AUTO_SEND,
    val autoSendDelayMs: Long = 500L
)

sealed class VoiceInputError(val message: String, val canRetry: Boolean = true) {
    object PermissionDenied : VoiceInputError("Microphone permission is required for voice input.")
    object NoSpeech : VoiceInputError("I didn't hear anything. Tap to try again.")
    object NetworkError : VoiceInputError("Connection problem. Your voice wasn't sent.")
    object AudioError : VoiceInputError("Audio recording error. Tap to try again.")
    data class General(val details: String) : VoiceInputError("Couldn't understand that. Tap to try again.")
}
