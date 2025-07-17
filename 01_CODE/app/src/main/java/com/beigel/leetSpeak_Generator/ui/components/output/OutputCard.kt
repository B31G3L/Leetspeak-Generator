package com.beigel.leetSpeak_Generator.ui.components.output

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import com.beigel.leetSpeak_Generator.ui.components.text.*
import kotlinx.coroutines.delay

/**
 * Output Card Component für Übersetzungsergebnisse
 * Unterstützt Reverse Mode und Copy-Funktionalität
 */
@Composable
fun OutputCard(
    outputText: String,
    currentMode: String,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    isReverseMode: Boolean = false,
    translationStats: LeetTranslator.TranslationStats? = null
) {
    var showCopyFeedback by remember { mutableStateOf(false) }

    val adaptiveTextSize = remember(outputText.length) {
        when {
            outputText.length <= 40 -> 30.sp
            outputText.length <= 180 -> 22.sp
            else -> 18.sp
        }
    }

    LaunchedEffect(showCopyFeedback) {
        if (showCopyFeedback) {
            delay(1500)
            showCopyFeedback = false
        }
    }

    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )

    val headerTextColor = if (isReverseMode) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    val borderColor = if (isReverseMode) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    AnimatedVisibility(
        visible = outputText.isNotEmpty(),
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
        Card(
            colors = cardColors,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = if (!isReverseMode) {
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
                    OutputCardHeader(
                        currentMode = currentMode,
                        headerTextColor = headerTextColor,
                        translationStats = translationStats,
                        currentTextSize = adaptiveTextSize,
                        showCopyFeedback = showCopyFeedback,
                        onCopyClick = {
                            onCopyClick()
                            showCopyFeedback = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Output Display
                SelectionContainer {
                    AdaptiveTextField(
                        value = outputText,
                        onValueChange = { }, // Read-only
                        modifier = Modifier.fillMaxSize(),
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = borderColor,
                            unfocusedBorderColor = borderColor.copy(alpha = 0.5f),
                            disabledBorderColor = borderColor.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }
        }
    }
}

/**
 * Header für die Output Card
 */
@Composable
private fun OutputCardHeader(
    currentMode: String,
    headerTextColor: androidx.compose.ui.graphics.Color,
    translationStats: LeetTranslator.TranslationStats?,
    currentTextSize: androidx.compose.ui.unit.TextUnit,
    showCopyFeedback: Boolean,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mode Info and Statistics
        Column {
            Text(
                text = currentMode,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = headerTextColor
            )

            // Translation Statistics
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                translationStats?.let { stats ->
                    AnimatedVisibility(
                        visible = stats.totalChars > 0,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        TranslationStatisticsChips(
                            translatedChars = stats.translatedChars,
                            totalChars = stats.totalChars,
                            translationPercentage = stats.translationPercentage
                        )
                    }
                }

                if (translationStats?.totalChars ?: 0 > 0) {
                    TextSizeIndicatorChip(currentTextSize = currentTextSize)
                }
            }
        }

        // Copy Button with Feedback
        CopyButton(
            showCopyFeedback = showCopyFeedback,
            onCopyClick = onCopyClick,
            tint = headerTextColor
        )
    }
}

/**
 * Copy Button mit animiertem Feedback
 */
@Composable
private fun CopyButton(
    showCopyFeedback: Boolean,
    onCopyClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
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
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Text kopieren",
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Success indicator dot
        AnimatedVisibility(
            visible = showCopyFeedback,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .offset(x = (-8).dp, y = (-8).dp)
                    .size(8.dp),
                color = tint,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {}
        }
    }
}

/**
 * Placeholder für leere Output
 */
@Composable
fun OutputPlaceholder(
    modifier: Modifier = Modifier,
    message: String = "Text eingeben für Leetspeak-Übersetzung"
) {
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
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Kompakte Output Card ohne Header
 */
@Composable
fun SimpleOutputCard(
    outputText: String,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier,
    isReverseMode: Boolean = false
) {
    var showCopyFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(showCopyFeedback) {
        if (showCopyFeedback) {
            delay(1500)
            showCopyFeedback = false
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ergebnis",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                CopyButton(
                    showCopyFeedback = showCopyFeedback,
                    onCopyClick = {
                        onCopyClick()
                        showCopyFeedback = true
                    },
                    tint = if (isReverseMode) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SelectionContainer {
                Text(
                    text = outputText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}