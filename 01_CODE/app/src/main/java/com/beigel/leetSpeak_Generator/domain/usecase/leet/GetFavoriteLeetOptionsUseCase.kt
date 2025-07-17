package com.beigel.leetSpeak_Generator.domain.usecase.leet

import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für das Laden der Favoriten-Leets
 */
@Singleton
class GetFavoriteLeetOptionsUseCase @Inject constructor(
    private val repository: LeetRepository
) {
    operator fun invoke(): Flow<List<LeetOption>> {
        return repository.getFavoriteLeetOptions()
    }
}