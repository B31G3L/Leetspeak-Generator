package com.beigel.leetSpeak_Generator.data

import android.content.Context
import android.content.pm.PackageManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Verwaltet "What's New" Dialog Präferenzen
 * Tracked App-Versionen um zu wissen, wann der Dialog angezeigt werden soll
 */
class WhatsNewPreferences(private val context: Context) {

    companion object {
        private val LAST_SHOWN_VERSION_CODE_KEY = intPreferencesKey("last_shown_version_code")
        private val LAST_SHOWN_VERSION_NAME_KEY = stringPreferencesKey("last_shown_version_name")
        private val FIRST_LAUNCH_KEY = intPreferencesKey("first_launch_version")
    }

    /**
     * Prüft ob der "What's New" Dialog angezeigt werden soll
     */
    val shouldShowWhatsNew: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val currentVersionCode = getCurrentVersionCode()
        val lastShownVersionCode = preferences[LAST_SHOWN_VERSION_CODE_KEY] ?: 0

        // Dialog anzeigen wenn:
        // 1. Erste Installation (lastShownVersionCode == 0)
        // 2. Version wurde erhöht (currentVersionCode > lastShownVersionCode)
        currentVersionCode > lastShownVersionCode
    }

    /**
     * Aktuelle App-Version Info
     */
    fun getCurrentVersionInfo(): VersionInfo {
        return VersionInfo(
            versionCode = getCurrentVersionCode(),
            versionName = getCurrentVersionName()
        )
    }

    /**
     * Letzte angezeigte Version Info
     */
    val lastShownVersionInfo: Flow<VersionInfo?> = context.dataStore.data.map { preferences ->
        val versionCode = preferences[LAST_SHOWN_VERSION_CODE_KEY]
        val versionName = preferences[LAST_SHOWN_VERSION_NAME_KEY]

        if (versionCode != null && versionName != null) {
            VersionInfo(versionCode, versionName)
        } else null
    }

    /**
     * Markiert die aktuelle Version als "angezeigt"
     */
    suspend fun markCurrentVersionAsShown() {
        val currentVersion = getCurrentVersionInfo()

        context.dataStore.edit { preferences ->
            preferences[LAST_SHOWN_VERSION_CODE_KEY] = currentVersion.versionCode
            preferences[LAST_SHOWN_VERSION_NAME_KEY] = currentVersion.versionName

            // Beim ersten Mal auch als "first launch" markieren
            if (preferences[FIRST_LAUNCH_KEY] == null) {
                preferences[FIRST_LAUNCH_KEY] = currentVersion.versionCode
            }
        }
    }

    /**
     * Prüft ob das die erste Installation ist
     */
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FIRST_LAUNCH_KEY] == null
    }

    /**
     * Reset für Testing (nur in Debug-Builds verwenden)
     */
    suspend fun resetForTesting() {
        context.dataStore.edit { preferences ->
            preferences.remove(LAST_SHOWN_VERSION_CODE_KEY)
            preferences.remove(LAST_SHOWN_VERSION_NAME_KEY)
            preferences.remove(FIRST_LAUNCH_KEY)
        }
    }

    /**
     * Force Show für Testing (setzt die letzte Version niedriger)
     */
    suspend fun forceShowNextTime() {
        context.dataStore.edit { preferences ->
            preferences[LAST_SHOWN_VERSION_CODE_KEY] = getCurrentVersionCode() - 1
        }
    }

    // Private Helper Functions
    private fun getCurrentVersionCode(): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
            packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            1 // Fallback
        }
    }

    private fun getCurrentVersionName(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
            packageInfo.versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0" // Fallback
        }
    }
}

/**
 * Version Information Data Class
 */
data class VersionInfo(
    val versionCode: Int,
    val versionName: String
) {
    /**
     * Vergleicht Versionen (basierend auf versionCode)
     */
    fun isNewerThan(other: VersionInfo?): Boolean {
        return other == null || this.versionCode > other.versionCode
    }

    /**
     * User-friendly Display
     */
    val displayVersion: String
        get() = "Version $versionName"
}