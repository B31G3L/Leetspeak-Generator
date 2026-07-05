package com.beigel.leetSpeak_Generator.domain.usecase.leet

import com.beigel.leetSpeak_Generator.repository.LeetRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für das Laden des Favoriten-Leets
 */
@Singleton
class LoadFavoriteLeetUseCase @Inject constructor(
    private val repository: LeetRepository
) {
    suspend operator fun invoke(): Result<LeetRepository.FavoriteLeetResult> {
        return repository.loadFavoriteLeet()
    }
}