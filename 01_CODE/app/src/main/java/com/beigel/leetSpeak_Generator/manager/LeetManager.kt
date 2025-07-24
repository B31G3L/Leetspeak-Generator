package com.beigel.leetSpeak_Generator.manager

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.utils.ErrorHandler
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Modern LeetManager with Kotlin Coroutines and Flow support
 * Handles custom leet configurations with reactive data streams
 */
class LeetManager(context: Context) {

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

        private const val TAG = "LeetManager"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // Coroutine scope for background operations
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Mutable state flows for reactive updates
    private val _leets = MutableStateFlow<List<CustomLeet>>(emptyList())
    private val _currentLeetIndex = MutableStateFlow(0)
    private val _favoriteIndex = MutableStateFlow(FAV_NONE)

    // Public flows for observing state changes
    val leets: StateFlow<List<CustomLeet>> = _leets.asStateFlow()
    val currentLeetIndex: StateFlow<Int> = _currentLeetIndex.asStateFlow()
    val favoriteIndex: StateFlow<Int> = _favoriteIndex.asStateFlow()

    // Computed flows
    val currentLeet: StateFlow<CustomLeet?> = combine(
        leets,
        currentLeetIndex
    ) { leets, index ->
        leets.getOrNull(index)
    }.stateIn(scope, SharingStarted.Lazily, null)

    val hasLeets: StateFlow<Boolean> = leets.map { it.isNotEmpty() }
        .stateIn(scope, SharingStarted.Lazily, false)

    init {
        loadLeets()
    }

    /**
     * Loads leets from SharedPreferences
     */
    private fun loadLeets() {
        scope.launch {
            try {
                val leetsJson = prefs.getString(LEETS_KEY, null)
                val currentIndex = prefs.getInt(CURRENT_LEET_KEY, 0)
                val favoriteIndex = prefs.getInt(FAVORITE_LEET_KEY, FAV_NONE)

                val loadedLeets = if (leetsJson != null) {
                    val type = object : TypeToken<List<CustomLeet>>() {}.type
                    gson.fromJson<List<CustomLeet>>(leetsJson, type) ?: emptyList()
                } else {
                    emptyList()
                }

                // Update state on main thread
                withContext(Dispatchers.Main) {
                    _leets.value = loadedLeets
                    _currentLeetIndex.value = currentIndex.coerceIn(0, maxOf(0, loadedLeets.size - 1))
                    _favoriteIndex.value = migrateFavoriteIndex(favoriteIndex, loadedLeets.size)
                }

                Log.d(TAG, "Loaded ${loadedLeets.size} leets, current: $currentIndex, favorite: $favoriteIndex")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading leets", e)
                // Initialize with empty state on error
                withContext(Dispatchers.Main) {
                    _leets.value = emptyList()
                    _currentLeetIndex.value = 0
                    _favoriteIndex.value = FAV_NONE
                }
            }
        }
    }

    /**
     * Migrates old favorite index format to new format
     */
    private fun migrateFavoriteIndex(oldIndex: Int, leetCount: Int): Int {
        return when {
            oldIndex == FAV_SIMPLE || oldIndex == FAV_EXTENDED || oldIndex == FAV_NONE -> oldIndex
            oldIndex == 0 -> FAV_SIMPLE // Migrate old default
            oldIndex == 1 -> FAV_EXTENDED // Migrate old extended
            oldIndex >= 0 && oldIndex < leetCount -> oldIndex // Valid custom leet index
            else -> FAV_NONE // Invalid index
        }
    }

    /**
     * Saves leets to SharedPreferences
     */
    private suspend fun saveLeets() = withContext(Dispatchers.IO) {
        try {
            val leetsJson = gson.toJson(_leets.value)
            val currentIndex = _currentLeetIndex.value
            val favoriteIndex = _favoriteIndex.value

            prefs.edit()
                .putString(LEETS_KEY, leetsJson)
                .putInt(CURRENT_LEET_KEY, currentIndex)
                .putInt(FAVORITE_LEET_KEY, favoriteIndex)
                .apply()

            Log.d(TAG, "Saved ${_leets.value.size} leets")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving leets", e)
            throw e
        }
    }

    /**
     * Adds a new leet
     */
    suspend fun addLeet(leet: CustomLeet): ErrorHandler.Result<Int> =
        ErrorHandler.safeExecute(errorMessage = "Failed to add leet") {
            val currentLeets = _leets.value.toMutableList()
            currentLeets.add(leet)
            val newIndex = currentLeets.size - 1

            _leets.value = currentLeets
            _currentLeetIndex.value = newIndex

            saveLeets()
            newIndex
        }

    /**
     * Updates a leet at the given index
     */
    suspend fun updateLeet(index: Int, leet: CustomLeet): ErrorHandler.Result<Unit> =
        ErrorHandler.safeExecute(errorMessage = "Failed to update leet") {
            require(index in 0 until _leets.value.size) { "Invalid leet index: $index" }

            val currentLeets = _leets.value.toMutableList()
            currentLeets[index] = leet
            _leets.value = currentLeets

            saveLeets()
        }

    /**
     * Deletes a leet at the given index
     */
    suspend fun deleteLeet(index: Int): ErrorHandler.Result<LeetDeletionResult> =
        ErrorHandler.safeExecute(errorMessage = "Failed to delete leet") {
            require(index in 0 until _leets.value.size) { "Invalid leet index: $index" }

            val currentLeets = _leets.value.toMutableList()
            val deletedLeet = currentLeets.removeAt(index)
            val wasFavorite = _favoriteIndex.value == index
            val wasLastLeet = currentLeets.isEmpty()

            // Update favorite index if necessary
            when {
                _favoriteIndex.value == index -> _favoriteIndex.value = FAV_NONE
                _favoriteIndex.value > index -> _favoriteIndex.value = _favoriteIndex.value - 1
            }

            // Update current index if necessary
            val newCurrentIndex = when {
                currentLeets.isEmpty() -> 0
                _currentLeetIndex.value >= currentLeets.size -> currentLeets.size - 1
                else -> _currentLeetIndex.value
            }

            _leets.value = currentLeets
            _currentLeetIndex.value = newCurrentIndex

            saveLeets()

            LeetDeletionResult(deletedLeet, wasFavorite, wasLastLeet)
        }

    /**
     * Sets the current leet index
     */
    suspend fun setCurrentLeetIndex(index: Int): ErrorHandler.Result<Unit> =
        ErrorHandler.safeExecute(errorMessage = "Failed to set current leet") {
            require(index in 0 until _leets.value.size) { "Invalid leet index: $index" }

            _currentLeetIndex.value = index
            saveLeets()
        }

    /**
     * Sets the favorite leet
     */
    suspend fun setFavorite(mode: Int, customIndex: Int = 0): ErrorHandler.Result<Unit> =
        ErrorHandler.safeExecute(errorMessage = "Failed to set favorite") {
            val favoriteIndex = when (mode) {
                MODE_SIMPLE -> FAV_SIMPLE
                MODE_EXTENDED -> FAV_EXTENDED
                MODE_CUSTOM -> {
                    require(customIndex in 0 until _leets.value.size) {
                        "Invalid custom leet index: $customIndex"
                    }
                    customIndex
                }

                else -> throw IllegalArgumentException("Invalid mode: $mode")
            }

            _favoriteIndex.value = favoriteIndex
            saveLeets()

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

            saveLeets()

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
    fun getFavoriteLeetInfo(): FavoriteLeetInfo? {
        return when (val favIndex = _favoriteIndex.value) {
            FAV_SIMPLE -> FavoriteLeetInfo(MODE_SIMPLE, -1, null)
            FAV_EXTENDED -> FavoriteLeetInfo(MODE_EXTENDED, -1, null)
            FAV_NONE -> null
            else -> {
                val leet = _leets.value.getOrNull(favIndex)
                if (leet != null) {
                    FavoriteLeetInfo(MODE_CUSTOM, favIndex, leet)
                } else null
            }
        }
    }

    /**
     * Creates a leet with simple defaults
     */
    suspend fun createLeetWithSimpleDefaults(name: String, iconImageVector: ImageVector = Icons.Default.Settings): ErrorHandler.Result<CustomLeet> = // FIXED: Parameter type
        ErrorHandler.safeExecute(errorMessage = "Failed to create leet") {
            val leet = CustomLeet.createWithSimpleDefaults(name, iconImageVector) // FIXED: Pass ImageVector
            addLeet(leet).getOrNull() // Add to manager
            leet
        }

    /**
     * Creates a leet with extended defaults
     */
    suspend fun createLeetWithExtendedDefaults(name: String, iconImageVector: ImageVector = Icons.Default.Settings): ErrorHandler.Result<CustomLeet> = // FIXED: Parameter type
        ErrorHandler.safeExecute(errorMessage = "Failed to create leet") {
            val leet = CustomLeet.createWithExtendedDefaults(name, iconImageVector) // FIXED: Pass ImageVector
            addLeet(leet).getOrNull() // Add to manager
            leet
        }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        scope.cancel()
    }

    /**
     * Data class for leet deletion result
     */
    data class LeetDeletionResult(
        val deletedLeet: CustomLeet,
        val wasFavorite: Boolean,
        val wasLastLeet: Boolean
    )

    /**
     * Data class for favorite leet information
     */
    data class FavoriteLeetInfo(
        val mode: Int,
        val customIndex: Int,
        val customLeet: CustomLeet?
    )
}