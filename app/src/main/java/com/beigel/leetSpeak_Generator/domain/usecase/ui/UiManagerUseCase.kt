package com.beigel.leetSpeak_Generator.domain.usecase.ui

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UiManagerUseCase @Inject constructor(
    private val reverseModeUseCase: ReverseModeModeUseCase,
    private val translationModeUseCase: TranslationModeUseCase,
    private val displayNameUseCase: DisplayNameUseCase,
    private val inputTextUseCase: InputTextUseCase,
    private val uiStateManagementUseCase: UiStateManagementUseCase
) {
    val isReverseMode = reverseModeUseCase.isReverseMode
    val currentMode = translationModeUseCase.currentMode
    val inputText = inputTextUseCase.inputText
    val uiState = uiStateManagementUseCase.uiState

    fun updateInputText(text: String) = inputTextUseCase.updateText(text)
    fun clearInput() = inputTextUseCase.clearText()
    fun hasInput() = inputTextUseCase.hasText()

    fun toggleReverseMode() = reverseModeUseCase.toggleReverseMode()
    fun setReverseMode(enabled: Boolean) = reverseModeUseCase.setReverseMode(enabled)
    fun setTranslationMode(mode: LeetTranslator.TranslationMode) =
        translationModeUseCase.setMode(mode)

    fun generateModeDisplayName(customLeet: CustomLeet?): String =
        displayNameUseCase.generateModeDisplayName(
            mode = translationModeUseCase.getCurrentMode(),
            customLeet = customLeet,
            isReverseMode = reverseModeUseCase.isReverseMode.value
        )

    fun generateInputTitle(currentModeDisplayName: String): String =
        displayNameUseCase.generateInputTitle(
            isReverseMode = reverseModeUseCase.isReverseMode.value,
            currentModeDisplayName = currentModeDisplayName
        )

    fun generateOutputTitle(currentModeDisplayName: String): String =
        displayNameUseCase.generateOutputTitle(
            isReverseMode = reverseModeUseCase.isReverseMode.value,
            currentModeDisplayName = currentModeDisplayName
        )

    fun setLoading(isLoading: Boolean) = uiStateManagementUseCase.setLoading(isLoading)
    fun setError(message: String) = uiStateManagementUseCase.setError(message)
    fun setSuccess(message: String) = uiStateManagementUseCase.setSuccess(message)
    fun clearError() = uiStateManagementUseCase.clearError()
    fun clearSuccess() = uiStateManagementUseCase.clearSuccess()
}