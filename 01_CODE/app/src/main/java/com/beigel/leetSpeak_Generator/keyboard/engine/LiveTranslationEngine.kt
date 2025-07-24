package com.beigel.leetSpeak_Generator.keyboard.engine

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 🔄 LIVE TRANSLATION ENGINE
 *
 * Herzstück der Leetspeak-Tastatur - übersetzt Text in Echtzeit während dem Tippen
 * Features:
 * - Character-by-character translation
 * - Word-level optimization
 * - Context-aware suggestions
 * - Smart capitalization handling
 */
class LiveTranslationEngine {

    // Current Translation State
    private var currentCustomLeet: CustomLeet? = null
    private var currentMode: LeetTranslator.TranslationMode = LeetTranslator.TranslationMode.SIMPLE

    // Translation Cache für Performance
    private val charTranslationCache = mutableMapOf<Pair<Char, LeetTranslator.TranslationMode>, String>()
    private val wordTranslationCache = mutableMapOf<Pair<String, LeetTranslator.TranslationMode>, String>()

    // Statistics & Analytics
    private val _translationStats = MutableStateFlow(TranslationStats())
    val translationStats: StateFlow<TranslationStats> = _translationStats.asStateFlow()

    /**
     * 🔤 Einzelnes Zeichen übersetzen (Hauptfunktion für Live-Typing)
     */
    fun translateChar(char: Char, mode: LeetTranslator.TranslationMode = currentMode): String {
        val cacheKey = char.uppercaseChar() to mode

        // Check cache first für Performance
        charTranslationCache[cacheKey]?.let { return it }

        val translated = when (mode) {
            LeetTranslator.TranslationMode.SIMPLE -> translateCharSimple(char)
            LeetTranslator.TranslationMode.EXTENDED -> translateCharExtended(char)
            LeetTranslator.TranslationMode.CUSTOM -> translateCharCustom(char)
        }

        // Cache result
        charTranslationCache[cacheKey] = translated

        // Update stats
        updateStats(char, translated)

        // Preserve original case if possible
        return preserveCase(char, translated)
    }

    private fun translateCharSimple(char: Char): String {
        return when (char.uppercaseChar()) {
            'A' -> "4"
            'B' -> "8"
            'E' -> "3"
            'G' -> "6"
            'H' -> "#"
            'I' -> "1"
            'L' -> "L"
            'O' -> "0"
            'S' -> "5"
            'T' -> "7"
            'Z' -> "2"
            else -> char.toString()
        }
    }

    private fun translateCharExtended(char: Char): String {
        return when (char.uppercaseChar()) {
            'A' -> "4"
            'B' -> "8"
            'C' -> "("
            'D' -> "|)"
            'E' -> "3"
            'F' -> "|="
            'G' -> "6"
            'H' -> "#"
            'I' -> "!"
            'J' -> "_|"
            'K' -> "|<"
            'L' -> "1"
            'M' -> "/\\/\\"
            'N' -> "|\\|"
            'O' -> "0"
            'P' -> "9"
            'Q' -> "0_"
            'R' -> "2"
            'S' -> "5"
            'T' -> "7"
            'U' -> "|_|"
            'V' -> "\\/"
            'W' -> "\\/\\/"
            'X' -> "><"
            'Y' -> "`/"
            'Z' -> "Z"
            else -> char.toString()
        }
    }

    private fun translateCharCustom(char: Char): String {
        return currentCustomLeet?.getTranslation(char.uppercaseChar().toString())
            ?: translateCharSimple(char) // Fallback to simple
    }

    /**
     * 📝 Ganzes Wort optimiert übersetzen
     */
    fun translateWord(word: String): String {
        if (word.isBlank()) return word

        val cacheKey = word.lowercase() to currentMode
        wordTranslationCache[cacheKey]?.let { return it }

        val translated = when {
            // Spezielle Wort-Optimierungen
            isSpecialWord(word) -> translateSpecialWord(word)

            // Normale Character-by-Character Translation
            else -> word.map { translateChar(it) }.joinToString("")
        }

        // Cache result
        wordTranslationCache[cacheKey] = translated

        return translated
    }

    private fun isSpecialWord(word: String): Boolean {
        val specialWords = setOf(
            "leet", "elite", "hacker", "pwned", "noob", "pro",
            "hello", "world", "the", "and", "you", "are"
        )
        return word.lowercase() in specialWords
    }

    private fun translateSpecialWord(word: String): String {
        return when (word.lowercase()) {
            "leet" -> "1337"
            "elite" -> "31173"
            "hacker" -> "#4(k3r"
            "pwned" -> "pwn3d"
            "noob" -> "n00b"
            "pro" -> "pr0"
            "hello" -> when (currentMode) {
                LeetTranslator.TranslationMode.SIMPLE -> "#3LL0"
                LeetTranslator.TranslationMode.EXTENDED -> "#3110"
                LeetTranslator.TranslationMode.CUSTOM -> translateWord_Custom("hello")
            }
            "world" -> when (currentMode) {
                LeetTranslator.TranslationMode.SIMPLE -> "W0RLD"
                LeetTranslator.TranslationMode.EXTENDED -> "\\/\\/0r1d"
                LeetTranslator.TranslationMode.CUSTOM -> translateWord_Custom("world")
            }
            "the" -> "7#3"
            "and" -> "4nd"
            "you" -> "u"
            "are" -> "r"
            else -> word.map { translateChar(it) }.joinToString("")
        }
    }

    private fun translateWord_Custom(word: String): String {
        return word.map { translateChar(it) }.joinToString("")
    }

    /**
     * 🎯 Case Preservation - versucht Groß-/Kleinschreibung zu erhalten
     */
    private fun preserveCase(original: Char, translated: String): String {
        return when {
            translated.length == 1 && original.isLowerCase() -> translated.lowercase()
            translated.length == 1 && original.isUpperCase() -> translated.uppercase()
            else -> translated // Multi-character translations bleiben unverändert
        }
    }

    /**
     * 🔄 Custom Leet Update (wird von Haupt-App synchronisiert)
     */
    fun updateFavoriteLeet(customLeet: CustomLeet?) {
        currentCustomLeet = customLeet
        currentMode = if (customLeet != null) {
            LeetTranslator.TranslationMode.CUSTOM
        } else {
            LeetTranslator.TranslationMode.SIMPLE
        }

        // Clear cache when leet changes
        clearCache()
    }

    fun getCurrentCustomLeet(): CustomLeet? = currentCustomLeet

    /**
     * 🎨 Smart Suggestions basierend auf Kontext
     */
    fun generateSuggestions(currentWord: String): List<String> {
        if (currentWord.isBlank()) return emptyList()

        val suggestions = mutableListOf<String>()

        // Alle drei Modi als Suggestions
        suggestions.add(LeetTranslator.translate(currentWord, LeetTranslator.TranslationMode.SIMPLE))
        suggestions.add(LeetTranslator.translate(currentWord, LeetTranslator.TranslationMode.EXTENDED))

        currentCustomLeet?.let { leet ->
            suggestions.add(LeetTranslator.translate(currentWord, LeetTranslator.TranslationMode.CUSTOM, leet))
        }

        // Remove duplicates und current word
        return suggestions.distinct().filter { it != currentWord }.take(3)
    }

    /**
     * 🎮 Context-aware Mode Suggestions
     */
    fun suggestModeForContext(packageName: String): LeetTranslator.TranslationMode {
        return when {
            packageName.contains("discord") || packageName.contains("steam") -> LeetTranslator.TranslationMode.EXTENDED
            packageName.contains("whatsapp") || packageName.contains("telegram") -> LeetTranslator.TranslationMode.SIMPLE
            packageName.contains("instagram") || packageName.contains("tiktok") -> currentCustomLeet?.let { LeetTranslator.TranslationMode.CUSTOM } ?: LeetTranslator.TranslationMode.SIMPLE
            else -> currentMode
        }
    }

    /**
     * 📊 Translation Statistics
     */
    private fun updateStats(original: Char, translated: String) {
        val currentStats = _translationStats.value
        _translationStats.value = currentStats.copy(
            totalCharsTranslated = currentStats.totalCharsTranslated + 1,
            mostUsedTranslations = currentStats.mostUsedTranslations.toMutableMap().apply {
                val key = "$original→$translated"
                this[key] = (this[key] ?: 0) + 1
            }
        )
    }

    /**
     * 🧹 Cache Management
     */
    private fun clearCache() {
        charTranslationCache.clear()
        wordTranslationCache.clear()
    }

    fun getCacheSize(): Int = charTranslationCache.size + wordTranslationCache.size

    /**
     * 🔍 Advanced Features
     */

    /**
     * Erkennt ob Text wahrscheinlich schon Leetspeak ist
     */
    fun isLikelyLeetspeak(text: String): Boolean {
        val leetChars = setOf('4', '3', '1', '0', '5', '7', '8', '#', '@', '|', '/', '\\', '_', '(', ')', '<', '>', '!')
        val leetCharCount = text.count { it in leetChars }
        return leetCharCount.toFloat() / text.length >= 0.3f
    }

    /**
     * Generiert "Leet Signature" für Ende von Nachrichten
     */
    fun generateLeetSignature(): String {
        val signatures = listOf(
            " - |_337 5P34K 6363|2470|2",
            " #4x0r3d",
            " pwn3d",
            " 1337",
            " gg n00b"
        )
        return signatures.random()
    }

    /**
     * Automatische Korrektur häufiger Leetspeak-Fehler
     */
    fun autoCorrectLeet(text: String): String {
        return text
            .replace("1337", "1337") // Bereits korrekt
            .replace("leet", "1337")
            .replace("elite", "31173")
            .replace("31337", "1337") // Doppelte Korrektur vermeiden
    }
}

/**
 * 📊 Translation Statistics Data Class
 */
data class TranslationStats(
    val totalCharsTranslated: Int = 0,
    val mostUsedTranslations: Map<String, Int> = emptyMap(),
    val currentStreak: Int = 0,
    val totalWordsTranslated: Int = 0
) {
    val averageTranslationsPerWord: Float
        get() = if (totalWordsTranslated > 0) totalCharsTranslated.toFloat() / totalWordsTranslated else 0f

    val topTranslation: Pair<String, Int>?
        get() = mostUsedTranslations.maxByOrNull { it.value }?.toPair()
}

/**
 * 🎯 Translation Context - für kontextuelle Anpassungen
 */
data class TranslationContext(
    val packageName: String,
    val fieldType: String,
    val isPasswordField: Boolean,
    val isNumericField: Boolean,
    val suggestedMode: LeetTranslator.TranslationMode?
)