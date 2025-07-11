package com.beigel.leetSpeak_Generator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.manager.LeetManager
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel

/**
 * Translation Table Dialog in Compose
 */
@Composable
fun TranslationTableDialog(
    leetOption: LeetOption,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Übersetzungstabelle - ${leetOption.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.secondary)
                )
            ) {
                Column {
                    // Table Header
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Plain",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Leet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Plain",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Leet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Translation Rows
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(8.dp)
                    ) {
                        items(13) { i ->
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

                                Text(
                                    text = getTranslatedCharForOption(alphabet[i], leetOption, viewModel),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )

                                // Right side (N-Z)
                                if (i + 13 < alphabet.length) {
                                    Text(
                                        text = alphabet[i + 13].toString(),
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = getTranslatedCharForOption(alphabet[i + 13], leetOption, viewModel),
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(2f))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

private fun getTranslatedCharForOption(char: Char, leetOption: LeetOption, viewModel: MainViewModel): String {
    // Vereinfachte Preview-Generation
    return when (leetOption.mode) {
        LeetManager.MODE_SIMPLE -> {
            when (char) {
                'A' -> "4"
                'B' -> "8"
                'C' -> "C"
                'D' -> "D"
                'E' -> "3"
                'F' -> "F"
                'G' -> "6"
                'H' -> "#"
                'I' -> "1"
                'J' -> "J"
                'K' -> "K"
                'L' -> "L"
                'M' -> "M"
                'N' -> "N"
                'O' -> "0"
                'P' -> "P"
                'Q' -> "Q"
                'R' -> "R"
                'S' -> "5"
                'T' -> "7"
                'U' -> "U"
                'V' -> "V"
                'W' -> "W"
                'X' -> "X"
                'Y' -> "Y"
                'Z' -> "2"
                else -> char.toString()
            }
        }
        LeetManager.MODE_EXTENDED -> {
            when (char) {
                'A' -> "4"
                'B' -> "8"
                'C' -> "("
                'D' -> "|)"
                'E' -> "3"
                'F' -> "|="
                'G' -> "6"
                'H' -> "#"
                'I' -> "!"
                'J' -> "_|"
                'K' -> "|<"
                'L' -> "1"
                'M' -> "/\\/\\"
                'N' -> "|\\|"
                'O' -> "0"
                'P' -> "9"
                'Q' -> "0_"
                'R' -> "2"
                'S' -> "5"
                'T' -> "7"
                'U' -> "|_|"
                'V' -> "\\/"
                'W' -> "\\/\\/"
                'X' -> "><"
                'Y' -> "`/"
                'Z' -> "Z"
                else -> char.toString()
            }
        }
        LeetManager.MODE_CUSTOM -> {
            // Für Custom Profile - würde echte Übersetzung verwenden
            val profiles = viewModel.profiles.value
            val profile = profiles.getOrNull(leetOption.customIndex)
            profile?.getTranslation(char.toString()) ?: char.toString()
        }
        else -> char.toString()
    }
}