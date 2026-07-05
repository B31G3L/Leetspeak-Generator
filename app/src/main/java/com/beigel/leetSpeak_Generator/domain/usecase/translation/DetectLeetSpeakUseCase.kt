package com.beigel.leetSpeak_Generator.domain.usecase.translation

import com.beigel.leetSpeak_Generator.translation.ReverseTranslator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für Leetspeak-Erkennung
 */
@Singleton
class DetectLeetSpeakUseCase @Inject constructor() {

    operator fun invoke(input: String): Boolean {
        return ReverseTranslator.isLikelyLeetspeak(input)
    }
}