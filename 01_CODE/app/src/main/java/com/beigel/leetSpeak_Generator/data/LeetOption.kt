package com.beigel.leetSpeak_Generator.data

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
        fun createSimple(isSelected: Boolean = false, isFavorite: Boolean = false): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_SIMPLE,
                name = "Simple Leet",
                description = "Basic character substitutions (A→4, E→3, etc.)",
                isCustom = false,
                customIndex = -1,
                isSelected = isSelected,
                isFavorite = isFavorite
            )

        /**
         * Creates an Extended Leet option
         */
        fun createExtended(isSelected: Boolean = false, isFavorite: Boolean = false): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_EXTENDED,
                name = "Extended Leet",
                description = "Advanced multi-character substitutions (M→/\\/\\, N→|\\|, etc.)",
                isCustom = false,
                customIndex = -1,
                isSelected = isSelected,
                isFavorite = isFavorite
            )

        /**
         * Creates a Custom Leet option from a profile
         */
        fun createCustom(
            leet: CustomLeet,
            customIndex: Int,
            isSelected: Boolean = false,
            isFavorite: Boolean = false
        ): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_CUSTOM,
                name = leet.name,
                description = "Custom character mappings",
                isCustom = true,
                customIndex = customIndex,
                isSelected = isSelected,
                isFavorite = isFavorite
            )

        /**
         * Composable versions for UI usage with proper string resources
         */
        @Composable
        fun createSimpleWithResources(isSelected: Boolean = false, isFavorite: Boolean = false): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_SIMPLE,
                name = "Simple Leet",
                description = stringResource(R.string.simple_leet_description),
                isCustom = false,
                customIndex = -1,
                isSelected = isSelected,
                isFavorite = isFavorite
            )

        @Composable
        fun createExtendedWithResources(isSelected: Boolean = false, isFavorite: Boolean = false): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_EXTENDED,
                name = "Extended Leet",
                description = stringResource(R.string.extended_leet_description),
                isCustom = false,
                customIndex = -1,
                isSelected = isSelected,
                isFavorite = isFavorite
            )

        @Composable
        fun createCustomWithResources(
            leet: CustomLeet,
            customIndex: Int,
            isSelected: Boolean = false,
            isFavorite: Boolean = false
        ): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_CUSTOM,
                name = leet.name,
                description = stringResource(R.string.custom_leet_description),
                isCustom = true,
                customIndex = customIndex,
                isSelected = isSelected,
                isFavorite = isFavorite
            )
    }
}