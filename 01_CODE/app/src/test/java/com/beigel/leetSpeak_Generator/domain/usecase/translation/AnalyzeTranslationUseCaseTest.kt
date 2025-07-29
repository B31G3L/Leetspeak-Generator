package com.beigel.leetSpeak_Generator.domain.usecase.translation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AnalyzeTranslationUseCaseTest {

    private lateinit var useCase: AnalyzeTranslationUseCase

    @Before
    fun setUp() {
        useCase = AnalyzeTranslationUseCase()
    }

    @Test
    fun `invoke analyzes simple translation correctly`() {
        val stats = useCase("HELLO", LeetTranslator.TranslationMode.SIMPLE)

        assertEquals(5, stats.totalChars)
        assertEquals(3, stats.translatedChars) // H, E, O
        assertEquals(2, stats.unchangedChars)   // L, L
        assertEquals(60.0f, stats.translationPercentage, 0.1f)
    }

    @Test
    fun `invoke analyzes extended translation correctly`() {
        val stats = useCase("HELLO", LeetTranslator.TranslationMode.EXTENDED)

        assertEquals(5, stats.totalChars)
        assertEquals(4, stats.translatedChars) // H, E, L, O
        assertEquals(1, stats.unchangedChars)   // L
        assertEquals(80.0f, stats.translationPercentage, 0.1f)
    }

    @Test
    fun `invoke with empty string returns empty stats`() {
        val stats = useCase("", LeetTranslator.TranslationMode.SIMPLE)

        assertEquals(LeetTranslator.TranslationStats.empty(), stats)
    }

    @Test
    fun `invoke with custom leet analyzes correctly`() {
        val customLeet = CustomLeet("Test", Icons.Default.Settings).apply {
            setTranslation("A", "@")
            setTranslation("E", "€")
            setTranslation("O", "Ø")
        }

        val stats = useCase("AEO", LeetTranslator.TranslationMode.CUSTOM, customLeet)

        assertEquals(3, stats.totalChars)
        assertEquals(3, stats.translatedChars)
        assertEquals(0, stats.unchangedChars)
        assertEquals(100.0f, stats.translationPercentage, 0.1f)
    }
}