package com.example.ui.theme.tokens

import androidx.compose.ui.graphics.Color

/**
 * Layer 3 — Component Tokens
 * Defines component-specific tokens mapped from semantic tokens.
 * Prevents individual components from hardcoding or inventing their own colors.
 */

data class VoiceButtonTokens(
    val background: Color,
    val foreground: Color,
    val glow: Color,
    val activeRing: Color
)

data class BubbleTokens(
    val background: Color,
    val text: Color,
    val border: Color = Color.Transparent
)

data class ChatBubbleTokens(
    val user: BubbleTokens,
    val ai: BubbleTokens
)

data class WaveformComponentTokens(
    val active: Color,
    val idle: Color,
    val processing: Color,
    val speaking: Color
)

data class ButtonTokenPair(
    val background: Color,
    val text: Color
)

data class ButtonTokens(
    val primary: ButtonTokenPair,
    val secondary: ButtonTokenPair
)

data class ComponentTokens(
    val voiceButton: VoiceButtonTokens,
    val chatBubble: ChatBubbleTokens,
    val waveform: WaveformComponentTokens,
    val button: ButtonTokens
)
