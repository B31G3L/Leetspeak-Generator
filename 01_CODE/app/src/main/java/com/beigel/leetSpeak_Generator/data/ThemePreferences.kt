
// 1. ThemePreferences.kt
package com.beigel.leetSpeak_Generator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemePreferences(private val context: Context) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_preference")
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: THEME_SYSTEM
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }
}