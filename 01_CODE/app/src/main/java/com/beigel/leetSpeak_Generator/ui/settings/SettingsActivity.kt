// SettingsActivity.kt
package com.beigel.leetSpeak_Generator.ui.settings

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
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
import java.util.Locale

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() { // ← Bereits AppCompatActivity

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

            LeetspeakGeneratorTheme(darkTheme = isDarkTheme) {
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

    // ✅ VERBESSERTE Sprachänderungs-Logik
    private fun applyLanguageChange(languageCode: String) {
        try {
            val localeList = if (languageCode == ThemePreferences.LANGUAGE_SYSTEM) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(languageCode)
            }

            // Verwende AppCompatDelegate für alle Android-Versionen
            AppCompatDelegate.setApplicationLocales(localeList)

            // Für API < 33 ist manchmal ein manueller recreate() nötig
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                // Kurze Verzögerung für bessere UX
                window.decorView.postDelayed({
                    recreate()
                }, 100)
            }
            // Für API 33+ passiert recreation automatisch

        } catch (e: Exception) {
            android.util.Log.e("SettingsActivity", "Error changing language", e)
            // Fallback zu recreation
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
    val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val defaultViewExpanded by viewModel.defaultViewExpanded.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language Selection
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_language),
                    icon = Icons.Default.Language
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
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_color_theme),
                    icon = Icons.Default.ColorLens
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
            // Theme Selection
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_appearance),
                    icon = Icons.Default.Palette
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

            // View Settings
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_view),
                    icon = Icons.Default.ViewModule
                ) {
                    ViewSelector(
                        defaultViewExpanded = defaultViewExpanded,
                        onViewSelected = { expanded ->
                            scope.launch {
                                viewModel.setDefaultViewExpanded(expanded)
                            }
                        }
                    )
                }
            }

            // Widget Settings
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_widget),
                    icon = Icons.Default.Widgets
                ) {
                    WidgetSettings(viewModel = viewModel)
                }
            }

            // About Section
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_about_app),
                    icon = Icons.Default.Info
                ) {
                    AboutSection()
                }
            }
        }
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

    Column {
        languages.forEach { language ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = currentLanguage == language.key,
                        onClick = { onLanguageSelected(language.key) }
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
fun SettingsSection(
    title: String,
    icon: ImageVector,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            content()
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

    Column {
        themes.forEach { theme ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = currentTheme == theme.key,
                        onClick = { onThemeSelected(theme.key) }
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
fun ViewSelector(
    defaultViewExpanded: Boolean,
    onViewSelected: (Boolean) -> Unit
) {
    val viewOptions = listOf(
        ViewOption(
            key = false,
            name = stringResource(R.string.settings_view_grid),
            description = stringResource(R.string.settings_view_grid_desc),
            icon = Icons.Default.GridView
        ),
        ViewOption(
            key = true,
            name = stringResource(R.string.settings_view_list),
            description = stringResource(R.string.settings_view_list_desc),
            icon = Icons.AutoMirrored.Filled.List
        )
    )

    Column {
        Text(
            text = stringResource(R.string.settings_view_default),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        viewOptions.forEach { viewOption ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = defaultViewExpanded == viewOption.key,
                        onClick = { onViewSelected(viewOption.key) }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = defaultViewExpanded == viewOption.key,
                    onClick = { onViewSelected(viewOption.key) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.secondary
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = viewOption.icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = viewOption.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = viewOption.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun WidgetSettings(viewModel: SettingsViewModel) {
    val favoriteLeet by viewModel.favoriteLeet.collectAsStateWithLifecycle()

    Column {
        Text(
            text = stringResource(R.string.settings_widget_config),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_widget_current_favorite),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = favoriteLeet ?: stringResource(R.string.settings_widget_no_favorite),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.settings_widget_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AboutSection() {
    Column {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.about_dialog_version),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.settings_about_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
// Füge diese Composables zu deiner SettingsActivity.kt hinzu

@Composable
fun AppThemeSelector(
    currentAppTheme: AppTheme,
    onAppThemeSelected: (AppTheme) -> Unit
) {
    val themeOptions = listOf(
        ThemeColorOption(
            theme = AppTheme.PLANIT,
            name = "Ocean",
            description = "Vibrant teal colors (Default)",
            primaryColor = Color(0xFF00A896),
            secondaryColor = Color(0xFF536360)
        ),
        ThemeColorOption(
            theme = AppTheme.NEXTIME,
            name = "Sunset",
            description = "Warm orange tones",
            primaryColor = Color(0xFFFF9800),
            secondaryColor = Color(0xFF934B00)
        ),
        ThemeColorOption(
            theme = AppTheme.LEETSPEAK,
            name = "Purple Haze",
            description = "Deep violet theme",
            primaryColor = Color(0xFF673AB7),
            secondaryColor = Color(0xFF804FB3)
        ),
        ThemeColorOption(
            theme = AppTheme.DAILYLIST,
            name = "Nature",
            description = "Fresh green colors",
            primaryColor = Color(0xFFA5D63E),
            secondaryColor = Color(0xFF558B2F)
        ),
        ThemeColorOption(
            theme = AppTheme.UNKNOWN,
            name = "Alert",
            description = "Bold red accent",
            primaryColor = Color(0xFFD32F2F),
            secondaryColor = Color(0xFFC62828)
        )
    )

    Column {
        Text(
            text = "Color Theme",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        themeOptions.forEach { option ->
            ThemeColorCard(
                option = option,
                isSelected = currentAppTheme == option.theme,
                onSelected = { onAppThemeSelected(option.theme) }
            )

            if (option != themeOptions.last()) {
                Spacer(modifier = Modifier.height(8.dp))
            }
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() },
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color Preview
            Row(
                modifier = Modifier.size(48.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = option.primaryColor,
                    shape = RoundedCornerShape(8.dp)
                ) {}

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = option.secondaryColor,
                    shape = RoundedCornerShape(8.dp)
                ) {}
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Theme Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.name,
                    style = MaterialTheme.typography.bodyLarge,
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
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    color = Color.Transparent
                ) {}
            }
        }
    }
}

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

data class ViewOption(
    val key: Boolean,
    val name: String,
    val description: String,
    val icon: ImageVector
)