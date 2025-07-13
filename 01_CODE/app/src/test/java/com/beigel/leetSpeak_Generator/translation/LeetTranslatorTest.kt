package com.beigel.leetSpeak_Generator.translation

import com.beigel.leetSpeak_Generator.data.CustomLeet
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests für LeetTranslator - Migriert von Java zu Kotlin
 */
class LeetTranslatorTest {

    @Test
    fun testSimpleTranslation() {
        val result = LeetTranslator.translate("HELLO", LeetTranslator.TranslationMode.SIMPLE, null)
        assertEquals("#3LL0", result)
    }

    @Test
    fun testExtendedTranslation() {
        val result = LeetTranslator.translate("HELLO", LeetTranslator.TranslationMode.EXTENDED, null)
        assertEquals("#3110", result)
    }

    @Test
    fun testMixedCase() {
        val result = LeetTranslator.translate("Hello", LeetTranslator.TranslationMode.SIMPLE, null)
        assertEquals("#3LL0", result)
    }

    @Test
    fun testSpecialCharacters() {
        val result = LeetTranslator.translate("HELLO!", LeetTranslator.TranslationMode.SIMPLE, null)
        assertEquals("#3LL0!", result)
    }

    @Test
    fun testNumbers() {
        val result = LeetTranslator.translate("ABC123", LeetTranslator.TranslationMode.SIMPLE, null)
        assertEquals("48C123", result)
    }

    @Test
    fun testEmptyString() {
        val result = LeetTranslator.translate("", LeetTranslator.TranslationMode.SIMPLE, null)
        assertEquals("", result)
    }

    @Test
    fun testNullInput() {
        val result = LeetTranslator.translate(null, LeetTranslator.TranslationMode.SIMPLE, null)
        assertEquals("", result) // Updated: returns empty string, not null
    }

    @Test
    fun testCustomLeet() {
        val customLeet = CustomLeet("Test")
        customLeet.setTranslation("A", "@")
        customLeet.setTranslation("E", "€")

        val result = LeetTranslator.translate("AE", LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals("@€", result)
    }

    @Test
    fun testCustomLeetFallback() {
        val result = LeetTranslator.translate("HELLO", LeetTranslator.TranslationMode.CUSTOM, null)
        // Should fallback to simple mode
        assertEquals("#3LL0", result)
    }

    @Test
    fun testTranslationStats() {
        val stats = LeetTranslator.analyzeTranslation(
            "HELLO",
            LeetTranslator.TranslationMode.SIMPLE,
            null
        )
        assertEquals(5, stats.totalChars)
        assertEquals(3, stats.translatedChars) // H, E, O → #, 3, 0
        assertEquals(2, stats.unchangedChars)   // L, L → L, L
    }

    @Test
    fun testCreatePreview() {
        val preview = LeetTranslator.createPreview(
            LeetTranslator.TranslationMode.SIMPLE,
            sampleText = "Test"
        )
        assertEquals("735t", preview)
    }
}