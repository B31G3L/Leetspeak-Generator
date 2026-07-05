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
import com.beigel.leetSpeak_Generator.ui.theme.AppTheme
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
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()

            val isDarkTheme = when (themeMode) {
                ThemePreferences.THEME_DARK -> true
                ThemePreferences.THEME_LIGHT -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            LeetspeakGeneratorTheme(
                darkTheme = isDarkTheme,
                appTheme = appTheme
            ) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBackPressed = { finish() },
                    onLanguageChanged = { newLanguage ->
                        applyLanguageChange(newLanguage)
                    }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackPressed: () -> Unit,
    onLanguageChanged: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity

    val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
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
    var colorThemeExpanded by remember { mutableStateOf(false) }
    var appearanceExpanded by remember { mutableStateOf(false) }
    var copyBehaviorExpanded by remember { mutableStateOf(false) }
    var reviewExpanded by remember { mutableStateOf(false) }
    var aboutExpanded by remember { mutableStateOf(false) }
    var hapticExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.settings_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
            )
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

            // Color Theme Selection
            item {
                CollapsibleSettingsSection(
                    title = stringResource(R.string.settings_color_theme),
                    icon = Icons.Default.ColorLens,
                    isExpanded = colorThemeExpanded,
                    onExpandToggle = { colorThemeExpanded = !colorThemeExpanded },
                    preview = getThemePreview(appTheme)
                ) {
                    AppThemeSelector(
                        currentAppTheme = appTheme,
                        onAppThemeSelected = { theme ->
                            scope.launch {
                                viewModel.setAppTheme(theme)
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
                            }
                        )
                    }
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
                    AboutSection()
                }
            }
        }
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
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
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                        tint = MaterialTheme.colorScheme.primary
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
fun getThemePreview(appTheme: AppTheme): String {
    return when (appTheme) {
        AppTheme.PLANIT -> stringResource(R.string.theme_planit)
        AppTheme.NEXTIME -> stringResource(R.string.theme_nextime)
        AppTheme.LEETSPEAK -> stringResource(R.string.theme_leetspeak)
        AppTheme.DAILYLIST -> stringResource(R.string.theme_dailylist)
        AppTheme.UNKNOWN -> stringResource(R.string.theme_unknown)
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
fun AboutSection() {
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
    }
}

@Composable
fun AppThemeSelector(
    currentAppTheme: AppTheme,
    onAppThemeSelected: (AppTheme) -> Unit
) {
    val themeOptions = listOf(
        ThemeColorOption(
            theme = AppTheme.PLANIT,
            name = stringResource(R.string.theme_planit),
            description = stringResource(R.string.theme_planit_desc),
            primaryColor = Color(0xFF00A896),
            secondaryColor = Color(0xFF536360)
        ),
        ThemeColorOption(
            theme = AppTheme.NEXTIME,
            name = stringResource(R.string.theme_nextime),
            description = stringResource(R.string.theme_nextime_desc),
            primaryColor = Color(0xFFFF9800),
            secondaryColor = Color(0xFF934B00)
        ),
        ThemeColorOption(
            theme = AppTheme.LEETSPEAK,
            name = stringResource(R.string.theme_leetspeak),
            description = stringResource(R.string.theme_leetspeak_desc),
            primaryColor = Color(0xFF673AB7),
            secondaryColor = Color(0xFF804FB3)
        ),
        ThemeColorOption(
            theme = AppTheme.DAILYLIST,
            name = stringResource(R.string.theme_dailylist),
            description = stringResource(R.string.theme_dailylist_desc),
            primaryColor = Color(0xFFA5D63E),
            secondaryColor = Color(0xFF558B2F)
        ),
        ThemeColorOption(
            theme = AppTheme.UNKNOWN,
            name = stringResource(R.string.theme_unknown),
            description = stringResource(R.string.theme_unknown_desc),
            primaryColor = Color(0xFFD32F2F),
            secondaryColor = Color(0xFFC62828)
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        themeOptions.forEach { option ->
            ThemeColorCard(
                option = option,
                isSelected = currentAppTheme == option.theme,
                onSelected = { onAppThemeSelected(option.theme) }
            )
        }
    }
}

@Composable
private fun ThemeColorCard(
    option: ThemeColorOption,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        onClick = onSelected,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color Preview
            Row(
                modifier = Modifier.size(40.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = option.primaryColor,
                    shape = RoundedCornerShape(6.dp)
                ) {}

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = option.secondaryColor,
                    shape = RoundedCornerShape(6.dp)
                ) {}
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Theme Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Selection Indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.selected),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Surface(
                    modifier = Modifier.size(20.dp),
                    shape = CircleShape,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    color = Color.Transparent
                ) {}
            }
        }
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
                onCheckedChange = onClearInputAfterCopyChanged
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
                    onCheckedChange = onAskBeforeClearChanged
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
data class ThemeColorOption(
    val theme: AppTheme,
    val name: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color
)

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