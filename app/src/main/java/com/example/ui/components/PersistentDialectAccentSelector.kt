package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RegionalDialect
import com.example.data.repository.DialectCatalog
import com.example.ui.theme.FuturisticTheme
import com.example.ui.theme.FuturisticTokens

/**
 * Persistent UI Component for Selecting the Regional Dialect or Accent Before Starting a Voice Session.
 *
 * Placed persistently above the floating AI input bar on the Home screen and in conversation views.
 * Allows instant, one-tap switching between Kerala regional dialects and international accents,
 * provides voice audition/preview of the selected dialect greeting, and opens the full 4-level
 * hierarchical dialect drill-down modal for exploring all regions.
 */
@Composable
fun PersistentDialectAccentSelector(
    selectedDialect: RegionalDialect,
    onSelectDialect: (RegionalDialect) -> Unit,
    onOpenFullDialectPicker: () -> Unit,
    onAuditionVoice: (String) -> Unit,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Curated high-frequency Kerala regional dialects and popular international accents
    val quickPicks = remember {
        val kerala = listOf(
            "ml_in_kl_kozhikode",     // Kozhikode (Malabar)
            "ml_in_kl_thrissur",      // Thrissur (Central Kerala)
            "ml_in_kl_ernakulam",     // Kochi (Coastal)
            "ml_in_kl_kannur",        // Kannur (North Malabar)
            "ml_in_kl_trivandrum",    // Thiruvananthapuram (Travancore)
            "ml_in_kl_wayanad"        // Wayanad (Highland)
        ).mapNotNull { id -> DialectCatalog.dialects.find { it.id == id } }

        val international = listOf(
            "en_gb_eng_liverpool",    // Scouse (UK)
            "en_gb_eng_london",       // London RP (UK)
            "en_us_ny_newyork",       // New York (US)
            "ar_kw_kuwait"            // Kuwait / Gulf
        ).mapNotNull { id -> DialectCatalog.dialects.find { it.id == id } }

        kerala + international
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("persistent_dialect_accent_selector")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // =====================================================================
            // 1. PRIMARY PERSISTENT HEADER BAR
            // =====================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FuturisticTheme.surfaceElevated.copy(alpha = 0.95f))
                    .border(
                        width = 1.dp,
                        color = FuturisticTheme.border.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Active Dialect Badge with Emoji & Name (Click to expand quick details)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { isExpanded = !isExpanded }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("active_dialect_badge")
                    ) {
                        // Flag/Emoji with pulsing ready indicator
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(FuturisticTheme.surface)
                                .border(0.5.dp, FuturisticTheme.border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedDialect.flagEmoji.ifBlank { "🌴" },
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedDialect.dialectName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FuturisticTheme.primaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand details",
                                    tint = FuturisticTheme.tertiaryText,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "${selectedDialect.cityOrArea} • ${selectedDialect.region}",
                                fontSize = 10.sp,
                                color = FuturisticTheme.secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Right Actions: Audition Greeting + All Accents button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 🔊 Audition / Preview Voice Button
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(FuturisticTheme.surface)
                                .border(0.5.dp, FuturisticTheme.border, CircleShape)
                                .clickable {
                                    val greetingToSpeak = selectedDialect.greeting.ifBlank { "നമസ്കാരം!" }
                                    onAuditionVoice(greetingToSpeak)
                                }
                                .testTag("btn_audition_accent_voice"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Preview accent voice",
                                tint = FuturisticTheme.accent,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // 🌐 "All Accents" Picker Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(FuturisticTheme.accent.copy(alpha = 0.12f))
                                .border(
                                    width = 0.8.dp,
                                    color = FuturisticTheme.accent.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { onOpenFullDialectPicker() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("btn_open_all_accents"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = FuturisticTheme.accent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "All Accents",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FuturisticTheme.accent
                                )
                            }
                        }
                    }
                }
            }

            // =====================================================================
            // 2. EXPANDED ACCENT PROFILE & SLANG PREVIEW CARD (Toggleable)
            // =====================================================================
            AnimatedVisibility(
                visible = isExpanded && !isCompact,
                enter = fadeIn(tween(180)) + expandVertically(tween(200)),
                exit = fadeOut(tween(140)) + shrinkVertically(tween(160))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FuturisticTheme.surface)
                        .border(0.5.dp, FuturisticTheme.border, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                        .testTag("expanded_accent_details_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACCENT SIGNATURE & GREETING",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = FuturisticTheme.tertiaryText,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Ready for voice",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF16A34A)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "\"${selectedDialect.greeting}\"",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = FuturisticTheme.primaryText,
                            lineHeight = 16.sp
                        )

                        // Sample slang expressions preview
                        if (selectedDialect.typicalExpressions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                selectedDialect.typicalExpressions.take(4).forEach { expr ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(FuturisticTheme.surfaceElevated)
                                            .border(0.5.dp, FuturisticTheme.border, RoundedCornerShape(6.dp))
                                            .clickable { onAuditionVoice(expr.expression) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = expr.expression,
                                            fontSize = 10.sp,
                                            color = FuturisticTheme.accent,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =====================================================================
            // 3. HORIZONTAL QUICK-SELECT ACCENT CAROUSEL (Visible when not compact)
            // =====================================================================
            if (!isCompact) {
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    quickPicks.forEach { dialect ->
                        val isSelected = dialect.id == selectedDialect.id
                        val chipBg by animateColorAsState(
                            targetValue = if (isSelected) FuturisticTheme.accent.copy(alpha = 0.2f) else FuturisticTheme.surfaceElevated,
                            label = "chipBg"
                        )
                        val chipBorder by animateColorAsState(
                            targetValue = if (isSelected) FuturisticTheme.accent else FuturisticTheme.border.copy(alpha = 0.7f),
                            label = "chipBorder"
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(chipBg)
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.8.dp,
                                    color = chipBorder,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { onSelectDialect(dialect) }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                                .testTag("dialect_chip_${dialect.id}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = dialect.flagEmoji.ifBlank { "🌴" },
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = dialect.cityOrArea,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FuturisticTheme.primaryText else FuturisticTheme.secondaryText
                                )
                            }
                        }
                    }

                    // "More Accents..." Pill at end of scroll
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(FuturisticTheme.surface)
                            .border(0.8.dp, FuturisticTheme.border, RoundedCornerShape(20.dp))
                            .clickable { onOpenFullDialectPicker() }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                            .testTag("dialect_chip_more")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "More...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = FuturisticTheme.tertiaryText
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = FuturisticTheme.tertiaryText,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
