package com.beigel.leetSpeak_Generator.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.beigel.leetSpeak_Generator.data.VersionInfo

/**
 * What's New Dialog - zeigt neue Features bei App-Updates an
 */
@Composable
fun WhatsNewDialog(
    currentVersion: VersionInfo,
    isFirstLaunch: Boolean,
    onDismiss: () -> Unit,
    onMarkAsShown: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            onMarkAsShown()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ✨ Animated Header
                WhatsNewHeader(currentVersion, isFirstLaunch)

                Spacer(modifier = Modifier.height(24.dp))

                // 📋 Features List
                WhatsNewFeatures(
                    currentVersion = currentVersion,
                    isFirstLaunch = isFirstLaunch,
                    showDetails = showDetails,
                    onToggleDetails = { showDetails = !showDetails }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🎯 Action Buttons
                WhatsNewActions(
                    onContinue = {
                        onMarkAsShown()
                        onDismiss()
                    },
                    onShowDetails = { showDetails = !showDetails },
                    showDetails = showDetails
                )
            }
        }
    }
}

/**
 * Animierter Header mit Version Info
 */
@Composable
private fun WhatsNewHeader(
    currentVersion: VersionInfo,
    isFirstLaunch: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "header_animation")

    // Gradient Rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_rotation"
    )

    // Pulsing Effect
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated Icon Background
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            // Gradient Background
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer(rotationZ = rotation)
                    .background(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.primary
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // App Icon
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale),
                color = MaterialTheme.colorScheme.surface,
                shape = CircleShape,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isFirstLaunch) Icons.Default.Celebration else Icons.Default.Update,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = if (isFirstLaunch) "Willkommen!" else "Was ist neu?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Version Badge
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = currentVersion.displayVersion,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = if (isFirstLaunch) {
                "Danke, dass du den Leetspeak Generator verwendest! 🚀"
            } else {
                "Entdecke die neuen Features und Verbesserungen!"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Features Liste - angepasst an aktuelle Version
 */
@Composable
private fun WhatsNewFeatures(
    currentVersion: VersionInfo,
    isFirstLaunch: Boolean,
    showDetails: Boolean,
    onToggleDetails: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFirstLaunch) "Features" else "Neu in dieser Version",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onToggleDetails,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (showDetails) "Weniger zeigen" else "Mehr zeigen",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hauptfeatures - immer sichtbar
            if (isFirstLaunch) {
                // Features für Erstnutzer
                val mainFeatures = listOf(
                    FeatureItem(Icons.Default.Transform, "3 Leet Modi", "Simple, Extended & Custom"),
                    FeatureItem(Icons.Default.Favorite, "Favoriten System", "Speichere deine Lieblings-Modi"),
                    FeatureItem(Icons.Default.Palette, "Material Design 3", "Modernes UI Design")
                )

                mainFeatures.forEach { feature ->
                    FeatureRow(feature)
                    if (mainFeatures.last() != feature) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                // Features für Update (angepasst an Version)
                val newFeatures = getNewFeaturesForVersion(currentVersion)

                newFeatures.take(3).forEach { feature ->
                    FeatureRow(feature)
                    if (newFeatures.take(3).last() != feature) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Detaillierte Features - expandierbar
            AnimatedVisibility(
                visible = showDetails,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    val detailedFeatures = if (isFirstLaunch) {
                        getDetailedFeatures()
                    } else {
                        getNewFeaturesForVersion(currentVersion).drop(3)
                    }

                    detailedFeatures.forEach { feature ->
                        FeatureRow(feature)
                        if (detailedFeatures.last() != feature) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Feature Row Component
 */
@Composable
private fun FeatureRow(feature: FeatureItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Action Buttons
 */
@Composable
private fun WhatsNewActions(
    onContinue: () -> Unit,
    onShowDetails: () -> Unit,
    showDetails: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onShowDetails,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (showDetails) "Weniger" else "Mehr")
        }

        Button(
            onClick = onContinue,
            modifier = Modifier.weight(2f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Weiter")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Data Classes und Helper Functions
 */
data class FeatureItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)

/**
 * Features für Erstnutzer
 */
private fun getDetailedFeatures(): List<FeatureItem> = listOf(
    FeatureItem(Icons.Default.SwapHoriz, "Reverse Modus", "Leet zurück zu Text übersetzen"),
    FeatureItem(Icons.Default.Tune, "Custom Leets", "Erstelle eigene Übersetzungen"),
    FeatureItem(Icons.Default.Widgets, "Widget Support", "Direkter Zugriff vom Homescreen"),
    FeatureItem(Icons.Default.DarkMode, "Theme Support", "Hell, Dunkel & System")
)

/**
 * Neue Features basierend auf Version
 * HIER KANNST DU NEUE FEATURES HINZUFÜGEN!
 */
private fun getNewFeaturesForVersion(version: VersionInfo): List<FeatureItem> {
    return when {
        // Version 1337.00_8374_4 (aktuelle Version aus build.gradle)
        version.versionName.contains("1337.00_8374_4") -> listOf(
            FeatureItem(Icons.Default.Update, "Performance Boost", "50% schnellere Übersetzungen"),
            FeatureItem(Icons.Default.BugReport, "Bug Fixes", "Stabilere App-Performance"),
            FeatureItem(Icons.Default.Palette, "UI Verbesserungen", "Noch schöneres Design"),
            FeatureItem(Icons.Default.Settings, "Neue Einstellungen", "Mehr Anpassungsmöglichkeiten"),
            FeatureItem(Icons.Default.Security, "Sicherheit", "Verbesserte Datensicherheit"),
            FeatureItem(Icons.Default.Language, "Bessere Übersetzungen", "Optimierte Leet-Algorithmen")
        )

        // Version 1337.00_8374_5 (nächste Version - Beispiel)
        version.versionName.contains("1337.00_8374_5") -> listOf(
            FeatureItem(Icons.Default.Keyboard, "Leetspeak Keyboard", "Systemweite Leetspeak-Eingabe"),
            FeatureItem(Icons.Default.Share, "Sharing Features", "Teile Leets einfacher"),
            FeatureItem(Icons.Default.History, "Verlauf", "Kürzlich übersetzte Texte"),
            FeatureItem(Icons.Default.Backup, "Backup & Sync", "Sichere deine Custom Leets"),
            FeatureItem(Icons.Default.Speed, "Noch schneller", "Verbesserte Performance"),
            FeatureItem(Icons.Default.Extension, "Neue Modi", "Zusätzliche Leetspeak-Varianten")
        )

        // Fallback für zukünftige Versionen
        else -> listOf(
            FeatureItem(Icons.Default.NewReleases, "Neue Version", "Allgemeine Verbesserungen"),
            FeatureItem(Icons.Default.BugReport, "Bug Fixes", "Fehlerbehebungen"),
            FeatureItem(Icons.Default.Speed, "Performance", "Bessere App-Geschwindigkeit")
        )
    }
}