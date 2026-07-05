package com.beigel.leetSpeak_Generator.domain.usecase.leet

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
        useExtendedDefaults: Boolean = false,
        customTranslations: Map<String, String>? = null
    ): Result<CustomLeet> {
        return if (customTranslations != null) {
            val customLeet = CustomLeet(name)
            customLeet.setTranslations(customTranslations)
            repository.addCustomLeet(customLeet)
        } else {
            if (useExtendedDefaults) {
                repository.createLeetWithExtendedDefaults(name)
            } else {
                repository.createLeetWithSimpleDefaults(name)
            }
        }
    }
}