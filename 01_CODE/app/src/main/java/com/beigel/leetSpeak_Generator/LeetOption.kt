package com.beigel.leetSpeak_Generator

/**
 * Represents a leet translation option in the UI
 * Converted to Kotlin data class with improved immutability and type safety
 */
data class LeetOption(
    val mode: Int,
    val name: String,
    val description: String,
    val iconResId: Int,
    val isCustom: Boolean = false,
    val customIndex: Int = -1,
    var isSelected: Boolean = false,
    var isFavorite: Boolean = false
) {

    /**
     * Creates a copy with updated selection state
     */
    fun withSelection(selected: Boolean): LeetOption =
        copy(isSelected = selected)

    /**
     * Creates a copy with updated favorite state
     */
    fun withFavorite(favorite: Boolean): LeetOption =
        copy(isFavorite = favorite)

    /**
     * Toggles the favorite state and returns a new instance
     */
    fun toggleFavorite(): LeetOption =
        copy(isFavorite = !isFavorite)

    /**
     * Checks if this option represents a built-in leet mode
     */
    val isBuiltIn: Boolean
        get() = !isCustom

    /**
     * Gets a display-friendly description including state indicators
     */
    val displayDescription: String
        get() = buildString {
            append(description)
            if (isFavorite) append(" ⭐")
            if (isSelected) append(" ✓")
        }

    companion object {
        /**
         * Creates a Simple Leet option
         */
        fun createSimple(isSelected: Boolean = false, isFavorite: Boolean = false): LeetOption =
            LeetOption(
                mode = ProfileManager.MODE_SIMPLE,
                name = "Simple Leet",
                description = "Einfache Leetspeak-Übersetzung",
                iconResId = R.drawable.ic_simple_mode,
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
                mode = ProfileManager.MODE_EXTENDED,
                name = "Extended Leet",
                description = "Erweiterte Leetspeak-Übersetzung",
                iconResId = R.drawable.ic_extended_mode,
                isCustom = false,
                customIndex = -1,
                isSelected = isSelected,
                isFavorite = isFavorite
            )

        /**
         * Creates a Custom Leet option from a profile
         */
        fun createCustom(
            profile: CustomProfile,
            customIndex: Int,
            isSelected: Boolean = false,
            isFavorite: Boolean = false
        ): LeetOption =
            LeetOption(
                mode = ProfileManager.MODE_CUSTOM,
                name = profile.name,
                description = "Benutzerdefiniertes Leet",
                iconResId = profile.iconResId,
                isCustom = true,
                customIndex = customIndex,
                isSelected = isSelected,
                isFavorite = isFavorite
            )
    }
}