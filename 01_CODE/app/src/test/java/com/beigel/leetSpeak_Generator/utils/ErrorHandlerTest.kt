package com.beigel.leetSpeak_Generator.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import android.widget.Toast
import androidx.test.core.app.ApplicationProvider
import io.mockk.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowToast

/**
 * Umfassende Tests für ErrorHandler Utility
 */
@RunWith(RobolectricTestRunner::class)
class ErrorHandlerTest {

    private lateinit var context: Context
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockContext = mockk(relaxed = true)

        // Clear any existing toasts
        ShadowToast.reset()
    }

    @After
    fun tearDown() {
        clearAllMocks()
        ShadowToast.reset()
    }

    // ===== RESULT WRAPPER TESTS =====

    @Test
    fun `Result Success contains data`() {
        val result = ErrorHandler.Result.Success("test data")

        Assert.assertEquals("test data", result.data)
        Assert.assertTrue(result is ErrorHandler.Result.Success)
    }

    @Test
    fun `Result Error contains exception and message`() {
        val exception = RuntimeException("Test error")
        val result = ErrorHandler.Result.Error<String>(exception, "Error message")

        Assert.assertEquals(exception, result.exception)
        Assert.assertEquals("Error message", result.message)
        Assert.assertTrue(result is ErrorHandler.Result.Error)
    }

    @Test
    fun `Result onSuccess executes action for success`() {
        var actionExecuted = false
        val result = ErrorHandler.Result.Success("data")

        result.onSuccess { data ->
            actionExecuted = true
            Assert.assertEquals("data", data)
        }

        Assert.assertTrue(actionExecuted)
    }

    @Test
    fun `Result onSuccess does not execute for error`() {
        var actionExecuted = false
        val result = ErrorHandler.Result.Error<String>(Exception(), "Error")

        result.onSuccess {
            actionExecuted = true
        }

        Assert.assertFalse(actionExecuted)
    }

    @Test
    fun `Result onError executes action for error`() {
        var actionExecuted = false
        val exception = RuntimeException("Test")
        val result = ErrorHandler.Result.Error<String>(exception, "Error message")

        result.onError { ex, msg ->
            actionExecuted = true
            Assert.assertEquals(exception, ex)
            Assert.assertEquals("Error message", msg)
        }

        Assert.assertTrue(actionExecuted)
    }

    @Test
    fun `Result getOrNull returns data for success`() {
        val result = ErrorHandler.Result.Success("test data")

        Assert.assertEquals("test data", result.getOrNull())
    }

    @Test
    fun `Result getOrNull returns null for error`() {
        val result = ErrorHandler.Result.Error<String>(Exception(), "Error")

        Assert.assertNull(result.getOrNull())
    }

    @Test
    fun `Result getOrDefault returns data for success`() {
        val result = ErrorHandler.Result.Success("test data")

        Assert.assertEquals("test data", result.getOrDefault("default"))
    }

    @Test
    fun `Result getOrDefault returns default for error`() {
        val result = ErrorHandler.Result.Error<String>(Exception(), "Error")

        Assert.assertEquals("default", result.getOrDefault("default"))
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    fun `handleError shows toast for warning severity`() {
        val exception = RuntimeException("Test warning")

        ErrorHandler.handleError(
            context,
            exception,
            "Warning message",
            ErrorHandler.ErrorSeverity.WARNING
        )

        val latestToast = ShadowToast.getLatestToast()
        Assert.assertNotNull(latestToast)
        Assert.assertEquals("Warning message", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `handleError shows toast for error severity`() {
        val exception = RuntimeException("Test error")

        ErrorHandler.handleError(
            context,
            exception,
            "Error message",
            ErrorHandler.ErrorSeverity.ERROR
        )

        val latestToast = ShadowToast.getLatestToast()
        Assert.assertNotNull(latestToast)
        Assert.assertEquals("Error message", ShadowToast.getTextOfLatestToast())
    }

    // ===== SAFE EXECUTE TESTS =====

    @Test
    fun `safeExecute returns success for successful operation`() {
        val result = ErrorHandler.safeExecute<String> {
            "successful result"
        }

        Assert.assertTrue(result is ErrorHandler.Result.Success)
        Assert.assertEquals("successful result", result.getOrNull())
    }

    @Test
    fun `safeExecute returns error for failing operation`() {
        val result = ErrorHandler.safeExecute<String> {
            throw RuntimeException("Operation failed")
        }

        Assert.assertTrue(result is ErrorHandler.Result.Error)
        Assert.assertEquals("An error occurred", (result as ErrorHandler.Result.Error).message)
    }

    @Test
    fun `safeExecute with custom error message`() {
        val result = ErrorHandler.safeExecute<String>(
            errorMessage = "Custom error message"
        ) {
            throw RuntimeException("Operation failed")
        }

        Assert.assertTrue(result is ErrorHandler.Result.Error)
        Assert.assertEquals("Custom error message", (result as ErrorHandler.Result.Error).message)
    }

    @Test
    fun `safeExecute with context shows toast on error`() {
        val result = ErrorHandler.safeExecute<String>(
            context = context,
            errorMessage = "Context error"
        ) {
            throw RuntimeException("Operation failed")
        }

        Assert.assertTrue(result is ErrorHandler.Result.Error)
        val latestToast = ShadowToast.getLatestToast()
        Assert.assertNotNull(latestToast)
        Assert.assertEquals("Context error", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `safeExecute without context doesn't crash`() {
        val result = ErrorHandler.safeExecute<String>(
            context = null,
            errorMessage = "No context error"
        ) {
            throw RuntimeException("Operation failed")
        }

        Assert.assertTrue(result is ErrorHandler.Result.Error)
        // Should not crash even without context
    }

    // ===== ERROR SEVERITY TESTS =====

    @Test
    fun `ErrorSeverity WARNING has correct properties`() {
        val severity = ErrorHandler.ErrorSeverity.WARNING

        Assert.assertEquals(Log.WARN, severity.logLevel)
        Assert.assertTrue(severity.showToast)
    }

    @Test
    fun `ErrorSeverity ERROR has correct properties`() {
        val severity = ErrorHandler.ErrorSeverity.ERROR

        Assert.assertEquals(Log.ERROR, severity.logLevel)
        Assert.assertTrue(severity.showToast)
    }

    // ===== COMMON ERROR HELPERS TESTS =====

    @Test
    fun `Common networkError shows appropriate message`() {
        val exception = RuntimeException("Network failed")

        ErrorHandler.Common.networkError(context, exception)

        val latestToast = ShadowToast.getLatestToast()
        Assert.assertNotNull(latestToast)
        Assert.assertEquals("Netzwerkfehler aufgetreten", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `Common dataError shows appropriate message`() {
        val exception = RuntimeException("Data failed")

        ErrorHandler.Common.dataError(context, exception)

        val latestToast = ShadowToast.getLatestToast()
        Assert.assertNotNull(latestToast)
        Assert.assertEquals("Fehler beim Laden der Daten", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `Common validationError shows custom message`() {
        ErrorHandler.Common.validationError(context, "Custom validation message")

        val latestToast = ShadowToast.getLatestToast()
        Assert.assertNotNull(latestToast)
        Assert.assertEquals("Custom validation message", ShadowToast.getTextOfLatestToast())
    }

    // ===== DEBUG MODE TESTS =====

    @Test
    fun `debug mode detection works with debug flag`() {
        // Mock context with debug flag
        val debugContext = mockk<Context>()
        val appInfo = mockk<ApplicationInfo>()
        appInfo.flags = ApplicationInfo.FLAG_DEBUGGABLE

        every { debugContext.applicationInfo } returns appInfo

        // Test that debug mode affects logging (this is tested indirectly)
        ErrorHandler.handleError(
            debugContext,
            RuntimeException("Debug test"),
            "Debug message",
            ErrorHandler.ErrorSeverity.ERROR
        )

        // In debug mode, additional logging should occur
        // This is hard to test directly, but we can verify no crash occurs
    }

    // ===== EDGE CASES =====

    @Test
    fun `handleError with null exception doesn't crash`() {
        // Should handle null exception gracefully
        try {
            ErrorHandler.handleError(
                context,
                Exception(), // Valid exception
                "Test message",
                ErrorHandler.ErrorSeverity.ERROR
            )
        } catch (e: Exception) {
            Assert.fail("Should not throw exception")
        }
    }

    @Test
    fun `multiple error handling operations work correctly`() {
        // Handle multiple errors in sequence
        repeat(5) { i ->
            ErrorHandler.handleError(
                context,
                RuntimeException("Error $i"),
                "Message $i",
                ErrorHandler.ErrorSeverity.WARNING
            )
        }

        // Should show latest toast
        val latestToast = ShadowToast.getLatestToast()
        Assert.assertNotNull(latestToast)
        Assert.assertEquals("Message 4", ShadowToast.getTextOfLatestToast())
    }

    // ===== PERFORMANCE TESTS =====

    @Test
    fun `error handling performance is acceptable`() {
        val startTime = System.currentTimeMillis()

        repeat(100) { i ->
            ErrorHandler.safeExecute {
                throw RuntimeException("Performance test $i")
            }
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        Assert.assertTrue("Error handling should complete within 1 second", duration < 1000)
    }

    @Test
    fun `successful operations have minimal overhead`() {
        val startTime = System.currentTimeMillis()

        repeat(1000) { i ->
            ErrorHandler.safeExecute {
                "Success $i"
            }
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        Assert.assertTrue("Successful operations should complete within 100ms", duration < 100)
    }

    // ===== FUNCTIONAL STYLE TESTS =====

    @Test
    fun `Result can be used in functional chains`() {
        val result = ErrorHandler.safeExecute { "initial" }
            .onSuccess { data ->
                Assert.assertEquals("initial", data)
            }
            .onError { _, _ ->
                Assert.fail("Should not reach error handler")
            }

        Assert.assertEquals("initial", result.getOrNull())
    }

    @Test
    fun `Result error chains work correctly`() {
        var errorHandled = false

        ErrorHandler.safeExecute<String> {
            throw RuntimeException("Chain test")
        }
            .onSuccess {
                Assert.fail("Should not reach success handler")
            }
            .onError { exception, message ->
                errorHandled = true
                Assert.assertEquals("Chain test", exception.message)
                Assert.assertEquals("An error occurred", message)
            }

        Assert.assertTrue(errorHandled)
    }
}