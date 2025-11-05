package com.beigel.leetSpeak_Generator.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.manager.LeetManager

/**
 * Represents a leet translation option in the UI
 * UPDATED: Icon-Handling entfernt
 */
data class LeetOption(
    val mode: Int,
    val name: String,
    val description: String,
    val isCustom: Boolean = false,
    val customIndex: Int = -1,
    var isSelected: Boolean = false,
    var isFavorite: Boolean = false
) {

    companion object {
        /**
         * Creates a Simple Leet option
         */
        fun createSimple(context: Context, isSelected: Boolean = false, isFavorite: Boolean = false): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_SIMPLE,
                name = context.getString(R.string.leet_option_simple_name),
                description = context.getString(R.string.simple_leet_description),
                isCustom = false,
                customIndex = -1,
                isSelected = isSelected,
                isFavorite = isFavorite
            )

        /**
         * Creates an Extended Leet option
         */
        fun createExtended(context: Context, isSelected: Boolean = false, isFavorite: Boolean = false): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_EXTENDED,
                name = context.getString(R.string.leet_option_extended_name),
                description = context.getString(R.string.extended_leet_description),
                isCustom = false,
                customIndex = -1,
                isSelected = isSelected,
                isFavorite = isFavorite
            )

        /**
         * Creates a Custom Leet option from a profile
         */
        fun createCustom(
            context: Context,
            leet: CustomLeet,
            customIndex: Int,
            isSelected: Boolean = false,
            isFavorite: Boolean = false
        ): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_CUSTOM,
                name = leet.name,
                description = context.getString(R.string.custom_leet_description),
                isCustom = true,
                customIndex = customIndex,
                isSelected = isSelected,
                isFavorite = isFavorite
            )
    }
}