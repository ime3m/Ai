package com.example.ui.theme.tokens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class BorderWidthTokens(
    val none: Dp = 0.dp,
    val thin: Dp = 1.dp,
    val thick: Dp = 2.dp
)

data class BorderStyleTokens(
    val default: Color,
    val subtle: Color,
    val focus: Color,
    val active: Color
)

data class AppBorder(
    val width: BorderWidthTokens = BorderWidthTokens(),
    val style: BorderStyleTokens
)
