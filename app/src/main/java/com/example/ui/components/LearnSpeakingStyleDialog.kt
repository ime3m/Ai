package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.SpeakingStylePreferenceEntity
import com.example.ui.theme.CyanPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnSpeakingStyleDialog(
    initialStyle: SpeakingStylePreferenceEntity?,
    onSave: (SpeakingStylePreferenceEntity) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val style = initialStyle ?: SpeakingStylePreferenceEntity()

    var learningEnabled by remember { mutableStateOf(style.learningEnabled) }
    var frequentlyUsedExpressions by remember { mutableStateOf(style.frequentlyUsedExpressionsCsv) }
    var preferredSlang by remember { mutableStateOf(style.preferredSlangCsv) }
    var formalityLevel by remember { mutableFloatStateOf(style.formalityLevel) }
    var sentenceStyle by remember { mutableStateOf(style.sentenceStyle) }
    var preferredResponseLength by remember { mutableStateOf(style.preferredResponseLength) }
    var preferredTone by remember { mutableStateOf(style.preferredTone) }
    var codeSwitchingHabit by remember { mutableStateOf(style.codeSwitchingHabit) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = Modifier.testTag("learn_my_style_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Privacy Protected",
                        tint = CyanPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Learn My Speaking Style",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Privacy-first personalized conversational style",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Opt-In Switch Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (learningEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
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
                            text = "Enable Speaking Style Learning",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (learningEnabled) "Personalization active. The AI adapts to your rhythm." else "Disabled. Responses follow standard regional baseline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = learningEnabled,
                        onCheckedChange = { learningEnabled = it },
                        modifier = Modifier.testTag("toggle_learning_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Formality Level Slider
            Text(
                text = "Communication Formality Level",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = formalityLevel,
                onValueChange = { formalityLevel = it },
                valueRange = 0.0f..1.0f
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ultra Casual (Banter)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Polite Formal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preferred Response Length
            Text(
                text = "Preferred Response Length",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Short & Punchy", "Medium", "Expressive & Detailed").forEach { length ->
                    FilterChip(
                        selected = preferredResponseLength == length,
                        onClick = { preferredResponseLength = length },
                        label = { Text(length) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preferred Tone
            Text(
                text = "Preferred Conversational Tone",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Warm & banter", "Respectful & calm", "Lively & energetic").forEach { tone ->
                    FilterChip(
                        selected = preferredTone == tone,
                        onClick = { preferredTone = tone },
                        label = { Text(tone) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Code Switching / Mixed Language Habit
            Text(
                text = "Code-Switching & Language Mixing Habit",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "Natural mixing with English (e.g. Manglish / Hinglish / Arabizi)",
                    "Moderate loanwords only",
                    "Pure regional language"
                ).forEach { habit ->
                    FilterChip(
                        selected = codeSwitchingHabit == habit,
                        onClick = { codeSwitchingHabit = habit },
                        label = { Text(habit) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Frequently Used Expressions
            OutlinedTextField(
                value = frequentlyUsedExpressions,
                onValueChange = { frequentlyUsedExpressions = it },
                label = { Text("Frequently Used Expressions (Comma-separated)") },
                placeholder = { Text("e.g. sound lad, deadass, scene kya hai, എന്തൂട്ടാ") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_frequent_expressions_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Preferred Slang Words
            OutlinedTextField(
                value = preferredSlang,
                onValueChange = { preferredSlang = it },
                label = { Text("Preferred Slang & Catchphrases") },
                placeholder = { Text("e.g. changayi, sulaimani, bantai, quillo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_preferred_slang_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy Guarantee Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Privacy Guarantee",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Preferences are stored strictly on-device in Room Database. You can edit, reset, or delete your preferences at any time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: Save & Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onReset()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reset_style_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }

                Button(
                    onClick = {
                        val updated = style.copy(
                            learningEnabled = learningEnabled,
                            frequentlyUsedExpressionsCsv = frequentlyUsedExpressions,
                            preferredSlangCsv = preferredSlang,
                            formalityLevel = formalityLevel,
                            sentenceStyle = sentenceStyle,
                            preferredResponseLength = preferredResponseLength,
                            preferredTone = preferredTone,
                            codeSwitchingHabit = codeSwitchingHabit
                        )
                        onSave(updated)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("save_style_button")
                ) {
                    Text("Save Preferences")
                }
            }
        }
    }
}
