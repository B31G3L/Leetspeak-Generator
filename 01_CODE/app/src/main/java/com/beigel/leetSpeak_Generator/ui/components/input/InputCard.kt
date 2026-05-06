package com.beigel.leetSpeak_Generator.ui.components.input

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.ui.components.text.AdaptiveTextField
import com.beigel.leetSpeak_Generator.ui.components.text.AnimatedPlaceholder
import com.beigel.leetSpeak_Generator.utils.SpeechInputManager

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
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    val speechManager = remember { SpeechInputManager(context) }

    // Strings hier auflösen wo R verfügbar ist
    val errorMessages = SpeechInputManager.SpeechErrorMessages(
        unavailable = stringResource(R.string.speech_error_unavailable),
        noMatch     = stringResource(R.string.speech_error_no_match),
        timeout     = stringResource(R.string.speech_error_timeout),
        audio       = stringResource(R.string.speech_error_audio),
        generic     = stringResource(R.string.speech_error_generic)
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            speechManager.startListening(
                onResult      = { onSpeechInput?.invoke(it) },
                onError       = { /* ignorieren oder Toast */ },
                onStateChange = { isListening = it },
                errorMessages = errorMessages
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose { speechManager.stop() }
    }

    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.background
    )
    val headerTextColor = if (isReverseMode)
        MaterialTheme.colorScheme.secondary
    else
        MaterialTheme.colorScheme.primary

    val borderColor = if (isReverseMode)
        MaterialTheme.colorScheme.secondary
    else
        MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier,
        colors = cardColors
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            if (showHeader) {
                InputCardHeader(
                    title          = title,
                    headerTextColor = headerTextColor,
                    hasText        = inputText.isNotEmpty(),
                    charCount       = inputText.length,  // NEU
                    onClearText    = onClearText,
                    showMicButton  = onSpeechInput != null,
                    isListening    = isListening,
                    onMicClick     = {
                        if (isListening) {
                            speechManager.stop()
                            isListening = false
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            AdaptiveTextField(
                value          = inputText,
                onValueChange  = onInputChange,
                modifier       = Modifier.fillMaxSize(),
                placeholder    = {
                    AnimatedPlaceholder(
                        adaptiveTextSize = when {
                            inputText.length <= 40  -> 30
                            inputText.length <= 180 -> 22
                            else                    -> 18
                        }.let { androidx.compose.ui.unit.TextUnit(
                            it.toFloat(),
                            androidx.compose.ui.unit.TextUnitType.Sp
                        )},
                        isReverseMode = isReverseMode
                    )
                },
                colors         = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor    = borderColor,
                    unfocusedBorderColor  = borderColor.copy(alpha = 0.5f),
                    disabledBorderColor   = borderColor.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor  = MaterialTheme.colorScheme.surface,
                    focusedTextColor      = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor    = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor     = MaterialTheme.colorScheme.onSurface
                ),
                shape          = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
            )
        }
    }
}

@Composable
private fun InputCardHeader(
    title: String,
    headerTextColor: androidx.compose.ui.graphics.Color,
    hasText: Boolean,
    charCount: Int,
    onClearText: () -> Unit,
    showMicButton: Boolean = false,
    isListening: Boolean = false,
    onMicClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text  = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = headerTextColor
        )
        AnimatedVisibility(visible = charCount > 0) {
            Text(
                text  = stringResource(R.string.char_count, charCount),
                style = MaterialTheme.typography.labelSmall,
                color = headerTextColor.copy(alpha = 0.6f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showMicButton) {
                IconButton(
                    onClick  = onMicClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff
                        else Icons.Default.Mic,
                        contentDescription = stringResource(
                            if (isListening) R.string.speech_input_stop
                            else R.string.speech_input_start
                        ),
                        modifier = Modifier.size(16.dp),
                        tint = if (isListening) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = hasText,
                enter   = fadeIn() + scaleIn(),
                exit    = fadeOut() + scaleOut()
            ) {
                IconButton(
                    onClick  = onClearText,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear),
                        modifier           = Modifier.size(16.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}