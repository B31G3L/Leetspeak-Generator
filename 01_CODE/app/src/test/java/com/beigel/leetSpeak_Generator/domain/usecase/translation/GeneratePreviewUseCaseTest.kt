package com.beigel.leetSpeak_Generator.domain.usecase.translation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GeneratePreviewUseCaseTest {

    private lateinit var useCase: GeneratePreviewUseCase

    @Before
    fun setUp() {
        useCase = GeneratePreviewUseCase()
    }

    @Test
    fun `invoke generates simple mode preview`() {
        val result = useCase(LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("#3LL0", result) // Default "Hello"
    }

    @Test
    fun `invoke generates extended mode preview`() {
        val result = useCase(LeetTranslator.TranslationMode.EXTENDED)
        assertEquals("#3110", result) // Default "Hello"
    }

    @Test
    fun `invoke with custom sample text`() {
        val result = useCase(LeetTranslator.TranslationMode.SIMPLE, sampleText = "TEST")
        assertEquals("735T", result)
    }

    @Test
    fun `invoke with custom leet`() {
        val customLeet = CustomLeet("Test", Icons.Default.Settings).apply {
            setTranslation("H", "!")
            setTranslation("E", "€")
        }

        val result = useCase(LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals("!€LL0", result) // "Hello" with custom mappings
    }
}
