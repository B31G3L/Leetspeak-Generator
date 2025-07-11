package com.beigel.leetSpeak_Generator;

import org.junit.Test;
import static org.junit.Assert.*;

public class LeetTranslatorTest {

    @Test
    public void testSimpleTranslation() {
        String result = LeetTranslator.translate("HELLO", LeetTranslator.TranslationMode.SIMPLE, null);
        assertEquals("#3LL0", result);
    }

    @Test
    public void testExtendedTranslation() {
        String result = LeetTranslator.translate("HELLO", LeetTranslator.TranslationMode.EXTENDED, null);
        assertEquals("#3110", result);
    }

    @Test
    public void testMixedCase() {
        String result = LeetTranslator.translate("Hello", LeetTranslator.TranslationMode.SIMPLE, null);
        assertEquals("#3LL0", result);
    }

    @Test
    public void testSpecialCharacters() {
        String result = LeetTranslator.translate("HELLO!", LeetTranslator.TranslationMode.SIMPLE, null);
        assertEquals("#3LL0!", result);
    }

    @Test
    public void testNumbers() {
        String result = LeetTranslator.translate("ABC123", LeetTranslator.TranslationMode.SIMPLE, null);
        assertEquals("48C123", result);
    }

    @Test
    public void testEmptyString() {
        String result = LeetTranslator.translate("", LeetTranslator.TranslationMode.SIMPLE, null);
        assertEquals("", result);
    }

    @Test
    public void testNullInput() {
        String result = LeetTranslator.translate(null, LeetTranslator.TranslationMode.SIMPLE, null);
        assertNull(result);
    }

    @Test
    public void testCustomProfile() {
        CustomLeet customLeet = new CustomLeet("Test");
        customLeet.setTranslation("A", "@");
        customLeet.setTranslation("E", "€");

        String result = LeetTranslator.translate("AE", LeetTranslator.TranslationMode.CUSTOM, customLeet);
        assertEquals("@€", result);
    }

    @Test
    public void testCustomProfileFallback() {
        String result = LeetTranslator.translate("HELLO", LeetTranslator.TranslationMode.CUSTOM, null);
        // Should fallback to simple mode
        assertEquals("#3LL0", result);
    }
}