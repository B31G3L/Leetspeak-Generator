package com.beigel.leetSpeak_Generator.ui.components.leet.creation

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import com.beigel.leetSpeak_Generator.R

/**
 * Template-Typen für Leet-Erstellung mit zugehöriger Logik
 */
enum class TemplateType(@StringRes val displayNameRes: Int, @StringRes val descriptionRes: Int) {
    SIMPLE(R.string.template_simple, R.string.template_simple_desc),
    EXTENDED(R.string.template_extended, R.string.template_extended_desc),
    CUSTOM(R.string.template_empty, R.string.template_empty_desc),
    NUMERIC(R.string.template_numeric, R.string.template_numeric_desc),
    SYMBOLS(R.string.template_symbols, R.string.template_symbols_desc)
}

/**
 * Template-spezifische Funktionen und Helpers
 */
object TemplateHelpers {

    /**
     * Wendet das Template auf die Translation States an
     */
    fun applyTemplate(
        template: TemplateType,
        translationStates: List<MutableState<String>>,
        alphabet: String
    ) {
        alphabet.forEachIndexed { index, char ->
            translationStates[index].value = when (template) {
                TemplateType.SIMPLE -> getSimpleTranslation(char)
                TemplateType.EXTENDED -> getExtendedTranslation(char)
                TemplateType.CUSTOM -> char.toString()
                TemplateType.NUMERIC -> getNumericTranslation(char)
                TemplateType.SYMBOLS -> getSymbolTranslation(char)
            }
        }
    }

    /**
     * Generiert Template-Vorschau
     */
    fun getTemplatePreview(template: TemplateType): String {
        return when (template) {
            TemplateType.SIMPLE -> "HELLO → #3LL0"
            TemplateType.EXTENDED -> "HELLO → #3110"
            TemplateType.CUSTOM -> "HELLO → HELLO"
            TemplateType.NUMERIC -> "HELLO → 43110"
            TemplateType.SYMBOLS -> "HELLO → #€££ø"
        }
    }

    /**
     * Kurze Vorschau für Buttons
     */
    fun getTemplateShortPreview(template: TemplateType): String {
        return when (template) {
            TemplateType.SIMPLE -> "A→4"
            TemplateType.EXTENDED -> "A→4"
            TemplateType.CUSTOM -> "A→A"
            TemplateType.NUMERIC -> "A→4"
            TemplateType.SYMBOLS -> "A→@"
        }
    }

    /**
     * Beispiele für Template-Details
     */
    fun getTemplateExamples(template: TemplateType): List<String> {
        return when (template) {
            TemplateType.SIMPLE -> listOf(
                "A → 4", "E → 3", "O → 0", "S → 5", "T → 7"
            )
            TemplateType.EXTENDED -> listOf(
                "A → 4", "M → /\\/\\", "N → |\\|", "U → |_|", "W → \\/\\/"
            )
            TemplateType.CUSTOM -> listOf(
                "A → A", "B → B", "C → C", "... (leer)"
            )
            TemplateType.NUMERIC -> listOf(
                "A → 4", "B → 8", "C → 3", "O → 0", "S → 5"
            )
            TemplateType.SYMBOLS -> listOf(
                "A → @", "E → €", "S → $", "T → †", "Y → ¥"
            )
        }
    }

    /**
     * Live-Vorschau eines ganzen Wortes für ein Template (Redesign v4 Template-Grid).
     */
    fun previewWord(template: TemplateType, word: String = "leetspeak"): String {
        return word.map { c ->
            val upper = c.uppercaseChar()
            when (template) {
                TemplateType.SIMPLE -> getSimpleTranslation(upper)
                TemplateType.EXTENDED -> getExtendedTranslation(upper)
                TemplateType.CUSTOM -> upper.toString()
                TemplateType.NUMERIC -> getNumericTranslation(upper)
                TemplateType.SYMBOLS -> getSymbolTranslation(upper)
            }
        }.joinToString("")
    }

    // Private Translation Functions
    private fun getSimpleTranslation(char: Char): String {
        return when (char) {
            'A' -> "4"; 'B' -> "8"; 'C' -> "C"; 'D' -> "D"; 'E' -> "3"
            'F' -> "F"; 'G' -> "6"; 'H' -> "#"; 'I' -> "1"; 'J' -> "J"
            'K' -> "K"; 'L' -> "L"; 'M' -> "M"; 'N' -> "N"; 'O' -> "0"
            'P' -> "P"; 'Q' -> "Q"; 'R' -> "R"; 'S' -> "5"; 'T' -> "7"
            'U' -> "U"; 'V' -> "V"; 'W' -> "W"; 'X' -> "X"; 'Y' -> "Y"
            'Z' -> "2"
            else -> char.toString()
        }
    }

    private fun getExtendedTranslation(char: Char): String {
        return when (char) {
            'A' -> "4"; 'B' -> "8"; 'C' -> "("; 'D' -> "|)"; 'E' -> "3"
            'F' -> "|="; 'G' -> "6"; 'H' -> "#"; 'I' -> "!"; 'J' -> "_|"
            'K' -> "|<"; 'L' -> "1"; 'M' -> "/\\/\\"; 'N' -> "|\\|"; 'O' -> "0"
            'P' -> "9"; 'Q' -> "0_"; 'R' -> "2"; 'S' -> "5"; 'T' -> "7"
            'U' -> "|_|"; 'V' -> "\\/"; 'W' -> "\\/\\/"; 'X' -> "><"; 'Y' -> "`/"
            'Z' -> "Z"
            else -> char.toString()
        }
    }

    private fun getNumericTranslation(char: Char): String {
        return when (char) {
            'A' -> "4"; 'B' -> "8"; 'C' -> "3"; 'D' -> "0"; 'E' -> "3"
            'F' -> "7"; 'G' -> "6"; 'H' -> "4"; 'I' -> "1"; 'J' -> "1"
            'K' -> "1"; 'L' -> "1"; 'M' -> "44"; 'N' -> "4"; 'O' -> "0"
            'P' -> "9"; 'Q' -> "0"; 'R' -> "2"; 'S' -> "5"; 'T' -> "7"
            'U' -> "0"; 'V' -> "5"; 'W' -> "5"; 'X' -> "2"; 'Y' -> "7"
            'Z' -> "2"
            else -> char.toString()
        }
    }

    private fun getSymbolTranslation(char: Char): String {
        return when (char) {
            'A' -> "@"; 'B' -> "β"; 'C' -> "©"; 'D' -> "Ð"; 'E' -> "€"
            'F' -> "ƒ"; 'G' -> "§"; 'H' -> "#"; 'I' -> "!"; 'J' -> "¿"
            'K' -> "₭"; 'L' -> "£"; 'M' -> "₥"; 'N' -> "ñ"; 'O' -> "ø"
            'P' -> "₱"; 'Q' -> "¤"; 'R' -> "®"; 'S' -> "$"; 'T' -> "†"
            'U' -> "µ"; 'V' -> "√"; 'W' -> "₩"; 'X' -> "×"; 'Y' -> "¥"
            'Z' -> "ƺ"
            else -> char.toString()
        }
    }
}