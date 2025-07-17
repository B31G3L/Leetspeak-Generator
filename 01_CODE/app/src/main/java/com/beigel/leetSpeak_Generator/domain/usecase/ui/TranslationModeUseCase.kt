package com.beigel.leetSpeak_Generator.domain.usecase.ui

import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für die Verwaltung des aktuellen Übersetzungsmodus
 */
@Singleton
class TranslationModeUseCase @Inject constructor() {

    private val _currentMode = MutableStateFlow(LeetTranslator.TranslationMode.SIMPLE)
    val currentMode: StateFlow<LeetTranslator.TranslationMode> = _currentMode.asStateFlow()

    fun setMode(mode: LeetTranslator.TranslationMode) {
        _currentMode.value = mode
    }

    fun getCurrentMode(): LeetTranslator.TranslationMode {
        return _currentMode.value
    }
}

