package com.beigel.leetSpeak_Generator.presentation.intent

import androidx.compose.ui.graphics.vector.ImageVector
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.data.LeetOption

/**
 * UI Intents für MainViewModel
 * UPDATED: CreateLeet mit customTranslations erweitert
 */
sealed class MainIntent {
    data class UpdateInput(val text: String) : MainIntent()
    data class ChangeMode(val leetOption: LeetOption) : MainIntent()
    data class ToggleFavorite(val leetOption: LeetOption) : MainIntent()

    // FIXED: Erweiterte CreateLeet Intent mit individuellen Übersetzungen
    data class CreateLeet(
        val name: String,
        val icon: ImageVector,
        val useExtendedDefaults: Boolean = false,
        val customTranslations: Map<String, String>? = null // NEU: Individuelle Übersetzungen
    ) : MainIntent()

    data class UpdateLeet(val index: Int, val leet: CustomLeet) : MainIntent()
    data class DeleteLeet(val index: Int) : MainIntent()
    object CopyToClipboard : MainIntent()
    object ClearInput : MainIntent()
    object ClearError : MainIntent()
    object ClearSuccess : MainIntent()
    object ToggleReverseMode : MainIntent()

    // What's New Dialog Intents
    object DismissWhatsNew : MainIntent()
    object MarkWhatsNewAsShown : MainIntent()
    object ResetWhatsNewForTesting : MainIntent()
    object ForceShowWhatsNew : MainIntent()
}

/**
 * UI State für MainViewModel
 * UPDATED: WhatsNew State hinzugefügt
 */
data class MainUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedLeetOption: LeetOption? = null,
    val showBottomSheet: Boolean = false,
    val showWhatsNew: Boolean = false
)