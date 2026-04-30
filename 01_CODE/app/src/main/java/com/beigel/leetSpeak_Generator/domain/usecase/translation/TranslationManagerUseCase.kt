package com.beigel.leetSpeak_Generator.domain.usecase.translation

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationManagerUseCase @Inject constructor(
    private val translateTextUseCase: TranslateTextUseCase,
    private val reverseTranslateUseCase: ReverseTranslateUseCase,
    private val detectLeetSpeakUseCase: DetectLeetSpeakUseCase,
    private val analyzeTranslationUseCase: AnalyzeTranslationUseCase,
    private val generatePreviewUseCase: GeneratePreviewUseCase
) {
    fun translate(
        input: String,
        mode: LeetTranslator.TranslationMode,
        customLeet: CustomLeet?,
        isReverseMode: Boolean
    ): String = if (isReverseMode) {
        reverseTranslateUseCase(input, mode, customLeet)
    } else {
        translateTextUseCase(input, mode, customLeet)
    }

    fun isLikelyLeetspeak(input: String): Boolean =
        detectLeetSpeakUseCase(input)

    fun analyzeTranslation(
        input: String,
        mode: LeetTranslator.TranslationMode,
        customLeet: CustomLeet?
    ): LeetTranslator.TranslationStats =
        analyzeTranslationUseCase(input, mode, customLeet)

    fun generatePreview(
        mode: LeetTranslator.TranslationMode,
        customLeet: CustomLeet?,
        sampleText: String = "Hello"
    ): String = generatePreviewUseCase(mode, customLeet, sampleText)
}