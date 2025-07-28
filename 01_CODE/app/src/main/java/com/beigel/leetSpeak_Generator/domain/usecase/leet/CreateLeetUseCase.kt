package com.beigel.leetSpeak_Generator.domain.usecase.leet

import androidx.compose.ui.graphics.vector.ImageVector
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case für das Erstellen eines neuen Leets
 * FIXED: Erweitert um individuelle Übersetzungen
 */
@Singleton
class CreateLeetUseCase @Inject constructor(
    private val repository: LeetRepository
) {
    suspend operator fun invoke(
        name: String,
        iconImageVector: ImageVector,
        useExtendedDefaults: Boolean = false,
        customTranslations: Map<String, String>? = null // NEU: Individuelle Übersetzungen
    ): Result<CustomLeet> {

        // FIXED: Wenn individuelle Übersetzungen bereitgestellt wurden, verwende diese
        return if (customTranslations != null) {
            // Erstelle Leet mit individuellen Übersetzungen
            val customLeet = CustomLeet(name, iconImageVector)
            customLeet.setTranslations(customTranslations)

            // Füge zum Repository hinzu
            repository.addCustomLeet(customLeet)
        } else {
            // Fallback zu Standard-Verhalten
            if (useExtendedDefaults) {
                repository.createLeetWithExtendedDefaults(name, iconImageVector)
            } else {
                repository.createLeetWithSimpleDefaults(name, iconImageVector)
            }
        }
    }
}