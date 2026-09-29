package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

    // Compute whether current background is dark for status bar icon contrast
    val isDark = finalTheme.metadata.isDark || finalTheme.colors.background.primary.luminance() < 0.5f

    // Dynamically adjust status & navigation bar icon colors (light icons on dark background, dark icons on light)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalAppTheme provides finalTheme) {
        MaterialTheme(
            colorScheme = finalTheme.toMaterialColorScheme(),
            typography = finalTheme.toMaterialTypography(),
            content = content
        )
    }
}
