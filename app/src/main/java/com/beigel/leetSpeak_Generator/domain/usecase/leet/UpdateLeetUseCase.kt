package com.beigel.leetSpeak_Generator.domain.usecase.leet

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für das Aktualisieren eines bestehenden Leets
 */
@Singleton
class UpdateLeetUseCase @Inject constructor(
    private val repository: LeetRepository
) {
    suspend operator fun invoke(
        index: Int,
        leet: CustomLeet
    ): Result<LeetRepository.LeetUpdateResult> {
        return repository.updateLeet(index, leet)
    }
}