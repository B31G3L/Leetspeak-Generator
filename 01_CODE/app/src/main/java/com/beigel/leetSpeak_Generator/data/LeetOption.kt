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
 * FIXED: Verwendet String für iconName intern, stellt aber ImageVector bereit
 */
data class LeetOption(
    val mode: Int,
    val name: String,
    val description: String,
    val iconName: String, // FIXED: String statt ImageVector
    val isCustom: Boolean = false,
    val customIndex: Int = -1,
    var isSelected: Boolean = false,
    var isFavorite: Boolean = false
) {

    /**
     * FIXED: Computed property für Abwärtskompatibilität
     */
    val iconImageVector: ImageVector
        get() = IconMapper.getIconByName(iconName)

    companion object {
        /**
         * Creates a Simple Leet option - NOT @Composable anymore
         * FIXED: Uses direct strings instead of stringResource()
         */
        fun createSimple(isSelected: Boolean = false, isFavorite: Boolean = false): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_SIMPLE,
                name = "Simple Leet",
                description = "Basic character substitutions (A→4, E→3, etc.)",
                iconName = "TextFields", // FIXED: String statt ImageVector
                isCustom = false,
                customIndex = -1,
                isSelected = isSelected,
                isFavorite = isFavorite
            )

        /**
         * Creates an Extended Leet option - NOT @Composable anymore
         * FIXED: Uses direct strings instead of stringResource()
         */
        fun createExtended(isSelected: Boolean = false, isFavorite: Boolean = false): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_EXTENDED,
                name = "Extended Leet",
                description = "Advanced multi-character substitutions (M→/\\/\\, N→|\\|, etc.)",
                iconName = "Extension", // FIXED: String statt ImageVector
                isCustom = false,
                customIndex = -1,
                isSelected = isSelected,
                isFavorite = isFavorite
            )

        /**
         * Creates a Custom Leet option from a profile - NOT @Composable anymore
         * FIXED: Uses direct strings instead of stringResource()
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
                iconName = leet.iconName, // FIXED: Verwendet direkt iconName
                isCustom = true,
                customIndex = customIndex,
                isSelected = isSelected,
                isFavorite = isFavorite
            )

        /**
         * NEW: Composable versions for UI usage with proper string resources
         */
        @Composable
        fun createSimpleWithResources(isSelected: Boolean = false, isFavorite: Boolean = false): LeetOption =
            LeetOption(
                mode = LeetManager.MODE_SIMPLE,
                name = "Simple Leet",
                description = stringResource(R.string.simple_leet_description),
                iconName = "TextFields",
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
                iconName = "Extension",
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
                iconName = leet.iconName,
                isCustom = true,
                customIndex = customIndex,
                isSelected = isSelected,
                isFavorite = isFavorite
            )
    }
}