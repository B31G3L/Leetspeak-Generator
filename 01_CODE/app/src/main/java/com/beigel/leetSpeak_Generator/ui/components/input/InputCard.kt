package com.beigel.leetSpeak_Generator.ui.components.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beigel.leetSpeak_Generator.ui.components.text.*

/**
 * Input Card Component für Text-Eingabe
 * Unterstützt Reverse Mode und adaptive UI
 */
@Composable
fun InputCard(
    inputText: String,
    onInputChange: (String) -> Unit,
    onClearText: () -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    isReverseMode: Boolean = false,
    title: String = "Input: Plaintext"
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Character and word count
    val charCount = inputText.length
    val wordCount = if (inputText.isBlank()) 0 else inputText.trim().split("\\s+".toRegex()).size

    // Adaptive text size
    val adaptiveTextSize = remember(inputText.length) {
        when {
            inputText.length <= 40 -> 30.sp
            inputText.length <= 180 -> 22.sp
            else -> 18.sp
        }
    }

    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )

    val headerTextColor = if (isReverseMode) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }

    val borderColor = if (isReverseMode) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier,
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isReverseMode) {
            BorderStroke(1.dp, borderColor.copy(alpha = 0.5f))
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Header Section
            if (showHeader) {
                InputCardHeader(
                    title = title,
                    headerTextColor = headerTextColor,
                    charCount = charCount,
                    wordCount = wordCount,
                    currentTextSize = adaptiveTextSize,
                    hasText = inputText.isNotEmpty(),
                    onClearText = onClearText
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Input Field
            AdaptiveTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxSize(),
                placeholder = {
                    AnimatedPlaceholder(
                        adaptiveTextSize = adaptiveTextSize,
                        isReverseMode = isReverseMode
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor.copy(alpha = 0.5f),
                    cursorColor = borderColor
                ),
                shape = MaterialTheme.shapes.medium,
                focusRequester = focusRequester,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                )
            )
        }
    }
}

/**
 * Header für die Input Card
 */
@Composable
private fun InputCardHeader(
    title: String,
    headerTextColor: androidx.compose.ui.graphics.Color,
    charCount: Int,
    wordCount: Int,
    currentTextSize: androidx.compose.ui.unit.TextUnit,
    hasText: Boolean,
    onClearText: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title und Statistics
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = headerTextColor
            )

            // Statistics
            StatisticsChipRow(
                charCount = charCount,
                wordCount = wordCount,
                currentTextSize = currentTextSize,
                hasText = hasText
            )
        }

        // Clear Button
        AnimatedVisibility(
            visible = hasText,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            IconButton(
                onClick = onClearText,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Vereinfachte Input Card ohne Header
 */
@Composable
fun SimpleInputCard(
    inputText: String,
    onInputChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Text eingeben...",
    isReverseMode: Boolean = false
) {
    val adaptiveTextSize = remember(inputText.length) {
        when {
            inputText.length <= 40 -> 30.sp
            inputText.length <= 180 -> 22.sp
            else -> 18.sp
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        AdaptiveTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = {
                StaticPlaceholder(
                    text = placeholder,
                    adaptiveTextSize = adaptiveTextSize
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isReverseMode) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            shape = MaterialTheme.shapes.medium
        )
    }
}