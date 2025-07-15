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

        WindowCompat.setDecorFitsSystemWindows(window, false)

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

        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onCopyToClipboard: (String) -> Unit
) {
    // Lokaler State
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }

    // ViewModel State
    val currentModeDisplayName by viewModel.currentModeDisplayName.collectAsStateWithLifecycle()
    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val currentLeet by viewModel.currentLeet.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ✅ EINFACHE Logik - nur diese 2 Variablen!
    val hasInput = inputText.isNotEmpty()
    val hasOutput = outputText.isNotEmpty()

    // Keyboard Detection
    val density = LocalDensity.current
    val keyboardHeight = WindowInsets.ime.getBottom(density)
    val isKeyboardVisible = keyboardHeight > 100

    var showBottomSheet by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Translation
    LaunchedEffect(inputText, currentMode, currentLeet) {
        outputText = if (inputText.isEmpty()) {
            ""
        } else {
            LeetTranslator.translate(inputText, currentMode, currentLeet)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Leetspeak Generator",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp)
                    )
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
                }
            )
        }
    ) { paddingValues ->

        // ✅ EINFACHES LAYOUT - Keyboard reduziert die Gesamthöhe
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .then(
                    if (isKeyboardVisible) {
                        Modifier.padding(bottom = with(density) { keyboardHeight.toDp() })
                    } else {
                        Modifier
                    }
                )
        ) {

            // ✅ CONTENT BEREICH - nimmt verfügbaren Platz
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ✅ INPUT CARD - immer sichtbar
                InputCard(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onClearText = { inputText = "" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (hasOutput) {
                                Modifier.weight(1f) // 50% wenn Output da ist
                            } else {
                                Modifier.weight(1f) // 100% wenn kein Output
                            }
                        )
                )

                // ✅ OUTPUT CARD - nur wenn Output vorhanden
                if (hasOutput) {
                    OutputCard(
                        outputText = outputText,
                        currentMode = currentModeDisplayName,
                        onCopyClick = { onCopyToClipboard(outputText) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // 50%
                    )
                }
            }

            // ✅ BUTTON SECTION - immer unten
            ButtonSection(
                currentMode = currentModeDisplayName,
                onLeetSelectorClick = { showBottomSheet = true },
                onClearClick = { inputText = "" }
            )
        }

        // Dialoge
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

        HandleUiState(uiState, viewModel, context)
    }
}

// ✅ INPUT CARD - Eigene Card-Komponente
@Composable
private fun InputCard(
    inputText: String,
    onInputChange: (String) -> Unit,
    onClearText: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ ADAPTIVE TEXTGRÖSSE basierend auf Textlänge
    val adaptiveTextSize = remember(inputText.length) {
        when {
            inputText.length <= 50 -> 18.sp      // Kurzer Text: Große Schrift
            inputText.length <= 200 -> 16.sp     // Mittlerer Text: Normale Schrift
            inputText.length <= 500 -> 14.sp     // Langer Text: Kleinere Schrift
            else -> 12.sp                        // Sehr langer Text: Kleine Schrift
        }
    }

    val adaptiveLineHeight = adaptiveTextSize * 1.4f
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Plaintext",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (inputText.isNotEmpty()) {
                    IconButton(
                        onClick = onClearText,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TextField
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxSize(),
                placeholder = {
                    Text(
                        text = "Hier deinen Text eingeben...",
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
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.medium,
                singleLine = false
            )
        }
    }
}

// ✅ OUTPUT CARD - Eigene Card-Komponente
@Composable
private fun OutputCard(
    outputText: String,
    currentMode: String,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCopyFeedback by remember { mutableStateOf(false) }

    // ✅ ADAPTIVE TEXTGRÖSSE auch für Output
    val adaptiveTextSize = remember(outputText.length) {
        when {
            outputText.length <= 50 -> 18.sp      // Kurzer Text: Große Schrift
            outputText.length <= 200 -> 16.sp     // Mittlerer Text: Normale Schrift
            outputText.length <= 500 -> 14.sp     // Langer Text: Kleinere Schrift
            else -> 12.sp                         // Sehr langer Text: Kleine Schrift
        }
    }

    val adaptiveLineHeight = adaptiveTextSize * 1.4f

    LaunchedEffect(showCopyFeedback) {
        if (showCopyFeedback) {
            delay(1500)
            showCopyFeedback = false
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentMode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Copy button
                IconButton(
                    onClick = {
                        onCopyClick()
                        showCopyFeedback = true
                    },
                    modifier = Modifier.size(40.dp)
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
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Output TextField
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
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    disabledBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = MaterialTheme.shapes.medium,
                singleLine = false
            )
        }
    }
}

// ✅ BUTTON SECTION - Vereinfacht
@Composable
private fun ButtonSection(
    currentMode: String,
    onLeetSelectorClick: () -> Unit,
    onClearClick: () -> Unit,
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

            OutlinedButton(
                onClick = onClearClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear")
            }

            // Animated arrows
            AnimatedArrows()

            Button(
                onClick = onLeetSelectorClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Transform,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentMode,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
                    .size(16.dp)
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
                    .size(16.dp)
                    .graphicsLayer(
                        alpha = arrowAlpha,
                        translationX = -arrowOffset
                    ),
                tint = MaterialTheme.colorScheme.secondary
            )
        }

        Text(
            text = "Modes",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun HandleUiState(
    uiState: com.beigel.leetSpeak_Generator.viewmodel.MainUiState,
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