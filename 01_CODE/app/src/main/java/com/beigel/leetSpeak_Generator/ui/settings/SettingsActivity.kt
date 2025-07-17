package com.beigel.leetSpeak_Generator.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beigel.leetSpeak_Generator.data.ThemePreferences
import com.beigel.leetSpeak_Generator.ui.theme.LeetspeakGeneratorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

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
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück")
                    }
                }
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
            // Theme Selection
            item {
                SettingsSection(
                    title = "Darstellung",
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

            // Widget Settings
            item {
                SettingsSection(
                    title = "Widget",
                    icon = Icons.Default.Widgets
                ) {
                    WidgetSettings(viewModel = viewModel)
                }
            }

            // About Section
            item {
                SettingsSection(
                    title = "Über die App",
                    icon = Icons.Default.Info
                ) {
                    AboutSection()
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
            name = "System",
            description = "Folgt den Systemeinstellungen",
            icon = Icons.Default.Settings
        ),
        ThemeOption(
            key = ThemePreferences.THEME_LIGHT,
            name = "Hell",
            description = "Helles Design",
            icon = Icons.Default.LightMode
        ),
        ThemeOption(
            key = ThemePreferences.THEME_DARK,
            name = "Dunkel",
            description = "Dunkles Design",
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
fun WidgetSettings(viewModel: SettingsViewModel) {
    val favoriteLeet by viewModel.favoriteLeet.collectAsStateWithLifecycle()

    Column {
        Text(
            text = "Widget-Konfiguration",
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
                        text = "Aktueller Favorit:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = favoriteLeet ?: "Kein Favorit ausgewählt",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Das Widget verwendet automatisch den als Favorit markierten Leet-Modus. Ändere den Favoriten in der Hauptapp.",
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
            text = "Leetspeak Generator",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Version 2.0.0",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Konvertiert Text in Leetspeak mit verschiedenen Modi und benutzerdefinierten Übersetzungen.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class ThemeOption(
    val key: String,
    val name: String,
    val description: String,
    val icon: ImageVector
)