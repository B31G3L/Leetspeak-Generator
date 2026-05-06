package com.beigel.leetSpeak_Generator.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.data.ThemePreferences
import com.beigel.leetSpeak_Generator.data.VersionInfo
import com.beigel.leetSpeak_Generator.data.WhatsNewPreferences
import com.beigel.leetSpeak_Generator.domain.usecase.leet.LeetManagerUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.translation.TranslationManagerUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.ui.UiManagerUseCase
import com.beigel.leetSpeak_Generator.manager.LeetManager
import com.beigel.leetSpeak_Generator.presentation.intent.MainIntent
import com.beigel.leetSpeak_Generator.presentation.intent.PendingDelete
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import com.beigel.leetSpeak_Generator.review.InAppReviewManager
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import com.beigel.leetSpeak_Generator.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    val inputText = uiManager.inputText
    val isReverseMode = uiManager.isReverseMode
    val currentMode = uiManager.currentMode
    val uiState = uiManager.uiState
    val leets = repository.leets
    val currentLeet = repository.currentLeet
    val hasLeets = repository.hasLeets

    val defaultViewExpanded: StateFlow<Boolean> = themePreferences.defaultViewExpanded
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val themeMode: StateFlow<String> = themePreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemePreferences.THEME_SYSTEM)

    val shouldShowWhatsNew: StateFlow<Boolean> = whatsNewPreferences.shouldShowWhatsNew
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentVersionInfo: VersionInfo = whatsNewPreferences.getCurrentVersionInfo()

    val appTheme: StateFlow<AppTheme> = themePreferences.appTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.PLANIT)

    val isFirstLaunch: StateFlow<Boolean> = whatsNewPreferences.isFirstLaunch
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val clearInputAfterCopy: StateFlow<Boolean> = themePreferences.clearInputAfterCopy
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val askBeforeClear: StateFlow<Boolean> = themePreferences.askBeforeClear
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _isInitialized = MutableStateFlow(false)

    val leetOptions: StateFlow<List<LeetOption>> = combine(
        leetManager.getLeetOptions(), currentMode, repository.currentLeetIndex, _isInitialized
    ) { options, currentTranslationMode, currentLeetIndex, isInitialized ->
        if (!isInitialized) return@combine emptyList()
        options.map { option ->
            option.copy(isSelected = when (option.mode) {
                LeetManager.MODE_SIMPLE -> currentTranslationMode == LeetTranslator.TranslationMode.SIMPLE
                LeetManager.MODE_EXTENDED -> currentTranslationMode == LeetTranslator.TranslationMode.EXTENDED
                LeetManager.MODE_CUSTOM -> currentTranslationMode == LeetTranslator.TranslationMode.CUSTOM
                        && option.customIndex == currentLeetIndex
                else -> false
            })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteLeetOptions: StateFlow<List<LeetOption>> = combine(
        leetManager.getFavoriteLeetOptions(), currentMode, repository.currentLeetIndex, _isInitialized
    ) { options, currentTranslationMode, currentLeetIndex, isInitialized ->
        if (!isInitialized) return@combine emptyList()
        options.map { option ->
            option.copy(isSelected = when (option.mode) {
                LeetManager.MODE_SIMPLE -> currentTranslationMode == LeetTranslator.TranslationMode.SIMPLE
                LeetManager.MODE_EXTENDED -> currentTranslationMode == LeetTranslator.TranslationMode.EXTENDED
                LeetManager.MODE_CUSTOM -> currentTranslationMode == LeetTranslator.TranslationMode.CUSTOM
                        && option.customIndex == currentLeetIndex
                else -> false
            })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val outputText: StateFlow<String> = combine(
        inputText, currentMode, currentLeet, isReverseMode
    ) { input, mode, leet, reverse ->
        translationManager.translate(input, mode, leet, reverse)
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")

    val isInputLikelyLeetspeak: StateFlow<Boolean> = inputText.map {
        translationManager.isLikelyLeetspeak(it)
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val translationStats = combine(inputText, currentMode, currentLeet) { input, mode, leet ->
        translationManager.analyzeTranslation(input, mode, leet)
    }.stateIn(viewModelScope, SharingStarted.Lazily,
        translationManager.analyzeTranslation("", currentMode.value, null))

    val currentModeDisplayName: StateFlow<String> = combine(
        currentMode, currentLeet, isReverseMode
    ) { _, leet, _ ->
        uiManager.generateModeDisplayName(leet)
    }.stateIn(viewModelScope, SharingStarted.Lazily, "Simple Leet")

    val shouldShowOutput: StateFlow<Boolean> = outputText.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val reviewStats: StateFlow<InAppReviewManager.ReviewStats> =
        inAppReviewManager.getReviewStats()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
                InAppReviewManager.ReviewStats(0, 0, 0, 0))

    private val _shouldRequestReview = MutableStateFlow(false)
    val shouldRequestReview: StateFlow<Boolean> = _shouldRequestReview.asStateFlow()

    private val _showClearInputDialog = MutableStateFlow(false)
    val showClearInputDialog: StateFlow<Boolean> = _showClearInputDialog.asStateFlow()

    /**
     * NEU: Hält den zuletzt gelöschten Leet für Undo bereit.
     * Wird nach Undo oder Snackbar-Timeout auf null gesetzt.
     */
    private val _pendingDelete = MutableStateFlow<PendingDelete?>(null)
    val pendingDelete: StateFlow<PendingDelete?> = _pendingDelete.asStateFlow()

    init {
        initializeFavoriteLeet()
        trackAppStart()
    }

    private fun trackAppStart() {
        viewModelScope.launch {
            inAppReviewManager.incrementAppStartCount()
            if (inAppReviewManager.shouldShowReview()) {
                _shouldRequestReview.value = true
            }
        }
    }

    private fun initializeFavoriteLeet() {
        viewModelScope.launch {
            try {
                leetManager.loadFavoriteLeet()
                    .onSuccess { result ->
                        when (result) {
                            is LeetRepository.FavoriteLeetResult.Simple ->
                                uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE)
                            is LeetRepository.FavoriteLeetResult.Extended ->
                                uiManager.setTranslationMode(LeetTranslator.TranslationMode.EXTENDED)
                            is LeetRepository.FavoriteLeetResult.Custom -> {
                                repository.setCurrentLeetIndex(result.customIndex)
                                    .onSuccess {
                                        uiManager.setTranslationMode(LeetTranslator.TranslationMode.CUSTOM)
                                    }
                                    .onFailure {
                                        uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE)
                                    }
                            }
                        }
                    }
                    .onFailure {
                        uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE)
                    }
            } catch (e: Exception) {
                uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE)
            } finally {
                _isInitialized.value = true
            }
        }
    }

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.UpdateInput -> uiManager.updateInputText(intent.text)
            is MainIntent.ChangeMode -> changeMode(intent.leetOption)
            is MainIntent.ToggleFavorite -> toggleFavorite(intent.leetOption)
            is MainIntent.CreateLeet -> createLeet(intent.name, intent.useExtendedDefaults, intent.customTranslations)
            is MainIntent.UpdateLeet -> updateLeet(intent.index, intent.leet)
            is MainIntent.DeleteLeet -> deleteLeet(intent.index)
            is MainIntent.UndoDeleteLeet -> undoDeleteLeet()
            is MainIntent.CopyToClipboard -> copyToClipboard()
            is MainIntent.ClearInput -> uiManager.clearInput()
            is MainIntent.ClearError -> uiManager.clearError()
            is MainIntent.ClearSuccess -> uiManager.clearSuccess()
            is MainIntent.ToggleReverseMode -> toggleReverseMode()
            is MainIntent.DismissWhatsNew -> { }
            is MainIntent.MarkWhatsNewAsShown -> markWhatsNewAsShown()
            is MainIntent.ResetWhatsNewForTesting -> resetWhatsNewForTesting()
            is MainIntent.ForceShowWhatsNew -> forceShowWhatsNew()
            is MainIntent.ReorderLeets -> reorderLeets(intent.from, intent.to)

        }
    }

    private fun createLeet(
        name: String,
        useExtendedDefaults: Boolean,
        customTranslations: Map<String, String>? = null
    ) {
        viewModelScope.launch {
            uiManager.setLoading(true)
            leetManager.createLeet(name, useExtendedDefaults, customTranslations)
                .onSuccess { leet ->
                    uiManager.setTranslationMode(LeetTranslator.TranslationMode.CUSTOM)
                    uiManager.setSuccess(application.getString(R.string.success_leet_created, leet.name))
                }
                .onFailure { exception ->
                    uiManager.setError(application.getString(R.string.error_create_leet, exception.message ?: ""))
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
                    if (leetOption.customIndex >= 0)
                        repository.setCurrentLeetIndex(leetOption.customIndex)
                }
            }
            leetManager.changeMode(leetOption).onFailure { exception ->
                uiManager.setError(application.getString(R.string.error_change_mode, exception.message ?: ""))
            }
        }
    }

    private fun toggleFavorite(leetOption: LeetOption) {
        viewModelScope.launch {
            leetManager.toggleFavorite(leetOption)
                .onSuccess { result ->
                    uiManager.setSuccess(
                        if (result.isNowFavorite) application.getString(R.string.success_added_favorite)
                        else application.getString(R.string.success_removed_favorite)
                    )
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
                .onSuccess { uiManager.setSuccess(application.getString(R.string.success_leet_updated, leet.name)) }
                .onFailure { exception ->
                    uiManager.setError(application.getString(R.string.error_update_leet, exception.message ?: ""))
                }
        }
    }

    /**
     * NEU: Löscht den Leet und speichert ihn für mögliches Undo.
     * Die Snackbar-Anzeige wird über pendingDelete in der UI getriggert.
     */
    private fun deleteLeet(index: Int) {
        viewModelScope.launch {
            val wasActive = currentMode.value == LeetTranslator.TranslationMode.CUSTOM
                    && repository.getCurrentLeetIndex() == index

            uiManager.setLoading(true)
            leetManager.deleteLeet(index)
                .onSuccess { result ->
                    _pendingDelete.value = PendingDelete(
                        leet = result.deletedLeet,
                        index = index
                    )

                    if (result.wasLastLeet || wasActive) {
                        uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE)
                    }

                    val message = when {
                        result.wasLastLeet -> application.getString(R.string.info_switched_to_simple)
                        wasActive -> application.getString(R.string.info_active_leet_deleted)  // NEU
                        result.wasFavorite -> application.getString(R.string.info_favorite_deleted)
                        else -> application.getString(R.string.success_leet_deleted)
                    }
                    uiManager.setSuccess(message)
                }
                .onFailure { exception ->
                    uiManager.setError(
                        application.getString(R.string.error_delete_leet, exception.message ?: "")
                    )
                }
        }
    }

    /**
     * NEU: Stellt den zuletzt gelöschten Leet wieder her.
     */
    private fun undoDeleteLeet() {
        val pending = _pendingDelete.value ?: return
        viewModelScope.launch {
            repository.insertLeetAt(pending.index, pending.leet)
                .onSuccess {
                    _pendingDelete.value = null
                    uiManager.setTranslationMode(LeetTranslator.TranslationMode.CUSTOM)
                    uiManager.setSuccess(
                        application.getString(R.string.success_leet_restored, pending.leet.name)
                    )
                }
                .onFailure { exception ->
                    uiManager.setError(
                        application.getString(R.string.error_undo_delete, exception.message ?: "")
                    )
                }
        }
    }

    /**
     * NEU: Wird aufgerufen wenn die Snackbar ohne Undo-Klick verschwindet.
     * Löscht den pendingDelete-State.
     */
    fun clearPendingDelete() {
        _pendingDelete.value = null
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
            if (clearInputAfterCopy.value) {
                if (askBeforeClear.value) _showClearInputDialog.value = true
                else uiManager.clearInput()
            }
        } else {
            uiManager.setError(application.getString(R.string.error_no_text_to_copy))
        }
    }

    fun confirmClearInput(shouldClear: Boolean, dontAskAgain: Boolean) {
        viewModelScope.launch {
            if (dontAskAgain) {
                themePreferences.setAskBeforeClear(false)
                themePreferences.setClearInputAfterCopy(shouldClear)
            }
            if (shouldClear) uiManager.clearInput()
            _showClearInputDialog.value = false
        }
    }

    fun dismissClearInputDialog() { _showClearInputDialog.value = false }

    private fun markWhatsNewAsShown() {
        viewModelScope.launch { whatsNewPreferences.markCurrentVersionAsShown() }
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
            else -> LeetTranslator.TranslationMode.CUSTOM
        }
        val leet = if (leetOption.isCustom && leetOption.customIndex >= 0)
            leets.value.getOrNull(leetOption.customIndex) else null
        return translationManager.generatePreview(mode, leet, sampleText)
    }

    fun onReviewHandled() { _shouldRequestReview.value = false }

    fun resetReviewForTesting() {
        viewModelScope.launch {
            inAppReviewManager.resetForTesting()
            uiManager.setSuccess(application.getString(R.string.debug_review_data_reset))
        }
    }

    fun updateInputText(text: String) = uiManager.updateInputText(text)
    fun clearInput() = uiManager.clearInput()

    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
    }

    private fun reorderLeets(from: Int, to: Int) {
        viewModelScope.launch {
            repository.reorderLeets(from, to)
                .onFailure { exception ->
                    uiManager.setError(
                        application.getString(R.string.error_reorder_leets, exception.message ?: "")
                    )
                }
        }
    }
}