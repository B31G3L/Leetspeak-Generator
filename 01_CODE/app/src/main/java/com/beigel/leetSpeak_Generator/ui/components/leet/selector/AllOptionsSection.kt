// AllOptionsSection.kt - PERFORMANCE-OPTIMIERT UND FIXED
package com.beigel.leetSpeak_Generator.ui.components.leet.selector

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
// FIXED: Added proper import for LeetOption
import com.beigel.leetSpeak_Generator.data.LeetOption

/**
 * PERFORMANCE-OPTIMIERTE Alle Leet-Modi Darstellung
 * FIXES:
 * - Entfernte schwere AnimatedContent
 * - Vereinfachte Grid-Darstellung
 * - Reduzierte Recompositions
 * - Fixed imports and references
 */
@Composable
fun AllOptionsSection(
    leetOptions: List<LeetOption>,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit,
    onEditOption: (LeetOption) -> Unit,
    onShowTable: (LeetOption) -> Unit,
    defaultViewExpanded: Boolean = false,
    modifier: Modifier = Modifier
) {
    // PERFORMANCE FIX: Local state to avoid ViewModel calls
    var showExpandedView by remember { mutableStateOf(defaultViewExpanded) }

    Column(modifier = modifier) {
        // Header mit Toggle
        AllOptionsHeader(
            totalCount = leetOptions.size,
            showExpandedView = showExpandedView,
            onToggleView = { showExpandedView = !showExpandedView }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // PERFORMANCE FIX: Simple conditional rendering instead of AnimatedContent
        if (showExpandedView) {
            // Detaillierte List View - Optimiert
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                leetOptions.forEach { option ->
                    DetailedModeCard(
                        option = option,
                        onOptionSelected = onOptionSelected,
                        onToggleFavorite = onToggleFavorite,
                        onEditOption = onEditOption,
                        onShowTable = onShowTable
                    )
                }
            }
        } else {
            // Kompakte Grid View - Vereinfacht ohne LazyVerticalGrid
            SimpleGridView(
                leetOptions = leetOptions,
                onOptionSelected = onOptionSelected,
                onToggleFavorite = onToggleFavorite
            )
        }
    }
}

/**
 * Header für die Alle-Modi Sektion
 */
@Composable
private fun AllOptionsHeader(
    totalCount: Int,
    showExpandedView: Boolean,
    onToggleView: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Alle Modi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "$totalCount verfügbar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // View Toggle Button
            IconButton(
                onClick = onToggleView,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (showExpandedView) Icons.Default.GridView else Icons.Default.List,
                    contentDescription = if (showExpandedView) "Grid-Ansicht" else "Listen-Ansicht",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * PERFORMANCE-OPTIMIERTE Grid-Darstellung ohne LazyVerticalGrid
 */
@Composable
private fun SimpleGridView(
    leetOptions: List<LeetOption>,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit
) {
    Column(
        modifier = Modifier.heightIn(max = 300.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // FIXED: Proper chunked processing with explicit types
        val chunkedOptions = leetOptions.chunked(2)
        chunkedOptions.forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowOptions.forEach { option ->
                    CompactModeCard(
                        option = option,
                        onOptionSelected = onOptionSelected,
                        onToggleFavorite = onToggleFavorite,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if odd number
                if (rowOptions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * PERFORMANCE-OPTIMIERTE Kompakte Mode Card für Grid
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactModeCard(
    option: LeetOption,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onOptionSelected(option) },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.2f),
        colors = CardDefaults.cardColors(
            containerColor = if (option.isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (option.isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
            )
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (option.isSelected) 4.dp else 1.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Favorite Button
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                IconButton(
                    onClick = { onToggleFavorite(option) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (option.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorit umschalten",
                        modifier = Modifier.size(14.dp),
                        tint = if (option.isFavorite) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Main Content
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon - FIXED: Use the iconImageVector from LeetOption
                Icon(
                    imageVector = option.iconImageVector,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Name
                Text(
                    text = option.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (option.isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (option.isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Selected Indicator
            if (option.isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Ausgewählt",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * PERFORMANCE-OPTIMIERTE Detaillierte Mode Card für Liste
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailedModeCard(
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
            // Icon - FIXED: Use the iconImageVector from LeetOption
            Icon(
                imageVector = option.iconImageVector,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    if (option.isFavorite) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorit",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (option.isSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Ausgewählt",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Action Buttons
            Row {
                IconButton(
                    onClick = { onToggleFavorite(option) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (option.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorit umschalten",
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
                        contentDescription = "Tabelle anzeigen",
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
                            contentDescription = "Bearbeiten",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}