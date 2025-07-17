package com.beigel.leetSpeak_Generator.ui.components.text

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Chip für einzelne Statistik
 */
@Composable
fun StatisticChip(
    label: String,
    count: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondary
) {
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = contentColor
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 13.sp
                ),
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Text-Size Indikator Chip
 */
@Composable
fun TextSizeIndicatorChip(
    currentTextSize: TextUnit,
    modifier: Modifier = Modifier
) {
    val sizeLabel = when {
        currentTextSize.value >= 30f -> "XXL"
        currentTextSize.value >= 22f -> "XL"
        currentTextSize.value >= 16f -> "L"
        else -> "M"
    }

    val sizeColor = when {
        currentTextSize.value >= 30f -> MaterialTheme.colorScheme.tertiary
        currentTextSize.value >= 22f -> MaterialTheme.colorScheme.secondary
        currentTextSize.value >= 16f -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        color = sizeColor.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small,
        modifier = modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FormatSize,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = sizeColor
            )
            Text(
                text = sizeLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 13.sp
                ),
                color = sizeColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Sammlung von Statistik-Chips mit Animation
 */
@Composable
fun StatisticsChipRow(
    charCount: Int,
    wordCount: Int,
    currentTextSize: TextUnit,
    hasText: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = hasText,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatisticChip(
                label = "Zeichen",
                count = charCount,
                icon = Icons.Default.TextFields
            )

            StatisticChip(
                label = "Wörter",
                count = wordCount,
                icon = Icons.AutoMirrored.Filled.Article
            )

            TextSizeIndicatorChip(currentTextSize = currentTextSize)
        }
    }
}

/**
 * Erweiterte Statistiken für Übersetzungen
 */
@Composable
fun TranslationStatisticsChips(
    translatedChars: Int,
    totalChars: Int,
    translationPercentage: Float,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        // Übersetzungsfortschritt
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Transform,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$translatedChars/$totalChars",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Prozentanzeige
        if (translationPercentage > 0) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Percent,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "${translationPercentage.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}