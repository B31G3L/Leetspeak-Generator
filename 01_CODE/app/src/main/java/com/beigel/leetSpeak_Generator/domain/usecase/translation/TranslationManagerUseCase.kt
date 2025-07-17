package com.beigel.leetSpeak_Generator.domain.usecase.translation

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Kombinierter Use Case für alle Übersetzungsoperationen
 * Vereinfacht das ViewModel erheblich
 */
@Singleton
class TranslationManagerUseCase @Inject constructor(
    private val translateTextUseCase: TranslateTextUseCase,
    private val reverseTranslateUseCase: ReverseTranslateUseCase,
    private val detectLeetSpeakUseCase: DetectLeetSpeakUseCase,
    private val analyzeTranslationUseCase: AnalyzeTranslationUseCase,
    private val generatePreviewUseCase: GeneratePreviewUseCase
) {

    /**
     * Hauptübersetzung basierend auf Reverse-Modus
     */
    fun translate(
        input: String,
        mode: LeetTranslator.TranslationMode,
        customLeet: CustomLeet?,
        isReverseMode: Boolean
    ): String {
        return if (isReverseMode) {
            reverseTranslateUseCase(input, mode, customLeet)
        } else {
            translateTextUseCase(input, mode, customLeet)
        }
    }

    /**
     * Prüft ob Input wahrscheinlich Leetspeak ist
     */
    fun isLikelyLeetspeak(input: String): Boolean {
        return detectLeetSpeakUseCase(input)
    }

    /**
     * Analysiert Übersetzung und gibt Statistiken zurück
     */
    fun analyzeTranslation(
        input: String,
        mode: LeetTranslator.TranslationMode,
        customLeet: CustomLeet?
    ): LeetTranslator.TranslationStats {
        return analyzeTranslationUseCase(input, mode, customLeet)
    }

    /**
     * Generiert Vorschau für eine bestimmte Option
     */
    fun generatePreview(
        mode: LeetTranslator.TranslationMode,
        customLeet: CustomLeet?,
        sampleText: String = "Hello"
    ): String {
        return generatePreviewUseCase(mode, customLeet, sampleText)
    }
}