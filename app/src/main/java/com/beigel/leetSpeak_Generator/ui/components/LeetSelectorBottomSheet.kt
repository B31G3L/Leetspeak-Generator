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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeetSelectorBottomSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val leetOptions by viewModel.leetOptions.collectAsStateWithLifecycle()

    var showLeetCreationDialog     by remember { mutableStateOf(false) }
    var showLeetEditDialog         by remember { mutableStateOf(false) }
    var showTranslationTableDialog by remember { mutableStateOf(false) }

    var currentEditOption  by remember { mutableStateOf<LeetOption?>(null) }
    var currentTableOption by remember { mutableStateOf<LeetOption?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier         = modifier,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(width = 32.dp, height = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(2.dp)
            ) {}
        },
        contentWindowInsets = { WindowInsets(0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AllOptionsSection(
                leetOptions      = leetOptions,
                onCreateNew      = { showLeetCreationDialog = true },
                onOptionSelected = { option ->
                    viewModel.handleIntent(MainIntent.ChangeMode(option))
                    onDismiss()
                },
                onToggleFavorite = { option ->
                    viewModel.handleIntent(MainIntent.ToggleFavorite(option))
                },
                onEditOption     = { option ->
                    // Edit nur für Custom Leets sinnvoll – State sauber halten
                    if (option.isCustom) {
                        currentEditOption  = option
                        showLeetEditDialog = true
                    }
                },
                onDeleteOption   = { option ->
                    viewModel.handleIntent(MainIntent.DeleteLeet(option.customIndex))
                    onDismiss()
                },
                onShowTable      = { option ->
                    currentTableOption         = option
                    showTranslationTableDialog = true
                },
                onReorder        = { fromId, toId ->
                    viewModel.handleIntent(MainIntent.ReorderOptions(fromId, toId))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLeetCreationDialog) {
        LeetCreationDialog(
            viewModel = viewModel,
            onDismiss = {
                showLeetCreationDialog = false
                onDismiss()
            }
        )
    }

    if (showLeetEditDialog) {
        val option = currentEditOption
        if (option != null && option.isCustom) {
            val leets by viewModel.leets.collectAsStateWithLifecycle()
            val leet   = leets.getOrNull(option.customIndex)
            if (leet != null) {
                LeetCreationDialog(
                    viewModel    = viewModel,
                    existingLeet = leet,
                    leetIndex    = option.customIndex,
                    onDismiss    = {
                        showLeetEditDialog = false
                        currentEditOption  = null
                    }
                )
            } else {
                // Leet wurde zwischenzeitlich gelöscht – State zurücksetzen
                LaunchedEffect(option) {
                    showLeetEditDialog = false
                    currentEditOption  = null
                }
            }
        } else {
            // Defensiv: Edit für nicht-Custom angefragt → State zurücksetzen
            LaunchedEffect(Unit) {
                showLeetEditDialog = false
                currentEditOption  = null
            }
        }
    }

    if (showTranslationTableDialog && currentTableOption != null) {
        currentTableOption?.let { option ->
            TranslationTableDialog(
                leetOption = option,
                viewModel  = viewModel,
                onDismiss  = {
                    showTranslationTableDialog = false
                    currentTableOption         = null
                }
            )
        }
    }
}