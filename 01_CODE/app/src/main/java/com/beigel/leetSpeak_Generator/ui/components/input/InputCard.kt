package com.beigel.leetSpeak_Generator.ui.components.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.ui.components.text.*

/**
 * Input Card Component für Text-Eingabe - OHNE RAHMEN
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
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current


    // Adaptive text size
    val adaptiveTextSize = remember(inputText.length) {
        when {
            inputText.length <= 40 -> 30.sp
            inputText.length <= 180 -> 22.sp
            else -> 18.sp
        }
    }

    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.background
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
                    disabledBorderColor = borderColor.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),

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
                    contentDescription = stringResource(R.string.clear),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
