package com.beigel.leetSpeak_Generator.domain.usecase.leet

import com.beigel.leetSpeak_Generator.repository.LeetRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für das Löschen eines Leets
 */
@Singleton
class DeleteLeetUseCase @Inject constructor(
    private val repository: LeetRepository
) {
    suspend operator fun invoke(
        index: Int
    ): Result<LeetRepository.LeetDeletionResult> {
        return repository.deleteLeet(index)
    }
}