package com.beigel.leetSpeak_Generator.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
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
        fun createSimple(isSelected: Boolean = false, isFavorite: Boolean = false): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_SIMPLE,
                name = "Simple Leet",
                description = "Einfache Leetspeak-Übersetzung",
                iconImageVector = Icons.Default.TextFields,
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
                description = "Erweiterte Leetspeak-Übersetzung",
                iconImageVector = Icons.Default.Extension,
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
                description = "Benutzerdefiniertes Leet",
                iconImageVector = leet.iconImageVector,
                isCustom = true,
                customIndex = customIndex,
                isSelected = isSelected,
                isFavorite = isFavorite
            )
    }
}