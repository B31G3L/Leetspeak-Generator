package com.beigel.leetSpeak_Generator.domain.usecase.translation

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReverseTranslateUseCaseTest {

    private val useCase = ReverseTranslateUseCase()

    @Test
    fun `returns empty string for empty input`() {
        val result = useCase("", LeetTranslator.TranslationMode.SIMPLE)
        assertThat(result).isEmpty()
    }

    @Test
    fun `reverses simple leet back to plaintext`() {
        val result = useCase("#3LL0", LeetTranslator.TranslationMode.SIMPLE)
        assertThat(result).isEqualTo("HELLO")
    }

    @Test
    fun `reverses extended leet back to plaintext`() {
        val leet = LeetTranslator.translate("MAN", LeetTranslator.TranslationMode.EXTENDED)
        val result = useCase(leet, LeetTranslator.TranslationMode.EXTENDED)
        assertThat(result).isEqualTo("MAN")
    }

    @Test
    fun `reverses custom leet using provided mapping`() {
        val customLeet = CustomLeet("Test").apply { setTranslation("A", "@") }
        val result = useCase("@", LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertThat(result).isEqualTo("A")
    }

    @Test
    fun `custom mode without customLeet falls back to simple reverse`() {
        val result = useCase("4", LeetTranslator.TranslationMode.CUSTOM, null)
        assertThat(result).isEqualTo("A")
    }
}
