package com.beigel.leetSpeak_Generator.ui.onboarding

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beigel.leetSpeak_Generator.R
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val titleRes: Int,
    val descriptionRes: Int,
    val accentColor: androidx.compose.ui.graphics.Color
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage(
            icon           = Icons.Default.Transform,
            titleRes       = R.string.onboarding_1_title,
            descriptionRes = R.string.onboarding_1_desc,
            accentColor    = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            icon           = Icons.Default.Tune,
            titleRes       = R.string.onboarding_2_title,
            descriptionRes = R.string.onboarding_2_desc,
            accentColor    = MaterialTheme.colorScheme.secondary
        ),
        OnboardingPage(
            icon           = Icons.Default.Favorite,
            titleRes       = R.string.onboarding_3_title,
            descriptionRes = R.string.onboarding_3_desc,
            accentColor    = MaterialTheme.colorScheme.tertiary
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = pagerState.currentPage == pages.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Skip-Button oben rechts – Row als Receiver für AnimatedVisibility
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                AnimatedVisibility(visible = !isLastPage) {
                    TextButton(onClick = onComplete) {
                        Text(
                            text  = stringResource(R.string.onboarding_skip),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Pager
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pageIndex ->
                OnboardingPageContent(
                    page     = pages[pageIndex],
                    isActive = pagerState.currentPage == pageIndex
                )
            }

            // Bottom: Dots + Button
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    pages.indices.forEach { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue   = if (isSelected) 24.dp else 8.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label         = "dot_width_$index"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                )
                        )
                    }
                }

                // Next / Fertig Button
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
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text       = stringResource(
                            if (isLastPage) R.string.onboarding_start
                            else R.string.onboarding_next
                        ),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isLastPage) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    isActive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "onboarding_anim")

    val scale by infiniteTransition.animateFloat(
        initialValue  = 0.92f,
        targetValue   = 1.08f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue  = -8f,
        targetValue   = 8f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_rotation"
    )

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon mit Gradient-Hintergrund
        Box(
            modifier         = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer(rotationZ = rotation * 0.3f)
                    .background(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                page.accentColor.copy(alpha = 0.3f),
                                page.accentColor.copy(alpha = 0.1f),
                                page.accentColor.copy(alpha = 0.3f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Surface(
                modifier        = Modifier
                    .size(120.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale),
                color           = page.accentColor.copy(alpha = 0.15f),
                shape           = CircleShape,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = page.icon,
                        contentDescription = null,
                        modifier           = Modifier.size(56.dp),
                        tint               = page.accentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Column als expliziter Receiver für AnimatedVisibility
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = isActive,
                enter   = fadeIn(tween(400)) + slideInVertically(
                    animationSpec  = tween(400),
                    initialOffsetY = { it / 4 }
                )
            ) {
                Text(
                    text       = stringResource(page.titleRes),
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = isActive,
                enter   = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(
                    animationSpec  = tween(500, delayMillis = 100),
                    initialOffsetY = { it / 4 }
                )
            ) {
                Text(
                    text       = stringResource(page.descriptionRes),
                    style      = MaterialTheme.typography.bodyLarge,
                    textAlign  = TextAlign.Center,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 26.sp
                )
            }
        }
    }
}