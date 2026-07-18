package com.beigel.leetSpeak_Generator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.presentation.intent.MainIntent
import com.beigel.leetSpeak_Generator.ui.components.leet.creation.TemplateHelpers
import com.beigel.leetSpeak_Generator.ui.components.leet.creation.TemplateType
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel

/**
 * Erstellen/Bearbeiten-Vollbildschirm (Redesign v4) — ersetzt das alte Card-basierte Layout,
 * behält aber die State-/Speicherlogik (LeetCreationDialogState) unveraendert bei.
 * Header (Zurück + Titel), Name-Feld, Basis-Vorlage-Grid mit Live-Vorschau,
 * editierbare A–Z Übersetzungstabelle, volle Breite "Speichern"-Button unten.
 */
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
                // Header: Zurück-Chevron + Titel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onDismiss,
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.modi_back),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(if (existingLeet == null) R.string.leet_creation_title else R.string.leet_edit_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    NameField(
                        value = dialogState.baseName,
                        onValueChange = { dialogState.baseName = it }
                    )

                    TemplateGrid(
                        selectedTemplate = dialogState.selectedTemplate,
                        onTemplateSelected = { template ->
                            dialogState.selectedTemplate = template
                            dialogState.applyTemplate()
                        }
                    )

                    TranslationTableCardV4(
                        alphabet = dialogState.alphabet,
                        translationStates = dialogState.translationStates
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Speichern-Button (volle Breite, unten fixiert)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val canSave = dialogState.baseName.isNotBlank()
                    Button(
                        onClick = {
                            dialogState.saveLeet(
                                viewModel = viewModel,
                                existingLeet = existingLeet,
                                leetIndex = leetIndex,
                                onDismiss = onDismiss
                            )
                        },
                        enabled = canSave,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                            .height(50.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.leet_creation_save),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NameField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.leet_creation_base_name).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.leet_creation_name_example)) },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun TemplateGrid(
    selectedTemplate: TemplateType,
    onTemplateSelected: (TemplateType) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.leet_creation_template_title).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // 2-spaltiges Grid — bei uns 5 statt der 4 Vorlagen aus dem Handoff-Mock,
        // da die App zusätzlich "Numerisch" und "Symbole" als Vorlagen anbietet.
        TemplateType.entries.chunked(2).forEach { rowTemplates ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowTemplates.forEach { template ->
                    TemplateCard(
                        template = template,
                        isSelected = selectedTemplate == template,
                        onClick = { onTemplateSelected(template) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowTemplates.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: TemplateType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(template.displayNameRes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = TemplateHelpers.previewWord(template),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TranslationTableCardV4(
    alphabet: String,
    translationStates: List<MutableState<String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp
                )
            ) {
                Text(
                    text = stringResource(R.string.translation_table_card_title),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                )
            }

            Column(modifier = Modifier.padding(14.dp)) {
                for (i in 0 until 13) {
                    if (i > 0) Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EditableTranslationField(
                            plainChar = alphabet[i],
                            state = translationStates[i],
                            modifier = Modifier.weight(1f)
                        )
                        val rightIndex = i + 13
                        if (rightIndex < alphabet.length) {
                            EditableTranslationField(
                                plainChar = alphabet[rightIndex],
                                state = translationStates[rightIndex],
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableTranslationField(
    plainChar: Char,
    state: MutableState<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = plainChar.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(18.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(6.dp))
        OutlinedTextField(
            value = state.value,
            onValueChange = { state.value = it },
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
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
