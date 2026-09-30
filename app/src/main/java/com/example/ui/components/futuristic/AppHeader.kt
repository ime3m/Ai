package com.example.ui.components.futuristic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FuturisticTheme
import com.example.ui.theme.FuturisticTokens

/**
 * Clean Top Header with Two Sides:
 * - LEFT SIDE: Main navigation menu (☰), App Name, Active Regional Style (● Kerala)
 * - RIGHT SIDE: Settings (⚙️) and Sound controls (🔊)
 */
@Composable
fun AppHeader(
    appName: String = "Regional Voice AI",
    activeDialectName: String? = null,
    regionEmoji: String = "🌴",
    regionName: String = "Kerala",
    isMuted: Boolean = false,
    selectedCategory: FuturisticCategory = FuturisticCategory.VOICE,
    onCategorySelected: (FuturisticCategory) -> Unit = {},
    onToggleMute: () -> Unit = {},
    onOpenRegionVoice: () -> Unit = {},
    onOpenDialects: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenDiagnostics: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("futuristic_app_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // =========================================================
        // TOP-LEFT: Navigation Menu (☰) + App Title + Regional Style
        // =========================================================
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // ☰ Compact Navigation Menu Button
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(FuturisticTheme.surface)
                        .border(1.dp, FuturisticTheme.border, CircleShape)
                        .testTag("header_nav_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Navigation Menu",
                        tint = FuturisticTheme.primaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Compact Modern Dropdown Navigation Menu
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(FuturisticTheme.surfaceElevated)
                        .border(1.dp, FuturisticTheme.border, RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = "NAVIGATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = FuturisticTheme.tertiaryText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        letterSpacing = 1.sp
                    )

                    HorizontalDivider(
                        color = FuturisticTheme.border.copy(alpha = 0.5f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )

                    // 1. Voice AI (Home)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Voice AI",
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedCategory == FuturisticCategory.VOICE) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedCategory == FuturisticCategory.VOICE) FuturisticTheme.accent else FuturisticTheme.primaryText
                                )
                                if (selectedCategory == FuturisticCategory.VOICE) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(FuturisticTheme.accent)
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (selectedCategory == FuturisticCategory.VOICE) FuturisticTheme.accent else FuturisticTheme.secondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            onCategorySelected(FuturisticCategory.VOICE)
                            menuExpanded = false
                        },
                        modifier = Modifier.testTag("nav_menu_voice_ai")
                    )

                    // 2. Create (Section 34 & 42)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Create",
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedCategory == FuturisticCategory.CREATE) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedCategory == FuturisticCategory.CREATE) FuturisticTheme.accent else FuturisticTheme.primaryText
                                )
                                if (selectedCategory == FuturisticCategory.CREATE) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(FuturisticTheme.accent)
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (selectedCategory == FuturisticCategory.CREATE) FuturisticTheme.accent else FuturisticTheme.secondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            onCategorySelected(FuturisticCategory.CREATE)
                            menuExpanded = false
                        },
                        modifier = Modifier.testTag("nav_menu_create")
                    )

                    // 3. History
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "History",
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedCategory == FuturisticCategory.CONVERSATION) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedCategory == FuturisticCategory.CONVERSATION) FuturisticTheme.accent else FuturisticTheme.primaryText
                                )
                                if (selectedCategory == FuturisticCategory.CONVERSATION) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(FuturisticTheme.accent)
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = if (selectedCategory == FuturisticCategory.CONVERSATION) FuturisticTheme.accent else FuturisticTheme.secondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            onCategorySelected(FuturisticCategory.CONVERSATION)
                            menuExpanded = false
                        },
                        modifier = Modifier.testTag("nav_menu_history")
                    )

                    // 4. Saved (Section 11)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Saved",
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedCategory == FuturisticCategory.SAVED) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedCategory == FuturisticCategory.SAVED) FuturisticTheme.accent else FuturisticTheme.primaryText
                                )
                                if (selectedCategory == FuturisticCategory.SAVED) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(FuturisticTheme.accent)
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (selectedCategory == FuturisticCategory.SAVED) FuturisticTheme.accent else FuturisticTheme.secondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            onCategorySelected(FuturisticCategory.SAVED)
                            menuExpanded = false
                        },
                        modifier = Modifier.testTag("nav_menu_saved")
                    )

                    HorizontalDivider(
                        color = FuturisticTheme.border,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )

                    // 5. Writing (Section 19 & 41)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Writing",
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedCategory == FuturisticCategory.WRITING) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedCategory == FuturisticCategory.WRITING) FuturisticTheme.accent else FuturisticTheme.primaryText
                                )
                                if (selectedCategory == FuturisticCategory.WRITING) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(FuturisticTheme.accent)
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = if (selectedCategory == FuturisticCategory.WRITING) FuturisticTheme.accent else FuturisticTheme.secondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            onCategorySelected(FuturisticCategory.WRITING)
                            menuExpanded = false
                        },
                        modifier = Modifier.testTag("nav_menu_writing")
                    )

                    // 6. Documents (Section 18 & 40)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Documents",
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedCategory == FuturisticCategory.DOCUMENTS) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedCategory == FuturisticCategory.DOCUMENTS) FuturisticTheme.accent else FuturisticTheme.primaryText
                                )
                                if (selectedCategory == FuturisticCategory.DOCUMENTS) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(FuturisticTheme.accent)
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = if (selectedCategory == FuturisticCategory.DOCUMENTS) FuturisticTheme.accent else FuturisticTheme.secondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            onCategorySelected(FuturisticCategory.DOCUMENTS)
                            menuExpanded = false
                        },
                        modifier = Modifier.testTag("nav_menu_documents")
                    )

                    // 7. Insights
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Insights",
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedCategory == FuturisticCategory.INSIGHTS) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedCategory == FuturisticCategory.INSIGHTS) FuturisticTheme.accent else FuturisticTheme.primaryText
                                )
                                if (selectedCategory == FuturisticCategory.INSIGHTS) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(FuturisticTheme.accent)
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (selectedCategory == FuturisticCategory.INSIGHTS) FuturisticTheme.accent else FuturisticTheme.secondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            onCategorySelected(FuturisticCategory.INSIGHTS)
                            menuExpanded = false
                        },
                        modifier = Modifier.testTag("nav_menu_insights")
                    )

                    // 8. Languages
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Languages",
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedCategory == FuturisticCategory.LANGUAGES) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedCategory == FuturisticCategory.LANGUAGES) FuturisticTheme.accent else FuturisticTheme.primaryText
                                )
                                if (selectedCategory == FuturisticCategory.LANGUAGES) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(FuturisticTheme.accent)
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = if (selectedCategory == FuturisticCategory.LANGUAGES) FuturisticTheme.accent else FuturisticTheme.secondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            onCategorySelected(FuturisticCategory.LANGUAGES)
                            menuExpanded = false
                        },
                        modifier = Modifier.testTag("nav_menu_languages")
                    )

                    HorizontalDivider(
                        color = FuturisticTheme.border,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )

                    // 9. AI Memory (Section 22)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AI Memory",
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedCategory == FuturisticCategory.MEMORY) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedCategory == FuturisticCategory.MEMORY) FuturisticTheme.accent else FuturisticTheme.primaryText
                                )
                                if (selectedCategory == FuturisticCategory.MEMORY) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(FuturisticTheme.accent)
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = if (selectedCategory == FuturisticCategory.MEMORY) FuturisticTheme.accent else FuturisticTheme.secondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            onCategorySelected(FuturisticCategory.MEMORY)
                            menuExpanded = false
                        },
                        modifier = Modifier.testTag("nav_menu_memory")
                    )

                    // 10. Settings
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Settings",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = FuturisticTheme.primaryText
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = FuturisticTheme.secondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            onOpenSettings()
                            menuExpanded = false
                        },
                        modifier = Modifier.testTag("nav_menu_settings")
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // App Name & Regional Style indicator (● Kerala)
            Column(
                modifier = Modifier.clickable(onClick = onOpenRegionVoice)
            ) {
                Text(
                    text = appName,
                    color = FuturisticTheme.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = regionName,
                        color = FuturisticTheme.secondaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // =========================================================
        // TOP-RIGHT: Utility Controls (🔊 Mute, ⚙️ Settings)
        // =========================================================
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 🔊 Sound Mute Toggle Icon
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(FuturisticTheme.surface)
                    .border(1.dp, FuturisticTheme.border, CircleShape)
                    .testTag("header_toggle_mute")
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Unmute Audio" else "Mute Audio",
                    tint = if (isMuted) FuturisticTheme.tertiaryText else FuturisticTheme.accent,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Optional Diagnostic trigger
            if (onOpenDiagnostics != null) {
                IconButton(
                    onClick = onOpenDiagnostics,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(FuturisticTheme.surface)
                        .border(1.dp, FuturisticTheme.border, CircleShape)
                        .testTag("header_open_diagnostics")
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Diagnostics",
                        tint = FuturisticTheme.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ⚙️ Settings Icon Button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(FuturisticTheme.surface)
                    .border(1.dp, FuturisticTheme.border, CircleShape)
                    .testTag("header_open_settings")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = FuturisticTheme.primaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
