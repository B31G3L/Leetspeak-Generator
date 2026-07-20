package com.beigel.leetSpeak_Generator.domain.usecase.translation

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TranslateTextUseCaseTest {

    private val useCase = TranslateTextUseCase()

    @Test
    fun `returns empty string for empty input`() {
        val result = useCase("", LeetTranslator.TranslationMode.SIMPLE)
        assertThat(result).isEmpty()
    }

    @Test
    fun `delegates to LeetTranslator for simple mode`() {
        val result = useCase("HELLO", LeetTranslator.TranslationMode.SIMPLE)
        assertThat(result).isEqualTo(LeetTranslator.translate("HELLO", LeetTranslator.TranslationMode.SIMPLE))
    }

    @Test
    fun `delegates to LeetTranslator for extended mode`() {
        val result = useCase("HELLO", LeetTranslator.TranslationMode.EXTENDED)
        assertThat(result).isEqualTo(LeetTranslator.translate("HELLO", LeetTranslator.TranslationMode.EXTENDED))
    }

    @Test
    fun `uses provided custom leet for custom mode`() {
        val customLeet = CustomLeet("Test").apply { setTranslation("A", "@") }
        val result = useCase("A", LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertThat(result).isEqualTo("@")
    }

    @Test
    fun `custom mode without customLeet falls back to simple map`() {
        val result = useCase("A", LeetTranslator.TranslationMode.CUSTOM, null)
        assertThat(result).isEqualTo("4")
    }
}
