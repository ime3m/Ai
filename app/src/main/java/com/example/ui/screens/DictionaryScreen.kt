package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DictionaryEntryEntity
import com.example.ui.VoiceViewModel
import com.example.ui.components.ExplainSlangDialog
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.VioletAccent

@Composable
fun DictionaryScreen(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.dictionaryEntries.collectAsState()
    val selectedExpression by viewModel.selectedExpressionForDetails.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    if (selectedExpression != null) {
        ExplainSlangDialog(
            expression = selectedExpression!!,
            onSpeak = { viewModel.speakText(it) },
            onDismiss = { viewModel.dismissExpressionDetails() }
        )
    }

    if (showAddDialog) {
        AddDictionaryEntryDialog(
            onAdd = { word, meaning, region, example, formal, cat ->
                viewModel.addDictionaryEntry(word, meaning, region, example, formal, cat)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    val categories = listOf("All", "User Added", "Friendly", "Casual", "Street Slang", "Humorous", "Affectionate")

    val filtered = remember(entries, searchQuery, selectedCategoryFilter) {
        entries.filter { entry ->
            val matchQuery = searchQuery.isBlank() ||
                    entry.expression.contains(searchQuery, ignoreCase = true) ||
                    entry.meaning.contains(searchQuery, ignoreCase = true) ||
                    entry.region.contains(searchQuery, ignoreCase = true)

            val matchCategory = when (selectedCategoryFilter) {
                "All" -> true
                "User Added" -> entry.isUserContributed
                else -> entry.category.equals(selectedCategoryFilter, ignoreCase = true)
            }
            matchQuery && matchCategory
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CyanPrimary,
                modifier = Modifier.testTag("add_slang_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add regional expression"
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = "Dictionary",
                    tint = CyanPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Regional Slang Dictionary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Verified community & regional expressions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search slang words, meanings, or regions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_dictionary_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${filtered.size} Expressions Found",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dictionary List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { entry ->
                    DictionaryEntryCard(
                        entry = entry,
                        onPlayAudio = { viewModel.speakText(entry.expression) },
                        onTapDetails = { viewModel.inspectExpression(entry.expression) },
                        onDelete = if (entry.isUserContributed) {
                            { viewModel.deleteDictionaryEntry(entry) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
fun DictionaryEntryCard(
    entry: DictionaryEntryEntity,
    onPlayAudio: () -> Unit,
    onTapDetails: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTapDetails),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.expression,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = VioletAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = entry.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = VioletAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = entry.region,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onPlayAudio) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Pronounce",
                            tint = CyanPrimary
                        )
                    }

                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = entry.meaning,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            if (entry.exampleSentence.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "“${entry.exampleSentence}”",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            if (entry.formalEquivalent.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Standard: ${entry.formalEquivalent}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AddDictionaryEntryDialog(
    onAdd: (word: String, meaning: String, region: String, example: String, formal: String, category: String) -> Unit,
    onDismiss: () -> Unit
) {
    var word by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var formal by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Casual") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add Regional Expression", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text("Expression / Slang Word *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = meaning,
                    onValueChange = { meaning = it },
                    label = { Text("Meaning *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = region,
                    onValueChange = { region = it },
                    label = { Text("Region / City (e.g. Kozhikode, Liverpool) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = example,
                    onValueChange = { example = it },
                    label = { Text("Example Sentence") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = formal,
                    onValueChange = { formal = it },
                    label = { Text("Standard-Language Equivalent") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (word.isNotBlank() && meaning.isNotBlank() && region.isNotBlank()) {
                        onAdd(word.trim(), meaning.trim(), region.trim(), example.trim(), formal.trim(), category)
                    }
                }
            ) {
                Text("Add Expression")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
