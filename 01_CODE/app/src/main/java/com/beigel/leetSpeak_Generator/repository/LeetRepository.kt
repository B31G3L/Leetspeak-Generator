package com.beigel.leetSpeak_Generator.repository

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.manager.LeetManager
import com.beigel.leetSpeak_Generator.utils.ErrorHandler
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FIXED: Updated to use ImageVector instead of Int for icons
 */
@Singleton
class LeetRepository @Inject constructor(
    context: Context
) {

    private val leetManager = LeetManager(context)

    // Expose reactive streams from LeetManager
    val leets: StateFlow<List<CustomLeet>> = leetManager.leets
    val currentLeet: StateFlow<CustomLeet?> = leetManager.currentLeet
    val currentLeetIndex: StateFlow<Int> = leetManager.currentLeetIndex
    val hasLeets: StateFlow<Boolean> = leetManager.hasLeets
    val favoriteIndex: StateFlow<Int> = leetManager.favoriteIndex

    /**
     * Creates a new leet with the given configuration
     */
    suspend fun createLeet(request: LeetCreationRequest): Result<LeetCreationResult> {
        return try {
            val leet = CustomLeet(request.name, request.iconImageVector) // FIXED: Use iconImageVector
            leet.setTranslations(request.translations)

            when (val indexResult = leetManager.addLeet(leet)) {
                is ErrorHandler.Result.Success -> {
                    Result.success(
                        LeetCreationResult(
                            leet = leet,
                            index = indexResult.data,
                            success = true,
                            message = "Leet created successfully"
                        )
                    )
                }
                is ErrorHandler.Result.Error -> {
                    Result.failure(indexResult.exception)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing leet
     */
    suspend fun updateLeet(index: Int, leet: CustomLeet): Result<LeetUpdateResult> {
        return try {
            leetManager.updateLeet(index, leet).let { result ->
                when (result) {
                    is ErrorHandler.Result.Success -> {
                        Result.success(
                            LeetUpdateResult(
                                leet = leet,
                                index = index,
                                success = true,
                                message = "Leet updated successfully"
                            )
                        )
                    }
                    is ErrorHandler.Result.Error -> {
                        Result.failure(result.exception)
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a leet at the given index
     */
    suspend fun deleteLeet(index: Int): Result<LeetDeletionResult> {
        return try {
            leetManager.deleteLeet(index).let { result ->
                when (result) {
                    is ErrorHandler.Result.Success -> {
                        Result.success(
                            LeetDeletionResult(
                                deletedLeet = result.data.deletedLeet,
                                wasFavorite = result.data.wasFavorite,
                                wasLastLeet = result.data.wasLastLeet,
                                success = true,
                                message = "Leet deleted successfully"
                            )
                        )
                    }
                    is ErrorHandler.Result.Error -> {
                        Result.failure(result.exception)
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sets the current leet index
     */
    suspend fun setCurrentLeetIndex(index: Int): Result<Unit> {
        return try {
            when (val result = leetManager.setCurrentLeetIndex(index)) {
                is ErrorHandler.Result.Success -> Result.success(Unit)
                is ErrorHandler.Result.Error -> Result.failure(result.exception)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggles favorite status for a mode
     */
    suspend fun toggleFavorite(mode: Int, customIndex: Int = 0): Result<FavoriteToggleResult> {
        return try {
            val wasAlreadyFavorite = leetManager.isFavorite(mode, customIndex)
            leetManager.toggleFavorite(mode, customIndex).let { result ->
                when (result) {
                    is ErrorHandler.Result.Success -> {
                        Result.success(
                            FavoriteToggleResult(
                                mode = mode,
                                customIndex = customIndex,
                                wasAlreadyFavorite = wasAlreadyFavorite,
                                isNowFavorite = result.data,
                                success = true
                            )
                        )
                    }
                    is ErrorHandler.Result.Error -> {
                        Result.failure(result.exception)
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Loads the favorite mode asynchronously
     */
    suspend fun loadFavoriteLeet(): Result<FavoriteLeetResult> {
        return try {
            val favoriteInfo = leetManager.getFavoriteLeetInfo()

            val result = when {
                favoriteInfo == null -> FavoriteLeetResult.simple()
                favoriteInfo.mode == LeetManager.MODE_SIMPLE -> FavoriteLeetResult.simple()
                favoriteInfo.mode == LeetManager.MODE_EXTENDED -> FavoriteLeetResult.extended()
                favoriteInfo.mode == LeetManager.MODE_CUSTOM -> {
                    if (favoriteInfo.customLeet != null) {
                        FavoriteLeetResult.custom(favoriteInfo.customIndex, favoriteInfo.customLeet)
                    } else {
                        FavoriteLeetResult.simple() // Fallback
                    }
                }
                else -> FavoriteLeetResult.simple() // Fallback für unbekannte Modi
            }

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a leet with simple defaults
     * FIXED: Updated parameter to use ImageVector
     */
    suspend fun createLeetWithSimpleDefaults(name: String, icon: ImageVector = Icons.Default.Settings): Result<CustomLeet> {
        return try {
            val leet = CustomLeet.createWithSimpleDefaults(name, icon)
            when (val result = leetManager.addLeet(leet)) { // FIXED: Use addLeet directly
                is ErrorHandler.Result.Success -> Result.success(leet) // FIXED: Return the leet, not result.data
                is ErrorHandler.Result.Error -> Result.failure(result.exception)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a leet with extended defaults
     * FIXED: Updated parameter to use ImageVector
     */
    suspend fun createLeetWithExtendedDefaults(name: String, icon: ImageVector = Icons.Default.Settings): Result<CustomLeet> {
        return try {
            val leet = CustomLeet.createWithExtendedDefaults(name, icon)
            when (val result = leetManager.addLeet(leet)) { // FIXED: Use addLeet directly
                is ErrorHandler.Result.Success -> Result.success(leet) // FIXED: Return the leet, not result.data
                is ErrorHandler.Result.Error -> Result.failure(result.exception)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets all leet options for UI display
     */
    fun getLeetOptions(): Flow<List<LeetOption>> = combine(
        leets,
        currentLeetIndex,
        favoriteIndex
    ) { leets, currentIndex, favoriteIndex ->
        buildList {
            // Add Simple Leet
            add(LeetOption.createSimple(
                isSelected = currentIndex == -1, // No custom leet selected
                isFavorite = favoriteIndex == LeetManager.FAV_SIMPLE
            ))

            // Add Extended Leet
            add(LeetOption.createExtended(
                isSelected = currentIndex == -2, // Extended mode indicator
                isFavorite = favoriteIndex == LeetManager.FAV_EXTENDED
            ))

            // Add Custom Leets
            leets.forEachIndexed { index, leet ->
                add(LeetOption.createCustom(
                    leet = leet,
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
     * Gets the current leet synchronously (for compatibility)
     */
    fun getCurrentLeet(): CustomLeet? = currentLeet.value

    /**
     * Gets the current leet index synchronously
     */
    fun getCurrentLeetIndex(): Int = currentLeetIndex.value

    /**
     * Checks if leets exist
     */
    fun hasLeets(): Boolean = hasLeets.value

    /**
     * Gets all leets synchronously
     */
    fun getLeets(): List<CustomLeet> = leets.value

    /**
     * Cleanup resources
     */
    fun cleanup() {
        leetManager.cleanup()
    }

    // Legacy compatibility for gradual migration
    fun getLeetManager(): LeetManager = leetManager

    /**
     * Data class for leet creation request
     * FIXED: Updated to use ImageVector
     */
    data class LeetCreationRequest(
        val name: String,
        val iconImageVector: ImageVector, // FIXED: Changed from iconResId
        val translations: Map<String, String>
    )

    /**
     * Result classes for operations
     */
    data class LeetCreationResult(
        val leet: CustomLeet,
        val index: Int,
        val success: Boolean,
        val message: String
    )

    data class LeetUpdateResult(
        val leet: CustomLeet,
        val index: Int,
        val success: Boolean,
        val message: String
    )

    data class LeetDeletionResult(
        val deletedLeet: CustomLeet,
        val wasFavorite: Boolean,
        val wasLastLeet: Boolean,
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
     * Favorite leet result sealed class
     */
    sealed class FavoriteLeetResult {
        abstract val mode: Int

        data class Simple(override val mode: Int = LeetManager.MODE_SIMPLE) : FavoriteLeetResult()
        data class Extended(override val mode: Int = LeetManager.MODE_EXTENDED) : FavoriteLeetResult()
        data class Custom(
            override val mode: Int = LeetManager.MODE_CUSTOM,
            val customIndex: Int,
            val leet: CustomLeet
        ) : FavoriteLeetResult()

        companion object {
            fun simple() = Simple()
            fun extended() = Extended()
            fun custom(index: Int, leet: CustomLeet) = Custom(customIndex = index, leet = leet)
        }
    }
}