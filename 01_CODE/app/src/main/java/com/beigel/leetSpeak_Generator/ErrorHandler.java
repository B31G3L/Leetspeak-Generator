package com.beigel.leetSpeak_Generator;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorHandler {
    private static final String TAG = "LeetGenerator";

    public static void handleError(Context context, Exception e, String userMessage) {
        handleError(context, e, userMessage, ErrorSeverity.ERROR);
    }

    public static void handleError(Context context, Exception e, String userMessage, ErrorSeverity severity) {
        // Logging basierend auf Schweregrad
        switch (severity) {
            case WARNING:
                Log.w(TAG, userMessage + ": " + e.getMessage(), e);
                break;
            case ERROR:
                Log.e(TAG, userMessage + ": " + e.getMessage(), e);
                break;
            case CRITICAL:
                Log.wtf(TAG, userMessage + ": " + e.getMessage(), e);
                break;
        }

        // Detaillierte Logs nur im Debug-Modus
        // BuildConfig ist automatisch verfügbar, aber wir verwenden eine sicherere Methode
        if (isDebugMode(context)) {
            Log.d(TAG, "Full stack trace:", e);
        }

        // Benutzerfreundliche Nachricht anzeigen
        showUserMessage(context, userMessage, severity, e);
    }

    // Sichere Methode zur Debug-Erkennung
    private static boolean isDebugMode(Context context) {
        try {
            // Verwende den Context um zu prüfen, ob wir im Debug-Modus sind
            return (context.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            // Fallback: Assume production mode if we can't determine
            return false;
        }
    }

    private static void showUserMessage(Context context, String message, ErrorSeverity severity, Exception e) {
        switch (severity) {
            case WARNING:
                showSnackbar(context, message, Snackbar.LENGTH_SHORT);
                break;
            case ERROR:
                showDialog(context, "Fehler", message, e);
                break;
            case CRITICAL:
                showDialog(context, "Schwerwiegender Fehler", message, e);
                break;
        }
    }

    public static void showSnackbar(Context context, String message, int duration) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            View rootView = activity.findViewById(android.R.id.content);
            if (rootView != null) {
                Snackbar.make(rootView, message, duration).show();
                return;
            }
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private static void showDialog(Context context, String title, String message, Exception e) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null);

        // Debug-Details nur im Debug-Modus anzeigen
        if (isDebugMode(context) && e != null) {
            builder.setNeutralButton("Details", (dialog, which) -> {
                showTechnicalDetails(context, e);
            });
        }

        builder.show();
    }

    private static void showTechnicalDetails(Context context, Exception e) {
        String technicalInfo = "Exception: " + e.getClass().getSimpleName() + "\n" +
                "Message: " + e.getMessage() + "\n\n" +
                "Stack Trace:\n" + getStackTrace(e);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Technical Details")
                .setMessage(technicalInfo)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public enum ErrorSeverity {
        WARNING, ERROR, CRITICAL
    }
}