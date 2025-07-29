package com.beigel.leetSpeak_Generator.ui.components.leet.creation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.R

/**
 * Card für Leet-Informationen: Name und Icon
 * Komplett überarbeitet für Material Icons Support
 * FIXED: Alle hardcodierten deutschen Texte durch String-Ressourcen ersetzt
 */
@Composable
fun LeetInfoCard(
    baseName: String,
    onBaseNameChange: (String) -> Unit,
    displayName: String,
    selectedIcon: ImageVector,
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
            // FIXED: String resource
            Text(
                text = stringResource(R.string.leet_creation_info_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon Button mit Material Icons
                IconButton(
                    onClick = onIconClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = selectedIcon,
                        // FIXED: String resource
                        contentDescription = stringResource(R.string.select_icon),
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
                        // FIXED: String resource
                        label = { Text(stringResource(R.string.leet_creation_base_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        // FIXED: String resource
                        placeholder = { Text(stringResource(R.string.leet_creation_name_example)) },
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
                        // FIXED: String resource with formatting
                        Text(
                            text = stringResource(R.string.leet_creation_preview, displayName),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // FIXED: String resource
            Text(
                text = stringResource(R.string.leet_creation_icon_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}