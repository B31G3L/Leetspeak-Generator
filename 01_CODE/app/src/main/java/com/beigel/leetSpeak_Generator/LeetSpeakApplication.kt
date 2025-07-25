package com.beigel.leetSpeak_Generator

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class mit Hilt Setup für die Haupt-App
 *
 * FIXED: Hilt funktioniert für die Haupt-App, aber nicht für InputMethodService
 * Der Keyboard Service wird separat ohne Hilt initialisiert
 */
@HiltAndroidApp
class LeetSpeakApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            // Haupt-App Initialisierung mit Hilt
            initializeMainApp()
        } catch (e: Exception) {
            android.util.Log.e("LeetSpeakApp", "Error initializing main app", e)
        }
    }

    private fun initializeMainApp() {
        // Hier können weitere Haupt-App Initialisierungen erfolgen
        // z.B. Crash-Reporting, Analytics, etc.
        android.util.Log.d("LeetSpeakApp", "Main app initialized successfully")
    }
}