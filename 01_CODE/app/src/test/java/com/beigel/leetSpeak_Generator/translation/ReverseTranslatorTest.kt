// app/src/test/java/com/beigel/leetSpeak_Generator/translation/ReverseTranslatorTest.kt
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

    @Test
    fun `reverseTranslate simple mode mixed with spaces and punctuation`() {
        val result = ReverseTranslator.reverseTranslate("#3LL0 W0RLD!", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("HELLO WORLD!", result)
    }

    @Test
    fun `reverseTranslate simple mode partial leet`() {
        val result = ReverseTranslator.reverseTranslate("H3LL0", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("HELLO", result)
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
        val leetText = "4|)\/4|\\|(3|) 1337"
        val result = ReverseTranslator.reverseTranslate(leetText, LeetTranslator.TranslationMode.EXTENDED)
        assertTrue(result.contains("ADVANCED") || result.contains("ADVAN(ED"))
    }

    @Test
    fun `reverseTranslate extended mode longer patterns first`() {
        // Test that longer patterns are matched first (e.g., "/\/\" before "/")
        val result = ReverseTranslator.reverseTranslate("/\\/\\", LeetTranslator.TranslationMode.EXTENDED)
        assertEquals("M", result)
    }

    // ===== CUSTOM MODE REVERSE TESTS =====

    @Test
    fun `reverseTranslate custom mode with custom mappings`() {
        val result = ReverseTranslator.reverseTranslate("@€Ø", LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals("AEO", result)
    }

    @Test
    fun `reverseTranslate custom mode multi-character patterns`() {
        val result = ReverseTranslator.reverseTranslate("/\\/\\|\\|", LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals("MN", result)
    }

    @Test
    fun `reverseTranslate custom mode with null custom leet fallback`() {
        val result = ReverseTranslator.reverseTranslate("#3LL0", LeetTranslator.TranslationMode.CUSTOM, null)
        assertEquals("HELLO", result) // Falls back to simple mode
    }

    @Test
    fun `reverseTranslate custom mode mixed patterns`() {
        val result = ReverseTranslator.reverseTranslate("@€LL0", LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals("AELLO", result) // @ -> A, € -> E, rest unchanged
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

    @Test
    fun `reverseTranslate no leet characters returns original`() {
        val result = ReverseTranslator.reverseTranslate("HELLO", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("HELLO", result)
    }

    @Test
    fun `reverseTranslate whitespace preserved`() {
        val result = ReverseTranslator.reverseTranslate("  #3LL0  W0RLD  ", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("  HELLO  WORLD  ", result)
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
        assertTrue(ReverseTranslator.isLikelyLeetspeak("\\/\\/"))
    }

    @Test
    fun `isLikelyLeetspeak rejects normal text`() {
        assertFalse(ReverseTranslator.isLikelyLeetspeak("Hello"))
        assertFalse(ReverseTranslator.isLikelyLeetspeak("World"))
        assertFalse(ReverseTranslator.isLikelyLeetspeak("Normal Text"))
    }

    @Test
    fun `isLikelyLeetspeak threshold test`() {
        // Text with exactly 20% leet chars should be detected
        val mixedText = "H3LL0" // 1 leet char out of 5 = 20%
        assertTrue(ReverseTranslator.isLikelyLeetspeak(mixedText))

        // Text with less than 20% should not be detected
        val normalText = "Hello World" // No leet chars
        assertFalse(ReverseTranslator.isLikelyLeetspeak(normalText))
    }

    @Test
    fun `isLikelyLeetspeak empty string returns false`() {
        assertFalse(ReverseTranslator.isLikelyLeetspeak(""))
    }

    @Test
    fun `isLikelyLeetspeak case insensitive`() {
        assertTrue(ReverseTranslator.isLikelyLeetspeak("h3ll0"))
        assertTrue(ReverseTranslator.isLikelyLeetspeak("W0rld"))
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

    // ===== COMPLEX PATTERN TESTS =====

    @Test
    fun `reverseTranslate handles overlapping patterns correctly`() {
        // Test that "0_" is matched as "Q" before "0" is matched as "O"
        val result = ReverseTranslator.reverseTranslate("0_", LeetTranslator.TranslationMode.EXTENDED)
        assertEquals("Q", result)
    }

    @Test
    fun `reverseTranslate handles adjacent multi-char patterns`() {
        val result = ReverseTranslator.reverseTranslate("/\\/\\|\\|", LeetTranslator.TranslationMode.EXTENDED)
        assertEquals("MN", result)
    }

    @Test
    fun `reverseTranslate preserves numbers that aren't leet`() {
        val result = ReverseTranslator.reverseTranslate("1234567890", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("I234567890", result) // Only "1" gets converted to "I"
    }

    // ===== STRING EXTENSION TESTS =====

    @Test
    fun `string extension fromLeet function`() {
        val result = "#3LL0".fromLeet(LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("HELLO", result)
    }

    @Test
    fun `string extension fromLeet with custom leet`() {
        val result = "@€Ø".fromLeet(LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertEquals("AEO", result)
    }

    // ===== CASE SENSITIVITY TESTS =====

    @Test
    fun `reverseTranslate case insensitive matching`() {
        val result = ReverseTranslator.reverseTranslate("h3ll0", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("HELLO", result)
    }

    @Test
    fun `reverseTranslate mixed case input`() {
        val result = ReverseTranslator.reverseTranslate("#3lL0", LeetTranslator.TranslationMode.SIMPLE)
        assertEquals("HELLO", result)
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