package com.beigel.leetSpeak_Generator.domain.usecase.translation

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Use Case für Übersetzungsstatistiken
 */
@Singleton
class AnalyzeTranslationUseCase @Inject constructor() {

    operator fun invoke(
        input: String,
        mode: LeetTranslator.TranslationMode,
        customLeet: CustomLeet? = null
    ): LeetTranslator.TranslationStats {
        return if (input.isEmpty()) {
            LeetTranslator.TranslationStats.empty()
        } else {
            LeetTranslator.analyzeTranslation(input, mode, customLeet)
        }
    }
}