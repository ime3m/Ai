package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AccentPrimaryDark,
    onPrimary = Neutral950,
    primaryContainer = Neutral800,
    onPrimaryContainer = Neutral100,
    secondary = Neutral400,
    onSecondary = Neutral950,
    secondaryContainer = Neutral800,
    onSecondaryContainer = Neutral100,
    tertiary = Neutral400,
    onTertiary = Neutral950,
    background = Neutral950,
    onBackground = Color(0xFFFAFAFA),
    surface = Neutral900,
    onSurface = Color(0xFFFAFAFA),
    surfaceVariant = Neutral800,
    onSurfaceVariant = Neutral400,
    outline = Neutral800,
    outlineVariant = Neutral700
)

private val LightColorScheme = lightColorScheme(
    primary = AccentPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Neutral100,
    onPrimaryContainer = Neutral900,
    secondary = Neutral500,
    onSecondary = Color.White,
    secondaryContainer = Neutral100,
    onSecondaryContainer = Neutral900,
    tertiary = Neutral700,
    onTertiary = Color.White,
    background = Neutral50,
    onBackground = Neutral950,
    surface = Color.White,
    onSurface = Neutral950,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral500,
    outline = Neutral200,
    outlineVariant = Neutral100
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
