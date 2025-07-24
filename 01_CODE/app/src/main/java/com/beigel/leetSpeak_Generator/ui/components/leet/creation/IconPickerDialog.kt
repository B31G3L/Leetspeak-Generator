package com.beigel.leetSpeak_Generator.ui.components.leet.creation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Dialog für Icon-Auswahl mit Material Icons
 * FIXED: Verwendet nur Material Icons - keine Drawable Resources
 */
@Composable
fun IconPickerDialog(
    selectedIcon: ImageVector, // ✅ Material Icon als Parameter
    onIconSelected: (ImageVector) -> Unit, // ✅ Gibt Material Icon zurück
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Material Icons als sichere Alternative zu Drawable Resources
    val availableIcons = listOf(
        Icons.Default.Settings,
        Icons.Default.Star,
        Icons.Default.Favorite,
        Icons.Default.Build,
        Icons.Default.Code,
        Icons.Default.Computer,
        Icons.Default.Games,
        Icons.Default.Sports,
        Icons.Default.Movie,
        Icons.Default.Book,
        Icons.Default.School,
        Icons.Default.Work,
        Icons.Default.Home,
        Icons.Default.Flight,
        Icons.Default.Restaurant,
        Icons.Default.Nature,
        Icons.Default.Pets,
        Icons.Default.Face,
        Icons.Default.Group,
        Icons.Default.Business,
        Icons.Default.Science,
        Icons.Default.PhotoCamera,
        Icons.Default.Videocam,
        Icons.Default.Gamepad,
        Icons.Default.SportsEsports,
        Icons.Default.Headphones,
        Icons.Default.Mic,
        Icons.Default.Security,
        Icons.Default.Shield,
        Icons.Default.Lock,
        Icons.Default.Key,
        Icons.Default.Diamond,
        Icons.Default.EmojiEvents,
        Icons.Default.Celebration,
        Icons.Default.LocalFireDepartment,
        Icons.Default.Bolt,
        Icons.Default.Psychology,
        Icons.Default.AutoAwesome,
        Icons.Default.Widgets,
        Icons.Default.Extension,
        Icons.Default.Memory,
        Icons.Default.Storage,
        Icons.Default.Cloud,
        Icons.Default.TextFields,
        Icons.Default.Transform,
        Icons.Default.Tune,
        Icons.Default.Palette,
        Icons.Default.Brush,
        Icons.Default.ColorLens,
        Icons.Default.Lightbulb,
        Icons.Default.Rocket,
        Icons.Default.Architecture,
        Icons.Default.Engineering,
        Icons.Default.Construction
    )

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
                modifier = Modifier.height(300.dp)
            ) {
                items(availableIcons) { icon ->
                    IconButton(
                        onClick = {
                            onIconSelected(icon) // ✅ Direkte Icon-Übergabe
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .then(
                                if (selectedIcon == icon) {
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
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (selectedIcon == icon) {
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