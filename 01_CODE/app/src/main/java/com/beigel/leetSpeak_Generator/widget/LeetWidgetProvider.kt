package com.beigel.leetSpeak_Generator.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LeetWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var leetRepository: LeetRepository

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_leet_translator)

        // Lade Favoriten-Leet
        CoroutineScope(Dispatchers.Main).launch {
            leetRepository.loadFavoriteLeet()
                .onSuccess { favoriteResult ->
                    val translationMode = when (favoriteResult) {
                        is LeetRepository.FavoriteLeetResult.Simple -> LeetTranslator.TranslationMode.SIMPLE
                        is LeetRepository.FavoriteLeetResult.Extended -> LeetTranslator.TranslationMode.EXTENDED
                        is LeetRepository.FavoriteLeetResult.Custom -> LeetTranslator.TranslationMode.CUSTOM
                    }

                    val customLeet = if (favoriteResult is LeetRepository.FavoriteLeetResult.Custom) {
                        favoriteResult.leet
                    } else null

                    // Setze Widget-Titel
                    val title = when (favoriteResult) {
                        is LeetRepository.FavoriteLeetResult.Simple -> "Simple Leet"
                        is LeetRepository.FavoriteLeetResult.Extended -> "Extended Leet"
                        is LeetRepository.FavoriteLeetResult.Custom -> favoriteResult.leet.name
                    }

                    views.setTextViewText(R.id.widget_title, title)

                    // Beispiel-Translation
                    val example = LeetTranslator.translate("Hello", translationMode, customLeet)
                    views.setTextViewText(R.id.widget_example, "Hello → $example")

                    // Update Widget
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
                .onFailure {
                    // Fallback zu Simple Leet
                    views.setTextViewText(R.id.widget_title, "Simple Leet")
                    val example = LeetTranslator.translate("Hello", LeetTranslator.TranslationMode.SIMPLE)
                    views.setTextViewText(R.id.widget_example, "Hello → $example")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
        }

        // Click-Intent zur Hauptapp
        val intent = Intent(context, com.beigel.leetSpeak_Generator.ui.ComposeMainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            context, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
    }
}
