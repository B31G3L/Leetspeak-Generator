package com.beigel.leetSpeak_Generator.ui.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.R
import kotlinx.coroutines.launch

/**
 * Onboarding (Redesign v4b + Update-Hinweise): 3-Schritt-Intro, Vollbild, ohne Bottom-Nav.
 * Seite 1: Über die App (zusammengefasste Funktionsübersicht).
 * Seite 2: Neu in diesem Update (Tastatur, Verlauf, Teilen, Widget) + Tastatur-Aktivierung.
 * Seite 3: Über mich (Solo-Entwickler) + Bewerten / Ko-Fi / Discord.
 * 96x96dp abgerundete Icon-Kachel (28dp Radius, primaryContainer), Step-Dots
 * (aktiv = 20dp Pill, inaktiv = 6dp Kreis), volle-Breite CTA-Button (50dp, 16dp Radius).
 *
 * Wird nicht mehr nur bei der ersten Installation gezeigt, sondern auch nach
 * Updates mit neuen Features erneut (siehe OnboardingPreferences.CURRENT_ONBOARDING_VERSION).
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    val pageCount = 3
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val isLastPage = pagerState.currentPage == pageCount - 1

    val playStoreUrl = stringResource(R.string.url_play_store)
    val kofiUrl = stringResource(R.string.url_kofi)
    val discordUrl = stringResource(R.string.discord_invite_url)
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Skip-Button oben rechts, ausgeblendet auf der letzten Seite
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                AnimatedVisibility(visible = !isLastPage) {
                    TextButton(onClick = onComplete) {
                        Text(
                            text = stringResource(R.string.onboarding_skip),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pageIndex ->
                val isActive = pagerState.currentPage == pageIndex
                when (pageIndex) {
                    0 -> OnboardingAppPage(isActive = isActive)
                    1 -> OnboardingWhatsNewPage(
                        isActive = isActive,
                        onEnableKeyboardClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    )
                    else -> OnboardingAboutMePage(
                        isActive = isActive,
                        onRateClick = { uriHandler.openUri(playStoreUrl) },
                        onKofiClick = { uriHandler.openUri(kofiUrl) },
                        onDiscordClick = { uriHandler.openUri(discordUrl) }
                    )
                }
            }

            // Bottom: Dots + CTA-Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pageCount) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 20.dp else 6.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label = "dot_width_$index"
                        )
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline
                                )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isLastPage) {
                            onComplete()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(
                            if (isLastPage) R.string.onboarding_start
                            else R.string.onboarding_next
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingAppPage(isActive: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 96x96dp Icon-Kachel, 28dp Radius, primaryContainer-Hintergrund
        Surface(
            modifier = Modifier.size(96.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Transform,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(
                visible = isActive,
                enter = fadeIn(tween(400)) + slideInVertically(
                    animationSpec = tween(400),
                    initialOffsetY = { it / 4 }
                )
            ) {
                Text(
                    text = stringResource(R.string.onboarding_app_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(
                visible = isActive,
                enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(
                    animationSpec = tween(500, delayMillis = 100),
                    initialOffsetY = { it / 4 }
                )
            ) {
                Text(
                    text = stringResource(R.string.onboarding_app_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OnboardingWhatsNewPage(
    isActive: Boolean,
    onEnableKeyboardClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(
                visible = isActive,
                enter = fadeIn(tween(400)) + slideInVertically(
                    animationSpec = tween(400),
                    initialOffsetY = { it / 4 }
                )
            ) {
                Text(
                    text = stringResource(R.string.onboarding_whatsnew_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(
                visible = isActive,
                enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(
                    animationSpec = tween(500, delayMillis = 100),
                    initialOffsetY = { it / 4 }
                )
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    OnboardingFeatureRow(
                        icon = Icons.Default.Keyboard,
                        title = stringResource(R.string.onboarding_whatsnew_keyboard_title),
                        description = stringResource(R.string.onboarding_whatsnew_keyboard_desc)
                    )
                    OnboardingFeatureRow(
                        icon = Icons.Default.History,
                        title = stringResource(R.string.onboarding_whatsnew_history_title),
                        description = stringResource(R.string.onboarding_whatsnew_history_desc)
                    )
                    OnboardingFeatureRow(
                        icon = Icons.Default.Share,
                        title = stringResource(R.string.onboarding_whatsnew_share_title),
                        description = stringResource(R.string.onboarding_whatsnew_share_desc)
                    )
                    OnboardingFeatureRow(
                        icon = Icons.Default.Widgets,
                        title = stringResource(R.string.onboarding_whatsnew_widget_title),
                        description = stringResource(R.string.onboarding_whatsnew_widget_desc)
                    )
                }
            }

            AnimatedVisibility(
                visible = isActive,
                enter = fadeIn(tween(600, delayMillis = 150)) + slideInVertically(
                    animationSpec = tween(600, delayMillis = 150),
                    initialOffsetY = { it / 4 }
                )
            ) {
                OutlinedButton(
                    onClick = onEnableKeyboardClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_keyboard_enable),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier.size(36.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
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
private fun OnboardingAboutMePage(
    isActive: Boolean,
    onRateClick: () -> Unit,
    onKofiClick: () -> Unit,
    onDiscordClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(
                visible = isActive,
                enter = fadeIn(tween(400)) + slideInVertically(
                    animationSpec = tween(400),
                    initialOffsetY = { it / 4 }
                )
            ) {
                Text(
                    text = stringResource(R.string.onboarding_about_me_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(
                visible = isActive,
                enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(
                    animationSpec = tween(500, delayMillis = 100),
                    initialOffsetY = { it / 4 }
                )
            ) {
                Text(
                    text = stringResource(R.string.onboarding_about_me_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = isActive,
                enter = fadeIn(tween(600, delayMillis = 150)) + slideInVertically(
                    animationSpec = tween(600, delayMillis = 150),
                    initialOffsetY = { it / 4 }
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.onboarding_support_hint),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OnboardingLinkButton(
                            icon = Icons.Default.Star,
                            label = stringResource(R.string.about_dialog_rate),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            onClick = onRateClick
                        )
                        OnboardingLinkButton(
                            icon = Icons.Default.LocalCafe,
                            label = stringResource(R.string.kofi_support_short),
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            onClick = onKofiClick
                        )
                        OnboardingLinkButton(
                            icon = Icons.Default.Forum,
                            label = stringResource(R.string.discord_support_short),
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                            onClick = onDiscordClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingLinkButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}