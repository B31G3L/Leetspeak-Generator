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

// Farben aus Ihren XML-Ressourcen
private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),           // primary
    onPrimary = Color(0xFFFFFFFF),         // on_primary
    primaryContainer = Color(0xFF1E40AF),   // primary_variant
    onPrimaryContainer = Color(0xFFFFFFFF),

    secondary = Color(0xFFF59E0B),         // secondary
    onSecondary = Color(0xFFFFFFFF),       // on_secondary
    secondaryContainer = Color(0xFFD97706), // secondary_variant
    onSecondaryContainer = Color(0xFFFFFFFF),

    background = Color(0xFFF8FAFC),        // background
    onBackground = Color(0xFF1E293B),      // on_background
    surface = Color(0xFFFFFFFF),           // surface
    onSurface = Color(0xFF334155),         // on_surface
    surfaceVariant = Color(0xFFE2E8F0),    // gray
    onSurfaceVariant = Color(0xFF64748B),  // text_secondary

    error = Color(0xFFEF4444),             // error
    onError = Color(0xFFFFFFFF),

    outline = Color(0xFF94A3B8),           // text_hint
    outlineVariant = Color(0xFFE2E8F0)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF60A5FA),           // primary (Dark)
    onPrimary = Color(0xFF1E293B),         // on_primary (Dark)
    primaryContainer = Color(0xFF3B82F6),   // primary_variant (Dark)
    onPrimaryContainer = Color(0xFF1E293B),

    secondary = Color(0xFFFBBF24),         // secondary (Dark)
    onSecondary = Color(0xFF1E293B),       // on_secondary (Dark)
    secondaryContainer = Color(0xFFF59E0B), // secondary_variant (Dark)
    onSecondaryContainer = Color(0xFF1E293B),

    background = Color(0xFF0F172A),        // background (Dark)
    onBackground = Color(0xFFF1F5F9),      // on_background (Dark)
    surface = Color(0xFF1E293B),           // surface (Dark)
    onSurface = Color(0xFFE2E8F0),         // on_surface (Dark)
    surfaceVariant = Color(0xFF334155),    // gray_light (Dark)
    onSurfaceVariant = Color(0xFF94A3B8),  // text_secondary (Dark)

    error = Color(0xFFF87171),             // error (Dark)
    onError = Color(0xFF000000),

    outline = Color(0xFF64748B),           // text_hint (Dark)
    outlineVariant = Color(0xFF475569)
)

@Composable
fun LeetspeakGeneratorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Deaktiviert für konsistente Farben
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