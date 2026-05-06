package com.beigel.leetSpeak_Generator.translation

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReverseTranslatorTest {

    // ─── Simple Reverse ────────────────────────────────────────────────────────

    @Test
    fun `simple reverse translates 4 back to A`() {
        val result = ReverseTranslator.reverseTranslate(
            "4",
            LeetTranslator.TranslationMode.SIMPLE
        )
        assertThat(result).isEqualTo("A")
    }

    @Test
    fun `simple reverse translates hello leet back to plaintext`() {
        val leet   = LeetTranslator.translate("HELLO", LeetTranslator.TranslationMode.SIMPLE)
        val result = ReverseTranslator.reverseTranslate(leet, LeetTranslator.TranslationMode.SIMPLE)
        assertThat(result).isEqualTo("HELLO")
    }

    @Test
    fun `simple reverse returns empty for empty input`() {
        val result = ReverseTranslator.reverseTranslate(
            "",
            LeetTranslator.TranslationMode.SIMPLE
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun `simple reverse handles null input`() {
        val result = ReverseTranslator.reverseTranslate(
            null,
            LeetTranslator.TranslationMode.SIMPLE
        )
        assertThat(result).isEmpty()
    }

    // ─── Extended Reverse ──────────────────────────────────────────────────────

    @Test
    fun `extended reverse translates multi-char M back`() {
        val result = ReverseTranslator.reverseTranslate(
            "/\\/\\",
            LeetTranslator.TranslationMode.EXTENDED
        )
        assertThat(result).isEqualTo("M")
    }

    @Test
    fun `extended reverse translates multi-char W back`() {
        val result = ReverseTranslator.reverseTranslate(
            "\\/\\/",
            LeetTranslator.TranslationMode.EXTENDED
        )
        assertThat(result).isEqualTo("W")
    }

    @Test
    fun `extended reverse handles full roundtrip`() {
        val input    = "AMAZING"
        val leet     = LeetTranslator.translate(input, LeetTranslator.TranslationMode.EXTENDED)
        val reversed = ReverseTranslator.reverseTranslate(
            leet,
            LeetTranslator.TranslationMode.EXTENDED
        )
        assertThat(reversed).isEqualTo(input)
    }

    // ─── Custom Reverse ────────────────────────────────────────────────────────

    @Test
    fun `custom reverse uses custom reverse map`() {
        val customLeet = CustomLeet("Test")
        customLeet.setTranslation("A", "X")
        customLeet.setTranslation("B", "Y")

        val result = ReverseTranslator.reverseTranslate(
            "XY",
            LeetTranslator.TranslationMode.CUSTOM,
            customLeet
        )
        assertThat(result).isEqualTo("AB")
    }

    @Test
    fun `custom reverse with null falls back to simple`() {
        val result = ReverseTranslator.reverseTranslate(
            "4",
            LeetTranslator.TranslationMode.CUSTOM,
            null
        )
        assertThat(result).isEqualTo("A")
    }

    // ─── isLikelyLeetspeak ─────────────────────────────────────────────────────

    @Test
    fun `isLikelyLeetspeak returns true for leet text`() {
        val leet   = LeetTranslator.translate("HELLO WORLD", LeetTranslator.TranslationMode.SIMPLE)
        val result = ReverseTranslator.isLikelyLeetspeak(leet)
        assertThat(result).isTrue()
    }

    @Test
    fun `isLikelyLeetspeak returns false for plain text`() {
        val result = ReverseTranslator.isLikelyLeetspeak("Hello World")
        assertThat(result).isFalse()
    }

    @Test
    fun `isLikelyLeetspeak returns false for empty string`() {
        val result = ReverseTranslator.isLikelyLeetspeak("")
        assertThat(result).isFalse()
    }

    @Test
    fun `isLikelyLeetspeak threshold is 20 percent`() {
        // 1 von 5 Zeichen ist Leet = 20% – genau an der Grenze
        val result = ReverseTranslator.isLikelyLeetspeak("aaaa4")
        assertThat(result).isTrue()
    }
}