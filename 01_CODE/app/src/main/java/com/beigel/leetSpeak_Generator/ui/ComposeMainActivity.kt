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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.ui.components.AboutDialog
import com.beigel.leetSpeak_Generator.ui.components.LeetSelectorBottomSheet
import com.beigel.leetSpeak_Generator.ui.theme.LeetspeakGeneratorTheme
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.beigel.leetSpeak_Generator.presentation.intent.MainIntent
import com.beigel.leetSpeak_Generator.review.InAppReviewManager
import com.beigel.leetSpeak_Generator.ui.components.ClearInputDialog
import com.beigel.leetSpeak_Generator.ui.components.WhatsNewDialog
import com.beigel.leetSpeak_Generator.ui.components.input.InputCard
import com.beigel.leetSpeak_Generator.ui.settings.SettingsActivity
import com.beigel.leetSpeak_Generator.ui.components.output.OutputCard
import javax.inject.Inject

@AndroidEntryPoint
class ComposeMainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var vibrator: Vibrator

    @Inject
    lateinit var inAppReviewManager: InAppReviewManager


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
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()

            val isDarkTheme = when (themeMode) {
                com.beigel.leetSpeak_Generator.data.ThemePreferences.THEME_DARK -> true
                com.beigel.leetSpeak_Generator.data.ThemePreferences.THEME_LIGHT -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            LeetspeakGeneratorTheme(
                darkTheme = isDarkTheme,
                appTheme = appTheme
            ) {
                val shouldRequestReview by viewModel.shouldRequestReview.collectAsStateWithLifecycle()

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
                    },
                    onBugReport = { sendBugReport() },
                    onKofiSupport = { openKofiLink() }
                )
                LaunchedEffect(shouldRequestReview) {
                    if (shouldRequestReview) {
                        // Kurze Verzögerung für bessere UX
                        kotlinx.coroutines.delay(1000)

                        // Review Dialog anzeigen
                        val success = inAppReviewManager.requestReview(this@ComposeMainActivity)

                        if (success) {
                            android.util.Log.d("Review", "✅ Review dialog shown successfully")
                        } else {
                            android.util.Log.w("Review", "⚠️ Review dialog failed to show")
                        }

                        // Markiere als behandelt
                        viewModel.onReviewHandled()
                    }
                }
            }
        }
    }

    private fun copyToClipboardWithFeedback(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(getString(R.string.clipboard_label), text)
        clipboard.setPrimaryClip(clip)

        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
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
                android.widget.Toast.makeText(
                    this,
                    getString(R.string.no_email_app),
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                this,
                getString(R.string.bug_report_error_format, e.message),
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    // NEU: Ko-Fi Link öffnen
    private fun openKofiLink() {
        try {
            val kofiUrl = "https://ko-fi.com/beigel" // Deine Ko-Fi URL hier anpassen
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(kofiUrl))
            startActivity(intent)

            // Optional: Dankeschön-Toast
            android.widget.Toast.makeText(
                this,
                getString(R.string.kofi_toast_thanks),
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                this,
                getString(R.string.kofi_toast_error),
                android.widget.Toast.LENGTH_SHORT
            ).show()
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
    onKofiSupport: () -> Unit = {}
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    // Bestehende State Variables...
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val outputText by viewModel.outputText.collectAsStateWithLifecycle()
    val currentModeDisplayName by viewModel.currentModeDisplayName.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isReverseMode by viewModel.isReverseMode.collectAsStateWithLifecycle()
    val isInputLikelyLeetspeak by viewModel.isInputLikelyLeetspeak.collectAsStateWithLifecycle()

    // What's New State
    val shouldShowWhatsNew by viewModel.shouldShowWhatsNew.collectAsStateWithLifecycle()
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsStateWithLifecycle()

    // NEU: Clear Input Dialog State
    val showClearInputDialog by viewModel.showClearInputDialog.collectAsStateWithLifecycle()

    // Bestehende lokale State Variables...
    val density = LocalDensity.current
    val keyboardHeight = WindowInsets.ime.getBottom(density)
    val isKeyboardVisible = keyboardHeight > 100
    val hasOutput = outputText.isNotEmpty()

    var showBottomSheet by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.app_name),
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
                                    text = stringResource(R.string.reverse_badge),
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
                                    text = stringResource(R.string.leet_detected_badge),
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
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                // Ersetze den actions Block in der TopBar mit diesem Code:
                actions = {
                    // Ko-Fi Support Button (bleibt separat)
                    IconButton(onClick = onKofiSupport) {
                        Icon(
                            imageVector = Icons.Default.LocalCafe,
                            contentDescription = stringResource(R.string.kofi_support),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Dropdown Menu Button
                    Box {
                        IconButton(onClick = { showDropdownMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.menu),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false }
                        ) {
                            // Bug Report Option
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BugReport,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(stringResource(R.string.bug_report))
                                    }
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    onBugReport()
                                }
                            )

                            // About/Info Option
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(stringResource(R.string.about))
                                    }
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    showAboutDialog = true
                                }
                            )

                            // Settings Option
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(stringResource(R.string.settings))
                                    }
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    onOpenSettings()
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
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
                    onCopyClick = {
                        // Zuerst in Clipboard kopieren
                        onCopyToClipboard(outputText)
                        // DANN ViewModel-Intent triggern für Löschlogik
                        viewModel.handleIntent(MainIntent.CopyToClipboard)
                    },                    showHeader = true,
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

        // What's New Dialog
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

        // NEU: Clear Input Dialog - HIER KOMMT ES HIN!
        if (showClearInputDialog) {
            ClearInputDialog(
                onDismiss = { viewModel.dismissClearInputDialog() },
                onConfirm = { dontAskAgain ->
                    viewModel.confirmClearInput(dontAskAgain)
                },
                isReverseMode = isReverseMode
            )
        }

        HandleUiState(uiState, viewModel, context)
    }
}

// Rest der Datei bleibt unverändert...
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
            Icon(
                imageVector = Icons.Default.Transform,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

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
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isReverseMode) {
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

            Text(
                text = when {
                    isReverseMode -> stringResource(R.string.mode_display_reverse)
                    isInputLikelyLeetspeak -> stringResource(R.string.mode_display_leet_detected)
                    else -> stringResource(R.string.mode_display_mode)
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