package com.beigel.leetSpeak_Generator.domain.usecase.leet

import com.beigel.leetSpeak_Generator.repository.LeetRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für das Umschalten der Favoriten-Markierung
 */
@Singleton
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: LeetRepository
) {
    suspend operator fun invoke(
        mode: Int,
        customIndex: Int = 0
    ): Result<LeetRepository.FavoriteToggleResult> {
        return repository.toggleFavorite(mode, customIndex)
    }
}