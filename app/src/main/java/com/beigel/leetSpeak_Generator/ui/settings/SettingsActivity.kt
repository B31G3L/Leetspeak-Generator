package com.beigel.leetSpeak_Generator.ui.settings

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.ThemePreferences
import com.beigel.leetSpeak_Generator.ui.components.AboutDialog
import com.beigel.leetSpeak_Generator.ui.theme.LeetspeakGeneratorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            val isDarkTheme = when (themeMode) {
                ThemePreferences.THEME_DARK -> true
                ThemePreferences.THEME_LIGHT -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            LeetspeakGeneratorTheme(
                darkTheme = isDarkTheme
            ) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBackPressed = { finish() },
                    onLanguageChanged = { newLanguage ->
                        applyLanguageChange(newLanguage)
                    },
                    onBugReport = { sendBugReport() },
                    onFeedback = { sendFeedback() },
                    onKofiSupport = { openKofiLink() }
                )
            }
        }
    }

    private fun applyLanguageChange(languageCode: String) {
        try {
            val localeList = if (languageCode == ThemePreferences.LANGUAGE_SYSTEM) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(languageCode)
            }

            AppCompatDelegate.setApplicationLocales(localeList)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                window.decorView.postDelayed({
                    recreate()
                }, 100)
            }

        } catch (e: Exception) {
            android.util.Log.e("SettingsActivity", "Error changing language", e)
            recreate()
        }
    }

    // Redesign v4 — "Support"-Sektion: dieselben Aktionen wie zuvor im Overflow-Menü von ComposeMainActivity.
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

            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(getString(R.string.bug_report_email)))
                putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.bug_report_subject, versionName))
                putExtra(android.content.Intent.EXTRA_TEXT, deviceInfo)
            }

            val chooser = android.content.Intent.createChooser(intent, getString(R.string.bug_report_send_chooser))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(chooser)
            } else {
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

            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(getString(R.string.bug_report_email)))
                putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject, versionName))
                putExtra(android.content.Intent.EXTRA_TEXT, body)
            }

            val chooser = android.content.Intent.createChooser(intent, getString(R.string.feedback_send))
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
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(kofiUrl))
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
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackPressed: () -> Unit,
    onLanguageChanged: (String) -> Unit = {},
    onBugReport: () -> Unit = {},
    onFeedback: () -> Unit = {},
    onKofiSupport: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

    // Copy behavior preferences
    val clearInputAfterCopy by viewModel.clearInputAfterCopy.collectAsStateWithLifecycle()
    val askBeforeClear by viewModel.askBeforeClear.collectAsStateWithLifecycle()

    // Review Stats
    val reviewStats by viewModel.reviewStats.collectAsStateWithLifecycle()
    val hapticFeedbackEnabled by viewModel.hapticFeedbackEnabled
        .collectAsStateWithLifecycle()
    // Expanded states für jede Sektion
    var languageExpanded by remember { mutableStateOf(false) }
    var appearanceExpanded by remember { mutableStateOf(false) }
    var copyBehaviorExpanded by remember { mutableStateOf(false) }
    var reviewExpanded by remember { mutableStateOf(false) }
    var aboutExpanded by remember { mutableStateOf(false) }
    var supportExpanded by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var hapticExpanded by remember { mutableStateOf(false) }
    var keyboardExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onBackPressed,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.settings_back),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Language Selection
                item {
                    CollapsibleSettingsSection(
                        title = stringResource(R.string.settings_language),
                        icon = Icons.Default.Language,
                        isExpanded = languageExpanded,
                        onExpandToggle = { languageExpanded = !languageExpanded },
                        preview = getLanguagePreview(language)
                    ) {
                        LanguageSelector(
                            currentLanguage = language,
                            onLanguageSelected = { newLanguage ->
                                scope.launch {
                                    viewModel.setLanguage(newLanguage)
                                    onLanguageChanged(newLanguage)
                                }
                            }
                        )
                    }
                }

                // Theme Selection (Light/Dark)
                item {
                    CollapsibleSettingsSection(
                        title = stringResource(R.string.settings_appearance),
                        icon = Icons.Default.Palette,
                        isExpanded = appearanceExpanded,
                        onExpandToggle = { appearanceExpanded = !appearanceExpanded },
                        preview = getAppearancePreview(themeMode)
                    ) {
                        ThemeSelector(
                            currentTheme = themeMode,
                            onThemeSelected = { theme ->
                                scope.launch {
                                    viewModel.setTheme(theme)
                                }
                            }
                        )
                    }
                }

                // Copy Behavior Section
                item {
                    CollapsibleSettingsSection(
                        title = stringResource(R.string.settings_copy_behavior),
                        icon = Icons.Default.ContentCopy,
                        isExpanded = copyBehaviorExpanded,
                        onExpandToggle = { copyBehaviorExpanded = !copyBehaviorExpanded },
                        preview = if (clearInputAfterCopy) {
                            if (askBeforeClear) stringResource(R.string.copy_behavior_ask_clear) else stringResource(R.string.copy_behavior_auto_clear)
                        } else {
                            stringResource(R.string.copy_behavior_no_clear)
                        }
                    ) {
                        CopyBehaviorSettings(
                            clearInputAfterCopy = clearInputAfterCopy,
                            askBeforeClear = askBeforeClear,
                            onClearInputAfterCopyChanged = { value ->
                                scope.launch {
                                    viewModel.setClearInputAfterCopy(value)
                                }
                            },
                            onAskBeforeClearChanged = { value ->
                                scope.launch {
                                    viewModel.setAskBeforeClear(value)
                                }
                            }
                        )
                    }
                }

                item {
                    CollapsibleSettingsSection(
                        title = stringResource(R.string.settings_keyboard_title),
                        icon = Icons.Default.Keyboard,
                        isExpanded = keyboardExpanded,
                        onExpandToggle = { keyboardExpanded = !keyboardExpanded },
                        preview = stringResource(R.string.settings_keyboard_preview)
                    ) {
                        KeyboardSettings()
                    }
                }

                item {
                    CollapsibleSettingsSection(
                        title           = stringResource(R.string.settings_haptic_title),
                        icon            = Icons.Default.Vibration,
                        isExpanded      = hapticExpanded,
                        onExpandToggle  = { hapticExpanded = !hapticExpanded },
                        preview         = if (hapticFeedbackEnabled)
                            stringResource(R.string.settings_haptic_on)
                        else
                            stringResource(R.string.settings_haptic_off)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text       = stringResource(R.string.settings_haptic_title),
                                    style      = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text  = stringResource(R.string.settings_haptic_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked         = hapticFeedbackEnabled,
                                onCheckedChange = { value ->
                                    scope.launch {
                                        viewModel.setHapticFeedbackEnabled(value)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                // Support Section (Redesign v4)
                item {
                    CollapsibleSettingsSection(
                        title = stringResource(R.string.settings_support),
                        icon = Icons.Default.HelpOutline,
                        isExpanded = supportExpanded,
                        onExpandToggle = { supportExpanded = !supportExpanded },
                        preview = stringResource(R.string.settings_support)
                    ) {
                        SupportSettings(
                            onBugReport = onBugReport,
                            onFeedback = onFeedback,
                            onKofiSupport = onKofiSupport
                        )
                    }
                }

                // About Section
                item {
                    CollapsibleSettingsSection(
                        title = stringResource(R.string.settings_about_app),
                        icon = Icons.Default.Info,
                        isExpanded = aboutExpanded,
                        onExpandToggle = { aboutExpanded = !aboutExpanded },
                        preview = stringResource(R.string.app_name)
                    ) {
                        AboutSection(
                            onReplayOnboarding = {
                                scope.launch {
                                    viewModel.resetOnboarding()
                                    onBackPressed()
                                }
                            },
                            onShowFullAbout = { showAboutDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
private fun SupportSettings(
    onBugReport: () -> Unit,
    onFeedback: () -> Unit,
    onKofiSupport: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        SupportRow(
            icon = Icons.Default.BugReport,
            label = stringResource(R.string.bug_report),
            onClick = onBugReport
        )
        SupportRow(
            icon = Icons.Default.Lightbulb,
            label = stringResource(R.string.feedback_title),
            onClick = onFeedback
        )
        SupportRow(
            icon = Icons.Default.LocalCafe,
            label = stringResource(R.string.kofi_support),
            onClick = onKofiSupport
        )
    }
}

@Composable
private fun SupportRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = false,
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

/**
 * Wiederverwendbare Collapsible Settings Section
 */
@Composable
fun CollapsibleSettingsSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    preview: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header mit Expand/Collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isExpanded) 12.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Preview wenn eingeklappt
                        AnimatedVisibility(
                            visible = !isExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = preview,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                // Expand/Collapse Button
                IconButton(
                    onClick = onExpandToggle,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Content (expandable)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(200))
            ) {
                content()
            }
        }
    }
}

/**
 * Preview Helper Functions
 */
@Composable
fun getLanguagePreview(languageCode: String): String {
    return when (languageCode) {
        ThemePreferences.LANGUAGE_SYSTEM -> stringResource(R.string.language_system)
        ThemePreferences.LANGUAGE_ENGLISH -> stringResource(R.string.language_english)
        ThemePreferences.LANGUAGE_GERMAN -> stringResource(R.string.language_german)
        ThemePreferences.LANGUAGE_SPANISH -> stringResource(R.string.language_spanish)
        ThemePreferences.LANGUAGE_FRENCH -> stringResource(R.string.language_french)
        ThemePreferences.LANGUAGE_ITALIAN -> stringResource(R.string.language_italian)
        else -> stringResource(R.string.language_system)
    }
}

@Composable
fun getAppearancePreview(themeMode: String): String {
    return when (themeMode) {
        ThemePreferences.THEME_SYSTEM -> stringResource(R.string.theme_system)
        ThemePreferences.THEME_LIGHT -> stringResource(R.string.theme_light)
        ThemePreferences.THEME_DARK -> stringResource(R.string.theme_dark)
        else -> stringResource(R.string.theme_system)
    }
}

@Composable
fun LanguageSelector(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf(
        LanguageOption(
            key = ThemePreferences.LANGUAGE_SYSTEM,
            name = stringResource(R.string.language_system),
            description = stringResource(R.string.language_system_desc),
            icon = Icons.Default.Settings,
            flag = "🌐"
        ),
        LanguageOption(
            key = ThemePreferences.LANGUAGE_ENGLISH,
            name = stringResource(R.string.language_english),
            description = stringResource(R.string.language_english_desc),
            icon = Icons.Default.Language,
            flag = "🇺🇸"
        ),
        LanguageOption(
            key = ThemePreferences.LANGUAGE_GERMAN,
            name = stringResource(R.string.language_german),
            description = stringResource(R.string.language_german_desc),
            icon = Icons.Default.Language,
            flag = "🇩🇪"
        ),
        LanguageOption(
            key = ThemePreferences.LANGUAGE_SPANISH,
            name = stringResource(R.string.language_spanish),
            description = stringResource(R.string.language_spanish_desc),
            icon = Icons.Default.Language,
            flag = "🇪🇸"
        ),
        LanguageOption(
            key = ThemePreferences.LANGUAGE_FRENCH,
            name = stringResource(R.string.language_french),
            description = stringResource(R.string.language_french_desc),
            icon = Icons.Default.Language,
            flag = "🇫🇷"
        ),
        LanguageOption(
            key = ThemePreferences.LANGUAGE_ITALIAN,
            name = stringResource(R.string.language_italian),
            description = stringResource(R.string.language_italian_desc),
            icon = Icons.Default.Language,
            flag = "🇮🇹"
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        languages.forEach { language ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = currentLanguage == language.key,
                        onClick = { onLanguageSelected(language.key) },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentLanguage == language.key,
                    onClick = { onLanguageSelected(language.key) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = language.flag,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.width(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = language.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = language.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeSelector(
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf(
        ThemeOption(
            key = ThemePreferences.THEME_SYSTEM,
            name = stringResource(R.string.theme_system),
            description = stringResource(R.string.theme_system_desc),
            icon = Icons.Default.Settings
        ),
        ThemeOption(
            key = ThemePreferences.THEME_LIGHT,
            name = stringResource(R.string.theme_light),
            description = stringResource(R.string.theme_light_desc),
            icon = Icons.Default.LightMode
        ),
        ThemeOption(
            key = ThemePreferences.THEME_DARK,
            name = stringResource(R.string.theme_dark),
            description = stringResource(R.string.theme_dark_desc),
            icon = Icons.Default.DarkMode
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        themes.forEach { theme ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = currentTheme == theme.key,
                        onClick = { onThemeSelected(theme.key) },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentTheme == theme.key,
                    onClick = { onThemeSelected(theme.key) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = theme.icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = theme.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = theme.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AboutSection(
    onReplayOnboarding: () -> Unit = {},
    onShowFullAbout: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(R.string.about_dialog_version),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.settings_about_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        SupportRow(
            icon = Icons.Default.Info,
            label = stringResource(R.string.settings_about_more),
            onClick = onShowFullAbout
        )

        SupportRow(
            icon = Icons.Default.Replay,
            label = stringResource(R.string.settings_replay_onboarding),
            onClick = onReplayOnboarding
        )
    }
}

@Composable
fun KeyboardSettings() {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.settings_keyboard_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(2.dp))

        SupportRow(
            icon = Icons.Default.Settings,
            label = stringResource(R.string.settings_keyboard_enable),
            onClick = {
                context.startActivity(
                    android.content.Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS)
                        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        )

        SupportRow(
            icon = Icons.Default.Keyboard,
            label = stringResource(R.string.settings_keyboard_switch),
            onClick = {
                val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showInputMethodPicker()
            }
        )
    }
}

@Composable
fun CopyBehaviorSettings(
    clearInputAfterCopy: Boolean,
    askBeforeClear: Boolean,
    onClearInputAfterCopyChanged: (Boolean) -> Unit,
    onAskBeforeClearChanged: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Clear Input After Copy Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_copy_clear_title),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.settings_copy_clear_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = clearInputAfterCopy,
                onCheckedChange = onClearInputAfterCopyChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        // Ask Before Clear Switch (nur aktiviert wenn clearInputAfterCopy = true)
        AnimatedVisibility(
            visible = clearInputAfterCopy,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_copy_ask_title),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.settings_copy_ask_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = askBeforeClear,
                    onCheckedChange = onAskBeforeClearChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // Info Text
        if (clearInputAfterCopy && !askBeforeClear) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.copy_auto_clear_info),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// Data Classes
data class LanguageOption(
    val key: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val flag: String
)

data class ThemeOption(
    val key: String,
    val name: String,
    val description: String,
    val icon: ImageVector
)