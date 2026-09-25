package com.example.ui.theme.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing

data class DurationTokens(
    val instant: Int = 50,
    val fast: Int = 150,
    val normal: Int = 250,
    val slow: Int = 400,
    val emphasis: Int = 600
)

data class EasingTokens(
    val standard: Easing = FastOutSlowInEasing,
    val enter: Easing = LinearOutSlowInEasing,
    val exit: Easing = FastOutLinearInEasing,
    val emphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
)

data class VoiceAnimationTokens(
    val pulseDurationMs: Int = 1200,
    val waveformRefreshRateMs: Long = 60L,
    val processingRotationMs: Int = 1000,
    val speakingRippleMs: Int = 1400
)

data class AppMotion(
    val duration: DurationTokens = DurationTokens(),
    val easing: EasingTokens = EasingTokens(),
    val voice: VoiceAnimationTokens = VoiceAnimationTokens(),
    val reducedMotion: Boolean = false
) {
    fun effectiveDuration(normalDurationMs: Int): Int {
        return if (reducedMotion) 0 else normalDurationMs
    }
}
