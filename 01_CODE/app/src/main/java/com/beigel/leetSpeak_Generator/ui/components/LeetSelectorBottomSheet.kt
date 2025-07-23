// LeetSelectorBottomSheet.kt
package com.beigel.leetSpeak_Generator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.presentation.intent.MainIntent
import com.beigel.leetSpeak_Generator.ui.components.leet.selector.*
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel

/**
 * PERFORMANCE-OPTIMIERTES Bottom Sheet für Leet-Auswahl
 * FIXES:
 * - Lazy Loading der StateFlows
 * - Vereinfachte Animationen
 * - Optimierte Recomposition
 * - Reduced State Collections
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeetSelectorBottomSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // PERFORMANCE FIX: Lazy state loading mit Remember aber korrekte initialValues
    val leetOptions by remember { viewModel.leetOptions }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val favoriteLeetOptions by remember { viewModel.favoriteLeetOptions }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // BUGFIX: Verwende den aktuellen Wert als initialValue, nicht hart-codiert false
    val defaultViewExpanded by remember { viewModel.defaultViewExpanded }
        .collectAsStateWithLifecycle()

    // PERFORMANCE FIX: Local state for dialogs to avoid ViewModel calls
    var showLeetCreationDialog by remember { mutableStateOf(false) }
    var showLeetEditDialog by remember { mutableStateOf(false) }
    var showTranslationTableDialog by remember { mutableStateOf(false) }

    var currentEditOption by remember { mutableStateOf<LeetOption?>(null) }
    var currentTableOption by remember { mutableStateOf<LeetOption?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(width = 32.dp, height = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(2.dp)
            ) {}
        },
        windowInsets = WindowInsets(0)
    ) {
        // PERFORMANCE FIX: Simplified content loading
        LazyBottomSheetContent(
            leetOptions = leetOptions,
            favoriteLeetOptions = favoriteLeetOptions,
            defaultViewExpanded = defaultViewExpanded,
            onCreateNew = { showLeetCreationDialog = true },
            onOptionSelected = { option ->
                viewModel.handleIntent(MainIntent.ChangeMode(option))
                onDismiss()
            },
            onToggleFavorite = { option ->
                viewModel.handleIntent(MainIntent.ToggleFavorite(option))
            },
            onEditOption = { option ->
                currentEditOption = option
                showLeetEditDialog = true
            },
            onShowTable = { option ->
                currentTableOption = option
                showTranslationTableDialog = true
            }
        )
    }

    // PERFORMANCE FIX: Lazy dialog loading - only when needed
    LaunchedEffect(showLeetCreationDialog) {
        if (!showLeetCreationDialog) {
            // Clear states when dialog closes
            currentEditOption = null
            currentTableOption = null
        }
    }

    // Dialog Handling - Only render when actually needed
    if (showLeetCreationDialog) {
        LeetCreationDialog(
            viewModel = viewModel,
            onDismiss = {
                showLeetCreationDialog = false
                onDismiss()
            }
        )
    }

    if (showLeetEditDialog && currentEditOption != null) {
        currentEditOption?.let { option ->
            if (option.isCustom) {
                val leets by viewModel.leets.collectAsStateWithLifecycle()
                val leet = leets.getOrNull(option.customIndex)

                if (leet != null) {
                    LeetCreationDialog(
                        viewModel = viewModel,
                        existingLeet = leet,
                        leetIndex = option.customIndex,
                        onDismiss = {
                            showLeetEditDialog = false
                            currentEditOption = null
                        }
                    )
                }
            }
        }
    }

    if (showTranslationTableDialog && currentTableOption != null) {
        currentTableOption?.let { option ->
            TranslationTableDialog(
                leetOption = option,
                viewModel = viewModel,
                onDismiss = {
                    showTranslationTableDialog = false
                    currentTableOption = null
                }
            )
        }
    }
}

/**
 * PERFORMANCE-OPTIMIERTE Content-Komponente
 * Vermeidet heavy recomposition durch simplified structure
 */
@Composable
private fun LazyBottomSheetContent(
    leetOptions: List<LeetOption>,
    favoriteLeetOptions: List<LeetOption>,
    defaultViewExpanded: Boolean,
    onCreateNew: () -> Unit,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit,
    onEditOption: (LeetOption) -> Unit,
    onShowTable: (LeetOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header - Always visible, no state dependency
        LeetSelectorHeader(onCreateNew = onCreateNew)

        // PERFORMANCE FIX: Conditional rendering instead of AnimatedVisibility
        if (favoriteLeetOptions.isNotEmpty()) {
            FavoritesSection(
                favoriteOptions = favoriteLeetOptions,
                onOptionSelected = onOptionSelected,
                onToggleFavorite = onToggleFavorite
            )
        }

        // PERFORMANCE FIX: Simplified All Options without heavy animations
        AllOptionsSection(
            leetOptions = leetOptions,
            onOptionSelected = onOptionSelected,
            onToggleFavorite = onToggleFavorite,
            onEditOption = onEditOption,
            onShowTable = onShowTable,
            defaultViewExpanded = defaultViewExpanded
        )

        // Bottom spacer
        Spacer(modifier = Modifier.height(16.dp))
    }
}