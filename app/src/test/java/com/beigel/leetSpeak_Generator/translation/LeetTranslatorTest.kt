package com.beigel.leetSpeak_Generator.translation

import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LeetTranslatorTest {

    // ─── Simple Mode ───────────────────────────────────────────────────────────

    @Test
    fun `simple mode translates A to 4`() {
        val result = LeetTranslator.translate("A", LeetTranslator.TranslationMode.SIMPLE)
        assertThat(result).isEqualTo("4")
    }

    @Test
    fun `simple mode translates hello correctly`() {
        val result = LeetTranslator.translate("HELLO", LeetTranslator.TranslationMode.SIMPLE)
        assertThat(result).isEqualTo("#3LL0")
    }

    @Test
    fun `simple mode preserves lowercase input as uppercase translation`() {
        val result = LeetTranslator.translate("hello", LeetTranslator.TranslationMode.SIMPLE)
        assertThat(result).isEqualTo("#3LL0")
    }

    @Test
    fun `simple mode preserves non-alphabet characters`() {
        val result = LeetTranslator.translate("Hello World!", LeetTranslator.TranslationMode.SIMPLE)
        assertThat(result).isEqualTo("#3LL0 W0RLD!")  // D hat kein Simple-Mapping → bleibt D
    }

    @Test
    fun `simple mode returns empty string for empty input`() {
        val result = LeetTranslator.translate("", LeetTranslator.TranslationMode.SIMPLE)
        assertThat(result).isEmpty()
    }

    @Test
    fun `simple mode handles null input`() {
        val result = LeetTranslator.translate(null, LeetTranslator.TranslationMode.SIMPLE)
        assertThat(result).isEmpty()
    }

    // ─── Extended Mode ─────────────────────────────────────────────────────────

    @Test
    fun `extended mode translates M to multi-char`() {
        val result = LeetTranslator.translate("M", LeetTranslator.TranslationMode.EXTENDED)
        assertThat(result).isEqualTo("/\\/\\")
    }

    @Test
    fun `extended mode translates W to multi-char`() {
        val result = LeetTranslator.translate("W", LeetTranslator.TranslationMode.EXTENDED)
        assertThat(result).isEqualTo("\\/\\/")
    }

    @Test
    fun `extended mode translates N to multi-char`() {
        val result = LeetTranslator.translate("N", LeetTranslator.TranslationMode.EXTENDED)
        assertThat(result).isEqualTo("|\\|")
    }

    @Test
    fun `extended mode output is longer than input for complex chars`() {
        val result = LeetTranslator.translate("MAN", LeetTranslator.TranslationMode.EXTENDED)
        assertThat(result.length).isGreaterThan(3)
    }

    // ─── Custom Mode ───────────────────────────────────────────────────────────

    @Test
    fun `custom mode uses custom translations`() {
        val customLeet = CustomLeet("Test")
        customLeet.setTranslation("A", "X")
        customLeet.setTranslation("B", "Y")

        val result = LeetTranslator.translate(
            "AB",
            LeetTranslator.TranslationMode.CUSTOM,
            customLeet
        )
        assertThat(result).isEqualTo("XY")
    }

    @Test
    fun `custom mode falls back to original char for unmapped chars`() {
        val customLeet = CustomLeet("Test")
        customLeet.setTranslation("A", "X")
        // B nicht gemappt → bleibt B (CustomLeet.getTranslation gibt Original zurück)

        val result = LeetTranslator.translate("AB", LeetTranslator.TranslationMode.CUSTOM, customLeet)
        assertThat(result).isEqualTo("XB")  // B hat kein Custom-Mapping → bleibt B
    }

    @Test
    fun `custom mode with null customLeet falls back to simple`() {
        val result = LeetTranslator.translate(
            "A",
            LeetTranslator.TranslationMode.CUSTOM,
            null
        )
        assertThat(result).isEqualTo("4")
    }

    // ─── TranslationStats ──────────────────────────────────────────────────────

    @Test
    fun `analyzeTranslation returns correct total chars`() {
        val stats = LeetTranslator.analyzeTranslation(
            "HELLO",
            LeetTranslator.TranslationMode.SIMPLE
        )
        assertThat(stats.totalChars).isEqualTo(5)
    }

    @Test
    fun `analyzeTranslation returns empty stats for empty input`() {
        val stats = LeetTranslator.analyzeTranslation(
            "",
            LeetTranslator.TranslationMode.SIMPLE
        )
        assertThat(stats).isEqualTo(LeetTranslator.TranslationStats.empty())
    }

    @Test
    fun `analyzeTranslation percentage is between 0 and 100`() {
        val stats = LeetTranslator.analyzeTranslation(
            "Hello World",
            LeetTranslator.TranslationMode.SIMPLE
        )
        assertThat(stats.translationPercentage).isAtLeast(0f)
        assertThat(stats.translationPercentage).isAtMost(100f)
    }

    // ─── createPreview ─────────────────────────────────────────────────────────

    @Test
    fun `createPreview returns non-empty string`() {
        val preview = LeetTranslator.createPreview(LeetTranslator.TranslationMode.SIMPLE)
        assertThat(preview).isNotEmpty()
    }

    @Test
    fun `createPreview differs between simple and extended`() {
        val simple   = LeetTranslator.createPreview(LeetTranslator.TranslationMode.SIMPLE)
        val extended = LeetTranslator.createPreview(LeetTranslator.TranslationMode.EXTENDED)
        assertThat(simple).isNotEqualTo(extended)
    }
}