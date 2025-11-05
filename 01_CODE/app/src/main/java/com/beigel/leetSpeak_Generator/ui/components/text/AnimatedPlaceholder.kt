package com.beigel.leetSpeak_Generator.ui.components.text

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import com.beigel.leetSpeak_Generator.R
import kotlinx.coroutines.delay

/**
 * Animierter Platzhalter mit wechselnden Texten
 */
@Composable
fun AnimatedPlaceholder(
    adaptiveTextSize: TextUnit,
    modifier: Modifier = Modifier,
    isReverseMode: Boolean = false,
    customTexts: List<String>? = null,
    animationDuration: Int = 3000
) {
    val placeholders = customTexts ?: if (isReverseMode) {
        listOf(
            stringResource(R.string.placeholder_leet_input),
            stringResource(R.string.placeholder_leet_example_1),
            stringResource(R.string.placeholder_leet_example_2),
            stringResource(R.string.placeholder_leet_example_3)
        )
    } else {
        listOf(
            stringResource(R.string.placeholder_text_input),
            stringResource(R.string.placeholder_text_input_2),
            stringResource(R.string.placeholder_text_input_3),
            stringResource(R.string.placeholder_text_example)
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(animationDuration.toLong())
            currentIndex = (currentIndex + 1) % placeholders.size
        }
    }

    AnimatedContent(
        targetState = placeholders[currentIndex],
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) togetherWith
                    fadeOut(animationSpec = tween(500))
        },
        label = "placeholder_animation",
        modifier = modifier
    ) { placeholder ->
        Text(
            text = placeholder,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = adaptiveTextSize
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

