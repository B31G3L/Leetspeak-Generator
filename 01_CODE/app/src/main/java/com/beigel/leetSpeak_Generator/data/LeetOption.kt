package com.beigel.leetSpeak_Generator.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.manager.LeetManager

/**
 * Represents a leet translation option in the UI
 * Converted to Kotlin data class with improved immutability and Material Icons
 */
data class LeetOption(
    val mode: Int,
    val name: String,
    val description: String,
    val iconImageVector: ImageVector,
    val isCustom: Boolean = false,
    val customIndex: Int = -1,
    var isSelected: Boolean = false,
    var isFavorite: Boolean = false
) {


    companion object {
        /**
         * Creates a Simple Leet option
         */
        @Composable
        fun createSimple(isSelected: Boolean = false, isFavorite: Boolean = false): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_SIMPLE,
                name = "Simple Leet",
                description = stringResource(R.string.simple_leet_description),
                iconImageVector = Icons.Default.TextFields,
                isCustom = false,
                customIndex = -1,
                isSelected = isSelected,
                isFavorite = isFavorite
            )

        /**
         * Creates an Extended Leet option
         */
        @Composable
        fun createExtended(isSelected: Boolean = false, isFavorite: Boolean = false): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_EXTENDED,
                name = "Extended Leet",
                description = stringResource(R.string.extended_leet_description),
                iconImageVector = Icons.Default.Extension,
                isCustom = false,
                customIndex = -1,
                isSelected = isSelected,
                isFavorite = isFavorite
            )

        /**
         * Creates a Custom Leet option from a profile
         */
        @Composable
        fun createCustom(
            leet: CustomLeet,
            customIndex: Int,
            isSelected: Boolean = false,
            isFavorite: Boolean = false
        ): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_CUSTOM,
                name = leet.name,
                description = stringResource(R.string.custom_leet_description),
                iconImageVector = leet.iconImageVector,
                isCustom = true,
                customIndex = customIndex,
                isSelected = isSelected,
                isFavorite = isFavorite
            )
    }
}