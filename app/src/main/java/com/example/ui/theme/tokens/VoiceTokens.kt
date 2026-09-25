package com.example.ui.theme.tokens

import androidx.compose.ui.graphics.Color

/**
 * Voice Interaction Tokens
 * Dedicated token group for voice AI states and audio waveform rendering.
 */

data class VoiceStateTokens(
    val background: Color,
    val foreground: Color,
    val ring: Color,
    val glow: Color = Color.Transparent
)

data class WaveformTokens(
    val idle: Color,
    val listening: Color,
    val processing: Color,
    val speaking: Color
)

data class VoiceTokens(
    val idle: VoiceStateTokens,
    val listening: VoiceStateTokens,
    val processing: VoiceStateTokens,
    val speaking: VoiceStateTokens,
    val waveform: WaveformTokens
)
