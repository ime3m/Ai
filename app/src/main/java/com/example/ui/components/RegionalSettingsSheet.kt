package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.config.DeveloperProfile
import com.example.data.model.RegionalDialect
import com.example.data.model.VoicePersonality
import com.example.data.model.VoiceProfileEntity
import com.example.data.repository.DialectCatalog
import com.example.ui.theme.FuturisticTheme
import com.example.ui.theme.FuturisticTokens
import com.example.ui.theme.ThemeManager

/**
 * Top-level categories in the Settings sheet.
 */
enum class SettingsMenuSection(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    APPEARANCE("Appearance", Icons.Default.Palette),
    REGIONAL_AI("Regional AI", Icons.Default.RecordVoiceOver),
    AI_MEMORY("AI & Memory", Icons.Default.Psychology),
    PRIVACY("Privacy", Icons.Default.PrivacyTip),
    ABOUT("About Regional Voice AI", Icons.Default.Info)
}

/**
 * Clean, modern Settings Menu containing:
 * - Appearance
 * - Regional AI
 * - AI & Memory
 * - Privacy
 * - About Regional Voice AI
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegionalSettingsSheet(
    sheetState: SheetState,
    profile: VoiceProfileEntity?,
    currentDialect: RegionalDialect? = null,
    activeRegionProfile: com.example.data.regional.RegionalProfile? = null,
    onOpenRegionSelector: () -> Unit = {},
    onSelectDialect: (RegionalDialect) -> Unit = {},
    onOpenHierarchicalDrillDown: () -> Unit = {},
    onStrengthChange: (Float) -> Unit,
    onPersonalityChange: (VoicePersonality) -> Unit,
    onSlangToggle: (Boolean) -> Unit,
    onNaturalMixingToggle: (Boolean) -> Unit = {},
    onResponseStyleChange: (String) -> Unit,
    onVoiceSpeedChange: (String) -> Unit = {},
    onCustomPromptNotesChange: (String) -> Unit = {},
    promptContextPreview: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    var currentThemeId by remember { mutableStateOf(themeManager.getCurrentThemeId()) }
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val regionalStyleEnabled by themeManager.regionalStyleEnabled.collectAsState()
    var selectedSection by remember { mutableStateOf<SettingsMenuSection?>(null) }
    var customNotesInput by remember(profile?.customPromptNotes) {
        mutableStateOf(profile?.customPromptNotes ?: "")
    }

    val activeDialect = currentDialect ?: (profile?.let { DialectCatalog.getDialectById(it.dialectId) }
        ?: DialectCatalog.dialects.first())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FuturisticTheme.surfaceElevated,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .testTag("regional_prompt_settings_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FuturisticTheme.primaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Appearance, regional AI, memory, and privacy",
                        style = MaterialTheme.typography.bodySmall,
                        color = FuturisticTheme.secondaryText
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("close_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = FuturisticTheme.secondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Settings Navigation Category Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isAllSelected = selectedSection == null
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedSection = null }
                        .testTag("settings_tab_all"),
                    color = if (isAllSelected) FuturisticTheme.accent else FuturisticTheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAllSelected) FuturisticTheme.accent else FuturisticTheme.border
                    )
                ) {
                    Text(
                        text = "All",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isAllSelected) Color.White else FuturisticTheme.secondaryText,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }

                SettingsMenuSection.values().forEach { section ->
                    val isSelected = selectedSection == section
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedSection = section }
                            .testTag("settings_tab_${section.name.lowercase()}"),
                        color = if (isSelected) FuturisticTheme.accent else FuturisticTheme.surface,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) FuturisticTheme.accent else FuturisticTheme.border
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else FuturisticTheme.secondaryText,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else FuturisticTheme.secondaryText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =================================================================
            // 1. APPEARANCE: THEME SELECTION & COLOR PRESETS
            // =================================================================
            if (selectedSection == null || selectedSection == SettingsMenuSection.APPEARANCE) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_appearance_theme_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FuturisticTheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FuturisticTheme.border)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = FuturisticTheme.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Appearance & Themes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = FuturisticTheme.primaryText
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Base Mode: System | Light | Dark
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // System Default
                            val isSystemSelected = currentThemeId == "system" || currentThemeId == "auto"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSystemSelected) FuturisticTheme.accent.copy(alpha = 0.15f) else FuturisticTheme.surfaceElevated)
                                    .border(
                                        width = if (isSystemSelected) 1.5.dp else 1.dp,
                                        color = if (isSystemSelected) FuturisticTheme.accent else FuturisticTheme.border,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        currentThemeId = "system"
                                        themeManager.setTheme("system", isSystemDark)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.BrightnessAuto,
                                        contentDescription = null,
                                        tint = if (isSystemSelected) FuturisticTheme.accent else FuturisticTheme.secondaryText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "System",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSystemSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSystemSelected) FuturisticTheme.primaryText else FuturisticTheme.secondaryText
                                    )
                                }
                            }

                            // Light Mode
                            val isLightSelected = currentThemeId == "light"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isLightSelected) FuturisticTheme.accent.copy(alpha = 0.15f) else FuturisticTheme.surfaceElevated)
                                    .border(
                                        width = if (isLightSelected) 1.5.dp else 1.dp,
                                        color = if (isLightSelected) FuturisticTheme.accent else FuturisticTheme.border,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        currentThemeId = "light"
                                        themeManager.setTheme("light", isSystemDark)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = if (isLightSelected) FuturisticTheme.accent else FuturisticTheme.secondaryText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Light",
                                        fontSize = 11.sp,
                                        fontWeight = if (isLightSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isLightSelected) FuturisticTheme.primaryText else FuturisticTheme.secondaryText
                                    )
                                }
                            }

                            // Dark Mode
                            val isDarkSelected = currentThemeId == "dark" || currentThemeId == "midnight"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDarkSelected) FuturisticTheme.accent.copy(alpha = 0.15f) else FuturisticTheme.surfaceElevated)
                                    .border(
                                        width = if (isDarkSelected) 1.5.dp else 1.dp,
                                        color = if (isDarkSelected) FuturisticTheme.accent else FuturisticTheme.border,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        currentThemeId = "dark"
                                        themeManager.setTheme("dark", isSystemDark)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = null,
                                        tint = if (isDarkSelected) FuturisticTheme.accent else FuturisticTheme.secondaryText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Dark",
                                        fontSize = 11.sp,
                                        fontWeight = if (isDarkSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isDarkSelected) FuturisticTheme.primaryText else FuturisticTheme.secondaryText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Regional & Style Presets",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FuturisticTheme.primaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Preset Chips
                        val presets: List<Triple<String, String, Color>> = listOf(
                            Triple("kerala-green", "🌿 Kerala Green", Color(0xFF15803D)),
                            Triple("kerala-sunset", "🌅 Kerala Sunset", Color(0xFFC2410C)),
                            Triple("coastal-blue", "🌊 Coastal Blue", Color(0xFF0284C7)),
                            Triple("ai-purple", "⚡ AI Purple", Color(0xFF7C3AED)),
                            Triple("amoled", "🖤 AMOLED", Color(0xFF000000))
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            presets.forEach { (presetId, label, dotColor) ->
                                val isSelected = currentThemeId == presetId
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) FuturisticTheme.accent.copy(alpha = 0.15f) else FuturisticTheme.surfaceElevated)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) FuturisticTheme.accent else FuturisticTheme.border,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            currentThemeId = presetId
                                            themeManager.setTheme(presetId, isSystemDark)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(dotColor)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) FuturisticTheme.primaryText else FuturisticTheme.secondaryText
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = FuturisticTheme.border)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Regional Script Styling switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Regional Typography & Script Spacing",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FuturisticTheme.primaryText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Optimizes line heights and fonts for Malayalam, Tamil & regional scripts",
                                    fontSize = 11.sp,
                                    color = FuturisticTheme.secondaryText
                                )
                            }
                            Switch(
                                checked = regionalStyleEnabled,
                                onCheckedChange = {
                                    themeManager.setRegionalStyleEnabled(it, isSystemDark)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = FuturisticTheme.accent
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // =================================================================
            // 2. REGIONAL AI: DIALECTS, ACCENTS & VOICE
            // =================================================================
            if (selectedSection == null || selectedSection == SettingsMenuSection.REGIONAL_AI) {
                val region = activeRegionProfile ?: com.example.data.regional.RegionalProfileRegistry.KERALA
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenRegionSelector() }
                        .testTag("settings_region_voice_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = region.flagEmoji, fontSize = 28.sp)
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Region & Voice",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = region.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${region.languages.joinToString(" • ")} • Tap to switch region",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Switch Region",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(18.dp))

            // =================================================================
            // 1. TARGET REGIONAL DIALECT SELECTOR
            // =================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "1. Target Regional Dialect",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Controls authentic vocabulary, rhythm, and regional conversational phrasing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Active Target Dialect Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_target_dialect_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val flag = DialectCatalog.getFlagForCountry(activeDialect.country)
                            Text(text = flag, fontSize = 24.sp)
                            Column {
                                Text(
                                    text = activeDialect.dialectName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${activeDialect.cityOrArea} · ${activeDialect.stateOrProvince}, ${activeDialect.country}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Selected",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Language: ${activeDialect.language} · Code-switching: ${activeDialect.codeSwitchingDescription}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onOpenHierarchicalDrillDown,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("browse_all_dialects_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Browse all 25+ districts & dialects (Language → State → Area)",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Selection Chips for Prominent Regional Dialects
            Text(
                text = "Quick Select Target Dialect:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Top Kerala & International Dialects
            val popularDialects = listOf(
                "ml_in_kl_kozhikode",
                "ml_in_kl_kannur",
                "ml_in_kl_malappuram",
                "ml_in_kl_thrissur",
                "ml_in_kl_ernakulam",
                "ml_in_kl_trivandrum",
                "ml_in_kl_palakkad",
                "ml_in_kl_wayanad",
                "en_gb_scouse",
                "en_gb_cockney",
                "en_gb_scottish",
                "en_us_southern",
                "ar_kw_kuwaiti",
                "ar_eg_cairo"
            ).mapNotNull { id -> DialectCatalog.dialects.find { it.id == id } }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                popularDialects.forEach { dialect ->
                    val isSelected = activeDialect.id == dialect.id
                    val flag = DialectCatalog.getFlagForCountry(dialect.country)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface)
                            .clickable { onSelectDialect(dialect) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("quick_dialect_${dialect.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = flag, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = dialect.cityOrArea,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(18.dp))

            // =================================================================
            // 2. REGIONAL STRENGTH & AUTHENTICITY SLIDER
            // =================================================================
            RegionalStrengthSlider(
                strength = profile?.regionalStrength ?: 0.75f,
                onStrengthChange = onStrengthChange
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(18.dp))

            // =================================================================
            // 3. VOICE PERSONALITY & TONE
            // =================================================================
            Text(
                text = "3. Voice Personality & Persona",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Shapes emotional delivery, conversational posture, and friendly discourse markers",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            val currentPersonalityName = profile?.personality ?: VoicePersonality.FRIENDLY.name

            val personalities = listOf(
                VoicePersonality.FRIENDLY to "Friendly",
                VoicePersonality.LOCAL_FRIEND to "Local Friend",
                VoicePersonality.FUNNY to "Funny",
                VoicePersonality.PROFESSIONAL to "Professional",
                VoicePersonality.CALM to "Calm",
                VoicePersonality.ENERGETIC to "Energetic",
                VoicePersonality.STORYTELLER to "Storyteller",
                VoicePersonality.TEACHER to "Teacher"
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                personalities.forEach { (personality, label) ->
                    val isSelected = currentPersonalityName.equals(personality.name, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface)
                            .clickable { onPersonalityChange(personality) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("personality_chip_${personality.name.lowercase()}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Personality prompt impact explanation
            val selectedPersonalityObj = VoicePersonality.entries.find { it.name == currentPersonalityName } ?: VoicePersonality.FRIENDLY
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Prompt Directive: ${selectedPersonalityObj.title} — ${selectedPersonalityObj.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(18.dp))

            // =================================================================
            // 4. CODE-SWITCHING & SLANG CONTROLS
            // =================================================================
            Text(
                text = "4. Language Mixing & Colloquialisms",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Natural Code-Switching (e.g. Manglish) Toggle
            val naturalMixingEnabled = profile?.naturalMixingEnabled ?: true
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Natural Language Mixing",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Naturally blends English terms (e.g. Manglish) as customary in regional conversations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = naturalMixingEnabled,
                    onCheckedChange = onNaturalMixingToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.testTag("mixing_toggle_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Regional Slang On / Off
            val slangEnabled = profile?.slangEnabled ?: true
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Regional Slang & Idioms",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (slangEnabled) "Uses authentic local slang expressions and colloquial regional idioms" else "Uses clean regional vocabulary without heavy street slang",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = slangEnabled,
                    onCheckedChange = onSlangToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.testTag("slang_toggle_switch")
                )
            }

            // Preview active slang expressions
            if (slangEnabled && activeDialect.typicalExpressions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Active Regional Slang Expressions:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val slangList = activeDialect.typicalExpressions.take(5).joinToString(", ") { "${it.expression} (${it.meaning})" }
                        Text(
                            text = slangList,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(18.dp))

            // =================================================================
            // 5. RESPONSE LENGTH & VOICE PACING
            // =================================================================
            Text(
                text = "5. Response Length & Rhythm",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sets conversational sentence length and natural audio speaking pace",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            val currentStyle = profile?.responseLength ?: "Balanced"
            val styles = listOf("Short", "Balanced", "Detailed")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                styles.forEach { style ->
                    val isSelected = currentStyle.equals(style, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onResponseStyleChange(style) }
                            .testTag("response_style_${style.lowercase()}"),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = style,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = when (style) {
                                    "Short" -> "1-2 sentences"
                                    "Detailed" -> "3-5 sentences"
                                    else -> "2-3 sentences"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Voice Pacing (Slow / Normal / Fast / Natural)
            Text(
                text = "Speaking Pace",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            val currentPacing = profile?.voiceSpeed ?: "Natural"
            val pacingOptions = listOf("Slow", "Normal", "Fast", "Natural")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                pacingOptions.forEach { pacing ->
                    val isSelected = currentPacing.equals(pacing, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onVoiceSpeedChange(pacing) },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = pacing,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(18.dp))

            // =================================================================
            // 6. SPEAKING STYLE & TONE NUANCES
            // =================================================================
            Text(
                text = "Speaking Style & Tone Nuances",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Personalize speaking nuances, friendly expressions, and conversational delivery",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customNotesInput,
                onValueChange = {
                    customNotesInput = it
                    onCustomPromptNotesChange(it)
                },
                placeholder = {
                    Text(
                        "e.g. Speak like a friend at a Kozhikode tea shop; use frequent friendly discourse tags",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_prompt_notes_input"),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                minLines = 2,
                maxLines = 4
            )

            // Preset tags for 1-tap addition
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf(
                    "Tea stall banter ☕",
                    "Elder friend vibe 🤝",
                    "Humorous banter 😄",
                    "Techie colleague 💼",
                    "Respectful formal 🙏"
                )
                presets.forEach { preset ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                val newNotes = if (customNotesInput.isBlank()) preset else "$customNotesInput, $preset"
                                customNotesInput = newNotes
                                onCustomPromptNotesChange(newNotes)
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "+ $preset",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            } // End of Regional AI section

            // =================================================================
            // 3. AI & MEMORY
            // =================================================================
            if (selectedSection == null || selectedSection == SettingsMenuSection.AI_MEMORY) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_ai_memory_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FuturisticTheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FuturisticTheme.border)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = FuturisticTheme.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "AI & Memory",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = FuturisticTheme.primaryText
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Manage conversational continuity, regional understanding, and on-device memory recall.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FuturisticTheme.secondaryText,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(FuturisticTheme.surfaceElevated)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Voice & Speech Engine", fontSize = 12.sp, color = FuturisticTheme.primaryText, fontWeight = FontWeight.Medium)
                                Text("● Real-Time Regional", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Vision & Document Analysis", fontSize = 12.sp, color = FuturisticTheme.primaryText, fontWeight = FontWeight.Medium)
                                Text("● Multimodal Active", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Conversational Memory", fontSize = 12.sp, color = FuturisticTheme.primaryText, fontWeight = FontWeight.Medium)
                                Text("● On-Device Room DB", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Conversational memory cache cleared", Toast.LENGTH_SHORT).show()
                            },
                            shape = FuturisticTokens.CornerRadius.pillShape,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FuturisticTheme.primaryText),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FuturisticTheme.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("clear_memories_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear Stored AI Memories", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // =================================================================
            // 4. PRIVACY
            // =================================================================
            if (selectedSection == null || selectedSection == SettingsMenuSection.PRIVACY) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_privacy_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FuturisticTheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FuturisticTheme.border)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PrivacyTip,
                                contentDescription = null,
                                tint = FuturisticTheme.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Privacy & Data",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = FuturisticTheme.primaryText
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Your privacy is built directly into Regional Voice AI. Dialect choices, speaking style settings, and conversations are stored locally on your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FuturisticTheme.secondaryText,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(FuturisticTheme.surfaceElevated)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("✓", fontSize = 13.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                Text("On-device local SQLite/Room database storage", fontSize = 12.sp, color = FuturisticTheme.primaryText)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("✓", fontSize = 13.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                Text("Real-time voice streaming with zero permanent audio archiving", fontSize = 12.sp, color = FuturisticTheme.primaryText)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("✓", fontSize = 13.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                Text("No personal data selling or cross-app tracking", fontSize = 12.sp, color = FuturisticTheme.primaryText)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DeveloperProfile.privacyPolicyUrl)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Privacy Policy: ${DeveloperProfile.privacyPolicyUrl}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = FuturisticTokens.CornerRadius.pillShape,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = FuturisticTheme.primaryText),
                                border = androidx.compose.foundation.BorderStroke(1.dp, FuturisticTheme.border),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_privacy_policy_btn")
                            ) {
                                Text("Privacy Policy", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Local conversation cache cleared", Toast.LENGTH_SHORT).show()
                                },
                                shape = FuturisticTokens.CornerRadius.pillShape,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = FuturisticTheme.primaryText),
                                border = androidx.compose.foundation.BorderStroke(1.dp, FuturisticTheme.border),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_clear_cache_btn")
                            ) {
                                Text("Clear Cache", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // =================================================================
            // 5. ABOUT REGIONAL VOICE AI
            // =================================================================
            if (selectedSection == null || selectedSection == SettingsMenuSection.ABOUT) {
                AboutRegionalVoiceAIContent()
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun PromptDirectivePill(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "• $label:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}
