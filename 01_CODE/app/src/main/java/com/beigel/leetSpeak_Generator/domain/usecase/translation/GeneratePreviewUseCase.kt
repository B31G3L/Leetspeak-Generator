package com.beigel.leetSpeak_Generator.domain.usecase.translation

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für Vorschau-Generierung
 */
@Singleton
class GeneratePreviewUseCase @Inject constructor() {

    operator fun invoke(
        mode: LeetTranslator.TranslationMode,
        customLeet: CustomLeet? = null,
        sampleText: String = "Hello"
    ): String {
        return LeetTranslator.createPreview(mode, customLeet, sampleText)
    }
}