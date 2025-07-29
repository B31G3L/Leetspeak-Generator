package com.beigel.leetSpeak_Generator.presentation.intent

import com.beigel.leetSpeak_Generator.data.LeetOption
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests für MainUiState Data Class
 */
class MainUiStateTest {

    @Test
    fun `default constructor creates correct initial state`() {
        val state = MainUiState()

        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
        assertNull(state.selectedLeetOption)
        assertFalse(state.showBottomSheet)
        assertFalse(state.showWhatsNew)
    }

    @Test
    fun `constructor with parameters sets correct values`() {
        val leetOption = LeetOption.createSimple()
        val state = MainUiState(
            isLoading = true,
            errorMessage = "Test error",
            successMessage = "Test success",
            selectedLeetOption = leetOption,
            showBottomSheet = true,
            showWhatsNew = true
        )

        assertTrue(state.isLoading)
        assertEquals("Test error", state.errorMessage)
        assertEquals("Test success", state.successMessage)
        assertEquals(leetOption, state.selectedLeetOption)
        assertTrue(state.showBottomSheet)
        assertTrue(state.showWhatsNew)
    }

    @Test
    fun `copy function works correctly`() {
        val original = MainUiState(isLoading = true, errorMessage = "Original error")
        val copy = original.copy(isLoading = false, successMessage = "Success")

        assertFalse(copy.isLoading)
        assertEquals("Original error", copy.errorMessage) // Preserved
        assertEquals("Success", copy.successMessage) // Changed
        assertNull(copy.selectedLeetOption) // Default preserved
    }

    @Test
    fun `equals and hashCode work correctly`() {
        val state1 = MainUiState(isLoading = true, errorMessage = "Error")
        val state2 = MainUiState(isLoading = true, errorMessage = "Error")
        val state3 = MainUiState(isLoading = false, errorMessage = "Error")

        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `toString contains state information`() {
        val state = MainUiState(
            isLoading = true,
            errorMessage = "Test error",
            showWhatsNew = true
        )
        val toString = state.toString()

        assertTrue(toString.contains("isLoading=true"))
        assertTrue(toString.contains("Test error"))
        assertTrue(toString.contains("showWhatsNew=true"))
    }

    @Test
    fun `partial copy preserves other values`() {
        val leetOption = LeetOption.createExtended()
        val original = MainUiState(
            isLoading = false,
            errorMessage = "Error",
            selectedLeetOption = leetOption,
            showBottomSheet = true
        )

        val copy = original.copy(isLoading = true)

        assertTrue(copy.isLoading) // Changed
        assertEquals("Error", copy.errorMessage) // Preserved
        assertEquals(leetOption, copy.selectedLeetOption) // Preserved
        assertTrue(copy.showBottomSheet) // Preserved
        assertFalse(copy.showWhatsNew) // Default preserved
    }

    @Test
    fun `state can have both error and success messages`() {
        val state = MainUiState(
            errorMessage = "Error occurred",
            successMessage = "But this succeeded"
        )

        assertEquals("Error occurred", state.errorMessage)
        assertEquals("But this succeeded", state.successMessage)
    }

    @Test
    fun `state can clear messages with copy`() {
        val state = MainUiState(
            errorMessage = "Error",
            successMessage = "Success"
        )

        val clearedState = state.copy(
            errorMessage = null,
            successMessage = null
        )

        assertNull(clearedState.errorMessage)
        assertNull(clearedState.successMessage)
    }

    @Test
    fun `loading state combinations`() {
        val loadingState = MainUiState(isLoading = true)
        val errorState = loadingState.copy(isLoading = false, errorMessage = "Failed")
        val successState = errorState.copy(errorMessage = null, successMessage = "Done")

        assertTrue(loadingState.isLoading)
        assertNull(loadingState.errorMessage)

        assertFalse(errorState.isLoading)
        assertEquals("Failed", errorState.errorMessage)

        assertNull(successState.errorMessage)
        assertEquals("Done", successState.successMessage)
    }
}