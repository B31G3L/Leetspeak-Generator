package com.beigel.leetSpeak_Generator.domain.usecase.ui

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für die Generierung von Display-Namen
 */
@Singleton
class DisplayNameUseCase @Inject constructor() {

    fun generateModeDisplayName(
        mode: LeetTranslator.TranslationMode,
        customLeet: CustomLeet?,
        isReverseMode: Boolean
    ): String {
        val baseName = when (mode) {
            LeetTranslator.TranslationMode.SIMPLE -> "Simple Leet"
            LeetTranslator.TranslationMode.EXTENDED -> "Extended Leet"
            LeetTranslator.TranslationMode.CUSTOM -> customLeet?.name ?: "Custom Leet"
        }
        return if (isReverseMode) "Reverse" else baseName
    }

    fun generateInputTitle(isReverseMode: Boolean, currentModeDisplayName: String): String {
        return if (isReverseMode) {
            "Input: $currentModeDisplayName"
        } else {
            "Input: Plaintext"
        }
    }

    fun generateOutputTitle(isReverseMode: Boolean, currentModeDisplayName: String): String {
        return if (isReverseMode) {
            "Output: Plaintext"
        } else {
            "Output: $currentModeDisplayName"
        }
    }
}