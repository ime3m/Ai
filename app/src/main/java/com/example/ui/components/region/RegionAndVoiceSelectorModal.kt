package com.example.ui.components.region

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.regional.RegionalProfile
import com.example.data.regional.RegionalProfileRegistry
import com.example.ui.theme.FuturisticTheme
import com.example.ui.theme.FuturisticTokens

/**
 * Region & Voice Modal Selector.
 * Allows users to choose their active region & language personality:
 * 🌴 Kerala (Default)
 * 🇮🇳 India (Pan-Indian)
 * 🇮🇳 Tamil Nadu
 * 🇮🇳 Karnataka
 * 🇮🇳 Andhra Pradesh & Telangana
 * 🇮🇳 North India
 * 🇵🇭 Philippines
 * 🇰🇼 Kuwait & Gulf
 * 🇺🇸 United States
 * 🇬🇧 United Kingdom
 * 🇯🇵 Japan
 * ⚙️ Custom
 *
 * Updates language preferences, conversational style, speech recognition, and TTS.
 * General AI intelligence and reasoning remain universal and unrestricted.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionAndVoiceSelectorModal(
    sheetState: SheetState,
    selectedRegionId: String,
    onSelectRegion: (RegionalProfile) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FuturisticTheme.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .testTag("region_and_voice_modal")
        ) {
            // Header row with title and close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FuturisticTheme.surfaceElevated)
                            .border(1.dp, FuturisticTheme.border, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = FuturisticTheme.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Region & Voice",
                            color = FuturisticTheme.primaryText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "General AI assistant with native regional personalities",
                            color = FuturisticTheme.secondaryText,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(FuturisticTheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = FuturisticTheme.secondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Informational pill: Regional style != knowledge limit
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FuturisticTokens.CornerRadius.mediumShape)
                    .background(FuturisticTheme.surfaceElevated)
                    .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.mediumShape)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "General knowledge, coding, science, and reasoning stay fully global across all regions.",
                        color = FuturisticTheme.secondaryText,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(RegionalProfileRegistry.allProfiles) { profile ->
                    val isSelected = profile.id.equals(selectedRegionId, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(FuturisticTokens.CornerRadius.mediumShape)
                            .background(if (isSelected) FuturisticTheme.surfaceElevated else FuturisticTheme.surface)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) FuturisticTheme.accent else FuturisticTheme.border,
                                shape = FuturisticTokens.CornerRadius.mediumShape
                            )
                            .clickable {
                                onSelectRegion(profile)
                                onDismiss()
                            }
                            .padding(16.dp)
                            .testTag("region_item_${profile.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = profile.flagEmoji,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = profile.name,
                                            color = FuturisticTheme.primaryText,
                                            fontSize = 16.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                        )
                                        if (profile.id == "kerala") {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(FuturisticTokens.CornerRadius.pillShape)
                                                    .background(FuturisticTheme.accent.copy(alpha = 0.12f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Default",
                                                    color = FuturisticTheme.accent,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${profile.languages.joinToString(" • ")} • ${profile.conversationalPersona}",
                                        color = FuturisticTheme.secondaryText,
                                        fontSize = 12.sp,
                                        maxLines = 2
                                    )
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(FuturisticTheme.accent),
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
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
