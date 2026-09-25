package com.example.ui.theme.regional

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.tokens.AccentTokens
import com.example.ui.theme.tokens.PrimitiveTokens

enum class RegionalScriptId(val code: String, val displayName: String, val nativeName: String) {
    MALAYALAM("ml", "Malayalam / Kerala", "മലയാളം"),
    TAMIL("ta", "Tamil", "தமிழ்"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ"),
    TELUGU("te", "Telugu", "తెలుగు"),
    BENGALI("bn", "Bengali", "বাংলা"),
    PUNJABI("pa", "Punjabi", "ਪੰਜਾਬੀ"),
    HINDI("hi", "Hindi", "हिन्दी"),
    ENGLISH("en", "English", "English")
}

data class RegionalStyleDefinition(
    val scriptId: RegionalScriptId,
    val name: String,
    val scriptLineHeightMultiplier: Float = 1.0f,
    val regionalAccentOverride: AccentTokens? = null,
    val regionalGlowTint: Color = Color.Transparent,
    val regionalBadgeColor: Color = PrimitiveTokens.neutral800
)

object RegionalStyleRegistry {
    val malayalam = RegionalStyleDefinition(
        scriptId = RegionalScriptId.MALAYALAM,
        name = "Malayalam / Kerala",
        scriptLineHeightMultiplier = 1.15f,
        regionalAccentOverride = AccentTokens(
            primary = PrimitiveTokens.amber500, // Kasavu Gold
            secondary = PrimitiveTokens.green500, // Lush Palm Green
            tertiary = PrimitiveTokens.orange600 // Terracotta Red
        ),
        regionalGlowTint = PrimitiveTokens.amber500.copy(alpha = 0.25f),
        regionalBadgeColor = PrimitiveTokens.amber600
    )

    val tamil = RegionalStyleDefinition(
        scriptId = RegionalScriptId.TAMIL,
        name = "Tamil",
        scriptLineHeightMultiplier = 1.12f,
        regionalAccentOverride = AccentTokens(
            primary = PrimitiveTokens.orange500,
            secondary = PrimitiveTokens.amber500,
            tertiary = PrimitiveTokens.red500
        ),
        regionalGlowTint = PrimitiveTokens.orange500.copy(alpha = 0.25f)
    )

    val kannada = RegionalStyleDefinition(
        scriptId = RegionalScriptId.KANNADA,
        name = "Kannada",
        scriptLineHeightMultiplier = 1.14f,
        regionalAccentOverride = AccentTokens(
            primary = PrimitiveTokens.amber400,
            secondary = PrimitiveTokens.red600,
            tertiary = PrimitiveTokens.blue500
        )
    )

    val telugu = RegionalStyleDefinition(
        scriptId = RegionalScriptId.TELUGU,
        name = "Telugu",
        scriptLineHeightMultiplier = 1.14f,
        regionalAccentOverride = AccentTokens(
            primary = PrimitiveTokens.teal500,
            secondary = PrimitiveTokens.amber500,
            tertiary = PrimitiveTokens.orange500
        )
    )

    val bengali = RegionalStyleDefinition(
        scriptId = RegionalScriptId.BENGALI,
        name = "Bengali",
        scriptLineHeightMultiplier = 1.12f,
        regionalAccentOverride = AccentTokens(
            primary = PrimitiveTokens.red500,
            secondary = PrimitiveTokens.amber400,
            tertiary = PrimitiveTokens.neutral200
        )
    )

    val punjabi = RegionalStyleDefinition(
        scriptId = RegionalScriptId.PUNJABI,
        name = "Punjabi",
        scriptLineHeightMultiplier = 1.12f,
        regionalAccentOverride = AccentTokens(
            primary = PrimitiveTokens.orange600,
            secondary = PrimitiveTokens.green600,
            tertiary = PrimitiveTokens.blue600
        )
    )

    val hindi = RegionalStyleDefinition(
        scriptId = RegionalScriptId.HINDI,
        name = "Hindi",
        scriptLineHeightMultiplier = 1.10f,
        regionalAccentOverride = AccentTokens(
            primary = PrimitiveTokens.orange500,
            secondary = PrimitiveTokens.blue600,
            tertiary = PrimitiveTokens.green600
        )
    )

    val english = RegionalStyleDefinition(
        scriptId = RegionalScriptId.ENGLISH,
        name = "English",
        scriptLineHeightMultiplier = 1.0f
    )

    val global = english

    val allRegionalStyles: List<RegionalStyleDefinition> = listOf(
        malayalam,
        tamil,
        kannada,
        telugu,
        bengali,
        punjabi,
        hindi,
        english
    )

    fun forLanguage(lang: String): RegionalStyleDefinition {
        val l = lang.lowercase()
        return when {
            l.contains("malayalam") || l.startsWith("ml") -> malayalam
            l.contains("tamil") || l.startsWith("ta") -> tamil
            l.contains("kannada") || l.startsWith("kn") -> kannada
            l.contains("telugu") || l.startsWith("te") -> telugu
            l.contains("bengali") || l.startsWith("bn") -> bengali
            l.contains("punjabi") || l.startsWith("pa") -> punjabi
            l.contains("hindi") || l.startsWith("hi") -> hindi
            else -> english
        }
    }
}
