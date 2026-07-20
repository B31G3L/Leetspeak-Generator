package com.beigel.leetSpeak_Generator.domain.usecase.ui

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DisplayNameUseCaseTest {

    private val useCase = DisplayNameUseCase()

    // ─── generateModeDisplayName ───────────────────────────────────────────────

    @Test
    fun `simple mode returns 'Simple Leet' when not reversed`() {
        val result = useCase.generateModeDisplayName(
            LeetTranslator.TranslationMode.SIMPLE, customLeet = null, isReverseMode = false
        )
        assertThat(result).isEqualTo("Simple Leet")
    }

    @Test
    fun `extended mode returns 'Extended Leet' when not reversed`() {
        val result = useCase.generateModeDisplayName(
            LeetTranslator.TranslationMode.EXTENDED, customLeet = null, isReverseMode = false
        )
        assertThat(result).isEqualTo("Extended Leet")
    }

    @Test
    fun `custom mode returns the custom leet's name`() {
        val customLeet = CustomLeet("Gaming Leet")
        val result = useCase.generateModeDisplayName(
            LeetTranslator.TranslationMode.CUSTOM, customLeet = customLeet, isReverseMode = false
        )
        assertThat(result).isEqualTo("Gaming Leet")
    }

    @Test
    fun `custom mode without a customLeet falls back to 'Custom Leet'`() {
        val result = useCase.generateModeDisplayName(
            LeetTranslator.TranslationMode.CUSTOM, customLeet = null, isReverseMode = false
        )
        assertThat(result).isEqualTo("Custom Leet")
    }

    @Test
    fun `reverse mode always returns 'Reverse' regardless of underlying mode`() {
        val result = useCase.generateModeDisplayName(
            LeetTranslator.TranslationMode.EXTENDED, customLeet = null, isReverseMode = true
        )
        assertThat(result).isEqualTo("Reverse")
    }

    // ─── generateInputTitle / generateOutputTitle ─────────────────────────────

    @Test
    fun `input title in forward mode is always 'Input Plaintext'`() {
        val result = useCase.generateInputTitle(isReverseMode = false, currentModeDisplayName = "Simple Leet")
        assertThat(result).isEqualTo("Input: Plaintext")
    }

    @Test
    fun `input title in reverse mode shows the current mode name`() {
        val result = useCase.generateInputTitle(isReverseMode = true, currentModeDisplayName = "Simple Leet")
        assertThat(result).isEqualTo("Input: Simple Leet")
    }

    @Test
    fun `output title in forward mode shows the current mode name`() {
        val result = useCase.generateOutputTitle(isReverseMode = false, currentModeDisplayName = "Simple Leet")
        assertThat(result).isEqualTo("Output: Simple Leet")
    }

    @Test
    fun `output title in reverse mode is always 'Output Plaintext'`() {
        val result = useCase.generateOutputTitle(isReverseMode = true, currentModeDisplayName = "Simple Leet")
        assertThat(result).isEqualTo("Output: Plaintext")
    }
}
