package com.beigel.leetSpeak_Generator.domain.usecase.translation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ReverseTranslateUseCaseTest {

    private lateinit var useCase: ReverseTranslateUseCase
    private lateinit var customLeet: CustomLeet

    @Before
    fun setUp() {
        useCase = ReverseTranslateUseCase()
        customLeet = CustomLeet("Test", Icons.Default.Settings).apply {
            setTranslation("A", "@")
            setTranslation("E", "€")
        }
    }

    @Test
    fun `invoke with simple mode reverses correctly`() {
        val result = useCase("#3LL0", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("HELLO", result)
    }

    @Test
    fun `invoke with extended mode reverses correctly`() {
        val result = useCase("#3110", LeetTranslator.TranslationMode.EXTENDED)
        assertEquals("HELLO", result)
    }

    @Test
    fun `invoke with custom mode reverses correctly`() {
        val result = useCase("@€", LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals("AE", result)
    }

    @Test
    fun `invoke with empty string returns empty`() {
        val result = useCase("", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("", result)
    }
}