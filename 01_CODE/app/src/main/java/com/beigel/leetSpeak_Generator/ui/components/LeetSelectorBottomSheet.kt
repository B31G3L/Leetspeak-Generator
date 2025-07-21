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
import com.beigel.leetSpeak_Generator.ui.components.leet.creation.LeetCreationDialog
import com.beigel.leetSpeak_Generator.ui.components.leet.selector.*
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel

/**
 * Hauptkomponente für die Leet-Auswahl als Bottom Sheet
 * Mit Settings-Integration für Standard-Ansicht
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeetSelectorBottomSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State Collection
    val leetOptions by viewModel.leetOptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteLeetOptions by viewModel.favoriteLeetOptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val defaultViewExpanded by viewModel.defaultViewExpanded.collectAsStateWithLifecycle()

    // Dialog States
    var showLeetCreationDialog by remember { mutableStateOf(false) }
    var showLeetEditDialog by remember { mutableStateOf(false) }
    var showTranslationTableDialog by remember { mutableStateOf(false) }

    // Current selections for dialogs
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Header mit "Neu erstellen" Button
            LeetSelectorHeader(
                onCreateNew = { showLeetCreationDialog = true }
            )

            // Favoriten Sektion (nur wenn vorhanden)
            FavoritesSection(
                favoriteOptions = favoriteLeetOptions,
                onOptionSelected = { option ->
                    viewModel.handleIntent(MainIntent.ChangeMode(option))
                    onDismiss()
                },
                onToggleFavorite = { option ->
                    viewModel.handleIntent(MainIntent.ToggleFavorite(option))
                }
            )

            // Alle Optionen Sektion mit Settings-Integration
            AllOptionsSection(
                leetOptions = leetOptions,
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
                },
                defaultViewExpanded = defaultViewExpanded // Settings werden verwendet
            )

            // Spacer für bessere Bedienung
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialog Handling
    DialogHandler(
        showLeetCreationDialog = showLeetCreationDialog,
        showLeetEditDialog = showLeetEditDialog,
        showTranslationTableDialog = showTranslationTableDialog,
        currentEditOption = currentEditOption,
        currentTableOption = currentTableOption,
        viewModel = viewModel,
        onDismissCreation = {
            showLeetCreationDialog = false
            onDismiss()
        },
        onDismissEdit = {
            showLeetEditDialog = false
            currentEditOption = null
        },
        onDismissTable = {
            showTranslationTableDialog = false
            currentTableOption = null
        }
    )
}

/**
 * Separate Composable für Dialog-Management
 */
@Composable
private fun DialogHandler(
    showLeetCreationDialog: Boolean,
    showLeetEditDialog: Boolean,
    showTranslationTableDialog: Boolean,
    currentEditOption: LeetOption?,
    currentTableOption: LeetOption?,
    viewModel: MainViewModel,
    onDismissCreation: () -> Unit,
    onDismissEdit: () -> Unit,
    onDismissTable: () -> Unit
) {
    // Leet Creation Dialog
    if (showLeetCreationDialog) {
        LeetCreationDialog(
            viewModel = viewModel,
            onDismiss = onDismissCreation
        )
    }

    // Leet Edit Dialog
    if (showLeetEditDialog) {
        currentEditOption?.let { option ->
            if (option.isCustom) {
                val leets by viewModel.leets.collectAsStateWithLifecycle()
                val leet = leets.getOrNull(option.customIndex)

                if (leet != null) {
                    LeetCreationDialog(
                        viewModel = viewModel,
                        existingLeet = leet,
                        leetIndex = option.customIndex,
                        onDismiss = onDismissEdit
                    )
                }
            }
        }
    }

    // Translation Table Dialog
    if (showTranslationTableDialog) {
        currentTableOption?.let { option ->
            TranslationTableDialog(
                leetOption = option,
                viewModel = viewModel,
                onDismiss = onDismissTable
            )
        }
    }
}