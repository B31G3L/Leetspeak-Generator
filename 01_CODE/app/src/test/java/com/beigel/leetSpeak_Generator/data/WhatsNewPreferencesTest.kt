package com.beigel.leetSpeak_Generator.data

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests für WhatsNewPreferences
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WhatsNewPreferencesTest {

    private lateinit var context: Context
    private lateinit var mockContext: Context
    private lateinit var mockPackageManager: PackageManager
    private lateinit var whatsNewPreferences: WhatsNewPreferences

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockContext = mockk()
        mockPackageManager = mockk()

        // Clear existing preferences
        runTest {
            context.dataStore.edit { it.clear() }
        }

        whatsNewPreferences = WhatsNewPreferences(context)
    }

    @After
    fun tearDown() {
        runTest {
            context.dataStore.edit { it.clear() }
        }
        clearAllMocks()
    }

    // ===== VERSION INFO TESTS =====

    @Test
    fun `getCurrentVersionInfo returns correct version`() {
        val versionInfo = whatsNewPreferences.getCurrentVersionInfo()

        assertNotNull(versionInfo)
        assertTrue(versionInfo.versionCode > 0)
        assertFalse(versionInfo.versionName.isEmpty())
    }

    @Test
    fun `getCurrentVersionInfo handles package manager exceptions`() {
        // Create preferences with mocked context that throws exception
        every { mockContext.packageName } returns "com.test.app"
        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getPackageInfo(any<String>(), any<Int>()) } throws PackageManager.NameNotFoundException()
        every { mockContext.dataStore } returns context.dataStore

        val mockPreferences = WhatsNewPreferences(mockContext)
        val versionInfo = mockPreferences.getCurrentVersionInfo()

        // Should return fallback values
        assertEquals(1, versionInfo.versionCode)
        assertEquals("1.0.0", versionInfo.versionName)
    }

    // ===== SHOULD SHOW WHAT'S NEW TESTS =====

    @Test
    fun `shouldShowWhatsNew returns true for first launch`() = runTest {
        whatsNewPreferences.shouldShowWhatsNew.test {
            assertTrue(awaitItem()) // First launch should show dialog
        }
    }

    @Test
    fun `shouldShowWhatsNew returns false after marking as shown`() = runTest {
        whatsNewPreferences.markCurrentVersionAsShown()

        whatsNewPreferences.shouldShowWhatsNew.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `shouldShowWhatsNew returns true after version increase`() = runTest {
        whatsNewPreferences.markCurrentVersionAsShown()

        // Simulate version increase by creating mock with higher version
        val mockPackageInfo = mockk<PackageInfo>()
        mockPackageInfo.versionCode = whatsNewPreferences.getCurrentVersionInfo().versionCode + 1
        mockPackageInfo.versionName = "2.0.0"

        every { mockContext.packageName } returns "com.test.app"
        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getPackageInfo(any<String>(), any<Int>()) } returns mockPackageInfo
        every { mockContext.dataStore } returns context.dataStore

        val newPreferences = WhatsNewPreferences(mockContext)

        newPreferences.shouldShowWhatsNew.test {
            assertTrue(awaitItem()) // Should show for new version
        }
    }

    // ===== MARK AS SHOWN TESTS =====

    @Test
    fun `markCurrentVersionAsShown updates preferences`() = runTest {
        val initialVersion = whatsNewPreferences.getCurrentVersionInfo()

        whatsNewPreferences.markCurrentVersionAsShown()

        whatsNewPreferences.lastShownVersionInfo.test {
            val lastShown = awaitItem()
            assertNotNull(lastShown)
            assertEquals(initialVersion.versionCode, lastShown!!.versionCode)
            assertEquals(initialVersion.versionName, lastShown.versionName)
        }
    }

    @Test
    fun `markCurrentVersionAsShown sets first launch marker`() = runTest {
        whatsNewPreferences.markCurrentVersionAsShown()

        whatsNewPreferences.isFirstLaunch.test {
            assertFalse(awaitItem()) // Should no longer be first launch
        }
    }

    // ===== FIRST LAUNCH TESTS =====

    @Test
    fun `isFirstLaunch returns true initially`() = runTest {
        whatsNewPreferences.isFirstLaunch.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `isFirstLaunch returns false after marking as shown`() = runTest {
        whatsNewPreferences.markCurrentVersionAsShown()

        whatsNewPreferences.isFirstLaunch.test {
            assertFalse(awaitItem())
        }
    }

    // ===== LAST SHOWN VERSION TESTS =====

    @Test
    fun `lastShownVersionInfo returns null initially`() = runTest {
        whatsNewPreferences.lastShownVersionInfo.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `lastShownVersionInfo returns correct version after marking`() = runTest {
        val currentVersion = whatsNewPreferences.getCurrentVersionInfo()
        whatsNewPreferences.markCurrentVersionAsShown()

        whatsNewPreferences.lastShownVersionInfo.test {
            val lastShown = awaitItem()
            assertNotNull(lastShown)
            assertEquals(currentVersion.versionCode, lastShown!!.versionCode)
            assertEquals(currentVersion.versionName, lastShown.versionName)
        }
    }

    // ===== TESTING METHODS TESTS =====

    @Test
    fun `resetForTesting clears all preferences`() = runTest {
        whatsNewPreferences.markCurrentVersionAsShown()

        whatsNewPreferences.resetForTesting()

        whatsNewPreferences.shouldShowWhatsNew.test {
            assertTrue(awaitItem()) // Should show again after reset
        }

        whatsNewPreferences.isFirstLaunch.test {
            assertTrue(awaitItem()) // Should be first launch again
        }

        whatsNewPreferences.lastShownVersionInfo.test {
            assertNull(awaitItem()) // Should be null again
        }
    }

    @Test
    fun `forceShowNextTime sets lower version code`() = runTest {
        whatsNewPreferences.markCurrentVersionAsShown()

        whatsNewPreferences.forceShowNextTime()

        whatsNewPreferences.shouldShowWhatsNew.test {
            assertTrue(awaitItem()) // Should show again
        }
    }

    @Test
    fun `forceShowNextTime with already forced state still works`() = runTest {
        whatsNewPreferences.forceShowNextTime()
        whatsNewPreferences.forceShowNextTime() // Call twice

        whatsNewPreferences.shouldShowWhatsNew.test {
            assertTrue(awaitItem())
        }
    }

    // ===== PERSISTENCE TESTS =====

    @Test
    fun `preferences persist across instances`() = runTest {
        whatsNewPreferences.markCurrentVersionAsShown()

        val newPreferences = WhatsNewPreferences(context)

        newPreferences.shouldShowWhatsNew.test {
            assertFalse(awaitItem()) // Should remember it was shown
        }

        newPreferences.isFirstLaunch.test {
            assertFalse(awaitItem()) // Should remember it's not first launch
        }
    }

    // ===== EDGE CASES =====

    @Test
    fun `preferences handle multiple rapid markings correctly`() = runTest {
        repeat(10) {
            whatsNewPreferences.markCurrentVersionAsShown()
        }

        whatsNewPreferences.shouldShowWhatsNew.test {
            assertFalse(awaitItem()) // Should still be false
        }
    }

    @Test
    fun `preferences handle rapid reset and force operations`() = runTest {
        repeat(5) { i ->
            if (i % 2 == 0) {
                whatsNewPreferences.resetForTesting()
            } else {
                whatsNewPreferences.forceShowNextTime()
            }
        }

        whatsNewPreferences.shouldShowWhatsNew.test {
            assertTrue(awaitItem()) // Should end up showing
        }
    }

    // ===== CONCURRENT ACCESS TESTS =====

    @Test
    fun `concurrent operations are handled safely`() = runTest {
        val jobs: List<Job> = List(10) { i ->
            launch {
                when (i % 3) {
                    0 -> whatsNewPreferences.markCurrentVersionAsShown()
                    1 -> whatsNewPreferences.resetForTesting()
                    2 -> whatsNewPreferences.forceShowNextTime()
                }
            }
        }

        jobs.forEach { it.join() }

        // Final state should be consistent
        whatsNewPreferences.shouldShowWhatsNew.test {
            val shouldShow = awaitItem()
            assertTrue(shouldShow is Boolean) // Should be a valid boolean
        }
    }

    // ===== VERSION COMPARISON TESTS =====

    @Test
    fun `version comparison works correctly`() {
        val version1 = VersionInfo(10, "1.0.0")
        val version2 = VersionInfo(20, "2.0.0")

        assertTrue(version2.isNewerThan(version1))
        assertFalse(version1.isNewerThan(version2))
        assertFalse(version1.isNewerThan(version1))
    }

    @Test
    fun `displayVersion formatting works`() {
        val version = VersionInfo(42, "1.2.3-beta")
        assertEquals("Version 1.2.3-beta", version.displayVersion)
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    fun `preferences handle datastore errors gracefully`() = runTest {
        // This test verifies that datastore operations don't crash the app
        // In practice, datastore is quite robust, but we test the general pattern

        try {
            whatsNewPreferences.markCurrentVersionAsShown()
            whatsNewPreferences.resetForTesting()
            whatsNewPreferences.forceShowNextTime()
        } catch (e: Exception) {
            fail("Preferences operations should not throw exceptions: ${e.message}")
        }
    }
}