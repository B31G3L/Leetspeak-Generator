package com.beigel.leetSpeak_Generator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

@Composable
fun LeetCreationDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    existingLeet: CustomLeet? = null,
    leetIndex: Int = -1,
) {
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LeetInfoCard(
                        baseName = dialogState.baseName,
                        onBaseNameChange = { dialogState.baseName = it },
                        displayName = dialogState.displayName
                    )

                    TemplateSelectionCard(
                        selectedTemplate = dialogState.selectedTemplate,
                        onTemplateSelected = { template ->
                            dialogState.selectedTemplate = template
                            dialogState.applyTemplate()
                        }
                    )

                    TranslationTableCard(
                        alphabet = dialogState.alphabet,
                        translationStates = dialogState.translationStates
                    )
                }
            }
        }
    }
}

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
            Button(onClick = onSave, enabled = canSave) {
                Text(stringResource(R.string.leet_creation_save))
            }
        }
    }
}

@Composable
private fun rememberLeetCreationDialogState(existingLeet: CustomLeet?): LeetCreationDialogState {
    val defaultName = stringResource(R.string.leet_default_name)
    val fallbackName = stringResource(R.string.leet_fallback_name)
    return remember(existingLeet) {
        LeetCreationDialogState(existingLeet, defaultName, fallbackName).apply {
            if (existingLeet == null) applyTemplate()
        }
    }
}

class LeetCreationDialogState(
    existingLeet: CustomLeet?,
    private val defaultName: String,
    private val fallbackName: String
) {
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    var baseName by mutableStateOf(existingLeet?.name?.removeSuffix("-Leet") ?: "")
    var selectedTemplate by mutableStateOf(TemplateType.SIMPLE)

    val translationStates = alphabet.map { char ->
        mutableStateOf(existingLeet?.getTranslation(char.toString()) ?: char.toString())
    }

    val displayName: String
        get() = if (baseName.isBlank()) defaultName else "$baseName-Leet"

    fun applyTemplate() {
        val hasUserChanges = translationStates.indices.any { i ->
            translationStates[i].value != alphabet[i].toString()
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
        val translations = mutableMapOf<String, String>()
        alphabet.forEachIndexed { index, char ->
            translations[char.toString()] = translationStates[index].value
        }
        val finalName = displayName.ifEmpty { fallbackName }

        if (existingLeet == null) {
            viewModel.handleIntent(
                MainIntent.CreateLeet(
                    name = finalName,
                    useExtendedDefaults = selectedTemplate == TemplateType.EXTENDED,
                    customTranslations = translations
                )
            )
        } else {
            val updatedLeet = CustomLeet(name = finalName).apply { setTranslations(translations) }
            viewModel.handleIntent(MainIntent.UpdateLeet(leetIndex, updatedLeet))
        }
        onDismiss()
    }
}