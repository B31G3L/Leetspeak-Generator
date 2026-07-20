package com.beigel.leetSpeak_Generator.ui.components.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.ui.components.text.AdaptiveTextField
import com.beigel.leetSpeak_Generator.ui.components.text.AnimatedPlaceholder

/**
 * Eingabe-Karte im Redesign-v4-Stil (design_handoff_leetspeak_redesign):
 * geränderte, abgerundete Karte (16dp Radius, 1.5dp Border in primary/secondary),
 * Header-Zeile mit Label + Zeichenanzahl + Clear-Button, borderloses Mehrzeilen-Textfeld.
 * Das Mikrofon lebt jetzt in der schwebenden Bottom-Nav (siehe MainScreen), nicht mehr hier.
 */
@Composable
fun InputCard(
    inputText: String,
    onInputChange: (String) -> Unit,
    onClearText: () -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    isReverseMode: Boolean = false,
    title: String = "Input: Plaintext",
    onSpeechInput: ((String) -> Unit)? = null
) {
    val bgColor = MaterialTheme.colorScheme.background
    val isDarkTheme = (0.299f * bgColor.red + 0.587f * bgColor.green + 0.114f * bgColor.blue) < 0.5f

    // Normal: primary (Violett). Reverse-Modus: einheitliche Reverse-Akzentfarbe (Türkis) —
    // dieselbe Farbe wie OutputCard und das Reverse-Badge, für ein durchgängiges Bild.
    val accentColor = if (isReverseMode) {
        if (isDarkTheme)
            com.beigel.leetSpeak_Generator.ui.theme.ReverseAccentDark
        else
            com.beigel.leetSpeak_Generator.ui.theme.ReverseAccentLight
    } else {
        MaterialTheme.colorScheme.primary
    }

    val textFieldDesc = stringResource(R.string.a11y_input_field, title)

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, accentColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (showHeader) {
                InputCardHeader(
                    title = title,
                    headerTextColor = accentColor,
                    hasText = inputText.isNotEmpty(),
                    charCount = inputText.length,
                    onClearText = onClearText
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            AdaptiveTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = textFieldDesc },
                placeholder = {
                    AnimatedPlaceholder(
                        adaptiveTextSize = when {
                            inputText.length <= 40 -> 30
                            inputText.length <= 180 -> 22
                            else -> 18
                        }.let {
                            androidx.compose.ui.unit.TextUnit(
                                it.toFloat(),
                                androidx.compose.ui.unit.TextUnitType.Sp
                            )
                        },
                        isReverseMode = isReverseMode
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
            )
        }
    }
}

@Composable
private fun InputCardHeader(
    title: String,
    headerTextColor: Color,
    hasText: Boolean,
    charCount: Int,
    onClearText: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = headerTextColor
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = charCount > 0) {
                Text(
                    text = stringResource(R.string.char_count, charCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = hasText,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(
                    onClick = onClearText,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}