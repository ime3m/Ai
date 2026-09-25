package com.example.ui.theme.presets

import com.example.ui.theme.*
import com.example.ui.theme.tokens.PrimitiveTokens

object SystemTheme {
    val metadata = ThemeMetadata(
        id = "auto",
        name = "⚙️ Auto",
        description = "Automatically adapt between light and dark based on the device system setting",
        mode = ThemeMode.SYSTEM,
        isDark = false,
        previewPrimary = PrimitiveTokens.neutral500,
        previewAccent = PrimitiveTokens.blue500
    )

    fun resolve(isSystemInDark: Boolean): ThemeDefinition {
        return if (isSystemInDark) {
            MidnightTheme.definition.copy(metadata = metadata.copy(isDark = true))
        } else {
            LightTheme.definition.copy(metadata = metadata.copy(isDark = false))
        }
    }
}
