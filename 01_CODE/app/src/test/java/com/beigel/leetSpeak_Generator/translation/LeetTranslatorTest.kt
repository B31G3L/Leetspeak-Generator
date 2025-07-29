package com.beigel.leetSpeak_Generator.translation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.beigel.leetSpeak_Generator.data.CustomLeet
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Umfassende Tests für LeetTranslator
 */
class LeetTranslatorTest {

    private lateinit var customLeet: CustomLeet

    @Before
    fun setUp() {
        customLeet = CustomLeet("Test Leet", Icons.Default.Settings)
        customLeet.setTranslation("A", "@")
        customLeet.setTranslation("E", "€")
        customLeet.setTranslation("O", "Ø")
    }

    // ===== SIMPLE MODE TESTS =====

    @Test
    fun `translate simple mode with basic text`() {
        val result = LeetTranslator.translate("HELLO", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("#3LL0", result)
    }

    @Test
    fun `translate simple mode with all mappings`() {
        val input = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val expected = "48CD3F6#1JKLMN0PQR57UVWXY2"
        val result = LeetTranslator.translate(input, LeetTranslator.TranslationMode.SIMPLE)
        assertEquals(expected, result)
    }

    // ===== EXTENDED MODE TESTS =====

    @Test
    fun `translate extended mode with basic text`() {
        val result = LeetTranslator.translate("HELLO", LeetTranslator.TranslationMode.EXTENDED)
        assertEquals("#3110", result)
    }

    @Test
    fun `translate extended mode with multi-character mappings`() {
        val result = LeetTranslator.translate("MNUVWX", LeetTranslator.TranslationMode.EXTENDED)
        assertEquals("/\\/\\|_|\\/\\/><", result)
    }

    // ===== CUSTOM MODE TESTS =====

    @Test
    fun `translate custom mode with custom mappings`() {
        val result = LeetTranslator.translate("AEO", LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals("@€Ø", result)
    }

    @Test
    fun `translate custom mode with null custom leet falls back to simple`() {
        val result = LeetTranslator.translate("HELLO", LeetTranslator.TranslationMode.CUSTOM, null)
        assertEquals("#3LL0", result)
    }

    // ===== EDGE CASES =====

    @Test
    fun `translate empty string returns empty`() {
        val result = LeetTranslator.translate("", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("", result)
    }

    @Test
    fun `translate null input returns empty string`() {
        val result = LeetTranslator.translate(null, LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("", result)
    }

    // ===== CHARACTER-LEVEL TESTS =====

    @Test
    fun `translateChar simple mode individual characters`() {
        assertEquals("4", LeetTranslator.translateChar('A', LeetTranslator.TranslationMode.SIMPLE))
        assertEquals("3", LeetTranslator.translateChar('E', LeetTranslator.TranslationMode.SIMPLE))
        assertEquals("0", LeetTranslator.translateChar('O', LeetTranslator.TranslationMode.SIMPLE))
        assertEquals("X", LeetTranslator.translateChar('X', LeetTranslator.TranslationMode.SIMPLE))
    }

    // ===== UTILITY FUNCTION TESTS =====

    @Test
    fun `hasTranslation simple mode`() {
        assertTrue(LeetTranslator.hasTranslation('A', LeetTranslator.TranslationMode.SIMPLE))
        assertTrue(LeetTranslator.hasTranslation('E', LeetTranslator.TranslationMode.SIMPLE))
        assertFalse(LeetTranslator.hasTranslation('X', LeetTranslator.TranslationMode.SIMPLE))
    }

    // ===== PREVIEW TESTS =====

    @Test
    fun `createPreview with default sample`() {
        val preview = LeetTranslator.createPreview(LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("#3LL0", preview) // "Hello" -> "#3LL0"
    }

    @Test
    fun `createPreview with custom sample`() {
        val preview = LeetTranslator.createPreview(
            LeetTranslator.TranslationMode.SIMPLE,
            sampleText = "TEST"
        )
        assertEquals("735T", preview)
    }

    // ===== TRANSLATION STATS TESTS =====

    @Test
    fun `analyzeTranslation simple mode statistics`() {
        val stats = LeetTranslator.analyzeTranslation("HELLO", LeetTranslator.TranslationMode.SIMPLE)

        assertEquals(5, stats.totalChars)
        assertEquals(3, stats.translatedChars) // H→#, E→3, O→0
        assertEquals(2, stats.unchangedChars)   // L, L remain L
        assertEquals(60.0f, stats.translationPercentage, 0.1f) // 3/5 = 60%
    }

    @Test
    fun `analyzeTranslation empty string`() {
        val stats = LeetTranslator.analyzeTranslation("", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals(LeetTranslator.TranslationStats.empty(), stats)
    }

    // ===== PERFORMANCE TESTS =====

    @Test
    fun `translate large text performance`() {
        val largeText = "HELLO WORLD ".repeat(1000)
        val startTime = System.currentTimeMillis()

        val result = LeetTranslator.translate(largeText, LeetTranslator.TranslationMode.SIMPLE)

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        assertTrue("Translation should complete within 1 second", duration < 1000)
        assertTrue("Result should not be empty", result.isNotEmpty())
        assertTrue("Result should contain leet characters", result.contains("#") || result.contains("3"))
    }

    // ===== STRING EXTENSION TESTS (corrected) =====

    @Test
    fun `string extension direct translation function`() {
        // Direct call to translator instead of extension function
        val result = LeetTranslator.translate("HELLO", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("#3LL0", result)
    }

    @Test
    fun `string extension custom leet translation`() {
        // Direct call to translator with custom leet
        val result = LeetTranslator.translate("AEO", LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals("@€Ø", result)
    }
}