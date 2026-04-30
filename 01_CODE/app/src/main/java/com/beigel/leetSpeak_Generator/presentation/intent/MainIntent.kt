package com.beigel.leetSpeak_Generator.presentation.intent

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.data.LeetOption

sealed class MainIntent {
    data class UpdateInput(val text: String) : MainIntent()
    data class ChangeMode(val leetOption: LeetOption) : MainIntent()
    data class ToggleFavorite(val leetOption: LeetOption) : MainIntent()
    data class CreateLeet(
        val name: String,
        val useExtendedDefaults: Boolean = false,
        val customTranslations: Map<String, String>? = null
    ) : MainIntent()
    data class UpdateLeet(val index: Int, val leet: CustomLeet) : MainIntent()
    data class DeleteLeet(val index: Int) : MainIntent()
    object UndoDeleteLeet : MainIntent()   // NEU
    object CopyToClipboard : MainIntent()
    object ClearInput : MainIntent()
    object ClearError : MainIntent()
    object ClearSuccess : MainIntent()
    object ToggleReverseMode : MainIntent()
    object DismissWhatsNew : MainIntent()
    object MarkWhatsNewAsShown : MainIntent()
    object ResetWhatsNewForTesting : MainIntent()
    object ForceShowWhatsNew : MainIntent()
}

data class MainUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedLeetOption: LeetOption? = null,
    val showBottomSheet: Boolean = false,
    val showWhatsNew: Boolean = false
)

data class PendingDelete(
    val leet: CustomLeet,
    val index: Int
)