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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LeetSelectorHeader(onCreateNew = { showLeetCreationDialog = true })

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
                // NEU: Löschen direkt als Intent weiterleiten
                onDeleteOption = { option ->
                    viewModel.handleIntent(MainIntent.DeleteLeet(option.customIndex))
                    onDismiss() // Bottom Sheet schließen damit Snackbar sichtbar ist
                },
                onShowTable = { option ->
                    currentTableOption = option
                    showTranslationTableDialog = true
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