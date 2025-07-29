package com.beigel.leetSpeak_Generator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.data.VersionInfo
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

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