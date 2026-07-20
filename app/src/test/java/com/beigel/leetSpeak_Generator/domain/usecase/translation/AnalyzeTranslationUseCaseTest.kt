package com.beigel.leetSpeak_Generator.domain.usecase.translation

import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AnalyzeTranslationUseCaseTest {

    private val useCase = AnalyzeTranslationUseCase()

    @Test
    fun `returns empty stats for empty input`() {
        val result = useCase("", LeetTranslator.TranslationMode.SIMPLE)
        assertThat(result).isEqualTo(LeetTranslator.TranslationStats.empty())
    }

    @Test
    fun `counts total characters correctly`() {
        val result = useCase("HELLO", LeetTranslator.TranslationMode.SIMPLE)
        assertThat(result.totalChars).isEqualTo(5)
    }

    @Test
    fun `counts translated and unchanged characters correctly for simple mode`() {
        // H-E-L-L-O: H, E, O werden übersetzt (#, 3, 0); L bleibt L (Simple-Map: L -> L)
        val result = useCase("HELLO", LeetTranslator.TranslationMode.SIMPLE)
        assertThat(result.translatedChars).isEqualTo(3)
        assertThat(result.unchangedChars).isEqualTo(2)
    }
}
