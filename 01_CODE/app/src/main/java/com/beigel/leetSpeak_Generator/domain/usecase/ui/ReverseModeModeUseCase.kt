package com.beigel.leetSpeak_Generator.domain.usecase.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für das Management des Reverse-Modus
 */
@Singleton
class ReverseModeModeUseCase @Inject constructor() {

    private val _isReverseMode = MutableStateFlow(false)
    val isReverseMode: StateFlow<Boolean> = _isReverseMode.asStateFlow()

    fun toggleReverseMode() {
        _isReverseMode.value = !_isReverseMode.value
    }

    fun setReverseMode(enabled: Boolean) {
        _isReverseMode.value = enabled
    }
}