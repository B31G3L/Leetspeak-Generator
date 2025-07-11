package com.beigel.leetSpeak_Generator.utils

import android.content.Context
import android.util.Log
import android.widget.Toast

/**
 * Modern error handling utility with Kotlin features
 * Provides structured error management with improved type safety
 */
object ErrorHandler {

    private const val TAG = "LeetGenerator"

    /**
     * Error severity levels with associated behavior
     */
    enum class ErrorSeverity(
        val logLevel: Int,
        val showToast: Boolean
    ) {
        WARNING(Log.WARN, showToast = true),
        ERROR(Log.ERROR, showToast = true),
        CRITICAL(Log.ASSERT, showToast = true)
    }

    /**
     * Result wrapper for operations that can fail
     */
    sealed class Result<T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error<T>(val exception: Throwable, val message: String) : Result<T>()

        inline fun onSuccess(action: (T) -> Unit): Result<T> {
            if (this is Success) action(data)
            return this
        }

        inline fun onError(action: (Throwable, String) -> Unit): Result<T> {
            if (this is Error) action(exception, message)
            return this
        }

        fun getOrNull(): T? = when (this) {
            is Success -> data
            is Error -> null
        }

        fun getOrDefault(default: T): T = when (this) {
            is Success -> data
            is Error -> default
        }
    }

    /**
     * Handles errors with appropriate user feedback
     */
    fun handleError(
        context: Context,
        exception: Exception,
        userMessage: String,
        severity: ErrorSeverity = ErrorSeverity.ERROR
    ) {
        // Log error based on severity
        when (severity) {
            ErrorSeverity.WARNING -> Log.w(TAG, "$userMessage: ${exception.message}", exception)
            ErrorSeverity.ERROR -> Log.e(TAG, "$userMessage: ${exception.message}", exception)
            ErrorSeverity.CRITICAL -> Log.wtf(TAG, "$userMessage: ${exception.message}", exception)
        }

        // Detailed logs only in debug mode
        if (context.isDebugMode()) {
            Log.d(TAG, "Full stack trace:", exception)
        }

        // Show toast for user feedback
        if (severity.showToast) {
            Toast.makeText(context, userMessage, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Safe execution wrapper that catches exceptions
     */
    inline fun <T> safeExecute(
        context: Context? = null,
        errorMessage: String = "An error occurred",
        severity: ErrorSeverity = ErrorSeverity.ERROR,
        operation: () -> T
    ): Result<T> = try {
        Result.Success(operation())
    } catch (e: Exception) {
        context?.let { handleError(it, e, errorMessage, severity) }
        Result.Error(e, errorMessage)
    }

    /**
     * Extension function to check if app is in debug mode
     */
    private fun Context.isDebugMode(): Boolean = try {
        (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    } catch (e: Exception) {
        false
    }

    /**
     * Convenience functions for common error scenarios
     */
    object Common {

        fun networkError(context: Context, exception: Exception) {
            handleError(
                context,
                exception,
                "Netzwerkfehler aufgetreten",
                ErrorSeverity.WARNING
            )
        }

        fun dataError(context: Context, exception: Exception) {
            handleError(
                context,
                exception,
                "Fehler beim Laden der Daten",
                ErrorSeverity.ERROR
            )
        }

        fun criticalError(context: Context, exception: Exception) {
            handleError(
                context,
                exception,
                "Ein schwerwiegender Fehler ist aufgetreten",
                ErrorSeverity.CRITICAL
            )
        }

        fun validationError(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}