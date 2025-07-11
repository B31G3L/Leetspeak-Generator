package com.beigel.leetSpeak_Generator.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beigel.leetSpeak_Generator.MainIntent
import com.beigel.leetSpeak_Generator.MainViewModel
import com.beigel.leetSpeak_Generator.ui.theme.LeetspeakGeneratorTheme
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * ✅ KORRIGIERTE LÖSUNG: Compose Bottom Sheet als DialogFragment
 * Funktioniert genauso wie Ihr bestehendes XML Bottom Sheet
 */
@AndroidEntryPoint
class ComposeBottomSheetDialog : BottomSheetDialogFragment() {

    private val viewModel: MainViewModel by viewModels()

    companion object {
        fun newInstance() = ComposeBottomSheetDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                LeetspeakGeneratorTheme {
                    LeetSelectorBottomSheetContent(
                        viewModel = viewModel,
                        onDismiss = { dismiss() }
                    )
                }
            }
        }
    }
}

/**
 * ✅ KORRIGIERTE Content für das Bottom Sheet
 */
@Composable
fun LeetSelectorBottomSheetContent(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    // ✅ FIX: initialValue Parameter hinzugefügt
    val leetOptions by viewModel.leetOptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteLeetOptions by viewModel.favoriteLeetOptions.collectAsStateWithLifecycle(initialValue = emptyList())

    // Direkt den Content rendern (BottomSheetDialogFragment übernimmt das Sheet)
    LeetSelectorContent(
        leetOptions = leetOptions,
        favoriteLeetOptions = favoriteLeetOptions,
        onOptionSelected = { option ->
            viewModel.handleIntent(MainIntent.ChangeMode(option))
            onDismiss()
        },
        onToggleFavorite = { option ->
            viewModel.handleIntent(MainIntent.ToggleFavorite(option))
        },
        onEditOption = { option ->
            // TODO: Edit functionality
            onDismiss()
        },
        onShowTable = { option ->
            // TODO: Show table functionality
            onDismiss()
        },
        onCreateNew = {
            // TODO: Create new functionality
            onDismiss()
        }
    )
}