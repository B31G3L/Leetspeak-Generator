package com.beigel.leetSpeak_Generator.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.ui.components.AboutDialog
import com.beigel.leetSpeak_Generator.ui.components.leet.selector.ModiScreen
import com.beigel.leetSpeak_Generator.ui.theme.LeetspeakGeneratorTheme
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.beigel.leetSpeak_Generator.presentation.intent.MainIntent
import com.beigel.leetSpeak_Generator.review.InAppReviewManager
import com.beigel.leetSpeak_Generator.ui.components.ClearInputDialog
import com.beigel.leetSpeak_Generator.ui.components.input.InputCard
import com.beigel.leetSpeak_Generator.ui.onboarding.OnboardingScreen
import com.beigel.leetSpeak_Generator.ui.settings.SettingsActivity
import com.beigel.leetSpeak_Generator.ui.components.output.OutputCard
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import javax.inject.Inject

@AndroidEntryPoint
class ComposeMainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var vibrator: Vibrator

    @Inject
    lateinit var inAppReviewManager: InAppReviewManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        setContent {
            val themeMode             by viewModel.themeMode.collectAsStateWithLifecycle()
            val appTheme              by viewModel.appTheme.collectAsStateWithLifecycle()
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()

            val isDarkTheme = when (themeMode) {
                com.beigel.leetSpeak_Generator.data.ThemePreferences.THEME_DARK  -> true
                com.beigel.leetSpeak_Generator.data.ThemePreferences.THEME_LIGHT -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            LeetspeakGeneratorTheme(
                darkTheme = isDarkTheme,
                appTheme  = appTheme
            ) {
                if (!isOnboardingCompleted) {
                    OnboardingScreen(
                        onComplete = { viewModel.completeOnboarding() }
                    )
                } else {
                    val shouldRequestReview by viewModel.shouldRequestReview.collectAsStateWithLifecycle()

                    MainScreen(
                        viewModel         = viewModel,
                        onCopyToClipboard = { text -> copyToClipboardWithFeedback(text) },
                        onOpenSettings    = {
                            startActivity(Intent(this@ComposeMainActivity, SettingsActivity::class.java))
                        },
                        onBugReport   = { sendBugReport() },
                        onFeedback    = { sendFeedback() },
                        onKofiSupport = { openKofiLink() }
                    )

                    LaunchedEffect(shouldRequestReview) {
                        if (shouldRequestReview) {
                            kotlinx.coroutines.delay(1000)
                            inAppReviewManager.requestReview(this@ComposeMainActivity)
                            viewModel.onReviewHandled()
                        }
                    }
                }
            }
        }
    }

    private fun copyToClipboardWithFeedback(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(getString(R.string.clipboard_label), text)
        clipboard.setPrimaryClip(clip)

        if (viewModel.hapticFeedbackEnabled.value) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun sendBugReport() {
        try {
            val versionName = getVersionName()
            val deviceInfo = buildString {
                appendLine(getString(R.string.bug_report_header))
                appendLine()
                appendLine(getString(R.string.bug_report_describe))
                appendLine(getString(R.string.bug_report_question_1))
                appendLine(getString(R.string.bug_report_question_2))
                appendLine(getString(R.string.bug_report_question_3))
                appendLine()
                appendLine(getString(R.string.bug_report_device_info))
                appendLine(getString(R.string.bug_report_app_version, versionName))
                appendLine(getString(R.string.bug_report_android_version, Build.VERSION.RELEASE, Build.VERSION.SDK_INT))
                appendLine(getString(R.string.bug_report_device, Build.MANUFACTURER, Build.MODEL))
                appendLine(getString(R.string.bug_report_brand, Build.BRAND))
                appendLine(getString(R.string.bug_report_language, java.util.Locale.getDefault().language))
                appendLine()
                appendLine(getString(R.string.bug_report_additional))
                appendLine(getString(R.string.bug_report_additional_hint))
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.bug_report_email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.bug_report_subject, versionName))
                putExtra(Intent.EXTRA_TEXT, deviceInfo)
            }

            val chooser = Intent.createChooser(intent, getString(R.string.bug_report_send_chooser))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(chooser)
            } else {
                copyToClipboardWithFeedback(deviceInfo)
                android.widget.Toast.makeText(this, getString(R.string.no_email_app), android.widget.Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, getString(R.string.bug_report_error_format, e.message), android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun sendFeedback() {
        try {
            val versionName = getVersionName()
            val body = buildString {
                appendLine(getString(R.string.feedback_header))
                appendLine()
                appendLine(getString(R.string.feedback_describe))
                appendLine(getString(R.string.feedback_placeholder))
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.bug_report_email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject, versionName))
                putExtra(Intent.EXTRA_TEXT, body)
            }

            val chooser = Intent.createChooser(intent, getString(R.string.feedback_send))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(chooser)
            } else {
                android.widget.Toast.makeText(this, getString(R.string.no_email_app), android.widget.Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, getString(R.string.bug_report_error_format, e.message), android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun openKofiLink() {
        try {
            val kofiUrl = getString(R.string.url_kofi)
            val intent  = Intent(Intent.ACTION_VIEW, Uri.parse(kofiUrl))
            startActivity(intent)
            android.widget.Toast.makeText(this, getString(R.string.kofi_toast_thanks), android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, getString(R.string.kofi_toast_error), android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun getVersionName(): String {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onCopyToClipboard: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onBugReport: () -> Unit = {},
    onFeedback: () -> Unit = {},
    onKofiSupport: () -> Unit = {}
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    val inputText              by viewModel.inputText.collectAsStateWithLifecycle()
    val outputText             by viewModel.outputText.collectAsStateWithLifecycle()
    val currentModeDisplayName by viewModel.currentModeDisplayName.collectAsStateWithLifecycle()
    val uiState                by viewModel.uiState.collectAsStateWithLifecycle()
    val isReverseMode          by viewModel.isReverseMode.collectAsStateWithLifecycle()
    val isInputLikelyLeetspeak by viewModel.isInputLikelyLeetspeak.collectAsStateWithLifecycle()
    val showClearInputDialog   by viewModel.showClearInputDialog.collectAsStateWithLifecycle()
    val pendingDelete          by viewModel.pendingDelete.collectAsStateWithLifecycle()
    val currentMode            by viewModel.currentMode.collectAsStateWithLifecycle()  // NEU: für Animation

    val density           = LocalDensity.current
    val keyboardHeight    = WindowInsets.ime.getBottom(density)
    val isKeyboardVisible = keyboardHeight > 100
    val hasOutput         = outputText.isNotEmpty()

    var showBottomSheet by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val context         = LocalContext.current

    val snackbarHostState  = remember { SnackbarHostState() }
    val undoLabel          = stringResource(R.string.undo)
    val leetDeletedMessage = stringResource(R.string.leet_deleted_snackbar)

    LaunchedEffect(pendingDelete) {
        val pending = pendingDelete ?: return@LaunchedEffect
        val result  = snackbarHostState.showSnackbar(
            message     = leetDeletedMessage,
            actionLabel = undoLabel,
            duration    = SnackbarDuration.Short
        )
        when (result) {
            SnackbarResult.ActionPerformed -> viewModel.handleIntent(MainIntent.UndoDeleteLeet)
            SnackbarResult.Dismissed       -> viewModel.clearPendingDelete()
        }
    }

    val inputTitle = if (isReverseMode) {
        stringResource(R.string.input_prefix) + currentModeDisplayName
    } else {
        stringResource(R.string.input_plaintext)
    }
    val outputTitle = if (isReverseMode) {
        stringResource(R.string.output_plaintext)
    } else {
        stringResource(R.string.output_prefix) + currentModeDisplayName
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.app_name),
                            style = com.beigel.leetSpeak_Generator.ui.theme.WordmarkStyle,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (isReverseMode) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) {
                                Text(
                                    text       = stringResource(R.string.reverse_badge),
                                    modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color      = MaterialTheme.colorScheme.onTertiary,
                                    fontSize   = 12.sp,
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
                                    text       = stringResource(R.string.leet_detected_badge),
                                    modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color      = MaterialTheme.colorScheme.onSecondary,
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = onKofiSupport) {
                        Icon(
                            imageVector        = Icons.Default.LocalCafe,
                            contentDescription = stringResource(R.string.kofi_support),
                            modifier           = Modifier.size(24.dp),
                            tint               = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Box {
                        IconButton(onClick = { showDropdownMenu = true }) {
                            Icon(
                                imageVector        = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.menu),
                                modifier           = Modifier.size(24.dp)
                            )
                        }
                        DropdownMenu(
                            expanded         = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Icon(Icons.Default.BugReport, null, modifier = Modifier.size(20.dp))
                                        Text(stringResource(R.string.bug_report))
                                    }
                                },
                                onClick = { showDropdownMenu = false; onBugReport() }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Icon(Icons.Default.Lightbulb, null, modifier = Modifier.size(20.dp))
                                        Text(stringResource(R.string.feedback_title))
                                    }
                                },
                                onClick = { showDropdownMenu = false; onFeedback() }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Icon(Icons.Default.Info, null, modifier = Modifier.size(20.dp))
                                        Text(stringResource(R.string.about))
                                    }
                                },
                                onClick = { showDropdownMenu = false; showAboutDialog = true }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Icon(Icons.Default.Settings, null, modifier = Modifier.size(20.dp))
                                        Text(stringResource(R.string.settings))
                                    }
                                },
                                onClick = { showDropdownMenu = false; onOpenSettings() }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .then(
                        if (isKeyboardVisible) Modifier.offset(y = 56.dp) else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                BottomPillNav(
                    currentMode = currentModeDisplayName,
                    isReverseMode = isReverseMode,
                    onSpeechResult = { viewModel.updateInputText(it) },
                    onModeSelectorClick = { showBottomSheet = true },
                    onSettingsClick = onOpenSettings
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InputCard(
                inputText     = inputText,
                onInputChange = { viewModel.updateInputText(it) },
                onClearText   = { viewModel.clearInput() },
                showHeader    = true,
                isReverseMode = isReverseMode,
                title         = inputTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            SwapButton(
                onClick = { viewModel.handleIntent(MainIntent.ToggleReverseMode) },
                modifier = Modifier.padding(vertical = 10.dp)
            )

            if (hasOutput) {
                OutputCard(
                    outputText   = outputText,
                    currentMode  = outputTitle,
                    animationKey = currentMode,  // NEU: nur bei Moduswechsel animieren
                    onCopyClick  = {
                        onCopyToClipboard(outputText)
                        viewModel.handleIntent(MainIntent.CopyToClipboard)
                    },
                    showHeader    = true,
                    isReverseMode = isReverseMode,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }

        if (showBottomSheet) {
            ModiScreen(
                viewModel = viewModel,
                onDismiss = { showBottomSheet = false },
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
            )
        }
        if (showAboutDialog) {
            AboutDialog(onDismiss = { showAboutDialog = false })
        }
        if (showClearInputDialog) {
            ClearInputDialog(
                onDismiss = { viewModel.dismissClearInputDialog() },
                onConfirm = { shouldClear, dontAskAgain ->
                    viewModel.confirmClearInput(shouldClear, dontAskAgain)
                },
                isReverseMode = isReverseMode
            )
        }

        HandleUiState(uiState, viewModel, context)
    }
}

/**
 * Kleiner runder Swap-Button zwischen Input- und Output-Karte (Redesign v4).
 * Ersetzt die alte "EnhancedAnimatedArrows"-Leiste in der Bottom-Bar.
 */
@Composable
private fun SwapButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val desc = stringResource(R.string.a11y_reverse_mode_inactive)
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(34.dp)
            .semantics {
                contentDescription = desc
                role = Role.Button
            },
        shape = androidx.compose.foundation.shape.CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 3.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Schwebende Pill-Bottom-Nav (Redesign v4): Mic-Toggle, Modus-Segment (Punkt + Name + Chevron),
 * Settings-Icon. Ersetzt die alte volle BottomAppBar.
 */
@Composable
private fun BottomPillNav(
    currentMode: String,
    isReverseMode: Boolean,
    onSpeechResult: (String) -> Unit,
    onModeSelectorClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    val speechManager = remember { com.beigel.leetSpeak_Generator.utils.SpeechInputManager(context) }

    val errorMessages = com.beigel.leetSpeak_Generator.utils.SpeechInputManager.SpeechErrorMessages(
        unavailable = stringResource(R.string.speech_error_unavailable),
        noMatch     = stringResource(R.string.speech_error_no_match),
        timeout     = stringResource(R.string.speech_error_timeout),
        audio       = stringResource(R.string.speech_error_audio),
        generic     = stringResource(R.string.speech_error_generic)
    )

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            speechManager.startListening(
                onResult = onSpeechResult,
                onError = { },
                onStateChange = { isListening = it },
                errorMessages = errorMessages
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose { speechManager.stop() }
    }

    Surface(
        modifier = modifier,
        shape = com.beigel.leetSpeak_Generator.ui.theme.PillShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mic-Toggle
            Surface(
                onClick = {
                    if (isReverseMode) return@Surface
                    if (isListening) {
                        speechManager.stop()
                        isListening = false
                    } else {
                        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier.size(44.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (isListening)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = stringResource(
                            if (isListening) R.string.speech_input_stop else R.string.speech_input_start
                        ),
                        modifier = Modifier.size(20.dp),
                        tint = if (isListening)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Modus-Segment: Punkt + Name + Chevron
            val modeDesc = stringResource(R.string.a11y_mode_selector, currentMode)
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .semantics {
                        contentDescription = modeDesc
                        role = Role.Button
                    }
                    .clickable(onClick = onModeSelectorClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentMode,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Settings
            Surface(
                onClick = onSettingsClick,
                modifier = Modifier.size(44.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = androidx.compose.ui.graphics.Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
            android.widget.Toast.makeText(context, "✅ $message", android.widget.Toast.LENGTH_SHORT).show()
            viewModel.handleIntent(MainIntent.ClearSuccess)
        }
    }
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            android.widget.Toast.makeText(context, "❌ $message", android.widget.Toast.LENGTH_LONG).show()
            viewModel.handleIntent(MainIntent.ClearError)
        }
    }
}