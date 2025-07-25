package com.beigel.leetSpeak_Generator.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import com.beigel.leetSpeak_Generator.ui.components.AboutDialog
import com.beigel.leetSpeak_Generator.ui.components.LeetSelectorBottomSheet
import com.beigel.leetSpeak_Generator.ui.theme.LeetspeakGeneratorTheme
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.text.style.TextAlign
import com.beigel.leetSpeak_Generator.presentation.intent.MainIntent
import com.beigel.leetSpeak_Generator.presentation.intent.MainUiState
import com.beigel.leetSpeak_Generator.ui.components.WhatsNewDialog
import com.beigel.leetSpeak_Generator.ui.settings.SettingsActivity


@AndroidEntryPoint
class ComposeMainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        setContent {
            // Theme aus Settings laden und überwachen
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDarkTheme = when (themeMode) {
                com.beigel.leetSpeak_Generator.data.ThemePreferences.THEME_DARK -> true
                com.beigel.leetSpeak_Generator.data.ThemePreferences.THEME_LIGHT -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            LeetspeakGeneratorTheme(darkTheme = isDarkTheme) {
                MainScreen(
                    viewModel = viewModel,
                    onCopyToClipboard = { text -> copyToClipboardWithFeedback(text) },
                    onOpenSettings = {
                        startActivity(
                            Intent(
                                this@ComposeMainActivity,
                                SettingsActivity::class.java
                            )
                        )
                    }
                )
            }
        }
    }

    private fun copyToClipboardWithFeedback(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Leetspeak Text", text)
        clipboard.setPrimaryClip(clip)

        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

// Nur der relevante Teil der ComposeMainActivity.kt mit What's New Integration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onCopyToClipboard: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    // Bestehende State Variables...
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val outputText by viewModel.outputText.collectAsStateWithLifecycle()
    val currentModeDisplayName by viewModel.currentModeDisplayName.collectAsStateWithLifecycle()
    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val currentLeet by viewModel.currentLeet.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isReverseMode by viewModel.isReverseMode.collectAsStateWithLifecycle()
    val isInputLikelyLeetspeak by viewModel.isInputLikelyLeetspeak.collectAsStateWithLifecycle()

    // NEW: What's New State
    val shouldShowWhatsNew by viewModel.shouldShowWhatsNew.collectAsStateWithLifecycle()
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsStateWithLifecycle()

    // Bestehende lokale State Variables...
    val density = LocalDensity.current
    val keyboardHeight = WindowInsets.ime.getBottom(density)
    val isKeyboardVisible = keyboardHeight > 100
    val hasInput = inputText.isNotEmpty()
    val hasOutput = outputText.isNotEmpty()

    var showBottomSheet by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val inputTitle = if (isReverseMode) {
        "Input: $currentModeDisplayName"
    } else {
        "Input: Plaintext"
    }

    val outputTitle = if (isReverseMode) {
        "Output: Plaintext"
    } else {
        "Output: $currentModeDisplayName"
    }

    // Bestehender Scaffold Content bleibt gleich...
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Leetspeak Generator",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp)
                        )

                        if (isReverseMode) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) {
                                Text(
                                    text = "R",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (!isReverseMode && isInputLikelyLeetspeak) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) {
                                Text(
                                    text = "L",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(onClick = { showAboutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, "Einstellungen")
                    }


                }
            )
        },
        bottomBar = {
            // Bestehende BottomAppBar bleibt gleich...
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.then(
                    if (isKeyboardVisible) {
                        Modifier.offset(y = 56.dp)
                    } else {
                        Modifier
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EnhancedAnimatedArrows(
                        isReverseMode = isReverseMode,
                        isInputLikelyLeetspeak = isInputLikelyLeetspeak,
                        onToggleReverse = {
                            viewModel.handleIntent(MainIntent.ToggleReverseMode)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ModeSelectorButton(
                        currentMode = currentModeDisplayName,
                        onLeetSelectorClick = { showBottomSheet = true },
                        modifier = Modifier.weight(2f)
                    )
                }
            }
        }
    ) { paddingValues ->

        // Bestehender Content bleibt gleich...
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            InputCard(
                inputText = inputText,
                onInputChange = {
                    viewModel.updateInputText(it)
                },
                onClearText = {
                    viewModel.clearInput()
                },
                showHeader = true,
                isReverseMode = isReverseMode,
                title = inputTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            if (hasOutput) {
                OutputCard(
                    outputText = outputText,
                    currentMode = outputTitle,
                    onCopyClick = { onCopyToClipboard(outputText) },
                    showHeader = true,
                    isReverseMode = isReverseMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }

        // Bestehende Dialoge...
        if (showBottomSheet) {
            LeetSelectorBottomSheet(
                viewModel = viewModel,
                onDismiss = { showBottomSheet = false }
            )
        }

        if (showAboutDialog) {
            AboutDialog(
                onDismiss = { showAboutDialog = false }
            )
        }

        // NEW: What's New Dialog
        if (shouldShowWhatsNew) {
            WhatsNewDialog(
                currentVersion = viewModel.currentVersionInfo,
                isFirstLaunch = isFirstLaunch,
                onDismiss = {
                    viewModel.handleIntent(MainIntent.DismissWhatsNew)
                },
                onMarkAsShown = {
                    viewModel.handleIntent(MainIntent.MarkWhatsNewAsShown)
                }
            )
        }

        HandleUiState(uiState, viewModel, context)
    }
}

// Bestehende Helper Functions bleiben unverändert...
// (EnhancedAnimatedArrows, ModeSelectorButton, InputCard, OutputCard, HandleUiState)


@Composable
fun InputCard(
    inputText: String,
    onInputChange: (String) -> Unit,
    onClearText: () -> Unit,
    showHeader: Boolean = true,
    isReverseMode: Boolean = false, // ✅ NEU: Reverse-Modus Parameter
    title: String = "Input: Plaintext", // ✅ NEU: Title Parameter
    modifier: Modifier = Modifier
) {
    // Adaptive Textgröße
    val adaptiveTextSize = remember(inputText.length) {
        when {
            inputText.length <= 50 -> 18.sp
            inputText.length <= 200 -> 16.sp
            inputText.length <= 500 -> 14.sp
            else -> 12.sp
        }
    }

    val adaptiveLineHeight = adaptiveTextSize * 1.4f

    val cardColors =  CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.3f)) // ✅ Immer Border
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            if (showHeader) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = headerTextColor
                    )

                    if (inputText.isNotEmpty()) {
                        IconButton(
                            onClick = onClearText,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxSize(),
                placeholder = {
                    Text(
                        text = if (isReverseMode) {
                            "Leetspeak Text eingeben..."
                        } else {
                            "Hier deinen Text eingeben..."
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = adaptiveTextSize
                        )
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = adaptiveTextSize,
                    lineHeight = adaptiveLineHeight
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.medium,
                singleLine = false
            )
        }
    }
}

@Composable
fun OutputCard(
    outputText: String,
    currentMode: String,
    onCopyClick: () -> Unit,
    showHeader: Boolean = true,
    isReverseMode: Boolean = false, // ✅ NEU: Reverse-Modus Parameter
    modifier: Modifier = Modifier
) {
    var showCopyFeedback by remember { mutableStateOf(false) }

    val adaptiveTextSize = remember(outputText.length) {
        when {
            outputText.length <= 50 -> 18.sp
            outputText.length <= 200 -> 16.sp
            outputText.length <= 500 -> 14.sp
            else -> 12.sp
        }
    }

    val adaptiveLineHeight = adaptiveTextSize * 1.4f

    LaunchedEffect(showCopyFeedback) {
        if (showCopyFeedback) {
            delay(1500)
            showCopyFeedback = false
        }
    }

    val cardColors =  CardDefaults.cardColors(
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

    Card(
        modifier = modifier,
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.3f)) // ✅ Immer Border
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            if (showHeader) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentMode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = headerTextColor
                    )

                    IconButton(
                        onClick = {
                            onCopyClick()
                            showCopyFeedback = true
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        AnimatedContent(
                            targetState = showCopyFeedback,
                            transitionSpec = {
                                scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                            },
                            label = "copy_feedback"
                        ) { feedback ->
                            Icon(
                                imageVector = if (feedback) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = if (feedback) "Kopiert!" else "Kopieren",
                                tint = headerTextColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = outputText,
                onValueChange = { },
                modifier = Modifier.fillMaxSize(),
                readOnly = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = adaptiveTextSize,
                    lineHeight = adaptiveLineHeight
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor.copy(alpha = 0.5f),
                ),
                shape = MaterialTheme.shapes.medium,
                singleLine = false
            )
        }
    }
}

// ✅ MODE SELECTOR BUTTON - Standalone Komponente
@Composable
private fun ModeSelectorButton(
    currentMode: String,
    onLeetSelectorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onLeetSelectorClick,
        modifier = modifier.height(48.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Transform Icon
            Icon(
                imageVector = Icons.Default.Transform,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Mode Text - größere Schrift
            Text(
                text = currentMode,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ✅ NEUE ENHANCED ANIMATED ARROWS - clickbar und informativ
@Composable
private fun EnhancedAnimatedArrows(
    isReverseMode: Boolean,
    isInputLikelyLeetspeak: Boolean,
    onToggleReverse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arrows")

    val arrowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_alpha"
    )

    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_offset"
    )

    // Pulsing effect für Leetspeak Detection
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        onClick = onToggleReverse,
        modifier = modifier,
        color = when {
            isReverseMode -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
            isInputLikelyLeetspeak -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = pulseAlpha * 0.5f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            // Arrows
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isReverseMode) {
                    // Reverse arrows (nach links)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer(
                                alpha = arrowAlpha,
                                translationX = -arrowOffset
                            ),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer(
                                alpha = arrowAlpha * 0.7f,
                                translationX = arrowOffset
                            ),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    // Normal arrows (nach rechts)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer(
                                alpha = arrowAlpha * 0.7f,
                                translationX = arrowOffset
                            ),
                        tint = if (isInputLikelyLeetspeak) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer(
                                alpha = arrowAlpha,
                                translationX = -arrowOffset
                            ),
                        tint = if (isInputLikelyLeetspeak) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Info Text
            Text(
                text = when {
                    isReverseMode -> "Reverse"
                    isInputLikelyLeetspeak -> "Leet erkannt!"
                    else -> "Modus"
                },
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = when {
                    isReverseMode -> MaterialTheme.colorScheme.tertiary
                    isInputLikelyLeetspeak -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                },
                fontWeight = if (isReverseMode || isInputLikelyLeetspeak) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun HandleUiState(
    uiState: com.beigel.leetSpeak_Generator.domain.usecase.ui.UiStateManagementUseCase.UiState,
    viewModel: MainViewModel,
    context: Context
) {
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            android.widget.Toast.makeText(
                context,
                "✅ $message",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            viewModel.handleIntent(MainIntent.ClearSuccess)
        }
    }

    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            android.widget.Toast.makeText(
                context,
                "❌ $message",
                android.widget.Toast.LENGTH_LONG
            ).show()
            viewModel.handleIntent(MainIntent.ClearError)
        }
    }
}