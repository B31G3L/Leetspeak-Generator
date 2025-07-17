package com.beigel.leetSpeak_Generator.domain.usecase.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für Input-Text Management
 */
@Singleton
class InputTextUseCase @Inject constructor() {

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    fun updateText(text: String) {
        _inputText.value = text
    }

    fun clearText() {
        _inputText.value = ""
    }

    fun getText(): String {
        return _inputText.value
    }

    fun hasText(): Boolean {
        return _inputText.value.isNotEmpty()
    }
}