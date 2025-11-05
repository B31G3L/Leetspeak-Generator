package com.beigel.leetSpeak_Generator.ui.theme

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat

/**
 * Helper für Edge-to-Edge Display (Android 15+ kompatibel)
 */
object EdgeToEdgeHelper {

    /**
     * Konfiguriert Edge-to-Edge für moderne Android-Versionen
     */
    fun setupEdgeToEdge(
        activity: Activity,
        view: View,
        isDarkTheme: Boolean
    ) {
        // Edge-to-Edge aktivieren
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)

        // System Bars Controller
        val windowInsetsController = WindowCompat.getInsetsController(
            activity.window,
            view
        )

        // Light/Dark Status Bar
        windowInsetsController.isAppearanceLightStatusBars = !isDarkTheme
        windowInsetsController.isAppearanceLightNavigationBars = !isDarkTheme

        // Für Android 15+: Nur Controller verwenden, keine direkten Farben
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // Nur für ältere Versionen die Farben setzen
            @Suppress("DEPRECATION")
            activity.window.statusBarColor = Color.Transparent.toArgb()

            @Suppress("DEPRECATION")
            activity.window.navigationBarColor = Color.Transparent.toArgb()
        }
        // Android 15+ verwendet automatisch transparente System Bars
    }
}