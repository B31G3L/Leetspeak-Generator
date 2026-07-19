package com.beigel.leetSpeak_Generator.repository

import android.content.Context
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.manager.LeetManager
import com.beigel.leetSpeak_Generator.utils.ErrorHandler
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeetRepository @Inject constructor(
    private val context: Context
) {
    private val leetManager = LeetManager(context)

    val leets:            StateFlow<List<CustomLeet>> = leetManager.leets
    val currentLeet:      StateFlow<CustomLeet?>      = leetManager.currentLeet
    val currentLeetIndex: StateFlow<Int>              = leetManager.currentLeetIndex
    val hasLeets:         StateFlow<Boolean>          = leetManager.hasLeets
    val favoriteIndices:  StateFlow<Set<Int>>         = leetManager.favoriteIndices
    val optionsOrder:     StateFlow<List<Int>>        = leetManager.optionsOrder

    suspend fun createLeet(request: LeetCreationRequest): Result<LeetCreationResult> {
        return try {
            val leet = CustomLeet(request.name)
            leet.setTranslations(request.translations)
            when (val indexResult = leetManager.addLeet(leet)) {
                is ErrorHandler.Result.Success -> Result.success(
                    LeetCreationResult(
                        leet, indexResult.data, true,
                        context.getString(R.string.success_leet_created_repo)
                    )
                )
                is ErrorHandler.Result.Error -> Result.failure(indexResult.exception)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateLeet(index: Int, leet: CustomLeet): Result<LeetUpdateResult> {
        return try {
            when (val result = leetManager.updateLeet(index, leet)) {
                is ErrorHandler.Result.Success -> Result.success(
                    LeetUpdateResult(
                        leet, index, true,
                        context.getString(R.string.success_leet_updated_repo)
                    )
                )
                is ErrorHandler.Result.Error -> Result.failure(result.exception)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteLeet(index: Int): Result<LeetDeletionResult> {
        return try {
            when (val result = leetManager.deleteLeet(index)) {
                is ErrorHandler.Result.Success -> Result.success(
                    LeetDeletionResult(
                        result.data.deletedLeet, result.data.wasFavorite,
                        result.data.wasLastLeet, true,
                        context.getString(R.string.success_leet_deleted_repo)
                    )
                )
                is ErrorHandler.Result.Error -> Result.failure(result.exception)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun insertLeetAt(index: Int, leet: CustomLeet): Result<Unit> {
        return try {
            when (val result = leetManager.insertLeetAt(index, leet)) {
                is ErrorHandler.Result.Success -> Result.success(Unit)
                is ErrorHandler.Result.Error   -> Result.failure(result.exception)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun setCurrentLeetIndex(index: Int): Result<Unit> {
        return try {
            when (val result = leetManager.setCurrentLeetIndex(index)) {
                is ErrorHandler.Result.Success -> Result.success(Unit)
                is ErrorHandler.Result.Error   -> Result.failure(result.exception)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun toggleFavorite(mode: Int, customIndex: Int = 0): Result<FavoriteToggleResult> {
        return try {
            val wasAlreadyFavorite = leetManager.isFavorite(mode, customIndex)
            when (val result = leetManager.toggleFavorite(mode, customIndex)) {
                is ErrorHandler.Result.Success -> Result.success(
                    FavoriteToggleResult(mode, customIndex, wasAlreadyFavorite, result.data, true)
                )
                is ErrorHandler.Result.Error -> Result.failure(result.exception)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun loadFavoriteLeet(): Result<FavoriteLeetResult> {
        return try {
            val favoriteInfo = leetManager.getFavoriteLeetInfos().firstOrNull()
            val result = when {
                favoriteInfo == null                              -> FavoriteLeetResult.simple()
                favoriteInfo.mode == LeetManager.MODE_SIMPLE     -> FavoriteLeetResult.simple()
                favoriteInfo.mode == LeetManager.MODE_EXTENDED   -> FavoriteLeetResult.extended()
                favoriteInfo.mode == LeetManager.MODE_CUSTOM     -> {
                    if (favoriteInfo.customLeet != null)
                        FavoriteLeetResult.custom(favoriteInfo.customIndex, favoriteInfo.customLeet)
                    else FavoriteLeetResult.simple()
                }
                else -> FavoriteLeetResult.simple()
            }
            Result.success(result)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun reorderOptions(fromIdentifier: Int, toIdentifier: Int): Result<Unit> {
        return try {
            when (val result = leetManager.reorderOptions(fromIdentifier, toIdentifier)) {
                is ErrorHandler.Result.Success -> Result.success(Unit)
                is ErrorHandler.Result.Error   -> Result.failure(result.exception)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun createLeetWithSimpleDefaults(name: String): Result<CustomLeet> {
        return try {
            val leet = CustomLeet.createWithSimpleDefaults(name)
            when (val result = leetManager.addLeet(leet)) {
                is ErrorHandler.Result.Success -> Result.success(leet)
                is ErrorHandler.Result.Error   -> Result.failure(result.exception)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun createLeetWithExtendedDefaults(name: String): Result<CustomLeet> {
        return try {
            val leet = CustomLeet.createWithExtendedDefaults(name)
            when (val result = leetManager.addLeet(leet)) {
                is ErrorHandler.Result.Success -> Result.success(leet)
                is ErrorHandler.Result.Error   -> Result.failure(result.exception)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun addCustomLeet(customLeet: CustomLeet): Result<CustomLeet> {
        return try {
            when (val result = leetManager.addLeet(customLeet)) {
                is ErrorHandler.Result.Success -> Result.success(customLeet)
                is ErrorHandler.Result.Error   -> Result.failure(result.exception)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    fun getLeetOptions(): Flow<List<LeetOption>> = combine(
        leets, favoriteIndices, optionsOrder
    ) { leets, favIndices, order ->
        // Alle Optionen aufbauen
        val allOptions = mutableMapOf<Int, LeetOption>()

        allOptions[LeetManager.FAV_SIMPLE] = LeetOption.createSimple(
            context,
            isSelected = false,
            isFavorite = LeetManager.FAV_SIMPLE in favIndices
        )
        allOptions[LeetManager.FAV_EXTENDED] = LeetOption.createExtended(
            context,
            isSelected = false,
            isFavorite = LeetManager.FAV_EXTENDED in favIndices
        )
        leets.forEachIndexed { index, leet ->
            allOptions[index] = LeetOption.createCustom(
                context, leet, index,
                isSelected = false,
                isFavorite = index in favIndices
            )
        }

        // Reihenfolge anwenden – neue Custom Leets die noch nicht in order sind, anhängen
        val orderedKeys = order.toMutableList()
        allOptions.keys.forEach { key ->
            if (key !in orderedKeys) orderedKeys.add(key)
        }

        orderedKeys.mapNotNull { allOptions[it] }
    }

    fun getFavoriteLeetOptions(): Flow<List<LeetOption>> =
        getLeetOptions().map { options -> options.filter { it.isFavorite } }

    fun isFavorite(mode: Int, customIndex: Int = 0): Boolean =
        leetManager.isFavorite(mode, customIndex)

    fun getCurrentLeet(): CustomLeet? = currentLeet.value
    fun getCurrentLeetIndex(): Int    = currentLeetIndex.value
    fun hasLeets(): Boolean           = hasLeets.value
    fun getLeets(): List<CustomLeet>  = leets.value
    fun cleanup()                     = leetManager.cleanup()

    data class LeetCreationRequest(val name: String, val translations: Map<String, String>)
    data class LeetCreationResult(val leet: CustomLeet, val index: Int, val success: Boolean, val message: String)
    data class LeetUpdateResult(val leet: CustomLeet, val index: Int, val success: Boolean, val message: String)
    data class LeetDeletionResult(val deletedLeet: CustomLeet, val wasFavorite: Boolean, val wasLastLeet: Boolean, val success: Boolean, val message: String)
    data class FavoriteToggleResult(val mode: Int, val customIndex: Int, val wasAlreadyFavorite: Boolean, val isNowFavorite: Boolean, val success: Boolean)

    sealed class FavoriteLeetResult {
        abstract val mode: Int
        data class Simple(override val mode: Int = LeetManager.MODE_SIMPLE) : FavoriteLeetResult()
        data class Extended(override val mode: Int = LeetManager.MODE_EXTENDED) : FavoriteLeetResult()
        data class Custom(override val mode: Int = LeetManager.MODE_CUSTOM, val customIndex: Int, val leet: CustomLeet) : FavoriteLeetResult()

        companion object {
            fun simple()                              = Simple()
            fun extended()                            = Extended()
            fun custom(index: Int, leet: CustomLeet) = Custom(customIndex = index, leet = leet)
        }
    }
}