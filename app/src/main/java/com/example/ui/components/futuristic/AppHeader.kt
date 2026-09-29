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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * Minimalist Futuristic App Header.
 * Features:
 * - Geometric app logo mark with soft blue inner glow
 * - App title & subtle active dialect badge
 * - Minimal utility action (Mute / Diagnostics)
 * - Rounded dark pill menu button for settings & dialect switching
 * - Lightweight, airy composition with generous horizontal padding
 */
@Composable
fun AppHeader(
    appName: String = "Regional Voice AI",
    activeDialectName: String? = null,
    regionEmoji: String = "🌴",
    regionName: String = "Kerala",
    isMuted: Boolean = false,
    onToggleMute: () -> Unit = {},
    onOpenRegionVoice: () -> Unit = {},
    onOpenDialects: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenDiagnostics: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("futuristic_app_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side: App Logo and Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onOpenRegionVoice)
        ) {
            // Minimal futuristic logo mark with region flag
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(FuturisticTheme.surface)
                    .border(1.dp, FuturisticTheme.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = regionEmoji, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = appName,
                    color = FuturisticTheme.primaryText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "$regionName • ${activeDialectName ?: "Voice"}",
                        color = FuturisticTheme.secondaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // Right side: Small utility icons and rounded dark menu button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sound Mute Toggle Icon
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(FuturisticTheme.surface)
                    .testTag("header_toggle_mute")
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Unmute Audio" else "Mute Audio",
                    tint = FuturisticTheme.secondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Optional Diagnostic sheet trigger
            if (onOpenDiagnostics != null) {
                IconButton(
                    onClick = onOpenDiagnostics,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(FuturisticTheme.surface)
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

            // Rounded dark menu button
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(FuturisticTokens.CornerRadius.pillShape)
                    .background(FuturisticTheme.primaryButton)
                    .clickable(onClick = onOpenSettings)
                    .padding(horizontal = 14.dp)
                    .testTag("header_menu_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = FuturisticTheme.primaryButtonText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Settings",
                        color = FuturisticTheme.primaryButtonText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
