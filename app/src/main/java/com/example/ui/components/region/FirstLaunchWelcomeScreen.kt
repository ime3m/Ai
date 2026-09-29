package com.example.ui.components.region

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.regional.RegionalProfile
import com.example.data.regional.RegionalProfileRegistry
import com.example.ui.components.futuristic.ParticleBackground
import com.example.ui.components.futuristic.PrimaryPillButton
import com.example.ui.theme.FuturisticTheme
import com.example.ui.theme.FuturisticTokens

/**
 * Minimal, aesthetic first-launch onboarding screen.
 *
 * Features:
 * - Detects first launch and defaults to 🌴 Kerala.
 * - Headline: "Welcome to Regional Voice AI"
 * - Subheadline: "Your AI. Your language. Your way."
 * - Dedicated aesthetic region confirmation card.
 * - Quick-pick pills for immediate selection.
 * - "Continue" primary button to confirm.
 * - "Choose another region" secondary button for the full regional catalog.
 * - Clear reminder that general knowledge (science, code, math, history) remains universal.
 */
@Composable
fun FirstLaunchWelcomeScreen(
    selectedProfile: RegionalProfile = RegionalProfileRegistry.KERALA,
    onSelectQuickProfile: (RegionalProfile) -> Unit = {},
    onContinue: () -> Unit,
    onChangeRegion: () -> Unit,
    modifier: Modifier = Modifier
) {
    ParticleBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .testTag("first_launch_welcome_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .widthIn(max = 540.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp)
            ) {
                // Top geometric focal icon
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(FuturisticTheme.surfaceElevated)
                        .border(1.dp, FuturisticTheme.border, CircleShape)
                        .shadow(elevation = 2.dp, shape = CircleShape, spotColor = FuturisticTheme.shadow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = selectedProfile.flagEmoji, fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Welcome Headline
                Text(
                    text = "Welcome to Regional Voice AI",
                    color = FuturisticTheme.primaryText,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.6).sp,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subheadline
                Text(
                    text = "Your AI. Your language. Your way.",
                    color = FuturisticTheme.secondaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Pre-selected Regional Card with dynamic animated profile change
                AnimatedContent(
                    targetState = selectedProfile,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                    label = "welcome_card_transition"
                ) { profile ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(FuturisticTokens.CornerRadius.largeShape)
                            .background(FuturisticTheme.surfaceElevated)
                            .border(1.5.dp, FuturisticTheme.accent.copy(alpha = 0.8f), FuturisticTokens.CornerRadius.largeShape)
                            .shadow(elevation = 3.dp, shape = FuturisticTokens.CornerRadius.largeShape, spotColor = FuturisticTheme.shadow)
                            .padding(20.dp)
                            .testTag("first_launch_kerala_card")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = profile.flagEmoji, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (profile.id == "kerala") "Start with Kerala style" else "${profile.name} Style",
                                                color = FuturisticTheme.primaryText,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (profile.id == "kerala") {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(FuturisticTokens.CornerRadius.pillShape)
                                                        .background(FuturisticTheme.accent.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "Default",
                                                        color = FuturisticTheme.accent,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Natural ${profile.languages.joinToString(" • ")} conversation",
                                            color = FuturisticTheme.secondaryText,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

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

                            Spacer(modifier = Modifier.height(14.dp))

                            // Personality tags
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Friendly", "Natural", "Warm", "Conversational").forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(FuturisticTokens.CornerRadius.pillShape)
                                            .background(FuturisticTheme.surface)
                                            .border(1.dp, FuturisticTheme.border.copy(alpha = 0.7f), FuturisticTokens.CornerRadius.pillShape)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            color = FuturisticTheme.secondaryText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Quick horizontal region switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickPicks = listOf(
                        RegionalProfileRegistry.KERALA,
                        RegionalProfileRegistry.INDIA_GENERAL,
                        RegionalProfileRegistry.TAMIL_NADU,
                        RegionalProfileRegistry.KARNATAKA,
                        RegionalProfileRegistry.PHILIPPINES,
                        RegionalProfileRegistry.KUWAIT,
                        RegionalProfileRegistry.UNITED_STATES
                    )

                    quickPicks.forEach { quickProfile ->
                        val isCurrent = quickProfile.id == selectedProfile.id
                        Box(
                            modifier = Modifier
                                .clip(FuturisticTokens.CornerRadius.pillShape)
                                .background(if (isCurrent) FuturisticTheme.accent.copy(alpha = 0.12f) else FuturisticTheme.surface)
                                .border(
                                    1.dp,
                                    if (isCurrent) FuturisticTheme.accent else FuturisticTheme.border.copy(alpha = 0.6f),
                                    FuturisticTokens.CornerRadius.pillShape
                                )
                                .clickable { onSelectQuickProfile(quickProfile) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = quickProfile.flagEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = quickProfile.name,
                                    color = if (isCurrent) FuturisticTheme.accent else FuturisticTheme.primaryText,
                                    fontSize = 12.sp,
                                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Universal Knowledge Guarantee
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FuturisticTokens.CornerRadius.mediumShape)
                        .background(FuturisticTheme.surface)
                        .border(1.dp, FuturisticTheme.border.copy(alpha = 0.5f), FuturisticTokens.CornerRadius.mediumShape)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Universal AI knowledge: coding, science, history, and reasoning stay fully global across all regions.",
                            color = FuturisticTheme.secondaryText,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Primary button: Continue
                PrimaryPillButton(
                    text = "Continue",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onContinue,
                    height = 54.dp,
                    modifier = Modifier.fillMaxWidth(0.92f),
                    testTag = "first_launch_continue_button"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Secondary option: Choose another region
                Box(
                    modifier = Modifier
                        .clip(FuturisticTokens.CornerRadius.pillShape)
                        .clickable(onClick = onChangeRegion)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .testTag("first_launch_choose_region_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = FuturisticTheme.secondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Choose another region",
                            color = FuturisticTheme.secondaryText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
