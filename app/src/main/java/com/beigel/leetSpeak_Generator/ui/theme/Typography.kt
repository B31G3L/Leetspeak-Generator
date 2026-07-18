package com.beigel.leetSpeak_Generator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.beigel.leetSpeak_Generator.R

// Raleway Font Family aus Ihren bestehenden Ressourcen (Überschriften/Wordmark)
val RalewayFontFamily = FontFamily(
    Font(R.font.raleway_regular, FontWeight.Normal),
    Font(R.font.raleway_regular, FontWeight.Medium),
    Font(R.font.raleway_regular, FontWeight.Bold)
)

// Redesign v4 sieht "Inter" für Body/UI-Text vor. Die TTF-Datei liegt (noch) nicht unter
// res/font, daher aktuell Fallback auf die System-Sans (sieht Inter sehr ähnlich).
// -> Um 1:1 Fidelity zu erreichen: Inter-Variable/Static TTFs von Google Fonts unter
//    res/font/inter_regular.ttf, inter_medium.ttf, inter_bold.ttf ablegen und hier
//    per Font(R.font.inter_regular, FontWeight.Normal) usw. einbinden.
val InterFontFamily = FontFamily.Default

// Typography mit Raleway Font (konsistent mit Ihrem bestehenden Design)
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = RalewayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = RalewayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = RalewayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = RalewayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = RalewayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = RalewayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // Onboarding-Headline (23sp Raleway Bold laut Handoff)
    titleLarge = TextStyle(
        fontFamily = RalewayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 23.sp,
        lineHeight = 29.sp,
        letterSpacing = 0.sp
    ),
    // Screen-Titel (21sp Raleway Bold, z.B. "Modi", "Einstellungen")
    titleMedium = TextStyle(
        fontFamily = RalewayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.5.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.6.sp
    )
)

/** Wordmark-Style für "Leetspeak" im Home-Header (Raleway ExtraBold 21sp lt. Handoff). */
val WordmarkStyle = TextStyle(
    fontFamily = RalewayFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 21.sp,
    letterSpacing = 0.sp
)