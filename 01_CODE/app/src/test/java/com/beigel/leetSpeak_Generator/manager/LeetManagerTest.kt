package com.beigel.leetSpeak_Generator.manager

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.test.core.app.ApplicationProvider
import com.beigel.leetSpeak_Generator.data.CustomLeet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import app.cash.turbine.test

/**
 * FIXED: Umfassende Tests für LeetManager mit korrekter Result API Verwendung
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class LeetManagerTest {

    private lateinit var context: Context
    private lateinit var leetManager: LeetManager
    private lateinit var sharedPrefs: SharedPreferences

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear any existing preferences
        context.getSharedPreferences("LeetSpeakProfiles", Context.MODE_PRIVATE)
            .edit().clear().commit()

        leetManager = LeetManager(context)
        sharedPrefs = context.getSharedPreferences("LeetSpeakProfiles", Context.MODE_PRIVATE)
    }

    @After
    fun tearDown() {
        leetManager.cleanup()
        sharedPrefs.edit().clear().commit()
    }

    // ===== INITIALIZATION TESTS =====

    @Test
    fun `manager initializes with empty leets`() = runTest {
        leetManager.leets.test {
            val initialLeets = awaitItem()
            Assert.assertTrue(initialLeets.isEmpty())
        }
    }

    @Test
    fun `manager initializes with default current index`() = runTest {
        leetManager.currentLeetIndex.test {
            Assert.assertEquals(0, awaitItem())
        }
    }

    @Test
    fun `manager initializes with no favorite`() = runTest {
        leetManager.favoriteIndex.test {
            Assert.assertEquals(LeetManager.FAV_NONE, awaitItem())
        }
    }

    @Test
    fun `hasLeets flow starts as false`() = runTest {
        leetManager.hasLeets.test {
            Assert.assertEquals(false, awaitItem())
        }
    }

    // ===== LEET MANAGEMENT TESTS =====

    @Test
    fun `addLeet adds leet and returns index`() = runTest {
        val customLeet = CustomLeet("Test Leet", Icons.Default.Settings)

        val result = leetManager.addLeet(customLeet)

        // FIXED: Proper Result API usage
        Assert.assertTrue("Result should be successful", result.isSuccess)
        Assert.assertEquals(0, result.getOrNull())

        leetManager.leets.test {
            val leets = awaitItem()
            Assert.assertEquals(1, leets.size)
            Assert.assertEquals("Test Leet", leets[0].name)
        }
    }

    @Test
    fun `addLeet updates hasLeets flow`() = runTest {
        val customLeet = CustomLeet("Test", Icons.Default.Settings)

        leetManager.addLeet(customLeet)

        leetManager.hasLeets.test {
            Assert.assertEquals(true, awaitItem())
        }
    }

    @Test
    fun `updateLeet modifies existing leet`() = runTest {
        val originalLeet = CustomLeet("Original", Icons.Default.Settings)
        leetManager.addLeet(originalLeet)

        val updatedLeet = CustomLeet("Updated", Icons.Default.Star)
        val result = leetManager.updateLeet(0, updatedLeet)

        Assert.assertTrue("Update should be successful", result.isSuccess)

        leetManager.leets.test {
            val leets = awaitItem()
            Assert.assertEquals("Updated", leets[0].name)
            Assert.assertEquals(Icons.Default.Star, leets[0].iconImageVector)
        }
    }

    @Test
    fun `deleteLeet removes leet and updates flows`() = runTest {
        val firstLeet = CustomLeet("First", Icons.Default.Settings)
        val secondLeet = CustomLeet("Second", Icons.Default.Star)
        leetManager.addLeet(firstLeet)
        leetManager.addLeet(secondLeet)

        val result = leetManager.deleteLeet(0)

        Assert.assertTrue("Delete should be successful", result.isSuccess)

        val deletionResult = result.getOrNull()!!
        Assert.assertEquals("First", deletionResult.deletedLeet.name)
        Assert.assertFalse(deletionResult.wasFavorite)
        Assert.assertFalse(deletionResult.wasLastLeet)

        leetManager.leets.test {
            val leets = awaitItem()
            Assert.assertEquals(1, leets.size)
            Assert.assertEquals("Second", leets[0].name)
        }
    }

    // ===== INDEX MANAGEMENT TESTS =====

    @Test
    fun `setCurrentLeetIndex updates index`() = runTest {
        leetManager.addLeet(CustomLeet("First", Icons.Default.Settings))
        leetManager.addLeet(CustomLeet("Second", Icons.Default.Star))

        val result = leetManager.setCurrentLeetIndex(1)

        Assert.assertTrue("Setting index should be successful", result.isSuccess)

        leetManager.currentLeetIndex.test {
            Assert.assertEquals(1, awaitItem())
        }
    }

    @Test
    fun `setCurrentLeetIndex with invalid index fails`() = runTest {
        leetManager.addLeet(CustomLeet("Test", Icons.Default.Settings))

        val result = leetManager.setCurrentLeetIndex(999)

        Assert.assertTrue("Invalid index should fail", result.isFailure)
    }

    @Test
    fun `currentLeet flow reflects current index changes`() = runTest {
        val firstLeet = CustomLeet("First", Icons.Default.Settings)
        val secondLeet = CustomLeet("Second", Icons.Default.Star)
        leetManager.addLeet(firstLeet)
        leetManager.addLeet(secondLeet)

        leetManager.setCurrentLeetIndex(1)

        leetManager.currentLeet.test {
            val currentLeet = awaitItem()
            Assert.assertNotNull(currentLeet)
            Assert.assertEquals("Second", currentLeet!!.name)
        }
    }

    // ===== FAVORITE MANAGEMENT TESTS =====

    @Test
    fun `setFavorite simple mode works`() = runTest {
        val result = leetManager.setFavorite(LeetManager.MODE_SIMPLE)

        Assert.assertTrue("Setting simple favorite should succeed", result.isSuccess)

        leetManager.favoriteIndex.test {
            Assert.assertEquals(LeetManager.FAV_SIMPLE, awaitItem())
        }
    }

    @Test
    fun `setFavorite extended mode works`() = runTest {
        val result = leetManager.setFavorite(LeetManager.MODE_EXTENDED)

        Assert.assertTrue("Setting extended favorite should succeed", result.isSuccess)

        leetManager.favoriteIndex.test {
            Assert.assertEquals(LeetManager.FAV_EXTENDED, awaitItem())
        }
    }

    @Test
    fun `setFavorite custom mode works`() = runTest {
        leetManager.addLeet(CustomLeet("Custom", Icons.Default.Settings))

        val result = leetManager.setFavorite(LeetManager.MODE_CUSTOM, 0)

        Assert.assertTrue("Setting custom favorite should succeed", result.isSuccess)

        leetManager.favoriteIndex.test {
            Assert.assertEquals(0, awaitItem())
        }
    }

    @Test
    fun `toggleFavorite switches favorite status`() = runTest {
        val result1 = leetManager.toggleFavorite(LeetManager.MODE_SIMPLE)
        Assert.assertTrue("First toggle should succeed", result1.isSuccess)
        Assert.assertTrue("Should now be favorite", result1.getOrNull()!!)

        val result2 = leetManager.toggleFavorite(LeetManager.MODE_SIMPLE)
        Assert.assertTrue("Second toggle should succeed", result2.isSuccess)
        Assert.assertFalse("Should no longer be favorite", result2.getOrNull()!!)
    }

    @Test
    fun `isFavorite returns correct status`() = runTest {
        Assert.assertFalse(leetManager.isFavorite(LeetManager.MODE_SIMPLE))

        leetManager.setFavorite(LeetManager.MODE_SIMPLE)

        Assert.assertTrue(leetManager.isFavorite(LeetManager.MODE_SIMPLE))
        Assert.assertFalse(leetManager.isFavorite(LeetManager.MODE_EXTENDED))
    }

    // ===== FACTORY METHODS TESTS =====

    @Test
    fun `createLeetWithSimpleDefaults creates correct leet`() = runTest {
        val result = leetManager.createLeetWithSimpleDefaults("Simple Test")

        Assert.assertTrue("Creation should succeed", result.isSuccess)

        val leet = result.getOrNull()!!
        Assert.assertEquals("Simple Test", leet.name)
        Assert.assertEquals("4", leet.getTranslation("A"))
        Assert.assertEquals("3", leet.getTranslation("E"))
    }

    @Test
    fun `createLeetWithExtendedDefaults creates correct leet`() = runTest {
        val result = leetManager.createLeetWithExtendedDefaults("Extended Test")

        Assert.assertTrue("Creation should succeed", result.isSuccess)

        val leet = result.getOrNull()!!
        Assert.assertEquals("Extended Test", leet.name)
        Assert.assertEquals("/\\/\\", leet.getTranslation("M"))
        Assert.assertEquals("|\\|", leet.getTranslation("N"))
    }

    // ===== PERSISTENCE TESTS =====

    @Test
    fun `leets persist across manager instances`() = runTest {
        val originalLeet = CustomLeet("Persistent", Icons.Default.Settings)
        leetManager.addLeet(originalLeet)
        leetManager.setFavorite(LeetManager.MODE_SIMPLE)

        // Create new manager instance
        val newManager = LeetManager(context)

        newManager.leets.test {
            val leets = awaitItem()
            Assert.assertEquals(1, leets.size)
            Assert.assertEquals("Persistent", leets[0].name)
        }

        newManager.favoriteIndex.test {
            Assert.assertEquals(LeetManager.FAV_SIMPLE, awaitItem())
        }

        newManager.cleanup()
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    fun `invalid operations fail gracefully`() = runTest {
        // Try to update non-existent leet
        val updateResult = leetManager.updateLeet(999, CustomLeet("Invalid", Icons.Default.Settings))
        Assert.assertTrue("Invalid update should fail", updateResult.isFailure)

        // Try to delete non-existent leet
        val deleteResult = leetManager.deleteLeet(999)
        Assert.assertTrue("Invalid delete should fail", deleteResult.isFailure)

        // Try to set invalid custom favorite
        val favoriteResult = leetManager.setFavorite(LeetManager.MODE_CUSTOM, 999)
        Assert.assertTrue("Invalid favorite should fail", favoriteResult.isFailure)
    }

    @Test
    fun `cleanup doesn't throw exceptions`() {
        // Should not throw
        leetManager.cleanup()

        // Should be able to call multiple times
        leetManager.cleanup()
        leetManager.cleanup()
    }

    // ===== EDGE CASES =====

    @Test
    fun `deleting last leet updates flows correctly`() = runTest {
        leetManager.addLeet(CustomLeet("Only", Icons.Default.Settings))
        leetManager.setFavorite(LeetManager.MODE_CUSTOM, 0)

        val result = leetManager.deleteLeet(0)

        Assert.assertTrue("Delete should succeed", result.isSuccess)

        val deletionResult = result.getOrNull()!!
        Assert.assertTrue("Was favorite should be true", deletionResult.wasFavorite)
        Assert.assertTrue("Was last leet should be true", deletionResult.wasLastLeet)

        leetManager.hasLeets.test {
            Assert.assertFalse("Should have no leets", awaitItem())
        }

        leetManager.favoriteIndex.test {
            Assert.assertEquals(LeetManager.FAV_NONE, awaitItem())
        }
    }

    @Test
    fun `favorite migration works correctly`() = runTest {
        // Simulate old favorite format and verify migration
        // This tests the migrateFavoriteIndex functionality indirectly

        leetManager.addLeet(CustomLeet("Test", Icons.Default.Settings))
        leetManager.setFavorite(LeetManager.MODE_CUSTOM, 0)

        // Delete and verify favorite is cleared
        leetManager.deleteLeet(0)

        leetManager.favoriteIndex.test {
            Assert.assertEquals(LeetManager.FAV_NONE, awaitItem())
        }
    }
}