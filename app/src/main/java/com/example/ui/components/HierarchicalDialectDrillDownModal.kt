package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RegionalDialect
import com.example.data.repository.DialectCatalog

/**
 * 4-Level Regional Hierarchy:
 * 1. Language (Malayalam, English, Arabic, Spanish)
 * 2. Country (India, UK, US, Australia, Ireland, Kuwait, UAE, Saudi Arabia, Egypt, Spain)
 * 3. State / Province (Kerala, England, Scotland, New York, New South Wales, Leinster, Dubai, Riyadh, etc.)
 * 4. District / Dialect (14 Kerala districts: Kozhikode, Malappuram, Thrissur...; Liverpool, Brooklyn...)
 */
enum class HierarchyLevel(val levelIndex: Int, val title: String, val icon: ImageVector) {
    LANGUAGE(1, "Language", Icons.Default.Language),
    COUNTRY(2, "Country", Icons.Default.Public),
    STATE(3, "State / Province", Icons.Default.Map),
    DISTRICT(4, "District / Dialect", Icons.Default.Place)
}

/**
 * Interactive Modal Bottom Sheet allowing users to drill down from:
 * Language -> Country -> State -> District / Dialect
 * Triggered from the dynamic header in ChatScreen & VoiceConversationScreen.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HierarchicalDialectDrillDownModal(
    sheetState: SheetState,
    selectedDialect: RegionalDialect,
    onSelectDialect: (RegionalDialect) -> Unit,
    onPreviewAudio: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var currentLevel by remember { mutableStateOf(HierarchyLevel.LANGUAGE) }
    var selectedLanguage by remember { mutableStateOf(selectedDialect.language) }
    var selectedCountry by remember { mutableStateOf(selectedDialect.country) }
    var selectedState by remember { mutableStateOf(selectedDialect.stateOrProvince) }
    var searchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("hierarchical_drilldown_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ---------------------------------------------------------
            // 1. MODAL TITLE BAR & CLOSE ACTION
            // ---------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentLevel != HierarchyLevel.LANGUAGE && searchQuery.isBlank()) {
                        IconButton(
                            onClick = {
                                currentLevel = when (currentLevel) {
                                    HierarchyLevel.DISTRICT -> HierarchyLevel.STATE
                                    HierarchyLevel.STATE -> HierarchyLevel.COUNTRY
                                    HierarchyLevel.COUNTRY -> HierarchyLevel.LANGUAGE
                                    HierarchyLevel.LANGUAGE -> HierarchyLevel.LANGUAGE
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("drilldown_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Column {
                        Text(
                            text = when {
                                searchQuery.isNotBlank() -> "Search Dialects"
                                currentLevel == HierarchyLevel.LANGUAGE -> "Step 1: Choose Language"
                                currentLevel == HierarchyLevel.COUNTRY -> "Step 2: Choose Country ($selectedLanguage)"
                                currentLevel == HierarchyLevel.STATE -> "Step 3: Choose State ($selectedCountry)"
                                currentLevel == HierarchyLevel.DISTRICT -> "Step 4: Choose District ($selectedState)"
                                else -> "Select Regional Dialect"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Regional hierarchy: Language > Country > State > District",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("drilldown_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---------------------------------------------------------
            // 2. SEARCH & QUICK FIND BAR
            // ---------------------------------------------------------
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search any district, city, state, or language...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("drilldown_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ---------------------------------------------------------
            // 3. INTERACTIVE BREADCRUMB NAVIGATION BAR
            // ---------------------------------------------------------
            if (searchQuery.isBlank()) {
                InteractiveBreadcrumbBar(
                    currentLevel = currentLevel,
                    selectedLanguage = selectedLanguage,
                    selectedCountry = selectedCountry,
                    selectedState = selectedState,
                    onNavigateToLevel = { targetLevel ->
                        currentLevel = targetLevel
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ---------------------------------------------------------
            // 4. MAIN CONTENT CONTAINER (FLAT SEARCH OR DRILL-DOWN)
            // ---------------------------------------------------------
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (searchQuery.isNotBlank()) {
                    // Global search results across all levels
                    val searchResults = DialectCatalog.searchDialects(searchQuery)
                    if (searchResults.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No dialects matching “$searchQuery”",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("drilldown_search_results_list"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults, key = { it.id }) { dialect ->
                                DistrictDialectItem(
                                    dialect = dialect,
                                    isSelected = dialect.id == selectedDialect.id,
                                    onSelect = {
                                        onSelectDialect(dialect)
                                        onDismiss()
                                    },
                                    onPreviewAudio = onPreviewAudio
                                )
                            }
                        }
                    }
                } else {
                    // Hierarchical Drill-down with Animated Transitions
                    AnimatedContent(
                        targetState = currentLevel,
                        transitionSpec = {
                            if (targetState.levelIndex > initialState.levelIndex) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut()
                                )
                            }
                        },
                        label = "drilldownLevelAnimation"
                    ) { level ->
                        when (level) {
                            HierarchyLevel.LANGUAGE -> {
                                LanguageSelectionList(
                                    activeLanguage = selectedDialect.language,
                                    onSelectLanguage = { lang ->
                                        selectedLanguage = lang
                                        // Pick first country for this language or keep valid
                                        val validCountries = DialectCatalog.getCountriesForLanguage(lang)
                                        selectedCountry = if (validCountries.contains(selectedCountry)) {
                                            selectedCountry
                                        } else {
                                            validCountries.firstOrNull() ?: ""
                                        }
                                        currentLevel = HierarchyLevel.COUNTRY
                                    }
                                )
                            }

                            HierarchyLevel.COUNTRY -> {
                                CountrySelectionList(
                                    language = selectedLanguage,
                                    activeCountry = selectedDialect.country,
                                    onSelectCountry = { country ->
                                        selectedCountry = country
                                        val validStates = DialectCatalog.getStatesForLanguageAndCountry(selectedLanguage, country)
                                        selectedState = if (validStates.contains(selectedState)) {
                                            selectedState
                                        } else {
                                            validStates.firstOrNull() ?: ""
                                        }
                                        currentLevel = HierarchyLevel.STATE
                                    }
                                )
                            }

                            HierarchyLevel.STATE -> {
                                StateSelectionList(
                                    language = selectedLanguage,
                                    country = selectedCountry,
                                    activeState = selectedDialect.stateOrProvince,
                                    onSelectState = { state ->
                                        selectedState = state
                                        currentLevel = HierarchyLevel.DISTRICT
                                    }
                                )
                            }

                            HierarchyLevel.DISTRICT -> {
                                DistrictSelectionList(
                                    language = selectedLanguage,
                                    country = selectedCountry,
                                    state = selectedState,
                                    activeDialectId = selectedDialect.id,
                                    onSelectDialect = { dialect ->
                                        onSelectDialect(dialect)
                                        onDismiss()
                                    },
                                    onPreviewAudio = onPreviewAudio
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Breadcrumb Bar:
 * Displays [Language] > [Country] > [State] > [District]
 * Each component is clickable to effortlessly navigate back to any previous level.
 */
@Composable
private fun InteractiveBreadcrumbBar(
    currentLevel: HierarchyLevel,
    selectedLanguage: String,
    selectedCountry: String,
    selectedState: String,
    onNavigateToLevel: (HierarchyLevel) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .testTag("drilldown_breadcrumb_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Step 1: Language
        BreadcrumbChip(
            title = if (currentLevel == HierarchyLevel.LANGUAGE) "1. Language" else selectedLanguage,
            icon = Icons.Default.Language,
            isCurrent = currentLevel == HierarchyLevel.LANGUAGE,
            isCompleted = currentLevel.levelIndex > HierarchyLevel.LANGUAGE.levelIndex,
            onClick = { onNavigateToLevel(HierarchyLevel.LANGUAGE) },
            testTag = "breadcrumb_language"
        )

        BreadcrumbDivider()

        // Step 2: Country
        BreadcrumbChip(
            title = when {
                currentLevel.levelIndex < HierarchyLevel.COUNTRY.levelIndex -> "2. Country"
                currentLevel == HierarchyLevel.COUNTRY -> "2. Country"
                else -> "${DialectCatalog.getFlagForCountry(selectedCountry)} $selectedCountry"
            },
            icon = Icons.Default.Public,
            isCurrent = currentLevel == HierarchyLevel.COUNTRY,
            isCompleted = currentLevel.levelIndex > HierarchyLevel.COUNTRY.levelIndex,
            enabled = currentLevel.levelIndex >= HierarchyLevel.COUNTRY.levelIndex,
            onClick = { onNavigateToLevel(HierarchyLevel.COUNTRY) },
            testTag = "breadcrumb_country"
        )

        BreadcrumbDivider()

        // Step 3: State
        BreadcrumbChip(
            title = when {
                currentLevel.levelIndex < HierarchyLevel.STATE.levelIndex -> "3. State"
                currentLevel == HierarchyLevel.STATE -> "3. State"
                else -> selectedState
            },
            icon = Icons.Default.Map,
            isCurrent = currentLevel == HierarchyLevel.STATE,
            isCompleted = currentLevel.levelIndex > HierarchyLevel.STATE.levelIndex,
            enabled = currentLevel.levelIndex >= HierarchyLevel.STATE.levelIndex,
            onClick = { onNavigateToLevel(HierarchyLevel.STATE) },
            testTag = "breadcrumb_state"
        )

        BreadcrumbDivider()

        // Step 4: District
        BreadcrumbChip(
            title = "4. District",
            icon = Icons.Default.Place,
            isCurrent = currentLevel == HierarchyLevel.DISTRICT,
            isCompleted = false,
            enabled = currentLevel.levelIndex >= HierarchyLevel.DISTRICT.levelIndex,
            onClick = { onNavigateToLevel(HierarchyLevel.DISTRICT) },
            testTag = "breadcrumb_district"
        )
    }
}

@Composable
private fun BreadcrumbChip(
    title: String,
    icon: ImageVector,
    isCurrent: Boolean,
    isCompleted: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    testTag: String
) {
    val bgColor = when {
        isCurrent -> MaterialTheme.colorScheme.primaryContainer
        isCompleted -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
        isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        border = if (isCurrent) {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}

@Composable
private fun BreadcrumbDivider() {
    Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        modifier = Modifier.size(14.dp)
    )
}

/**
 * Level 1: Language List
 */
@Composable
private fun LanguageSelectionList(
    activeLanguage: String,
    onSelectLanguage: (String) -> Unit
) {
    val languages = DialectCatalog.getAllLanguages()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("drilldown_language_list"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(languages) { lang ->
            val emoji = DialectCatalog.getLanguageEmoji(lang)
            val countries = DialectCatalog.getCountriesForLanguage(lang)
            val dialectsCount = DialectCatalog.dialects.count { it.language.equals(lang, true) }
            val isCurrent = lang.equals(activeLanguage, true)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectLanguage(lang) }
                    .testTag("language_card_$lang"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    }
                ),
                border = if (isCurrent) {
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                } else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = lang,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    ActiveBadge()
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${countries.size} countries · $dialectsCount regional dialects",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Choose $lang",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Level 2: Country List
 */
@Composable
private fun CountrySelectionList(
    language: String,
    activeCountry: String,
    onSelectCountry: (String) -> Unit
) {
    val countries = DialectCatalog.getCountriesForLanguage(language)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("drilldown_country_list"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(countries) { country ->
            val flag = DialectCatalog.getFlagForCountry(country)
            val states = DialectCatalog.getStatesForLanguageAndCountry(language, country)
            val dialectCount = DialectCatalog.dialects.count {
                it.language.equals(language, true) && it.country.equals(country, true)
            }
            val isCurrent = country.equals(activeCountry, true)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectCountry(country) }
                    .testTag("country_card_$country"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    }
                ),
                border = if (isCurrent) {
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                } else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = flag, fontSize = 26.sp)

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = country,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    ActiveBadge()
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${states.size} states/provinces · $dialectCount dialects",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Choose $country",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Level 3: State / Province List
 */
@Composable
private fun StateSelectionList(
    language: String,
    country: String,
    activeState: String,
    onSelectState: (String) -> Unit
) {
    val states = DialectCatalog.getStatesForLanguageAndCountry(language, country)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("drilldown_state_list"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(states) { state ->
            val dialects = DialectCatalog.getDialectsForHierarchy(language, country, state)
            val isCurrent = state.equals(activeState, true)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectState(state) }
                    .testTag("state_card_$state"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    }
                ),
                border = if (isCurrent) {
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                } else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = state,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    ActiveBadge()
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${dialects.size} district dialects available",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Choose $state",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Level 4: District / Dialect List
 */
@Composable
private fun DistrictSelectionList(
    language: String,
    country: String,
    state: String,
    activeDialectId: String,
    onSelectDialect: (RegionalDialect) -> Unit,
    onPreviewAudio: ((String) -> Unit)?
) {
    val dialects = DialectCatalog.getDialectsForHierarchy(language, country, state)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("drilldown_district_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(dialects, key = { it.id }) { dialect ->
            DistrictDialectItem(
                dialect = dialect,
                isSelected = dialect.id == activeDialectId,
                onSelect = { onSelectDialect(dialect) },
                onPreviewAudio = onPreviewAudio
            )
        }
    }
}

/**
 * Individual District / Dialect Card with sample greeting and audio preview
 */
@Composable
private fun DistrictDialectItem(
    dialect: RegionalDialect,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPreviewAudio: ((String) -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("district_item_${dialect.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = dialect.flagEmoji, fontSize = 20.sp)
                    Text(
                        text = "${dialect.cityOrArea} · ${dialect.dialectName}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${dialect.language} · ${dialect.stateOrProvince}, ${dialect.country} (${dialect.region})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (dialect.samplePhrases.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "“${dialect.samplePhrases.first()}”",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${dialect.typicalExpressions.size} slang expressions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (onPreviewAudio != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onPreviewAudio(dialect.greeting) }
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Preview voice",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Preview Voice",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Active",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 10.sp
        )
    }
}
