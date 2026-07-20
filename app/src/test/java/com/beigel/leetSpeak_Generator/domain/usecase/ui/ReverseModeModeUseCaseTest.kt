package com.beigel.leetSpeak_Generator.domain.usecase.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReverseModeModeUseCaseTest {

    private val useCase = ReverseModeModeUseCase()

    @Test
    fun `starts disabled`() {
        assertThat(useCase.isReverseMode.value).isFalse()
    }

    @Test
    fun `toggleReverseMode flips the state each call`() {
        useCase.toggleReverseMode()
        assertThat(useCase.isReverseMode.value).isTrue()

        useCase.toggleReverseMode()
        assertThat(useCase.isReverseMode.value).isFalse()
    }

    @Test
    fun `setReverseMode sets state directly regardless of previous value`() {
        useCase.setReverseMode(true)
        assertThat(useCase.isReverseMode.value).isTrue()

        useCase.setReverseMode(true)
        assertThat(useCase.isReverseMode.value).isTrue()

        useCase.setReverseMode(false)
        assertThat(useCase.isReverseMode.value).isFalse()
    }
}
