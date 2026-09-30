package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemoryItemEntity
import com.example.ui.VoiceViewModel
import com.example.ui.theme.FuturisticTheme
import com.example.ui.theme.FuturisticTokens

@Composable
fun MemoryScreen(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val memories by viewModel.allMemories.collectAsState()
    val isMemoryEnabled by viewModel.isMemoryEnabled.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FuturisticTheme.surface)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(FuturisticTheme.accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = FuturisticTheme.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AI Personal Memory",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FuturisticTheme.primaryText
                    )
                    Text(
                        text = "Manage what your regional assistant remembers",
                        fontSize = 12.sp,
                        color = FuturisticTheme.secondaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy & Transparency Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(FuturisticTheme.surfaceElevated)
                .border(1.dp, FuturisticTheme.border, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = FuturisticTheme.accent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Stored 100% locally on your device. Used only to personalize responses when you want. You can edit, delete, or turn memory off anytime.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = FuturisticTheme.secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Master Switch Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(FuturisticTheme.surfaceElevated)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Enable AI Memory",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FuturisticTheme.primaryText
                    )
                    Text(
                        text = if (isMemoryEnabled) "Assistant uses remembered preferences" else "Memory is paused",
                        fontSize = 12.sp,
                        color = FuturisticTheme.secondaryText
                    )
                }

                Switch(
                    checked = isMemoryEnabled,
                    onCheckedChange = { viewModel.toggleMasterMemory(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = FuturisticTheme.accent,
                        uncheckedTrackColor = FuturisticTheme.border
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons Row: Add Memory & Clear All
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Remembered Items (${memories.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = FuturisticTheme.primaryText
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (memories.isNotEmpty()) {
                    TextButton(onClick = { showClearConfirm = true }) {
                        Text("Clear All", fontSize = 12.sp, color = Color.Red.copy(alpha = 0.8f))
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = FuturisticTokens.CornerRadius.pillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = FuturisticTheme.accent),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Memory", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Memory List
        if (memories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = FuturisticTheme.border,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No memories saved yet",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = FuturisticTheme.secondaryText
                    )
                    Text(
                        text = "Tell the AI \"Remember that I prefer Malayalam\" or tap Add Memory above",
                        fontSize = 11.sp,
                        color = FuturisticTheme.tertiaryText
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(memories, key = { it.id }) { memory ->
                    MemoryItemCard(
                        memory = memory,
                        onToggle = { viewModel.toggleMemory(memory.id, !memory.isEnabled) },
                        onDelete = { viewModel.deleteMemory(memory.id) }
                    )
                }
            }
        }
    }

    // Add Memory Dialog
    if (showAddDialog) {
        var keyInput by remember { mutableStateOf("") }
        var valueInput by remember { mutableStateOf("") }
        var categoryInput by remember { mutableStateOf("Preference") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("Add to AI Memory", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FuturisticTheme.primaryText)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Specify a preference or note for the AI to remember:",
                        fontSize = 12.sp,
                        color = FuturisticTheme.secondaryText
                    )

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        placeholder = { Text("e.g. Preferred Language, Name, Style", fontSize = 12.sp) },
                        label = { Text("Topic / Key") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = valueInput,
                        onValueChange = { valueInput = it },
                        placeholder = { Text("e.g. Always explain in simple Malayalam", fontSize = 12.sp) },
                        label = { Text("Details / Note") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (valueInput.isNotBlank()) {
                            viewModel.addMemory(
                                key = keyInput.ifBlank { "Preference" },
                                value = valueInput,
                                category = categoryInput
                            )
                            showAddDialog = false
                            Toast.makeText(context, "Memory saved!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FuturisticTheme.accent),
                    shape = FuturisticTokens.CornerRadius.pillShape
                ) {
                    Text("Save", fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = FuturisticTheme.secondaryText)
                }
            },
            containerColor = FuturisticTheme.surface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Clear All Confirmation
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Memories?", fontWeight = FontWeight.Bold, color = FuturisticTheme.primaryText) },
            text = { Text("This will remove all stored personal memory notes. You can always add new ones.", fontSize = 13.sp, color = FuturisticTheme.secondaryText) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllMemories()
                        showClearConfirm = false
                        Toast.makeText(context, "All memories cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = FuturisticTokens.CornerRadius.pillShape
                ) {
                    Text("Clear All", fontSize = 13.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = FuturisticTheme.secondaryText)
                }
            },
            containerColor = FuturisticTheme.surface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
fun MemoryItemCard(
    memory: MemoryItemEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FuturisticTheme.surfaceElevated)
            .border(1.dp, FuturisticTheme.border, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(FuturisticTheme.accent.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = memory.key,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FuturisticTheme.accent
                        )
                    }
                    if (!memory.isEnabled) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "(Paused)", fontSize = 10.sp, color = FuturisticTheme.tertiaryText)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = memory.value,
                    fontSize = 13.sp,
                    color = if (memory.isEnabled) FuturisticTheme.primaryText else FuturisticTheme.tertiaryText,
                    lineHeight = 18.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FuturisticTheme.secondaryText, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
