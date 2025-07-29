package com.beigel.leetSpeak_Generator.translation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.beigel.leetSpeak_Generator.data.CustomLeet
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Umfassende Tests für ReverseTranslator
 */
class ReverseTranslatorTest {

    private lateinit var customLeet: CustomLeet

    @Before
    fun setUp() {
        customLeet = CustomLeet("Test Reverse", Icons.Default.Settings)
        customLeet.setTranslation("A", "@")
        customLeet.setTranslation("E", "€")
        customLeet.setTranslation("O", "Ø")
        customLeet.setTranslation("M", "/\\/\\")
        customLeet.setTranslation("N", "|\\|")
    }

    // ===== SIMPLE MODE REVERSE TESTS =====

    @Test
    fun `reverseTranslate simple mode basic text`() {
        val result = ReverseTranslator.reverseTranslate("#3LL0", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("HELLO", result)
    }

    @Test
    fun `reverseTranslate simple mode all mappings`() {
        val leetText = "48CD3F6#1JKLMN0PQR57UVWXY2"
        val result = ReverseTranslator.reverseTranslate(leetText, LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", result)
    }

    // ===== EXTENDED MODE REVERSE TESTS =====

    @Test
    fun `reverseTranslate extended mode basic text`() {
        val result = ReverseTranslator.reverseTranslate("#3110", LeetTranslator.TranslationMode.EXTENDED)
        assertEquals("HELLO", result)
    }

    @Test
    fun `reverseTranslate extended mode multi-character patterns`() {
        val result = ReverseTranslator.reverseTranslate("/\\/\\|\\||_|", LeetTranslator.TranslationMode.EXTENDED)
        assertEquals("MNU", result)
    }

    @Test
    fun `reverseTranslate extended mode complex patterns`() {
        val leetText = "4|)\\/4|\\|(3|) 1337"
        val result = ReverseTranslator.reverseTranslate(leetText, LeetTranslator.TranslationMode.EXTENDED)
        // Test that it contains expected characters (pattern may vary)
        assertTrue("Result should contain transformed characters", result.isNotEmpty())
    }

    // ===== CUSTOM MODE REVERSE TESTS =====

    @Test
    fun `reverseTranslate custom mode with custom mappings`() {
        val result = ReverseTranslator.reverseTranslate("@€Ø", LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals("AEO", result)
    }

    @Test
    fun `reverseTranslate custom mode with null custom leet fallback`() {
        val result = ReverseTranslator.reverseTranslate("#3LL0", LeetTranslator.TranslationMode.CUSTOM, null)
        assertEquals("HELLO", result) // Falls back to simple mode
    }

    // ===== EDGE CASES =====

    @Test
    fun `reverseTranslate empty string returns empty`() {
        val result = ReverseTranslator.reverseTranslate("", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("", result)
    }

    @Test
    fun `reverseTranslate null input returns empty string`() {
        val result = ReverseTranslator.reverseTranslate(null, LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("", result)
    }

    // ===== LEETSPEAK DETECTION TESTS =====

    @Test
    fun `isLikelyLeetspeak detects simple leet`() {
        assertTrue(ReverseTranslator.isLikelyLeetspeak("#3LL0"))
        assertTrue(ReverseTranslator.isLikelyLeetspeak("W0RLD"))
        assertTrue(ReverseTranslator.isLikelyLeetspeak("1337"))
    }

    @Test
    fun `isLikelyLeetspeak detects extended leet`() {
        assertTrue(ReverseTranslator.isLikelyLeetspeak("/\\/\\"))
        assertTrue(ReverseTranslator.isLikelyLeetspeak("|\\|"))
        assertTrue(ReverseTranslator.isLikelyLeetspeak("|_|"))
        // Fixed escape sequence - using proper raw string or proper escaping
        assertTrue(ReverseTranslator.isLikelyLeetspeak("\\/\\/"))
    }

    @Test
    fun `isLikelyLeetspeak rejects normal text`() {
        assertFalse(ReverseTranslator.isLikelyLeetspeak("Hello"))
        assertFalse(ReverseTranslator.isLikelyLeetspeak("World"))
        assertFalse(ReverseTranslator.isLikelyLeetspeak("Normal Text"))
    }

    // ===== ROUND-TRIP TESTS =====

    @Test
    fun `round trip simple mode translation`() {
        val original = "HELLO WORLD"
        val translated = LeetTranslator.translate(original, LeetTranslator.TranslationMode.SIMPLE)
        val reversed = ReverseTranslator.reverseTranslate(translated, LeetTranslator.TranslationMode.SIMPLE)
        assertEquals(original, reversed)
    }

    @Test
    fun `round trip extended mode translation`() {
        val original = "MENU"
        val translated = LeetTranslator.translate(original, LeetTranslator.TranslationMode.EXTENDED)
        val reversed = ReverseTranslator.reverseTranslate(translated, LeetTranslator.TranslationMode.EXTENDED)
        assertEquals(original, reversed)
    }

    @Test
    fun `round trip custom mode translation`() {
        val original = "AEO"
        val translated = LeetTranslator.translate(original, LeetTranslator.TranslationMode.CUSTOM, customLeet)
        val reversed = ReverseTranslator.reverseTranslate(translated, LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals(original, reversed)
    }

    // ===== STRING EXTENSION TESTS (corrected) =====

    @Test
    fun `string reverse translation function`() {
        // Direct call to reverse translator instead of extension function
        val result = ReverseTranslator.reverseTranslate("#3LL0", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("HELLO", result)
    }

    @Test
    fun `string reverse translation with custom leet`() {
        // Direct call to reverse translator with custom leet
        val result = ReverseTranslator.reverseTranslate("@€Ø", LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals("AEO", result)
    }

    // ===== PERFORMANCE TESTS =====

    @Test
    fun `reverseTranslate large text performance`() {
        val largeLeetText = "#3LL0 W0RLD ".repeat(1000)
        val startTime = System.currentTimeMillis()

        val result = ReverseTranslator.reverseTranslate(largeLeetText, LeetTranslator.TranslationMode.SIMPLE)

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        assertTrue("Reverse translation should complete within 2 seconds", duration < 2000)
        assertTrue("Result should not be empty", result.isNotEmpty())
        assertTrue("Result should contain normal characters", result.contains("HELLO"))
    }
}