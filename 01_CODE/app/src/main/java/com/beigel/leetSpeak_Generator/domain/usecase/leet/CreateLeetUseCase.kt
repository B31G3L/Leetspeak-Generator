package com.beigel.leetSpeak_Generator.domain.usecase.leet

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für das Erstellen eines neuen Leets
 */
@Singleton
class CreateLeetUseCase @Inject constructor(
    private val repository: LeetRepository
) {
    suspend operator fun invoke(
        name: String,
        iconResId: Int,
        useExtendedDefaults: Boolean = false
    ): Result<CustomLeet> {
        return if (useExtendedDefaults) {
            repository.createLeetWithExtendedDefaults(name, iconResId)
        } else {
            repository.createLeetWithSimpleDefaults(name, iconResId)
        }
    }
}