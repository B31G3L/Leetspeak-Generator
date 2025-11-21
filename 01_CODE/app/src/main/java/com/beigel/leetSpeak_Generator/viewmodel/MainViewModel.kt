package com.beigel.leetSpeak_Generator.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beigel.leetSpeak_Generator.R
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
import com.beigel.leetSpeak_Generator.review.InAppReviewManager
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.beigel.leetSpeak_Generator.ui.theme.AppTheme


@HiltViewModel
class MainViewModel @Inject constructor(
    private val application: Application,
    private val translationManager: TranslationManagerUseCase,
    private val leetManager: LeetManagerUseCase,
    private val uiManager: UiManagerUseCase,
    private val repository: LeetRepository,
    private val themePreferences: ThemePreferences,
    private val whatsNewPreferences: WhatsNewPreferences,
    private val inAppReviewManager: InAppReviewManager
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
    val appTheme: StateFlow<AppTheme> = themePreferences.appTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.PLANIT)
    val isFirstLaunch: StateFlow<Boolean> = whatsNewPreferences.isFirstLaunch
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Copy behavior preferences
    val clearInputAfterCopy: StateFlow<Boolean> = themePreferences.clearInputAfterCopy
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val askBeforeClear: StateFlow<Boolean> = themePreferences.askBeforeClear
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // FIXED: Leet Options with proper initialization
    private val _isInitialized = MutableStateFlow(false)

    val leetOptions: StateFlow<List<LeetOption>> = combine(
        leetManager.getLeetOptions(),
        currentMode,
        repository.currentLeetIndex,
        _isInitialized
    ) { options, currentTranslationMode, currentLeetIndex, isInitialized ->
        if (!isInitialized) return@combine emptyList()

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
        repository.currentLeetIndex,
        _isInitialized
    ) { options, currentTranslationMode, currentLeetIndex, isInitialized ->
        if (!isInitialized) return@combine emptyList()

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

    // NEU: Review Stats State
    val reviewStats: StateFlow<InAppReviewManager.ReviewStats> =
        inAppReviewManager.getReviewStats()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                InAppReviewManager.ReviewStats(0, 0, 0, 0)
            )

    // NEU: StateFlow für Review-Request
    private val _shouldRequestReview = MutableStateFlow(false)
    val shouldRequestReview: StateFlow<Boolean> = _shouldRequestReview.asStateFlow()

    // Add this StateFlow
    private val _showClearInputDialog = MutableStateFlow(false)
    val showClearInputDialog: StateFlow<Boolean> = _showClearInputDialog.asStateFlow()

    init {
        initializeFavoriteLeet()
        trackAppStart() // NEU: App-Start tracken
    }

    /**
     * NEU: Trackt den App-Start und prüft Review-Bedingungen
     */
    private fun trackAppStart() {
        viewModelScope.launch {
            // App-Start zählen
            inAppReviewManager.incrementAppStartCount()

            // Prüfe ob Review angezeigt werden sollte
            if (inAppReviewManager.shouldShowReview()) {
                android.util.Log.d("MainViewModel", "✅ Review conditions met - triggering review request")
                _shouldRequestReview.value = true
            } else {
                android.util.Log.d("MainViewModel", "ℹ️ Review conditions not met yet")
            }
        }
    }

    /**
     * Lädt das favorisierte Leet beim App-Start und wählt es automatisch aus
     *
     * Diese Funktion stellt sicher, dass das vom Nutzer markierte Favorit-Leet
     * nach einem App-Neustart automatisch aus SharedPreferences geladen und ausgewählt wird.
     *
     * WICHTIG: Die Funktion wartet automatisch, bis alle Daten aus SharedPreferences
     * geladen wurden (verhindert Race Conditions).
     */
    private fun initializeFavoriteLeet() {
        viewModelScope.launch {
            try {
                android.util.Log.d("MainViewModel", "🔄 Starting favorite leet initialization (waiting for data load)...")

                // loadFavoriteLeet() wartet automatisch bis SharedPreferences geladen sind
                leetManager.loadFavoriteLeet()
                    .onSuccess { result ->
                        android.util.Log.d("MainViewModel", "✅ Favorite leet loaded from SharedPreferences: $result")

                        when (result) {
                            is LeetRepository.FavoriteLeetResult.Simple -> {
                                android.util.Log.d("MainViewModel", "📌 Auto-selecting favorite: Simple Leet")
                                uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE)
                                android.util.Log.d("MainViewModel", "✅ Mode set to SIMPLE")

                                // Kurze Verzögerung um sicherzustellen, dass State propagiert wurde
                                kotlinx.coroutines.delay(100)
                                _isInitialized.value = true
                                android.util.Log.d("MainViewModel", "✅ Initialization complete (SIMPLE) - Favorite auto-selected ✨")
                            }

                            is LeetRepository.FavoriteLeetResult.Extended -> {
                                android.util.Log.d("MainViewModel", "📌 Auto-selecting favorite: Extended Leet")
                                uiManager.setTranslationMode(LeetTranslator.TranslationMode.EXTENDED)
                                android.util.Log.d("MainViewModel", "✅ Mode set to EXTENDED")

                                // Kurze Verzögerung um sicherzustellen, dass State propagiert wurde
                                kotlinx.coroutines.delay(100)
                                _isInitialized.value = true
                                android.util.Log.d("MainViewModel", "✅ Initialization complete (EXTENDED) - Favorite auto-selected ✨")
                            }

                            is LeetRepository.FavoriteLeetResult.Custom -> {
                                android.util.Log.d("MainViewModel", "📌 Auto-selecting favorite: Custom Leet '${result.leet.name}' (index: ${result.customIndex})")

                                // Erst den Index setzen, dann den Modus
                                repository.setCurrentLeetIndex(result.customIndex)
                                    .onSuccess {
                                        android.util.Log.d("MainViewModel", "✅ Custom leet index set: ${result.customIndex}")

                                        viewModelScope.launch {
                                            // Verzögerung um sicherzustellen, dass der Index propagiert wurde
                                            kotlinx.coroutines.delay(100)

                                            uiManager.setTranslationMode(LeetTranslator.TranslationMode.CUSTOM)
                                            android.util.Log.d("MainViewModel", "✅ Mode set to CUSTOM")

                                            // Weitere Verzögerung für Custom Mode
                                            kotlinx.coroutines.delay(150)

                                            _isInitialized.value = true
                                            android.util.Log.d("MainViewModel", "✅ Initialization complete (CUSTOM) - Favorite '${result.leet.name}' auto-selected ✨")
                                        }
                                    }
                                    .onFailure { exception ->
                                        android.util.Log.e("MainViewModel", "❌ Failed to set custom leet index", exception)
                                        android.util.Log.w("MainViewModel", "⚠️ Falling back to SIMPLE mode")

                                        uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE)
                                        kotlinx.coroutines.delay(100)
                                        _isInitialized.value = true
                                    }
                            }
                        }
                    }
                    .onFailure { exception ->
                        android.util.Log.e("MainViewModel", "❌ Failed to load favorite leet from SharedPreferences", exception)
                        android.util.Log.w("MainViewModel", "⚠️ Falling back to SIMPLE mode")

                        uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE)
                        kotlinx.coroutines.delay(100)
                        _isInitialized.value = true
                    }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "❌ Exception during initialization", e)
                android.util.Log.w("MainViewModel", "⚠️ Falling back to SIMPLE mode")

                uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE)
                kotlinx.coroutines.delay(100)
                _isInitialized.value = true
            }
        }
    }

    private fun createLeet(
        name: String,
        iconImageVector: ImageVector,
        useExtendedDefaults: Boolean,
        customTranslations: Map<String, String>? = null
    ) {
        viewModelScope.launch {
            uiManager.setLoading(true)

            leetManager.createLeet(name, iconImageVector, useExtendedDefaults, customTranslations)
                .onSuccess { leet ->
                    uiManager.setTranslationMode(LeetTranslator.TranslationMode.CUSTOM)
                    uiManager.setSuccess(application.getString(R.string.success_leet_created, leet.name))
                }
                .onFailure { exception ->
                    uiManager.setError(application.getString(R.string.error_create_leet, exception.message ?: ""))
                }
        }
    }

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.UpdateInput -> uiManager.updateInputText(intent.text)
            is MainIntent.ChangeMode -> changeMode(intent.leetOption)
            is MainIntent.ToggleFavorite -> toggleFavorite(intent.leetOption)
            is MainIntent.CreateLeet -> createLeet(
                intent.name,
                intent.icon,
                intent.useExtendedDefaults,
                intent.customTranslations
            )
            is MainIntent.UpdateLeet -> updateLeet(intent.index, intent.leet)
            is MainIntent.DeleteLeet -> deleteLeet(intent.index)
            is MainIntent.CopyToClipboard -> copyToClipboard()
            is MainIntent.ClearInput -> uiManager.clearInput()
            is MainIntent.ClearError -> uiManager.clearError()
            is MainIntent.ClearSuccess -> uiManager.clearSuccess()
            is MainIntent.ToggleReverseMode -> toggleReverseMode()
            is MainIntent.DismissWhatsNew -> { }
            is MainIntent.MarkWhatsNewAsShown -> markWhatsNewAsShown()
            is MainIntent.ResetWhatsNewForTesting -> resetWhatsNewForTesting()
            is MainIntent.ForceShowWhatsNew -> forceShowWhatsNew()
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
                    uiManager.setError(application.getString(R.string.error_change_mode, exception.message ?: ""))
                }
        }
    }

    private fun toggleFavorite(leetOption: LeetOption) {
        viewModelScope.launch {
            leetManager.toggleFavorite(leetOption)
                .onSuccess { result ->
                    val message = if (result.isNowFavorite)
                        application.getString(R.string.success_added_favorite)
                    else
                        application.getString(R.string.success_removed_favorite)
                    uiManager.setSuccess(message)
                }
                .onFailure { exception ->
                    uiManager.setError(application.getString(R.string.error_toggle_favorite, exception.message ?: ""))
                }
        }
    }

    private fun updateLeet(index: Int, leet: CustomLeet) {
        viewModelScope.launch {
            uiManager.setLoading(true)

            leetManager.updateLeet(index, leet)
                .onSuccess {
                    uiManager.setSuccess(application.getString(R.string.success_leet_updated, leet.name))
                }
                .onFailure { exception ->
                    uiManager.setError(application.getString(R.string.error_update_leet, exception.message ?: ""))
                }
        }
    }

    private fun deleteLeet(index: Int) {
        viewModelScope.launch {
            uiManager.setLoading(true)

            leetManager.deleteLeet(index)
                .onSuccess { result ->
                    val message = when {
                        result.wasLastLeet -> application.getString(R.string.info_switched_to_simple)
                        result.wasFavorite -> application.getString(R.string.info_favorite_deleted)
                        else -> application.getString(R.string.success_leet_deleted)
                    }

                    if (result.wasLastLeet) {
                        uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE)
                    }

                    uiManager.setSuccess(message)
                }
                .onFailure { exception ->
                    uiManager.setError(application.getString(R.string.error_delete_leet, exception.message ?: ""))
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
            uiManager.setSuccess(application.getString(R.string.success_copied_to_clipboard))

            // Trigger clear input dialog/action
            if (clearInputAfterCopy.value) {
                if (askBeforeClear.value) {
                    // Show dialog - handled in UI
                    _showClearInputDialog.value = true
                } else {
                    // Clear automatically
                    uiManager.clearInput()
                }
            }
        } else {
            uiManager.setError(application.getString(R.string.error_no_text_to_copy))
        }
    }

    fun dismissClearInputDialog() {
        _showClearInputDialog.value = false
    }

    fun confirmClearInput(dontAskAgain: Boolean) {
        viewModelScope.launch {
            if (dontAskAgain) {
                themePreferences.setAskBeforeClear(false)
            }
            uiManager.clearInput()
            _showClearInputDialog.value = false
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
            uiManager.setSuccess(application.getString(R.string.debug_whatsnew_reset_shown))
        }
    }

    private fun forceShowWhatsNew() {
        viewModelScope.launch {
            whatsNewPreferences.forceShowNextTime()
            uiManager.setSuccess(application.getString(R.string.debug_whatsnew_will_show))
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

    // NEU: Review wurde angezeigt/abgelehnt
    fun onReviewHandled() {
        _shouldRequestReview.value = false
    }

    // NEU: Review Reset für Testing
    fun resetReviewForTesting() {
        viewModelScope.launch {
            inAppReviewManager.resetForTesting()
            uiManager.setSuccess(application.getString(R.string.debug_review_data_reset))
        }
    }

    fun updateInputText(text: String) {
        uiManager.updateInputText(text)
    }

    fun clearInput() = uiManager.clearInput()

    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
    }
}