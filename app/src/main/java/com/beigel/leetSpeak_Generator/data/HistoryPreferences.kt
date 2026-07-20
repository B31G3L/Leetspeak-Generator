package com.beigel.leetSpeak_Generator.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persistiert die letzten Übersetzungen (Verlauf) im selben DataStore wie die
 * übrigen App-Einstellungen (siehe ThemePreferences), als JSON-Liste via Gson —
 * analog zum bestehenden Muster in LeetManager.
 */
class HistoryPreferences(private val context: Context) {

    companion object {
        private val HISTORY_KEY = stringPreferencesKey("translation_history")

        /** Maximale Anzahl gespeicherter Verlaufseinträge ("letzte X Übersetzungen"). */
        const val MAX_HISTORY_SIZE = 20
    }

    private val gson = Gson()
    private val listType = object : TypeToken<List<HistoryEntry>>() {}.type

    val history: Flow<List<HistoryEntry>> = context.dataStore.data.map { prefs ->
        parse(prefs[HISTORY_KEY])
    }

    private fun parse(json: String?): List<HistoryEntry> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            gson.fromJson<List<HistoryEntry>>(json, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Fügt einen neuen Eintrag ganz vorne ein. Ein inhaltlich identischer,
     * bereits vorhandener Eintrag (gleicher Input + gleicher Modus) wird dabei
     * entfernt, statt dupliziert, und wandert stattdessen nach oben.
     */
    suspend fun addEntry(entry: HistoryEntry) {
        context.dataStore.edit { prefs ->
            val current = parse(prefs[HISTORY_KEY])
            val filtered = current.filterNot {
                it.inputText == entry.inputText &&
                    it.mode == entry.mode &&
                    it.customLeetIndex == entry.customLeetIndex &&
                    it.isReverseMode == entry.isReverseMode
            }
            val updated = (listOf(entry) + filtered).take(MAX_HISTORY_SIZE)
            prefs[HISTORY_KEY] = gson.toJson(updated)
        }
    }

    suspend fun removeEntry(id: String) {
        context.dataStore.edit { prefs ->
            val current = parse(prefs[HISTORY_KEY])
            prefs[HISTORY_KEY] = gson.toJson(current.filterNot { it.id == id })
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(HISTORY_KEY)
        }
    }
}
