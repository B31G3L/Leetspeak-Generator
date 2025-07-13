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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

        // WindowCompat für moderne Inset-Behandlung
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

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
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

    // ✅ EINFACHE Keyboard Detection - ohne komplexe LaunchedEffect
    val density = LocalDensity.current
    val keyboardHeight = WindowInsets.ime.getBottom(density)
    val isKeyboardVisible = keyboardHeight > 100 // Einfacher Threshold
    val hasOutput = outputText.isNotEmpty()

    var showBottomSheet by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Lokale Translation
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

            // ✅ FIXES LAYOUT mit besserer Platz-Verteilung
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RectangleShape
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // ✅ INPUT - mit besserer Mindest-Höhe
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                when {
                                    !hasOutput -> Modifier.weight(1f) // Kein Output: 100%
                                    !isKeyboardVisible -> Modifier.weight(0.5f) // Normal: 50%
                                    else -> Modifier.heightIn(min = 120.dp).weight(0.5f) // ✅ Mit Keyboard: Mindest-Höhe + 50%
                                }
                            )
                    ) {
                        InputSection(
                            inputText = inputText,
                            onInputChange = { inputText = it },
                            onClearText = { inputText = "" },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // ✅ OUTPUT - nur wenn Text da, mit intelligenter Höhe
                    AnimatedVisibility(
                        visible = hasOutput,
                        enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                        exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
                    ) {
                        Column {
                            // Divider
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                thickness = if (isKeyboardVisible) 1.dp else 2.dp
                            )

                            // Output Section mit angepasster Höhe
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (isKeyboardVisible) {
                                            // ✅ Bei Keyboard: Feste kompakte Höhe
                                            Modifier.height(100.dp)
                                        } else {
                                            // ✅ Ohne Keyboard: 50% des Platzes
                                            Modifier.weight(0.5f)
                                        }
                                    )
                            ) {
                                if (isKeyboardVisible) {
                                    // Kompakte Version für Keyboard
                                    CompactKeyboardOutput(
                                        outputText = outputText,
                                        currentMode = currentModeDisplayName,
                                        onCopyClick = { onCopyToClipboard(outputText) }
                                    )
                                } else {
                                    // Normale Version
                                    OutputSection(
                                        outputText = outputText,
                                        currentMode = currentModeDisplayName,
                                        onCopyClick = { onCopyToClipboard(outputText) },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Button Section
            ButtonSection(
                currentMode = currentModeDisplayName,
                onLeetSelectorClick = { showBottomSheet = true },
                onPlainModeClick = { inputText = "" }
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

// LAYOUT KOMPONENTEN - VEREINFACHT

// ✅ Die alten Layout-Komponenten sind nicht mehr nötig!
// Alles läuft über die einheitliche Struktur im MainScreen

// UI KOMPONENTEN

@Composable
private fun InputSection(
    inputText: String,
    onInputChange: (String) -> Unit,
    onClearText: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ MINIMAL - wie die funktionierende Version
    Column(modifier = modifier.padding(16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Plaintext",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
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

        // ✅ EINFACHES TextField - zurück zur funktionierenden Version
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange, // Direkt weiterleiten
            modifier = Modifier.fillMaxSize(),
            placeholder = { Text("Hier deinen Text eingeben...") },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            shape = MaterialTheme.shapes.large,
            singleLine = false,
            maxLines = Int.MAX_VALUE
        )
    }
}

@Composable
private fun OutputSection(
    outputText: String,
    currentMode: String,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCopyFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(showCopyFeedback) {
        if (showCopyFeedback) {
            delay(1500)
            showCopyFeedback = false
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentMode,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

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
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.large
        )
    }
}

@Composable
private fun CompactKeyboardOutput(
    outputText: String,
    currentMode: String,
    onCopyClick: () -> Unit
) {
    var showCopyFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(showCopyFeedback) {
        if (showCopyFeedback) {
            delay(1200)
            showCopyFeedback = false
        }
    }

    // ✅ ULTRA-KOMPAKTE Keyboard-Version - nur das Nötigste!
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Kompakter Text-Bereich
            Column(modifier = Modifier.weight(1f)) {
                // Mini-Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Transform,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = currentMode,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                    // Character count
                    Text(
                        text = "(${outputText.length})",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ✅ Output Text - scrollable, 2 Zeilen max
                Text(
                    text = outputText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ✅ Kompakter Copy-Button
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
                    label = "ultra_compact_copy"
                ) { feedback ->
                    if (feedback) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Kopiert!",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Kopieren",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
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
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.secondary)
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
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Clear",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
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

            AnimatedArrows()

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
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        currentMode,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
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