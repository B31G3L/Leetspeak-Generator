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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import com.beigel.leetSpeak_Generator.ui.components.text.AdaptiveTextField
import kotlinx.coroutines.delay

@Composable
fun OutputCard(
    outputText: String,
    currentMode: String,
    animationKey: LeetTranslator.TranslationMode,
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
        containerColor = MaterialTheme.colorScheme.surface
    )

    // Normal: secondary (Pink). Reverse-Modus: zweiter Reverse-Akzentton (Blau) — verwandt
    // mit dem Türkis von InputCard/Badge, aber unterscheidbar von der Eingabekarte.
    val bgColorForMode = MaterialTheme.colorScheme.background
    val isDarkThemeForAccent = (0.299f * bgColorForMode.red + 0.587f * bgColorForMode.green + 0.114f * bgColorForMode.blue) < 0.5f

    val headerTextColor = if (isReverseMode) {
        if (isDarkThemeForAccent)
            com.beigel.leetSpeak_Generator.ui.theme.ReverseAccentSecondaryDark
        else
            com.beigel.leetSpeak_Generator.ui.theme.ReverseAccentSecondaryLight
    } else {
        MaterialTheme.colorScheme.secondary
    }
    val borderColor = headerTextColor

    val textFieldDesc = stringResource(R.string.a11y_output_field, currentMode)

    AnimatedVisibility(
        visible  = outputText.isNotEmpty(),
        enter    = fadeIn(animationSpec = tween(300)) + slideInVertically(
            animationSpec  = tween(300),
            initialOffsetY = { it / 4 }
        ),
        exit     = fadeOut(animationSpec = tween(200)) + slideOutVertically(
            animationSpec = tween(200),
            targetOffsetY = { it / 4 }
        ),
        modifier = modifier
    ) {
        Card(
            colors = cardColors,
            shape = MaterialTheme.shapes.medium,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (showHeader) {
                    OutputCardHeader(
                        currentMode      = currentMode,
                        headerTextColor  = headerTextColor,
                        charCount        = outputText.length,
                        translationStats = translationStats,
                        showCopyFeedback = showCopyFeedback,
                        onCopyClick      = {
                            onCopyClick()
                            showCopyFeedback = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // AnimatedContent reagiert NUR auf Moduswechsel, nicht auf Texteingabe
                AnimatedContent(
                    targetState    = animationKey,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(200)) +
                                slideInHorizontally(
                                    animationSpec  = tween(200),
                                    initialOffsetX = { it / 8 }
                                )) togetherWith
                                (fadeOut(animationSpec = tween(150)) +
                                        slideOutHorizontally(
                                            animationSpec = tween(150),
                                            targetOffsetX = { -it / 8 }
                                        ))
                    },
                    label = "output_mode_transition"
                ) { _ ->
                    SelectionContainer {
                        AdaptiveTextField(
                            value         = outputText,
                            onValueChange = { },
                            modifier      = Modifier
                                .fillMaxSize()
                                .semantics { contentDescription = textFieldDesc },
                            readOnly = true,
                            colors   = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedBorderColor    = androidx.compose.ui.graphics.Color.Transparent,
                                disabledBorderColor     = androidx.compose.ui.graphics.Color.Transparent,
                                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                disabledContainerColor  = MaterialTheme.colorScheme.surface,
                                focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor      = MaterialTheme.colorScheme.onSurface,
                                disabledTextColor       = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }
            }
        }
    }
}

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
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text       = currentMode,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = headerTextColor
            )
            AnimatedVisibility(visible = charCount > 0) {
                Text(
                    text  = stringResource(R.string.char_count, charCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        CopyButton(
            showCopyFeedback = showCopyFeedback,
            onCopyClick      = onCopyClick,
            tint             = headerTextColor
        )
    }
}

@Composable
private fun CopyButton(
    showCopyFeedback: Boolean,
    onCopyClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        IconButton(
            onClick  = onCopyClick,
            modifier = Modifier.size(40.dp)
        ) {
            AnimatedContent(
                targetState    = showCopyFeedback,
                transitionSpec = {
                    scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                },
                label = "copy_feedback"
            ) { feedback ->
                if (feedback) {
                    Icon(
                        imageVector        = Icons.Default.Check,
                        contentDescription = stringResource(R.string.copied),
                        tint               = tint,
                        modifier           = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector        = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.copy_text),
                        tint               = tint,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showCopyFeedback,
            enter   = scaleIn() + fadeIn(),
            exit    = scaleOut() + fadeOut()
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