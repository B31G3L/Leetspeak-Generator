package com.beigel.leetSpeak_Generator.ui.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.beigel.leetSpeak_Generator.R

/**
 * Überarbeitete About Dialog mit modernem Design
 * FIXED: Alle hardcodierten deutschen Texte durch String-Ressourcen ersetzt
 */
@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                // ✅ Neuer Header mit Gradient
                HeaderSection()

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ App Info kompakter
                AppInfoSection()

                Spacer(modifier = Modifier.height(20.dp))

                // ✅ Neue Stats Section
                StatsSection()

                Spacer(modifier = Modifier.height(20.dp))

                // ✅ Features als Chips
                FeaturesChipSection()

                Spacer(modifier = Modifier.height(20.dp))

                // ✅ Action Buttons
                ActionButtons(
                    onDismiss = onDismiss,
                    uriHandler = uriHandler
                )
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    val infiniteTransition = rememberInfiniteTransition(label = "header_animation")

    // Gradient Rotation Animation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_rotation"
    )

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
            modifier = Modifier.size(80.dp),
            color = MaterialTheme.colorScheme.surface,
            shape = CircleShape,
            shadowElevation = 4.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_custom_mode),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AppInfoSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // FIXED: String resource
        Text(
            text = stringResource(R.string.about_dialog_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            // FIXED: String resource
            Text(
                text = stringResource(R.string.about_dialog_version),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // FIXED: String resource
        Text(
            text = stringResource(R.string.about_dialog_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatsSection() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.Translate,
                value = "3",
                // FIXED: String resource
                label = stringResource(R.string.about_stats_modes)
            )
            StatItem(
                icon = Icons.Default.Speed,
                value = "∞",
                // FIXED: String resource
                label = stringResource(R.string.about_stats_custom)
            )
            StatItem(
                icon = Icons.Default.Star,
                value = "100%",
                // FIXED: String resource
                label = stringResource(R.string.about_stats_free)
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeaturesChipSection() {
    Column {
        // FIXED: String resource
        Text(
            text = stringResource(R.string.whats_new_features),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val features = listOf(
            // FIXED: String resources
            stringResource(R.string.about_feature_simple) to Icons.Default.TextFields,
            stringResource(R.string.about_feature_extended) to Icons.Default.ExtensionOff,
            stringResource(R.string.about_feature_custom_leets) to Icons.Default.Tune,
            stringResource(R.string.about_feature_favorites) to Icons.Default.Favorite,
            stringResource(R.string.about_feature_material3) to Icons.Default.Palette,
            stringResource(R.string.about_feature_offline) to Icons.Default.CloudOff
        )

        // Features als Chips in 2 Spalten
        features.chunked(2).forEach { rowFeatures ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowFeatures.forEach { (feature, icon) ->
                    FeatureChip(
                        text = feature,
                        icon = icon,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Leerer Platz wenn ungerade Anzahl
                if (rowFeatures.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FeatureChip(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onDismiss: () -> Unit,
    uriHandler: UriHandler
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            // FIXED: String resource
            Text(stringResource(R.string.about_dialog_close))
        }

        Button(
            onClick = {
                uriHandler.openUri("https://play.google.com/store/apps/details?id=com.beigel.leetSpeak_Generator")
                onDismiss()
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            // FIXED: String resource
            Text(stringResource(R.string.about_dialog_rate))
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Copyright - FIXED: String resource
    Text(
        text = stringResource(R.string.about_dialog_copyright),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}