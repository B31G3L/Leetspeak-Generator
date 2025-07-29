package com.beigel.leetSpeak_Generator.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import com.beigel.leetSpeak_Generator.data.CustomLeet
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
 * Tests für LeetWidgetProvider
 * Note: Widget testing is limited in unit tests, but we can test the core logic
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class LeetWidgetProviderTest {

    private lateinit var context: Context
    private lateinit var mockRepository: LeetRepository
    private lateinit var mockAppWidgetManager: AppWidgetManager
    private lateinit var widgetProvider: LeetWidgetProvider

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockRepository = mockk(relaxed = true)
        mockAppWidgetManager = mockk(relaxed = true)

        widgetProvider = LeetWidgetProvider().apply {
            // Inject mocked repository
            leetRepository = mockRepository
        }
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ===== BASIC WIDGET TESTS =====

    @Test
    fun `onUpdate calls updateAppWidget for each widget ID`() {
        val widgetIds = intArrayOf(1, 2, 3)

        // Mock successful favorite loading
        coEvery { mockRepository.loadFavoriteLeet() } returns Result.success(
            LeetRepository.FavoriteLeetResult.simple()
        )

        // onUpdate should not crash
        widgetProvider.onUpdate(context, mockAppWidgetManager, widgetIds)

        // Verify that updateAppWidget was attempted (hard to verify directly due to coroutines)
        coVerify(exactly = 3) { mockRepository.loadFavoriteLeet() }
    }

    @Test
    fun `onUpdate with empty widget IDs array doesn't crash`() {
        val widgetIds = intArrayOf()

        // Should not crash with empty array
        widgetProvider.onUpdate(context, mockAppWidgetManager, widgetIds)

        // Should not call repository
        coVerify(exactly = 0) { mockRepository.loadFavoriteLeet() }
    }

    @Test
    fun `onUpdate handles single widget ID`() {
        val widgetIds = intArrayOf(42)

        coEvery { mockRepository.loadFavoriteLeet() } returns Result.success(
            LeetRepository.FavoriteLeetResult.extended()
        )

        widgetProvider.onUpdate(context, mockAppWidgetManager, widgetIds)

        coVerify(exactly = 1) { mockRepository.loadFavoriteLeet() }
    }

    // ===== FAVORITE LEET HANDLING TESTS =====

    @Test
    fun `widget handles simple leet favorite correctly`() = runTest {
        coEvery { mockRepository.loadFavoriteLeet() } returns Result.success(
            LeetRepository.FavoriteLeetResult.simple()
        )

        widgetProvider.onUpdate(context, mockAppWidgetManager, intArrayOf(1))

        // Verify repository was called
        coVerify { mockRepository.loadFavoriteLeet() }

        // In a real scenario, this would update the widget views
        // We can't easily test RemoteViews updates in unit tests
    }

    @Test
    fun `widget handles extended leet favorite correctly`() = runTest {
        coEvery { mockRepository.loadFavoriteLeet() } returns Result.success(
            LeetRepository.FavoriteLeetResult.extended()
        )

        widgetProvider.onUpdate(context, mockAppWidgetManager, intArrayOf(1))

        coVerify { mockRepository.loadFavoriteLeet() }
    }

    @Test
    fun `widget handles custom leet favorite correctly`() = runTest {
        val customLeet = CustomLeet("Widget Custom", Icons.Default.Settings)
        customLeet.setTranslation("A", "∀")

        coEvery { mockRepository.loadFavoriteLeet() } returns Result.success(
            LeetRepository.FavoriteLeetResult.custom(0, customLeet)
        )

        widgetProvider.onUpdate(context, mockAppWidgetManager, intArrayOf(1))

        coVerify { mockRepository.loadFavoriteLeet() }
    }

    @Test
    fun `widget handles repository failure gracefully`() = runTest {
        coEvery { mockRepository.loadFavoriteLeet() } returns Result.failure(
            Exception("Repository error")
        )

        // Should not crash even when repository fails
        widgetProvider.onUpdate(context, mockAppWidgetManager, intArrayOf(1))

        coVerify { mockRepository.loadFavoriteLeet() }
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    fun `widget handles valid context and app widget manager`() {
        val widgetIds = intArrayOf(1)

        // Should not crash with valid parameters
        try {
            widgetProvider.onUpdate(context, mockAppWidgetManager, widgetIds)
        } catch (e: Exception) {
            fail("Should not throw exception with valid parameters: ${e.message}")
        }
    }

    @Test
    fun `widget handles repository exceptions during loading`() = runTest {
        coEvery { mockRepository.loadFavoriteLeet() } throws RuntimeException("Database error")

        // Should handle exception gracefully
        widgetProvider.onUpdate(context, mockAppWidgetManager, intArrayOf(1))

        coVerify { mockRepository.loadFavoriteLeet() }
    }

    // ===== WIDGET PROVIDER LIFECYCLE TESTS =====

    @Test
    fun `widget provider can be instantiated`() {
        val provider = LeetWidgetProvider()
        assertNotNull(provider)
    }

    @Test
    fun `multiple widget updates can be called sequentially`() = runTest {
        coEvery { mockRepository.loadFavoriteLeet() } returns Result.success(
            LeetRepository.FavoriteLeetResult.simple()
        )

        widgetProvider.onUpdate(context, mockAppWidgetManager, intArrayOf(1))
        widgetProvider.onUpdate(context, mockAppWidgetManager, intArrayOf(2))
        widgetProvider.onUpdate(context, mockAppWidgetManager, intArrayOf(3))

        coVerify(exactly = 3) { mockRepository.loadFavoriteLeet() }
    }

    @Test
    fun `widget updates with overlapping IDs work correctly`() = runTest {
        coEvery { mockRepository.loadFavoriteLeet() } returns Result.success(
            LeetRepository.FavoriteLeetResult.simple()
        )

        widgetProvider.onUpdate(context, mockAppWidgetManager, intArrayOf(1, 2))
        widgetProvider.onUpdate(context, mockAppWidgetManager, intArrayOf(2, 3))

        // Should call repository for each widget update
        coVerify(exactly = 4) { mockRepository.loadFavoriteLeet() }
    }

    // ===== PERFORMANCE TESTS =====

    @Test
    fun `widget update completes within reasonable time`() = runTest {
        coEvery { mockRepository.loadFavoriteLeet() } returns Result.success(
            LeetRepository.FavoriteLeetResult.simple()
        )

        val startTime = System.currentTimeMillis()

        widgetProvider.onUpdate(context, mockAppWidgetManager, intArrayOf(1, 2, 3, 4, 5))

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        assertTrue("Widget update should complete within 2 seconds", duration < 2000)
    }

    @Test
    fun `multiple concurrent widget updates don't cause issues`() = runTest {
        coEvery { mockRepository.loadFavoriteLeet() } returns Result.success(
            LeetRepository.FavoriteLeetResult.simple()
        )

        val jobs: List<Job> = List(5) { i ->
            launch {
                widgetProvider.onUpdate(context, mockAppWidgetManager, intArrayOf(i))
            }
        }

        jobs.forEach { it.join() }

        coVerify(exactly = 5) { mockRepository.loadFavoriteLeet() }
    }
}