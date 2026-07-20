package com.beigel.leetSpeak_Generator.domain.usecase.translation

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GeneratePreviewUseCaseTest {

    private val useCase = GeneratePreviewUseCase()

    @Test
    fun `uses default sample text when none is provided`() {
        val result = useCase(LeetTranslator.TranslationMode.SIMPLE)
        assertThat(result).isEqualTo(LeetTranslator.createPreview(LeetTranslator.TranslationMode.SIMPLE))
    }

    @Test
    fun `uses provided sample text`() {
        val result = useCase(LeetTranslator.TranslationMode.SIMPLE, sampleText = "Gaming")
        assertThat(result).isEqualTo(
            LeetTranslator.createPreview(LeetTranslator.TranslationMode.SIMPLE, sampleText = "Gaming")
        )
    }

    @Test
    fun `preview reflects custom leet mapping`() {
        val customLeet = CustomLeet("Test").apply { setTranslation("H", "#") }
        val result = useCase(LeetTranslator.TranslationMode.CUSTOM, customLeet, sampleText = "Hi")
        assertThat(result).startsWith("#")
    }
}
