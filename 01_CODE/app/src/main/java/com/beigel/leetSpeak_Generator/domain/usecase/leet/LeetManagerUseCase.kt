package com.beigel.leetSpeak_Generator.domain.usecase.leet

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeetManagerUseCase @Inject constructor(
    private val getLeetOptionsUseCase: GetLeetOptionsUseCase,
    private val getFavoriteLeetOptionsUseCase: GetFavoriteLeetOptionsUseCase,
    private val createLeetUseCase: CreateLeetUseCase,
    private val updateLeetUseCase: UpdateLeetUseCase,
    private val deleteLeetUseCase: DeleteLeetUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val loadFavoriteLeetUseCase: LoadFavoriteLeetUseCase,
    private val setCurrentLeetIndexUseCase: SetCurrentLeetIndexUseCase
) {
    fun getLeetOptions(): Flow<List<LeetOption>> = getLeetOptionsUseCase()

    fun getFavoriteLeetOptions(): Flow<List<LeetOption>> = getFavoriteLeetOptionsUseCase()

    suspend fun createLeet(
        name: String,
        useExtendedDefaults: Boolean = false,
        customTranslations: Map<String, String>? = null
    ): Result<CustomLeet> = createLeetUseCase(name, useExtendedDefaults, customTranslations)

    suspend fun updateLeet(
        index: Int,
        leet: CustomLeet
    ): Result<LeetRepository.LeetUpdateResult> = updateLeetUseCase(index, leet)

    suspend fun deleteLeet(
        index: Int
    ): Result<LeetRepository.LeetDeletionResult> = deleteLeetUseCase(index)

    suspend fun toggleFavorite(
        leetOption: LeetOption
    ): Result<LeetRepository.FavoriteToggleResult> =
        toggleFavoriteUseCase(leetOption.mode, leetOption.customIndex)

    suspend fun loadFavoriteLeet(): Result<LeetRepository.FavoriteLeetResult> =
        loadFavoriteLeetUseCase()

    suspend fun changeMode(leetOption: LeetOption): Result<Unit> =
        if (leetOption.isCustom && leetOption.customIndex >= 0) {
            setCurrentLeetIndexUseCase(leetOption.customIndex)
        } else {
            Result.success(Unit)
        }
}