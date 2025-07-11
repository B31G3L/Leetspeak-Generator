package com.beigel.leetSpeak_Generator

/**
 * Core translation engine for converting text to leetspeak
 * Migrated to Kotlin object with improved performance and type safety
 */
object LeetTranslator {

    // Static translation maps initialized once for performance
    private val simpleMap = mapOf(
        'A' to "4", 'B' to "8", 'C' to "C", 'D' to "D", 'E' to "3",
        'F' to "F", 'G' to "6", 'H' to "#", 'I' to "1", 'J' to "J",
        'K' to "K", 'L' to "L", 'M' to "M", 'N' to "N", 'O' to "0",
        'P' to "P", 'Q' to "Q", 'R' to "R", 'S' to "5", 'T' to "7",
        'U' to "U", 'V' to "V", 'W' to "W", 'X' to "X", 'Y' to "Y",
        'Z' to "2"
    )

    private val extendedMap = mapOf(
        'A' to "4", 'B' to "8", 'C' to "(", 'D' to "|)", 'E' to "3",
        'F' to "|=", 'G' to "6", 'H' to "#", 'I' to "!", 'J' to "_|",
        'K' to "|<", 'L' to "1", 'M' to "/\\/\\", 'N' to "|\\|", 'O' to "0",
        'P' to "9", 'Q' to "0_", 'R' to "2", 'S' to "5", 'T' to "7",
        'U' to "|_|", 'V' to "\\/", 'W' to "\\/\\/", 'X' to "><", 'Y' to "`/",
        'Z' to "Z"
    )

    /**
     * Translation modes enum for type safety
     */
    enum class TranslationMode {
        SIMPLE, EXTENDED, CUSTOM
    }

    /**
     * Translates input text using specified mode and optional custom profile
     *
     * @param input The text to translate
     * @param mode The translation mode to use
     * @param customLeet Optional custom profile for CUSTOM mode
     * @return Translated text
     */
    fun translate(
        input: String?,
        mode: TranslationMode,
        customLeet: CustomLeet? = null
    ): String {
        if (input.isNullOrEmpty()) return input.orEmpty()

        return input.map { char ->
            translateChar(char, mode, customLeet)
        }.joinToString("")
    }

    /**
     * Translates a single character
     *
     * @param char The character to translate
     * @param mode The translation mode
     * @param customLeet Optional custom profile
     * @return Translated character as string
     */
    fun translateChar(
        char: Char,
        mode: TranslationMode,
        customLeet: CustomLeet? = null
    ): String {
        val upperChar = char.uppercaseChar()

        return when (mode) {
            TranslationMode.SIMPLE -> simpleMap[upperChar] ?: char.toString()
            TranslationMode.EXTENDED -> extendedMap[upperChar] ?: char.toString()
            TranslationMode.CUSTOM -> {
                customLeet?.getTranslation(upperChar.toString())
                    ?: simpleMap[upperChar]
                    ?: char.toString()
            }
        }
    }

    /**
     * Extension function for easier string translation
     */
    fun String.toLeet(
        mode: TranslationMode,
        customLeet: CustomLeet? = null
    ): String = translate(this, mode, customLeet)

    /**
     * Gets the translation map for a specific mode
     */
    fun getTranslationMap(mode: TranslationMode): Map<Char, String> = when (mode) {
        TranslationMode.SIMPLE -> simpleMap
        TranslationMode.EXTENDED -> extendedMap
        TranslationMode.CUSTOM -> simpleMap // Fallback to simple for custom
    }

    /**
     * Validates if a character has a translation in the given mode
     */
    fun hasTranslation(char: Char, mode: TranslationMode): Boolean {
        val upperChar = char.uppercaseChar()
        return when (mode) {
            TranslationMode.SIMPLE -> simpleMap.containsKey(upperChar)
            TranslationMode.EXTENDED -> extendedMap.containsKey(upperChar)
            TranslationMode.CUSTOM -> true // Custom can have any translation
        }
    }

    /**
     * Gets all available characters that can be translated
     */
    fun getTranslatableChars(mode: TranslationMode): Set<Char> = when (mode) {
        TranslationMode.SIMPLE -> simpleMap.keys
        TranslationMode.EXTENDED -> extendedMap.keys
        TranslationMode.CUSTOM -> (simpleMap.keys + extendedMap.keys).toSet()
    }

    /**
     * Creates a preview translation for the given mode
     */
    fun createPreview(
        mode: TranslationMode,
        customLeet: CustomLeet? = null,
        sampleText: String = "Hello"
    ): String = translate(sampleText, mode, customLeet)

    /**
     * Analyzes text and returns translation statistics
     */
    fun analyzeTranslation(
        input: String,
        mode: TranslationMode,
        customLeet: CustomLeet? = null
    ): TranslationStats {
        if (input.isEmpty()) return TranslationStats.empty()

        var totalChars = 0
        var translatedChars = 0
        var unchangedChars = 0

        input.forEach { char ->
            totalChars++
            val translated = translateChar(char, mode, customLeet)
            if (translated != char.toString()) {
                translatedChars++
            } else {
                unchangedChars++
            }
        }

        return TranslationStats(
            totalChars = totalChars,
            translatedChars = translatedChars,
            unchangedChars = unchangedChars,
            translationPercentage = if (totalChars > 0) (translatedChars * 100f) / totalChars else 0f
        )
    }

    /**
     * Data class for translation statistics
     */
    data class TranslationStats(
        val totalChars: Int,
        val translatedChars: Int,
        val unchangedChars: Int,
        val translationPercentage: Float
    ) {
        companion object {
            fun empty() = TranslationStats(0, 0, 0, 0f)
        }
    }
}