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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.manager.LeetManager
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel

@Composable
fun TranslationTableDialog(
    leetOption: LeetOption,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                TranslationTableHeader(
                    title = stringResource(R.string.translation_table_dialog_title, leetOption.name),
                    onDismiss = onDismiss
                )
                TranslationTableContent(
                    alphabet = alphabet,
                    leetOption = leetOption,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun TranslationTableHeader(title: String, onDismiss: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(
                    text = stringResource(R.string.translation_table_close),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TranslationTableContent(
    alphabet: String,
    leetOption: LeetOption,
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TranslationTableColumnHeaders()
        Spacer(modifier = Modifier.height(8.dp))

        repeat(13) { rowIndex ->
            TranslationTableRow(
                leftChar = alphabet[rowIndex],
                leftTranslation = getTranslationFor(alphabet[rowIndex], leetOption, viewModel),
                rightChar = if (rowIndex + 13 < alphabet.length) alphabet[rowIndex + 13] else null,
                rightTranslation = if (rowIndex + 13 < alphabet.length)
                    getTranslationFor(alphabet[rowIndex + 13], leetOption, viewModel) else null,
                isEvenRow = rowIndex % 2 == 0
            )
        }
    }
}

@Composable
private fun TranslationTableColumnHeaders() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            listOf(
                Pair(stringResource(R.string.translation_table_plain), MaterialTheme.colorScheme.onSurfaceVariant),
                Pair(stringResource(R.string.translation_table_leet), MaterialTheme.colorScheme.secondary),
                Pair(stringResource(R.string.translation_table_plain), MaterialTheme.colorScheme.onSurfaceVariant),
                Pair(stringResource(R.string.translation_table_leet), MaterialTheme.colorScheme.secondary),
            ).forEach { (label, color) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TranslationTableRow(
    leftChar: Char,
    leftTranslation: String,
    rightChar: Char?,
    rightTranslation: String?,
    isEvenRow: Boolean
) {
    Surface(
        color = if (isEvenRow) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = leftChar.toString(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = leftTranslation,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            if (rightChar != null && rightTranslation != null) {
                Text(
                    text = rightChar.toString(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = rightTranslation,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Spacer(modifier = Modifier.weight(2f))
            }
        }
    }
}

private fun getTranslationFor(
    char: Char,
    leetOption: LeetOption,
    viewModel: MainViewModel
): String {
    return when (leetOption.mode) {
        LeetManager.MODE_SIMPLE ->
            LeetTranslator.translateChar(char, LeetTranslator.TranslationMode.SIMPLE)

        LeetManager.MODE_EXTENDED ->
            LeetTranslator.translateChar(char, LeetTranslator.TranslationMode.EXTENDED)

        LeetManager.MODE_CUSTOM -> {
            val leet = viewModel.leets.value.getOrNull(leetOption.customIndex)
            leet?.getTranslation(char.toString()) ?: char.toString()
        }

        else -> char.toString()
    }
}