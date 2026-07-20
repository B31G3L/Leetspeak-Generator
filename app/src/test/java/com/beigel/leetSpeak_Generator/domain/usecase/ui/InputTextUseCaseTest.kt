package com.beigel.leetSpeak_Generator.domain.usecase.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InputTextUseCaseTest {

    private val useCase = InputTextUseCase()

    @Test
    fun `initial text is empty`() {
        assertThat(useCase.getText()).isEmpty()
        assertThat(useCase.inputText.value).isEmpty()
    }

    @Test
    fun `updateText updates state and getText`() {
        useCase.updateText("Hello")
        assertThat(useCase.getText()).isEqualTo("Hello")
        assertThat(useCase.inputText.value).isEqualTo("Hello")
    }

    @Test
    fun `clearText resets to empty`() {
        useCase.updateText("Hello")
        useCase.clearText()
        assertThat(useCase.getText()).isEmpty()
    }

    @Test
    fun `hasText reflects current content`() {
        assertThat(useCase.hasText()).isFalse()
        useCase.updateText("x")
        assertThat(useCase.hasText()).isTrue()
        useCase.clearText()
        assertThat(useCase.hasText()).isFalse()
    }
}
