package com.beigel.leetSpeak_Generator.presentation.intent

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.data.LeetOption
import org.junit.Assert
import org.junit.Test

/**
 * Tests für MainIntent Sealed Class
 */
class MainIntentTest {

    @Test
    fun `UpdateInput intent contains correct text`() {
        val intent = MainIntent.UpdateInput("Hello World")

        Assert.assertTrue(intent is MainIntent.UpdateInput)
        Assert.assertEquals("Hello World", intent.text)
    }

    @Test
    fun `ChangeMode intent contains correct leet option`() {
        val leetOption = LeetOption.createSimple()
        val intent = MainIntent.ChangeMode(leetOption)

        Assert.assertTrue(intent is MainIntent.ChangeMode)
        Assert.assertEquals(leetOption, intent.leetOption)
    }

    @Test
    fun `ToggleFavorite intent contains correct leet option`() {
        val leetOption = LeetOption.createExtended()
        val intent = MainIntent.ToggleFavorite(leetOption)

        Assert.assertTrue(intent is MainIntent.ToggleFavorite)
        Assert.assertEquals(leetOption, intent.leetOption)
    }

    @Test
    fun `CreateLeet intent with basic parameters`() {
        val intent = MainIntent.CreateLeet(
            name = "Test Leet",
            icon = Icons.Default.Settings
        )

        Assert.assertTrue(intent is MainIntent.CreateLeet)
        Assert.assertEquals("Test Leet", intent.name)
        Assert.assertEquals(Icons.Default.Settings, intent.icon)
        Assert.assertFalse(intent.useExtendedDefaults)
        Assert.assertNull(intent.customTranslations)
    }

    @Test
    fun `CreateLeet intent with extended defaults`() {
        val intent = MainIntent.CreateLeet(
            name = "Extended Leet",
            icon = Icons.Default.Settings,
            useExtendedDefaults = true
        )

        Assert.assertTrue(intent is MainIntent.CreateLeet)
        Assert.assertTrue(intent.useExtendedDefaults)
    }

    @Test
    fun `CreateLeet intent with custom translations`() {
        val customTranslations = mapOf("A" to "@", "E" to "€")
        val intent = MainIntent.CreateLeet(
            name = "Custom Leet",
            icon = Icons.Default.Settings,
            customTranslations = customTranslations
        )

        Assert.assertTrue(intent is MainIntent.CreateLeet)
        Assert.assertEquals(customTranslations, intent.customTranslations)
    }

    @Test
    fun `UpdateLeet intent contains correct parameters`() {
        val customLeet = CustomLeet("Updated", Icons.Default.Settings)
        val intent = MainIntent.UpdateLeet(5, customLeet)

        Assert.assertTrue(intent is MainIntent.UpdateLeet)
        Assert.assertEquals(5, intent.index)
        Assert.assertEquals(customLeet, intent.leet)
    }

    @Test
    fun `DeleteLeet intent contains correct index`() {
        val intent = MainIntent.DeleteLeet(3)

        Assert.assertTrue(intent is MainIntent.DeleteLeet)
        Assert.assertEquals(3, intent.index)
    }

    @Test
    fun `object intents are singletons`() {
        val copyToClipboard1 = MainIntent.CopyToClipboard
        val copyToClipboard2 = MainIntent.CopyToClipboard

        Assert.assertSame(copyToClipboard1, copyToClipboard2)

        val clearInput1 = MainIntent.ClearInput
        val clearInput2 = MainIntent.ClearInput

        Assert.assertSame(clearInput1, clearInput2)
    }

    @Test
    fun `all object intents exist`() {
        // Verify all object intents can be accessed
        Assert.assertNotNull(MainIntent.CopyToClipboard)
        Assert.assertNotNull(MainIntent.ClearInput)
        Assert.assertNotNull(MainIntent.ClearError)
        Assert.assertNotNull(MainIntent.ClearSuccess)
        Assert.assertNotNull(MainIntent.ToggleReverseMode)
        Assert.assertNotNull(MainIntent.DismissWhatsNew)
        Assert.assertNotNull(MainIntent.MarkWhatsNewAsShown)
        Assert.assertNotNull(MainIntent.ResetWhatsNewForTesting)
        Assert.assertNotNull(MainIntent.ForceShowWhatsNew)
    }

    @Test
    fun `intent toString methods work`() {
        val updateIntent = MainIntent.UpdateInput("test")
        val copyIntent = MainIntent.CopyToClipboard

        val updateString = updateIntent.toString()
        val copyString = copyIntent.toString()

        Assert.assertTrue(updateString.contains("UpdateInput"))
        Assert.assertTrue(updateString.contains("test"))
        Assert.assertTrue(copyString.contains("CopyToClipboard"))
    }

    @Test
    fun `intent equals and hashCode work correctly`() {
        val intent1 = MainIntent.UpdateInput("test")
        val intent2 = MainIntent.UpdateInput("test")
        val intent3 = MainIntent.UpdateInput("different")

        Assert.assertEquals(intent1, intent2)
        Assert.assertNotEquals(intent1, intent3)
        Assert.assertEquals(intent1.hashCode(), intent2.hashCode())

        // Object intents should be the same instance
        val copy1 = MainIntent.CopyToClipboard
        val copy2 = MainIntent.CopyToClipboard
        Assert.assertSame(copy1, copy2)
    }

    @Test
    fun `CreateLeet intent with all parameters`() {
        val customTranslations = mapOf(
            "A" to "@",
            "E" to "€",
            "O" to "Ø"
        )

        val intent = MainIntent.CreateLeet(
            name = "Full Custom Leet",
            icon = Icons.Default.Settings,
            useExtendedDefaults = true,
            customTranslations = customTranslations
        )

        Assert.assertEquals("Full Custom Leet", intent.name)
        Assert.assertEquals(Icons.Default.Settings, intent.icon)
        Assert.assertTrue(intent.useExtendedDefaults)
        Assert.assertEquals(customTranslations, intent.customTranslations)
        Assert.assertEquals(3, intent.customTranslations?.size)
    }

    @Test
    fun `DeleteLeet intent with negative index`() {
        val intent = MainIntent.DeleteLeet(-1)

        Assert.assertEquals(-1, intent.index)
    }

    @Test
    fun `UpdateLeet intent with zero index`() {
        val leet = CustomLeet("Zero Index", Icons.Default.Settings)
        val intent = MainIntent.UpdateLeet(0, leet)

        Assert.assertEquals(0, intent.index)
        Assert.assertEquals("Zero Index", intent.leet.name)
    }
}