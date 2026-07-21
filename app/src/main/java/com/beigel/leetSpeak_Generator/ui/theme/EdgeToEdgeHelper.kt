package com.beigel.leetSpeak_Generator.ui.theme

import android.app.Activity
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

/**
 * Helper für Edge-to-Edge Display (Android 15+ kompatibel).
 *
 * Nutzt bewusst die offizielle androidx.activity.enableEdgeToEdge()-API statt
 * manuell WindowCompat + Window.statusBarColor/navigationBarColor zu setzen.
 * Die alten Setter sind ab Android 15 als veraltet markiert — selbst wenn ihr
 * Aufruf per SDK-Versionsabfrage abgesichert ist, taucht das Symbol trotzdem
 * im kompilierten Code auf und wird von Play Console's statischer Analyse
 * ("Nicht mehr unterstützte APIs für randlose Anzeige") angemeckert.
 * enableEdgeToEdge() deckt alle Android-Versionen offiziell und ohne diese
 * veralteten Aufrufe ab.
 */
object EdgeToEdgeHelper {

    /**
     * Konfiguriert Edge-to-Edge für moderne Android-Versionen.
     */
    fun setupEdgeToEdge(
        activity: Activity,
        isDarkTheme: Boolean
    ) {
        val componentActivity = activity as? ComponentActivity ?: return

        // Dunkles Theme -> helle (weiße) Status-/Navigationsleisten-Icons,
        // helles Theme -> dunkle Icons. Scrim transparent, da wir selbst per
        // Insets-Padding in den Screens/der Tastatur für ausreichend Kontrast sorgen.
        val style = if (isDarkTheme) {
            SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        } else {
            SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        }

        componentActivity.enableEdgeToEdge(
            statusBarStyle = style,
            navigationBarStyle = style
        )
    }
}
