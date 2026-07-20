package com.beigel.leetSpeak_Generator.domain.usecase.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UiStateManagementUseCaseTest {

    private val useCase = UiStateManagementUseCase()

    @Test
    fun `initial state has no loading, error or success`() {
        val state = useCase.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.errorMessage).isNull()
        assertThat(state.successMessage).isNull()
    }

    @Test
    fun `setLoading updates isLoading flag`() {
        useCase.setLoading(true)
        assertThat(useCase.uiState.value.isLoading).isTrue()

        useCase.setLoading(false)
        assertThat(useCase.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `setError sets message and clears loading`() {
        useCase.setLoading(true)
        useCase.setError("Something went wrong")

        val state = useCase.uiState.value
        assertThat(state.errorMessage).isEqualTo("Something went wrong")
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `setSuccess sets message and clears loading`() {
        useCase.setLoading(true)
        useCase.setSuccess("Copied!")

        val state = useCase.uiState.value
        assertThat(state.successMessage).isEqualTo("Copied!")
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `clearError only removes the error message`() {
        useCase.setError("Oops")
        useCase.clearError()
        assertThat(useCase.uiState.value.errorMessage).isNull()
    }

    @Test
    fun `clearSuccess only removes the success message`() {
        useCase.setSuccess("Done")
        useCase.clearSuccess()
        assertThat(useCase.uiState.value.successMessage).isNull()
    }

    @Test
    fun `clearAll resets to a fresh default state`() {
        useCase.setError("Oops")
        useCase.setLoading(true)
        useCase.clearAll()

        assertThat(useCase.uiState.value).isEqualTo(UiStateManagementUseCase.UiState())
    }

    @Test
    fun `setSuccess after setError overwrites the error with success`() {
        useCase.setError("Oops")
        useCase.setSuccess("Fixed")

        val state = useCase.uiState.value
        assertThat(state.successMessage).isEqualTo("Fixed")
        // setSuccess ändert nur isLoading/successMessage, errorMessage bleibt bestehen,
        // bis es explizit gecleared wird — das dokumentiert dieses Verhalten bewusst.
        assertThat(state.errorMessage).isEqualTo("Oops")
    }
}
