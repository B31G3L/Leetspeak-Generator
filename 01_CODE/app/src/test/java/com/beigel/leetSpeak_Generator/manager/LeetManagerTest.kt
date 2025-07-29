package com.beigel.leetSpeak_Generator.manager

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import com.beigel.leetSpeak_Generator.data.CustomLeet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Umfassende Tests für LeetManager
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

    // ===== LEET ADDITION TESTS =====

    @Test
    fun `addLeet adds leet and returns index`() = runTest {
        val customLeet = CustomLeet("Test Leet", Icons.Default.Settings)

        val result = leetManager.addLeet(customLeet)

        Assert.assertTrue(result.isSuccess)
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
    fun `addLeet sets current index to new leet`() = runTest {
        val customLeet = CustomLeet("Test", Icons.Default.Settings)

        leetManager.addLeet(customLeet)

        leetManager.currentLeetIndex.test {
            Assert.assertEquals(0, awaitItem())
        }
    }

    @Test
    fun `addLeet multiple leets increments index`() = runTest {
        leetManager.addLeet(CustomLeet("First", Icons.Default.Settings))
        leetManager.addLeet(CustomLeet("Second", Icons.Default.Star))

        leetManager.leets.test {
            val leets = awaitItem()
            Assert.assertEquals(2, leets.size)
        }

        leetManager.currentLeetIndex.test {
            Assert.assertEquals(1, awaitItem()) // Should be set to latest added
        }
    }

    // ===== LEET UPDATE TESTS =====

    @Test
    fun `updateLeet modifies existing leet`() = runTest {
        val originalLeet = CustomLeet("Original", Icons.Default.Settings)
        originalLeet.setTranslation("A", "4")
        leetManager.addLeet(originalLeet)

        val updatedLeet = CustomLeet("Updated", Icons.Default.Star)
        updatedLeet.setTranslation("A", "@")

        val result = leetManager.updateLeet(0, updatedLeet)

        Assert.assertTrue(result.isSuccess)

        leetManager.leets.test {
            val leets = awaitItem()
            Assert.assertEquals("Updated", leets[0].name)
            Assert.assertEquals("@", leets[0].getTranslation("A"))
        }
    }

    @Test
    fun `updateLeet with invalid index fails`() = runTest {
        val leet = CustomLeet("Test", Icons.Default.Settings)

        val result = leetManager.updateLeet(999, leet)

        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `updateLeet preserves other leets`() = runTest {
        leetManager.addLeet(CustomLeet("First", Icons.Default.Settings))
        leetManager.addLeet(CustomLeet("Second", Icons.Default.Star))

        val updatedLeet = CustomLeet("Updated First", Icons.Default.Settings)
        leetManager.updateLeet(0, updatedLeet)

        leetManager.leets.test {
            val leets = awaitItem()
            Assert.assertEquals("Updated First", leets[0].name)
            Assert.assertEquals("Second", leets[1].name)
        }
    }

    // ===== LEET DELETION TESTS =====

    @Test
    fun `deleteLeet removes leet and returns result`() = runTest {
        val leet = CustomLeet("To Delete", Icons.Default.Settings)
        leetManager.addLeet(leet)

        val result = leetManager.deleteLeet(0)

        Assert.assertTrue(result.isSuccess)
        val deletionResult = result.getOrNull()!!
        Assert.assertEquals("To Delete", deletionResult.deletedLeet.name)
        Assert.assertFalse(deletionResult.wasFavorite)
        Assert.assertTrue(deletionResult.wasLastLeet)

        leetManager.leets.test {
            val leets = awaitItem()
            Assert.assertTrue(leets.isEmpty())
        }
    }

    @Test
    fun `deleteLeet with invalid index fails`() = runTest {
        val result = leetManager.deleteLeet(999)

        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `deleteLeet favorite leet updates favorite index`() = runTest {
        leetManager.addLeet(CustomLeet("Favorite", Icons.Default.Settings))
        leetManager.setFavorite(LeetManager.MODE_CUSTOM, 0)

        val result = leetManager.deleteLeet(0)

        Assert.assertTrue(result.isSuccess)
        Assert.assertTrue(result.getOrNull()!!.wasFavorite)

        leetManager.favoriteIndex.test {
            Assert.assertEquals(LeetManager.FAV_NONE, awaitItem())
        }
    }

    @Test
    fun `deleteLeet adjusts favorite index when deleting before favorite`() = runTest {
        leetManager.addLeet(CustomLeet("First", Icons.Default.Settings))
        leetManager.addLeet(CustomLeet("Favorite", Icons.Default.Star))
        leetManager.setFavorite(LeetManager.MODE_CUSTOM, 1)

        leetManager.deleteLeet(0) // Delete first, favorite should shift to index 0

        leetManager.favoriteIndex.test {
            Assert.assertEquals(0, awaitItem())
        }
    }

    @Test
    fun `deleteLeet last leet updates current index`() = runTest {
        leetManager.addLeet(CustomLeet("First", Icons.Default.Settings))
        leetManager.addLeet(CustomLeet("Second", Icons.Default.Star))
        leetManager.setCurrentLeetIndex(1)

        leetManager.deleteLeet(1) // Delete last leet

        leetManager.currentLeetIndex.test {
            Assert.assertEquals(0, awaitItem()) // Should adjust to valid index
        }
    }

    // ===== CURRENT LEET INDEX TESTS =====

    @Test
    fun `setCurrentLeetIndex updates index`() = runTest {
        leetManager.addLeet(CustomLeet("First", Icons.Default.Settings))
        leetManager.addLeet(CustomLeet("Second", Icons.Default.Star))

        val result = leetManager.setCurrentLeetIndex(1)

        Assert.assertTrue(result.isSuccess)

        leetManager.currentLeetIndex.test {
            Assert.assertEquals(1, awaitItem())
        }
    }

    @Test
    fun `setCurrentLeetIndex with invalid index fails`() = runTest {
        leetManager.addLeet(CustomLeet("Test", Icons.Default.Settings))

        val result = leetManager.setCurrentLeetIndex(999)

        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `currentLeet flow reflects index changes`() = runTest {
        leetManager.addLeet(CustomLeet("First", Icons.Default.Settings))
        leetManager.addLeet(CustomLeet("Second", Icons.Default.Star))

        leetManager.setCurrentLeetIndex(1)

        leetManager.currentLeet.test {
            val currentLeet = awaitItem()
            Assert.assertNotNull(currentLeet)
            Assert.assertEquals("Second", currentLeet!!.name)
        }
    }

    @Test
    fun `currentLeet flow returns null for empty collection`() = runTest {
        leetManager.currentLeet.test {
            Assert.assertNull(awaitItem())
        }
    }

    // ===== FAVORITE MANAGEMENT TESTS =====

    @Test
    fun `setFavorite simple mode works`() = runTest {
        val result = leetManager.setFavorite(LeetManager.MODE_SIMPLE)

        Assert.assertTrue(result.isSuccess)

        leetManager.favoriteIndex.test {
            Assert.assertEquals(LeetManager.FAV_SIMPLE, awaitItem())
        }
    }

    @Test
    fun `setFavorite extended mode works`() = runTest {
        val result = leetManager.setFavorite(LeetManager.MODE_EXTENDED)

        Assert.assertTrue(result.isSuccess)

        leetManager.favoriteIndex.test {
            Assert.assertEquals(LeetManager.FAV_EXTENDED, awaitItem())
        }
    }

    @Test
    fun `setFavorite custom mode with valid index works`() = runTest {
        leetManager.addLeet(CustomLeet("Custom", Icons.Default.Settings))

        val result = leetManager.setFavorite(LeetManager.MODE_CUSTOM, 0)

        Assert.assertTrue(result.isSuccess)

        leetManager.favoriteIndex.test {
            Assert.assertEquals(0, awaitItem())
        }
    }

    @Test
    fun `setFavorite custom mode with invalid index fails`() = runTest {
        val result = leetManager.setFavorite(LeetManager.MODE_CUSTOM, 999)

        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `setFavorite invalid mode fails`() = runTest {
        val result = leetManager.setFavorite(999)

        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `toggleFavorite enables favorite`() = runTest {
        val result = leetManager.toggleFavorite(LeetManager.MODE_SIMPLE)

        Assert.assertTrue(result.isSuccess)
        Assert.assertTrue(result.getOrNull()!!)

        leetManager.favoriteIndex.test {
            Assert.assertEquals(LeetManager.FAV_SIMPLE, awaitItem())
        }
    }

    @Test
    fun `toggleFavorite disables favorite`() = runTest {
        leetManager.setFavorite(LeetManager.MODE_SIMPLE)

        val result = leetManager.toggleFavorite(LeetManager.MODE_SIMPLE)

        Assert.assertTrue(result.isSuccess)
        Assert.assertFalse(result.getOrNull()!!)

        leetManager.favoriteIndex.test {
            Assert.assertEquals(LeetManager.FAV_NONE, awaitItem())
        }
    }

    @Test
    fun `isFavorite returns correct status`() = runTest {
        Assert.assertFalse(leetManager.isFavorite(LeetManager.MODE_SIMPLE))

        leetManager.setFavorite(LeetManager.MODE_SIMPLE)

        Assert.assertTrue(leetManager.isFavorite(LeetManager.MODE_SIMPLE))
        Assert.assertFalse(leetManager.isFavorite(LeetManager.MODE_EXTENDED))
    }

    @Test
    fun `isFavorite for custom mode works`() = runTest {
        leetManager.addLeet(CustomLeet("Custom", Icons.Default.Settings))
        leetManager.setFavorite(LeetManager.MODE_CUSTOM, 0)

        Assert.assertTrue(leetManager.isFavorite(LeetManager.MODE_CUSTOM, 0))
        Assert.assertFalse(leetManager.isFavorite(LeetManager.MODE_CUSTOM, 1))
    }

    // ===== FAVORITE INFO TESTS =====

    @Test
    fun `getFavoriteLeetInfo returns null for no favorite`() {
        val info = leetManager.getFavoriteLeetInfo()
        Assert.assertNull(info)
    }

    @Test
    fun `getFavoriteLeetInfo returns simple info`() = runTest {
        leetManager.setFavorite(LeetManager.MODE_SIMPLE)

        val info = leetManager.getFavoriteLeetInfo()

        Assert.assertNotNull(info)
        Assert.assertEquals(LeetManager.MODE_SIMPLE, info!!.mode)
        Assert.assertEquals(-1, info.customIndex)
        Assert.assertNull(info.customLeet)
    }

    @Test
    fun `getFavoriteLeetInfo returns extended info`() = runTest {
        leetManager.setFavorite(LeetManager.MODE_EXTENDED)

        val info = leetManager.getFavoriteLeetInfo()

        Assert.assertNotNull(info)
        Assert.assertEquals(LeetManager.MODE_EXTENDED, info!!.mode)
        Assert.assertEquals(-1, info.customIndex)
        Assert.assertNull(info.customLeet)
    }

    @Test
    fun `getFavoriteLeetInfo returns custom info`() = runTest {
        val customLeet = CustomLeet("Custom Fav", Icons.Default.Star)
        leetManager.addLeet(customLeet)
        leetManager.setFavorite(LeetManager.MODE_CUSTOM, 0)

        val info = leetManager.getFavoriteLeetInfo()

        Assert.assertNotNull(info)
        Assert.assertEquals(LeetManager.MODE_CUSTOM, info!!.mode)
        Assert.assertEquals(0, info.customIndex)
        Assert.assertEquals("Custom Fav", info.customLeet!!.name)
    }

    // ===== FACTORY METHOD TESTS =====

    @Test
    fun `createLeetWithSimpleDefaults creates and adds leet`() = runTest {
        val result = leetManager.createLeetWithSimpleDefaults("Simple Test", Icons.Default.Settings)

        Assert.assertTrue(result.isSuccess)
        val leet = result.getOrNull()!!
        Assert.assertEquals("Simple Test", leet.name)
        Assert.assertEquals("4", leet.getTranslation("A"))
        Assert.assertEquals("3", leet.getTranslation("E"))

        leetManager.leets.test {
            val leets = awaitItem()
            Assert.assertEquals(1, leets.size)
            Assert.assertEquals("Simple Test", leets[0].name)
        }
    }

    @Test
    fun `createLeetWithExtendedDefaults creates and adds leet`() = runTest {
        val result = leetManager.createLeetWithExtendedDefaults("Extended Test", Icons.Default.Star)

        Assert.assertTrue(result.isSuccess)
        val leet = result.getOrNull()!!
        Assert.assertEquals("Extended Test", leet.name)
        Assert.assertEquals("/\\/\\", leet.getTranslation("M"))
        Assert.assertEquals("|\\|", leet.getTranslation("N"))

        leetManager.leets.test {
            val leets = awaitItem()
            Assert.assertEquals(1, leets.size)
            Assert.assertEquals("Extended Test", leets[0].name)
        }
    }

    // ===== PERSISTENCE TESTS =====

    @Test
    fun `leets persist across manager instances`() = runTest {
        leetManager.addLeet(CustomLeet("Persistent", Icons.Default.Settings))
        leetManager.setFavorite(LeetManager.MODE_CUSTOM, 0)
        leetManager.cleanup()

        // Create new manager instance
        val newManager = LeetManager(context)

        newManager.leets.test {
            val leets = awaitItem()
            Assert.assertEquals(1, leets.size)
            Assert.assertEquals("Persistent", leets[0].name)
        }

        newManager.favoriteIndex.test {
            Assert.assertEquals(0, awaitItem())
        }

        newManager.cleanup()
    }

    @Test
    fun `favorite index migration works`() = runTest {
        // Simulate old favorite format by writing directly to preferences
        sharedPrefs.edit()
            .putInt("favorite_leet", 0) // Old format: 0 = simple
            .apply()

        val newManager = LeetManager(context)

        newManager.favoriteIndex.test {
            Assert.assertEquals(LeetManager.FAV_SIMPLE, awaitItem())
        }

        newManager.cleanup()
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    fun `corrupted preferences don't crash manager`() = runTest {
        // Write corrupted JSON to preferences
        sharedPrefs.edit()
            .putString("leets", "invalid json {[")
            .apply()

        // Should not crash and start with empty collection
        val newManager = LeetManager(context)

        newManager.leets.test {
            val leets = awaitItem()
            Assert.assertTrue(leets.isEmpty())
        }

        newManager.cleanup()
    }

    @Test
    fun `invalid current index gets corrected`() = runTest {
        leetManager.addLeet(CustomLeet("Test", Icons.Default.Settings))

        // Simulate invalid index in preferences
        sharedPrefs.edit()
            .putInt("current_leet", 999)
            .apply()

        val newManager = LeetManager(context)

        newManager.currentLeetIndex.test {
            val index = awaitItem()
            Assert.assertTrue(index >= 0 && index < 1) // Should be corrected to valid range
        }

        newManager.cleanup()
    }

    // ===== CLEANUP TESTS =====

    @Test
    fun `cleanup cancels coroutine scope`() {
        // Should not throw
        leetManager.cleanup()

        // Should be safe to call multiple times
        leetManager.cleanup()
        leetManager.cleanup()
    }

    // ===== CONCURRENT ACCESS TESTS =====

    @Test
    fun `concurrent modifications are handled safely`() = runTest {
        // Add initial leets
        repeat(5) { i ->
            leetManager.addLeet(CustomLeet("Leet $i", Icons.Default.Settings))
        }

        // Simulate concurrent operations
        val jobs = List(10) { i ->
            kotlinx.coroutines.launch {
                when (i % 3) {
                    0 -> leetManager.addLeet(CustomLeet("Concurrent $i", Icons.Default.Settings))
                    1 -> leetManager.updateLeet(0, CustomLeet("Updated $i", Icons.Default.Star))
                    2 -> leetManager.setFavorite(LeetManager.MODE_SIMPLE)
                }
            }
        }

        // Wait for all operations to complete
        jobs.forEach { it.join() }

        // Verify final state is consistent
        leetManager.leets.test {
            val leets = awaitItem()
            Assert.assertTrue(leets.isNotEmpty()) // Should have some leets
            // All leets should have valid names
            Assert.assertTrue(leets.all { it.name.isNotEmpty() })
        }
    }

    // ===== DATA CLASS TESTS =====

    @Test
    fun `LeetDeletionResult contains correct data`() = runTest {
        val leet = CustomLeet("Test", Icons.Default.Settings)
        leetManager.addLeet(leet)
        leetManager.setFavorite(LeetManager.MODE_CUSTOM, 0)

        val result = leetManager.deleteLeet(0)
        val deletionResult = result.getOrNull()!!

        Assert.assertEquals(leet.name, deletionResult.deletedLeet.name)
        Assert.assertTrue(deletionResult.wasFavorite)
        Assert.assertTrue(deletionResult.wasLastLeet)
    }

    @Test
    fun `FavoriteLeetInfo contains correct data`() = runTest {
        val customLeet = CustomLeet("Favorite Custom", Icons.Default.Star)
        leetManager.addLeet(customLeet)
        leetManager.setFavorite(LeetManager.MODE_CUSTOM, 0)

        val info = leetManager.getFavoriteLeetInfo()!!

        Assert.assertEquals(LeetManager.MODE_CUSTOM, info.mode)
        Assert.assertEquals(0, info.customIndex)
        Assert.assertEquals("Favorite Custom", info.customLeet!!.name)
    }
}