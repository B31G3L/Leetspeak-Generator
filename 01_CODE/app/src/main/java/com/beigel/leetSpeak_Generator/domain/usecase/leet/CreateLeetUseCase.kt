package com.beigel.leetSpeak_Generator.domain.usecase.leet

import androidx.compose.ui.graphics.vector.ImageVector
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für das Erstellen eines neuen Leets
 * FIXED: Updated to use ImageVector instead of Int
 */
@Singleton
class CreateLeetUseCase @Inject constructor(
    private val repository: LeetRepository
) {
    suspend operator fun invoke(
        name: String,
        iconImageVector: ImageVector, // FIXED: Changed from iconResId: Int
        useExtendedDefaults: Boolean = false
    ): Result<CustomLeet> {
        return if (useExtendedDefaults) {
            repository.createLeetWithExtendedDefaults(name, iconImageVector)
        } else {
            repository.createLeetWithSimpleDefaults(name, iconImageVector)
        }
    }
}