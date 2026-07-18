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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.R
import kotlinx.coroutines.launch

/**
 * Onboarding (Redesign v4): 3-Schritt-Intro, Vollbild, ohne Bottom-Nav.
 * 96x96dp abgerundete Icon-Kachel (28dp Radius, primaryContainer), Step-Dots
 * (aktiv = 20dp Pill, inaktiv = 6dp Kreis), volle-Breite CTA-Button (50dp, 16dp Radius).
 */
data class OnboardingPage(
    val icon: ImageVector,
    val titleRes: Int,
    val descriptionRes: Int
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.Transform,
            titleRes = R.string.onboarding_1_title,
            descriptionRes = R.string.onboarding_1_desc
        ),
        OnboardingPage(
            icon = Icons.Default.Tune,
            titleRes = R.string.onboarding_2_title,
            descriptionRes = R.string.onboarding_2_desc
        ),
        OnboardingPage(
            icon = Icons.Default.Favorite,
            titleRes = R.string.onboarding_3_title,
            descriptionRes = R.string.onboarding_3_desc
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
                OnboardingPageContent(
                    page = pages[pageIndex],
                    isActive = pagerState.currentPage == pageIndex
                )
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
                    pages.indices.forEach { index ->
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
private fun OnboardingPageContent(
    page: OnboardingPage,
    isActive: Boolean
) {
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
                    imageVector = page.icon,
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
                    text = stringResource(page.titleRes),
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
                    text = stringResource(page.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
