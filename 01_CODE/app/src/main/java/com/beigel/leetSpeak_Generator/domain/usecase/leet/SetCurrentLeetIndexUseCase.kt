package com.beigel.leetSpeak_Generator.domain.usecase.leet

import com.beigel.leetSpeak_Generator.repository.LeetRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für das Setzen des aktuellen Leet-Index
 */
@Singleton
class SetCurrentLeetIndexUseCase @Inject constructor(
    private val repository: LeetRepository
) {
    suspend operator fun invoke(index: Int): Result<Unit> {
        return repository.setCurrentLeetIndex(index)
    }
}