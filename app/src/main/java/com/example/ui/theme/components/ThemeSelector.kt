package com.example.ui.theme.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeManager
import com.example.ui.theme.ThemeMetadata

/**
 * Modern Theme Selector Component
 * Allows users to switch active semantic design token themes instantly,
 * preview color tokens, and open the full Theme Settings page.
 */
@Composable
fun ThemeSelector(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val regionalEnabled by themeManager.regionalStyleEnabled.collectAsState()
    val availableThemes = remember { themeManager.getAvailableThemes() }

    var showThemeSettingsModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s3)
    ) {
        // Section Header: Settings → Appearance → Themes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s2)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = AppTheme.colors.accent.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Appearance & Themes",
                        style = AppTheme.typography.heading.h3,
                        color = AppTheme.colors.text.primary
                    )
                }
                Text(
                    text = "Settings → Appearance → Themes",
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.text.muted
                )
            }

            TextButton(
                onClick = { showThemeSettingsModal = true },
                modifier = Modifier.testTag("open_theme_settings_button")
            ) {
                Text(
                    text = "Customize",
                    style = AppTheme.typography.labelMedium,
                    color = AppTheme.colors.accent.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = AppTheme.colors.accent.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Text(
            text = "Tap any theme card below for instant live preview without restarting:",
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.text.secondary
        )

        // Horizontal Theme Preset Cards
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s3),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
        ) {
            items(availableThemes) { metadata ->
                val isSelected = currentTheme.metadata.id == metadata.id
                ThemePresetCard(
                    metadata = metadata,
                    isSelected = isSelected,
                    onClick = { themeManager.setTheme(metadata.id) }
                )
            }

            // Custom Theme button
            item {
                val isCustomSelected = currentTheme.metadata.id == "custom"
                Surface(
                    modifier = Modifier
                        .width(148.dp)
                        .clip(AppTheme.radius.cardShape)
                        .clickable { showThemeSettingsModal = true }
                        .testTag("theme_card_custom_trigger"),
                    shape = AppTheme.radius.cardShape,
                    color = AppTheme.colors.surface.card,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isCustomSelected) 2.dp else 1.dp,
                        color = if (isCustomSelected) AppTheme.colors.accent.primary else AppTheme.colors.border.subtle
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.surface.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = null,
                                tint = AppTheme.colors.accent.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🎨 Custom Theme",
                            style = AppTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.text.primary
                        )
                        Text(
                            text = "Build your palette",
                            style = AppTheme.typography.caption,
                            color = AppTheme.colors.text.muted
                        )
                    }
                }
            }
        }

        // Regional Style Card with link to detailed settings
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppTheme.radius.cardShape)
                .clickable { showThemeSettingsModal = true },
            shape = AppTheme.radius.cardShape,
            color = AppTheme.colors.surface.card,
            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border.subtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.spacing.s3),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Regional Visual Style",
                            style = AppTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.text.primary
                        )
                        if (regionalEnabled) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AppTheme.colors.accent.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = currentTheme.regionalStyle.name,
                                    style = AppTheme.typography.caption,
                                    color = AppTheme.colors.accent.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (regionalEnabled) "Active: ${currentTheme.regionalStyle.name} · Tap to customize 8 languages" else "Disabled · Tap to configure",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.muted
                    )
                }

                Switch(
                    checked = regionalEnabled,
                    onCheckedChange = { themeManager.setRegionalStyleEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppTheme.colors.surface.primary,
                        checkedTrackColor = AppTheme.colors.accent.primary,
                        uncheckedThumbColor = AppTheme.colors.text.muted,
                        uncheckedTrackColor = AppTheme.colors.surface.secondary
                    ),
                    modifier = Modifier.testTag("theme_selector_regional_switch")
                )
            }
        }

        // Live Token Preview Card
        ThemePreviewCard()
    }

    // Full-Screen Theme Customization Dialog
    if (showThemeSettingsModal) {
        Dialog(
            onDismissRequest = { showThemeSettingsModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ThemeSettingsSheet(
                onDismiss = { showThemeSettingsModal = false }
            )
        }
    }
}

@Composable
fun ThemePresetCard(
    metadata: ThemeMetadata,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(152.dp)
            .clip(AppTheme.radius.cardShape)
            .clickable(onClick = onClick)
            .testTag("theme_preset_card_${metadata.id}"),
        shape = AppTheme.radius.cardShape,
        color = AppTheme.colors.surface.card,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) AppTheme.colors.accent.primary else AppTheme.colors.border.subtle
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // Miniature color card preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(metadata.previewPrimary)
                    .padding(6.dp)
            ) {
                // Miniature Chat bubble preview
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(4.dp))
                        .background(metadata.previewAccent.copy(alpha = 0.3f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(metadata.previewAccent)
                    )
                }

                // Selected Check indicator
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.accent.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Accent dot
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(metadata.previewAccent)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = metadata.name,
                style = AppTheme.typography.labelMedium,
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
                lineHeight = 13.sp
            )
        }
    }
}

/**
 * Interactive Token Inspector Card
 */
@Composable
fun ThemePreviewCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("theme_live_preview_card"),
        shape = AppTheme.radius.cardShape,
        color = AppTheme.colors.surface.card,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, AppTheme.colors.border.subtle)
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.s3),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s2)
        ) {
            Text(
                text = "Live Semantic Color Swatches",
                style = AppTheme.typography.labelSmall,
                color = AppTheme.colors.text.secondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniSwatch(label = "Bg", color = AppTheme.colors.background.primary)
                MiniSwatch(label = "Card", color = AppTheme.colors.surface.card)
                MiniSwatch(label = "Accent", color = AppTheme.colors.accent.primary)
                MiniSwatch(label = "Text", color = AppTheme.colors.text.primary)
                MiniSwatch(label = "Voice", color = AppTheme.voice.idle.background)
                MiniSwatch(label = "Status", color = AppTheme.colors.status.success)
            }
        }
    }
}

@Composable
private fun RowScope.MiniSwatch(label: String, color: Color) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .border(0.5.dp, AppTheme.colors.border.default, RoundedCornerShape(4.dp))
        )
        Text(
            text = label,
            style = AppTheme.typography.caption,
            color = AppTheme.colors.text.muted,
            fontSize = 9.sp
        )
    }
}
