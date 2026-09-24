package com.example.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import com.example.data.model.RegionalDialect

/**
 * Top-level Dialect Selector Sheet delegating to the 4-level
 * hierarchical drill-down modal: Language -> Country -> State -> District / Dialect.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialectSelectorSheet(
    sheetState: SheetState,
    selectedDialect: RegionalDialect,
    onSelectDialect: (RegionalDialect) -> Unit,
    onPreviewAudio: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    HierarchicalDialectDrillDownModal(
        sheetState = sheetState,
        selectedDialect = selectedDialect,
        onSelectDialect = onSelectDialect,
        onPreviewAudio = onPreviewAudio,
        onDismiss = onDismiss
    )
}
