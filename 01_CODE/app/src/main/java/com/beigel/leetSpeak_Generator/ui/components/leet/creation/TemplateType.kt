package com.beigel.leetSpeak_Generator.ui.components.leet.creation

import androidx.compose.runtime.MutableState

/**
 * Template-Typen für Leet-Erstellung mit zugehöriger Logik
 */
enum class TemplateType(val displayName: String, val description: String) {
    SIMPLE("Simple", "Einfache 1:1 Ersetzungen"),
    EXTENDED("Extended", "Erweiterte Multi-Zeichen Ersetzungen"),
    CUSTOM("Leer", "Startet mit leerer Tabelle"),
    NUMERIC("Numerisch", "Nur Zahlen als Ersetzungen"),
    SYMBOLS("Symbole", "Nur Symbole als Ersetzungen")
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