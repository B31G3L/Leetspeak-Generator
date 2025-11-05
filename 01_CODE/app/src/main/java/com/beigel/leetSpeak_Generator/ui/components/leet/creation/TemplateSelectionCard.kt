package com.beigel.leetSpeak_Generator.ui.components.leet.creation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.beigel.leetSpeak_Generator.R

/**
 * Card für Template-Auswahl mit erweiterbarem Interface
 * FIXED: Alle hardcodierten deutschen Texte durch String-Ressourcen ersetzt
 */
@Composable
fun TemplateSelectionCard(
    selectedTemplate: TemplateType,
    onTemplateSelected: (TemplateType) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.tertiary)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header mit Expand/Collapse Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // FIXED: String resource
                Text(
                    text = stringResource(R.string.leet_creation_template_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        // FIXED: String resource
                        contentDescription = stringResource(if (isExpanded) R.string.template_collapse else R.string.template_expand),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Current Selection Preview (immer sichtbar)
            CurrentTemplatePreview(
                selectedTemplate = selectedTemplate
            )

            // Expandable Template Selection
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Template Grid - 2 Zeilen mit gleich großen Buttons
                    val templates = TemplateType.entries

                    // Erste Zeile: 3 Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        templates.take(3).forEach { template ->
                            TemplateButton(
                                template = template,
                                isSelected = selectedTemplate == template,
                                onTemplateSelected = onTemplateSelected,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Zweite Zeile: 2 Buttons + Spacer
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        templates.drop(3).forEach { template ->
                            TemplateButton(
                                template = template,
                                isSelected = selectedTemplate == template,
                                onTemplateSelected = onTemplateSelected,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fülle leeren Platz in der letzten Zeile
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Template Details für ausgewählte Vorlage
                    TemplateDetailsCard(selectedTemplate = selectedTemplate)
                }
            }

            // Hilfetext - FIXED: String resource
            Text(
                text = stringResource(
                    if (isExpanded)
                        R.string.leet_creation_template_hint
                    else
                        R.string.leet_creation_template_hint_collapsed
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun CurrentTemplatePreview(
    selectedTemplate: TemplateType,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                // FIXED: String resource with formatting
                Text(
                    text = stringResource(R.string.leet_creation_current_template, stringResource(selectedTemplate.displayNameRes)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Medium
                )
                // FIXED: String resource with formatting
                Text(
                    text = stringResource(R.string.leet_creation_preview_short, TemplateHelpers.getTemplatePreview(selectedTemplate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TemplateButton(
    template: TemplateType,
    isSelected: Boolean,
    onTemplateSelected: (TemplateType) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Card(
        onClick = { onTemplateSelected(template) },
        modifier = modifier.height(72.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = stringResource(template.displayNameRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                if (!isSelected) {
                    Text(
                        text = TemplateHelpers.getTemplateShortPreview(template),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateDetailsCard(
    selectedTemplate: TemplateType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // FIXED: String resource with formatting
            Text(
                text = stringResource(R.string.leet_creation_template_details, stringResource(selectedTemplate.displayNameRes)),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(selectedTemplate.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            val examples = TemplateHelpers.getTemplateExamples(selectedTemplate)
            Text(
                text = stringResource(R.string.leet_creation_examples),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary
            )

            examples.forEach { example ->
                Text(
                    text = "• $example",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}