package com.beigel.leetSpeak_Generator

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class LeetSpeakApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            initializeMainApp()
            initializeLocale()
        } catch (e: Exception) {
            android.util.Log.e("LeetSpeakApp", "Error initializing main app", e)
        }
    }

    private fun initializeMainApp() {
        android.util.Log.d("LeetSpeakApp", "Main app initialized successfully")
    }

    private fun initializeLocale() {
        try {
            val currentLocales = AppCompatDelegate.getApplicationLocales()

            if (currentLocales.isEmpty) {
                android.util.Log.d("LeetSpeakApp", "Using system default locale")
            } else {
                android.util.Log.d("LeetSpeakApp", "Current app locales: $currentLocales")

                val primaryLocale = currentLocales[0]
                primaryLocale?.let { locale ->
                    android.util.Log.d("LeetSpeakApp", "Primary app locale: ${locale.displayName}")
                    configureLocaleSpecificSettings(locale.language)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("LeetSpeakApp", "Error initializing locale", e)
        }
    }


    private fun configureLocaleSpecificSettings(languageCode: String) {
        when (languageCode) {
            "de" -> {
                // Deutsche spezifische Konfigurationen
                android.util.Log.d("LeetSpeakApp", "Configuring German locale settings")
            }
            "es" -> {
                // Spanische spezifische Konfigurationen
                android.util.Log.d("LeetSpeakApp", "Configuring Spanish locale settings")
            }
            "fr" -> {
                // Französische spezifische Konfigurationen
                android.util.Log.d("LeetSpeakApp", "Configuring French locale settings")
            }
            "it" -> {
                // Italienische spezifische Konfigurationen
                android.util.Log.d("LeetSpeakApp", "Configuring Italian locale settings")
            }
            "en" -> {
                // Englische spezifische Konfigurationen
                android.util.Log.d("LeetSpeakApp", "Configuring English locale settings")
            }
            else -> {
                // Standard/Default Konfigurationen
                android.util.Log.d("LeetSpeakApp", "Using default locale settings for: $languageCode")
            }
        }
    }
}