package com.beigel.leetSpeak_Generator.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Custom Theme Varianten für verschiedene App-Styles
 */
enum class AppTheme {
    PLANIT,      // Standard Ocean Theme (aktuell verwendet)
    NEXTIME,     // Orange/Warm Theme
    LEETSPEAK,   // Purple/Violet Theme
    DAILYLIST,   // Green/Lime Theme
    UNKNOWN      // Red/Warning Theme
}

/**
 * PLANIT Theme (Ocean) - Standard
 */
val PlanitLightTheme = lightColorScheme(
    primary = Color(0xFF00A896),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF88FBE9),
    onPrimaryContainer = Color(0xFF00211D),
    secondary = Color(0xFF536360),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6E8E4),
    onSecondaryContainer = Color(0xFF101F1D),
    tertiary = Color(0xFF5062F0),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE0E0FF),
    onTertiaryContainer = Color(0xFF070068),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF191C1C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFFDAE5E2),
    onSurfaceVariant = Color(0xFF3F4947),
    outline = Color(0xFF6F7977),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

val PlanitDarkTheme = darkColorScheme(
    primary = Color(0xFF4DBCC6),
    onPrimary = Color(0xFF003730),
    primaryContainer = Color(0xFF005147),
    onPrimaryContainer = Color(0xFF88FBE9),
    secondary = Color(0xFFBCCBC8),
    onSecondary = Color(0xFF243230),
    secondaryContainer = Color(0xFF3B4947),
    onSecondaryContainer = Color(0xFFD6E8E4),
    tertiary = Color(0xFFBFC2FF),
    onTertiary = Color(0xFF2130B9),
    tertiaryContainer = Color(0xFF3946D2),
    onTertiaryContainer = Color(0xFFE0E0FF),
    background = Color(0xFF111414),
    onBackground = Color(0xFFE0E3E3),
    surface = Color(0xFF191C1C),
    onSurface = Color(0xFFE0E3E3),
    surfaceVariant = Color(0xFF3F4947),
    onSurfaceVariant = Color(0xFFBFC9C6),
    outline = Color(0xFF899391),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

/**
 * NEXTIME Theme (Orange/Warm)
 */
val NextimeLightTheme = lightColorScheme(
    primary = Color(0xFFFF9800),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDCC0),
    onPrimaryContainer = Color(0xFF331C00),
    secondary = Color(0xFF934B00),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF301300),
    tertiary = Color(0xFFFFC107),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFFFFE082),
    onTertiaryContainer = Color(0xFF201A00),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1E1B16),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E1B16),
    surfaceVariant = Color(0xFFF2E0D0),
    onSurfaceVariant = Color(0xFF4F4539),
    outline = Color(0xFF817568),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

val NextimeDarkTheme = darkColorScheme(
    primary = Color(0xFFFFB763),
    onPrimary = Color(0xFF552D00),
    primaryContainer = Color(0xFF7A4500),
    onPrimaryContainer = Color(0xFFFFDCC0),
    secondary = Color(0xFFFFB78E),
    onSecondary = Color(0xFF512400),
    secondaryContainer = Color(0xFF723500),
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = Color(0xFFE8C547),
    onTertiary = Color(0xFF3D3000),
    tertiaryContainer = Color(0xFF564700),
    onTertiaryContainer = Color(0xFFFFE082),
    background = Color(0xFF16130E),
    onBackground = Color(0xFFE9E1D9),
    surface = Color(0xFF1E1B16),
    onSurface = Color(0xFFE9E1D9),
    surfaceVariant = Color(0xFF4F4539),
    onSurfaceVariant = Color(0xFFD6C4B4),
    outline = Color(0xFF9C8F83),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

/**
 * LEETSPEAK Theme (Purple/Violet) — Redesign v4
 * Farbwerte 1:1 aus dem UI-Redesign-Handoff (design_handoff_leetspeak_redesign/README.md).
 *
 * Mapping auf Material3-Rollen:
 *   bg               -> background
 *   surface          -> surface
 *   surfaceAlt       -> surfaceVariant
 *   primary          -> primary
 *   primaryContainer -> primaryContainer
 *   secondary(accent)-> secondary
 *   secondaryContainer -> secondaryContainer
 *   text             -> onBackground / onSurface
 *   textSub          -> onSurfaceVariant
 *   border           -> outline
 *   borderAccent     -> outlineVariant
 */
val LeetspeakLightTheme = lightColorScheme(
    primary = Color(0xFF6D42E0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE7DEFB),
    onPrimaryContainer = Color(0xFF211D2B),
    secondary = Color(0xFFD1487A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFBE1EA),
    onSecondaryContainer = Color(0xFF211D2B),
    tertiary = Color(0xFFD1487A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFBE1EA),
    onTertiaryContainer = Color(0xFF211D2B),
    background = Color(0xFFF7F5FB),
    onBackground = Color(0xFF211D2B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF211D2B),
    surfaceVariant = Color(0xFFEFEAFA),
    onSurfaceVariant = Color(0xFF71698A),
    outline = Color(0xFFE4DFF0),
    outlineVariant = Color(0xFFF6D3E0),
    error = Color(0xFFD0483F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFBE1EA),
    onErrorContainer = Color(0xFF410002)
)

val LeetspeakDarkTheme = darkColorScheme(
    primary = Color(0xFFB7A4FF),
    onPrimary = Color(0xFF211545),
    primaryContainer = Color(0xFF362A63),
    onPrimaryContainer = Color(0xFFEDE9F5),
    secondary = Color(0xFFFFA0BF),
    onSecondary = Color(0xFF4A2035),
    secondaryContainer = Color(0xFF4A2035),
    onSecondaryContainer = Color(0xFFEDE9F5),
    tertiary = Color(0xFFFFA0BF),
    onTertiary = Color(0xFF4A2035),
    tertiaryContainer = Color(0xFF4A2035),
    onTertiaryContainer = Color(0xFFEDE9F5),
    background = Color(0xFF15131B),
    onBackground = Color(0xFFEDE9F5),
    surface = Color(0xFF1E1B27),
    onSurface = Color(0xFFEDE9F5),
    surfaceVariant = Color(0xFF262233),
    onSurfaceVariant = Color(0xFFA79FBD),
    outline = Color(0xFF322C42),
    outlineVariant = Color(0xFF4A2035),
    error = Color(0xFFF3776E),
    onError = Color(0xFF4A0002),
    errorContainer = Color(0xFF4A2323),
    onErrorContainer = Color(0xFFFFDAD6)
)

/**
 * DAILYLIST Theme (Green/Lime)
 */
val DailylistLightTheme = lightColorScheme(
    primary = Color(0xFFA5D63E),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFFD6F996),
    onPrimaryContainer = Color(0xFF1E3600),
    secondary = Color(0xFF558B2F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6E8C0),
    onSecondaryContainer = Color(0xFF101F00),
    tertiary = Color(0xFF8BC34A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC9F07E),
    onTertiaryContainer = Color(0xFF1D3500),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1C16),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C16),
    surfaceVariant = Color(0xFFE3E4D4),
    onSurfaceVariant = Color(0xFF45483D),
    outline = Color(0xFF76796E),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

val DailylistDarkTheme = darkColorScheme(
    primary = Color(0xFFB5E072),
    onPrimary = Color(0xFF2F4600),
    primaryContainer = Color(0xFF466600),
    onPrimaryContainer = Color(0xFFD6F996),
    secondary = Color(0xFFB3CC9C),
    onSecondary = Color(0xFF263319),
    secondaryContainer = Color(0xFF3C4A2E),
    onSecondaryContainer = Color(0xFFD6E8C0),
    tertiary = Color(0xFFB5E072),
    onTertiary = Color(0xFF2F4600),
    tertiaryContainer = Color(0xFF466600),
    onTertiaryContainer = Color(0xFFC9F07E),
    background = Color(0xFF12140D),
    onBackground = Color(0xFFE3E4D4),
    surface = Color(0xFF1A1C16),
    onSurface = Color(0xFFE3E4D4),
    surfaceVariant = Color(0xFF45483D),
    onSurfaceVariant = Color(0xFFC5C8B8),
    outline = Color(0xFF909387),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

/**
 * UNKNOWN Theme (Red/Warning)
 */
val UnknownLightTheme = lightColorScheme(
    primary = Color(0xFFD32F2F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFFC62828),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF410002),
    tertiary = Color(0xFF00B0FF),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFFBFE9FF),
    onTertiaryContainer = Color(0xFF00334A),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1D1B1C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1D1B1C),
    surfaceVariant = Color(0xFFF5DDDC),
    onSurfaceVariant = Color(0xFF524343),
    outline = Color(0xFF857373),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

val UnknownDarkTheme = darkColorScheme(
    primary = Color(0xFFFFB4A9),
    onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFFFB4A9),
    onSecondary = Color(0xFF690005),
    secondaryContainer = Color(0xFF93000A),
    onSecondaryContainer = Color(0xFFFFDAD6),
    tertiary = Color(0xFF78D7FF),
    onTertiary = Color(0xFF00334A),
    tertiaryContainer = Color(0xFF004B6A),
    onTertiaryContainer = Color(0xFFBFE9FF),
    background = Color(0xFF151212),
    onBackground = Color(0xFFE7E0E1),
    surface = Color(0xFF1D1B1C),
    onSurface = Color(0xFFE7E0E1),
    surfaceVariant = Color(0xFF524343),
    onSurfaceVariant = Color(0xFFD8C2C2),
    outline = Color(0xFFA08C8C),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

/**
 * Helper function zum Abrufen der Theme-ColorSchemes
 */
fun getThemeColorScheme(theme: AppTheme, isDark: Boolean): ColorScheme {
    return when (theme) {
        AppTheme.PLANIT -> if (isDark) PlanitDarkTheme else PlanitLightTheme
        AppTheme.NEXTIME -> if (isDark) NextimeDarkTheme else NextimeLightTheme
        AppTheme.LEETSPEAK -> if (isDark) LeetspeakDarkTheme else LeetspeakLightTheme
        AppTheme.DAILYLIST -> if (isDark) DailylistDarkTheme else DailylistLightTheme
        AppTheme.UNKNOWN -> if (isDark) UnknownDarkTheme else UnknownLightTheme
    }
}