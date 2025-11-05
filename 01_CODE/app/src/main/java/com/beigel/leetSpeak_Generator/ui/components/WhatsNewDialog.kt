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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.VersionInfo

/**
 * What's New Dialog - zeigt neue Features bei App-Updates an
 * CLEANED: Keyboard-Features entfernt
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
                    .graphicsLayer(rotationZ = rotation * 0.5f)
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
            text = stringResource(if (isFirstLaunch) R.string.whats_new_welcome else R.string.whats_new_title),
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
            text = stringResource(
                if (isFirstLaunch) {
                    R.string.whats_new_welcome_subtitle
                } else {
                    R.string.whats_new_subtitle
                }
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Features Liste - OHNE Keyboard Features
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
                    text = stringResource(if (isFirstLaunch) R.string.whats_new_features else R.string.whats_new_version_features),
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
                        contentDescription = stringResource(if (showDetails) R.string.whats_new_show_less else R.string.whats_new_show_more),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hauptfeatures - immer sichtbar (OHNE Keyboard)
            if (isFirstLaunch) {
                val mainFeatures = listOf(
                    FeatureItem(Icons.Default.Transform, stringResource(R.string.feature_transform_title), stringResource(R.string.feature_transform_desc)),
                    FeatureItem(Icons.Default.Favorite, stringResource(R.string.feature_favorites_title), stringResource(R.string.feature_favorites_desc)),
                    FeatureItem(Icons.Default.Palette, stringResource(R.string.feature_material_title), stringResource(R.string.feature_material_desc))
                )

                mainFeatures.forEach { feature ->
                    FeatureRow(feature)
                    if (mainFeatures.last() != feature) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                // Features für Update (angepasst an Version) - OHNE Keyboard
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
            Text(stringResource(if (showDetails) R.string.whats_new_show_less else R.string.whats_new_show_more))
        }

        Button(
            onClick = onContinue,
            modifier = Modifier.weight(2f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.whats_new_continue))
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
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
 * Features für Erstnutzer - OHNE Keyboard
 */
@Composable
private fun getDetailedFeatures(): List<FeatureItem> = listOf(
    FeatureItem(Icons.Default.SwapHoriz, stringResource(R.string.feature_reverse_title), stringResource(R.string.feature_reverse_desc)),
    FeatureItem(Icons.Default.Tune, stringResource(R.string.feature_custom_title), stringResource(R.string.feature_custom_desc)),
    FeatureItem(Icons.Default.Widgets, stringResource(R.string.feature_widget_title), stringResource(R.string.feature_widget_desc)),
    FeatureItem(Icons.Default.DarkMode, stringResource(R.string.feature_theme_title), stringResource(R.string.feature_theme_desc))
)

/**
 * Neue Features basierend auf Version - OHNE Keyboard
 * HIER KANNST DU NEUE FEATURES HINZUFÜGEN!
 */
@Composable
private fun getNewFeaturesForVersion(version: VersionInfo): List<FeatureItem> {
    return when {
        // Version 1337.00_8374_4 (aktuelle Version)
        version.versionName.contains("1337.00_8374_4") -> listOf(
            FeatureItem(Icons.Default.Update, stringResource(R.string.feature_performance_title), stringResource(R.string.feature_performance_desc)),
            FeatureItem(Icons.Default.BugReport, stringResource(R.string.feature_bugfixes_title), stringResource(R.string.feature_bugfixes_desc)),
            FeatureItem(Icons.Default.Palette, stringResource(R.string.feature_ui_improvements_title), stringResource(R.string.feature_ui_improvements_desc)),
            FeatureItem(Icons.Default.Settings, stringResource(R.string.feature_new_settings_title), stringResource(R.string.feature_new_settings_desc)),
            FeatureItem(Icons.Default.Security, stringResource(R.string.feature_security_title), stringResource(R.string.feature_security_desc)),
            FeatureItem(Icons.Default.Language, stringResource(R.string.feature_translations_title), stringResource(R.string.feature_translations_desc))
        )

        // Version 1337.00_8374_5 (nächste Version - Beispiel) - OHNE Keyboard
        version.versionName.contains("1337.00_8374_5") -> listOf(
            FeatureItem(Icons.Default.Share, stringResource(R.string.whats_new_feature_sharing), stringResource(R.string.whats_new_feature_sharing_desc)),
            FeatureItem(Icons.Default.History, stringResource(R.string.whats_new_feature_history), stringResource(R.string.whats_new_feature_history_desc)),
            FeatureItem(Icons.Default.Backup, stringResource(R.string.whats_new_feature_backup), stringResource(R.string.whats_new_feature_backup_desc)),
            FeatureItem(Icons.Default.Speed, stringResource(R.string.whats_new_feature_faster), stringResource(R.string.whats_new_feature_faster_desc)),
            FeatureItem(Icons.Default.Extension, stringResource(R.string.whats_new_feature_new_modes), stringResource(R.string.whats_new_feature_new_modes_desc))
        )

        // Fallback für zukünftige Versionen
        else -> listOf(
            FeatureItem(Icons.Default.NewReleases, stringResource(R.string.whats_new_fallback_title), stringResource(R.string.whats_new_fallback_desc)),
            FeatureItem(Icons.Default.BugReport, stringResource(R.string.feature_bugfixes_title), stringResource(R.string.feature_bugfixes_desc)),
            FeatureItem(Icons.Default.Speed, stringResource(R.string.whats_new_performance), stringResource(R.string.whats_new_performance_desc))
        )
    }
}