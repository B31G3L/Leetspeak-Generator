package com.beigel.leetSpeak_Generator

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class mit Hilt Setup
 * Ersetzt die manuelle Dependency Injection
 */
@HiltAndroidApp
class LeetSpeakApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Hier können später weitere Initialisierungen erfolgen
    }
}