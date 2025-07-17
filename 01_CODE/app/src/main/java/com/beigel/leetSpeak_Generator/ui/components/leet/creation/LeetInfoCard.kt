package com.beigel.leetSpeak_Generator.ui.components.leet.creation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.R

/**
 * Card für Leet-Informationen: Name und Icon
 */
@Composable
fun LeetInfoCard(
    baseName: String,
    onBaseNameChange: (String) -> Unit,
    displayName: String,
    selectedIconResId: Int,
    onIconClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.secondary)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Leet-Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon Button mit Safe Loading
                IconButton(
                    onClick = onIconClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    // Safe Icon Loading - fallback zu Material Icon
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Icon ändern",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name Input mit Preview
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = baseName,
                        onValueChange = onBaseNameChange,
                        label = { Text("Basis-Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("z.B. Gaming") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            focusedLabelColor = MaterialTheme.colorScheme.secondary,
                            cursorColor = MaterialTheme.colorScheme.secondary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preview des finalen Namens
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Wird zu: $displayName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Text(
                text = "Tippe auf das Icon, um es zu ändern. Der Name wird automatisch mit '-Leet' ergänzt.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}