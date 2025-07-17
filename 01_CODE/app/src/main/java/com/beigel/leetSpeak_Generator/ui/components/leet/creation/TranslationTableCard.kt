package com.beigel.leetSpeak_Generator.ui.components.leet.creation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Übersetzungstabelle für die Leet-Erstellung
 */
@Composable
fun TranslationTableCard(
    alphabet: String,
    translationStates: List<MutableState<String>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            TranslationTableHeader()

            // Column Headers
            TranslationTableColumnHeaders()

            // Translation Rows
            TranslationTableContent(
                alphabet = alphabet,
                translationStates = translationStates
            )
        }
    }
}

@Composable
private fun TranslationTableHeader() {
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
}

@Composable
private fun TranslationTableColumnHeaders() {
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
}

@Composable
private fun TranslationTableContent(
    alphabet: String,
    translationStates: List<MutableState<String>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // 13 Zeilen für A-M und N-Z nebeneinander
        for (i in 0 until 13) {
            TranslationTableRow(
                leftIndex = i,
                rightIndex = i + 13,
                alphabet = alphabet,
                translationStates = translationStates
            )
        }
    }
}

@Composable
private fun TranslationTableRow(
    leftIndex: Int,
    rightIndex: Int,
    alphabet: String,
    translationStates: List<MutableState<String>>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side (A-M)
        TranslationPair(
            plainChar = alphabet[leftIndex].toString(),
            translationState = translationStates[leftIndex],
            modifier = Modifier.weight(2f)
        )

        // Right side (N-Z) - nur wenn Index gültig ist
        if (rightIndex < alphabet.length) {
            TranslationPair(
                plainChar = alphabet[rightIndex].toString(),
                translationState = translationStates[rightIndex],
                modifier = Modifier.weight(2f)
            )
        } else {
            Spacer(modifier = Modifier.weight(2f))
        }
    }
}

@Composable
private fun TranslationPair(
    plainChar: String,
    translationState: MutableState<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Plain Character
        Text(
            text = plainChar,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        // Translation Input
        OutlinedTextField(
            value = translationState.value,
            onValueChange = { translationState.value = it },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                textAlign = TextAlign.Center
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                cursorColor = MaterialTheme.colorScheme.secondary
            )
        )
    }
}