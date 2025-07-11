package com.beigel.leetSpeak_Generator

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.PrintWriter
import java.io.StringWriter

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
        val showDialog: Boolean,
        val showSnackbar: Boolean
    ) {
        WARNING(Log.WARN, showDialog = false, showSnackbar = true),
        ERROR(Log.ERROR, showDialog = true, showSnackbar = false),
        CRITICAL(Log.ASSERT, showDialog = true, showSnackbar = false)
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

        // Show appropriate user feedback
        showUserFeedback(context, userMessage, severity, exception)
    }

    /**
     * Handles errors with Result wrapper
     */
    fun <T> handleResult(
        context: Context,
        result: Result<T>,
        onSuccess: (T) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        result
            .onSuccess { data -> onSuccess(data) }
            .onError { exception, message ->
                handleError(context, Exception(exception), message)
                onError(message)
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
     * Shows a snackbar with optional action
     */
    fun showSnackbar(
        context: Context,
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,
        actionText: String? = null,
        action: (() -> Unit)? = null
    ) {
        val view = when (context) {
            is Activity -> context.findViewById<View>(android.R.id.content)
            else -> null
        }

        if (view != null) {
            val snackbar = Snackbar.make(view, message, duration)

            if (actionText != null && action != null) {
                snackbar.setAction(actionText) { action() }
            }

            snackbar.show()
        } else {
            // Fallback to toast
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shows user feedback based on error severity
     */
    private fun showUserFeedback(
        context: Context,
        message: String,
        severity: ErrorSeverity,
        exception: Exception
    ) {
        when (severity) {
            ErrorSeverity.WARNING -> {
                if (severity.showSnackbar) {
                    showSnackbar(context, message, Snackbar.LENGTH_SHORT)
                }
            }
            ErrorSeverity.ERROR, ErrorSeverity.CRITICAL -> {
                if (severity.showDialog) {
                    showErrorDialog(context, getSeverityTitle(severity), message, exception)
                }
            }
        }
    }

    /**
     * Shows error dialog with technical details option
     */
    private fun showErrorDialog(
        context: Context,
        title: String,
        message: String,
        exception: Exception
    ) {
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)

        // Add technical details button in debug mode
        if (context.isDebugMode()) {
            builder.setNeutralButton("Details") { _, _ ->
                showTechnicalDetails(context, exception)
            }
        }

        builder.show()
    }

    /**
     * Shows technical error details in debug mode
     */
    private fun showTechnicalDetails(context: Context, exception: Exception) {
        val technicalInfo = buildString {
            appendLine("Exception: ${exception.javaClass.simpleName}")
            appendLine("Message: ${exception.message}")
            appendLine()
            appendLine("Stack Trace:")
            append(exception.getStackTraceString())
        }

        MaterialAlertDialogBuilder(context)
            .setTitle("Technical Details")
            .setMessage(technicalInfo)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    /**
     * Gets title for error severity
     */
    private fun getSeverityTitle(severity: ErrorSeverity): String = when (severity) {
        ErrorSeverity.WARNING -> "Warnung"
        ErrorSeverity.ERROR -> "Fehler"
        ErrorSeverity.CRITICAL -> "Schwerwiegender Fehler"
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
     * Extension function to get stack trace as string
     */
    private fun Exception.getStackTraceString(): String {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        printStackTrace(printWriter)
        return stringWriter.toString()
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
            showSnackbar(context, message, Snackbar.LENGTH_LONG)
        }
    }
}