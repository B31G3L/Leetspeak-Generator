package com.beigel.leetSpeak_Generator.ui.components.leet.creation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.data.IconMapper

/**
 * Dialog für Icon-Auswahl mit Material Icons
 * FIXED: Verwendet IconMapper für Icon-Liste und String-basierte Auswahl
 */
@Composable
fun IconPickerDialog(
    selectedIcon: ImageVector,
    onIconSelected: (ImageVector) -> Unit,
    onDismiss: () -> Unit,
) {
    // FIXED: Hole alle verfügbaren Icon-Namen vom IconMapper
    val availableIconNames = IconMapper.getAllIconNames()

    // Konvertiere aktuelles Icon zu String für Vergleich
    val selectedIconName = IconMapper.getNameByIcon(selectedIcon)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Icon auswählen",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(availableIconNames) { iconName ->
                    val icon = IconMapper.getIconByName(iconName)
                    val isSelected = selectedIconName == iconName

                    IconButton(
                        onClick = {
                            onIconSelected(icon)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .then(
                                if (isSelected) {
                                    Modifier.background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        MaterialTheme.shapes.medium
                                    )
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = iconName,
                            modifier = Modifier.size(24.dp),
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}