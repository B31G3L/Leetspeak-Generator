package com.beigel.leetSpeak_Generator.domain.usecase.ui

import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TranslationModeUseCaseTest {

    private val useCase = TranslationModeUseCase()

    @Test
    fun `defaults to SIMPLE mode`() {
        assertThat(useCase.getCurrentMode()).isEqualTo(LeetTranslator.TranslationMode.SIMPLE)
        assertThat(useCase.currentMode.value).isEqualTo(LeetTranslator.TranslationMode.SIMPLE)
    }

    @Test
    fun `setMode updates state and getCurrentMode`() {
        useCase.setMode(LeetTranslator.TranslationMode.EXTENDED)
        assertThat(useCase.getCurrentMode()).isEqualTo(LeetTranslator.TranslationMode.EXTENDED)
        assertThat(useCase.currentMode.value).isEqualTo(LeetTranslator.TranslationMode.EXTENDED)
    }

    @Test
    fun `setMode to CUSTOM is reflected immediately`() {
        useCase.setMode(LeetTranslator.TranslationMode.CUSTOM)
        assertThat(useCase.getCurrentMode()).isEqualTo(LeetTranslator.TranslationMode.CUSTOM)
    }
}
