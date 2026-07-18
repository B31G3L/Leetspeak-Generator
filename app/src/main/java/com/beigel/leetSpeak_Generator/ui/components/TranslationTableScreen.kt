package com.beigel.leetSpeak_Generator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.manager.LeetManager
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel

/**
 * Tabelle-Vollbildschirm (Redesign v4) — ersetzt den alten TranslationTableDialog.
 * Header (Zurück-Chevron + Modusname), darunter eine Karte mit farbigem Kopfbalken
 * ("Übersetzungstabelle", secondary bg) und einem 2-spaltigen A–M / N–Z Grid,
 * jede Zeile als kleine Pille "Buchstabe → Symbol".
 */
@Composable
fun TranslationTableScreen(
    leetOption: LeetOption,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Zurück + Modusname
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
                    text = leetOption.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column {
                        // Farbiger Kopfbalken
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
                            repeat(13) { rowIndex ->
                                if (rowIndex > 0) Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val leftChar = alphabet[rowIndex]
                                    val rightChar = alphabet[rowIndex + 13]
                                    TranslationPillRow(
                                        char = leftChar,
                                        translation = getTranslationFor(leftChar, leetOption, viewModel),
                                        modifier = Modifier.weight(1f)
                                    )
                                    TranslationPillRow(
                                        char = rightChar,
                                        translation = getTranslationFor(rightChar, leetOption, viewModel),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TranslationPillRow(
    char: Char,
    translation: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = char.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(18.dp),
            textAlign = TextAlign.Center
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier
                .size(12.dp)
                .padding(horizontal = 2.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = translation,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
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
