package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext

/**
 * Root Application Theme Composable.
 * Bridges Centralized Semantic Design Tokens with Material Design 3.
 * All UI components consume tokens via AppTheme (e.g. AppTheme.colors.background.primary)
 * or through standard MaterialTheme values mapped automatically from tokens.
 */
@Composable
fun MyApplicationTheme(
    themeOverride: ThemeDefinition? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    val themeManager = ThemeManager.getInstance(context)
    val activeTheme by themeManager.currentTheme.collectAsState()

    LaunchedEffect(isSystemDark) {
        themeManager.applySystemTheme(isSystemDark)
    }

    val finalTheme = themeOverride ?: activeTheme

    CompositionLocalProvider(LocalAppTheme provides finalTheme) {
        MaterialTheme(
            colorScheme = finalTheme.toMaterialColorScheme(),
            typography = finalTheme.toMaterialTypography(),
            content = content
        )
    }
}
