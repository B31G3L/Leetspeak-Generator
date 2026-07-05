package com.beigel.leetSpeak_Generator.ui.components.text

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Intelligentes TextField mit adaptiver Schriftgröße
 * Passt die Textgröße automatisch an die Textlänge an
 */
@Composable
fun AdaptiveTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium,
    focusRequester: FocusRequester? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null,
    minFontSize: TextUnit = 12.sp,
    maxFontSize: TextUnit = 30.sp
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Adaptive Textgröße basierend auf Textlänge
    val adaptiveTextSize = remember(value.length) {
        when {
            value.length <= 40 -> maxFontSize
            value.length <= 180 -> 22.sp
            value.length <= 500 -> 16.sp
            else -> minFontSize
        }
    }

    val adaptiveLineHeight = adaptiveTextSize * 1.4f

    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .then(
                if (focusRequester != null) {
                    Modifier.focusRequester(focusRequester)
                } else {
                    Modifier
                }
            )
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                onFocusChanged?.invoke(focusState.isFocused)
            },
        placeholder = placeholder,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = adaptiveTextSize,
            lineHeight = adaptiveLineHeight
        ),
        keyboardOptions = keyboardOptions.copy(
            imeAction = if (singleLine) ImeAction.Done else ImeAction.Default
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        ),
        colors = colors,
        shape = shape
    )
}
