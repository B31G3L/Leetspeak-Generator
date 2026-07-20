package com.beigel.leetSpeak_Generator.data

import java.util.UUID

/**
 * Ein einzelner Eintrag im Übersetzungsverlauf.
 * Speichert genug Kontext (Modus, ggf. Custom-Leet-Index, Reverse-Flag),
 * um die Übersetzung später 1:1 wiederherstellen zu können.
 */
data class HistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val inputText: String,
    val outputText: String,
    val modeDisplayName: String,
    val mode: Int,                     // LeetManager.MODE_SIMPLE / MODE_EXTENDED / MODE_CUSTOM
    val customLeetIndex: Int = -1,
    val isReverseMode: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
