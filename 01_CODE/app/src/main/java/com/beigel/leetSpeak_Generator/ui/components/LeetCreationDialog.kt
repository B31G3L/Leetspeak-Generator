package com.beigel.leetSpeak_Generator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.presentation.intent.MainIntent
import com.beigel.leetSpeak_Generator.ui.components.leet.creation.IconPickerDialog
import com.beigel.leetSpeak_Generator.ui.components.leet.creation.LeetInfoCard
import com.beigel.leetSpeak_Generator.ui.components.leet.creation.TemplateHelpers
import com.beigel.leetSpeak_Generator.ui.components.leet.creation.TemplatePickerDialog
import com.beigel.leetSpeak_Generator.ui.components.leet.creation.TemplateSelectionCard
import com.beigel.leetSpeak_Generator.ui.components.leet.creation.TemplateType
import com.beigel.leetSpeak_Generator.ui.components.leet.creation.TranslationTableCard
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel

/**
 * Leet Creation Dialog - Mit Material Icons
 * FIXED: Alle Drawable-Referenzen durch Material Icons ersetzt
 */
@Composable
fun LeetCreationDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    existingLeet: CustomLeet? = null,
    leetIndex: Int = -1,
    modifier: Modifier = Modifier
) {
    // State Management
    val dialogState = rememberLeetCreationDialogState(existingLeet)
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Toolbar
                LeetCreationHeader(
                    isNewLeet = existingLeet == null,
                    canSave = dialogState.baseName.isNotBlank(),
                    onDismiss = onDismiss,
                    onSave = {
                        dialogState.saveLeet(
                            viewModel = viewModel,
                            existingLeet = existingLeet,
                            leetIndex = leetIndex,
                            onDismiss = onDismiss
                        )
                    }
                )

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Leet Info Card (Name + Icon)
                    LeetInfoCard(
                        baseName = dialogState.baseName,
                        onBaseNameChange = { dialogState.baseName = it },
                        displayName = dialogState.displayName,
                        selectedIcon = dialogState.selectedIcon, // ✅ Material Icon
                        onIconClick = { dialogState.showIconPicker = true }
                    )

                    // Template Selection Card
                    TemplateSelectionCard(
                        selectedTemplate = dialogState.selectedTemplate,
                        onTemplateSelected = { template ->
                            dialogState.selectedTemplate = template
                            dialogState.applyTemplate()
                        }
                    )

                    // Translation Table Card
                    TranslationTableCard(
                        alphabet = dialogState.alphabet,
                        translationStates = dialogState.translationStates
                    )
                }
            }
        }
    }

    // Dialog Management
    LeetCreationDialogManager(
        dialogState = dialogState,
        onDismissIconPicker = { dialogState.showIconPicker = false },
        onDismissTemplatePicker = { dialogState.showTemplatePicker = false }
    )
}

/**
 * Header mit Toolbar-Style für Dialog
 */
@Composable
private fun LeetCreationHeader(
    isNewLeet: Boolean,
    canSave: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isNewLeet) "Leet erstellen" else "Leet bearbeiten",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSave,
                enabled = canSave
            ) {
                Text("Speichern")
            }
        }
    }
}

/**
 * Dialog Manager für Icon und Template Picker
 */
@Composable
private fun LeetCreationDialogManager(
    dialogState: LeetCreationDialogState,
    onDismissIconPicker: () -> Unit,
    onDismissTemplatePicker: () -> Unit
) {
    // Icon Picker Dialog
    if (dialogState.showIconPicker) {
        IconPickerDialog(
            selectedIcon = dialogState.selectedIcon, // ✅ Material Icon
            onIconSelected = { icon ->
                dialogState.selectedIcon = icon // ✅ Setzt Material Icon
                onDismissIconPicker()
            },
            onDismiss = onDismissIconPicker
        )
    }

    // Template Picker Dialog (Alternative zur Inline-Auswahl)
    if (dialogState.showTemplatePicker) {
        TemplatePickerDialog(
            selectedTemplate = dialogState.selectedTemplate,
            onTemplateSelected = { template ->
                dialogState.selectedTemplate = template
                dialogState.applyTemplate()
                onDismissTemplatePicker()
            },
            onDismiss = onDismissTemplatePicker
        )
    }
}

/**
 * State Management für Leet Creation Dialog
 * FIXED: Verwendet Material Icons statt Drawable Resources
 */
@Composable
private fun rememberLeetCreationDialogState(
    existingLeet: CustomLeet?
): LeetCreationDialogState {
    return remember(existingLeet) {
        LeetCreationDialogState(existingLeet).apply {
            // Template automatisch beim ersten Laden anwenden
            if (existingLeet == null) {
                applyTemplate()
            }
        }
    }
}

/**
 * State-Klasse für Dialog-Verwaltung
 * FIXED: Verwendet Material Icons statt Drawable Resources
 */
class LeetCreationDialogState(existingLeet: CustomLeet?) {
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    var baseName by mutableStateOf(
        existingLeet?.name?.removeSuffix("-Leet") ?: ""
    )

    var selectedIcon by mutableStateOf(
        existingLeet?.iconImageVector ?: Icons.Default.Settings // ✅ Material Icon statt ResId
    )

    var selectedTemplate by mutableStateOf(TemplateType.SIMPLE)
    var showIconPicker by mutableStateOf(false)
    var showTemplatePicker by mutableStateOf(false)

    // Translation states für alle Buchstaben
    val translationStates = alphabet.map { char ->
        mutableStateOf(
            existingLeet?.getTranslation(char.toString()) ?: char.toString()
        )
    }

    val displayName: String
        get() = if (baseName.isBlank()) "Neues Leet" else "$baseName-Leet"

    /**
     * Wendet das Template auf die Übersetzungstabelle an
     */
    fun applyTemplate() {
        TemplateHelpers.applyTemplate(selectedTemplate, translationStates, alphabet)
    }

    fun saveLeet(
        viewModel: MainViewModel,
        existingLeet: CustomLeet?,
        leetIndex: Int,
        onDismiss: () -> Unit
    ) {
        val translations = mutableMapOf<String, String>()
        alphabet.forEachIndexed { index, char ->
            translations[char.toString()] = translationStates[index].value
        }

        val finalName = displayName.ifEmpty { "Custom-Leet" }

        if (existingLeet == null) {
            // Neues Leet erstellen
            viewModel.handleIntent(
                MainIntent.CreateLeet(
                    name = finalName,
                    icon = selectedIcon, // ✅ Übergebe Material Icon
                    useExtendedDefaults = selectedTemplate == TemplateType.EXTENDED
                )
            )
        } else {
            // Bestehendes Leet aktualisieren
            val updatedLeet = CustomLeet(
                name = finalName,
                iconImageVector = selectedIcon // ✅ Verwende Material Icon
            ).apply {
                setTranslations(translations)
            }

            viewModel.handleIntent(
                MainIntent.UpdateLeet(leetIndex, updatedLeet)
            )
        }

        onDismiss()
    }
}