package com.example.ui.theme.tokens

import androidx.compose.ui.graphics.Color

/**
 * Layer 1 — Primitive Tokens
 * Raw values that define the visual palette.
 * These should generally NOT be used directly by UI components;
 * components consume Layer 2 (Semantic) or Layer 3 (Component) tokens.
 */
object PrimitiveTokens {
    // Blacks & Pitch Dark
    val blackPure = Color(0xFF000000)
    val black950 = Color(0xFF070709)
    val black900 = Color(0xFF0F0F12)
    val black850 = Color(0xFF141418)

    // Whites
    val white100 = Color(0xFFFFFFFF)
    val white90 = Color(0xFFFAFAFA)
    val white80 = Color(0xFFF4F4F5)
    val white70 = Color(0xFFE4E4E7)

    // Neutrals / Zinc
    val neutral50 = Color(0xFFFAFAFA)
    val neutral100 = Color(0xFFF4F4F5)
    val neutral200 = Color(0xFFE4E4E7)
    val neutral300 = Color(0xFFD4D4D8)
    val neutral400 = Color(0xFFA1A1AA)
    val neutral500 = Color(0xFF71717A)
    val neutral600 = Color(0xFF52525B)
    val neutral700 = Color(0xFF3F3F46)
    val neutral800 = Color(0xFF27272A)
    val neutral850 = Color(0xFF1F1F23)
    val neutral900 = Color(0xFF18181B)
    val neutral950 = Color(0xFF09090B)

    // Blues
    val blue300 = Color(0xFF93C5FD)
    val blue400 = Color(0xFF60A5FA)
    val blue500 = Color(0xFF3B82F6)
    val blue600 = Color(0xFF2563EB)
    val blue700 = Color(0xFF1D4ED8)
    val blue800 = Color(0xFF1E40AF)
    val blue900 = Color(0xFF1E3A8A)

    // Greens / Emeralds (Kerala Backwaters & Tropical Palms)
    val green300 = Color(0xFF86EFAC)
    val green400 = Color(0xFF4ADE80)
    val green500 = Color(0xFF22C55E)
    val green600 = Color(0xFF16A34A)
    val green700 = Color(0xFF15803D)
    val green800 = Color(0xFF166534)
    val green900 = Color(0xFF14532D)

    // Purples / Violets (Digital AI & Future Voice)
    val purple200 = Color(0xFFE9D5FF)
    val purple300 = Color(0xFFD8B4FE)
    val purple400 = Color(0xFFC084FC)
    val purple500 = Color(0xFFA855F7)
    val purple600 = Color(0xFF9333EA)
    val purple700 = Color(0xFF7E22CE)
    val purple800 = Color(0xFF6B21A8)
    val purple900 = Color(0xFF581C87)

    // Oranges / Saffrons / Terracotta (Kerala Sunset & Temple Heritage)
    val orange300 = Color(0xFFFDBA74)
    val orange400 = Color(0xFFFB923C)
    val orange500 = Color(0xFFF97316)
    val orange600 = Color(0xFFEA580C)
    val orange700 = Color(0xFFC2410C)
    val orange800 = Color(0xFF9A3412)

    // Ambers / Golds (Kasavu Gold & Warm Brass)
    val amber300 = Color(0xFFFCD34D)
    val amber400 = Color(0xFFFBBF24)
    val amber500 = Color(0xFFF59E0B)
    val amber600 = Color(0xFFD97706)

    // Teals (Arabian Sea Coastal Waves)
    val teal300 = Color(0xFF5EEAD4)
    val teal400 = Color(0xFF2DD4BF)
    val teal500 = Color(0xFF14B8A6)
    val teal600 = Color(0xFF0D9488)
    val teal700 = Color(0xFF0F766E)

    // Reds / Status Error
    val red400 = Color(0xFFF87171)
    val red500 = Color(0xFFEF4444)
    val red600 = Color(0xFFDC2626)

    // Overlays / Alpha Primitives
    val blackAlpha10 = Color(0x1A000000)
    val blackAlpha30 = Color(0x4D000000)
    val blackAlpha50 = Color(0x80000000)
    val blackAlpha75 = Color(0xBF000000)
    val blackAlpha90 = Color(0xE6000000)

    val whiteAlpha10 = Color(0x1AFFFFFF)
    val whiteAlpha20 = Color(0x33FFFFFF)
    val whiteAlpha35 = Color(0x59FFFFFF)
    val whiteAlpha70 = Color(0xB3FFFFFF)
}
