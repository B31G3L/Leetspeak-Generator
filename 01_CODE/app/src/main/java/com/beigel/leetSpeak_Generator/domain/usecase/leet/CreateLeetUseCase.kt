package com.beigel.leetSpeak_Generator.domain.usecase.leet

import androidx.compose.ui.graphics.vector.ImageVector
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CreateLeetUseCase @Inject constructor(
    private val repository: LeetRepository
) {
    suspend operator fun invoke(
        name: String,
        iconImageVector: ImageVector, // Wird ignoriert, aber aus Kompatibilitätsgründen behalten
        useExtendedDefaults: Boolean = false,
        customTranslations: Map<String, String>? = null
    ): Result<CustomLeet> {

        // Wenn individuelle Übersetzungen bereitgestellt wurden, verwende diese
        return if (customTranslations != null) {
            // Erstelle Leet mit individuellen Übersetzungen
            val customLeet = CustomLeet(name)
            customLeet.setTranslations(customTranslations)

            // Füge zum Repository hinzu
            repository.addCustomLeet(customLeet)
        } else {
            // Fallback zu Standard-Verhalten
            if (useExtendedDefaults) {
                repository.createLeetWithExtendedDefaults(name)
            } else {
                repository.createLeetWithSimpleDefaults(name)
            }
        }
    }
}