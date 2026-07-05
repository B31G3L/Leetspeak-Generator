package com.beigel.leetSpeak_Generator.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OnboardingPreferences(private val context: Context) {

    companion object {
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] ?: false
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = true
        }
    }

    // Für Testing
    suspend fun resetOnboarding() {
        context.dataStore.edit { preferences ->
            preferences.remove(ONBOARDING_COMPLETED_KEY)
        }
    }
}