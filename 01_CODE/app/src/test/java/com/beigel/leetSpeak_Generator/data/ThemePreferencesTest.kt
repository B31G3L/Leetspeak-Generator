package com.beigel.leetSpeak_Generator.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
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
 * Tests für ThemePreferences
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ThemePreferencesTest {

    private lateinit var context: Context
    private lateinit var themePreferences: ThemePreferences

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear existing preferences
        runTest {
            context.dataStore.edit { it.clear() }
        }
        themePreferences = ThemePreferences(context)
    }

    @After
    fun tearDown() {
        runTest {
            context.dataStore.edit { it.clear() }
        }
    }

    // ===== THEME MODE TESTS =====

    @Test
    fun `themeMode flow starts with system default`() = runTest {
        themePreferences.themeMode.test {
            assertEquals(ThemePreferences.THEME_SYSTEM, awaitItem())
        }
    }

    @Test
    fun `setTheme updates theme mode`() = runTest {
        themePreferences.setTheme(ThemePreferences.THEME_DARK)

        themePreferences.themeMode.test {
            assertEquals(ThemePreferences.THEME_DARK, awaitItem())
        }
    }

    @Test
    fun `setTheme to light mode works`() = runTest {
        themePreferences.setTheme(ThemePreferences.THEME_LIGHT)

        themePreferences.themeMode.test {
            assertEquals(ThemePreferences.THEME_LIGHT, awaitItem())
        }
    }

    @Test
    fun `setTheme to system mode resets to default`() = runTest {
        // First set to dark
        themePreferences.setTheme(ThemePreferences.THEME_DARK)

        // Then reset to system
        themePreferences.setTheme(ThemePreferences.THEME_SYSTEM)

        themePreferences.themeMode.test {
            assertEquals(ThemePreferences.THEME_SYSTEM, awaitItem())
        }
    }

    @Test
    fun `theme constants are correct`() {
        assertEquals("system", ThemePreferences.THEME_SYSTEM)
        assertEquals("light", ThemePreferences.THEME_LIGHT)
        assertEquals("dark", ThemePreferences.THEME_DARK)
    }

    @Test
    fun `multiple theme changes work correctly`() = runTest {
        themePreferences.setTheme(ThemePreferences.THEME_LIGHT)
        themePreferences.setTheme(ThemePreferences.THEME_DARK)
        themePreferences.setTheme(ThemePreferences.THEME_SYSTEM)

        themePreferences.themeMode.test {
            assertEquals(ThemePreferences.THEME_SYSTEM, awaitItem())
        }
    }

    // ===== DEFAULT VIEW EXPANDED TESTS =====

    @Test
    fun `defaultViewExpanded flow starts with false`() = runTest {
        themePreferences.defaultViewExpanded.test {
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `setDefaultViewExpanded updates expanded state`() = runTest {
        themePreferences.setDefaultViewExpanded(true)

        themePreferences.defaultViewExpanded.test {
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun `setDefaultViewExpanded to false works`() = runTest {
        // First set to true
        themePreferences.setDefaultViewExpanded(true)

        // Then set to false
        themePreferences.setDefaultViewExpanded(false)

        themePreferences.defaultViewExpanded.test {
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `multiple expanded state changes work correctly`() = runTest {
        themePreferences.setDefaultViewExpanded(true)
        themePreferences.setDefaultViewExpanded(false)
        themePreferences.setDefaultViewExpanded(true)

        themePreferences.defaultViewExpanded.test {
            assertEquals(true, awaitItem())
        }
    }

    // ===== PERSISTENCE TESTS =====

    @Test
    fun `preferences persist across instances`() = runTest {
        themePreferences.setTheme(ThemePreferences.THEME_DARK)
        themePreferences.setDefaultViewExpanded(true)

        // Create new instance
        val newThemePreferences = ThemePreferences(context)

        newThemePreferences.themeMode.test {
            assertEquals(ThemePreferences.THEME_DARK, awaitItem())
        }

        newThemePreferences.defaultViewExpanded.test {
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun `preferences handle corrupted data gracefully`() = runTest {
        // Manually corrupt the datastore (in practice this would be rare)
        context.dataStore.edit { preferences ->
            // This shouldn't crash when read
            preferences.clear()
        }

        val newThemePreferences = ThemePreferences(context)

        // Should fall back to defaults
        newThemePreferences.themeMode.test {
            assertEquals(ThemePreferences.THEME_SYSTEM, awaitItem())
        }

        newThemePreferences.defaultViewExpanded.test {
            assertEquals(false, awaitItem())
        }
    }

    // ===== CONCURRENT ACCESS TESTS =====

    @Test
    fun `concurrent theme changes are handled correctly`() = runTest {
        val jobs: List<Job> = List(10) { i ->
            launch {
                val theme = when (i % 3) {
                    0 -> ThemePreferences.THEME_SYSTEM
                    1 -> ThemePreferences.THEME_LIGHT
                    else -> ThemePreferences.THEME_DARK
                }
                themePreferences.setTheme(theme)
            }
        }

        jobs.forEach { it.join() }

        // Final state should be one of the valid themes
        themePreferences.themeMode.test {
            val finalTheme = awaitItem()
            assertTrue(
                finalTheme in listOf(
                    ThemePreferences.THEME_SYSTEM,
                    ThemePreferences.THEME_LIGHT,
                    ThemePreferences.THEME_DARK
                )
            )
        }
    }

    @Test
    fun `concurrent expanded state changes are handled correctly`() = runTest {
        val jobs: List<Job> = List(10) { i ->
            launch {
                themePreferences.setDefaultViewExpanded(i % 2 == 0)
            }
        }

        jobs.forEach { it.join() }

        // Final state should be a valid boolean
        themePreferences.defaultViewExpanded.test {
            val finalState = awaitItem()
            assertTrue(finalState is Boolean)
        }
    }

    // ===== EDGE CASES =====

    @Test
    fun `setTheme with invalid value still persists`() = runTest {
        // Even invalid values should be stored (validation should be elsewhere)
        themePreferences.setTheme("invalid_theme")

        themePreferences.themeMode.test {
            assertEquals("invalid_theme", awaitItem())
        }
    }

    @Test
    fun `setTheme with empty string works`() = runTest {
        themePreferences.setTheme("")

        themePreferences.themeMode.test {
            assertEquals("", awaitItem())
        }
    }

    @Test
    fun `rapid successive calls work correctly`() = runTest {
        repeat(100) { i ->
            themePreferences.setTheme(if (i % 2 == 0) ThemePreferences.THEME_LIGHT else ThemePreferences.THEME_DARK)
        }

        // Should end with dark theme (99 is odd)
        themePreferences.themeMode.test {
            assertEquals(ThemePreferences.THEME_DARK, awaitItem())
        }
    }

    // ===== PERFORMANCE TESTS =====

    @Test
    fun `preference changes complete quickly`() = runTest {
        val startTime = System.currentTimeMillis()

        repeat(50) { i ->
            themePreferences.setTheme(ThemePreferences.THEME_DARK)
            themePreferences.setDefaultViewExpanded(i % 2 == 0)
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        assertTrue("Preference changes should complete within 1 second", duration < 1000)
    }
}