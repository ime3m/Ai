package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversationMode
import com.example.data.model.VoicePersonality
import com.example.data.repository.DialectCatalog
import com.example.ui.VoiceViewModel
import com.example.ui.components.DialectSelectorSheet
import com.example.ui.components.ExplainSlangDialog
import com.example.ui.components.LearnSpeakingStyleDialog
import com.example.ui.components.ProfileEditorDialog
import com.example.ui.components.RegionalStrengthSlider
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.VioletAccent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: VoiceViewModel,
    onNavigateToVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDialect by viewModel.currentDialect.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val speakingStyle by viewModel.speakingStyle.collectAsState()
    val selectedExpression by viewModel.selectedExpressionForDetails.collectAsState()

    var showDialectSheet by remember { mutableStateOf(false) }
    var showLearnStyleDialog by remember { mutableStateOf(false) }
    var showProfileEditor by remember { mutableStateOf(false) }

    val dialectSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showDialectSheet) {
        DialectSelectorSheet(
            sheetState = dialectSheetState,
            selectedDialect = currentDialect,
            onSelectDialect = { viewModel.selectDialect(it) },
            onDismiss = { showDialectSheet = false }
        )
    }

    if (showLearnStyleDialog) {
        LearnSpeakingStyleDialog(
            initialStyle = speakingStyle,
            onSave = { viewModel.updateSpeakingStylePreferences(it) },
            onReset = { viewModel.resetSpeakingStylePreferences() },
            onDismiss = { showLearnStyleDialog = false }
        )
    }

    if (showProfileEditor) {
        ProfileEditorDialog(
            profileToEdit = currentProfile,
            onSaveProfile = { viewModel.createOrUpdateProfile(it) },
            onDismiss = { showProfileEditor = false }
        )
    }

    if (selectedExpression != null) {
        ExplainSlangDialog(
            expression = selectedExpression!!,
            onSpeak = { viewModel.speakText(it) },
            onDismiss = { viewModel.dismissExpressionDetails() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Regional Voice AI",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Authentic regional voices, dialects & slang",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { showLearnStyleDialog = true },
                modifier = Modifier.testTag("home_speaking_style_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Speaking Style Settings",
                    tint = CyanPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hero: Active Dialect Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "ACTIVE REGIONAL VOICE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.speakText(currentDialect.greeting) },
                        modifier = Modifier.testTag("hear_greeting_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Hear dialect greeting",
                            tint = CyanPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentDialect.flagEmoji,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = currentDialect.dialectName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${currentDialect.cityOrArea} • ${currentDialect.region}, ${currentDialect.country}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "“${currentDialect.greeting}”",
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToVoice,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("start_talking_hero_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Mic")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Talking")
                    }

                    OutlinedButton(
                        onClick = { showDialectSheet = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("change_dialect_hero_button")
                    ) {
                        Text("Change")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Regional Style Slider (Section 3)
        RegionalStrengthSlider(
            strength = currentProfile?.regionalStrength ?: 0.75f,
            onStrengthChange = { newStrength ->
                viewModel.updateRegionalStrength(newStrength)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Mode Cards: Free Voice, Dialect Practice, Slang Translator
        Text(
            text = "Conversation Modes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModeCard(
                title = "Voice Chat",
                subtitle = "Natural AI talking partner",
                icon = Icons.Default.Hearing,
                iconTint = CyanPrimary,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        viewModel.setConversationMode(ConversationMode.FREE_CONVERSATION)
                        onNavigateToVoice()
                    }
                    .testTag("mode_voice_chat_card")
            )

            ModeCard(
                title = "Practice",
                subtitle = "Voice learning & accent tips",
                icon = Icons.Default.School,
                iconTint = VioletAccent,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        viewModel.setConversationMode(ConversationMode.DIALECT_PRACTICE)
                        onNavigateToVoice()
                    }
                    .testTag("mode_practice_card")
            )

            ModeCard(
                title = "Translate",
                subtitle = "Convert speech & slang",
                icon = Icons.Default.SwapHoriz,
                iconTint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        viewModel.setConversationMode(ConversationMode.SLANG_TRANSLATOR)
                        onNavigateToVoice()
                    }
                    .testTag("mode_translate_card")
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Voice Personality Selector (Section 9)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Voice Personality",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Layered with ${currentDialect.dialectName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(onClick = { showProfileEditor = true }) {
                        Text("Edit Profile")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VoicePersonality.entries.forEach { personality ->
                        val isSelected = (currentProfile?.personality ?: VoicePersonality.FRIENDLY.name) == personality.name
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updatePersonality(personality) },
                            label = { Text("${personality.emoji} ${personality.title}") }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Featured Slang for Current Region (Section 4 & 7)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentDialect.cityOrArea} Slang Expressions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tap to explain",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            currentDialect.typicalExpressions.forEach { expr ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.inspectExpression(expr.expression) }
                        .testTag("slang_item_${expr.expression}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = expr.expression,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = VioletAccent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = expr.toneCategory,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VioletAccent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = expr.meaning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { viewModel.speakText(expr.expression) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Pronounce",
                                tint = CyanPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Popular Regions Quick Access Carousel
        Text(
            text = "Popular Regional Dialects",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DialectCatalog.dialects.take(4).forEach { dialect ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectDialect(dialect)
                            onNavigateToVoice()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (dialect.id == currentDialect.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
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
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = dialect.flagEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = dialect.dialectName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${dialect.cityOrArea} • ${dialect.country}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Select",
                            tint = CyanPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun ModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}
