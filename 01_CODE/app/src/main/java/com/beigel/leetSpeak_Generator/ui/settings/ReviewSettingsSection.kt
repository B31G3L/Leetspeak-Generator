package com.beigel.leetSpeak_Generator.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.review.InAppReviewManager
import kotlinx.coroutines.launch

/**
 * Settings Section für In-App Review
 * UPDATED: Von Übersetzungen auf App-Starts umgestellt
 */
@Composable
fun ReviewSettingsSection(
    reviewStats: InAppReviewManager.ReviewStats,
    onRequestReview: suspend () -> Boolean,
    onResetReview: () -> Unit,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    CollapsibleSettingsSection(
        title = stringResource(R.string.review_section_title),
        icon = Icons.Default.Star,
        isExpanded = isExpanded,
        onExpandToggle = onExpandToggle,
        preview = stringResource(R.string.review_section_preview)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Review Statistiken
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.review_stats_title_full),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    ReviewStatRow(
                        label = stringResource(R.string.review_stat_app_starts),
                        value = "${reviewStats.appStartCount} / 3"
                    )

                    ReviewStatRow(
                        label = stringResource(R.string.review_stats_requests),
                        value = "${reviewStats.reviewPromptsShown} / 3"
                    )

                    if (reviewStats.startsUntilReview > 0) {
                        ReviewStatRow(
                            label = stringResource(R.string.review_stats_remaining),
                            value = "${reviewStats.startsUntilReview} ${stringResource(R.string.review_stat_app_starts_suffix)}"
                        )
                    }

                    if (reviewStats.successfulReviews > 0) {
                        ReviewStatRow(
                            label = stringResource(R.string.review_stats_successful),
                            value = "${reviewStats.successfulReviews}x"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Manuelle Review-Anfrage Button
            var isRequestingReview by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    scope.launch {
                        isRequestingReview = true
                        val success = onRequestReview()
                        isRequestingReview = false

                        if (!success) {
                            android.util.Log.w("ReviewButton", "Review failed")
                        }
                    }
                },
                enabled = !isRequestingReview,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isRequestingReview) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.review_manual_button_full))
            }

            // Testing: Reset Button (nur in Debug-Builds)
            // Auskommentiert für Release-Builds
            /*
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.review_reset_button))
            }
            */

            // Hinweistext
            Text(
                text = stringResource(R.string.review_hint_full),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Reset-Bestätigungsdialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.review_reset_dialog_title)) },
            text = {
                Text(stringResource(R.string.review_reset_dialog_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetReview()
                        showResetDialog = false
                    }
                ) {
                    Text(stringResource(R.string.review_reset_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.review_reset_cancel))
                }
            }
        )
    }
}

@Composable
private fun ReviewStatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}