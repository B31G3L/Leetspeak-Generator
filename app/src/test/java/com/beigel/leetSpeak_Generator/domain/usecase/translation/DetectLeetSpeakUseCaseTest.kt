package com.beigel.leetSpeak_Generator.domain.usecase.translation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DetectLeetSpeakUseCaseTest {

    private val useCase = DetectLeetSpeakUseCase()

    @Test
    fun `returns false for empty input`() {
        assertThat(useCase("")).isFalse()
    }

    @Test
    fun `returns false for plain text without leet indicators`() {
        assertThat(useCase("Hello World")).isFalse()
    }

    @Test
    fun `returns true for text with many leet indicators`() {
        assertThat(useCase("#3LL0 W0RLD")).isTrue()
    }

    @Test
    fun `returns false for single leet char in long plain text`() {
        // Nur ein "0" auf viele Buchstaben verteilt -> unter der 20%-Schwelle
        assertThat(useCase("This is a totally normal sentence with 0 leet chars really")).isFalse()
    }
}
