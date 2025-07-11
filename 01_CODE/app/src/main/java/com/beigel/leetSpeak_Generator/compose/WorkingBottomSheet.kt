// 1. NEUE DATEI: WorkingBottomSheet.kt
// Erstelle: app/src/main/java/com/beigel/leetSpeak_Generator/compose/WorkingBottomSheet.kt

package com.beigel.leetSpeak_Generator.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beigel.leetSpeak_Generator.LeetOption
import com.beigel.leetSpeak_Generator.MainIntent
import com.beigel.leetSpeak_Generator.MainViewModel
import com.beigel.leetSpeak_Generator.ProfileManager
import com.beigel.leetSpeak_Generator.R

/**
 * Funktionierende Bottom Sheet Implementation ohne experimentelle APIs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkingBottomSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val leetOptions by viewModel.leetOptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteLeetOptions by viewModel.favoriteLeetOptions.collectAsStateWithLifecycle(initialValue = emptyList())

    var showProfileDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingOption by remember { mutableStateOf<LeetOption?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(width = 32.dp, height = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(2.dp)
            ) {}
        },
        windowInsets = WindowInsets(0)
    ) {
        WorkingBottomSheetContent(
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
                editingOption = option
                showEditDialog = true
            },
            onCreateNew = {
                showProfileDialog = true
            },
            onShowTable = { option ->
                // Temporär: Zeige Info Toast
                // Später: Zeige Translation Table Dialog
            }
        )

        // Spacer für bessere Bedienung
        Spacer(modifier = Modifier.height(16.dp))
    }

    // Profile Creation Dialog
    if (showProfileDialog) {
        WorkingProfileDialog(
            viewModel = viewModel,
            onDismiss = {
                showProfileDialog = false
                onDismiss() // Close bottom sheet after creation
            }
        )
    }

    // Profile Edit Dialog
    if (showEditDialog && editingOption != null) {
        val profiles by viewModel.profiles.collectAsStateWithLifecycle()
        val profile = profiles.getOrNull(editingOption!!.customIndex)

        if (profile != null) {
            WorkingProfileDialog(
                viewModel = viewModel,
                existingProfile = profile,
                profileIndex = editingOption!!.customIndex,
                onDismiss = {
                    showEditDialog = false
                    editingOption = null
                }
            )
        }
    }
}

@Composable
private fun WorkingBottomSheetContent(
    leetOptions: List<LeetOption>,
    favoriteLeetOptions: List<LeetOption>,
    onOptionSelected: (LeetOption) -> Unit,
    onToggleFavorite: (LeetOption) -> Unit,
    onEditOption: (LeetOption) -> Unit,
    onCreateNew: () -> Unit,
    onShowTable: (LeetOption) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        WorkingBottomSheetHeader(onCreateNew = onCreateNew)

        // Favorites Section
        if (favoriteLeetOptions.isNotEmpty()) {
            WorkingFavoritesSection(
                favoriteOptions = favoriteLeetOptions,
                onOptionSelected = onOptionSelected,
                onToggleFavorite = onToggleFavorite
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // All Options Section
        WorkingAllOptionsSection(
            leetOptions = leetOptions,
            onOptionSelected = onOptionSelected,
            onToggleFavorite = onToggleFavorite,
            onEditOption = onEditOption,
            onShowTable = onShowTable
        )
    }
}

@Composable
private fun WorkingBottomSheetHeader(
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
private fun WorkingFavoritesSection(
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
                text = "★ Favoriten",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(favoriteOptions, key = { "${it.name}_${it.customIndex}" }) { option ->
                WorkingCompactCard(
                    option = option,
                    onOptionSelected = onOptionSelected,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    }
}

@Composable
private fun WorkingAllOptionsSection(
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
            items(leetOptions, key = { "${it.name}_${it.customIndex}" }) { option ->
                WorkingOptionCard(
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
private fun WorkingOptionCard(
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
                        if (option.isFavorite) {
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
                if (option.isSelected) {
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
            WorkingPreviewSection(
                option = option,
                onToggleFavorite = onToggleFavorite,
                onEditOption = onEditOption,
                onShowTable = onShowTable
            )
        }
    }
}

@Composable
private fun WorkingPreviewSection(
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
                        text = generateWorkingPreview(option),
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
private fun WorkingCompactCard(
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
            // Icon with indicators
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
                textAlign = TextAlign.Center,
                maxLines = 2,
                fontWeight = if (option.isSelected) FontWeight.Bold else FontWeight.Normal
            )

            // Preview
            Text(
                text = generateWorkingPreview(option),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

// Helper function für Preview Generation
private fun generateWorkingPreview(option: LeetOption): String {
    return when (option.mode) {
        ProfileManager.MODE_SIMPLE -> "H3110"
        ProfileManager.MODE_EXTENDED -> "#3110"
        ProfileManager.MODE_CUSTOM -> "H3ll0"
        else -> "H3110"
    }
}

// 2. ERWEITERE: WorkingBottomSheet.kt - Profile Dialog hinzufügen

/**
 * Funktionierende Profile Creation/Edit Dialog
 */
@Composable
fun WorkingProfileDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    existingProfile: com.beigel.leetSpeak_Generator.CustomProfile? = null,
    profileIndex: Int = -1
) {
    var profileName by remember {
        mutableStateOf(existingProfile?.name ?: "")
    }

    var selectedIconResId by remember {
        mutableStateOf(existingProfile?.iconResId ?: R.drawable.ic_custom_mode)
    }

    var showIconPicker by remember { mutableStateOf(false) }

    val isNewProfile = existingProfile == null

    // Translation states für alle Buchstaben (vereinfacht)
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val translationStates = remember {
        alphabet.map { char ->
            mutableStateOf(
                existingProfile?.getTranslation(char.toString()) ?: char.toString()
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isNewProfile) "Neues Profil erstellen" else "Profil bearbeiten")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Profile Info Section
                WorkingProfileInfoSection(
                    profileName = profileName,
                    onProfileNameChange = { profileName = it },
                    selectedIconResId = selectedIconResId,
                    onIconClick = { showIconPicker = true }
                )

                // Simplified Translation Section
                WorkingSimpleTranslationSection(
                    translationStates = translationStates,
                    alphabet = alphabet
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Save profile
                    saveWorkingProfile(
                        viewModel = viewModel,
                        profileName = profileName.ifEmpty { "Custom Profile" },
                        selectedIconResId = selectedIconResId,
                        translationStates = translationStates,
                        alphabet = alphabet,
                        isNewProfile = isNewProfile,
                        profileIndex = profileIndex
                    )
                    onDismiss()
                }
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )

    // Icon Picker Dialog
    if (showIconPicker) {
        WorkingIconPickerDialog(
            selectedIconResId = selectedIconResId,
            onIconSelected = { newIconResId ->
                selectedIconResId = newIconResId
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}

@Composable
private fun WorkingProfileInfoSection(
    profileName: String,
    onProfileNameChange: (String) -> Unit,
    selectedIconResId: Int,
    onIconClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Profil-Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon Button
                IconButton(
                    onClick = onIconClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(selectedIconResId),
                        contentDescription = "Icon ändern",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name Input
                OutlinedTextField(
                    value = profileName,
                    onValueChange = onProfileNameChange,
                    label = { Text("Profilname") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Text(
                text = "Tippe auf das Icon, um es zu ändern",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun WorkingSimpleTranslationSection(
    translationStates: List<MutableState<String>>,
    alphabet: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Header
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Übersetzungen (Top 8 Buchstaben)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Simplified Grid für die wichtigsten Buchstaben
            val importantLetters = listOf('A', 'E', 'H', 'I', 'L', 'O', 'S', 'T')

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                importantLetters.chunked(2).forEach { rowLetters ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowLetters.forEach { letter ->
                            val index = alphabet.indexOf(letter)
                            if (index >= 0) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = letter.toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(20.dp)
                                    )

                                    Text("→", color = MaterialTheme.colorScheme.secondary)

                                    OutlinedTextField(
                                        value = translationStates[index].value,
                                        onValueChange = { translationStates[index].value = it },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    )
                                }
                            }
                        }

                        // Fill remaining space if odd number
                        if (rowLetters.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Text(
                    text = "Tipp: Andere Buchstaben bleiben unverändert",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun WorkingIconPickerDialog(
    selectedIconResId: Int,
    onIconSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val availableIcons = listOf(
        R.drawable.ic_custom_mode,
        R.drawable.ic_simple_mode,
        R.drawable.ic_extended_mode,
        R.drawable.ic_about,
        R.drawable.ic_add_profile,
        R.drawable.ic_edit,
        R.drawable.ic_favorite_border,
        R.drawable.ic_favorite
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Icon auswählen") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                items(availableIcons.chunked(4)) { iconRow ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        iconRow.forEach { iconResId ->
                            IconButton(
                                onClick = { onIconSelected(iconResId) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .weight(1f)
                            ) {
                                Icon(
                                    painter = painterResource(iconResId),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = if (iconResId == selectedIconResId) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        // Fill remaining space
                        repeat(4 - iconRow.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fertig")
            }
        }
    )
}

// Helper function to save profile
private fun saveWorkingProfile(
    viewModel: MainViewModel,
    profileName: String,
    selectedIconResId: Int,
    translationStates: List<MutableState<String>>,
    alphabet: String,
    isNewProfile: Boolean,
    profileIndex: Int
) {
    try {
        val translations = mutableMapOf<String, String>()

        // Collect translations from states
        alphabet.forEachIndexed { index, char ->
            translations[char.toString()] = translationStates[index].value
        }

        if (isNewProfile) {
            viewModel.handleIntent(
                com.beigel.leetSpeak_Generator.MainIntent.CreateProfile(
                    name = profileName,
                    iconResId = selectedIconResId,
                    useExtendedDefaults = false
                )
            )
        } else {
            val updatedProfile = com.beigel.leetSpeak_Generator.CustomProfile(profileName, selectedIconResId)
            updatedProfile.setTranslations(translations)
            viewModel.handleIntent(
                com.beigel.leetSpeak_Generator.MainIntent.UpdateProfile(profileIndex, updatedProfile)
            )
        }
    } catch (e: Exception) {
        // Error handling - könnte erweitert werden
        println("Error saving profile: ${e.message}")
    }
}