package com.beigel.leetSpeak_Generator.ui.components.leet.selector

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.beigel.leetSpeak_Generator.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.data.LeetOption

@Composable
fun AllOptionsSection(
    leetOptions: List<LeetOption>,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit,
    onEditOption: (LeetOption) -> Unit,
    onShowTable: (LeetOption) -> Unit,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        DetailedListView(
            leetOptions = leetOptions,
            onOptionSelected = onOptionSelected,
            onToggleFavorite = onToggleFavorite,
            onEditOption = onEditOption,
            onShowTable = onShowTable
        )
    }
}

@Composable
private fun DetailedListView(
    leetOptions: List<LeetOption>,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit,
    onEditOption: (LeetOption) -> Unit,
    onShowTable: (LeetOption) -> Unit
) {
    // FIX 1: verticalScroll hinzugefügt, damit alle Einträge erreichbar sind.
    // heightIn(max) bleibt als sinnvolle Begrenzung im BottomSheet erhalten.
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .heightIn(max = 400.dp)
            .verticalScroll(rememberScrollState())   // ← NEU
    ) {
        leetOptions.forEach { option ->
            DetailedCard(
                option = option,
                onOptionSelected = onOptionSelected,
                onToggleFavorite = onToggleFavorite,
                onEditOption = onEditOption,
                onShowTable = onShowTable
            )
        }
    }
}

@Composable
private fun DetailedCard(
    option: LeetOption,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit,
    onEditOption: (LeetOption) -> Unit,
    onShowTable: (LeetOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onOptionSelected(option) },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (option.isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (option.isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = option.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    if (option.isFavorite) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(R.string.content_description_favorite),
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (option.isSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.leet_selector_selected),
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            ActionButtons(
                option = option,
                onToggleFavorite = onToggleFavorite,
                onEditOption = onEditOption,
                onShowTable = onShowTable
            )
        }
    }
}

@Composable
private fun ActionButtons(
    option: LeetOption,
    onToggleFavorite: (LeetOption) -> Unit,
    onEditOption: (LeetOption) -> Unit,
    onShowTable: (LeetOption) -> Unit
) {
    Row {
        IconButton(
            onClick = { onToggleFavorite(option) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (option.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(R.string.leet_selector_toggle_favorite),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        }

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
        }
    }
}