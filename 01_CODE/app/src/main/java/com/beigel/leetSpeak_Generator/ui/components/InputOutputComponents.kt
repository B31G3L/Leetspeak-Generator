package com.beigel.leetSpeak_Generator.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import com.beigel.leetSpeak_Generator.ui.components.input.InputCard
import com.beigel.leetSpeak_Generator.ui.components.output.OutputCard
import com.beigel.leetSpeak_Generator.ui.components.output.OutputPlaceholder


/**
 * Main Input Section - Wrapper für die neue InputCard
 */
@Composable
fun InputSection(
    inputText: String,
    onInputChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onClearText: () -> Unit = {},
    isReverseMode: Boolean = false,
    title: String = "Input: Plaintext"
) {
    InputCard(
        inputText = inputText,
        onInputChange = onInputChange,
        onClearText = onClearText,
        modifier = modifier,
        showHeader = true,
        isReverseMode = isReverseMode,
        title = title
    )
}

/**
 * Main Output Section - Wrapper für die neue OutputCard
 */
@Composable
fun OutputSection(
    outputText: String,
    currentMode: String,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier,
    translationStats: LeetTranslator.TranslationStats? = null,
    showContent: Boolean = true,
    isReverseMode: Boolean = false
) {
    if (showContent && outputText.isNotEmpty()) {
        OutputCard(
            outputText = outputText,
            currentMode = currentMode,
            onCopyClick = onCopyClick,
            modifier = modifier,
            showHeader = true,
            isReverseMode = isReverseMode,
            translationStats = translationStats
        )
    } else {
        OutputPlaceholder(
            modifier = modifier,
            message = "Text eingeben für Leetspeak-Übersetzung"
        )
    }
}

/**
 * Legacy Compatibility - für schrittweise Migration
 * @deprecated Verwende stattdessen die spezialisierten Components
 */
@Deprecated(
    message = "Use InputCard from ui.components.input package instead",
    replaceWith = ReplaceWith(
        "InputCard(inputText, onInputChange, onClearText, modifier, showHeader, isReverseMode, title)",
        "com.beigel.leetSpeak_Generator.ui.components.input.InputCard"
    )
)
@Composable
fun LegacyInputSection(
    inputText: String,
    onInputChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onClearText: () -> Unit = {},
    onUndoText: () -> Unit = {},
    onRedoText: () -> Unit = {}
) {
    // Forward to new implementation
    InputSection(
        inputText = inputText,
        onInputChange = onInputChange,
        onClearText = onClearText,
        modifier = modifier
    )
}