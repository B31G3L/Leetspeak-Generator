package com.beigel.leetSpeak_Generator.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.ui.ComposeMainActivity

/**
 * Kein Übersetzungs-Widget mehr — nur ein schneller Launch-Button, der die App
 * öffnet. Übersetzen passiert seit der Leetspeak-Tastatur (IME) direkt in
 * jedem Textfeld, dafür braucht es kein separates Widget mehr.
 */
class LeetLaunchWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Column(
                modifier = androidx.glance.GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(R.color.widget_background))
                    .padding(12.dp)
                    .clickable(actionStartActivity<ComposeMainActivity>()),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.mipmap.ic_launcher_round),
                    contentDescription = null,
                    modifier = androidx.glance.GlanceModifier.size(40.dp)
                )
                androidx.glance.layout.Spacer(modifier = androidx.glance.GlanceModifier.size(6.dp))
                Text(
                    text = context.getString(R.string.widget_open_app),
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        color = ColorProvider(R.color.widget_on_background)
                    )
                )
            }
        }
    }
}
