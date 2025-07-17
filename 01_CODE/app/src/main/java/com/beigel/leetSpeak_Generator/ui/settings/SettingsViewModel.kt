package com.beigel.leetSpeak_Generator.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beigel.leetSpeak_Generator.data.ThemePreferences
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    private val leetRepository: LeetRepository
) : ViewModel() {

    val themeMode: StateFlow<String> = themePreferences.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePreferences.THEME_SYSTEM
        )

    val favoriteLeet: StateFlow<String?> = leetRepository.getFavoriteLeetOptions()
        .map { favorites ->
            favorites.firstOrNull()?.name
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    suspend fun setTheme(theme: String) {
        themePreferences.setTheme(theme)
    }
}