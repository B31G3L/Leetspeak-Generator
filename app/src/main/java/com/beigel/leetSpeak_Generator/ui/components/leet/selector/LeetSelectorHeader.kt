package com.beigel.leetSpeak_Generator.ui.components.leet.selector

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LowPriority
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.R

@Composable
fun LeetSelectorHeader(
    onCreateNew: () -> Unit,
    isReorderMode: Boolean = false,
    onToggleReorder: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text       = stringResource(R.string.leet_selector_title),
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.weight(1f)
        )

        // Reorder-Toggle Button
        IconButton(onClick = onToggleReorder) {
            Icon(
                imageVector        = Icons.Default.LowPriority,
                contentDescription = stringResource(
                    if (isReorderMode) R.string.reorder_mode_disable
                    else R.string.reorder_mode_enable
                ),
                modifier = Modifier.size(22.dp),
                tint     = if (isReorderMode)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        // Neu-Button
        FilledTonalButton(
            onClick = onCreateNew,
            colors  = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Icon(
                imageVector        = Icons.Default.Add,
                contentDescription = null,
                modifier           = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.leet_selector_new))
        }
    }
}