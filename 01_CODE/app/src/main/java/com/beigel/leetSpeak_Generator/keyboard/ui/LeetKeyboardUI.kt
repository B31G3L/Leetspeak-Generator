package com.beigel.leetSpeak_Generator.keyboard.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beigel.leetSpeak_Generator.keyboard.LeetKeyboardViewModel
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import kotlinx.coroutines.launch

/**
 * 🎹 LEETSPEAK KEYBOARD UI
 *
 * Custom Jetpack Compose Keyboard Layout für systemweite Leetspeak-Eingabe
 * Features:
 * - Live Translation Preview
 * - Gesture Support (Swipe, Long Press)
 * - Adaptive Layout
 * - Mode Indicators
 * - Smart Suggestions
 */
@Composable
fun LeetKeyboardUI(
    viewModel: LeetKeyboardViewModel,
    onKeyPress: (String) -> Unit,
    onSpecialAction: (String) -> Unit,
    onModeToggle: () -> Unit,
    onSettingsOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State aus ViewModel
    val currentMode by viewModel.currentMode
    val isLeetModeActive by viewModel.isLeetModeActive
    val livePreview by viewModel.livePreview
    val suggestions by viewModel.suggestions
    val toastMessage by viewModel.toastMessage
    val currentCustomLeet by viewModel.currentCustomLeet

    // Animation States
    val hapticFeedback = LocalHapticFeedback.current
    var pressedKey by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            )
            .padding(4.dp)
    ) {

        // 📊 Top Status Bar
        KeyboardStatusBar(
            currentMode = currentMode,
            isLeetModeActive = isLeetModeActive,
            livePreview = livePreview,
            customLeetName = currentCustomLeet?.name,
            onModeToggle = onModeToggle,
            onSettingsOpen = onSettingsOpen
        )

        // 🎯 Suggestions Row (wenn vorhanden)
        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            SuggestionsRow(
                suggestions = suggestions,
                onSuggestionClick = { suggestion ->
                    onSpecialAction("INSERT_SUGGESTION:$suggestion")
                },
                onClearSuggestions = { onSpecialAction("CLEAR_SUGGESTIONS") }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ⌨️ Main Keyboard Layout
        KeyboardLayout(
            onKeyPress = { key ->
                pressedKey = key
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onKeyPress(key)

                // Clear pressed state after animation
                kotlinx.coroutines.GlobalScope.launch {
                    kotlinx.coroutines.delay(150)
                    pressedKey = null
                }
            },
            onSpecialAction = onSpecialAction,
            pressedKey = pressedKey,
            isLeetModeActive = isLeetModeActive
        )
    }

    // 🍞 Toast Messages
    toastMessage?.let { message ->
        LaunchedEffect(message) {
            // Toast wird im ViewModel automatisch gecleard
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            KeyboardToast(message = message)
        }
    }
}

/**
 * 📊 Status Bar am oberen Rand der Tastatur
 */
@Composable
private fun KeyboardStatusBar(
    currentMode: LeetTranslator.TranslationMode,
    isLeetModeActive: Boolean,
    livePreview: String,
    customLeetName: String?,
    onModeToggle: () -> Unit,
    onSettingsOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isLeetModeActive) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Status Indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLeetModeActive) Color.Green else Color.Gray
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Mode Text
                Text(
                    text = when (currentMode) {
                        LeetTranslator.TranslationMode.SIMPLE -> "Simple"
                        LeetTranslator.TranslationMode.EXTENDED -> "Extended"
                        LeetTranslator.TranslationMode.CUSTOM -> customLeetName ?: "Custom"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isLeetModeActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Live Preview
            if (livePreview.isNotEmpty()) {
                Text(
                    text = livePreview,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            // Action Buttons
            Row {
                // Mode Toggle
                IconButton(
                    onClick = onModeToggle,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isLeetModeActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                        contentDescription = "Toggle Leet Mode",
                        modifier = Modifier.size(16.dp),
                        tint = if (isLeetModeActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Settings
                IconButton(
                    onClick = onSettingsOpen,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Open Settings",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 💭 Suggestions Row
 */
@Composable
private fun SuggestionsRow(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    onClearSuggestions: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        items(suggestions) { suggestion ->
            SuggestionChip(
                text = suggestion,
                onClick = { onSuggestionClick(suggestion) }
            )
        }

        // Clear button
        item {
            IconButton(
                onClick = onClearSuggestions,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear Suggestions",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1
        )
    }
}

/**
 * ⌨️ Main Keyboard Layout
 */
@Composable
private fun KeyboardLayout(
    onKeyPress: (String) -> Unit,
    onSpecialAction: (String) -> Unit,
    pressedKey: String?,
    isLeetModeActive: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1: Q W E R T Y U I O P
        KeyboardRow(
            keys = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
            onKeyPress = onKeyPress,
            pressedKey = pressedKey,
            isLeetModeActive = isLeetModeActive,
            specialKey = KeyboardKey.Special(
                "LEET",
                Icons.Default.Transform,
                action = "CYCLE_MODE"
            ),
            onSpecialAction = onSpecialAction
        )

        // Row 2: A S D F G H J K L
        KeyboardRow(
            keys = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
            onKeyPress = onKeyPress,
            pressedKey = pressedKey,
            isLeetModeActive = isLeetModeActive,
            specialKey = KeyboardKey.Special(
                "FAV",
                Icons.Default.Favorite,
                action = "QUICK_FAVORITE"
            ),
            onSpecialAction = onSpecialAction
        )

        // Row 3: Z X C V B N M + Backspace
        KeyboardRow(
            keys = listOf("Z", "X", "C", "V", "B", "N", "M"),
            onKeyPress = onKeyPress,
            pressedKey = pressedKey,
            isLeetModeActive = isLeetModeActive,
            specialKey = KeyboardKey.Special(
                "⌫",
                Icons.Default.Backspace,
                action = "BACKSPACE"
            ),
            onSpecialAction = { onKeyPress("BACKSPACE") }
        )

        // Row 4: Space + Enter + Special Actions
        BottomRow(
            onKeyPress = onKeyPress,
            onSpecialAction = onSpecialAction,
            pressedKey = pressedKey,
            isLeetModeActive = isLeetModeActive
        )
    }
}

@Composable
private fun KeyboardRow(
    keys: List<String>,
    onKeyPress: (String) -> Unit,
    pressedKey: String?,
    isLeetModeActive: Boolean,
    specialKey: KeyboardKey.Special? = null,
    onSpecialAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Regular keys
        keys.forEach { key ->
            KeyboardButton(
                key = KeyboardKey.Letter(key),
                onPress = { onKeyPress(key) },
                isPressed = pressedKey == key,
                isLeetModeActive = isLeetModeActive,
                modifier = Modifier.weight(1f)
            )
        }

        // Special key (if provided)
        specialKey?.let { special ->
            KeyboardButton(
                key = special,
                onPress = { onSpecialAction(special.action) },
                isPressed = pressedKey == special.text,
                isLeetModeActive = isLeetModeActive,
                modifier = Modifier.weight(1.2f)
            )
        }
    }
}

@Composable
private fun BottomRow(
    onKeyPress: (String) -> Unit,
    onSpecialAction: (String) -> Unit,
    pressedKey: String?,
    isLeetModeActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Numbers toggle
        KeyboardButton(
            key = KeyboardKey.Special("123", Icons.Default.Numbers, "TOGGLE_NUMBERS"),
            onPress = { onSpecialAction("TOGGLE_NUMBERS") },
            isPressed = false,
            isLeetModeActive = isLeetModeActive,
            modifier = Modifier.weight(1f)
        )

        // Space bar
        KeyboardButton(
            key = KeyboardKey.Letter("SPACE"),
            onPress = { onKeyPress("SPACE") },
            isPressed = pressedKey == "SPACE",
            isLeetModeActive = isLeetModeActive,
            modifier = Modifier.weight(4f)
        )

        // Enter
        KeyboardButton(
            key = KeyboardKey.Special("↵", Icons.Default.KeyboardReturn, "ENTER"),
            onPress = { onKeyPress("ENTER") },
            isPressed = pressedKey == "ENTER",
            isLeetModeActive = isLeetModeActive,
            modifier = Modifier.weight(1.2f)
        )

        // Leet signature
        KeyboardButton(
            key = KeyboardKey.Special("1337", Icons.Default.Casino, "LEET_SIGNATURE"),
            onPress = { onSpecialAction("LEET_SIGNATURE") },
            isPressed = false,
            isLeetModeActive = isLeetModeActive,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 🔘 Individual Keyboard Button
 */
@Composable
private fun KeyboardButton(
    key: KeyboardKey,
    onPress: () -> Unit,
    isPressed: Boolean,
    isLeetModeActive: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "button_scale"
    )

    val backgroundColor = when {
        isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        key is KeyboardKey.Special -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        isLeetModeActive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        isPressed -> MaterialTheme.colorScheme.primary
        key is KeyboardKey.Special -> MaterialTheme.colorScheme.secondary
        isLeetModeActive -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onPress,
        modifier = modifier
            .height(48.dp)
            .scale(scale),
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (isPressed) 0.dp else 2.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            when (key) {
                is KeyboardKey.Letter -> {
                    Text(
                        text = key.text,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                }
                is KeyboardKey.Special -> {
                    if (key.icon != null) {
                        Icon(
                            imageVector = key.icon,
                            contentDescription = key.text,
                            modifier = Modifier.size(18.dp),
                            tint = textColor
                        )
                    } else {
                        Text(
                            text = key.text,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 🍞 Custom Toast für Keyboard
 */
@Composable
private fun KeyboardToast(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.inverseSurface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 8.dp
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.inverseOnSurface
        )
    }
}

/**
 * 🔑 Keyboard Key Types
 */
sealed class KeyboardKey {
    data class Letter(val text: String) : KeyboardKey()
    data class Special(
        val text: String,
        val icon: ImageVector? = null,
        val action: String
    ) : KeyboardKey()
}

/**
 * 🎨 Preview Functions
 */
/*
@Preview(showBackground = true)
@Composable
private fun KeyboardPreview() {
    MaterialTheme {
        LeetKeyboardUI(
            viewModel = LeetKeyboardViewModel(/* mock dependencies */),
            onKeyPress = {},
            onSpecialAction = {},
            onModeToggle = {},
            onSettingsOpen = {}
        )
    }
}
*/