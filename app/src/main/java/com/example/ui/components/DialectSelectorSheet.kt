package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RegionalDialect
import com.example.data.repository.DialectCatalog
import com.example.ui.theme.CyanPrimary

enum class SelectorStep {
    LANGUAGE,
    COUNTRY,
    REGION,
    CITY,
    DIALECT
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DialectSelectorSheet(
    sheetState: SheetState,
    selectedDialect: RegionalDialect,
    onSelectDialect: (RegionalDialect) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentStep by remember { mutableStateOf(SelectorStep.LANGUAGE) }

    var selectedLang by remember { mutableStateOf(selectedDialect.language) }
    var selectedCountry by remember { mutableStateOf(selectedDialect.country) }
    var selectedRegion by remember { mutableStateOf(selectedDialect.region) }
    var selectedCity by remember { mutableStateOf(selectedDialect.cityOrArea) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = Modifier.testTag("dialect_selector_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Dialect",
                        tint = CyanPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Regional Dialect Selector",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Language → Country → Region → City → Dialect",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar for quick jump
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_dialect_input"),
                placeholder = { Text("Search language, city, or slang (e.g. Kozhikode, Liverpool, Kuwait)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (searchQuery.isNotBlank()) {
                // Direct Search Results
                val searchResults = DialectCatalog.searchDialects(searchQuery)
                Text(
                    text = "Search Results (${searchResults.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(searchResults) { dialect ->
                        DialectItemCard(
                            dialect = dialect,
                            isSelected = dialect.id == selectedDialect.id,
                            onClick = {
                                onSelectDialect(dialect)
                                onDismiss()
                            }
                        )
                    }
                }
            } else {
                // Breadcrumbs
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = currentStep == SelectorStep.LANGUAGE,
                        onClick = { currentStep = SelectorStep.LANGUAGE },
                        label = { Text("1. $selectedLang") }
                    )
                    FilterChip(
                        selected = currentStep == SelectorStep.COUNTRY,
                        onClick = { currentStep = SelectorStep.COUNTRY },
                        label = { Text("2. $selectedCountry") }
                    )
                    FilterChip(
                        selected = currentStep == SelectorStep.REGION,
                        onClick = { currentStep = SelectorStep.REGION },
                        label = { Text("3. $selectedRegion") }
                    )
                    FilterChip(
                        selected = currentStep == SelectorStep.CITY,
                        onClick = { currentStep = SelectorStep.CITY },
                        label = { Text("4. $selectedCity") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hierarchical Step Content
                when (currentStep) {
                    SelectorStep.LANGUAGE -> {
                        Text(
                            text = "Step 1: Choose Language",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val languages = DialectCatalog.getAllLanguages()
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(languages) { lang ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedLang = lang
                                            selectedCountry = DialectCatalog.getCountriesForLanguage(lang).firstOrNull() ?: ""
                                            selectedRegion = DialectCatalog.getRegionsForCountry(selectedCountry).firstOrNull() ?: ""
                                            selectedCity = DialectCatalog.getCitiesForRegion(selectedRegion).firstOrNull() ?: ""
                                            currentStep = SelectorStep.COUNTRY
                                        },
                                    color = if (selectedLang == lang) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = lang, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                                    }
                                }
                            }
                        }
                    }

                    SelectorStep.COUNTRY -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { currentStep = SelectorStep.LANGUAGE }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Text(
                                text = "Step 2: Choose Country for $selectedLang",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val countries = DialectCatalog.getCountriesForLanguage(selectedLang)
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(countries) { country ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedCountry = country
                                            selectedRegion = DialectCatalog.getRegionsForCountry(country).firstOrNull() ?: ""
                                            selectedCity = DialectCatalog.getCitiesForRegion(selectedRegion).firstOrNull() ?: ""
                                            currentStep = SelectorStep.REGION
                                        },
                                    color = if (selectedCountry == country) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = country, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                                    }
                                }
                            }
                        }
                    }

                    SelectorStep.REGION -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { currentStep = SelectorStep.COUNTRY }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Text(
                                text = "Step 3: Choose State/Province in $selectedCountry",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val regions = DialectCatalog.getRegionsForCountry(selectedCountry)
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(regions) { region ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedRegion = region
                                            selectedCity = DialectCatalog.getCitiesForRegion(region).firstOrNull() ?: ""
                                            currentStep = SelectorStep.CITY
                                        },
                                    color = if (selectedRegion == region) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = region, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                                    }
                                }
                            }
                        }
                    }

                    SelectorStep.CITY -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { currentStep = SelectorStep.REGION }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Text(
                                text = "Step 4: Choose City / Area in $selectedRegion",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val cities = DialectCatalog.getCitiesForRegion(selectedRegion)
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(cities) { city ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedCity = city
                                            currentStep = SelectorStep.DIALECT
                                        },
                                    color = if (selectedCity == city) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = city, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                                    }
                                }
                            }
                        }
                    }

                    SelectorStep.DIALECT -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { currentStep = SelectorStep.CITY }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Text(
                                text = "Step 5: Select Dialect & Slang for $selectedCity",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val dialects = DialectCatalog.getDialectsForCity(selectedCity)
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(dialects) { dialect ->
                                DialectItemCard(
                                    dialect = dialect,
                                    isSelected = dialect.id == selectedDialect.id,
                                    onClick = {
                                        onSelectDialect(dialect)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialectItemCard(
    dialect: RegionalDialect,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = dialect.flagEmoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
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
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = CyanPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dialect.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (dialect.typicalExpressions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    dialect.typicalExpressions.take(2).forEach { expr ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = expr.expression,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
