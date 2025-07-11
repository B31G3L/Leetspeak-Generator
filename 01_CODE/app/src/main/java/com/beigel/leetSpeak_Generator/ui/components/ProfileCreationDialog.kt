package com.beigel.leetSpeak_Generator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.viewmodel.MainIntent
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel

/**
 * Compose Implementation für Profile Creation Dialog
 * Ersetzt dialog_comprehensive_edit.xml
 */
@Composable
fun ProfileCreationDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    existingLeet: CustomLeet? = null,
    profileIndex: Int = -1
) {
    var profileName by remember {
        mutableStateOf(existingLeet?.name ?: "")
    }

    var selectedIconResId by remember {
        mutableStateOf(existingLeet?.iconResId ?: R.drawable.ic_custom_mode)
    }

    var showIconPicker by remember { mutableStateOf(false) }

    // Translation states für alle Buchstaben
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val translationStates = remember {
        alphabet.map { char ->
            mutableStateOf(
                existingLeet?.getTranslation(char.toString()) ?: char.toString()
            )
        }
    }

    val isNewProfile = existingLeet == null
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Fullscreen
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header mit Toolbar Style
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isNewProfile) "Profil erstellen" else "Profil bearbeiten",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = onDismiss
                        ) {
                            Text("Abbrechen")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                // Profile speichern
                                val translations = mutableMapOf<String, String>()
                                alphabet.forEachIndexed { index, char ->
                                    translations[char.toString()] = translationStates[index].value
                                }

                                if (isNewProfile) {
                                    // Neues Profile erstellen
                                    viewModel.handleIntent(
                                        MainIntent.CreateProfile(
                                            name = profileName.ifEmpty { "Custom Profile" },
                                            iconResId = selectedIconResId,
                                            useExtendedDefaults = false
                                        )
                                    )
                                } else {
                                    // Bestehendes Profile aktualisieren
                                    val updatedProfile = CustomLeet(
                                        name = profileName,
                                        iconResId = selectedIconResId
                                    ).apply {
                                        setTranslations(translations)
                                    }

                                    viewModel.handleIntent(
                                        MainIntent.UpdateProfile(profileIndex, updatedProfile)
                                    )
                                }

                                onDismiss()
                            }
                        ) {
                            Text("Speichern")
                        }
                    }
                }

                // Scrollbarer Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Info Card
                    ProfileInfoCard(
                        profileName = profileName,
                        onProfileNameChange = { profileName = it },
                        selectedIconResId = selectedIconResId,
                        onIconClick = { showIconPicker = true }
                    )

                    // Translation Table Card
                    TranslationTableCard(
                        alphabet = alphabet,
                        translationStates = translationStates
                    )
                }
            }
        }
    }

    // Icon Picker Dialog
    if (showIconPicker) {
        IconPickerDialog(
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
private fun ProfileInfoCard(
    profileName: String,
    onProfileNameChange: (String) -> Unit,
    selectedIconResId: Int,
    onIconClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                text = "Profil-Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
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
private fun TranslationTableCard(
    alphabet: String,
    translationStates: List<MutableState<String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.secondary)
        )
    ) {
        Column {
            // Header
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Übersetzungstabelle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Column Headers
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Plain",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Leet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Plain",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Leet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Translation Rows
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                for (i in 0 until 13) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        // Left side (A-M)
                        Text(
                            text = alphabet[i].toString(),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        OutlinedTextField(
                            value = translationStates[i].value,
                            onValueChange = { translationStates[i].value = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                textAlign = TextAlign.Center
                            )
                        )

                        // Right side (N-Z)
                        if (i + 13 < alphabet.length) {
                            Text(
                                text = alphabet[i + 13].toString(),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            OutlinedTextField(
                                value = translationStates[i + 13].value,
                                onValueChange = { translationStates[i + 13].value = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    textAlign = TextAlign.Center
                                )
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(2f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconPickerDialog(
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(availableIcons) { iconResId ->
                    IconButton(
                        onClick = { onIconSelected(iconResId) },
                        modifier = Modifier
                            .size(48.dp)
                            .then(
                                if (iconResId == selectedIconResId) {
                                    Modifier.padding(2.dp)
                                } else {
                                    Modifier
                                }
                            )
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
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}