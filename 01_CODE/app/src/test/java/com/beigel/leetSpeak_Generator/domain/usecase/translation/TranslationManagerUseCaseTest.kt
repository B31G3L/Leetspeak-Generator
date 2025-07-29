package com.beigel.leetSpeak_Generator.domain.usecase.translation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TranslationManagerUseCaseTest {

    private lateinit var translateTextUseCase: TranslateTextUseCase
    private lateinit var reverseTranslateUseCase: ReverseTranslateUseCase
    private lateinit var detectLeetSpeakUseCase: DetectLeetSpeakUseCase
    private lateinit var analyzeTranslationUseCase: AnalyzeTranslationUseCase
    private lateinit var generatePreviewUseCase: GeneratePreviewUseCase

    private lateinit var translationManager: TranslationManagerUseCase

    @Before
    fun setUp() {
        translateTextUseCase = mockk()
        reverseTranslateUseCase = mockk()
        detectLeetSpeakUseCase = mockk()
        analyzeTranslationUseCase = mockk()
        generatePreviewUseCase = mockk()

        translationManager = TranslationManagerUseCase(
            translateTextUseCase,
            reverseTranslateUseCase,
            detectLeetSpeakUseCase,
            analyzeTranslationUseCase,
            generatePreviewUseCase
        )
    }

    @Test
    fun `translate in normal mode calls translateTextUseCase`() {
        every { translateTextUseCase("Hello", any(), any()) } returns "#3LL0"

        val result = translationManager.translate(
            "Hello",
            LeetTranslator.TranslationMode.SIMPLE,
            null,
            isReverseMode = false
        )

        assertEquals("#3LL0", result)
        verify { translateTextUseCase("Hello", LeetTranslator.TranslationMode.SIMPLE, null) }
        verify(exactly = 0) { reverseTranslateUseCase(any(), any(), any()) }
    }

    @Test
    fun `translate in reverse mode calls reverseTranslateUseCase`() {
        every { reverseTranslateUseCase("#3LL0", any(), any()) } returns "HELLO"

        val result = translationManager.translate(
            "#3LL0",
            LeetTranslator.TranslationMode.SIMPLE,
            null,
            isReverseMode = true
        )

        assertEquals("HELLO", result)
        verify { reverseTranslateUseCase("#3LL0", LeetTranslator.TranslationMode.SIMPLE, null) }
        verify(exactly = 0) { translateTextUseCase(any(), any(), any()) }
    }

    @Test
    fun `isLikelyLeetspeak delegates to detectLeetSpeakUseCase`() {
        every { detectLeetSpeakUseCase("#3LL0") } returns true

        val result = translationManager.isLikelyLeetspeak("#3LL0")

        assertTrue(result)
        verify { detectLeetSpeakUseCase("#3LL0") }
    }

    @Test
    fun `analyzeTranslation delegates to analyzeTranslationUseCase`() {
        val mockStats = mockk<LeetTranslator.TranslationStats>()
        every { analyzeTranslationUseCase("Hello", any(), any()) } returns mockStats

        val result = translationManager.analyzeTranslation(
            "Hello",
            LeetTranslator.TranslationMode.SIMPLE,
            null
        )

        assertEquals(mockStats, result)
        verify { analyzeTranslationUseCase("Hello", LeetTranslator.TranslationMode.SIMPLE, null) }
    }

    @Test
    fun `generatePreview delegates to generatePreviewUseCase`() {
        every { generatePreviewUseCase(any(), any(), any()) } returns "Preview"

        val result = translationManager.generatePreview(
            LeetTranslator.TranslationMode.SIMPLE,
            null,
            "Test"
        )

        assertEquals("Preview", result)
        verify { generatePreviewUseCase(LeetTranslator.TranslationMode.SIMPLE, null, "Test") }
    }

    @Test
    fun `translate with custom leet passes custom leet to use cases`() {
        val customLeet = CustomLeet("Test", Icons.Default.Settings)
        every { translateTextUseCase("Hello", any(), customLeet) } returns "@€LL0"

        val result = translationManager.translate(
            "Hello",
            LeetTranslator.TranslationMode.CUSTOM,
            customLeet,
            isReverseMode = false
        )

        assertEquals("@€LL0", result)
        verify { translateTextUseCase("Hello", LeetTranslator.TranslationMode.CUSTOM, customLeet) }
    }
}