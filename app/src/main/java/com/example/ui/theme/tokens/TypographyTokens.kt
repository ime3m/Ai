package com.example.ui.theme.tokens

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Typography Token Definition
 */
data class FontFamilies(
    val primary: FontFamily = FontFamily.Default,
    val display: FontFamily = FontFamily.Default,
    val mono: FontFamily = FontFamily.Monospace
)

data class DisplayTypography(
    val large: TextStyle,
    val medium: TextStyle,
    val small: TextStyle
)

data class HeadingTypography(
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle
)

data class BodyTypography(
    val large: TextStyle,
    val medium: TextStyle,
    val small: TextStyle
)

data class LabelTypography(
    val large: TextStyle,
    val medium: TextStyle,
    val small: TextStyle
)

data class AppTypography(
    val fontFamily: FontFamilies = FontFamilies(),
    val display: DisplayTypography,
    val heading: HeadingTypography,
    val body: BodyTypography,
    val label: LabelTypography,
    val caption: TextStyle
) {
    val displayLarge: TextStyle get() = display.large
    val displayMedium: TextStyle get() = display.medium
    val displaySmall: TextStyle get() = display.small
    val headlineLarge: TextStyle get() = heading.h1
    val headlineMedium: TextStyle get() = heading.h2
    val headlineSmall: TextStyle get() = heading.h3
    val titleLarge: TextStyle get() = heading.h2
    val titleMedium: TextStyle get() = heading.h3
    val titleSmall: TextStyle get() = label.large
    val bodyLarge: TextStyle get() = body.large
    val bodyMedium: TextStyle get() = body.medium
    val bodySmall: TextStyle get() = body.small
    val labelLarge: TextStyle get() = label.large
    val labelMedium: TextStyle get() = label.medium
    val labelSmall: TextStyle get() = label.small
}

/**
 * Factory for creating typography tokens with regional font script support.
 * Ensures consistent line height and baseline adjustments for Indic scripts
 * (Malayalam, Tamil, Kannada, Telugu, Bengali, Punjabi, Hindi).
 */
object TypographyTokenFactory {

    fun createTypography(
        fontFamily: FontFamilies = FontFamilies(),
        scriptLineHeightMultiplier: Float = 1.0f
    ): AppTypography {
        val baseMultiplier = scriptLineHeightMultiplier

        fun spAdjust(sp: Float): TextUnit = (sp * baseMultiplier).sp

        return AppTypography(
            fontFamily = fontFamily,
            display = DisplayTypography(
                large = TextStyle(
                    fontFamily = fontFamily.display,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    lineHeight = spAdjust(40f),
                    letterSpacing = (-0.5).sp
                ),
                medium = TextStyle(
                    fontFamily = fontFamily.display,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    lineHeight = spAdjust(36f),
                    letterSpacing = (-0.3).sp
                ),
                small = TextStyle(
                    fontFamily = fontFamily.display,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    lineHeight = spAdjust(32f),
                    letterSpacing = (-0.2).sp
                )
            ),
            heading = HeadingTypography(
                h1 = TextStyle(
                    fontFamily = fontFamily.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = spAdjust(28f),
                    letterSpacing = (-0.2).sp
                ),
                h2 = TextStyle(
                    fontFamily = fontFamily.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    lineHeight = spAdjust(24f),
                    letterSpacing = (-0.1).sp
                ),
                h3 = TextStyle(
                    fontFamily = fontFamily.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = spAdjust(22f)
                )
            ),
            body = BodyTypography(
                large = TextStyle(
                    fontFamily = fontFamily.primary,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = spAdjust(24f),
                    letterSpacing = 0.1.sp
                ),
                medium = TextStyle(
                    fontFamily = fontFamily.primary,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = spAdjust(21f),
                    letterSpacing = 0.1.sp
                ),
                small = TextStyle(
                    fontFamily = fontFamily.primary,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = spAdjust(17f),
                    letterSpacing = 0.2.sp
                )
            ),
            label = LabelTypography(
                large = TextStyle(
                    fontFamily = fontFamily.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = spAdjust(20f),
                    letterSpacing = 0.1.sp
                ),
                medium = TextStyle(
                    fontFamily = fontFamily.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = spAdjust(16f),
                    letterSpacing = 0.3.sp
                ),
                small = TextStyle(
                    fontFamily = fontFamily.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    lineHeight = spAdjust(14f),
                    letterSpacing = 0.4.sp
                )
            ),
            caption = TextStyle(
                fontFamily = fontFamily.primary,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                lineHeight = spAdjust(13f),
                letterSpacing = 0.4.sp
            )
        )
    }
}
