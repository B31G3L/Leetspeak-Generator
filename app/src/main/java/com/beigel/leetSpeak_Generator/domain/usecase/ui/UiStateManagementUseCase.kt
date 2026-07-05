package com.beigel.leetSpeak_Generator.domain.usecase.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für UI-Zustandsmanagement (Loading, Errors, Success)
 */
@Singleton
class UiStateManagementUseCase @Inject constructor() {

    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val successMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun setLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(
            errorMessage = message,
            isLoading = false
        )
    }

    fun setSuccess(message: String) {
        _uiState.value = _uiState.value.copy(
            successMessage = message,
            isLoading = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun clearAll() {
        _uiState.value = UiState()
    }
}