@file:OptIn(ExperimentalMaterial3Api::class)

package com.beigel.leetSpeak_Generator.compose

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beigel.leetSpeak_Generator.LeetOption
import com.beigel.leetSpeak_Generator.MainIntent
import com.beigel.leetSpeak_Generator.MainViewModel
import com.beigel.leetSpeak_Generator.ProfileManager
import com.beigel.leetSpeak_Generator.R

/**
 * Enhanced Compose Bottom Sheet für Leet Selector mit integrierten Dialogen
 * Vollständige Funktionalität ohne XML Dependencies
 */
@Composable
fun LeetSelectorBottomSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val leetOptions by viewModel.leetOptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteLeetOptions by viewModel.favoriteLeetOptions.collectAsStateWithLifecycle(initialValue = emptyList())

    // Dialog States
    var showProfileCreationDialog by remember { mutableStateOf(false) }
    var showProfileEditDialog by remember { mutableStateOf(false) }
    var showTranslationTableDialog by remember { mutableStateOf(false) }

    // Current selections for dialogs
    var currentEditOption by remember { mutableStateOf<LeetOption?>(null) }
    var currentTableOption by remember { mutableStateOf<LeetOption?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(width = 32.dp, height = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.extraLarge
            ) {}
        }
    ) {
        LeetSelectorContent(
            leetOptions = leetOptions,
            favoriteLeetOptions = favoriteLeetOptions,
            onOptionSelected = { option ->
                viewModel.handleIntent(MainIntent.ChangeMode(option))
                onDismiss()
            },
            onToggleFavorite = { option ->
                viewModel.handleIntent(MainIntent.ToggleFavorite(option))
            },
            onEditOption = { option ->
                currentEditOption = option
                showProfileEditDialog = true
            },
            onShowTable = { option ->
                currentTableOption = option
                showTranslationTableDialog = true
            },
            onCreateNew = {
                showProfileCreationDialog = true
            }
        )
    }

    // Profile Creation Dialog
    if (showProfileCreationDialog) {
        ProfileCreationDialog(
            viewModel = viewModel,
            onDismiss = {
                showProfileCreationDialog = false
                onDismiss() // Close bottom sheet after creation
            }
        )
    }

    // Profile Edit Dialog
    if (showProfileEditDialog) {
        currentEditOption?.let { option ->
            if (option.isCustom) {
                val profiles by viewModel.profiles.collectAsStateWithLifecycle()
                val profile = profiles.getOrNull(option.customIndex)

                if (profile != null) {
                    ProfileCreationDialog(
                        viewModel = viewModel,
                        existingProfile = profile,
                        profileIndex = option.customIndex,
                        onDismiss = {
                            showProfileEditDialog = false
                            currentEditOption = null
                        }
                    )
                }
            }
        }
    }

    // Translation Table Dialog
    if (showTranslationTableDialog) {
        currentTableOption?.let { option ->
            TranslationTableDialog(
                leetOption = option,
                viewModel = viewModel,
                onDismiss = {
                    showTranslationTableDialog = false
                    currentTableOption = null
                }
            )
        }
    }
}

@Composable
fun LeetSelectorContent(
    leetOptions: List<LeetOption>,
    favoriteLeetOptions: List<LeetOption>,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit,
    onEditOption: (LeetOption) -> Unit,
    onShowTable: (LeetOption) -> Unit,
    onCreateNew: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        // Header
        LeetSelectorHeader(onCreateNew = onCreateNew)

        // Favorites Section
        AnimatedVisibility(
            visible = favoriteLeetOptions.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            FavoritesSection(
                favoriteOptions = favoriteLeetOptions,
                onOptionSelected = onOptionSelected,
                onToggleFavorite = onToggleFavorite
            )
        }

        // All Options Section
        AllOptionsSection(
            leetOptions = leetOptions,
            onOptionSelected = onOptionSelected,
            onToggleFavorite = onToggleFavorite,
            onEditOption = onEditOption,
            onShowTable = onShowTable
        )
    }
}

@Composable
private fun LeetSelectorHeader(
    onCreateNew: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Leet-Modus auswählen",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        FilledTonalButton(
            onClick = onCreateNew,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Neu")
        }
    }
}

@Composable
private fun FavoritesSection(
    favoriteOptions: List<LeetOption>,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Favoriten",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(favoriteOptions, key = { it.name + it.customIndex }) { option ->
                CompactLeetCard(
                    option = option,
                    onOptionSelected = onOptionSelected,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    }
}

@Composable
private fun AllOptionsSection(
    leetOptions: List<LeetOption>,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit,
    onEditOption: (LeetOption) -> Unit,
    onShowTable: (LeetOption) -> Unit
) {
    Column {
        Text(
            text = "Alle Leet-Modi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            items(leetOptions, key = { it.name + it.customIndex }) { option ->
                LeetOptionCard(
                    option = option,
                    onOptionSelected = onOptionSelected,
                    onToggleFavorite = onToggleFavorite,
                    onEditOption = onEditOption,
                    onShowTable = onShowTable
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeetOptionCard(
    option: LeetOption,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit,
    onEditOption: (LeetOption) -> Unit,
    onShowTable: (LeetOption) -> Unit
) {
    Card(
        onClick = { onOptionSelected(option) },
        modifier = Modifier.fillMaxWidth(),
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
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (option.isSelected) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Main content row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Icon(
                    painter = painterResource(option.iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )

                        // Favorite indicator
                        AnimatedVisibility(
                            visible = option.isFavorite,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorite",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Text(
                        text = option.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Selected indicator
                AnimatedVisibility(
                    visible = option.isSelected,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preview section with actions
            PreviewSection(
                option = option,
                onToggleFavorite = onToggleFavorite,
                onEditOption = onEditOption,
                onShowTable = onShowTable
            )
        }
    }
}

@Composable
private fun PreviewSection(
    option: LeetOption,
    onToggleFavorite: (LeetOption) -> Unit,
    onEditOption: (LeetOption) -> Unit,
    onShowTable: (LeetOption) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preview text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hello",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = generatePreview(option),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Action buttons
            Row {
                // Favorite button
                IconButton(
                    onClick = { onToggleFavorite(option) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (option.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                // Table button
                IconButton(
                    onClick = { onShowTable(option) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = "Show table",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                // Edit button (only for custom)
                if (option.isCustom) {
                    IconButton(
                        onClick = { onEditOption(option) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactLeetCard(
    option: LeetOption,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit
) {
    Card(
        onClick = { onOptionSelected(option) },
        modifier = Modifier.width(120.dp),
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
            // Icon with favorite overlay
            Box {
                Icon(
                    painter = painterResource(option.iconResId),
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
                fontWeight = if (option.isSelected) FontWeight.Bold else FontWeight.Normal
            )

            // Preview
            Text(
                text = generatePreview(option),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1
            )
        }
    }
}

// Helper function für Preview Generation
private fun generatePreview(option: LeetOption): String {
    return when (option.mode) {
        ProfileManager.MODE_SIMPLE -> "H3110"
        ProfileManager.MODE_EXTENDED -> "#3110"
        ProfileManager.MODE_CUSTOM -> "H3ll0"
        else -> "H3110"
    }
}

/**
 * Translation Table Dialog in Compose
 */
@Composable
fun TranslationTableDialog(
    leetOption: LeetOption,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Übersetzungstabelle - ${leetOption.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.secondary)
                )
            ) {
                Column {
                    // Table Header
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Plain",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                text = "Leet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                text = "Plain",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                text = "Leet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    // Translation Rows
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(8.dp)
                    ) {
                        items(13) { i ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                // Left side (A-M)
                                Text(
                                    text = alphabet[i].toString(),
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = getTranslatedCharForOption(alphabet[i], leetOption, viewModel),
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )

                                // Right side (N-Z)
                                if (i + 13 < alphabet.length) {
                                    Text(
                                        text = alphabet[i + 13].toString(),
                                        modifier = Modifier.weight(1f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = getTranslatedCharForOption(alphabet[i + 13], leetOption, viewModel),
                                        modifier = Modifier.weight(1f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(2f))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

private fun getTranslatedCharForOption(char: Char, leetOption: LeetOption, viewModel: MainViewModel): String {
    // Vereinfachte Preview-Generation
    return when (leetOption.mode) {
        ProfileManager.MODE_SIMPLE -> {
            when (char) {
                'A' -> "4"
                'E' -> "3"
                'H' -> "#"
                'I' -> "1"
                'L' -> "L"
                'O' -> "0"
                'S' -> "5"
                'T' -> "7"
                'B' -> "8"
                'G' -> "6"
                'Z' -> "2"
                else -> char.toString()
            }
        }
        ProfileManager.MODE_EXTENDED -> {
            when (char) {
                'A' -> "4"
                'B' -> "8"
                'C' -> "("
                'D' -> "|)"
                'E' -> "3"
                'F' -> "|="
                'G' -> "6"
                'H' -> "#"
                'I' -> "!"
                'J' -> "_|"
                'K' -> "|<"
                'L' -> "1"
                'M' -> "/\\/\\"
                'N' -> "|\\|"
                'O' -> "0"
                'P' -> "9"
                'Q' -> "0_"
                'R' -> "2"
                'S' -> "5"
                'T' -> "7"
                'U' -> "|_|"
                'V' -> "\\/"
                'W' -> "\\/\\/"
                'X' -> "><"
                'Y' -> "`/"
                'Z' -> "Z"
                else -> char.toString()
            }
        }
        ProfileManager.MODE_CUSTOM -> {
            // Für Custom Profile - würde echte Übersetzung verwenden
            char.toString() // Placeholder
        }
        else -> char.toString()
    }
}