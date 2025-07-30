package com.beigel.leetSpeak_Generator

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class mit Hilt Setup für die Haupt-App
 *
 * UPDATED: Locale-Initialization hinzugefügt für bessere Sprachunterstützung
 */
@HiltAndroidApp
class LeetSpeakApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            // Haupt-App Initialisierung mit Hilt
            initializeMainApp()

            // NEU: Locale-Initialization für konsistente Sprachunterstützung
            initializeLocale()
        } catch (e: Exception) {
            android.util.Log.e("LeetSpeakApp", "Error initializing main app", e)
        }
    }

    private fun initializeMainApp() {
        // Hier können weitere Haupt-App Initialisierungen erfolgen
        // z.B. Crash-Reporting, Analytics, etc.
        android.util.Log.d("LeetSpeakApp", "Main app initialized successfully")
    }

    /**
     * NEU: Locale-Initialization für konsistente Sprachunterstützung
     *
     * Diese Methode stellt sicher, dass:
     * - Für API 33+ die App-Locale automatisch aus System-Settings geladen wird
     * - Für API < 33 die App-Locale aus AppCompatDelegate-Storage geladen wird
     * - Die App beim Start die korrekte Sprache verwendet
     */
    private fun initializeLocale() {
        try {
            // Aktuelle App-Locales abrufen
            val currentLocales = AppCompatDelegate.getApplicationLocales()

            if (currentLocales.isEmpty) {
                android.util.Log.d("LeetSpeakApp", "Using system default locale")
            } else {
                android.util.Log.d("LeetSpeakApp", "Current app locales: $currentLocales")

                // Optional: Locale-spezifische Initialisierungen hier
                val primaryLocale = currentLocales[0]
                primaryLocale?.let { locale ->
                    android.util.Log.d("LeetSpeakApp", "Primary app locale: ${locale.displayName}")

                    // Hier könnten locale-spezifische Konfigurationen erfolgen
                    // z.B. RTL-Support, Number-Formatting, Date-Formatting, etc.
                    configureLocaleSpecificSettings(locale.language)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("LeetSpeakApp", "Error initializing locale", e)
        }
    }

    /**
     * Konfiguriert sprachspezifische Einstellungen
     */
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