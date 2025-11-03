package com.beigel.leetSpeak_Generator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.presentation.intent.MainIntent
import com.beigel.leetSpeak_Generator.ui.components.leet.creation.LeetInfoCard
import com.beigel.leetSpeak_Generator.ui.components.leet.creation.TemplateSelectionCard
import com.beigel.leetSpeak_Generator.ui.components.leet.creation.TemplateType
import com.beigel.leetSpeak_Generator.ui.components.leet.creation.TranslationTableCard
import com.beigel.leetSpeak_Generator.ui.components.leet.creation.TemplateHelpers
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel

/**
 * Leet Creation Dialog ohne Icon-Auswahl
 * UPDATED: Icon-Handling komplett entfernt
 */
@Composable
fun LeetCreationDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    existingLeet: CustomLeet? = null,
    leetIndex: Int = -1,
) {
    // State Management
    val dialogState = rememberLeetCreationDialogState(existingLeet)

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
                    // Leet Info Card (nur Name)
                    LeetInfoCard(
                        baseName = dialogState.baseName,
                        onBaseNameChange = { dialogState.baseName = it },
                        displayName = dialogState.displayName
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
        color = MaterialTheme.colorScheme.background,
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
                text = stringResource(if (isNewLeet) R.string.leet_creation_title else R.string.leet_edit_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.leet_creation_cancel))
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSave,
                enabled = canSave
            ) {
                Text(stringResource(R.string.leet_creation_save))
            }
        }
    }
}

/**
 * State Management für Leet Creation Dialog
 */
@Composable
private fun rememberLeetCreationDialogState(
    existingLeet: CustomLeet?
): LeetCreationDialogState {
    return remember(existingLeet) {
        LeetCreationDialogState(existingLeet).apply {
            if (existingLeet == null) {
                applyTemplate()
            }
        }
    }
}

/**
 * State-Klasse für Dialog-Verwaltung (ohne Icon)
 */
class LeetCreationDialogState(existingLeet: CustomLeet?) {
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    var baseName by mutableStateOf(
        existingLeet?.name?.removeSuffix("-Leet") ?: ""
    )

    var selectedTemplate by mutableStateOf(TemplateType.SIMPLE)

    // Translation states für alle Buchstaben
    val translationStates = alphabet.map { char ->
        mutableStateOf(
            existingLeet?.getTranslation(char.toString()) ?: char.toString()
        )
    }

    val displayName: String
        get() = if (baseName.isBlank()) "Neues Leet" else "$baseName-Leet"

    fun applyTemplate() {
        val hasUserChanges = translationStates.any { state ->
            val originalChar = alphabet[translationStates.indexOf(state)]
            state.value != originalChar.toString()
        }

        if (!hasUserChanges || selectedTemplate != TemplateType.CUSTOM) {
            TemplateHelpers.applyTemplate(selectedTemplate, translationStates, alphabet)
        }
    }

    fun saveLeet(
        viewModel: MainViewModel,
        existingLeet: CustomLeet?,
        leetIndex: Int,
        onDismiss: () -> Unit
    ) {
        // Übersetzungen sammeln
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
                    icon = androidx.compose.material.icons.Icons.Default.Settings, // Dummy - wird ignoriert
                    useExtendedDefaults = selectedTemplate == TemplateType.EXTENDED,
                    customTranslations = translations
                )
            )
        } else {
            // Bestehendes Leet aktualisieren
            val updatedLeet = CustomLeet(name = finalName).apply {
                setTranslations(translations)
            }

            viewModel.handleIntent(
                MainIntent.UpdateLeet(leetIndex, updatedLeet)
            )
        }

        onDismiss()
    }
}