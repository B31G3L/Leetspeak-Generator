package com.beigel.leetSpeak_Generator

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Modern ProfileManager with Kotlin Coroutines and Flow support
 * Handles custom leet profiles with reactive data streams
 */
class ProfileManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "LeetSpeakProfiles"
        private const val LEETS_KEY = "leets"
        private const val CURRENT_LEET_KEY = "current_leet"
        private const val FAVORITE_LEET_KEY = "favorite_leet"

        // Mode constants
        const val MODE_SIMPLE = 0
        const val MODE_EXTENDED = 1
        const val MODE_CUSTOM = 2
        const val MODE_ABOUT = 3

        // Favorite index constants
        const val FAV_NONE = -1
        const val FAV_SIMPLE = -100
        const val FAV_EXTENDED = -101

        private const val TAG = "ProfileManager"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // Coroutine scope for background operations
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Mutable state flows for reactive updates
    private val _profiles = MutableStateFlow<List<CustomProfile>>(emptyList())
    private val _currentProfileIndex = MutableStateFlow(0)
    private val _favoriteIndex = MutableStateFlow(FAV_NONE)

    // Public flows for observing state changes
    val profiles: StateFlow<List<CustomProfile>> = _profiles.asStateFlow()
    val currentProfileIndex: StateFlow<Int> = _currentProfileIndex.asStateFlow()
    val favoriteIndex: StateFlow<Int> = _favoriteIndex.asStateFlow()

    // Computed flows
    val currentProfile: StateFlow<CustomProfile?> = combine(
        profiles,
        currentProfileIndex
    ) { profiles, index ->
        profiles.getOrNull(index)
    }.stateIn(scope, SharingStarted.Lazily, null)

    val hasProfiles: StateFlow<Boolean> = profiles.map { it.isNotEmpty() }
        .stateIn(scope, SharingStarted.Lazily, false)

    init {
        loadProfiles()
    }

    /**
     * Loads profiles from SharedPreferences
     */
    private fun loadProfiles() {
        scope.launch {
            try {
                val profilesJson = prefs.getString(LEETS_KEY, null)
                val currentIndex = prefs.getInt(CURRENT_LEET_KEY, 0)
                val favoriteIndex = prefs.getInt(FAVORITE_LEET_KEY, FAV_NONE)

                val loadedProfiles = if (profilesJson != null) {
                    val type = object : TypeToken<List<CustomProfile>>() {}.type
                    gson.fromJson<List<CustomProfile>>(profilesJson, type) ?: emptyList()
                } else {
                    emptyList()
                }

                // Update state on main thread
                withContext(Dispatchers.Main) {
                    _profiles.value = loadedProfiles
                    _currentProfileIndex.value = currentIndex.coerceIn(0, maxOf(0, loadedProfiles.size - 1))
                    _favoriteIndex.value = migrateFavoriteIndex(favoriteIndex, loadedProfiles.size)
                }

                Log.d(TAG, "Loaded ${loadedProfiles.size} profiles, current: $currentIndex, favorite: $favoriteIndex")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading profiles", e)
                // Initialize with empty state on error
                withContext(Dispatchers.Main) {
                    _profiles.value = emptyList()
                    _currentProfileIndex.value = 0
                    _favoriteIndex.value = FAV_NONE
                }
            }
        }
    }

    /**
     * Migrates old favorite index format to new format
     */
    private fun migrateFavoriteIndex(oldIndex: Int, profileCount: Int): Int {
        return when {
            oldIndex == FAV_SIMPLE || oldIndex == FAV_EXTENDED || oldIndex == FAV_NONE -> oldIndex
            oldIndex == 0 -> FAV_SIMPLE // Migrate old default
            oldIndex == 1 -> FAV_EXTENDED // Migrate old extended
            oldIndex >= 0 && oldIndex < profileCount -> oldIndex // Valid custom profile index
            else -> FAV_NONE // Invalid index
        }
    }

    /**
     * Saves profiles to SharedPreferences
     */
    private suspend fun saveProfiles() = withContext(Dispatchers.IO) {
        try {
            val profilesJson = gson.toJson(_profiles.value)
            val currentIndex = _currentProfileIndex.value
            val favoriteIndex = _favoriteIndex.value

            prefs.edit()
                .putString(LEETS_KEY, profilesJson)
                .putInt(CURRENT_LEET_KEY, currentIndex)
                .putInt(FAVORITE_LEET_KEY, favoriteIndex)
                .apply()

            Log.d(TAG, "Saved ${_profiles.value.size} profiles")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profiles", e)
            throw e
        }
    }

    /**
     * Adds a new profile
     */
    suspend fun addProfile(profile: CustomProfile): ErrorHandler.Result<Int> =
        ErrorHandler.safeExecute(errorMessage = "Failed to add profile") {
            val currentProfiles = _profiles.value.toMutableList()
            currentProfiles.add(profile)
            val newIndex = currentProfiles.size - 1

            _profiles.value = currentProfiles
            _currentProfileIndex.value = newIndex

            saveProfiles()
            newIndex
        }

    /**
     * Updates a profile at the given index
     */
    suspend fun updateProfile(index: Int, profile: CustomProfile): ErrorHandler.Result<Unit> =
        ErrorHandler.safeExecute(errorMessage = "Failed to update profile") {
            require(index in 0 until _profiles.value.size) { "Invalid profile index: $index" }

            val currentProfiles = _profiles.value.toMutableList()
            currentProfiles[index] = profile
            _profiles.value = currentProfiles

            saveProfiles()
        }

    /**
     * Deletes a profile at the given index
     */
    suspend fun deleteProfile(index: Int): ErrorHandler.Result<ProfileDeletionResult> =
        ErrorHandler.safeExecute(errorMessage = "Failed to delete profile") {
            require(index in 0 until _profiles.value.size) { "Invalid profile index: $index" }

            val currentProfiles = _profiles.value.toMutableList()
            val deletedProfile = currentProfiles.removeAt(index)
            val wasFavorite = _favoriteIndex.value == index
            val wasLastProfile = currentProfiles.isEmpty()

            // Update favorite index if necessary
            when {
                _favoriteIndex.value == index -> _favoriteIndex.value = FAV_NONE
                _favoriteIndex.value > index -> _favoriteIndex.value = _favoriteIndex.value - 1
            }

            // Update current index if necessary
            val newCurrentIndex = when {
                currentProfiles.isEmpty() -> 0
                _currentProfileIndex.value >= currentProfiles.size -> currentProfiles.size - 1
                else -> _currentProfileIndex.value
            }

            _profiles.value = currentProfiles
            _currentProfileIndex.value = newCurrentIndex

            saveProfiles()

            ProfileDeletionResult(deletedProfile, wasFavorite, wasLastProfile)
        }

    /**
     * Sets the current profile index
     */
    suspend fun setCurrentProfileIndex(index: Int): ErrorHandler.Result<Unit> =
        ErrorHandler.safeExecute(errorMessage = "Failed to set current profile") {
            require(index in 0 until _profiles.value.size) { "Invalid profile index: $index" }

            _currentProfileIndex.value = index
            saveProfiles()
        }

    /**
     * Sets the favorite profile
     */
    suspend fun setFavorite(mode: Int, customIndex: Int = 0): ErrorHandler.Result<Unit> =
        ErrorHandler.safeExecute(errorMessage = "Failed to set favorite") {
            val favoriteIndex = when (mode) {
                MODE_SIMPLE -> FAV_SIMPLE
                MODE_EXTENDED -> FAV_EXTENDED
                MODE_CUSTOM -> {
                    require(customIndex in 0 until _profiles.value.size) {
                        "Invalid custom profile index: $customIndex"
                    }
                    customIndex
                }
                else -> throw IllegalArgumentException("Invalid mode: $mode")
            }

            _favoriteIndex.value = favoriteIndex
            saveProfiles()

            Log.d(TAG, "Favorite set to: $favoriteIndex")
        }

    /**
     * Toggles favorite status for a mode
     */
    suspend fun toggleFavorite(mode: Int, customIndex: Int = 0): ErrorHandler.Result<Boolean> =
        ErrorHandler.safeExecute(errorMessage = "Failed to toggle favorite") {
            val targetIndex = when (mode) {
                MODE_SIMPLE -> FAV_SIMPLE
                MODE_EXTENDED -> FAV_EXTENDED
                MODE_CUSTOM -> customIndex
                else -> throw IllegalArgumentException("Invalid mode: $mode")
            }

            val isCurrentlyFavorite = _favoriteIndex.value == targetIndex
            _favoriteIndex.value = if (isCurrentlyFavorite) FAV_NONE else targetIndex

            saveProfiles()

            val newState = !isCurrentlyFavorite
            Log.d(TAG, "Favorite toggled for mode $mode: $newState")
            newState
        }

    /**
     * Checks if a mode is currently favorite
     */
    fun isFavorite(mode: Int, customIndex: Int = 0): Boolean {
        val targetIndex = when (mode) {
            MODE_SIMPLE -> FAV_SIMPLE
            MODE_EXTENDED -> FAV_EXTENDED
            MODE_CUSTOM -> customIndex
            else -> return false
        }

        return _favoriteIndex.value == targetIndex
    }

    /**
     * Gets the favorite mode information
     */
    fun getFavoriteMode(): FavoriteModeInfo? {
        return when (val favIndex = _favoriteIndex.value) {
            FAV_SIMPLE -> FavoriteModeInfo(MODE_SIMPLE, -1, null)
            FAV_EXTENDED -> FavoriteModeInfo(MODE_EXTENDED, -1, null)
            FAV_NONE -> null
            else -> {
                val profile = _profiles.value.getOrNull(favIndex)
                if (profile != null) {
                    FavoriteModeInfo(MODE_CUSTOM, favIndex, profile)
                } else null
            }
        }
    }

    /**
     * Creates a profile with simple leet defaults
     */
    suspend fun createProfileWithSimpleDefaults(name: String, iconResId: Int = R.drawable.ic_custom_mode): ErrorHandler.Result<CustomProfile> =
        ErrorHandler.safeExecute(errorMessage = "Failed to create profile") {
            val profile = CustomProfile.createWithSimpleDefaults(name, iconResId)
            addProfile(profile).getOrNull() // Add to manager
            profile
        }

    /**
     * Creates a profile with extended leet defaults
     */
    suspend fun createProfileWithExtendedDefaults(name: String, iconResId: Int = R.drawable.ic_custom_mode): ErrorHandler.Result<CustomProfile> =
        ErrorHandler.safeExecute(errorMessage = "Failed to create profile") {
            val profile = CustomProfile.createWithExtendedDefaults(name, iconResId)
            addProfile(profile).getOrNull() // Add to manager
            profile
        }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        scope.cancel()
    }

    /**
     * Data class for profile deletion result
     */
    data class ProfileDeletionResult(
        val deletedProfile: CustomProfile,
        val wasFavorite: Boolean,
        val wasLastProfile: Boolean
    )

    /**
     * Data class for favorite mode information
     */
    data class FavoriteModeInfo(
        val mode: Int,
        val customIndex: Int,
        val customProfile: CustomProfile?
    )
}