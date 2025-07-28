package com.beigel.leetSpeak_Generator.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 🌞 LIGHT THEME - Warme helle Grautöne mit eleganten Akzenten
private val LightColors = lightColorScheme(
    // 🔵 PRIMARY (Plaintext-Modus) - Warmes Teal/Blaugrün
    primary = Color(0xFF0D9488),           // Teal-600 - für Plaintext
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCCFBF1),  // Teal-100 - heller Container
    onPrimaryContainer = Color(0xFF134E4A), // Teal-900

    // 🟠 SECONDARY (Leet-Modus) - Warmes Amber/Orange
    secondary = Color(0xFFD97706),         // Amber-600 - für Leet-Text
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFEF3C7), // Amber-100 - heller Container
    onSecondaryContainer = Color(0xFF92400E), // Amber-800

    // 🔴 TERTIARY - Akzent für Reverse-Modus
    tertiary = Color(0xFFDC2626),          // Red-600 - für Reverse-Modus
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFEE2E2), // Red-100
    onTertiaryContainer = Color(0xFF7F1D1D), // Red-900

    // 🎨 HINTERGRÜNDE - Warme Grautöne
    background = Color(0xFFFAFAFA),        // Sehr helles Warmgrau
    onBackground = Color(0xFF1F2937),      // Gray-800 - Haupttext

    surface = Color(0xFFFFFFFF),           // Weiß für Cards
    onSurface = Color(0xFF374151),         // Gray-700 - Card-Text
    surfaceVariant = Color(0xFFF3F4F6),    // Gray-100 - subtile Backgrounds
    onSurfaceVariant = Color(0xFF6B7280),  // Gray-500 - Secondary Text

    // 🖼️ OUTLINES & BORDERS
    outline = Color(0xFFD1D5DB),           // Gray-300 - Borders
    outlineVariant = Color(0xFFE5E7EB),    // Gray-200 - subtile Borders

    // ❌ ERROR
    error = Color(0xFFEF4444),             // Red-500
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),    // Red-100
    onErrorContainer = Color(0xFF7F1D1D)   // Red-900
)

// 🌙 DARK THEME - Warme dunkle Grautöne mit leuchtenden Akzenten
private val DarkColors = darkColorScheme(
    // 🔵 PRIMARY (Plaintext-Modus) - Helles Teal
    primary = Color(0xFF5EEAD4),           // Teal-300 - für Plaintext
    onPrimary = Color(0xFF134E4A),         // Teal-900
    primaryContainer = Color(0xFF0F766E),  // Teal-700 - dunkler Container
    onPrimaryContainer = Color(0xFFCCFBF1), // Teal-100

    // 🟠 SECONDARY (Leet-Modus) - Helles Amber
    secondary = Color(0xFFFBBF24),         // Amber-400 - für Leet-Text
    onSecondary = Color(0xFF92400E),       // Amber-800
    secondaryContainer = Color(0xFFB45309), // Amber-700 - dunkler Container
    onSecondaryContainer = Color(0xFFFEF3C7), // Amber-100

    // 🔴 TERTIARY - Helles Rot für Reverse-Modus
    tertiary = Color(0xFFF87171),          // Red-400
    onTertiary = Color(0xFF7F1D1D),        // Red-900
    tertiaryContainer = Color(0xFFB91C1C), // Red-600
    onTertiaryContainer = Color(0xFFFEE2E2), // Red-100

    // 🌙 DUNKLE HINTERGRÜNDE - Warme dunkle Grautöne
    background = Color(0xFF111827),        // Gray-900 - Haupthintergrund
    onBackground = Color(0xFFF9FAFB),      // Gray-50 - Haupttext

    surface = Color(0xFF1F2937),           // Gray-800 - Cards
    onSurface = Color(0xFFE5E7EB),         // Gray-200 - Card-Text
    surfaceVariant = Color(0xFF374151),    // Gray-700 - subtile Surfaces
    onSurfaceVariant = Color(0xFF9CA3AF),  // Gray-400 - Secondary Text

    // 🖼️ DUNKLE OUTLINES
    outline = Color(0xFF4B5563),           // Gray-600 - Borders
    outlineVariant = Color(0xFF6B7280),    // Gray-500 - subtile Borders

    // ❌ DUNKLER ERROR
    error = Color(0xFFF87171),             // Red-400
    onError = Color(0xFF7F1D1D),           // Red-900
    errorContainer = Color(0xFFB91C1C),    // Red-600
    onErrorContainer = Color(0xFFFEE2E2)   // Red-100
)

@Composable
fun LeetspeakGeneratorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color ist deaktiviert für konsistente Farben
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}