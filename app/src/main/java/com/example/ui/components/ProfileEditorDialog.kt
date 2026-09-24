package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Person
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
import com.example.data.model.VoicePersonality
import com.example.data.model.VoiceProfileEntity
import com.example.data.repository.DialectCatalog
import com.example.ui.theme.CyanPrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileEditorDialog(
    profileToEdit: VoiceProfileEntity?,
    onSaveProfile: (VoiceProfileEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isNew = profileToEdit == null
    val defaultDialect = DialectCatalog.dialects.first()

    var profileName by remember { mutableStateOf(profileToEdit?.profileName ?: "My Custom Regional Voice") }
    var selectedDialectId by remember { mutableStateOf(profileToEdit?.dialectId ?: defaultDialect.id) }
    var strength by remember { mutableFloatStateOf(profileToEdit?.regionalStrength ?: 0.75f) }
    var selectedPersonality by remember { mutableStateOf(profileToEdit?.personality ?: VoicePersonality.FRIENDLY.name) }
    var responseStyle by remember { mutableStateOf(profileToEdit?.responseStyle ?: "Casual") }
    var slangEnabled by remember { mutableStateOf(profileToEdit?.slangEnabled ?: true) }
    var setAsDefault by remember { mutableStateOf(profileToEdit?.isDefault ?: false) }

    val currentDialect = DialectCatalog.getDialectById(selectedDialectId)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = Modifier.testTag("profile_editor_sheet")
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
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = CyanPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isNew) "Create Regional Profile" else "Edit Regional Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Layer Language, Dialect, Personality & Slang",
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

            // Profile Name Input
            OutlinedTextField(
                value = profileName,
                onValueChange = { profileName = it },
                label = { Text("Profile Name") },
                placeholder = { Text("e.g. My Kozhikode Mate, Liverpool Lad") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dialect Selector Pills
            Text(
                text = "Select Dialect / Speaking Style",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DialectCatalog.dialects.forEach { dialect ->
                    FilterChip(
                        selected = dialect.id == selectedDialectId,
                        onClick = { selectedDialectId = dialect.id },
                        label = { Text("${dialect.flagEmoji} ${dialect.dialectName}") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Regional Strength Slider
            Text(
                text = "Regional Strength: ${(strength * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = strength,
                onValueChange = { strength = it },
                valueRange = 0.0f..1.0f
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Standard", style = MaterialTheme.typography.labelSmall)
                Text("Strong Regional", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Voice Personality Selector
            Text(
                text = "Voice Personality (Layered with Dialect)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoicePersonality.entries.forEach { personality ->
                    FilterChip(
                        selected = personality.name == selectedPersonality,
                        onClick = { selectedPersonality = personality.name },
                        label = { Text("${personality.emoji} ${personality.title}") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Slang Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Regional Slang & Expressions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (slangEnabled) "Natural slang and idioms enabled" else "Polite regional vocabulary only",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = slangEnabled,
                        onCheckedChange = { slangEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Set as Default Profile
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Set as Default Profile",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Opens automatically on app launch",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = setAsDefault,
                        onCheckedChange = { setAsDefault = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val entity = VoiceProfileEntity(
                            id = profileToEdit?.id ?: 0L,
                            profileName = profileName.trim().ifEmpty { currentDialect.dialectName },
                            dialectId = currentDialect.id,
                            language = currentDialect.language,
                            country = currentDialect.country,
                            region = currentDialect.region,
                            cityOrArea = currentDialect.cityOrArea,
                            dialectName = currentDialect.dialectName,
                            regionalStrength = strength,
                            personality = selectedPersonality,
                            responseStyle = responseStyle,
                            slangEnabled = slangEnabled,
                            isDefault = setAsDefault
                        )
                        onSaveProfile(entity)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("save_profile_button")
                ) {
                    Text(if (isNew) "Create Profile" else "Save Changes")
                }
            }
        }
    }
}
