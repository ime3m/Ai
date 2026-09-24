package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.data.model.DictionaryEntryEntity
import com.example.data.model.RegionalExpression
import com.example.data.repository.DialectCatalog
import com.example.ui.VoiceViewModel
import com.example.ui.components.ExplainSlangDialog

enum class DictionaryTab {
    DICTIONARY,
    TRANSLATOR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val entries by viewModel.dictionaryEntries.collectAsState()
    val selectedExpression by viewModel.selectedExpressionForDetails.collectAsState()
    val translationState by viewModel.slangTranslationState.collectAsState()

    var currentTab by remember { mutableStateOf(DictionaryTab.DICTIONARY) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRegionFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Distinct regions
    val regions = listOf("All") + entries.map { it.region }.distinct()

    val filteredEntries = entries.filter { entry ->
        val matchesQuery = searchQuery.isBlank() ||
                entry.expression.contains(searchQuery, ignoreCase = true) ||
                entry.meaning.contains(searchQuery, ignoreCase = true) ||
                entry.formalEquivalent.contains(searchQuery, ignoreCase = true)

        val matchesRegion = selectedRegionFilter == "All" || entry.region.equals(selectedRegionFilter, ignoreCase = true)

        matchesQuery && matchesRegion
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (currentTab == DictionaryTab.DICTIONARY) "Dictionary" else "Slang Translator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (currentTab == DictionaryTab.DICTIONARY)
                            "Regional expressions, vocabulary & colloquial usage"
                        else
                            "Convert standard speech to authentic regional dialects",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (currentTab == DictionaryTab.DICTIONARY) {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("add_dictionary_entry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add slang",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Segmented Tab Switch: [ Dictionary | Translator ]
            Row(
                modifier = Modifier
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(2.dp)
                    .testTag("dictionary_translator_segmented_toggle"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (currentTab == DictionaryTab.DICTIONARY) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                        .clickable { currentTab = DictionaryTab.DICTIONARY }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("tab_dictionary"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Dictionary",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (currentTab == DictionaryTab.DICTIONARY) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (currentTab == DictionaryTab.DICTIONARY) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (currentTab == DictionaryTab.TRANSLATOR) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                        .clickable { currentTab = DictionaryTab.TRANSLATOR }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("tab_translator"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Translator",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (currentTab == DictionaryTab.TRANSLATOR) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (currentTab == DictionaryTab.TRANSLATOR) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

        if (currentTab == DictionaryTab.DICTIONARY) {
            // SEARCH & REGION FILTERS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                // Minimal search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search slang, meaning, or standard phrase...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dictionary_search_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter by region
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(regions) { region ->
                        val isSelected = region == selectedRegionFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedRegionFilter = region }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = region,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

            // List of Slang Expressions (Minimal flat rows)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("dictionary_entries_list"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                items(filteredEntries, key = { it.id }) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                val expr = RegionalExpression(
                                    expression = entry.expression,
                                    meaning = entry.meaning,
                                    region = entry.region,
                                    dialect = entry.category,
                                    context = "Colloquial usage",
                                    formalEquivalent = entry.formalEquivalent,
                                    exampleSentence = entry.exampleSentence,
                                    toneCategory = entry.category,
                                    culturalNotes = "Regional expression"
                                )
                                viewModel.inspectExpression(expr)
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                            .testTag("dictionary_row_${entry.id}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = entry.expression,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${entry.category} · ${entry.region}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "“${entry.meaning}”",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (entry.formalEquivalent.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Standard: ${entry.formalEquivalent}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (entry.exampleSentence.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Example: ${entry.exampleSentence}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Pronunciation audio button
                        IconButton(
                            onClick = { viewModel.speakText(entry.expression) },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("listen_dictionary_entry_${entry.id}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Pronounce",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            // TRANSLATOR VIEW
            SlangTranslatorSection(
                viewModel = viewModel,
                onCopy = { text ->
                    clipboardManager.setText(AnnotatedString(text))
                    Toast.makeText(context, "Copied regional translation", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Detail dialog when clicked
    selectedExpression?.let { expr ->
        ExplainSlangDialog(
            expression = expr,
            onSpeak = { viewModel.speakText(it) },
            onDismiss = { viewModel.dismissExpressionDetails() }
        )
    }

    // Add Slang Sheet
    if (showAddDialog) {
        AddSlangSheet(
            onSave = { expr, meaning, region, dialect, formal, example ->
                viewModel.addDictionaryEntry(
                    word = expr,
                    meaning = meaning,
                    region = region,
                    exampleSentence = example,
                    formalEquivalent = formal,
                    category = dialect.ifBlank { "Slang" }
                )
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSlangSheet(
    onSave: (expression: String, meaning: String, region: String, dialect: String, formal: String, example: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var expression by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("Kerala") }
    var dialect by remember { mutableStateOf("Kozhikode Slang") }
    var formal by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("add_slang_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add regional slang",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = expression,
                onValueChange = { expression = it },
                placeholder = { Text("Expression or slang word", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_slang_expression"),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = meaning,
                onValueChange = { meaning = it },
                placeholder = { Text("Meaning / definition", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_slang_meaning"),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = region,
                    onValueChange = { region = it },
                    placeholder = { Text("Region", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = dialect,
                    onValueChange = { dialect = it },
                    placeholder = { Text("Dialect", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = formal,
                onValueChange = { formal = it },
                placeholder = { Text("Standard formal equivalent", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = example,
                onValueChange = { example = it },
                placeholder = { Text("Example sentence", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (expression.isNotBlank() && meaning.isNotBlank()) {
                        onSave(expression, meaning, region, dialect, formal, example)
                    }
                },
                enabled = expression.isNotBlank() && meaning.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_custom_slang_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Add expression", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SlangTranslatorSection(
    viewModel: VoiceViewModel,
    onCopy: (String) -> Unit
) {
    val translationState by viewModel.slangTranslationState.collectAsState()
    var showSourceMenu by remember { mutableStateOf(false) }
    var showTargetMenu by remember { mutableStateOf(false) }

    val allDialects = DialectCatalog.dialects
    val quickPhrases = listOf(
        "I'm going to have tea.",
        "Where are you going?",
        "This food was delicious.",
        "Why are you staring like that?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("slang_translator_container")
    ) {
        // Dialect Selector Row: From ─── [Swap] ─── To
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Source Dialect
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable { showSourceMenu = true }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        Text(
                            text = "From",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = translationState.sourceDialect,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }

                DropdownMenu(
                    expanded = showSourceMenu,
                    onDismissRequest = { showSourceMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Standard Malayalam") },
                        onClick = {
                            viewModel.setTranslationDialects("Standard Malayalam", translationState.targetDialect)
                            showSourceMenu = false
                        }
                    )
                    allDialects.take(8).forEach { dialect ->
                        DropdownMenuItem(
                            text = { Text("${dialect.dialectName} (${dialect.cityOrArea})") },
                            onClick = {
                                viewModel.setTranslationDialects(dialect.dialectName, translationState.targetDialect)
                                showSourceMenu = false
                            }
                        )
                    }
                }
            }

            // Swap Button
            IconButton(
                onClick = { viewModel.swapTranslationDialects() },
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(36.dp)
                    .testTag("swap_translation_dialects_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Swap dialects",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Target Dialect
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable { showTargetMenu = true }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        Text(
                            text = "To",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = translationState.targetDialect,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }

                DropdownMenu(
                    expanded = showTargetMenu,
                    onDismissRequest = { showTargetMenu = false }
                ) {
                    allDialects.forEach { dialect ->
                        DropdownMenuItem(
                            text = { Text("${dialect.dialectName} (${dialect.cityOrArea})") },
                            onClick = {
                                viewModel.setTranslationDialects(translationState.sourceDialect, dialect.dialectName)
                                showTargetMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Input Field
        OutlinedTextField(
            value = translationState.inputText,
            onValueChange = { viewModel.updateTranslationInput(it) },
            placeholder = {
                Text(
                    "Type or speak something...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("translator_input_field"),
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick suggestions
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(quickPhrases) { phrase ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            viewModel.updateTranslationInput(phrase)
                        }
                        .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = phrase,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Translate Button
        Button(
            onClick = { viewModel.translateCurrentInput() },
            enabled = translationState.inputText.isNotBlank() && !translationState.isTranslating,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("translate_action_button"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = if (translationState.isTranslating) "Translating..." else "Convert to regional",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Output Display Card
        translationState.result?.let { result ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .padding(14.dp)
                    .testTag("translation_result_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Regional version",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Listen button
                            IconButton(
                                onClick = { viewModel.speakText(result.convertedText) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("listen_translation_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Listen",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Copy button
                            IconButton(
                                onClick = { onCopy(result.convertedText) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("copy_translation_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = result.convertedText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (result.explanation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (result.substitutedSlang.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Substituted regional slang:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        result.substitutedSlang.forEach { item ->
                            Text(
                                text = "• ${item.originalWord} → ${item.regionalSlang} (${item.meaning})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

