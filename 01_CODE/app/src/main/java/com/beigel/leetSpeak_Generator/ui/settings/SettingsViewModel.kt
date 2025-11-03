package com.beigel.leetSpeak_Generator.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beigel.leetSpeak_Generator.data.ThemePreferences
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import com.beigel.leetSpeak_Generator.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    leetRepository: LeetRepository
) : ViewModel() {

    val themeMode: StateFlow<String> = themePreferences.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePreferences.THEME_SYSTEM
        )

    val appTheme: StateFlow<AppTheme> = themePreferences.appTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.PLANIT
        )

    val defaultViewExpanded: StateFlow<Boolean> = themePreferences.defaultViewExpanded
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val language: StateFlow<String> = themePreferences.language
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePreferences.LANGUAGE_SYSTEM
        )

    // Copy behavior states
    val clearInputAfterCopy: StateFlow<Boolean> = themePreferences.clearInputAfterCopy
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val askBeforeClear: StateFlow<Boolean> = themePreferences.askBeforeClear
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    suspend fun setTheme(theme: String) {
        themePreferences.setTheme(theme)
    }

    suspend fun setAppTheme(appTheme: AppTheme) {
        themePreferences.setAppTheme(appTheme)
    }

    suspend fun setDefaultViewExpanded(expanded: Boolean) {
        themePreferences.setDefaultViewExpanded(expanded)
    }

    suspend fun setLanguage(language: String) {
        themePreferences.setLanguage(language)
    }

    suspend fun setClearInputAfterCopy(clear: Boolean) {
        themePreferences.setClearInputAfterCopy(clear)
    }

    suspend fun setAskBeforeClear(ask: Boolean) {
        themePreferences.setAskBeforeClear(ask)
    }
}