package com.beigel.leetSpeak_Generator.ui.components.leet.selector

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.LeetOption

/**
 * PERFORMANCE-OPTIMIERTE Favoriten-Sektion
 * FIXES:
 * - Entfernt schwere AnimatedVisibility
 * - Vereinfachte Animationen
 * - Reduzierte Recompositions
 * - Faster rendering
 * - FIXED: clickable durch Card onClick ersetzt
 */
@Composable
fun FavoritesSection(
    favoriteOptions: List<LeetOption>,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit,
    modifier: Modifier = Modifier
) {
    // PERFORMANCE FIX: Simple conditional rendering instead of AnimatedVisibility
    if (favoriteOptions.isNotEmpty()) {
        Column(modifier = modifier) {
            Spacer(modifier = Modifier.height(8.dp))

            // Vollbreite Favoriten-Cards
            favoriteOptions.forEach { option ->
                FullWidthFavoriteCard(
                    option = option,
                    onOptionSelected = onOptionSelected,
                    onToggleFavorite = onToggleFavorite,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * PERFORMANCE-OPTIMIERTE Vollbreite Favoriten-Card
 * FIXES:
 * - Entfernt schwere AnimatedVisibility für Selected Indicator
 * - Vereinfachte Surface-Operationen
 * - Reduzierte State Dependencies
 * - FIXED: clickable durch Card onClick ersetzt
 */
@Composable
private fun FullWidthFavoriteCard(
    option: LeetOption,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onOptionSelected(option) }, // FIXED: Verwende Card onClick
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (option.isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = if (option.isSelected) 2.dp else 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(
                if (option.isSelected) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                }
            )
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (option.isSelected) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon mit Selection Indicator - PERFORMANCE FIX: Simplified structure
            Box {
                // Safe Icon Loading
                Icon(
                    imageVector = option.iconImageVector,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )

                // Selected Ring - PERFORMANCE FIX: Simple conditional rendering
                if (option.isSelected) {
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = (-4).dp, y = (-4).dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.secondary
                        )
                    ) {}
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (option.isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (option.isSelected) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action Buttons - PERFORMANCE FIX: Simplified structure
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Selected Indicator - PERFORMANCE FIX: Simple conditional rendering
                if (option.isSelected) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.leet_selector_selected),
                            modifier = Modifier
                                .size(24.dp)
                                .padding(4.dp),
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }

                // Remove from Favorites Button
                IconButton(
                    onClick = { onToggleFavorite(option) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = stringResource(R.string.leet_selector_remove_favorite),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}