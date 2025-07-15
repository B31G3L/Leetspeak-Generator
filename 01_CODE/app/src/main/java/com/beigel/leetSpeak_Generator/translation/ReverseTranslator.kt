package com.beigel.leetSpeak_Generator.translation

import com.beigel.leetSpeak_Generator.data.CustomLeet

/**
 * Reverse Translation Engine - Konvertiert Leetspeak zurück zu Plaintext
 */
object ReverseTranslator {

    // Reverse mapping für Simple Mode (längste Matches zuerst)
    private val simpleReverseMap = mapOf(
        "4" to "A", "8" to "B", "3" to "E", "6" to "G", "#" to "H",
        "1" to "I", "0" to "O", "5" to "S", "7" to "T", "2" to "Z"
    ).toList().sortedByDescending { it.first.length }

    // Reverse mapping für Extended Mode (längste Matches zuerst für bessere Erkennung)
    private val extendedReverseMap = mapOf(
        "/\\/\\" to "M", "|\\|" to "N", "|_|" to "U", "\\/" to "V",
        "\\/\\/" to "W", "><" to "X", "`/" to "Y", "0_" to "Q",
        "|)" to "D", "|=" to "F", "_|" to "J", "|<" to "K",
        "4" to "A", "8" to "B", "(" to "C", "3" to "E", "6" to "G",
        "#" to "H", "!" to "I", "1" to "L", "0" to "O", "9" to "P",
        "2" to "R", "5" to "S", "7" to "T"
    ).toList().sortedByDescending { it.first.length }

    /**
     * Hauptfunktion für Reverse Translation
     */
    fun reverseTranslate(
        leetText: String?,
        mode: LeetTranslator.TranslationMode,
        customLeet: CustomLeet? = null
    ): String {
        if (leetText.isNullOrEmpty()) return leetText.orEmpty()

        return when (mode) {
            LeetTranslator.TranslationMode.SIMPLE -> reverseTranslateSimple(leetText)
            LeetTranslator.TranslationMode.EXTENDED -> reverseTranslateExtended(leetText)
            LeetTranslator.TranslationMode.CUSTOM -> reverseTranslateCustom(leetText, customLeet)
        }
    }

    /**
     * Reverse für Simple Mode
     */
    private fun reverseTranslateSimple(text: String): String {
        return reverseTranslateWithMap(text, simpleReverseMap)
    }

    /**
     * Reverse für Extended Mode
     */
    private fun reverseTranslateExtended(text: String): String {
        return reverseTranslateWithMap(text, extendedReverseMap)
    }

    /**
     * Reverse für Custom Mode
     */
    private fun reverseTranslateCustom(text: String, customLeet: CustomLeet?): String {
        return if (customLeet != null) {
            // Erstelle Reverse Map aus Custom Leet
            val customReverseMap = customLeet.translations
                .map { (plain, leet) -> leet to plain }
                .sortedByDescending { it.first.length }

            reverseTranslateWithMap(text, customReverseMap)
        } else {
            // Fallback zu Simple
            reverseTranslateSimple(text)
        }
    }

    /**
     * Kern-Algorithmus: Pattern-basierte Rückübersetzung
     */
    private fun reverseTranslateWithMap(text: String, reverseMap: List<Pair<String, String>>): String {
        var result = text
        var position = 0

        while (position < result.length) {
            var matched = false

            // Versuche längste Matches zuerst
            for ((leetPattern, plainChar) in reverseMap) {
                if (position + leetPattern.length <= result.length) {
                    val substring = result.substring(position, position + leetPattern.length)

                    if (substring.equals(leetPattern, ignoreCase = true)) {
                        // Ersetze das Pattern
                        result = result.substring(0, position) +
                                plainChar +
                                result.substring(position + leetPattern.length)

                        position += plainChar.length
                        matched = true
                        break
                    }
                }
            }

            if (!matched) {
                position++
            }
        }

        return result
    }

    /**
     * Prüft ob ein Text wahrscheinlich Leetspeak ist
     */
    fun isLikelyLeetspeak(text: String): Boolean {
        if (text.isEmpty()) return false

        val leetIndicators = listOf(
            "4", "3", "1", "0", "5", "7", "8", "#", "@",
            "|", "/", "\\", "_", "(", ")", "<", ">", "!"
        )

        val leetChars = text.count { char ->
            leetIndicators.any { indicator ->
                char.toString().equals(indicator, ignoreCase = true)
            }
        }

        return leetChars.toFloat() / text.length >= 0.2f // Mind. 20% Leet-Chars
    }

    /**
     * Extension function für einfachere Nutzung
     */
    fun String.fromLeet(
        mode: LeetTranslator.TranslationMode,
        customLeet: CustomLeet? = null
    ): String = reverseTranslate(this, mode, customLeet)
}