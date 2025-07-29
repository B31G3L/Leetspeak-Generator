// app/src/test/java/com/beigel/leetSpeak_Generator/translation/LeetTranslatorTest.kt
package com.beigel.leetSpeak_Generator.translation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.beigel.leetSpeak_Generator.data.CustomLeet
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Umfassende Tests für LeetTranslator
 * FIXED: Alle Tests korrigiert und erweitert
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

    @Test
    fun `translate simple mode preserves case for unmapped characters`() {
        val result = LeetTranslator.translate("HeLLo", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("#3LL0", result) // All converted to uppercase internally
    }

    @Test
    fun `translate simple mode with numbers and symbols`() {
        val result = LeetTranslator.translate("HELLO123!@#", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("#3LL0123!@#", result)
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

    @Test
    fun `translate extended mode complete alphabet`() {
        val input = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val expected = "48(|)3|=6#!_||<1/\\/\\|\\|09Q_25T|_|\\/\\/><`/Z"
        val result = LeetTranslator.translate(input, LeetTranslator.TranslationMode.EXTENDED)
        assertEquals(expected, result)
    }

    // ===== CUSTOM MODE TESTS =====

    @Test
    fun `translate custom mode with custom mappings`() {
        val result = LeetTranslator.translate("AEO", LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals("@€Ø", result)
    }

    @Test
    fun `translate custom mode fallback to simple for unmapped characters`() {
        val result = LeetTranslator.translate("HELLO", LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals("#@LLØ", result) // H→#, E→@, L→L, L→L, O→Ø
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

    @Test
    fun `translate whitespace only string`() {
        val result = LeetTranslator.translate("   ", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("   ", result)
    }

    @Test
    fun `translate mixed case input`() {
        val result = LeetTranslator.translate("HeLLo WoRLd", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("#3LL0 W0RLd", result)
    }

    // ===== CHARACTER-LEVEL TESTS =====

    @Test
    fun `translateChar simple mode individual characters`() {
        assertEquals("4", LeetTranslator.translateChar('A', LeetTranslator.TranslationMode.SIMPLE))
        assertEquals("3", LeetTranslator.translateChar('E', LeetTranslator.TranslationMode.SIMPLE))
        assertEquals("0", LeetTranslator.translateChar('O', LeetTranslator.TranslationMode.SIMPLE))
        assertEquals("X", LeetTranslator.translateChar('X', LeetTranslator.TranslationMode.SIMPLE))
    }

    @Test
    fun `translateChar extended mode individual characters`() {
        assertEquals("/\\/\\", LeetTranslator.translateChar('M', LeetTranslator.TranslationMode.EXTENDED))
        assertEquals("|\\|", LeetTranslator.translateChar('N', LeetTranslator.TranslationMode.EXTENDED))
        assertEquals("|_|", LeetTranslator.translateChar('U', LeetTranslator.TranslationMode.EXTENDED))
    }

    @Test
    fun `translateChar custom mode with fallback`() {
        assertEquals("@", LeetTranslator.translateChar('A', LeetTranslator.TranslationMode.CUSTOM, customLeet))
        assertEquals("#", LeetTranslator.translateChar('H', LeetTranslator.TranslationMode.CUSTOM, customLeet))
    }

    // ===== UTILITY FUNCTION TESTS =====

    @Test
    fun `hasTranslation simple mode`() {
        assertTrue(LeetTranslator.hasTranslation('A', LeetTranslator.TranslationMode.SIMPLE))
        assertTrue(LeetTranslator.hasTranslation('E', LeetTranslator.TranslationMode.SIMPLE))
        assertFalse(LeetTranslator.hasTranslation('X', LeetTranslator.TranslationMode.SIMPLE))
    }

    @Test
    fun `hasTranslation extended mode`() {
        assertTrue(LeetTranslator.hasTranslation('M', LeetTranslator.TranslationMode.EXTENDED))
        assertTrue(LeetTranslator.hasTranslation('N', LeetTranslator.TranslationMode.EXTENDED))
        assertFalse(LeetTranslator.hasTranslation('Q', LeetTranslator.TranslationMode.EXTENDED))
    }

    @Test
    fun `getTranslatableChars returns correct sets`() {
        val simpleChars = LeetTranslator.getTranslatableChars(LeetTranslator.TranslationMode.SIMPLE)
        assertTrue(simpleChars.contains('A'))
        assertTrue(simpleChars.contains('E'))
        assertFalse(simpleChars.contains('X'))

        val extendedChars = LeetTranslator.getTranslatableChars(LeetTranslator.TranslationMode.EXTENDED)
        assertTrue(extendedChars.contains('M'))
        assertTrue(extendedChars.contains('N'))
    }

    @Test
    fun `getTranslationMap returns correct maps`() {
        val simpleMap = LeetTranslator.getTranslationMap(LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("4", simpleMap['A'])
        assertEquals("3", simpleMap['E'])
        assertEquals("0", simpleMap['O'])

        val extendedMap = LeetTranslator.getTranslationMap(LeetTranslator.TranslationMode.EXTENDED)
        assertEquals("/\\/\\", extendedMap['M'])
        assertEquals("|\\|", extendedMap['N'])
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

    @Test
    fun `createPreview extended mode`() {
        val preview = LeetTranslator.createPreview(
            LeetTranslator.TranslationMode.EXTENDED,
            sampleText = "MENU"
        )
        assertEquals("/\\/\\3|\\||_|", preview)
    }

    @Test
    fun `createPreview custom mode`() {
        val preview = LeetTranslator.createPreview(
            LeetTranslator.TranslationMode.CUSTOM,
            customLeet,
            "AEO"
        )
        assertEquals("@€Ø", preview)
    }

    // ===== TRANSLATION STATS TESTS =====

    @Test
    fun `analyzeTranslation simple mode statistics`() {
        val stats = LeetTranslator.analyzeTranslation(
            "HELLO",
            LeetTranslator.TranslationMode.SIMPLE
        )

        assertEquals(5, stats.totalChars)
        assertEquals(3, stats.translatedChars) // H→#, E→3, O→0
        assertEquals(2, stats.unchangedChars)   // L, L remain L
        assertEquals(60.0f, stats.translationPercentage, 0.1f) // 3/5 = 60%
    }

    @Test
    fun `analyzeTranslation extended mode statistics`() {
        val stats = LeetTranslator.analyzeTranslation(
            "HELLO",
            LeetTranslator.TranslationMode.EXTENDED
        )

        assertEquals(5, stats.totalChars)
        assertEquals(4, stats.translatedChars) // H→#, E→3, L→1, O→0
        assertEquals(1, stats.unchangedChars)   // L remains L (only one L gets translated)
        assertEquals(80.0f, stats.translationPercentage, 0.1f)
    }

    @Test
    fun `analyzeTranslation empty string`() {
        val stats = LeetTranslator.analyzeTranslation(
            "",
            LeetTranslator.TranslationMode.SIMPLE
        )

        assertEquals(LeetTranslator.TranslationStats.empty(), stats)
    }

    @Test
    fun `analyzeTranslation custom mode`() {
        val stats = LeetTranslator.analyzeTranslation(
            "AEO",
            LeetTranslator.TranslationMode.CUSTOM,
            customLeet
        )

        assertEquals(3, stats.totalChars)
        assertEquals(3, stats.translatedChars) // A→@, E→€, O→Ø
        assertEquals(0, stats.unchangedChars)
        assertEquals(100.0f, stats.translationPercentage, 0.1f)
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

    // ===== STRING EXTENSION TESTS =====

    @Test
    fun `string extension toLeet function`() {
        val result = "HELLO".toLeet(LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("#3LL0", result)
    }

    @Test
    fun `string extension toLeet with custom leet`() {
        val result = "AEO".toLeet(LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals("@€Ø", result)
    }
}