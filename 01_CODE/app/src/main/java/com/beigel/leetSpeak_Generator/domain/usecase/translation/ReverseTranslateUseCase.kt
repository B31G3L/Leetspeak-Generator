package com.beigel.leetSpeak_Generator.domain.usecase.translation

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import com.beigel.leetSpeak_Generator.translation.ReverseTranslator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für Leet-zu-Text Rückübersetzung
 */
@Singleton
class ReverseTranslateUseCase @Inject constructor() {

    operator fun invoke(
        input: String,
        mode: LeetTranslator.TranslationMode,
        customLeet: CustomLeet? = null
    ): String {
        return if (input.isEmpty()) {
            ""
        } else {
            ReverseTranslator.reverseTranslate(input, mode, customLeet)
        }
    }
}