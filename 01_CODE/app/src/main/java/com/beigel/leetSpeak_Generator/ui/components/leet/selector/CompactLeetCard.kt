package com.beigel.leetSpeak_Generator.ui.components.leet.selector

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.manager.LeetManager

/**
 * Kompakte Karte für die Favoriten-Sektion
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactLeetCard(
    option: LeetOption,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onOptionSelected(option) },
        modifier = modifier.width(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (option.isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (option.isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.secondary)
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon with selection indicator
            Box {
                // Safe Icon Loading - fallback zu Material Icon
                Icon(
                    imageVector = Icons.Default.Settings, // Fallback Icon
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )

                // Selected indicator
                if (option.isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.TopEnd),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = option.name,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                fontWeight = if (option.isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            // Preview
            Text(
                text = generatePreview(option),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Generiert Preview Text für die Leet Option
 */
private fun generatePreview(option: LeetOption): String {
    return when (option.mode) {
        LeetManager.MODE_SIMPLE -> "H3110"
        LeetManager.MODE_EXTENDED -> "#3110"
        LeetManager.MODE_CUSTOM -> "H3ll0"
        else -> "H3110"
    }
}