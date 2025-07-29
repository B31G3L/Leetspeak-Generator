package com.beigel.leetSpeak_Generator.domain.usecase.translation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class TranslateTextUseCaseTest {

    private lateinit var useCase: TranslateTextUseCase
    private lateinit var customLeet: CustomLeet

    @Before
    fun setUp() {
        useCase = TranslateTextUseCase()
        customLeet = CustomLeet("Test", Icons.Default.Settings).apply {
            setTranslation("A", "@")
            setTranslation("E", "€")
        }
    }

    @Test
    fun `invoke with simple mode translates correctly`() {
        val result = useCase("HELLO", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("#3LL0", result)
    }

    @Test
    fun `invoke with extended mode translates correctly`() {
        val result = useCase("HELLO", LeetTranslator.TranslationMode.EXTENDED)
        assertEquals("#3110", result)
    }

    @Test
    fun `invoke with custom mode and custom leet translates correctly`() {
        val result = useCase("AE", LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals("@€", result)
    }

    @Test
    fun `invoke with empty string returns empty`() {
        val result = useCase("", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("", result)
    }

    @Test
    fun `invoke with custom mode but no custom leet falls back to simple`() {
        val result = useCase("HELLO", LeetTranslator.TranslationMode.CUSTOM, null)
        assertEquals("#3LL0", result)
    }
}

