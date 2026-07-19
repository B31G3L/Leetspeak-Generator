package com.beigel.leetSpeak_Generator.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * App Farbschema — Redesign v4
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
val AppLightColorScheme = lightColorScheme(
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

val AppDarkColorScheme = darkColorScheme(
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
 * Eigener Akzentton für den Reverse-Modus (Blau/Türkis) — bewusst getrennt von
 * secondary/tertiary, da beide aktuell dieselbe Pink-Farbe nutzen und der
 * Reverse-Badge sich klar davon abheben soll.
 * Wird für Reverse-Badge UND InputCard verwendet (Türkis).
 */
val ReverseAccentLight = Color(0xFF0E7C93)
val OnReverseAccentLight = Color(0xFFFFFFFF)
val ReverseAccentDark = Color(0xFF6FD5EC)
val OnReverseAccentDark = Color(0xFF00363F)

/**
 * Zweiter Ton derselben "kühlen" Reverse-Familie (Blau statt Türkis) — für die
 * OutputCard im Reverse-Modus. So bleiben Input/Output/Badge als zusammengehörige
 * Gruppe erkennbar, aber Input und Output sind weiterhin unterscheidbar.
 */
val ReverseAccentSecondaryLight = Color(0xFF3E5FC4)
val OnReverseAccentSecondaryLight = Color(0xFFFFFFFF)
val ReverseAccentSecondaryDark = Color(0xFFAAC0FF)
val OnReverseAccentSecondaryDark = Color(0xFF1A2B66)