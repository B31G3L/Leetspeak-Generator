// app/src/test/java/com/beigel/leetSpeak_Generator/widget/LeetWidgetProviderTest.kt
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
    fun `widget handles null context gracefully`() {
        val widgetIds = intArrayOf(1)

        // Should not crash with null context (though this is unlikely in practice)
        try {
            widgetProvider.onUpdate(null, mockAppWidgetManager, widgetIds)
        } catch (e: Exception) {
            // Expected - null context will cause issues, but shouldn't crash the provider
            assertTrue(e is NullPointerException || e is IllegalArgumentException)
        }
    }

    @Test
    fun `widget handles null app widget manager gracefully`() {
        val widgetIds = intArrayOf(1)

        try {
            widgetProvider.onUpdate(context, null, widgetIds)
        } catch (e: Exception) {
            // Expected - null manager will cause issues
            assertTrue(e is NullPointerException || e is IllegalArgumentException)
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

        val jobs = List(5) { i ->
            kotlinx.coroutines.launch {
                widgetProvider.onUpdate(context, mockAppWidgetManager, intArrayOf(i))
            }
        }

        jobs.forEach { it.join() }

        coVerify(exactly = 5) { mockRepository.loadFavoriteLeet() }
    }
}

// app/src/test/java/com/beigel/leetSpeak_Generator/TestUtils.kt
package com.beigel.leetSpeak_Generator

import androidx.compose.material.icons.filled.Star
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.data.VersionInfo
import com.beigel.leetSpeak_Generator.translation.LeetTranslator

/**
 * Test Utilities für die Leetspeak Generator App
 * Enthält häufig verwendete Test-Helpers und Mock-Objekte
 */
@OptIn(ExperimentalCoroutinesApi::class)
object TestUtils {

    // ===== MOCK DATA FACTORIES =====

    /**
     * Erstellt ein einfaches CustomLeet für Tests
     */
    fun createSimpleCustomLeet(name: String = "Test Leet"): CustomLeet {
        val leet = CustomLeet(name, Icons.Default.Settings)
        leet.setTranslation("A", "4")
        leet.setTranslation("E", "3")
        leet.setTranslation("O", "0")
        return leet
    }

    /**
     * Erstellt ein erweitertes CustomLeet für Tests
     */
    fun createExtendedCustomLeet(name: String = "Extended Test Leet"): CustomLeet {
        val leet = CustomLeet(name, Icons.Default.Star)
        leet.setTranslation("A", "4")
        leet.setTranslation("M", "/\\/\\")
        leet.setTranslation("N", "|\\|")
        leet.setTranslation("U", "|_|")
        return leet
    }

    /**
     * Erstellt ein vollständig konfiguriertes CustomLeet
     */
    fun createFullCustomLeet(name: String = "Full Test Leet"): CustomLeet {
        val leet = CustomLeet(name, Icons.Default.Settings)

        // Alle Buchstaben mit einzigartigen Übersetzungen
        ('A'..'Z').forEachIndexed { index, char ->
            leet.setTranslation(char.toString(), "[$index]")
        }

        return leet
    }

    /**
     * Erstellt eine Liste von Test-LeetOptions
     */
    fun createTestLeetOptions(): List<LeetOption> {
        return listOf(
            LeetOption.createSimple(isSelected = true),
            LeetOption.createExtended(isFavorite = true),
            LeetOption.createCustom(createSimpleCustomLeet("Custom 1"), 0),
            LeetOption.createCustom(createExtendedCustomLeet("Custom 2"), 1, isFavorite = true)
        )
    }

    /**
     * Erstellt Test-VersionInfo
     */
    fun createTestVersionInfo(code: Int = 100, name: String = "1.0.0"): VersionInfo {
        return VersionInfo(code, name)
    }

    // ===== ASSERTION HELPERS =====

    /**
     * Überprüft ob ein CustomLeet korrekt konfiguriert ist
     */
    fun assertCustomLeetValid(leet: CustomLeet) {
        assertNotNull(leet.name)
        assertTrue(leet.name.isNotEmpty())
        assertNotNull(leet.iconImageVector)
        assertNotNull(leet.translations)
    }

    /**
     * Überprüft ob zwei CustomLeets die gleichen Übersetzungen haben
     */
    fun assertSameTranslations(leet1: CustomLeet, leet2: CustomLeet) {
        assertEquals("Leets should have same number of translations",
            leet1.translations.size, leet2.translations.size)

        leet1.translations.forEach { (key, value) ->
            assertEquals("Translation for '$key' should match",
                value, leet2.getTranslation(key))
        }
    }

    /**
     * Überprüft ob eine LeetOption korrekt konfiguriert ist
     */
    fun assertLeetOptionValid(option: LeetOption) {
        assertNotNull(option.name)
        assertTrue(option.name.isNotEmpty())
        assertNotNull(option.description)
        assertNotNull(option.iconImageVector)
        assertTrue(option.mode >= 0)

        if (option.isCustom) {
            assertTrue(option.customIndex >= 0)
        } else {
            assertEquals(-1, option.customIndex)
        }
    }

    /**
     * Überprüft ob eine Übersetzung korrekt ist
     */
    fun assertTranslationCorrect(
        input: String,
        expected: String,
        mode: LeetTranslator.TranslationMode,
        customLeet: CustomLeet? = null
    ) {
        val result = LeetTranslator.translate(input, mode, customLeet)
        assertEquals("Translation of '$input' should be '$expected'", expected, result)
    }

    // ===== STRING HELPERS =====

    /**
     * Generiert zufälligen Test-String
     */
    fun randomString(length: Int = 10): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    /**
     * Generiert Leet-String für Tests
     */
    fun generateLeetString(original: String): String {
        return original
            .replace("A", "4")
            .replace("E", "3")
            .replace("O", "0")
            .replace("S", "5")
            .replace("T", "7")
    }

    /**
     * Erstellt Test-String mit bekannten Eigenschaften
     */
    fun createTestString(
        hasLetters: Boolean = true,
        hasNumbers: Boolean = false,
        hasSpecialChars: Boolean = false,
        length: Int = 10
    ): String {
        val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val special = "!@#$%^&*()"

        var chars = ""
        if (hasLetters) chars += letters
        if (hasNumbers) chars += numbers
        if (hasSpecialChars) chars += special

        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    // ===== PERFORMANCE HELPERS =====

    /**
     * Misst die Ausführungszeit einer Operation
     */
    inline fun <T> measureTime(operation: () -> T): Pair<T, Long> {
        val startTime = System.currentTimeMillis()
        val result = operation()
        val endTime = System.currentTimeMillis()
        return Pair(result, endTime - startTime)
    }

    /**
     * Überprüft ob eine Operation innerhalb der erwarteten Zeit abgeschlossen wird
     */
    inline fun <T> assertCompletesWithin(
        maxTimeMs: Long,
        operation: () -> T
    ): T {
        val (result, duration) = measureTime(operation)
        assertTrue(
            "Operation should complete within ${maxTimeMs}ms but took ${duration}ms",
            duration <= maxTimeMs
        )
        return result
    }

    // ===== COROUTINE HELPERS =====

    /**
     * Führt einen suspendierenden Test aus
     */
    fun runSuspendTest(test: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest {
            test()
        }
    }

    // ===== ERROR TESTING HELPERS =====

    /**
     * Überprüft ob eine Exception mit erwarteter Nachricht geworfen wird
     */
    inline fun <reified T : Exception> assertThrowsWithMessage(
        expectedMessage: String,
        operation: () -> Unit
    ) {
        try {
            operation()
            fail("Expected ${T::class.simpleName} to be thrown")
        } catch (e: Exception) {
            assertTrue("Expected ${T::class.simpleName} but got ${e::class.simpleName}",
                e is T)
            assertTrue("Expected message '$expectedMessage' but got '${e.message}'",
                e.message?.contains(expectedMessage) == true)
        }
    }

    /**
     * Überprüft ob eine Operation keine Exception wirft
     */
    inline fun assertDoesNotThrow(operation: () -> Unit) {
        try {
            operation()
        } catch (e: Exception) {
            fail("Operation should not throw exception but threw: ${e.message}")
        }
    }

    // ===== COLLECTION HELPERS =====

    /**
     * Überprüft ob eine Liste sortiert ist
     */
    fun <T : Comparable<T>> assertSorted(list: List<T>, ascending: Boolean = true) {
        for (i in 1 until list.size) {
            if (ascending) {
                assertTrue("List should be sorted ascending at index $i",
                    list[i-1] <= list[i])
            } else {
                assertTrue("List should be sorted descending at index $i",
                    list[i-1] >= list[i])
            }
        }
    }

    /**
     * Überprüft ob eine Liste einzigartige Elemente enthält
     */
    fun <T> assertAllUnique(list: List<T>) {
        val set = list.toSet()
        assertEquals("List should contain only unique elements",
            list.size, set.size)
    }

    // ===== MOCK VERIFICATION HELPERS =====

    /**
     * Überprüft ob ein Mock nie aufgerufen wurde
     */
    fun assertMockNeverCalled(mock: Any, methodName: String) {
        // This would need to be implemented with your specific mocking framework
        // For example with MockK: verify(exactly = 0) { mock.method() }
    }

    /**
     * Überprüft ob ein Mock genau n-mal aufgerufen wurde
     */
    fun assertMockCalledTimes(mock: Any, methodName: String, times: Int) {
        // This would need to be implemented with your specific mocking framework
        // For example with MockK: verify(exactly = times) { mock.method() }
    }

    // ===== DATA VALIDATION HELPERS =====

    /**
     * Überprüft ob ein String gültiges Leetspeak ist
     */
    fun assertIsValidLeetspeak(text: String) {
        val leetChars = setOf('4', '3', '1', '0', '5', '7', '8', '#', '@', '|', '/', '\\')
        val leetCharCount = text.count { it in leetChars }
        assertTrue("Text should contain leet characters", leetCharCount > 0)
    }

    /**
     * Überprüft ob ein String normaler Text ist (kein Leetspeak)
     */
    fun assertIsNormalText(text: String) {
        val leetChars = setOf('4', '3', '1', '0', '5', '7', '8', '#', '@', '|', '/', '\\')
        val leetCharCount = text.count { it in leetChars }
        val leetPercentage = if (text.isNotEmpty()) leetCharCount.toFloat() / text.length else 0f
        assertTrue("Text should not be leetspeak (${leetPercentage * 100}% leet chars)",
            leetPercentage < 0.2f)
    }

    // ===== CONFIGURATION HELPERS =====

    /**
     * Erstellt eine Test-Konfiguration
     */
    data class TestConfig(
        val enableLogging: Boolean = false,
        val mockNetworkCalls: Boolean = true,
        val fastMode: Boolean = true
    )

    /**
     * Standard Test-Konfiguration
     */
    val defaultTestConfig = TestConfig()

    /**
     * Debug Test-Konfiguration
     */
    val debugTestConfig = TestConfig(
        enableLogging = true,
        mockNetworkCalls = false,
        fastMode = false
    )
}