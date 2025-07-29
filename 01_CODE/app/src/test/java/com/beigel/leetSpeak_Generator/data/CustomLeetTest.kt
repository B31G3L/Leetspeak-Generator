// app/src/test/java/com/beigel/leetSpeak_Generator/data/CustomLeetTest.kt
package com.beigel.leetSpeak_Generator.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Umfassende Tests für CustomLeet Data Class
 */
class CustomLeetTest {

    private lateinit var customLeet: CustomLeet

    @Before
    fun setUp() {
        customLeet = CustomLeet("Test Leet", Icons.Default.Settings)
    }

    // ===== BASIC FUNCTIONALITY TESTS =====

    @Test
    fun `constructor creates leet with name and icon`() {
        assertEquals("Test Leet", customLeet.name)
        assertEquals(Icons.Default.Settings, customLeet.iconImageVector)
        assertTrue(customLeet.translations.isEmpty())
    }

    @Test
    fun `setTranslation adds single translation`() {
        customLeet.setTranslation("A", "4")

        assertEquals("4", customLeet.getTranslation("A"))
        assertEquals(1, customLeet.translations.size)
        assertTrue(customLeet.hasTranslation("A"))
    }

    @Test
    fun `setTranslation overwrites existing translation`() {
        customLeet.setTranslation("A", "4")
        customLeet.setTranslation("A", "@")

        assertEquals("@", customLeet.getTranslation("A"))
        assertEquals(1, customLeet.translations.size)
    }

    @Test
    fun `getTranslation returns original char for unmapped`() {
        assertEquals("B", customLeet.getTranslation("B"))
        assertFalse(customLeet.hasTranslation("B"))
    }

    @Test
    fun `setTranslations replaces all translations`() {
        // Add initial translations
        customLeet.setTranslation("A", "4")
        customLeet.setTranslation("E", "3")

        // Replace with new set
        val newTranslations = mapOf(
            "B" to "8",
            "O" to "0"
        )
        customLeet.setTranslations(newTranslations)

        assertEquals(2, customLeet.translations.size)
        assertEquals("8", customLeet.getTranslation("B"))
        assertEquals("0", customLeet.getTranslation("O"))
        assertEquals("A", customLeet.getTranslation("A")) // Should return original, not "4"
        assertFalse(customLeet.hasTranslation("A"))
    }

    @Test
    fun `removeTranslation removes single translation`() {
        customLeet.setTranslation("A", "4")
        customLeet.setTranslation("E", "3")

        customLeet.removeTranslation("A")

        assertEquals(1, customLeet.translations.size)
        assertFalse(customLeet.hasTranslation("A"))
        assertTrue(customLeet.hasTranslation("E"))
        assertEquals("A", customLeet.getTranslation("A")) // Returns original
    }

    @Test
    fun `removeTranslation ignores non-existent key`() {
        customLeet.setTranslation("A", "4")

        customLeet.removeTranslation("Z") // Doesn't exist

        assertEquals(1, customLeet.translations.size)
        assertTrue(customLeet.hasTranslation("A"))
    }

    @Test
    fun `clearTranslations removes all translations`() {
        customLeet.setTranslation("A", "4")
        customLeet.setTranslation("E", "3")
        customLeet.setTranslation("O", "0")

        customLeet.clearTranslations()

        assertEquals(0, customLeet.translations.size)
        assertFalse(customLeet.hasTranslation("A"))
        assertFalse(customLeet.hasTranslation("E"))
    }

    // ===== IMMUTABILITY TESTS =====

    @Test
    fun `translations returns immutable view`() {
        customLeet.setTranslation("A", "4")

        val translations = customLeet.translations

        // Try to modify the returned map - should not affect original
        try {
            (translations as MutableMap).put("B", "8")
            fail("Expected ClassCastException or similar")
        } catch (e: Exception) {
            // Expected - map should be immutable
        }

        // Original should be unchanged
        assertEquals(1, customLeet.translations.size)
        assertFalse(customLeet.hasTranslation("B"))
    }

    @Test
    fun `modifications after getting translations don't affect returned map`() {
        customLeet.setTranslation("A", "4")
        val translations = customLeet.translations

        customLeet.setTranslation("B", "8")

        // Original returned map should not reflect new changes
        assertEquals(1, translations.size)
        assertFalse(translations.containsKey("B"))
    }

    // ===== COPY TESTS =====

    @Test
    fun `copy creates independent instance with same name`() {
        customLeet.setTranslation("A", "4")
        customLeet.setTranslation("E", "3")

        val copy = customLeet.copy()

        assertEquals(customLeet.name, copy.name)
        assertEquals(customLeet.iconImageVector, copy.iconImageVector)
        assertEquals(customLeet.translations, copy.translations)

        // Verify independence
        copy.setTranslation("O", "0")
        assertFalse(customLeet.hasTranslation("O"))
        assertTrue(copy.hasTranslation("O"))
    }

    @Test
    fun `copy with new name changes name only`() {
        customLeet.setTranslation("A", "4")

        val copy = customLeet.copy("New Name")

        assertEquals("New Name", copy.name)
        assertEquals(customLeet.iconImageVector, copy.iconImageVector)
        assertEquals(customLeet.translations, copy.translations)
    }

    @Test
    fun `copy independence test`() {
        customLeet.setTranslation("A", "4")
        val copy = customLeet.copy()

        // Modify original
        customLeet.setTranslation("E", "3")
        customLeet.removeTranslation("A")

        // Copy should be unaffected
        assertTrue(copy.hasTranslation("A"))
        assertFalse(copy.hasTranslation("E"))
    }

    // ===== COMPANION OBJECT FACTORY TESTS =====

    @Test
    fun `createWithSimpleDefaults creates leet with simple mappings`() {
        val simpleLeet = CustomLeet.createWithSimpleDefaults("Simple Test")

        assertEquals("Simple Test", simpleLeet.name)
        assertEquals(Icons.Default.Settings, simpleLeet.iconImageVector)

        // Check some simple mappings
        assertEquals("4", simpleLeet.getTranslation("A"))
        assertEquals("8", simpleLeet.getTranslation("B"))
        assertEquals("3", simpleLeet.getTranslation("E"))
        assertEquals("0", simpleLeet.getTranslation("O"))
        assertEquals("5", simpleLeet.getTranslation("S"))

        // Check unmapped characters
        assertEquals("C", simpleLeet.getTranslation("C"))
        assertEquals("X", simpleLeet.getTranslation("X"))
    }

    @Test
    fun `createWithSimpleDefaults with custom icon`() {
        val simpleLeet = CustomLeet.createWithSimpleDefaults("Simple Test", Icons.Default.Star)

        assertEquals(Icons.Default.Star, simpleLeet.iconImageVector)
    }

    @Test
    fun `createWithExtendedDefaults creates leet with extended mappings`() {
        val extendedLeet = CustomLeet.createWithExtendedDefaults("Extended Test")

        assertEquals("Extended Test", extendedLeet.name)
        assertEquals(Icons.Default.Settings, extendedLeet.iconImageVector)

        // Check extended mappings
        assertEquals("4", extendedLeet.getTranslation("A"))
        assertEquals("/\\/\\", extendedLeet.getTranslation("M"))
        assertEquals("|\\|", extendedLeet.getTranslation("N"))
        assertEquals("|_|", extendedLeet.getTranslation("U"))
        assertEquals("\\/\\/", extendedLeet.getTranslation("W"))
        assertEquals("><", extendedLeet.getTranslation("X"))
        assertEquals("`/", extendedLeet.getTranslation("Y"))

        // Check that it has more mappings than simple
        assertTrue(extendedLeet.translations.size > 15)
    }

    @Test
    fun `createWithExtendedDefaults with custom icon`() {
        val extendedLeet = CustomLeet.createWithExtendedDefaults("Extended Test", Icons.Default.Star)

        assertEquals(Icons.Default.Star, extendedLeet.iconImageVector)
    }

    // ===== EDGE CASES =====

    @Test
    fun `empty string translation`() {
        customLeet.setTranslation("A", "")

        assertEquals("", customLeet.getTranslation("A"))
        assertTrue(customLeet.hasTranslation("A"))
    }

    @Test
    fun `multi-character translation`() {
        customLeet.setTranslation("M", "/\\/\\")

        assertEquals("/\\/\\", customLeet.getTranslation("M"))
        assertTrue(customLeet.hasTranslation("M"))
    }

    @Test
    fun `special character translation`() {
        customLeet.setTranslation("A", "!@#$%")

        assertEquals("!@#$%", customLeet.getTranslation("A"))
    }

    @Test
    fun `unicode character translation`() {
        customLeet.setTranslation("A", "€")
        customLeet.setTranslation("E", "ñ")

        assertEquals("€", customLeet.getTranslation("A"))
        assertEquals("ñ", customLeet.getTranslation("E"))
    }

    @Test
    fun `case sensitivity test`() {
        customLeet.setTranslation("A", "4")

        assertEquals("4", customLeet.getTranslation("A"))
        assertEquals("a", customLeet.getTranslation("a")) // Lowercase returns original
    }

    // ===== LARGE DATASET TESTS =====

    @Test
    fun `large translation map performance`() {
        // Add translations for all letters and numbers
        val startTime = System.currentTimeMillis()

        for (c in 'A'..'Z') {
            customLeet.setTranslation(c.toString(), "${c.code}")
        }
        for (c in '0'..'9') {
            customLeet.setTranslation(c.toString(), "[$c]")
        }

        val endTime = System.currentTimeMillis()

        assertEquals(36, customLeet.translations.size) // 26 letters + 10 digits
        assertTrue("Should complete within 100ms", endTime - startTime < 100)

        // Test some translations
        assertEquals("65", customLeet.getTranslation("A")) // ASCII code for 'A'
        assertEquals("[5]", customLeet.getTranslation("5"))
    }

    // ===== VALIDATION TESTS =====

    @Test
    fun `name property can be modified`() {
        customLeet.name = "New Name"
        assertEquals("New Name", customLeet.name)
    }

    @Test
    fun `icon property can be modified`() {
        customLeet.iconImageVector = Icons.Default.Star
        assertEquals(Icons.Default.Star, customLeet.iconImageVector)
    }

    @Test
    fun `translations survive icon and name changes`() {
        customLeet.setTranslation("A", "4")
        customLeet.setTranslation("E", "3")

        customLeet.name = "Changed Name"
        customLeet.iconImageVector = Icons.Default.Star

        assertEquals("4", customLeet.getTranslation("A"))
        assertEquals("3", customLeet.getTranslation("E"))
        assertEquals(2, customLeet.translations.size)
    }
}