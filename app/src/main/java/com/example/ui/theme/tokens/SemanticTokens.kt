package com.example.ui.theme.tokens

import androidx.compose.ui.graphics.Color

/**
 * Layer 2 — Semantic Tokens
 * Maps primitives to meaningful UI purposes.
 * Components should consume this layer.
 */

data class BackgroundTokens(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val overlay: Color
)

data class SurfaceTokens(
    val primary: Color,
    val secondary: Color,
    val elevated: Color,
    val card: Color,
    val cardHover: Color,
    val input: Color
)

data class TextTokens(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val muted: Color,
    val disabled: Color,
    val inverse: Color
)

data class AccentTokens(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color
)

data class BorderTokens(
    val default: Color,
    val subtle: Color,
    val strong: Color,
    val focus: Color
)

data class StatusTokens(
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
    val neutral: Color
)

data class OverlayTokens(
    val scrim: Color,
    val modal: Color,
    val pressed: Color
)

/**
 * Complete Semantic Color Palette Structure
 */
data class ColorTokens(
    val background: BackgroundTokens,
    val surface: SurfaceTokens,
    val text: TextTokens,
    val accent: AccentTokens,
    val border: BorderTokens,
    val status: StatusTokens,
    val overlay: OverlayTokens,
    val component: ComponentTokens
)
