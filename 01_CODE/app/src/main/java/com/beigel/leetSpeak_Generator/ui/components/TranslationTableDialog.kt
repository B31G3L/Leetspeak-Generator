// TranslationTableDialog.kt - Verbesserte Version
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
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel

/**
 * Translation Table Dialog - Vollständig sichtbar ohne internes Scrollen
 * FIXED: Alle hardcodierten deutschen Texte durch String-Ressourcen ersetzt
 */
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
                // Header
                TranslationTableHeader(
                    // FIXED: String resource with formatting
                    title = stringResource(R.string.translation_table_dialog_title, leetOption.name),
                    onDismiss = onDismiss
                )

                // Table Content
                TranslationTableContent(
                    alphabet = alphabet,
                    leetOption = leetOption,
                    viewModel = viewModel
                )
            }
        }
    }
}

/**
 * Header mit Schließen-Button
 * FIXED: Hardcodierte Strings durch String-Ressourcen ersetzt
 */
@Composable
private fun TranslationTableHeader(
    title: String,
    onDismiss: () -> Unit
) {
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
                // FIXED: String resource
                Text(
                    text = stringResource(R.string.translation_table_close),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Vollständige Tabelle ohne Scrollen
 */
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
        // Column Headers
        TranslationTableColumnHeaders()

        Spacer(modifier = Modifier.height(8.dp))

        // Table Rows - Alle 13 Zeilen ohne Scrollen
        repeat(13) { rowIndex ->
            TranslationTableRow(
                leftChar = alphabet[rowIndex],
                leftTranslation = getTranslatedCharForOption(alphabet[rowIndex], leetOption, viewModel),
                rightChar = if (rowIndex + 13 < alphabet.length) alphabet[rowIndex + 13] else null,
                rightTranslation = if (rowIndex + 13 < alphabet.length)
                    getTranslatedCharForOption(alphabet[rowIndex + 13], leetOption, viewModel) else null,
                isEvenRow = rowIndex % 2 == 0
            )
        }
    }
}

/**
 * Column Headers
 * FIXED: Hardcodierte Strings durch String-Ressourcen ersetzt
 */
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
            // FIXED: String resource
            Text(
                text = stringResource(R.string.translation_table_plain),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            // FIXED: String resource
            Text(
                text = stringResource(R.string.translation_table_leet),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            // FIXED: String resource
            Text(
                text = stringResource(R.string.translation_table_plain),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            // FIXED: String resource
            Text(
                text = stringResource(R.string.translation_table_leet),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Table Row mit alternierender Hintergrundfarbe
 */
@Composable
private fun TranslationTableRow(
    leftChar: Char,
    leftTranslation: String,
    rightChar: Char?,
    rightTranslation: String?,
    isEvenRow: Boolean
) {
    Surface(
        color = if (isEvenRow) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side (A-M)
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

            // Right side (N-Z)
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

private fun getTranslatedCharForOption(char: Char, leetOption: LeetOption, viewModel: MainViewModel): String {
    return when (leetOption.mode) {
        LeetManager.MODE_SIMPLE -> {
            when (char) {
                'A' -> "4"; 'B' -> "8"; 'C' -> "C"; 'D' -> "D"; 'E' -> "3"
                'F' -> "F"; 'G' -> "6"; 'H' -> "#"; 'I' -> "1"; 'J' -> "J"
                'K' -> "K"; 'L' -> "L"; 'M' -> "M"; 'N' -> "N"; 'O' -> "0"
                'P' -> "P"; 'Q' -> "Q"; 'R' -> "R"; 'S' -> "5"; 'T' -> "7"
                'U' -> "U"; 'V' -> "V"; 'W' -> "W"; 'X' -> "X"; 'Y' -> "Y"
                'Z' -> "2"
                else -> char.toString()
            }
        }
        LeetManager.MODE_EXTENDED -> {
            when (char) {
                'A' -> "4"; 'B' -> "8"; 'C' -> "("; 'D' -> "|)"; 'E' -> "3"
                'F' -> "|="; 'G' -> "6"; 'H' -> "#"; 'I' -> "!"; 'J' -> "_|"
                'K' -> "|<"; 'L' -> "1"; 'M' -> "/\\/\\"; 'N' -> "|\\|"; 'O' -> "0"
                'P' -> "9"; 'Q' -> "0_"; 'R' -> "2"; 'S' -> "5"; 'T' -> "7"
                'U' -> "|_|"; 'V' -> "\\/"; 'W' -> "\\/\\/"; 'X' -> "><"; 'Y' -> "`/"
                'Z' -> "Z"
                else -> char.toString()
            }
        }
        LeetManager.MODE_CUSTOM -> {
            val leets = viewModel.leets.value
            val leet = leets.getOrNull(leetOption.customIndex)
            leet?.getTranslation(char.toString()) ?: char.toString()
        }
        else -> char.toString()
    }
}