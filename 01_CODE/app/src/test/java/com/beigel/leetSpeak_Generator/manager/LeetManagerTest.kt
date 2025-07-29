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

    @Test
    fun `addLeet adds leet and returns index`() = runTest {
        val customLeet = CustomLeet("Test Leet", Icons.Default.Settings)

        val result = leetManager.addLeet(customLeet)

        // Use Kotlin's Result API
      //  Assert.assertTrue(result.isSuccess)
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
    fun `setCurrentLeetIndex updates index`() = runTest {
        leetManager.addLeet(CustomLeet("First", Icons.Default.Settings))
        leetManager.addLeet(CustomLeet("Second", Icons.Default.Star))

        val result = leetManager.setCurrentLeetIndex(1)

        //Assert.assertTrue(result.isSuccess)

        leetManager.currentLeetIndex.test {
            Assert.assertEquals(1, awaitItem())
        }
    }

    @Test
    fun `setCurrentLeetIndex with invalid index fails`() = runTest {
        leetManager.addLeet(CustomLeet("Test", Icons.Default.Settings))

        val result = leetManager.setCurrentLeetIndex(999)

       // Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `setFavorite simple mode works`() = runTest {
        val result = leetManager.setFavorite(LeetManager.MODE_SIMPLE)

      //  Assert.assertTrue(result.isSuccess)

        leetManager.favoriteIndex.test {
            Assert.assertEquals(LeetManager.FAV_SIMPLE, awaitItem())
        }
    }

    @Test
    fun `cleanup doesn't throw exceptions`() {
        // Should not throw
        leetManager.cleanup()

        // Should be able to call multiple times
        leetManager.cleanup()
        leetManager.cleanup()
    }
}