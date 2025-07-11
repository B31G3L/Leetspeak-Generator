package com.beigel.leetSpeak_Generator.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beigel.leetSpeak_Generator.ui.components.*
import com.beigel.leetSpeak_Generator.ui.theme.LeetspeakGeneratorTheme
import com.beigel.leetSpeak_Generator.viewmodel.MainIntent
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class ComposeMainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        setContent {
            LeetspeakGeneratorTheme {
                MainScreen(
                    viewModel = viewModel,
                    onCopyToClipboard = { text ->
                        copyToClipboardWithFeedback(text)
                    }
                )
            }
        }
    }

    private fun copyToClipboardWithFeedback(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Leetspeak Text", text)
        clipboard.setPrimaryClip(clip)

        // Modern haptic feedback
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }

        viewModel.handleIntent(MainIntent.CopyToClipboard)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onCopyToClipboard: (String) -> Unit
) {
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val outputText by viewModel.outputText.collectAsStateWithLifecycle()
    val currentModeDisplayName by viewModel.currentModeDisplayName.collectAsStateWithLifecycle()
    val translationStats by viewModel.translationStats.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shouldShowOutput by viewModel.shouldShowOutput.collectAsStateWithLifecycle()

    var showBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                ),
                actions = {
                    IconButton(onClick = {
                        android.widget.Toast.makeText(
                            context,
                            "Aktueller Modus: $currentModeDisplayName",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // Main Content Card - FIXIERTES LAYOUT
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RectangleShape
            ) {
                // FIXIERTE CONTAINER-HÖHEN
                if (shouldShowOutput) {
                    // Mit Output: 50/50 Aufteilung
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Input Section - exakt die Hälfte
                        InputSection(
                            inputText = inputText,
                            onInputChange = { text ->
                                viewModel.handleIntent(MainIntent.UpdateInput(text))
                            },
                            onClearText = {
                                viewModel.handleIntent(MainIntent.ClearInput)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f) // 50%
                        )

                        // Divider
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            thickness = 2.dp
                        )

                        // Output Section - exakt die Hälfte
                        OutputSection(
                            outputText = outputText,
                            currentMode = currentModeDisplayName,
                            onCopyClick = { onCopyToClipboard(outputText) },
                            translationStats = translationStats,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f) // 50%
                        )
                    }
                } else {
                    // Ohne Output: Input nimmt alles
                    InputSection(
                        inputText = inputText,
                        onInputChange = { text ->
                            viewModel.handleIntent(MainIntent.UpdateInput(text))
                        },
                        onClearText = {
                            viewModel.handleIntent(MainIntent.ClearInput)
                        },
                        modifier = Modifier.fillMaxSize() // 100%
                    )
                }
            }

            // Button Section
            ButtonSection(
                currentMode = currentModeDisplayName,
                onLeetSelectorClick = {
                    showBottomSheet = true
                },
                onPlainModeClick = {
                    viewModel.handleIntent(MainIntent.ClearInput)
                }
            )
        }

        // Bottom Sheet
        if (showBottomSheet) {
            LeetSelectorBottomSheet(
                viewModel = viewModel,
                onDismiss = { showBottomSheet = false }
            )
        }

        // UI State Handling
        HandleUiState(uiState, viewModel, context)
    }
}

@Composable
private fun ButtonSection(
    currentMode: String,
    onLeetSelectorClick: () -> Unit,
    onPlainModeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Plain Text Button
            var plainButtonPressed by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = {
                    plainButtonPressed = true
                    onPlainModeClick()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    containerColor = if (plainButtonPressed)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.surface
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        MaterialTheme.colorScheme.secondary
                    )
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Clear",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            LaunchedEffect(plainButtonPressed) {
                if (plainButtonPressed) {
                    delay(150)
                    plainButtonPressed = false
                }
            }

            // Animated Arrows
            AnimatedArrows()

            // Leet Mode Button
            var leetButtonPressed by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    leetButtonPressed = true
                    onLeetSelectorClick()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (leetButtonPressed)
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                    else
                        MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
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
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            LaunchedEffect(leetButtonPressed) {
                if (leetButtonPressed) {
                    delay(150)
                    leetButtonPressed = false
                }
            }
        }
    }
}

@Composable
private fun AnimatedArrows() {
    val infiniteTransition = rememberInfiniteTransition(label = "arrows")

    val arrowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_alpha"
    )

    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_offset"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .graphicsLayer(
                        alpha = arrowAlpha,
                        translationX = arrowOffset
                    ),
                tint = MaterialTheme.colorScheme.secondary
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .graphicsLayer(
                        alpha = arrowAlpha,
                        translationX = -arrowOffset
                    ),
                tint = MaterialTheme.colorScheme.secondary
            )
        }

        // Mini-Text für Klarheit
        Text(
            text = "Modes",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
            fontSize = 9.sp
        )
    }
}

@Composable
private fun HandleUiState(
    uiState: com.beigel.leetSpeak_Generator.viewmodel.MainUiState,
    viewModel: MainViewModel,
    context: Context
) {
    // Success Message
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

    // Error Message
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