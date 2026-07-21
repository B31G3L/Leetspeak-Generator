package com.beigel.leetSpeak_Generator.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Steuert, ob das Onboarding gezeigt wird — nicht mehr nur "einmal beim ersten
 * Start", sondern versionsbasiert: [CURRENT_ONBOARDING_VERSION] wird bei jedem
 * Update mit onboarding-würdigen neuen Features hochgezählt. Wer eine ältere
 * (oder gar keine) Version gesehen hat, bekommt das Onboarding erneut gezeigt
 * — sowohl komplette Neuinstallationen als auch bestehende Nutzer nach einem
 * Update.
 */
class OnboardingPreferences(private val context: Context) {

    companion object {
        private val LAST_SEEN_VERSION_KEY = intPreferencesKey("onboarding_last_seen_version")

        /**
         * Bei jedem Update mit neuen, onboarding-würdigen Features hochzählen
         * (z.B. Leetspeak-Tastatur, Verlauf, Teilen, Widget -> Version 2).
         * Dadurch sehen auch Bestandsnutzer das Onboarding erneut, nicht nur
         * Neuinstallationen.
         */
        const val CURRENT_ONBOARDING_VERSION = 2
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        (preferences[LAST_SEEN_VERSION_KEY] ?: 0) >= CURRENT_ONBOARDING_VERSION
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[LAST_SEEN_VERSION_KEY] = CURRENT_ONBOARDING_VERSION
        }
    }

    // Für Testing
    suspend fun resetOnboarding() {
        context.dataStore.edit { preferences ->
            preferences.remove(LAST_SEEN_VERSION_KEY)
        }
    }
}