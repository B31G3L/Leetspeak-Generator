package com.beigel.leetSpeak_Generator.ui.components.leet.creation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


/**
 * Dialog für Icon-Auswahl mit Material Icons
 * FIXED: Removed unresolved 'Flash' reference and added proper icons
 */
@Composable
fun IconPickerDialog(
    selectedIcon: ImageVector,
    onIconSelected: (ImageVector) -> Unit,
    onDismiss: () -> Unit,
) {
    // Kuratierte Liste von Material Icons für verschiedene Leet-Kategorien
    val availableIcons = listOf(
        // Basis Icons
        Icons.Default.Settings,
        Icons.Default.Build,
        Icons.Default.Code,
        Icons.Default.Computer,

        // Gaming & Entertainment
        Icons.Default.Games,
        Icons.Default.SportsEsports,
        Icons.Default.Gamepad,
        Icons.Default.Sports,
        Icons.Default.Movie,
        Icons.Default.Videocam,
        Icons.Default.PhotoCamera,

        // Tech & Science
        Icons.Default.Science,
        Icons.Default.Memory,
        Icons.Default.Storage,
        Icons.Default.Psychology,
        Icons.Default.AutoAwesome,
        Icons.Default.Widgets,
        Icons.Default.Extension,

        // Creative & Design
        Icons.Default.Palette,
        Icons.Default.Brush,
        Icons.Default.Create,
        Icons.Default.Diamond,
        Icons.Default.Star,
        Icons.Default.Favorite,

        // Professional
        Icons.Default.Work,
        Icons.Default.Business,
        Icons.Default.School,
        Icons.Default.Book,
        Icons.AutoMirrored.Filled.Assignment,

        // Lifestyle
        Icons.Default.Home,
        Icons.Default.Flight,
        Icons.Default.Restaurant,
        Icons.Default.Nature,
        Icons.Default.Pets,
        Icons.Default.Face,
        Icons.Default.Group,

        // Audio & Music
        Icons.Default.Headphones,
        Icons.Default.Mic,
        Icons.Default.MusicNote,
        Icons.AutoMirrored.Filled.VolumeUp,

        // Security & Tools
        Icons.Default.Security,
        Icons.Default.Shield,
        Icons.Default.Lock,
        Icons.Default.Key,
        Icons.Default.VpnKey,

        // Special Effects - FIXED: Replaced 'Flash' with valid icons
        Icons.Default.LocalFireDepartment,
        Icons.Default.Bolt,
        Icons.Default.FlashOn, // FIXED: Use FlashOn instead of Flash
        Icons.Default.Celebration,
        Icons.Default.EmojiEvents,

        // Cloud & Storage
        Icons.Default.Cloud,
        Icons.Default.CloudDownload,
        Icons.Default.CloudUpload,
        Icons.Default.Backup,

        // Communication
        Icons.AutoMirrored.Filled.Chat,
        Icons.AutoMirrored.Filled.Message,
        Icons.Default.Email,
        Icons.Default.Phone,

        // Transport
        Icons.Default.DirectionsCar,
        Icons.Default.Train,
        Icons.Default.Motorcycle,
        Icons.Default.RocketLaunch
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
                modifier = Modifier.height(400.dp) // Größer für mehr Icons
            ) {
                items(availableIcons) { icon ->
                    val isSelected = selectedIcon == icon

                    IconButton(
                        onClick = { onIconSelected(icon) },
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
                            contentDescription = null,
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