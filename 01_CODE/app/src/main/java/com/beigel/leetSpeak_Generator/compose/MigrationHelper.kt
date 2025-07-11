
package com.beigel.leetSpeak_Generator.compose

import android.content.Context
import android.content.Intent
import com.beigel.leetSpeak_Generator.MainActivity

object MigrationHelper {

    /**
     * Entscheidet welche Activity gestartet werden soll
     * Ermöglicht A/B Testing zwischen XML und Compose
     */
    fun shouldUseCompose(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("use_compose_ui", true) // Default: Compose
    }

    /**
     * Startet die entsprechende Activity
     */
    fun startMainActivity(context: Context) {
        val intent = if (shouldUseCompose(context)) {
            Intent(context, ComposeMainActivity::class.java)
        } else {
            Intent(context, MainActivity::class.java)
        }
        context.startActivity(intent)
    }

    /**
     * Toggle zwischen Compose und XML (für Testing)
     */
    fun toggleUI(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val currentlyCompose = prefs.getBoolean("use_compose_ui", true)
        prefs.edit().putBoolean("use_compose_ui", !currentlyCompose).apply()
    }
}