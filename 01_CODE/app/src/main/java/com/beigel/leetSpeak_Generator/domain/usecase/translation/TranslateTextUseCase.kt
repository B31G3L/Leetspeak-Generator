package com.beigel.leetSpeak_Generator.domain.usecase.translation

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für Text-zu-Leet Übersetzung
 */
@Singleton
class TranslateTextUseCase @Inject constructor() {

    operator fun invoke(
        input: String,
        mode: LeetTranslator.TranslationMode,
        customLeet: CustomLeet? = null
    ): String {
        return if (input.isEmpty()) {
            ""
        } else {
            LeetTranslator.translate(input, mode, customLeet)
        }
    }
}
