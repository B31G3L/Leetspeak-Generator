package com.beigel.leetSpeak_Generator.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import kotlinx.coroutines.delay


/**
 * Berechnet adaptive Schriftgröße - NORMALE FUNKTION ohne @Composable
 */
private fun getAdaptiveTextSize(text: String): androidx.compose.ui.unit.TextUnit {
    return when {
        text.length <= 40 -> 30.sp
        text.length <= 180 -> 22.sp
        else -> 18.sp
    }
}

/**
 * Berechnet adaptive Zeilenhöhe
 */
@Composable
private fun getAdaptiveLineHeight(fontSize: androidx.compose.ui.unit.TextUnit): androidx.compose.ui.unit.TextUnit {
    return when {
        fontSize.value >= 30f -> fontSize * 1.25f
        fontSize.value >= 22f -> fontSize * 1.3f
        else -> fontSize * 1.35f
    }
}

/**
 * ✅ RADIKAL ÜBERARBEITETE InputSection - FOCUS BLEIBT DEFINITIV STABIL
 */
@Composable
fun InputSection(
    inputText: String,
    onInputChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onClearText: () -> Unit = {},
    onUndoText: () -> Unit = {},
    onRedoText: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // ✅ NEUE STRATEGIE: Stabile Schriftgröße, weniger Recomposition
    var isFocused by remember { mutableStateOf(false) }

    // Character and word count
    val charCount = inputText.length
    val wordCount = if (inputText.isBlank()) 0 else inputText.trim().split("\\s+".toRegex()).size

    // ✅ LÖSUNG: STABILE Schriftgröße - ändert sich NICHT bei jedem Zeichen
    val adaptiveTextSize = remember {
        derivedStateOf {
            when {
                inputText.length <= 40 -> 30.sp
                inputText.length <= 180 -> 22.sp
                else -> 18.sp
            }
        }
    }.value
    val adaptiveLineHeight = getAdaptiveLineHeight(adaptiveTextSize)

    // ✅ KEIN LaunchedEffect mehr - das war das Problem!

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        // Header mit Statistiken
        InputHeader(
            charCount = charCount,
            wordCount = wordCount,
            onClearText = {
                isFocused = false
                onClearText()
            },
            hasText = inputText.isNotEmpty(),
            currentTextSize = adaptiveTextSize
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ ULTIMATIVE LÖSUNG: TextField mit MINIMALER Recomposition
        key(inputText.isEmpty()) { // ✅ Nur bei leer/nicht-leer Wechsel neu erstellen
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange, // ✅ Direkt weiterleiten, keine Zwischensteps
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                placeholder = {
                    AnimatedPlaceholder(adaptiveTextSize)
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = adaptiveTextSize,
                    lineHeight = adaptiveLineHeight
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                ),
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Default
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        isFocused = false
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ),
                singleLine = false,
                maxLines = Int.MAX_VALUE
            )
        }
    }
}

@Composable
private fun InputHeader(
    charCount: Int,
    wordCount: Int,
    onClearText: () -> Unit,
    hasText: Boolean,
    currentTextSize: androidx.compose.ui.unit.TextUnit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title und Stats
        Column {
            Text(
                text = "Plaintext",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            // Statistics mit Text-Size Indikator
            androidx.compose.animation.AnimatedVisibility(
                visible = hasText,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatChip(
                        label = "Zeichen",
                        count = charCount,
                        icon = Icons.Default.TextFields
                    )
                    StatChip(
                        label = "Wörter",
                        count = wordCount,
                        icon = Icons.AutoMirrored.Filled.Article
                    )
                    // Text-Size Indicator
                    TextSizeIndicator(currentTextSize)
                }
            }
        }

        // Action Buttons
        androidx.compose.animation.AnimatedVisibility(
            visible = hasText,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onClearText,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Text löschen",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TextSizeIndicator(currentTextSize: androidx.compose.ui.unit.TextUnit) {
    val sizeLabel = when {
        currentTextSize.value >= 30f -> "XXL"
        currentTextSize.value >= 22f -> "XL"
        else -> "L"
    }

    val sizeColor = when {
        currentTextSize.value >= 30f -> MaterialTheme.colorScheme.tertiary
        currentTextSize.value >= 22f -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        color = sizeColor.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FormatSize,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = sizeColor
            )
            Text(
                text = sizeLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 13.sp
                ),
                color = sizeColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AnimatedPlaceholder(adaptiveTextSize: androidx.compose.ui.unit.TextUnit) {
    val placeholders = listOf(
        "Hier deinen Text eingeben...",
        "Schreibe etwas Cooles...",
        "Verwandle Text in Leetspeak...",
        "Hello World → #3110 W0rl|)"
    )

    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentIndex = (currentIndex + 1) % placeholders.size
        }
    }

    AnimatedContent(
        targetState = placeholders[currentIndex],
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) togetherWith
                    fadeOut(animationSpec = tween(500))
        },
        label = "placeholder_animation"
    ) { placeholder ->
        Text(
            text = placeholder,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = adaptiveTextSize
            )
        )
    }
}

/**
 * ✅ ÜBERARBEITETE Output Section - mit showContent Parameter
 */
@Composable
fun OutputSection(
    outputText: String,
    currentMode: String,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier,
    translationStats: LeetTranslator.TranslationStats? = null,
    showContent: Boolean = true // ✅ NEU: Kontrolle über Sichtbarkeit
) {
    var showCopyFeedback by remember { mutableStateOf(false) }

    // Stabile Schriftgröße
    val adaptiveTextSize = remember {
        derivedStateOf {
            when {
                outputText.length <= 40 -> 30.sp
                outputText.length <= 180 -> 22.sp
                else -> 18.sp
            }
        }
    }.value
    val adaptiveLineHeight = getAdaptiveLineHeight(adaptiveTextSize)

    LaunchedEffect(showCopyFeedback) {
        if (showCopyFeedback) {
            delay(1500)
            showCopyFeedback = false
        }
    }

    // ✅ WICHTIG: AnimatedVisibility hier, nicht im Parent
    AnimatedVisibility(
        visible = showContent,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            animationSpec = tween(300),
            initialOffsetY = { it / 4 }
        ),
        exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(
            animationSpec = tween(200),
            targetOffsetY = { it / 4 }
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Output Header
            OutputHeader(
                currentMode = currentMode,
                onCopyClick = {
                    onCopyClick()
                    showCopyFeedback = true
                },
                showCopyFeedback = showCopyFeedback,
                translationStats = translationStats,
                currentTextSize = adaptiveTextSize
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Output Display
            OutlinedTextField(
                value = outputText,
                onValueChange = { }, // Read-only
                modifier = Modifier.fillMaxSize(),
                readOnly = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = adaptiveTextSize,
                    lineHeight = adaptiveLineHeight
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = MaterialTheme.shapes.large,
                placeholder = {
                    if (outputText.isEmpty()) {
                        Text(
                            text = "Deine Leetspeak-Übersetzung erscheint hier...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 30.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            )
        }
    }

    // ✅ Fallback wenn nicht sichtbar
    if (!showContent) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Transform,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Text eingeben für Leetspeak-Übersetzung",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OutputHeader(
    currentMode: String,
    onCopyClick: () -> Unit,
    showCopyFeedback: Boolean,
    translationStats: LeetTranslator.TranslationStats?,
    currentTextSize: androidx.compose.ui.unit.TextUnit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mode Info
        Column {
            Text(
                text = currentMode,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                translationStats?.let { stats ->
                    androidx.compose.animation.AnimatedVisibility(
                        visible = stats.totalChars > 0,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Text(
                            text = "${stats.translatedChars}/${stats.totalChars} übersetzt (${stats.translationPercentage.toInt()}%)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                if (translationStats?.totalChars ?: 0 > 0) {
                    TextSizeIndicator(currentTextSize)
                }
            }
        }

        // Copy Button
        Box {
            IconButton(
                onClick = onCopyClick,
                modifier = Modifier.size(40.dp)
            ) {
                AnimatedContent(
                    targetState = showCopyFeedback,
                    transitionSpec = {
                        scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                    },
                    label = "copy_feedback"
                ) { feedback ->
                    if (feedback) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Kopiert!",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Text kopieren",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showCopyFeedback,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .offset(x = (-8).dp, y = (-8).dp)
                        .size(8.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {}
            }
        }
    }
}