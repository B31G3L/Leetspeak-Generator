package com.beigel.leetSpeak_Generator.review

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * Manager für Google Play In-App Reviews
 *
 * Strategie:
 * - Zeigt Review-Dialog nach dem 3. App-Start
 * - Berücksichtigt Zeit seit letztem Review-Request
 * - Limitiert Häufigkeit der Review-Anfragen
 */
class InAppReviewManager(private val context: Context) {

    private val reviewManager: ReviewManager = ReviewManagerFactory.create(context)

    companion object {
        private const val TAG = "InAppReviewManager"

        // DataStore Keys
        private val REVIEW_COUNT_KEY = intPreferencesKey("review_prompt_count")
        private val LAST_REVIEW_TIME_KEY = longPreferencesKey("last_review_time")
        private val APP_STARTS_KEY = intPreferencesKey("app_starts_count")
        private val SUCCESSFUL_REVIEWS_KEY = intPreferencesKey("successful_reviews")

        // Schwellenwerte
        private const val MIN_APP_STARTS_BEFORE_REVIEW = 3
        private const val DAYS_BETWEEN_REVIEW_PROMPTS = 90L
        private const val MAX_REVIEW_PROMPTS = 3
    }

    private val Context.reviewDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "in_app_review_prefs"
    )

    /**
     * Prüft ob Review-Dialog angezeigt werden sollte
     */
    suspend fun shouldShowReview(): Boolean {
        val prefs = context.reviewDataStore.data.first()

        val reviewCount = prefs[REVIEW_COUNT_KEY] ?: 0
        val lastReviewTime = prefs[LAST_REVIEW_TIME_KEY] ?: 0L
        val appStarts = prefs[APP_STARTS_KEY] ?: 0

        // Bedingungen prüfen
        val hasEnoughStarts = appStarts >= MIN_APP_STARTS_BEFORE_REVIEW
        val enoughTimePassed = System.currentTimeMillis() - lastReviewTime >=
                TimeUnit.DAYS.toMillis(DAYS_BETWEEN_REVIEW_PROMPTS)
        val notTooManyPrompts = reviewCount < MAX_REVIEW_PROMPTS

        val shouldShow = hasEnoughStarts && enoughTimePassed && notTooManyPrompts

        Log.d(TAG, """
            Review Check:
            - App Starts: $appStarts (need $MIN_APP_STARTS_BEFORE_REVIEW)
            - Time passed: $enoughTimePassed
            - Prompts shown: $reviewCount (max $MAX_REVIEW_PROMPTS)
            - Should show: $shouldShow
        """.trimIndent())

        return shouldShow
    }

    /**
     * Erhöht den App-Start-Zähler
     */
    suspend fun incrementAppStartCount() {
        context.reviewDataStore.edit { prefs ->
            val current = prefs[APP_STARTS_KEY] ?: 0
            prefs[APP_STARTS_KEY] = current + 1

            Log.d(TAG, "App start count: ${current + 1}")
        }
    }

    /**
     * Zeigt den In-App Review Dialog
     *
     * @param activity Die aktuelle Activity
     * @return true wenn Review erfolgreich angezeigt wurde
     */
    suspend fun requestReview(activity: Activity): Boolean {
        return try {
            Log.d(TAG, "Requesting review info...")

            // Review Info anfordern
            val reviewInfo: ReviewInfo = reviewManager.requestReviewFlow().await()

            Log.d(TAG, "Launching review flow...")

            // Review Flow starten
            reviewManager.launchReviewFlow(activity, reviewInfo).await()

            // Erfolg tracken
            markReviewShown()

            Log.d(TAG, "✅ Review flow completed successfully")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error showing review", e)
            false
        }
    }

    /**
     * Markiert dass Review-Dialog angezeigt wurde
     */
    private suspend fun markReviewShown() {
        context.reviewDataStore.edit { prefs ->
            val reviewCount = (prefs[REVIEW_COUNT_KEY] ?: 0) + 1
            val successfulReviews = (prefs[SUCCESSFUL_REVIEWS_KEY] ?: 0) + 1

            prefs[REVIEW_COUNT_KEY] = reviewCount
            prefs[LAST_REVIEW_TIME_KEY] = System.currentTimeMillis()
            prefs[SUCCESSFUL_REVIEWS_KEY] = successfulReviews
            prefs[APP_STARTS_KEY] = 0 // Counter zurücksetzen

            Log.d(TAG, "Review marked as shown. Total prompts: $reviewCount")
        }
    }

    /**
     * Setzt alle Review-Statistiken zurück (für Testing)
     */
    suspend fun resetForTesting() {
        context.reviewDataStore.edit { prefs ->
            prefs.clear()
            Log.d(TAG, "Review preferences reset for testing")
        }
    }

    /**
     * Flow für Review-Statistiken (für UI-Anzeige in Settings)
     */
    fun getReviewStats(): Flow<ReviewStats> {
        return context.reviewDataStore.data.map { prefs ->
            ReviewStats(
                appStartCount = prefs[APP_STARTS_KEY] ?: 0,
                reviewPromptsShown = prefs[REVIEW_COUNT_KEY] ?: 0,
                lastReviewTime = prefs[LAST_REVIEW_TIME_KEY] ?: 0L,
                successfulReviews = prefs[SUCCESSFUL_REVIEWS_KEY] ?: 0
            )
        }
    }

    /**
     * Data class für Review-Statistiken
     */
    data class ReviewStats(
        val appStartCount: Int,
        val reviewPromptsShown: Int,
        val lastReviewTime: Long,
        val successfulReviews: Int
    ) {
        val startsUntilReview: Int
            get() = (MIN_APP_STARTS_BEFORE_REVIEW - appStartCount).coerceAtLeast(0)

        val canShowReview: Boolean
            get() = appStartCount >= MIN_APP_STARTS_BEFORE_REVIEW &&
                    reviewPromptsShown < MAX_REVIEW_PROMPTS
    }
}