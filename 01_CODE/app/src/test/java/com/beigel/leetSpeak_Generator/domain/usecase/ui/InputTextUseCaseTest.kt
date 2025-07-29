package com.beigel.leetSpeak_Generator.domain.usecase.ui

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InputTextUseCaseTest {

    private lateinit var useCase: InputTextUseCase

    @Before
    fun setUp() {
        useCase = InputTextUseCase()
    }

    @Test
    fun `inputText flow starts with empty string`() = runTest {
        useCase.inputText.test {
            assertEquals("", awaitItem())
        }
    }

    @Test
    fun `updateText updates flow`() = runTest {
        useCase.updateText("Hello")

        useCase.inputText.test {
            assertEquals("Hello", awaitItem())
        }
    }

    @Test
    fun `clearText resets to empty string`() = runTest {
        useCase.updateText("Hello")
        useCase.clearText()

        useCase.inputText.test {
            assertEquals("", awaitItem())
        }
    }

    @Test
    fun `getText returns current value`() {
        useCase.updateText("Test")
        assertEquals("Test", useCase.getText())
    }

    @Test
    fun `hasText returns correct boolean`() {
        assertFalse(useCase.hasText())

        useCase.updateText("Hello")
        assertTrue(useCase.hasText())

        useCase.clearText()
        assertFalse(useCase.hasText())
    }

    @Test
    fun `multiple updates work correctly`() = runTest {
        useCase.updateText("First")
        useCase.updateText("Second")
        useCase.updateText("Third")

        useCase.inputText.test {
            assertEquals("Third", awaitItem())
        }
    }
}