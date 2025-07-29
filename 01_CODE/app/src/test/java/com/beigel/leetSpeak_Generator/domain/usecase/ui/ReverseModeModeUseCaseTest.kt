package com.beigel.leetSpeak_Generator.domain.usecase.ui

import app.cash.turbine.test
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReverseModeModeUseCaseTest {

    private lateinit var useCase: ReverseModeModeUseCase

    @Before
    fun setUp() {
        useCase = ReverseModeModeUseCase()
    }

    @Test
    fun `isReverseMode flow starts with false`() = runTest {
        useCase.isReverseMode.test {
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `toggleReverseMode changes state`() = runTest {
        useCase.toggleReverseMode()

        useCase.isReverseMode.test {
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun `toggleReverseMode toggles back and forth`() = runTest {
        useCase.toggleReverseMode() // false -> true
        useCase.toggleReverseMode() // true -> false

        useCase.isReverseMode.test {
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `setReverseMode sets specific value`() = runTest {
        useCase.setReverseMode(true)

        useCase.isReverseMode.test {
            assertEquals(true, awaitItem())
        }

        useCase.setReverseMode(false)

        useCase.isReverseMode.test {
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `setReverseMode with same value doesn't duplicate emissions`() = runTest {
        useCase.setReverseMode(false) // Should be same as initial

        useCase.isReverseMode.test {
            assertEquals(false, awaitItem())
            expectNoEvents()
        }
    }
}