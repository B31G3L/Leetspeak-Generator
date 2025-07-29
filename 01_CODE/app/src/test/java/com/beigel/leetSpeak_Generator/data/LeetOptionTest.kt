package com.beigel.leetSpeak_Generator.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import com.beigel.leetSpeak_Generator.manager.LeetManager
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Tests für LeetOption Data Class
 */
class LeetOptionTest {

    private lateinit var customLeet: CustomLeet

    @Before
    fun setUp() {
        customLeet = CustomLeet("Test Custom", Icons.Default.Settings)
        customLeet.setTranslation("A", "@")
    }

    // ===== FACTORY FUNCTION TESTS =====

    @Test
    fun `createSimple creates correct simple option`() {
        val option = LeetOption.createSimple()

        assertEquals(LeetManager.MODE_SIMPLE, option.mode)
        assertEquals("Simple Leet", option.name)
        assertEquals("Basic character substitutions (A→4, E→3, etc.)", option.description)
        assertEquals(Icons.Default.TextFields, option.iconImageVector)
        assertFalse(option.isCustom)
        assertEquals(-1, option.customIndex)
        assertFalse(option.isSelected)
        assertFalse(option.isFavorite)
    }

    @Test
    fun `createSimple with parameters sets correct values`() {
        val option = LeetOption.createSimple(isSelected = true, isFavorite = true)

        assertTrue(option.isSelected)
        assertTrue(option.isFavorite)
        assertEquals(LeetManager.MODE_SIMPLE, option.mode)
    }

    @Test
    fun `createExtended creates correct extended option`() {
        val option = LeetOption.createExtended()

        assertEquals(LeetManager.MODE_EXTENDED, option.mode)
        assertEquals("Extended Leet", option.name)
        assertEquals("Advanced multi-character substitutions (M→/\\/\\, N→|\\|, etc.)", option.description)
        assertEquals(Icons.Default.Extension, option.iconImageVector)
        assertFalse(option.isCustom)
        assertEquals(-1, option.customIndex)
        assertFalse(option.isSelected)
        assertFalse(option.isFavorite)
    }

    @Test
    fun `createExtended with parameters sets correct values`() {
        val option = LeetOption.createExtended(isSelected = true, isFavorite = true)

        assertTrue(option.isSelected)
        assertTrue(option.isFavorite)
        assertEquals(LeetManager.MODE_EXTENDED, option.mode)
    }

    @Test
    fun `createCustom creates correct custom option`() {
        val option = LeetOption.createCustom(customLeet, customIndex = 5)

        assertEquals(LeetManager.MODE_CUSTOM, option.mode)
        assertEquals("Test Custom", option.name)
        assertEquals("Custom character mappings", option.description)
        assertEquals(Icons.Default.Settings, option.iconImageVector)
        assertTrue(option.isCustom)
        assertEquals(5, option.customIndex)
        assertFalse(option.isSelected)
        assertFalse(option.isFavorite)
    }

    @Test
    fun `createCustom with parameters sets correct values`() {
        val option = LeetOption.createCustom(
            leet = customLeet,
            customIndex = 3,
            isSelected = true,
            isFavorite = true
        )

        assertTrue(option.isSelected)
        assertTrue(option.isFavorite)
        assertEquals(3, option.customIndex)
        assertEquals("Test Custom", option.name)
    }

    // ===== COMPOSABLE FACTORY TESTS =====
    // Note: These would need to be tested in an Android environment with Compose
    // For now, we'll test that the non-composable versions work correctly

    @Test
    fun `non-composable factories use fallback strings`() {
        val simple = LeetOption.createSimple()
        val extended = LeetOption.createExtended()
        val custom = LeetOption.createCustom(customLeet, 0)

        // Verify they use hardcoded strings instead of string resources
        assertEquals("Basic character substitutions (A→4, E→3, etc.)", simple.description)
        assertEquals("Advanced multi-character substitutions (M→/\\/\\, N→|\\|, etc.)", extended.description)
        assertEquals("Custom character mappings", custom.description)
    }

    // ===== DATA CLASS PROPERTIES TESTS =====

    @Test
    fun `data class properties are immutable by default`() {
        val option = LeetOption.createSimple()

        // Properties should be val (read-only)
        assertEquals(LeetManager.MODE_SIMPLE, option.mode)
        assertEquals("Simple Leet", option.name)

        // These are var properties that can be modified
        option.isSelected = true
        option.isFavorite = true

        assertTrue(option.isSelected)
        assertTrue(option.isFavorite)
    }

    @Test
    fun `copy works correctly`() {
        val original = LeetOption.createSimple(isSelected = true, isFavorite = true)
        val copy = original.copy(isSelected = false)

        assertFalse(copy.isSelected)
        assertTrue(copy.isFavorite) // Should preserve other properties
        assertEquals(original.mode, copy.mode)
        assertEquals(original.name, copy.name)
    }

    @Test
    fun `equals and hashCode work correctly`() {
        val option1 = LeetOption.createSimple()
        val option2 = LeetOption.createSimple()
        val option3 = LeetOption.createExtended()

        assertEquals(option1, option2)
        assertNotEquals(option1, option3)
        assertEquals(option1.hashCode(), option2.hashCode())
    }

    @Test
    fun `toString contains useful information`() {
        val option = LeetOption.createCustom(customLeet, 2, isSelected = true)
        val toString = option.toString()

        assertTrue(toString.contains("Test Custom"))
        assertTrue(toString.contains("isCustom=true"))
        assertTrue(toString.contains("customIndex=2"))
        assertTrue(toString.contains("isSelected=true"))
    }

    // ===== ICON TESTS =====

    @Test
    fun `simple option has correct icon`() {
        val option = LeetOption.createSimple()
        assertEquals(Icons.Default.TextFields, option.iconImageVector)
    }

    @Test
    fun `extended option has correct icon`() {
        val option = LeetOption.createExtended()
        assertEquals(Icons.Default.Extension, option.iconImageVector)
    }

    @Test
    fun `custom option uses leet icon`() {
        val customLeetWithIcon = CustomLeet("Custom", Icons.Default.Extension)
        val option = LeetOption.createCustom(customLeetWithIcon, 0)

        assertEquals(Icons.Default.Extension, option.iconImageVector)
    }

    // ===== EDGE CASES =====

    @Test
    fun `custom option with negative index`() {
        val option = LeetOption.createCustom(customLeet, customIndex = -5)

        assertEquals(-5, option.customIndex)
        assertTrue(option.isCustom)
    }

    @Test
    fun `custom option with zero index`() {
        val option = LeetOption.createCustom(customLeet, customIndex = 0)

        assertEquals(0, option.customIndex)
        assertTrue(option.isCustom)
    }

    @Test
    fun `mutable properties can be changed after creation`() {
        val option = LeetOption.createSimple()

        assertFalse(option.isSelected)
        assertFalse(option.isFavorite)

        option.isSelected = true
        option.isFavorite = true

        assertTrue(option.isSelected)
        assertTrue(option.isFavorite)
    }

    // ===== VALIDATION TESTS =====

    @Test
    fun `built-in options have correct mode constants`() {
        val simple = LeetOption.createSimple()
        val extended = LeetOption.createExtended()
        val custom = LeetOption.createCustom(customLeet, 0)

        assertEquals(LeetManager.MODE_SIMPLE, simple.mode)
        assertEquals(LeetManager.MODE_EXTENDED, extended.mode)
        assertEquals(LeetManager.MODE_CUSTOM, custom.mode)
    }

    @Test
    fun `custom option uses leet name`() {
        val customLeetWithName = CustomLeet("My Special Leet", Icons.Default.Settings)
        val option = LeetOption.createCustom(customLeetWithName, 0)

        assertEquals("My Special Leet", option.name)
    }

    @Test
    fun `options are not custom by default for built-ins`() {
        val simple = LeetOption.createSimple()
        val extended = LeetOption.createExtended()

        assertFalse(simple.isCustom)
        assertFalse(extended.isCustom)
        assertEquals(-1, simple.customIndex)
        assertEquals(-1, extended.customIndex)
    }

    @Test
    fun `custom options are marked as custom`() {
        val option = LeetOption.createCustom(customLeet, 5)

        assertTrue(option.isCustom)
        assertEquals(5, option.customIndex)
    }
}