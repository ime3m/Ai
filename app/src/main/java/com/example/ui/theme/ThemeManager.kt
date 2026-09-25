package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.ui.theme.presets.*
import com.example.ui.theme.regional.RegionalScriptId
import com.example.ui.theme.regional.RegionalStyleDefinition
import com.example.ui.theme.regional.RegionalStyleRegistry
import com.example.ui.theme.tokens.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Custom theme palette model.
 * Enables user customization of primary, accent, background, text, button, and card colors.
 */
data class CustomThemePalette(
    val primaryColor: Color = PrimitiveTokens.blue600,
    val accentColor: Color = PrimitiveTokens.blue400,
    val backgroundColor: Color = PrimitiveTokens.black950,
    val textColor: Color = PrimitiveTokens.white90,
    val buttonColor: Color = PrimitiveTokens.blue600,
    val cardColor: Color = PrimitiveTokens.neutral900
)

/**
 * Centralized Theme Manager
 * Sole authority responsible for managing active theme state, preset switching,
 * regional styling overlays, custom token overrides, and persistence.
 */
class ThemeManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)

    private val presetRegistry = mapOf(
        MidnightTheme.definition.metadata.id to MidnightTheme.definition,
        KeralaSunsetTheme.definition.metadata.id to KeralaSunsetTheme.definition,
        KeralaGreenTheme.definition.metadata.id to KeralaGreenTheme.definition,
        CoastalBlueTheme.definition.metadata.id to CoastalBlueTheme.definition,
        AiPurpleTheme.definition.metadata.id to AiPurpleTheme.definition,
        LightTheme.definition.metadata.id to LightTheme.definition,
        AmoledTheme.definition.metadata.id to AmoledTheme.definition
    )

    private var activeThemeId: String = prefs.getString(KEY_SELECTED_THEME, "midnight") ?: "midnight"
    private var isRegionalStyleEnabled: Boolean = prefs.getBoolean(KEY_REGIONAL_ENABLED, true)

    private val savedRegionalCode = prefs.getString(KEY_REGIONAL_CODE, "ml") ?: "ml"
    private var currentRegionalStyle: RegionalStyleDefinition = RegionalStyleRegistry.forLanguage(savedRegionalCode)

    private var customPalette: CustomThemePalette = loadCustomPalette()

    private val _currentTheme = MutableStateFlow(computeActiveTheme(false))
    val currentTheme: StateFlow<ThemeDefinition> = _currentTheme.asStateFlow()

    private val _regionalStyleEnabledFlow = MutableStateFlow(isRegionalStyleEnabled)
    val regionalStyleEnabled: StateFlow<Boolean> = _regionalStyleEnabledFlow.asStateFlow()

    private val _currentRegionalStyleFlow = MutableStateFlow(currentRegionalStyle)
    val currentRegionalStyleFlow: StateFlow<RegionalStyleDefinition> = _currentRegionalStyleFlow.asStateFlow()

    private val _customPaletteFlow = MutableStateFlow(customPalette)
    val customPaletteFlow: StateFlow<CustomThemePalette> = _customPaletteFlow.asStateFlow()

    fun getAvailableThemes(): List<ThemeMetadata> {
        val list = mutableListOf<ThemeMetadata>()
        list.addAll(presetRegistry.values.map { it.metadata })
        list.add(SystemTheme.metadata)
        return list
    }

    fun getCurrentThemeId(): String = activeThemeId

    fun setTheme(themeId: String, isSystemInDark: Boolean = true) {
        activeThemeId = themeId
        prefs.edit().putString(KEY_SELECTED_THEME, themeId).apply()
        _currentTheme.value = computeActiveTheme(isSystemInDark)
    }

    fun setRegionalStyle(style: RegionalStyleDefinition, isSystemInDark: Boolean = true) {
        currentRegionalStyle = style
        _currentRegionalStyleFlow.value = style
        prefs.edit().putString(KEY_REGIONAL_CODE, style.scriptId.code).apply()
        _currentTheme.value = computeActiveTheme(isSystemInDark)
    }

    fun setRegionalStyleEnabled(enabled: Boolean, isSystemInDark: Boolean = true) {
        isRegionalStyleEnabled = enabled
        _regionalStyleEnabledFlow.value = enabled
        prefs.edit().putBoolean(KEY_REGIONAL_ENABLED, enabled).apply()
        _currentTheme.value = computeActiveTheme(isSystemInDark)
    }

    fun getCustomPalette(): CustomThemePalette = customPalette

    fun applyCustomPalette(palette: CustomThemePalette, isSystemInDark: Boolean = true) {
        customPalette = palette
        _customPaletteFlow.value = palette
        saveCustomPalette(palette)
        setTheme("custom", isSystemInDark)
    }

    fun resetToDefault(isSystemInDark: Boolean = true) {
        customPalette = CustomThemePalette()
        _customPaletteFlow.value = customPalette
        saveCustomPalette(customPalette)
        setTheme("midnight", isSystemInDark)
    }

    fun applySystemTheme(isSystemInDark: Boolean) {
        if (activeThemeId == "auto" || activeThemeId == "system") {
            _currentTheme.value = computeActiveTheme(isSystemInDark)
        }
    }

    private fun computeActiveTheme(isSystemInDark: Boolean): ThemeDefinition {
        val baseTheme = when (activeThemeId) {
            "auto", "system" -> SystemTheme.resolve(isSystemInDark)
            "custom" -> buildCustomTheme(customPalette)
            else -> presetRegistry[activeThemeId] ?: MidnightTheme.definition
        }

        return if (isRegionalStyleEnabled) {
            baseTheme.withRegionalStyle(currentRegionalStyle)
        } else {
            baseTheme
        }
    }

    private fun buildCustomTheme(palette: CustomThemePalette): ThemeDefinition {
        val buttonTextColor = ThemeSafety.readableTextColor(palette.buttonColor)
        val cardTextColor = ThemeSafety.readableTextColor(palette.cardColor)

        return ThemeDefinition(
            metadata = ThemeMetadata(
                id = "custom",
                name = "🎨 Custom Theme",
                description = "User-customized color scheme with personalized palette",
                mode = ThemeMode.DARK,
                isDark = true,
                previewPrimary = palette.backgroundColor,
                previewAccent = palette.accentColor
            ),
            colors = ColorTokens(
                background = BackgroundTokens(
                    primary = palette.backgroundColor,
                    secondary = palette.cardColor,
                    tertiary = palette.cardColor,
                    overlay = Color.Black.copy(alpha = 0.75f)
                ),
                surface = SurfaceTokens(
                    primary = palette.cardColor,
                    secondary = palette.cardColor.copy(alpha = 0.9f),
                    elevated = palette.cardColor,
                    card = palette.cardColor,
                    cardHover = palette.cardColor,
                    input = palette.cardColor
                ),
                text = TextTokens(
                    primary = palette.textColor,
                    secondary = palette.textColor.copy(alpha = 0.75f),
                    tertiary = palette.textColor.copy(alpha = 0.6f),
                    muted = palette.textColor.copy(alpha = 0.45f),
                    disabled = palette.textColor.copy(alpha = 0.3f),
                    inverse = palette.backgroundColor
                ),
                accent = AccentTokens(
                    primary = palette.primaryColor,
                    secondary = palette.accentColor,
                    tertiary = palette.accentColor
                ),
                border = BorderTokens(
                    default = palette.cardColor.copy(alpha = 0.8f),
                    subtle = palette.backgroundColor.copy(alpha = 0.6f),
                    strong = palette.primaryColor.copy(alpha = 0.5f),
                    focus = palette.primaryColor
                ),
                status = StatusTokens(
                    success = PrimitiveTokens.green500,
                    warning = PrimitiveTokens.amber500,
                    error = PrimitiveTokens.red500,
                    info = palette.primaryColor,
                    neutral = palette.textColor.copy(alpha = 0.5f)
                ),
                overlay = OverlayTokens(
                    scrim = Color.Black.copy(alpha = 0.6f),
                    modal = palette.cardColor,
                    pressed = palette.primaryColor.copy(alpha = 0.15f)
                ),
                component = ComponentTokens(
                    voiceButton = VoiceButtonTokens(
                        background = palette.buttonColor,
                        foreground = buttonTextColor,
                        glow = palette.accentColor.copy(alpha = 0.45f),
                        activeRing = palette.accentColor
                    ),
                    chatBubble = ChatBubbleTokens(
                        user = BubbleTokens(
                            background = palette.buttonColor,
                            text = buttonTextColor,
                            border = palette.primaryColor
                        ),
                        ai = BubbleTokens(
                            background = palette.cardColor,
                            text = cardTextColor,
                            border = palette.cardColor.copy(alpha = 0.8f)
                        )
                    ),
                    waveform = WaveformComponentTokens(
                        active = palette.accentColor,
                        idle = palette.textColor.copy(alpha = 0.4f),
                        processing = palette.primaryColor,
                        speaking = palette.accentColor
                    ),
                    button = ButtonTokens(
                        primary = ButtonTokenPair(palette.buttonColor, buttonTextColor),
                        secondary = ButtonTokenPair(palette.cardColor, cardTextColor)
                    )
                )
            ),
            typography = TypographyTokenFactory.createTypography(),
            spacing = AppSpacing(),
            radius = AppRadius(),
            elevation = AppElevation(),
            border = AppBorder(
                style = BorderStyleTokens(
                    default = palette.cardColor.copy(alpha = 0.8f),
                    subtle = palette.backgroundColor,
                    focus = palette.primaryColor,
                    active = palette.accentColor
                )
            ),
            motion = AppMotion(),
            voice = VoiceTokens(
                idle = VoiceStateTokens(
                    background = palette.cardColor,
                    foreground = cardTextColor,
                    ring = palette.cardColor.copy(alpha = 0.8f)
                ),
                listening = VoiceStateTokens(
                    background = PrimitiveTokens.red600,
                    foreground = PrimitiveTokens.white100,
                    ring = PrimitiveTokens.red400,
                    glow = PrimitiveTokens.red500.copy(alpha = 0.4f)
                ),
                processing = VoiceStateTokens(
                    background = palette.primaryColor,
                    foreground = buttonTextColor,
                    ring = palette.accentColor,
                    glow = palette.accentColor.copy(alpha = 0.4f)
                ),
                speaking = VoiceStateTokens(
                    background = palette.buttonColor,
                    foreground = buttonTextColor,
                    ring = palette.accentColor,
                    glow = palette.accentColor.copy(alpha = 0.45f)
                ),
                waveform = WaveformTokens(
                    idle = palette.textColor.copy(alpha = 0.35f),
                    listening = PrimitiveTokens.red400,
                    processing = palette.primaryColor,
                    speaking = palette.accentColor
                )
            )
        )
    }

    private fun loadCustomPalette(): CustomThemePalette {
        return CustomThemePalette(
            primaryColor = Color(prefs.getInt(KEY_CUSTOM_PRIMARY, PrimitiveTokens.blue600.toArgb())),
            accentColor = Color(prefs.getInt(KEY_CUSTOM_ACCENT, PrimitiveTokens.blue400.toArgb())),
            backgroundColor = Color(prefs.getInt(KEY_CUSTOM_BG, PrimitiveTokens.black950.toArgb())),
            textColor = Color(prefs.getInt(KEY_CUSTOM_TEXT, PrimitiveTokens.white90.toArgb())),
            buttonColor = Color(prefs.getInt(KEY_CUSTOM_BTN, PrimitiveTokens.blue600.toArgb())),
            cardColor = Color(prefs.getInt(KEY_CUSTOM_CARD, PrimitiveTokens.neutral900.toArgb()))
        )
    }

    private fun saveCustomPalette(palette: CustomThemePalette) {
        prefs.edit()
            .putInt(KEY_CUSTOM_PRIMARY, palette.primaryColor.toArgb())
            .putInt(KEY_CUSTOM_ACCENT, palette.accentColor.toArgb())
            .putInt(KEY_CUSTOM_BG, palette.backgroundColor.toArgb())
            .putInt(KEY_CUSTOM_TEXT, palette.textColor.toArgb())
            .putInt(KEY_CUSTOM_BTN, palette.buttonColor.toArgb())
            .putInt(KEY_CUSTOM_CARD, palette.cardColor.toArgb())
            .apply()
    }

    companion object {
        private const val KEY_SELECTED_THEME = "selected_theme_id"
        private const val KEY_REGIONAL_ENABLED = "regional_style_enabled"
        private const val KEY_REGIONAL_CODE = "selected_regional_code"

        private const val KEY_CUSTOM_PRIMARY = "custom_primary_color"
        private const val KEY_CUSTOM_ACCENT = "custom_accent_color"
        private const val KEY_CUSTOM_BG = "custom_bg_color"
        private const val KEY_CUSTOM_TEXT = "custom_text_color"
        private const val KEY_CUSTOM_BTN = "custom_btn_color"
        private const val KEY_CUSTOM_CARD = "custom_card_color"

        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
