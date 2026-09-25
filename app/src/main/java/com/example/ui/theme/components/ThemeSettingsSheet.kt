package com.example.ui.theme.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.theme.regional.RegionalStyleDefinition
import com.example.ui.theme.regional.RegionalStyleRegistry
import com.example.ui.theme.tokens.PrimitiveTokens

enum class ThemeSettingsTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    PRESETS("Built-in Themes", Icons.Default.Palette),
    REGIONAL("Regional Style", Icons.Default.Translate),
    CUSTOM("Custom Theme", Icons.Default.ColorLens)
}

/**
 * Full Theme Settings Sheet / Page
 * Accessible from Settings/Profile -> Appearance -> Themes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val regionalEnabled by themeManager.regionalStyleEnabled.collectAsState()
    val currentRegionalStyle by themeManager.currentRegionalStyleFlow.collectAsState()
    val customPalette by themeManager.customPaletteFlow.collectAsState()
    val availableThemes = remember { themeManager.getAvailableThemes() }

    var selectedTab by remember { mutableStateOf(ThemeSettingsTab.PRESETS) }

    // State for Custom Theme Editor
    var editingPrimary by remember(customPalette) { mutableStateOf(customPalette.primaryColor) }
    var editingAccent by remember(customPalette) { mutableStateOf(customPalette.accentColor) }
    var editingBg by remember(customPalette) { mutableStateOf(customPalette.backgroundColor) }
    var editingText by remember(customPalette) { mutableStateOf(customPalette.textColor) }
    var editingBtn by remember(customPalette) { mutableStateOf(customPalette.buttonColor) }
    var editingCard by remember(customPalette) { mutableStateOf(customPalette.cardColor) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("theme_settings_page"),
        color = AppTheme.colors.background.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Sheet Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_theme_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AppTheme.colors.text.primary
                        )
                    }
                    Column {
                        Text(
                            text = "Appearance & Themes",
                            style = AppTheme.typography.heading.h2,
                            color = AppTheme.colors.text.primary
                        )
                        Text(
                            text = "Settings → Appearance → Themes",
                            style = AppTheme.typography.caption,
                            color = AppTheme.colors.text.muted
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onDismiss,
                    shape = AppTheme.radius.buttonShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AppTheme.colors.surface.card,
                        contentColor = AppTheme.colors.text.primary
                    )
                ) {
                    Text("Done", style = AppTheme.typography.labelMedium)
                }
            }

            HorizontalDivider(
                color = AppTheme.colors.border.subtle,
                thickness = 0.5.dp
            )

            // Tab navigation
            SecondaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = AppTheme.colors.background.primary,
                contentColor = AppTheme.colors.accent.primary,
                divider = {}
            ) {
                ThemeSettingsTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.label,
                                style = AppTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        selectedContentColor = AppTheme.colors.accent.primary,
                        unselectedContentColor = AppTheme.colors.text.muted
                    )
                }
            }

            HorizontalDivider(
                color = AppTheme.colors.border.subtle,
                thickness = 0.5.dp
            )

            // Content per tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    ThemeSettingsTab.PRESETS -> {
                        BuiltInThemesTab(
                            availableThemes = availableThemes,
                            currentThemeId = currentTheme.metadata.id,
                            onSelectTheme = { themeId ->
                                themeManager.setTheme(themeId)
                            }
                        )
                    }
                    ThemeSettingsTab.REGIONAL -> {
                        RegionalStyleTab(
                            regionalEnabled = regionalEnabled,
                            currentRegionalStyle = currentRegionalStyle,
                            onToggleEnabled = { enabled ->
                                themeManager.setRegionalStyleEnabled(enabled)
                            },
                            onSelectRegionalStyle = { style ->
                                themeManager.setRegionalStyle(style)
                            }
                        )
                    }
                    ThemeSettingsTab.CUSTOM -> {
                        CustomThemeTab(
                            primaryColor = editingPrimary,
                            accentColor = editingAccent,
                            backgroundColor = editingBg,
                            textColor = editingText,
                            buttonColor = editingBtn,
                            cardColor = editingCard,
                            onPrimaryChange = { editingPrimary = it },
                            onAccentChange = { editingAccent = it },
                            onBgChange = { editingBg = it },
                            onTextChange = { editingText = it },
                            onBtnChange = { editingBtn = it },
                            onCardChange = { editingCard = it },
                            onApply = {
                                val newPalette = CustomThemePalette(
                                    primaryColor = editingPrimary,
                                    accentColor = editingAccent,
                                    backgroundColor = editingBg,
                                    textColor = editingText,
                                    buttonColor = editingBtn,
                                    cardColor = editingCard
                                )
                                themeManager.applyCustomPalette(newPalette)
                            },
                            onReset = {
                                themeManager.resetToDefault()
                                editingPrimary = PrimitiveTokens.blue600
                                editingAccent = PrimitiveTokens.blue400
                                editingBg = PrimitiveTokens.black950
                                editingText = PrimitiveTokens.white90
                                editingBtn = PrimitiveTokens.blue600
                                editingCard = PrimitiveTokens.neutral900
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BuiltInThemesTab(
    availableThemes: List<ThemeMetadata>,
    currentThemeId: String,
    onSelectTheme: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Select from 8 carefully balanced themes optimized for contrast, voice interaction, and OLED battery life. Changes apply instantly.",
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.text.secondary
        )

        // 2-column grid of preview cards
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            availableThemes.chunked(2).forEach { rowThemes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowThemes.forEach { metadata ->
                        val isSelected = currentThemeId == metadata.id
                        Box(modifier = Modifier.weight(1f)) {
                            ThemeCardDetailed(
                                metadata = metadata,
                                isSelected = isSelected,
                                onClick = { onSelectTheme(metadata.id) }
                            )
                        }
                    }
                    if (rowThemes.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeCardDetailed(
    metadata: ThemeMetadata,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppTheme.radius.cardShape)
            .clickable(onClick = onClick)
            .testTag("theme_card_${metadata.id}"),
        shape = AppTheme.radius.cardShape,
        color = AppTheme.colors.surface.card,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) AppTheme.colors.accent.primary else AppTheme.colors.border.subtle
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Miniature UI preview frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(metadata.previewPrimary)
                    .padding(6.dp)
            ) {
                // Miniature Chat bubble preview
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(6.dp))
                        .background(metadata.previewAccent.copy(alpha = 0.25f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(metadata.previewAccent)
                    )
                }

                // Miniature mic button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(metadata.previewAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.accent.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = metadata.name,
                style = AppTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) AppTheme.colors.accent.primary else AppTheme.colors.text.primary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = metadata.description,
                style = AppTheme.typography.caption,
                color = AppTheme.colors.text.muted,
                maxLines = 2,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun RegionalStyleTab(
    regionalEnabled: Boolean,
    currentRegionalStyle: RegionalStyleDefinition,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectRegionalStyle: (RegionalStyleDefinition) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppTheme.radius.cardShape,
            color = AppTheme.colors.surface.card,
            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border.subtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Regional Style",
                        style = AppTheme.typography.heading.h3,
                        color = AppTheme.colors.text.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Applies script-specific line heights and subtle heritage accent colors for native clarity.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = regionalEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppTheme.colors.surface.primary,
                        checkedTrackColor = AppTheme.colors.accent.primary
                    ),
                    modifier = Modifier.testTag("toggle_regional_style_switch")
                )
            }
        }

        Text(
            text = "Select Regional Identity (8 Regions):",
            style = AppTheme.typography.labelLarge,
            color = AppTheme.colors.text.primary
        )

        // List of all 8 Regional Identities
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RegionalStyleRegistry.allRegionalStyles.forEach { style ->
                val isSelected = currentRegionalStyle.scriptId == style.scriptId
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppTheme.radius.cardShape)
                        .clickable(enabled = regionalEnabled) {
                            onSelectRegionalStyle(style)
                        }
                        .testTag("regional_style_item_${style.scriptId.code}"),
                    shape = AppTheme.radius.cardShape,
                    color = if (isSelected && regionalEnabled) AppTheme.colors.accent.primary.copy(alpha = 0.1f) else AppTheme.colors.surface.card,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected && regionalEnabled) 1.5.dp else 1.dp,
                        color = if (isSelected && regionalEnabled) AppTheme.colors.accent.primary else AppTheme.colors.border.subtle
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        style.regionalAccentOverride?.primary?.copy(alpha = 0.2f)
                                            ?: AppTheme.colors.surface.secondary
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = style.scriptId.nativeName.take(2),
                                    style = AppTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = style.regionalAccentOverride?.primary ?: AppTheme.colors.text.primary
                                )
                            }

                            Column {
                                Text(
                                    text = style.name,
                                    style = AppTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.text.primary
                                )
                                Text(
                                    text = "${style.scriptId.nativeName} · Line-height ${style.scriptLineHeightMultiplier}x",
                                    style = AppTheme.typography.caption,
                                    color = AppTheme.colors.text.muted
                                )
                            }
                        }

                        if (isSelected && regionalEnabled) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = AppTheme.colors.accent.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomThemeTab(
    primaryColor: Color,
    accentColor: Color,
    backgroundColor: Color,
    textColor: Color,
    buttonColor: Color,
    cardColor: Color,
    onPrimaryChange: (Color) -> Unit,
    onAccentChange: (Color) -> Unit,
    onBgChange: (Color) -> Unit,
    onTextChange: (Color) -> Unit,
    onBtnChange: (Color) -> Unit,
    onCardChange: (Color) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit
) {
    val contrastBgText = ThemeSafety.contrastRatio(backgroundColor, textColor)
    val contrastCardText = ThemeSafety.contrastRatio(cardColor, textColor)
    val isContrastGood = contrastBgText >= 4.5 && contrastCardText >= 3.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live Preview Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppTheme.radius.cardShape,
            color = backgroundColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, cardColor.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Theme Preview",
                        style = AppTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.6f)
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isContrastGood) PrimitiveTokens.green500.copy(alpha = 0.2f) else PrimitiveTokens.amber500.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isContrastGood) "Contrast: WCAG Pass" else "Contrast: Review Colors",
                            style = AppTheme.typography.caption,
                            color = if (isContrastGood) PrimitiveTokens.green400 else PrimitiveTokens.amber400,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Chat bubble preview
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = buttonColor,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "User message (Button / Primary)",
                        style = AppTheme.typography.bodySmall,
                        color = ThemeSafety.readableTextColor(buttonColor),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = cardColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardColor.copy(alpha = 0.8f)),
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            text = "AI response message (Card)",
                            style = AppTheme.typography.bodySmall,
                            color = textColor
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Text(
                                text = "Accent badge",
                                style = AppTheme.typography.caption,
                                color = accentColor
                            )
                        }
                    }
                }
            }
        }

        // Color Selectors
        Text(
            text = "Customize Colors:",
            style = AppTheme.typography.labelLarge,
            color = AppTheme.colors.text.primary
        )

        ColorSwatchRow(label = "Primary Color", currentColor = primaryColor, onSelect = onPrimaryChange)
        ColorSwatchRow(label = "Accent Color", currentColor = accentColor, onSelect = onAccentChange)
        ColorSwatchRow(label = "Background Color", currentColor = backgroundColor, onSelect = onBgChange)
        ColorSwatchRow(label = "Text Color", currentColor = textColor, onSelect = onTextChange)
        ColorSwatchRow(label = "Button Color", currentColor = buttonColor, onSelect = onBtnChange)
        ColorSwatchRow(label = "Card Color", currentColor = cardColor, onSelect = onCardChange)

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons: Apply and Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier
                    .weight(1f)
                    .testTag("reset_theme_button"),
                shape = AppTheme.radius.buttonShape
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset to Default")
            }

            Button(
                onClick = onApply,
                modifier = Modifier
                    .weight(1f)
                    .testTag("apply_custom_theme_button"),
                shape = AppTheme.radius.buttonShape,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = ThemeSafety.readableTextColor(primaryColor),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Apply Custom Theme",
                    color = ThemeSafety.readableTextColor(primaryColor)
                )
            }
        }
    }
}

@Composable
private fun ColorSwatchRow(
    label: String,
    currentColor: Color,
    onSelect: (Color) -> Unit
) {
    val standardSwatches = listOf(
        PrimitiveTokens.blue600,
        PrimitiveTokens.blue400,
        PrimitiveTokens.green500,
        PrimitiveTokens.green600,
        PrimitiveTokens.orange500,
        PrimitiveTokens.orange600,
        PrimitiveTokens.purple600,
        PrimitiveTokens.purple500,
        PrimitiveTokens.teal500,
        PrimitiveTokens.teal400,
        PrimitiveTokens.amber500,
        PrimitiveTokens.red600,
        PrimitiveTokens.white100,
        PrimitiveTokens.neutral100,
        PrimitiveTokens.neutral800,
        PrimitiveTokens.neutral900,
        PrimitiveTokens.black950,
        PrimitiveTokens.blackPure
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = AppTheme.typography.labelMedium,
                color = AppTheme.colors.text.primary
            )
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(currentColor)
                    .border(1.dp, AppTheme.colors.border.strong, CircleShape)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            standardSwatches.forEach { color ->
                val isSelected = currentColor == color
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onSelect(color) }
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) AppTheme.colors.accent.primary else AppTheme.colors.border.subtle,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ThemeSafety.readableTextColor(color),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
