package com.beigel.leetSpeak_Generator.ui.components.text

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
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
            "Leetspeak Text eingeben...",
            "#3110 W0rl|) → Hello World",
            "4|V|4Z1|V|6 → Amazing",
            "L337 5P34K → Leet Speak"
        )
    } else {
        listOf(
            "Hier deinen Text eingeben...",
            "Schreibe etwas Cooles...",
            "Verwandle Text in Leetspeak...",
            "Hello World → #3110 W0rl|)"
        )
    }

    var currentIndex by remember { mutableStateOf(0) }

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

/**
 * Einfacher statischer Platzhalter
 */
@Composable
fun StaticPlaceholder(
    text: String,
    adaptiveTextSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = adaptiveTextSize
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = modifier
    )
}

/**
 * Typing-Animation Platzhalter (simuliert Tippen)
 */
@Composable
fun TypingPlaceholder(
    fullText: String,
    adaptiveTextSize: TextUnit,
    modifier: Modifier = Modifier,
    typingSpeed: Long = 100L
) {
    var visibleText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(true) }

    LaunchedEffect(fullText) {
        visibleText = ""
        isTyping = true

        fullText.forEachIndexed { index, _ ->
            delay(typingSpeed)
            visibleText = fullText.substring(0, index + 1)
        }

        isTyping = false
        delay(2000) // Pause before restarting

        // Restart animation
        visibleText = ""
        isTyping = true
    }

    Text(
        text = visibleText + if (isTyping) "|" else "",
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = adaptiveTextSize
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = modifier
    )
}