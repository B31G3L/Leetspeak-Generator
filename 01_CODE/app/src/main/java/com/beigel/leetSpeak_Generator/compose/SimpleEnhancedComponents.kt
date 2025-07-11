// ALTERNATIVE: Vereinfachte Version ohne experimentelle APIs
// Erstelle: app/src/main/java/com/beigel/leetSpeak_Generator/compose/SimpleEnhancedComponents.kt

package com.beigel.leetSpeak_Generator.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beigel.leetSpeak_Generator.MainIntent
import com.beigel.leetSpeak_Generator.MainViewModel
import kotlinx.coroutines.delay

/**
 * Vereinfachte Input Section ohne experimentelle APIs
 */
@Composable
fun SimpleInputSection(
    inputText: String,
    onInputChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onClearText: () -> Unit = {}
) {
    val charCount = inputText.length
    val wordCount = if (inputText.isBlank()) 0 else inputText.trim().split("\\s+".toRegex()).size

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        // Header mit Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Plaintext",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                if (inputText.isNotEmpty()) {
                    Text(
                        text = "$charCount Zeichen • $wordCount Wörter",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            if (inputText.isNotEmpty()) {
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

        Spacer(modifier = Modifier.height(12.dp))

        // Text Field
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.fillMaxSize(),
            placeholder = {
                Text("Hier deinen Text eingeben...")
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            shape = MaterialTheme.shapes.large
        )
    }
}

/**
 * Vereinfachte Output Section
 */
@Composable
fun SimpleOutputSection(
    outputText: String,
    currentMode: String,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier,
    translationStats: com.beigel.leetSpeak_Generator.LeetTranslator.TranslationStats? = null
) {
    var showCopyFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(showCopyFeedback) {
        if (showCopyFeedback) {
            delay(1500)
            showCopyFeedback = false
        }
    }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = currentMode,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                translationStats?.let { stats ->
                    if (stats.totalChars > 0) {
                        Text(
                            text = "${stats.translatedChars}/${stats.totalChars} übersetzt (${stats.translationPercentage.toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Copy Button
            IconButton(
                onClick = {
                    onCopyClick()
                    showCopyFeedback = true
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (showCopyFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = if (showCopyFeedback) "Kopiert!" else "Text kopieren",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Output Display
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
            ),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            SelectionContainer {
                Text(
                    text = outputText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                )
            }
        }
    }
}

/**
 * Vereinfachte ComposeMainActivity - Verwendung der stabilen Komponenten
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleComposeMainScreen(
    viewModel: MainViewModel,
    onCopyToClipboard: (String) -> Unit
) {
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val outputText by viewModel.outputText.collectAsStateWithLifecycle()
    val currentModeDisplayName by viewModel.currentModeDisplayName.collectAsStateWithLifecycle()
    val translationStats by viewModel.translationStats.collectAsStateWithLifecycle()
    val shouldShowOutput by viewModel.shouldShowOutput.collectAsStateWithLifecycle()

    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Leetspeak Generator",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // Main Content Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    // Simple Input Section
                    SimpleInputSection(
                        inputText = inputText,
                        onInputChange = { text ->
                            viewModel.handleIntent(MainIntent.UpdateInput(text))
                        },
                        onClearText = {
                            viewModel.handleIntent(MainIntent.ClearInput)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Divider mit einfacher Animation
                    if (shouldShowOutput) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Simple Output Section
                    if (shouldShowOutput) {
                        SimpleOutputSection(
                            outputText = outputText,
                            currentMode = currentModeDisplayName,
                            onCopyClick = { onCopyToClipboard(outputText) },
                            translationStats = translationStats,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Simple Button Section
            SimpleButtonSection(
                currentMode = currentModeDisplayName,
                onLeetSelectorClick = {
                    // Temporär deaktiviert bis Bottom Sheet fix
                    // showBottomSheet = true
                },
                onPlainModeClick = {
                    // Plain mode action
                }
            )
        }
    }
}

@Composable
private fun SimpleButtonSection(
    currentMode: String,
    onLeetSelectorClick: () -> Unit,
    onPlainModeClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Plain Text Button
            OutlinedButton(
                onClick = onPlainModeClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text("Plain")
                }
            }

            // Simple Arrows
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            // Leet Mode Button
            OutlinedButton(
                onClick = onLeetSelectorClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Transform,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        currentMode,
                        maxLines = 1
                    )
                }
            }
        }
    }
}