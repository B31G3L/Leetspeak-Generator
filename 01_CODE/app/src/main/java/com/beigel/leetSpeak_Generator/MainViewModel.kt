package com.beigel.leetSpeak_Generator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val repository: ProfileRepository
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
    val profiles = repository.profiles
    val currentProfile = repository.currentProfile
    val hasProfiles = repository.hasProfiles
    val leetOptions = repository.getLeetOptions()
    val favoriteLeetOptions = repository.getFavoriteLeetOptions()

    // Profile Index State
    val currentProfileIndex = repository.currentProfileIndex

    // Computed output text
    val outputText: StateFlow<String> = combine(
        inputText,
        currentMode,
        currentProfile
    ) { input, mode, profile ->
        if (input.isEmpty()) {
            ""
        } else {
            LeetTranslator.translate(input, mode, profile)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")

    // Translation statistics
    val translationStats: StateFlow<LeetTranslator.TranslationStats> = combine(
        inputText,
        currentMode,
        currentProfile
    ) { input, mode, profile ->
        if (input.isEmpty()) {
            LeetTranslator.TranslationStats.empty()
        } else {
            LeetTranslator.analyzeTranslation(input, mode, profile)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, LeetTranslator.TranslationStats.empty())

    // Current mode display name
    val currentModeDisplayName: StateFlow<String> = combine(
        currentMode,
        currentProfile
    ) { mode, profile ->
        when (mode) {
            LeetTranslator.TranslationMode.SIMPLE -> "Simple Leet"
            LeetTranslator.TranslationMode.EXTENDED -> "Extended Leet"
            LeetTranslator.TranslationMode.CUSTOM -> profile?.name ?: "Custom Leet"
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
            repository.loadFavoriteMode()
                .onSuccess { result ->
                    when (result) {
                        is ProfileRepository.FavoriteModeResult.Simple -> {
                            _currentMode.value = LeetTranslator.TranslationMode.SIMPLE
                        }
                        is ProfileRepository.FavoriteModeResult.Extended -> {
                            _currentMode.value = LeetTranslator.TranslationMode.EXTENDED
                        }
                        is ProfileRepository.FavoriteModeResult.Custom -> {
                            _currentMode.value = LeetTranslator.TranslationMode.CUSTOM
                            repository.setCurrentProfileIndex(result.customIndex)
                        }
                    }
                }
                .onError { _, message ->
                    updateUiState { copy(errorMessage = message) }
                }
        }
    }

    /**
     * Handles input text changes with debouncing
     */
    fun updateInputText(text: String) {
        _inputText.value = text

        // Cancel previous debounce job
        debounceJob?.cancel()

        // Debounce for performance with large texts
        debounceJob = viewModelScope.launch {
            kotlinx.coroutines.delay(100) // 100ms debounce
            // Translation happens automatically via combine flow
        }
    }

    /**
     * Changes the current leet mode
     */
    fun changeMode(leetOption: LeetOption) {
        viewModelScope.launch {
            try {
                when (leetOption.mode) {
                    LeetManager.MODE_SIMPLE -> {
                        _currentMode.value = LeetTranslator.TranslationMode.SIMPLE
                    }
                    LeetManager.MODE_EXTENDED -> {
                        _currentMode.value = LeetTranslator.TranslationMode.EXTENDED
                    }
                    LeetManager.MODE_CUSTOM -> {
                        if (leetOption.isCustom && leetOption.customIndex >= 0) {
                            repository.setCurrentProfileIndex(leetOption.customIndex)
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
                .onError { _, message ->
                    updateUiState { copy(errorMessage = message) }
                }
        }
    }

    /**
     * Creates a new profile
     */
    fun createProfile(name: String, iconResId: Int, useExtendedDefaults: Boolean = false) {
        viewModelScope.launch {
            updateUiState { copy(isLoading = true) }

            val result = if (useExtendedDefaults) {
                repository.createProfileWithExtendedDefaults(name, iconResId)
            } else {
                repository.createProfileWithSimpleDefaults(name, iconResId)
            }

            result
                .onSuccess { profile ->
                    _currentMode.value = LeetTranslator.TranslationMode.CUSTOM
                    updateUiState {
                        copy(
                            isLoading = false,
                            successMessage = "Profile '${profile.name}' created successfully"
                        )
                    }
                }
                .onError { _, message ->
                    updateUiState {
                        copy(
                            isLoading = false,
                            errorMessage = message
                        )
                    }
                }
        }
    }

    /**
     * Updates an existing profile
     */
    fun updateProfile(index: Int, profile: CustomLeet) {
        viewModelScope.launch {
            updateUiState { copy(isLoading = true) }

            repository.updateProfile(index, profile)
                .onSuccess {
                    updateUiState {
                        copy(
                            isLoading = false,
                            successMessage = "Profile '${profile.name}' updated successfully"
                        )
                    }
                }
                .onError { _, message ->
                    updateUiState {
                        copy(
                            isLoading = false,
                            errorMessage = message
                        )
                    }
                }
        }
    }

    /**
     * Deletes a profile
     */
    fun deleteProfile(index: Int) {
        viewModelScope.launch {
            updateUiState { copy(isLoading = true) }

            repository.deleteProfile(index)
                .onSuccess { result ->
                    val message = if (result.wasLastProfile) {
                        "Last profile deleted, switched to Simple mode"
                    } else if (result.wasFavorite) {
                        "Favorite profile deleted"
                    } else {
                        "Profile deleted successfully"
                    }

                    // Switch to simple mode if no profiles left
                    if (result.wasLastProfile) {
                        _currentMode.value = LeetTranslator.TranslationMode.SIMPLE
                    }

                    updateUiState {
                        copy(
                            isLoading = false,
                            successMessage = message
                        )
                    }
                }
                .onError { _, message ->
                    updateUiState {
                        copy(
                            isLoading = false,
                            errorMessage = message
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
            LeetManager.MODE_SIMPLE -> LeetTranslator.TranslationMode.SIMPLE
            LeetManager.MODE_EXTENDED -> LeetTranslator.TranslationMode.EXTENDED
            LeetManager.MODE_CUSTOM -> LeetTranslator.TranslationMode.CUSTOM
            else -> LeetTranslator.TranslationMode.SIMPLE
        }

        val profile = if (leetOption.isCustom && leetOption.customIndex >= 0) {
            profiles.value.getOrNull(leetOption.customIndex)
        } else null

        return LeetTranslator.translate(sampleText, mode, profile)
    }

    /**
     * Handles UI events
     */
    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.UpdateInput -> updateInputText(intent.text)
            is MainIntent.ChangeMode -> changeMode(intent.leetOption)
            is MainIntent.ToggleFavorite -> toggleFavorite(intent.leetOption)
            is MainIntent.CreateProfile -> createProfile(
                intent.name,
                intent.iconResId,
                intent.useExtendedDefaults
            )
            is MainIntent.UpdateProfile -> updateProfile(intent.index, intent.profile)
            is MainIntent.DeleteProfile -> deleteProfile(intent.index)
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
    data class CreateProfile(
        val name: String,
        val iconResId: Int,
        val useExtendedDefaults: Boolean = false
    ) : MainIntent()
    data class UpdateProfile(val index: Int, val profile: CustomLeet) : MainIntent()
    data class DeleteProfile(val index: Int) : MainIntent()
    object CopyToClipboard : MainIntent()
    object ClearInput : MainIntent()
    object ClearError : MainIntent()
    object ClearSuccess : MainIntent()
}