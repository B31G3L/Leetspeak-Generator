package com.beigel.leetSpeak_Generator.data

import com.beigel.leetSpeak_Generator.R

/**
 * Represents a custom leetspeak translation profile
 * Migrated from Java to Kotlin with improved type safety and conciseness
 */
data class CustomLeet(
    var name: String,
    var iconResId: Int = R.drawable.ic_custom_mode,
    private val _translations: MutableMap<String, String> = mutableMapOf()
) {

    /**
     * Immutable view of translations
     */
    val translations: Map<String, String>
        get() = _translations.toMap()

    /**
     * Sets a translation mapping from plain character to leet character
     */
    fun setTranslation(plainChar: String, leetChar: String) {
        _translations[plainChar] = leetChar
    }

    /**
     * Gets the leet translation for a plain character
     * Returns the original character if no translation exists
     */
    fun getTranslation(plainChar: String): String =
        _translations[plainChar] ?: plainChar

    /**
     * Sets multiple translations at once
     */
    fun setTranslations(newTranslations: Map<String, String>) {
        _translations.clear()
        _translations.putAll(newTranslations)
    }

    /**
     * Checks if a translation exists for the given character
     */
    fun hasTranslation(plainChar: String): Boolean =
        _translations.containsKey(plainChar)

    /**
     * Removes a translation
     */
    fun removeTranslation(plainChar: String) {
        _translations.remove(plainChar)
    }

    /**
     * Clears all translations
     */
    fun clearTranslations() {
        _translations.clear()
    }

    /**
     * Creates a copy of this profile with a new name
     */
    fun copy(newName: String = this.name): CustomLeet =
        CustomLeet(newName, iconResId, _translations.toMutableMap())

    companion object {
        /**
         * Creates a CustomProfile with default Simple Leet translations
         */
        fun createWithSimpleDefaults(name: String, iconResId: Int = R.drawable.ic_custom_mode): CustomLeet {
            val profile = CustomLeet(name, iconResId)

            // Initialize with Simple Leet mappings
            val simpleMap = mapOf(
                "A" to "4", "B" to "8", "C" to "C", "D" to "D", "E" to "3",
                "F" to "F", "G" to "6", "H" to "#", "I" to "1", "J" to "J",
                "K" to "K", "L" to "L", "M" to "M", "N" to "N", "O" to "0",
                "P" to "P", "Q" to "Q", "R" to "R", "S" to "5", "T" to "7",
                "U" to "U", "V" to "V", "W" to "W", "X" to "X", "Y" to "Y",
                "Z" to "2"
            )

            profile.setTranslations(simpleMap)
            return profile
        }

        /**
         * Creates a CustomProfile with Extended Leet translations
         */
        fun createWithExtendedDefaults(name: String, iconResId: Int = R.drawable.ic_custom_mode): CustomLeet {
            val profile = CustomLeet(name, iconResId)

            // Initialize with Extended Leet mappings
            val extendedMap = mapOf(
                "A" to "4", "B" to "8", "C" to "(", "D" to "|)", "E" to "3",
                "F" to "|=", "G" to "6", "H" to "#", "I" to "!", "J" to "_|",
                "K" to "|<", "L" to "1", "M" to "/\\/\\", "N" to "|\\|", "O" to "0",
                "P" to "9", "Q" to "0_", "R" to "2", "S" to "5", "T" to "7",
                "U" to "|_|", "V" to "\\/", "W" to "\\/\\/", "X" to "><", "Y" to "`/",
                "Z" to "Z"
            )

            profile.setTranslations(extendedMap)
            return profile
        }
    }
}