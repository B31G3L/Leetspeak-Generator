package com.beigel.leetSpeak_Generator

import android.content.Context
import kotlinx.coroutines.flow.*

/**
 * Modern repository implementing reactive data access with Kotlin Flows
 * Provides clean API for profile management with proper error handling
 */
class ProfileRepository(context: Context) {

    private val leetManager = LeetManager(context)

    // Expose reactive streams from ProfileManager
    val profiles: StateFlow<List<CustomLeet>> = leetManager.profiles
    val currentProfile: StateFlow<CustomLeet?> = leetManager.currentProfile
    val currentProfileIndex: StateFlow<Int> = leetManager.currentProfileIndex
    val hasProfiles: StateFlow<Boolean> = leetManager.hasProfiles
    val favoriteIndex: StateFlow<Int> = leetManager.favoriteIndex

    /**
     * Creates a new profile with the given configuration
     */
    suspend fun createProfile(request: ProfileCreationRequest): ErrorHandler.Result<ProfileCreationResult> =
        ErrorHandler.safeExecute(errorMessage = "Failed to create profile") {
            val profile = CustomLeet(request.name, request.iconResId)
            profile.setTranslations(request.translations)

            val indexResult = leetManager.addProfile(profile)
            when (indexResult) {
                is ErrorHandler.Result.Success -> {
                    ProfileCreationResult(profile, indexResult.data, true, "Profile created successfully")
                }
                is ErrorHandler.Result.Error -> {
                    throw indexResult.exception
                }
            }
        }

    /**
     * Updates an existing profile
     */
    suspend fun updateProfile(index: Int, profile: CustomLeet): ErrorHandler.Result<ProfileUpdateResult> =
        ErrorHandler.safeExecute(errorMessage = "Failed to update profile") {
            leetManager.updateProfile(index, profile).getOrNull()
            ProfileUpdateResult(profile, index, true, "Profile updated successfully")
        }

    /**
     * Deletes a profile at the given index
     */
    suspend fun deleteProfile(index: Int): ErrorHandler.Result<ProfileDeletionResult> =
        ErrorHandler.safeExecute(errorMessage = "Failed to delete profile") {
            val result = leetManager.deleteProfile(index)
            when (result) {
                is ErrorHandler.Result.Success -> {
                    ProfileDeletionResult(
                        deletedProfile = result.data.deletedProfile,
                        wasFavorite = result.data.wasFavorite,
                        wasLastProfile = result.data.wasLastProfile,
                        success = true,
                        message = "Profile deleted successfully"
                    )
                }
                is ErrorHandler.Result.Error -> throw result.exception
            }
        }

    /**
     * Sets the current profile index
     */
    suspend fun setCurrentProfileIndex(index: Int): ErrorHandler.Result<Unit> =
        leetManager.setCurrentProfileIndex(index)

    /**
     * Toggles favorite status for a mode
     */
    suspend fun toggleFavorite(mode: Int, customIndex: Int = 0): ErrorHandler.Result<FavoriteToggleResult> =
        ErrorHandler.safeExecute(errorMessage = "Failed to toggle favorite") {
            val wasAlreadyFavorite = leetManager.isFavorite(mode, customIndex)
            val toggleResult = leetManager.toggleFavorite(mode, customIndex)

            when (toggleResult) {
                is ErrorHandler.Result.Success -> {
                    FavoriteToggleResult(
                        mode = mode,
                        customIndex = customIndex,
                        wasAlreadyFavorite = wasAlreadyFavorite,
                        isNowFavorite = toggleResult.data,
                        success = true
                    )
                }
                is ErrorHandler.Result.Error -> throw toggleResult.exception
            }
        }

    /**
     * Loads the favorite mode asynchronously
     */
    /**
     * Loads the favorite mode asynchronously
     */
    suspend fun loadFavoriteMode(): ErrorHandler.Result<FavoriteModeResult> =
        ErrorHandler.safeExecute(errorMessage = "Failed to load favorite mode") {
            val favoriteInfo = leetManager.getFavoriteMode()

            when {
                favoriteInfo == null -> FavoriteModeResult.simple()
                favoriteInfo.mode == LeetManager.MODE_SIMPLE -> FavoriteModeResult.simple()
                favoriteInfo.mode == LeetManager.MODE_EXTENDED -> FavoriteModeResult.extended()
                favoriteInfo.mode == LeetManager.MODE_CUSTOM -> {
                    if (favoriteInfo.customLeet != null) {
                        FavoriteModeResult.custom(favoriteInfo.customIndex, favoriteInfo.customLeet)
                    } else {
                        FavoriteModeResult.simple() // Fallback
                    }
                }
                else -> FavoriteModeResult.simple() // Fallback für unbekannte Modi
            }
        }

    /**
     * Creates a profile with simple defaults
     */
    suspend fun createProfileWithSimpleDefaults(name: String, iconResId: Int = R.drawable.ic_custom_mode): ErrorHandler.Result<CustomLeet> =
        leetManager.createProfileWithSimpleDefaults(name, iconResId)

    /**
     * Creates a profile with extended defaults
     */
    suspend fun createProfileWithExtendedDefaults(name: String, iconResId: Int = R.drawable.ic_custom_mode): ErrorHandler.Result<CustomLeet> =
        leetManager.createProfileWithExtendedDefaults(name, iconResId)

    /**
     * Gets all leet options for UI display
     */
    fun getLeetOptions(): Flow<List<LeetOption>> = combine(
        profiles,
        currentProfileIndex,
        favoriteIndex
    ) { profiles, currentIndex, favoriteIndex ->
        buildList {
            // Add Simple Leet
            add(LeetOption.createSimple(
                isSelected = currentIndex == -1, // No custom profile selected
                isFavorite = favoriteIndex == LeetManager.FAV_SIMPLE
            ))

            // Add Extended Leet
            add(LeetOption.createExtended(
                isSelected = currentIndex == -2, // Extended mode indicator
                isFavorite = favoriteIndex == LeetManager.FAV_EXTENDED
            ))

            // Add Custom Leets
            profiles.forEachIndexed { index, profile ->
                add(LeetOption.createCustom(
                    profile = profile,
                    customIndex = index,
                    isSelected = currentIndex == index,
                    isFavorite = favoriteIndex == index
                ))
            }
        }
    }

    /**
     * Gets only favorite leet options
     */
    fun getFavoriteLeetOptions(): Flow<List<LeetOption>> =
        getLeetOptions().map { options ->
            options.filter { it.isFavorite }
        }

    /**
     * Checks if a mode is favorite
     */
    fun isFavorite(mode: Int, customIndex: Int = 0): Boolean =
        leetManager.isFavorite(mode, customIndex)

    /**
     * Gets the current profile synchronously (for compatibility)
     */
    fun getCurrentProfile(): CustomLeet? = currentProfile.value

    /**
     * Gets the current profile index synchronously
     */
    fun getCurrentProfileIndex(): Int = currentProfileIndex.value

    /**
     * Checks if profiles exist
     */
    fun hasProfiles(): Boolean = hasProfiles.value

    /**
     * Gets all profiles synchronously
     */
    fun getProfiles(): List<CustomLeet> = profiles.value

    /**
     * Cleanup resources
     */
    fun cleanup() {
        leetManager.cleanup()
    }

    // Legacy compatibility for gradual migration
    fun getProfileManager(): LeetManager = leetManager

    /**
     * Data class for profile creation request
     */
    data class ProfileCreationRequest(
        val name: String,
        val iconResId: Int,
        val translations: Map<String, String>
    )

    /**
     * Result classes for operations
     */
    data class ProfileCreationResult(
        val profile: CustomLeet,
        val index: Int,
        val success: Boolean,
        val message: String
    )

    data class ProfileUpdateResult(
        val profile: CustomLeet,
        val index: Int,
        val success: Boolean,
        val message: String
    )

    data class ProfileDeletionResult(
        val deletedProfile: CustomLeet,
        val wasFavorite: Boolean,
        val wasLastProfile: Boolean,
        val success: Boolean,
        val message: String
    )

    data class FavoriteToggleResult(
        val mode: Int,
        val customIndex: Int,
        val wasAlreadyFavorite: Boolean,
        val isNowFavorite: Boolean,
        val success: Boolean
    )

    /**
     * Favorite mode result sealed class
     */
    sealed class FavoriteModeResult {
        abstract val mode: Int

        data class Simple(override val mode: Int = LeetManager.MODE_SIMPLE) : FavoriteModeResult()
        data class Extended(override val mode: Int = LeetManager.MODE_EXTENDED) : FavoriteModeResult()
        data class Custom(
            override val mode: Int = LeetManager.MODE_CUSTOM,
            val customIndex: Int,
            val profile: CustomLeet
        ) : FavoriteModeResult()

        companion object {
            fun simple() = Simple()
            fun extended() = Extended()
            fun custom(index: Int, profile: CustomLeet) = Custom(customIndex = index, profile = profile)
        }
    }
}