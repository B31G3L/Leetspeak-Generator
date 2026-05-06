package com.beigel.leetSpeak_Generator.ui.components.leet.selector

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.LeetOption

@Composable
fun AllOptionsSection(
    leetOptions: List<LeetOption>,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit,
    onEditOption: (LeetOption) -> Unit,
    onDeleteOption: (LeetOption) -> Unit,
    onShowTable: (LeetOption) -> Unit,
    onReorder: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingDeleteOption by remember { mutableStateOf<LeetOption?>(null) }

    val customOptions = leetOptions.filter { it.isCustom }
    val fixedOptions  = leetOptions.filter { !it.isCustom }

    // Drag-State
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY   by remember { mutableStateOf(0f) }
    val itemHeightPx  = remember { mutableStateOf(0) }

    Column(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .heightIn(max = 400.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Fixe Optionen – kein Drag
            fixedOptions.forEach { option ->
                DetailedCard(
                    option           = option,
                    onOptionSelected = onOptionSelected,
                    onToggleFavorite = onToggleFavorite,
                    onEditOption     = onEditOption,
                    onDeleteOption   = { pendingDeleteOption = it },
                    onShowTable      = onShowTable
                )
            }

            // Custom Leets – mit Drag Handle
            customOptions.forEachIndexed { index, option ->
                val isDragging = draggingIndex == index

                // Visueller Offset während des Ziehens
                val offsetDp = if (isDragging) {
                    with(LocalDensity.current) { dragOffsetY.toDp() }
                } else 0.dp

                Box(
                    modifier = Modifier
                        .offset(y = offsetDp)
                        .zIndex(if (isDragging) 1f else 0f)
                        .onSizeChanged { itemHeightPx.value = it.height }
                ) {
                    DetailedCard(
                        option           = option,
                        onOptionSelected = onOptionSelected,
                        onToggleFavorite = onToggleFavorite,
                        onEditOption     = onEditOption,
                        onDeleteOption   = { pendingDeleteOption = it },
                        onShowTable      = onShowTable,
                        isDragging       = isDragging,
                        dragHandleModifier = Modifier.pointerInput(index) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggingIndex = index
                                    dragOffsetY   = 0f
                                },
                                onDrag = { _, dragAmount ->
                                    dragOffsetY += dragAmount.y

                                    // Berechne Zielindex
                                    val itemH = itemHeightPx.value.toFloat()
                                    if (itemH > 0) {
                                        val shift = (dragOffsetY / itemH).toInt()
                                        val targetIndex = (index + shift)
                                            .coerceIn(0, customOptions.size - 1)
                                        if (targetIndex != index) {
                                            onReorder(index, targetIndex)
                                            draggingIndex = targetIndex
                                            dragOffsetY  -= shift * itemH
                                        }
                                    }
                                },
                                onDragEnd    = { draggingIndex = null; dragOffsetY = 0f },
                                onDragCancel = { draggingIndex = null; dragOffsetY = 0f }
                            )
                        }
                    )
                }
            }
        }
    }

    pendingDeleteOption?.let { option ->
        DeleteConfirmationDialog(
            leetName  = option.name,
            onConfirm = {
                onDeleteOption(option)
                pendingDeleteOption = null
            },
            onDismiss = { pendingDeleteOption = null }
        )
    }
}
@Composable
private fun EmptyCustomLeetsHint() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column {
                Text(
                    text = stringResource(R.string.empty_custom_leets_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.empty_custom_leets_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
@Composable
private fun DeleteConfirmationDialog(
    leetName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = stringResource(R.string.delete_leet_dialog_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(stringResource(R.string.delete_leet_dialog_message, leetName))
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.delete_leet_dialog_confirm))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.delete_leet_dialog_cancel))
            }
        }
    )
}

@Composable
private fun DetailedCard(
    option: LeetOption,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit,
    onEditOption: (LeetOption) -> Unit,
    onDeleteOption: (LeetOption) -> Unit,
    onShowTable: (LeetOption) -> Unit,
    isDragging: Boolean = false,
    dragHandleModifier: Modifier = Modifier,  // NEU
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 0.dp,
        label       = "drag_elevation"
    )

    Card(
        onClick   = { onOptionSelected(option) },
        modifier  = modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(
            containerColor = if (option.isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border    = if (option.isSelected) CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
        ) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag Handle – nur bei Custom Leets sichtbar
            if (option.isCustom) {
                Icon(
                    imageVector        = Icons.Default.DragHandle,
                    contentDescription = stringResource(R.string.drag_handle),
                    modifier           = Modifier
                        .size(20.dp)
                        .then(dragHandleModifier),  // Gesture hier
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = option.name,
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (option.isFavorite) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector        = Icons.Default.Star,
                            contentDescription = stringResource(R.string.content_description_favorite),
                            modifier           = Modifier.size(14.dp),
                            tint               = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (option.isSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector        = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.leet_selector_selected),
                            modifier           = Modifier.size(14.dp),
                            tint               = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            ActionButtons(
                option           = option,
                onToggleFavorite = onToggleFavorite,
                onEditOption     = onEditOption,
                onDeleteOption   = onDeleteOption,
                onShowTable      = onShowTable
            )
        }
    }
}

@Composable
private fun ActionButtons(
    option: LeetOption,
    onToggleFavorite: (LeetOption) -> Unit,
    onEditOption: (LeetOption) -> Unit,
    onDeleteOption: (LeetOption) -> Unit,
    onShowTable: (LeetOption) -> Unit
) {
    Row {
        // Favorit-Button (alle Modi)
        IconButton(
            onClick = { onToggleFavorite(option) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (option.isFavorite) Icons.Default.Favorite
                else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(R.string.leet_selector_toggle_favorite),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        }

        // Übersetzungstabelle (alle Modi)
        IconButton(
            onClick = { onShowTable(option) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TableChart,
                contentDescription = stringResource(R.string.leet_selector_show_table),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Bearbeiten + Löschen nur für Custom Leets
        if (option.isCustom) {
            IconButton(
                onClick = { onEditOption(option) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.leet_selector_edit),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // NEU: Löschen-Button
            IconButton(
                onClick = { onDeleteOption(option) },
                modifier = Modifier.size(32.dp)
            ) {
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