package com.beigel.leetSpeak_Generator.viewmodel

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.data.ThemePreferences
import com.beigel.leetSpeak_Generator.data.WhatsNewPreferences
import com.beigel.leetSpeak_Generator.data.VersionInfo
import com.beigel.leetSpeak_Generator.domain.usecase.leet.LeetManagerUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.translation.TranslationManagerUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.ui.UiManagerUseCase
import com.beigel.leetSpeak_Generator.manager.LeetManager
import com.beigel.leetSpeak_Generator.presentation.intent.MainIntent
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val translationManager: TranslationManagerUseCase,
    private val leetManager: LeetManagerUseCase,
    private val uiManager: UiManagerUseCase,
    private val repository: LeetRepository,
    private val themePreferences: ThemePreferences,
    private val whatsNewPreferences: WhatsNewPreferences
) : ViewModel() {

    // Core UI State
    val inputText = uiManager.inputText
    val isReverseMode = uiManager.isReverseMode
    val currentMode = uiManager.currentMode
    val uiState = uiManager.uiState

    // Repository State
    val leets = repository.leets
    val currentLeet = repository.currentLeet
    val hasLeets = repository.hasLeets

    // Theme State
    val defaultViewExpanded: StateFlow<Boolean> = themePreferences.defaultViewExpanded
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val themeMode: StateFlow<String> = themePreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemePreferences.THEME_SYSTEM)

    // What's New State
    val shouldShowWhatsNew: StateFlow<Boolean> = whatsNewPreferences.shouldShowWhatsNew
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentVersionInfo: VersionInfo = whatsNewPreferences.getCurrentVersionInfo()

    val isFirstLaunch: StateFlow<Boolean> = whatsNewPreferences.isFirstLaunch
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Leet Options with Selection Logic
    val leetOptions: StateFlow<List<LeetOption>> = combine(
        leetManager.getLeetOptions(),
        currentMode,
        repository.currentLeetIndex
    ) { options, currentTranslationMode, currentLeetIndex ->
        options.map { option ->
            val isSelected = when (option.mode) {
                LeetManager.MODE_SIMPLE -> currentTranslationMode == LeetTranslator.TranslationMode.SIMPLE
                LeetManager.MODE_EXTENDED -> currentTranslationMode == LeetTranslator.TranslationMode.EXTENDED
                LeetManager.MODE_CUSTOM -> currentTranslationMode == LeetTranslator.TranslationMode.CUSTOM && option.customIndex == currentLeetIndex
                else -> false
            }
            option.copy(isSelected = isSelected)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteLeetOptions: StateFlow<List<LeetOption>> = combine(
        leetManager.getFavoriteLeetOptions(),
        currentMode,
        repository.currentLeetIndex
    ) { options, currentTranslationMode, currentLeetIndex ->
        options.map { option ->
            val isSelected = when (option.mode) {
                LeetManager.MODE_SIMPLE -> currentTranslationMode == LeetTranslator.TranslationMode.SIMPLE
                LeetManager.MODE_EXTENDED -> currentTranslationMode == LeetTranslator.TranslationMode.EXTENDED
                LeetManager.MODE_CUSTOM -> currentTranslationMode == LeetTranslator.TranslationMode.CUSTOM && option.customIndex == currentLeetIndex
                else -> false
            }
            option.copy(isSelected = isSelected)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Computed Properties
    val outputText: StateFlow<String> = combine(
        inputText, currentMode, currentLeet, isReverseMode
    ) { input, mode, leet, reverse ->
        translationManager.translate(input, mode, leet, reverse)
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")

    val isInputLikelyLeetspeak: StateFlow<Boolean> = inputText.map { text ->
        translationManager.isLikelyLeetspeak(text)
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val translationStats = combine(
        inputText, currentMode, currentLeet
    ) { input, mode, leet ->
        translationManager.analyzeTranslation(input, mode, leet)
    }.stateIn(viewModelScope, SharingStarted.Lazily,
        translationManager.analyzeTranslation("", currentMode.value, null))

    val currentModeDisplayName: StateFlow<String> = combine(
        currentMode, currentLeet, isReverseMode
    ) { mode, leet, reverse ->
        uiManager.generateModeDisplayName(leet)
    }.stateIn(viewModelScope, SharingStarted.Lazily, "Simple Leet")

    val shouldShowOutput: StateFlow<Boolean> = outputText.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        initializeFavoriteLeet()
    }

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.UpdateInput -> uiManager.updateInputText(intent.text)
            is MainIntent.ChangeMode -> changeMode(intent.leetOption)
            is MainIntent.ToggleFavorite -> toggleFavorite(intent.leetOption)
            is MainIntent.CreateLeet -> createLeet(intent.name, intent.icon, intent.useExtendedDefaults)
            is MainIntent.UpdateLeet -> updateLeet(intent.index, intent.leet)
            is MainIntent.DeleteLeet -> deleteLeet(intent.index)
            is MainIntent.CopyToClipboard -> copyToClipboard()
            is MainIntent.ClearInput -> uiManager.clearInput()
            is MainIntent.ClearError -> uiManager.clearError()
            is MainIntent.ClearSuccess -> uiManager.clearSuccess()
            is MainIntent.ToggleReverseMode -> toggleReverseMode()
            is MainIntent.DismissWhatsNew -> { /* Auto-handled by dialog */ }
            is MainIntent.MarkWhatsNewAsShown -> markWhatsNewAsShown()
            is MainIntent.ResetWhatsNewForTesting -> resetWhatsNewForTesting()
            is MainIntent.ForceShowWhatsNew -> forceShowWhatsNew()
        }
    }

    private fun initializeFavoriteLeet() {
        viewModelScope.launch {
            leetManager.loadFavoriteLeet()
                .onSuccess { result ->
                    when (result) {
                        is LeetRepository.FavoriteLeetResult.Simple ->
                            uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE)
                        is LeetRepository.FavoriteLeetResult.Extended ->
                            uiManager.setTranslationMode(LeetTranslator.TranslationMode.EXTENDED)
                        is LeetRepository.FavoriteLeetResult.Custom -> {
                            uiManager.setTranslationMode(LeetTranslator.TranslationMode.CUSTOM)
                            repository.setCurrentLeetIndex(result.customIndex)
                        }
                    }
                }
                .onFailure { exception ->
                    uiManager.setError("Failed to load favorite leet: ${exception.message}")
                }
        }
    }

    private fun changeMode(leetOption: LeetOption) {
        viewModelScope.launch {
            when (leetOption.mode) {
                LeetManager.MODE_SIMPLE ->
                    uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE)
                LeetManager.MODE_EXTENDED ->
                    uiManager.setTranslationMode(LeetTranslator.TranslationMode.EXTENDED)
                LeetManager.MODE_CUSTOM -> {
                    uiManager.setTranslationMode(LeetTranslator.TranslationMode.CUSTOM)
                    if (leetOption.customIndex >= 0) {
                        repository.setCurrentLeetIndex(leetOption.customIndex)
                    }
                }
            }

            leetManager.changeMode(leetOption)
                .onFailure { exception ->
                    uiManager.setError("Failed to change mode: ${exception.message}")
                }
        }
    }

    private fun toggleFavorite(leetOption: LeetOption) {
        viewModelScope.launch {
            leetManager.toggleFavorite(leetOption)
                .onSuccess { result ->
                    val message = if (result.isNowFavorite) "Added to favorites" else "Removed from favorites"
                    uiManager.setSuccess(message)
                }
                .onFailure { exception ->
                    uiManager.setError("Failed to toggle favorite: ${exception.message}")
                }
        }
    }

    private fun createLeet(name: String, iconImageVector: ImageVector, useExtendedDefaults: Boolean) {
        viewModelScope.launch {
            uiManager.setLoading(true)

            leetManager.createLeet(name, iconImageVector, useExtendedDefaults)
                .onSuccess { leet ->
                    uiManager.setTranslationMode(LeetTranslator.TranslationMode.CUSTOM)
                    uiManager.setSuccess("Leet '${leet.name}' created successfully")
                }
                .onFailure { exception ->
                    uiManager.setError("Failed to create leet: ${exception.message}")
                }
        }
    }

    private fun updateLeet(index: Int, leet: CustomLeet) {
        viewModelScope.launch {
            uiManager.setLoading(true)

            leetManager.updateLeet(index, leet)
                .onSuccess {
                    uiManager.setSuccess("Leet '${leet.name}' updated successfully")
                }
                .onFailure { exception ->
                    uiManager.setError("Failed to update leet: ${exception.message}")
                }
        }
    }

    private fun deleteLeet(index: Int) {
        viewModelScope.launch {
            uiManager.setLoading(true)

            leetManager.deleteLeet(index)
                .onSuccess { result ->
                    val message = when {
                        result.wasLastLeet -> "Last leet deleted, switched to Simple mode"
                        result.wasFavorite -> "Favorite leet deleted"
                        else -> "Leet deleted successfully"
                    }

                    if (result.wasLastLeet) {
                        uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE)
                    }

                    uiManager.setSuccess(message)
                }
                .onFailure { exception ->
                    uiManager.setError("Failed to delete leet: ${exception.message}")
                }
        }
    }

    private fun toggleReverseMode() {
        val currentOutputBeforeToggle = outputText.value

        uiManager.toggleReverseMode()

        if (currentOutputBeforeToggle.isNotEmpty()) {
            uiManager.updateInputText(currentOutputBeforeToggle)
        }
    }

    private fun copyToClipboard() {
        val text = outputText.value
        if (text.isNotEmpty()) {
            uiManager.setSuccess("Copied to clipboard")
        } else {
            uiManager.setError("No text to copy")
        }
    }

    private fun markWhatsNewAsShown() {
        viewModelScope.launch {
            whatsNewPreferences.markCurrentVersionAsShown()
        }
    }

    private fun resetWhatsNewForTesting() {
        viewModelScope.launch {
            whatsNewPreferences.resetForTesting()
            uiManager.setSuccess("What's New Dialog reset - wird beim nächsten Start angezeigt")
        }
    }

    private fun forceShowWhatsNew() {
        viewModelScope.launch {
            whatsNewPreferences.forceShowNextTime()
            uiManager.setSuccess("What's New Dialog wird beim nächsten Start angezeigt")
        }
    }

    fun generatePreview(leetOption: LeetOption, sampleText: String = "Hello"): String {
        val mode = when (leetOption.mode) {
            LeetManager.MODE_SIMPLE -> LeetTranslator.TranslationMode.SIMPLE
            LeetManager.MODE_EXTENDED -> LeetTranslator.TranslationMode.EXTENDED
            LeetManager.MODE_CUSTOM -> LeetTranslator.TranslationMode.CUSTOM
            else -> LeetTranslator.TranslationMode.SIMPLE
        }

        val leet = if (leetOption.isCustom && leetOption.customIndex >= 0) {
            leets.value.getOrNull(leetOption.customIndex)
        } else null

        return translationManager.generatePreview(mode, leet, sampleText)
    }

    // Legacy Support
    fun updateInputText(text: String) = uiManager.updateInputText(text)
    fun clearInput() = uiManager.clearInput()

    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
    }
}