package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.RegionalDialect
import com.example.data.repository.DialectCatalog
import com.example.ui.VoiceViewModel
import com.example.ui.components.ExplainSlangDialog
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.VioletAccent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreDialectsScreen(
    viewModel: VoiceViewModel,
    onStartVoiceChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLanguageFilter by remember { mutableStateOf("All") }

    val currentDialect by viewModel.currentDialect.collectAsState()
    val selectedExpression by viewModel.selectedExpressionForDetails.collectAsState()

    if (selectedExpression != null) {
        ExplainSlangDialog(
            expression = selectedExpression!!,
            onSpeak = { viewModel.speakText(it) },
            onDismiss = { viewModel.dismissExpressionDetails() }
        )
    }

    val allLanguages = listOf("All") + DialectCatalog.getAllLanguages()

    val filteredDialects = remember(searchQuery, selectedLanguageFilter) {
        val base = if (searchQuery.isNotBlank()) {
            DialectCatalog.searchDialects(searchQuery)
        } else {
            DialectCatalog.dialects
        }
        if (selectedLanguageFilter == "All") {
            base
        } else {
            base.filter { it.language.equals(selectedLanguageFilter, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = "Explore",
                tint = CyanPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Discover Regional Dialects",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Explore speaking styles, slang, and cultural cadence",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search city, country, dialect, or slang...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("explore_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Language Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allLanguages) { lang ->
                FilterChip(
                    selected = selectedLanguageFilter == lang,
                    onClick = { selectedLanguageFilter = lang },
                    label = { Text(lang) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Showing ${filteredDialects.size} Dialect & Slang Regions",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Dialects List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredDialects) { dialect ->
                ExploreDialectCard(
                    dialect = dialect,
                    isCurrent = dialect.id == currentDialect.id,
                    onSelectAndTalk = {
                        viewModel.selectDialect(dialect)
                        onStartVoiceChat()
                    },
                    onPlayGreeting = {
                        viewModel.speakText(dialect.greeting)
                    },
                    onInspectSlang = { slang ->
                        viewModel.inspectExpression(slang)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreDialectCard(
    dialect: RegionalDialect,
    isCurrent: Boolean,
    onSelectAndTalk: () -> Unit,
    onPlayGreeting: () -> Unit,
    onInspectSlang: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Flag, Name, Region, Greeting Audio Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = dialect.flagEmoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = dialect.dialectName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${dialect.cityOrArea} • ${dialect.region}, ${dialect.country}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onPlayGreeting,
                    modifier = Modifier.testTag("listen_dialect_sample_${dialect.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Listen to native greeting",
                        tint = CyanPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Linguistic description
            Text(
                text = dialect.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Code-switching & English mixing habit
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Mixing style: ${dialect.codeSwitchingDescription}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Typical Slang Pills
            if (dialect.typicalExpressions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Featured Slang (Tap to learn):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    dialect.typicalExpressions.forEach { expr ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = VioletAccent.copy(alpha = 0.15f),
                            modifier = Modifier.clickable { onInspectSlang(expr.expression) }
                        ) {
                            Text(
                                text = expr.expression,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = VioletAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            Button(
                onClick = onSelectAndTalk,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("select_and_talk_${dialect.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrent) CyanPrimary else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = "Talk")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCurrent) "Continue Talking in this Dialect" else "Start Voice Chat in ${dialect.cityOrArea}",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
