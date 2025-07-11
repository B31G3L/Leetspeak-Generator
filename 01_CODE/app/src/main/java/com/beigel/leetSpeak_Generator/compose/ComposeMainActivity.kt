// KORRIGIERTE ComposeMainActivity.kt - Final Version ohne Compile-Fehler
// Ersetze: app/src/main/java/com/beigel/leetSpeak_Generator/compose/ComposeMainActivity.kt

package com.beigel.leetSpeak_Generator.compose

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beigel.leetSpeak_Generator.MainIntent
import com.beigel.leetSpeak_Generator.MainViewModel
import com.beigel.leetSpeak_Generator.ui.theme.LeetspeakGeneratorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay


@AndroidEntryPoint
class ComposeMainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        setContent {
            LeetspeakGeneratorTheme {
                // ✅ Enhanced Version mit funktionierendem Bottom Sheet
                EnhancedComposeMainScreenWithWorkingBottomSheet(
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

        // Haptic feedback
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, 120))
        }

        viewModel.handleIntent(MainIntent.CopyToClipboard)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedComposeMainScreenWithWorkingBottomSheet(
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
    val context = LocalContext.current // ✅ FIX: Innerhalb der Composable

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
                    // Settings/Info Button
                    IconButton(onClick = {
                        // ✅ FIX: context innerhalb onClick verwenden
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

            // Main Content Card
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
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    // Input Section
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

                    // Animated Divider
                    AnimatedVisibility(
                        visible = shouldShowOutput,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            thickness = 2.dp
                        )
                    }

                    // Output Section
                    AnimatedVisibility(
                        visible = shouldShowOutput,
                        enter = expandVertically() + fadeIn(
                            animationSpec = tween(300, delayMillis = 100)
                        ),
                        exit = shrinkVertically() + fadeOut()
                    ) {
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

            // Enhanced Button Section
            EnhancedButtonSectionWithBottomSheet(
                currentMode = currentModeDisplayName,
                onLeetSelectorClick = {
                    // ✅ Aktiviere das Working Bottom Sheet
                    showBottomSheet = true
                },
                onPlainModeClick = {
                    // Clear Input als Demo-Funktion
                    viewModel.handleIntent(MainIntent.ClearInput)
                }
            )
        }

        // ✅ Working Bottom Sheet
        if (showBottomSheet) {
            WorkingBottomSheet(
                viewModel = viewModel,
                onDismiss = { showBottomSheet = false }
            )
        }

        // UI State Handling mit korrigiertem Context
        HandleUiStateFixed(uiState, viewModel, context)
    }
}

@Composable
private fun EnhancedButtonSectionWithBottomSheet(
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

            // Enhanced Animated Arrows
            EnhancedAnimatedArrows()

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
private fun EnhancedAnimatedArrows() {
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
                    .alpha(arrowAlpha)
                    .offset(x = arrowOffset.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .alpha(arrowAlpha)
                    .offset(x = (-arrowOffset).dp),
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

// ✅ FIX: HandleUiState mit Context Parameter
@Composable
private fun HandleUiStateFixed(
    uiState: com.beigel.leetSpeak_Generator.MainUiState,
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

// ✅ FALLBACK VERSION - Falls Working Bottom Sheet Probleme macht
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleBottomSheetVersion(
    viewModel: MainViewModel,
    onCopyToClipboard: (String) -> Unit
) {
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val outputText by viewModel.outputText.collectAsStateWithLifecycle()
    val currentModeDisplayName by viewModel.currentModeDisplayName.collectAsStateWithLifecycle()
    val shouldShowOutput by viewModel.shouldShowOutput.collectAsStateWithLifecycle()

    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leetspeak Generator") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Simple Input
            OutlinedTextField(
                value = inputText,
                onValueChange = { viewModel.handleIntent(MainIntent.UpdateInput(it)) },
                label = { Text("Input") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )

            // Simple Output
            if (shouldShowOutput) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Output ($currentModeDisplayName):",
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { onCopyToClipboard(outputText) }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    "Copy",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        Text(
                            outputText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Simple Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.handleIntent(MainIntent.ClearInput) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }

                Button(
                    onClick = { showBottomSheet = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(currentModeDisplayName)
                }
            }
        }

        // Working Bottom Sheet
        if (showBottomSheet) {
            WorkingBottomSheet(
                viewModel = viewModel,
                onDismiss = { showBottomSheet = false }
            )
        }
    }
}