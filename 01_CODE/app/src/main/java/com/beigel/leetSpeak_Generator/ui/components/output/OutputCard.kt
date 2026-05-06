package com.beigel.leetSpeak_Generator.ui.components.output

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.R
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


    LaunchedEffect(showCopyFeedback) {
        if (showCopyFeedback) {
            delay(1500)
            showCopyFeedback = false
        }
    }

    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.background
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
                        charCount        = outputText.length,
                        translationStats = translationStats,
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
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            disabledContainerColor = MaterialTheme.colorScheme.background,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            disabledTextColor = MaterialTheme.colorScheme.onBackground
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
    charCount: Int,
    translationStats: LeetTranslator.TranslationStats?,
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
            AnimatedVisibility(visible = charCount > 0) {
                Text(
                    text  = stringResource(R.string.char_count, charCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = headerTextColor.copy(alpha = 0.6f)
                )
            }

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

                    }
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
                        contentDescription = stringResource(R.string.copied),
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.copy_text),
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
