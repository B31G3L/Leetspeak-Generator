package com.beigel.leetSpeak_Generator.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.beigel.leetSpeak_Generator.R

/**
 * About Dialog mit modernem Material3 Design
 */
@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }
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
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Icon mit Animation
                AppIconSection()

                Spacer(modifier = Modifier.height(16.dp))

                // App Info
                AppInfoSection()

                Spacer(modifier = Modifier.height(24.dp))

                // Features Section
                FeaturesSection()

                Spacer(modifier = Modifier.height(24.dp))

                // Details Toggle
                DetailsSection(
                    showDetails = showDetails,
                    onToggleDetails = { showDetails = !showDetails }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                ActionButtons(onDismiss = onDismiss)
            }
        }
    }
}

@Composable
private fun AppIconSection() {
    val infiniteTransition = rememberInfiniteTransition(label = "app_icon")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "icon_rotation"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Background Circle
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape
        ) {}

        // App Icon
        Icon(
            painter = painterResource(R.drawable.ic_custom_mode),
            contentDescription = "App Icon",
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer(rotationZ = rotation),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun AppInfoSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Leetspeak Generator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = "Version 2.0.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Verwandle normalen Text in coolen Leetspeak! 🚀",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FeaturesSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Features",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            val features = listOf(
                "Simple Leet" to "Grundlegende Leetspeak-Übersetzung",
                "Extended Leet" to "Erweiterte Zeichen und Symbole",
                "Custom Leet" to "Erstelle deine eigenen Übersetzungen",
                "Favoriten" to "Speichere deine bevorzugten Modi",
                "Material Design" to "Moderne und intuitive Benutzeroberfläche"
            )

            features.forEach { (title, description) ->
                FeatureItem(
                    title = title,
                    description = description
                )
                if (features.last() != (title to description)) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailsSection(
    showDetails: Boolean,
    onToggleDetails: () -> Unit
) {
    Column {
        TextButton(
            onClick = onToggleDetails,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (showDetails) "Weniger anzeigen" else "Mehr Details",
                style = MaterialTheme.typography.labelLarge
            )
        }

        AnimatedVisibility(
            visible = showDetails,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    DetailItem(
                        icon = Icons.Default.Build,
                        title = "Entwickelt mit",
                        value = "Kotlin • Jetpack Compose • Material3"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailItem(
                        icon = Icons.Default.Palette,
                        title = "Design",
                        value = "Material Design 3 Guidelines"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailItem(
                        icon = Icons.Default.Code,
                        title = "Architecture",
                        value = "MVVM • Hilt DI • StateFlow"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailItem(
                        icon = Icons.Default.Copyright,
                        title = "Copyright",
                        value = "© B31G3L 2025"
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f)
        ) {
            Text("Schließen")
        }

        Button(
            onClick = {
                // Hier könnte ein Link zu GitHub, Play Store, etc.
                onDismiss()
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Bewerten")
        }
    }
}