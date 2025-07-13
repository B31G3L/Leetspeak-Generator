package com.beigel.leetSpeak_Generator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject

/**
 * MainViewModel mit Hilt Dependency Injection
 * Repository wird automatisch von Hilt injiziert
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: LeetRepository
) : ViewModel() {

    // Input state
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // Current mode state
    private val _currentMode = MutableStateFlow(LeetTranslator.TranslationMode.SIMPLE)
    val currentMode: StateFlow<LeetTranslator.TranslationMode> = _currentMode.asStateFlow()

    // UI state
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Repository state flows
    val leets = repository.leets
    val currentLeet = repository.currentLeet
    val hasLeets = repository.hasLeets
    val leetOptions = repository.getLeetOptions()
    val favoriteLeetOptions = repository.getFavoriteLeetOptions()

    // Leet Index State
    val currentLeetIndex = repository.currentLeetIndex

    // Computed output text
    val outputText: StateFlow<String> = combine(
        inputText,
        currentMode,
        currentLeet
    ) { input, mode, leet ->
        if (input.isEmpty()) {
            ""
        } else {
            LeetTranslator.translate(input, mode, leet)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")

    // Translation statistics
    val translationStats: StateFlow<LeetTranslator.TranslationStats> = combine(
        inputText,
        currentMode,
        currentLeet
    ) { input, mode, leet ->
        if (input.isEmpty()) {
            LeetTranslator.TranslationStats.empty()
        } else {
            LeetTranslator.analyzeTranslation(input, mode, leet)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, LeetTranslator.TranslationStats.empty())

    // Current mode display name
    val currentModeDisplayName: StateFlow<String> = combine(
        currentMode,
        currentLeet
    ) { mode, leet ->
        when (mode) {
            LeetTranslator.TranslationMode.SIMPLE -> "Simple Leet"
            LeetTranslator.TranslationMode.EXTENDED -> "Extended Leet"
            LeetTranslator.TranslationMode.CUSTOM -> leet?.name ?: "Custom Leet"
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, "Simple Leet")

    // Layout state for output section visibility
    val shouldShowOutput: StateFlow<Boolean> = outputText.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    private var debounceJob: Job? = null

    init {
        loadInitialState()
    }

    /**
     * Loads initial state from repository
     */
    private fun loadInitialState() {
        viewModelScope.launch {
            repository.loadFavoriteLeet()
                .onSuccess { result ->
                    when (result) {
                        is LeetRepository.FavoriteLeetResult.Simple -> {
                            _currentMode.value = LeetTranslator.TranslationMode.SIMPLE
                        }
                        is LeetRepository.FavoriteLeetResult.Extended -> {
                            _currentMode.value = LeetTranslator.TranslationMode.EXTENDED
                        }
                        is LeetRepository.FavoriteLeetResult.Custom -> {
                            _currentMode.value = LeetTranslator.TranslationMode.CUSTOM
                            repository.setCurrentLeetIndex(result.customIndex)
                        }
                    }
                }
                .onFailure { exception ->
                    updateUiState { copy(errorMessage = "Failed to load favorite leet: ${exception.message}") }
                }
        }
    }

    /**
     * Handles input text changes with debouncing
     */
    fun updateInputText(text: String) {
        _inputText.value = text

    }

    /**
     * Changes the current leet mode
     */
    fun changeMode(leetOption: LeetOption) {
        viewModelScope.launch {
            try {
                when (leetOption.mode) {
                    com.beigel.leetSpeak_Generator.manager.LeetManager.MODE_SIMPLE -> {
                        _currentMode.value = LeetTranslator.TranslationMode.SIMPLE
                    }
                    com.beigel.leetSpeak_Generator.manager.LeetManager.MODE_EXTENDED -> {
                        _currentMode.value = LeetTranslator.TranslationMode.EXTENDED
                    }
                    com.beigel.leetSpeak_Generator.manager.LeetManager.MODE_CUSTOM -> {
                        if (leetOption.isCustom && leetOption.customIndex >= 0) {
                            repository.setCurrentLeetIndex(leetOption.customIndex)
                            _currentMode.value = LeetTranslator.TranslationMode.CUSTOM
                        }
                    }
                }

                updateUiState { copy(selectedLeetOption = leetOption) }

            } catch (e: Exception) {
                updateUiState { copy(errorMessage = "Failed to change mode: ${e.message}") }
            }
        }
    }

    /**
     * Toggles favorite status for a leet option
     */
    fun toggleFavorite(leetOption: LeetOption) {
        viewModelScope.launch {
            repository.toggleFavorite(leetOption.mode, leetOption.customIndex)
                .onSuccess { result ->
                    val message = if (result.isNowFavorite) {
                        "Added to favorites"
                    } else {
                        "Removed from favorites"
                    }
                    updateUiState { copy(successMessage = message) }
                }
                .onFailure { exception ->
                    updateUiState { copy(errorMessage = "Failed to toggle favorite: ${exception.message}") }
                }
        }
    }

    /**
     * Creates a new leet
     */
    fun createLeet(name: String, iconResId: Int, useExtendedDefaults: Boolean = false) {
        viewModelScope.launch {
            updateUiState { copy(isLoading = true) }

            val result = if (useExtendedDefaults) {
                repository.createLeetWithExtendedDefaults(name, iconResId)
            } else {
                repository.createLeetWithSimpleDefaults(name, iconResId)
            }

            result
                .onSuccess { leet ->
                    _currentMode.value = LeetTranslator.TranslationMode.CUSTOM
                    updateUiState {
                        copy(
                            isLoading = false,
                            successMessage = "Leet '${leet.name}' created successfully"
                        )
                    }
                }
                .onFailure { exception ->
                    updateUiState {
                        copy(
                            isLoading = false,
                            errorMessage = "Failed to create leet: ${exception.message}"
                        )
                    }
                }
        }
    }

    /**
     * Updates an existing leet
     */
    fun updateLeet(index: Int, leet: CustomLeet) {
        viewModelScope.launch {
            updateUiState { copy(isLoading = true) }

            repository.updateLeet(index, leet)
                .onSuccess {
                    updateUiState {
                        copy(
                            isLoading = false,
                            successMessage = "Leet '${leet.name}' updated successfully"
                        )
                    }
                }
                .onFailure { exception ->
                    updateUiState {
                        copy(
                            isLoading = false,
                            errorMessage = "Failed to update leet: ${exception.message}"
                        )
                    }
                }
        }
    }

    /**
     * Deletes a leet
     */
    fun deleteLeet(index: Int) {
        viewModelScope.launch {
            updateUiState { copy(isLoading = true) }

            repository.deleteLeet(index)
                .onSuccess { result ->
                    val message = if (result.wasLastLeet) {
                        "Last leet deleted, switched to Simple mode"
                    } else if (result.wasFavorite) {
                        "Favorite leet deleted"
                    } else {
                        "Leet deleted successfully"
                    }

                    // Switch to simple mode if no leets left
                    if (result.wasLastLeet) {
                        _currentMode.value = LeetTranslator.TranslationMode.SIMPLE
                    }

                    updateUiState {
                        copy(
                            isLoading = false,
                            successMessage = message
                        )
                    }
                }
                .onFailure { exception ->
                    updateUiState {
                        copy(
                            isLoading = false,
                            errorMessage = "Failed to delete leet: ${exception.message}"
                        )
                    }
                }
        }
    }

    /**
     * Copies output text to clipboard
     */
    fun copyToClipboard() {
        val text = outputText.value
        if (text.isNotEmpty()) {
            updateUiState { copy(successMessage = "Copied to clipboard") }
        } else {
            updateUiState { copy(errorMessage = "No text to copy") }
        }
    }

    /**
     * Clears current input text
     */
    fun clearInput() {
        _inputText.value = ""
    }

    /**
     * Generates a preview for a leet option
     */
    fun generatePreview(leetOption: LeetOption, sampleText: String = "Hello"): String {
        val mode = when (leetOption.mode) {
            com.beigel.leetSpeak_Generator.manager.LeetManager.MODE_SIMPLE -> LeetTranslator.TranslationMode.SIMPLE
            com.beigel.leetSpeak_Generator.manager.LeetManager.MODE_EXTENDED -> LeetTranslator.TranslationMode.EXTENDED
            com.beigel.leetSpeak_Generator.manager.LeetManager.MODE_CUSTOM -> LeetTranslator.TranslationMode.CUSTOM
            else -> LeetTranslator.TranslationMode.SIMPLE
        }

        val leet = if (leetOption.isCustom && leetOption.customIndex >= 0) {
            leets.value.getOrNull(leetOption.customIndex)
        } else null

        return LeetTranslator.translate(sampleText, mode, leet)
    }

    /**
     * Handles UI events
     */
    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.UpdateInput -> updateInputText(intent.text)
            is MainIntent.ChangeMode -> changeMode(intent.leetOption)
            is MainIntent.ToggleFavorite -> toggleFavorite(intent.leetOption)
            is MainIntent.CreateLeet -> createLeet(
                intent.name,
                intent.iconResId,
                intent.useExtendedDefaults
            )
            is MainIntent.UpdateLeet -> updateLeet(intent.index, intent.leet)
            is MainIntent.DeleteLeet -> deleteLeet(intent.index)
            is MainIntent.CopyToClipboard -> copyToClipboard()
            is MainIntent.ClearInput -> clearInput()
            is MainIntent.ClearError -> clearError()
            is MainIntent.ClearSuccess -> clearSuccess()
        }
    }

    /**
     * Clears error message
     */
    fun clearError() {
        updateUiState { copy(errorMessage = null) }
    }

    /**
     * Clears success message
     */
    fun clearSuccess() {
        updateUiState { copy(successMessage = null) }
    }

    /**
     * Updates UI state safely
     */
    private fun updateUiState(update: MainUiState.() -> MainUiState) {
        _uiState.value = _uiState.value.update()
    }

    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
    }
}

/**
 * UI state data class
 */
data class MainUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedLeetOption: LeetOption? = null,
    val showBottomSheet: Boolean = false
)

/**
 * Intent sealed class für UI Events
 */
sealed class MainIntent {
    data class UpdateInput(val text: String) : MainIntent()
    data class ChangeMode(val leetOption: LeetOption) : MainIntent()
    data class ToggleFavorite(val leetOption: LeetOption) : MainIntent()
    data class CreateLeet(
        val name: String,
        val iconResId: Int,
        val useExtendedDefaults: Boolean = false
    ) : MainIntent()
    data class UpdateLeet(val index: Int, val leet: CustomLeet) : MainIntent()
    data class DeleteLeet(val index: Int) : MainIntent()
    object CopyToClipboard : MainIntent()
    object ClearInput : MainIntent()
    object ClearError : MainIntent()
    object ClearSuccess : MainIntent()
}