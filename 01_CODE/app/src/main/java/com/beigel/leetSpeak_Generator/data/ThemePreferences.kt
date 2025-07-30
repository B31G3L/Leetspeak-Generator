package com.beigel.leetSpeak_Generator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemePreferences(private val context: Context) {

    companion object {
        private val THEME_KEY = androidx.datastore.preferences.core.stringPreferencesKey("theme_preference")
        private val DEFAULT_VIEW_EXPANDED_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("default_view_expanded")
        private val LANGUAGE_KEY = androidx.datastore.preferences.core.stringPreferencesKey("language_preference")

        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        // Language constants
        const val LANGUAGE_SYSTEM = "system"
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_GERMAN = "de"
        const val LANGUAGE_SPANISH = "es"
        const val LANGUAGE_FRENCH = "fr"
        const val LANGUAGE_ITALIAN = "it"
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: THEME_SYSTEM
    }

    val defaultViewExpanded: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_VIEW_EXPANDED_KEY] ?: false
    }

    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: LANGUAGE_SYSTEM
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun setDefaultViewExpanded(expanded: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_VIEW_EXPANDED_KEY] = expanded
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }
}