package com.example.ui.theme.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Corner radius tokens
 */
data class AppRadius(
    val none: Dp = 0.dp,
    val xs: Dp = 2.dp,
    val sm: Dp = 4.dp,
    val md: Dp = 8.dp,
    val lg: Dp = 12.dp,
    val xl: Dp = 16.dp,
    val xxl: Dp = 24.dp,
    val full: Dp = 999.dp
) {
    // Semantic Shapes mapped directly from radius tokens
    val buttonShape: Shape = RoundedCornerShape(lg)
    val cardShape: Shape = RoundedCornerShape(xl)
    val inputShape: Shape = RoundedCornerShape(lg)
    val chipShape: Shape = RoundedCornerShape(full)
    val voiceButtonShape: Shape = RoundedCornerShape(full)
    val modalShape: Shape = RoundedCornerShape(topStart = xxl, topEnd = xxl)
}
