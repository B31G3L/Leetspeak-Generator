package com.beigel.leetSpeak_Generator.ui.components.leet.selector

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.presentation.intent.MainIntent
import com.beigel.leetSpeak_Generator.ui.components.LeetCreationDialog
import com.beigel.leetSpeak_Generator.ui.components.TranslationTableScreen
import com.beigel.leetSpeak_Generator.ui.theme.PillShape
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel

private enum class ModiFilter { ALL, FAVORITES, CUSTOM }

/**
 * Modi-Vollbildschirm (Redesign v4) — ersetzt die alte LeetSelectorBottomSheet.
 * Header (Zurück + "Modi" + "+ Neu"), Filter-Chips (Alle/Favoriten/Eigene),
 * Modus-Liste als Karten mit Favoriten-Stern, Tabellen-Icon, Edit/Delete für Eigene.
 */
@Composable
fun ModiScreen(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val leetOptions by viewModel.leetOptions.collectAsStateWithLifecycle()

    var filter by remember { mutableStateOf(ModiFilter.ALL) }
    var showLeetCreationDialog by remember { mutableStateOf(false) }
    var showLeetEditDialog by remember { mutableStateOf(false) }
    var showTranslationTableDialog by remember { mutableStateOf(false) }
    var currentEditOption by remember { mutableStateOf<LeetOption?>(null) }
    var currentTableOption by remember { mutableStateOf<LeetOption?>(null) }
    var pendingDeleteOption by remember { mutableStateOf<LeetOption?>(null) }

    val filteredOptions = remember(leetOptions, filter) {
        when (filter) {
            ModiFilter.ALL -> leetOptions
            ModiFilter.FAVORITES -> leetOptions.filter { it.isFavorite }
            ModiFilter.CUSTOM -> leetOptions.filter { it.isCustom }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (showTranslationTableDialog && currentTableOption != null) {
            TranslationTableScreen(
                leetOption = currentTableOption!!,
                viewModel = viewModel,
                onDismiss = {
                    showTranslationTableDialog = false
                    currentTableOption = null
                }
            )
        } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Zurück + "Modi" + "+ Neu"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = onDismiss,
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.modi_back),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.modi_screen_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Surface(
                    onClick = { showLeetCreationDialog = true },
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.leet_selector_new),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Filter-Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    label = stringResource(R.string.modi_filter_all),
                    selected = filter == ModiFilter.ALL,
                    onClick = { filter = ModiFilter.ALL }
                )
                FilterChip(
                    label = stringResource(R.string.modi_filter_favorites),
                    selected = filter == ModiFilter.FAVORITES,
                    onClick = { filter = ModiFilter.FAVORITES }
                )
                FilterChip(
                    label = stringResource(R.string.modi_filter_custom),
                    selected = filter == ModiFilter.CUSTOM,
                    onClick = { filter = ModiFilter.CUSTOM }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredOptions, key = { it.mode.toString() + "_" + it.customIndex }) { option ->
                    ModiCard(
                        option = option,
                        onSelect = {
                            viewModel.handleIntent(MainIntent.ChangeMode(option))
                            onDismiss()
                        },
                        onToggleFavorite = { viewModel.handleIntent(MainIntent.ToggleFavorite(option)) },
                        onEdit = {
                            currentEditOption = option
                            showLeetEditDialog = true
                        },
                        onDelete = { pendingDeleteOption = option },
                        onShowTable = {
                            currentTableOption = option
                            showTranslationTableDialog = true
                        }
                    )
                }
            }
        }
        }
    }

    if (showLeetCreationDialog) {
        LeetCreationDialog(
            viewModel = viewModel,
            onDismiss = { showLeetCreationDialog = false }
        )
    }

    if (showLeetEditDialog) {
        val option = currentEditOption
        if (option != null && option.isCustom) {
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
            } else {
                LaunchedEffect(option) {
                    showLeetEditDialog = false
                    currentEditOption = null
                }
            }
        }
    }

    pendingDeleteOption?.let { option ->
        AlertDialog(
            onDismissRequest = { pendingDeleteOption = null },
            title = { Text(stringResource(R.string.delete_leet_dialog_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_leet_dialog_message, option.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.handleIntent(MainIntent.DeleteLeet(option.customIndex))
                        pendingDeleteOption = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.delete_leet_dialog_confirm)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingDeleteOption = null }) {
                    Text(stringResource(R.string.delete_leet_dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = PillShape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun ModiCard(
    option: LeetOption,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowTable: () -> Unit
) {
    Card(
        onClick = onSelect,
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = if (option.isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (option.isSelected)
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = if (option.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = stringResource(R.string.leet_selector_toggle_favorite),
                    modifier = Modifier.size(18.dp),
                    tint = if (option.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = option.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (option.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = stringResource(R.string.modi_custom_badge),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (option.isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.leet_selector_selected),
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                IconButton(onClick = onShowTable, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = stringResource(R.string.leet_selector_show_table),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (option.isCustom) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.leet_selector_edit),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.leet_selector_delete),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
