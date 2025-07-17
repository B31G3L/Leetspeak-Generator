package com.beigel.leetSpeak_Generator.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.viewmodel.MainIntent
import com.beigel.leetSpeak_Generator.viewmodel.MainViewModel

/**
 * Enhanced Leet Creation Dialog mit Auto-Suffix und Template-Auswahl
 */
@Composable
fun LeetCreationDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    existingLeet: CustomLeet? = null,
    leetIndex: Int = -1
) {
    // Base name state (ohne -Leet Suffix)
    var baseName by remember {
        mutableStateOf(
            existingLeet?.name?.removeSuffix("-Leet") ?: ""
        )
    }

    var selectedIconResId by remember {
        mutableStateOf(existingLeet?.iconResId ?: R.drawable.ic_custom_mode)
    }

    var showIconPicker by remember { mutableStateOf(false) }
    var showTemplatePicker by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf(TemplateType.SIMPLE) }

    // Translation states für alle Buchstaben
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val translationStates = remember {
        alphabet.map { char ->
            mutableStateOf(
                existingLeet?.getTranslation(char.toString()) ?: char.toString()
            )
        }
    }

    // Computed display name mit -Leet Suffix
    val displayName = remember(baseName) {
        if (baseName.isBlank()) {
            "Neues Leet"
        } else {
            "$baseName-Leet"
        }
    }

    val isNewLeet = existingLeet == null
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Fullscreen
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header mit Toolbar Style
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isNewLeet) "Leet erstellen" else "Leet bearbeiten",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = onDismiss
                        ) {
                            Text("Abbrechen")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                // Leet speichern
                                val translations = mutableMapOf<String, String>()
                                alphabet.forEachIndexed { index, char ->
                                    translations[char.toString()] = translationStates[index].value
                                }

                                val finalName = displayName.ifEmpty { "Custom-Leet" }

                                if (isNewLeet) {
                                    // Neues Leet erstellen
                                    viewModel.handleIntent(
                                        MainIntent.CreateLeet(
                                            name = finalName,
                                            iconResId = selectedIconResId,
                                            useExtendedDefaults = selectedTemplate == TemplateType.EXTENDED
                                        )
                                    )
                                } else {
                                    // Bestehendes Leet aktualisieren
                                    val updatedLeet = CustomLeet(
                                        name = finalName,
                                        iconResId = selectedIconResId
                                    ).apply {
                                        setTranslations(translations)
                                    }

                                    viewModel.handleIntent(
                                        MainIntent.UpdateLeet(leetIndex, updatedLeet)
                                    )
                                }

                                onDismiss()
                            },
                            enabled = baseName.isNotBlank()
                        ) {
                            Text("Speichern")
                        }
                    }
                }

                // Scrollbarer Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Enhanced Leet Info Card
                    EnhancedLeetInfoCard(
                        baseName = baseName,
                        onBaseNameChange = { baseName = it },
                        displayName = displayName,
                        selectedIconResId = selectedIconResId,
                        onIconClick = { showIconPicker = true }
                    )

                    // Template Selection Card
                    TemplateSelectionCard(
                        selectedTemplate = selectedTemplate,
                        onTemplateSelected = { template ->
                            selectedTemplate = template
                            // Apply template to translation states
                            applyTemplate(template, translationStates, alphabet)
                        },
                        onShowTemplatePicker = { showTemplatePicker = true }
                    )

                    // Enhanced Translation Table Card
                    EnhancedTranslationTableCard(
                        alphabet = alphabet,
                        translationStates = translationStates
                    )
                }
            }
        }
    }

    // Enhanced Icon Picker Dialog
    if (showIconPicker) {
        EnhancedIconPickerDialog(
            selectedIconResId = selectedIconResId,
            onIconSelected = { newIconResId ->
                selectedIconResId = newIconResId
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }

    // Template Picker Dialog
    if (showTemplatePicker) {
        TemplatePickerDialog(
            selectedTemplate = selectedTemplate,
            onTemplateSelected = { template ->
                selectedTemplate = template
                applyTemplate(template, translationStates, alphabet)
                showTemplatePicker = false
            },
            onDismiss = { showTemplatePicker = false }
        )
    }
}

@Composable
private fun EnhancedLeetInfoCard(
    baseName: String,
    onBaseNameChange: (String) -> Unit,
    displayName: String,
    selectedIconResId: Int,
    onIconClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.secondary)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Leet-Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon Button
                IconButton(
                    onClick = onIconClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(selectedIconResId),
                        contentDescription = "Icon ändern",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name Input mit Preview
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = baseName,
                        onValueChange = onBaseNameChange,
                        label = { Text("Basis-Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("z.B. Gaming") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preview des finalen Namens
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Wird zu: $displayName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Text(
                text = "Tippe auf das Icon, um es zu ändern. Der Name wird automatisch mit '-Leet' ergänzt.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun TemplateSelectionCard(
    selectedTemplate: TemplateType,
    onTemplateSelected: (TemplateType) -> Unit,
    onShowTemplatePicker: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = "Vorlage auswählen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Zuklappen" else "Aufklappen",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Current Selection Preview (immer sichtbar)
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
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
                        Text(
                            text = "Aktuelle Vorlage: ${selectedTemplate.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Vorschau: ${getTemplatePreview(selectedTemplate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Expandable Template Selection
            AnimatedVisibility(
                visible = isExpanded,
                enter =  expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Template Grid - 2 Zeilen mit gleich großen Buttons
                    val templates = TemplateType.values().toList()

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
                    TemplateDetails(selectedTemplate = selectedTemplate)
                }
            }

            // Hilfetext
            Text(
                text = if (isExpanded)
                    "Wähle eine Vorlage als Startpunkt. Du kannst alle Übersetzungen später anpassen."
                else
                    "Tippe auf den Pfeil, um alle Vorlagen zu sehen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
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
        modifier = modifier.height(72.dp), // Feste Höhe für alle Buttons
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
                    text = template.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                if (!isSelected) {
                    Text(
                        text = getTemplateShortPreview(template),
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
private fun TemplateDetails(selectedTemplate: TemplateType) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = "Details: ${selectedTemplate.displayName}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = selectedTemplate.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Beispiel-Übersetzungen
            val examples = getTemplateExamples(selectedTemplate)
            Text(
                text = "Beispiele:",
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

@Composable
private fun EnhancedTranslationTableCard(
    alphabet: String,
    translationStates: List<MutableState<String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.secondary)
        )
    ) {
        Column {
            // Header
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Übersetzungstabelle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Column Headers
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Plain",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Leet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Plain",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Leet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Translation Rows
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                for (i in 0 until 13) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        // Left side (A-M)
                        Text(
                            text = alphabet[i].toString(),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        OutlinedTextField(
                            value = translationStates[i].value,
                            onValueChange = { translationStates[i].value = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                textAlign = TextAlign.Center
                            )
                        )

                        // Right side (N-Z)
                        if (i + 13 < alphabet.length) {
                            Text(
                                text = alphabet[i + 13].toString(),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            OutlinedTextField(
                                value = translationStates[i + 13].value,
                                onValueChange = { translationStates[i + 13].value = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    textAlign = TextAlign.Center
                                )
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(2f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedIconPickerDialog(
    selectedIconResId: Int,
    onIconSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Erweiterte Icon-Liste mit allen verfügbaren Icons
    val availableIcons = listOf(
        // Existing icons
        R.drawable.ic_custom_mode,
        R.drawable.ic_simple_mode,
        R.drawable.ic_extended_mode,
        R.drawable.ic_about,
        R.drawable.ic_add_profile,
        R.drawable.ic_edit,
        R.drawable.ic_favorite_border,
        R.drawable.ic_favorite,
        R.drawable.ic_copy,
        R.drawable.ic_search,
        R.drawable.ic_arrow_left,
        R.drawable.ic_arrow_right,
        R.drawable.ic_toolbar_save,
        R.drawable.ic_toolbar_delete,
        R.drawable.ic_favorite_star
    )

    // Material Icons (als fallback falls drawable nicht verfügbar)
    val materialIcons = listOf(
        Icons.Default.Star,
        Icons.Default.Favorite,
        Icons.Default.Settings,
        Icons.Default.Build,
        Icons.Default.Code,
        Icons.Default.Computer,
        Icons.Default.Games,
        Icons.Default.Sports,
        Icons.Default.Movie,
        Icons.Default.Book,
        Icons.Default.School,
        Icons.Default.Work,
        Icons.Default.Home,
        Icons.Default.Flight,
        Icons.Default.Restaurant,
        Icons.Default.Nature,
        Icons.Default.Pets,
        Icons.Default.Face,
        Icons.Default.Group,
        Icons.Default.Business,
        Icons.Default.Science,
        Icons.Default.PhotoCamera,
        Icons.Default.Videocam,
        Icons.Default.Gamepad,
        Icons.Default.SportsEsports,
        Icons.Default.Headphones,
        Icons.Default.Mic,
        Icons.Default.Security,
        Icons.Default.Shield,
        Icons.Default.Lock,
        Icons.Default.Key,
        Icons.Default.Diamond,
        Icons.Default.EmojiEvents,
        Icons.Default.Celebration,
        Icons.Default.LocalFireDepartment,
        Icons.Default.Bolt,
        Icons.Default.Rocket,
        Icons.Default.Psychology,
        Icons.Default.AutoAwesome,
        Icons.Default.Widgets,
        Icons.Default.Extension,
        Icons.Default.Memory,
        Icons.Default.Storage,
        Icons.Default.Cloud,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Icon auswählen",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(400.dp)
            ) {
                // Drawable Icons
                items(availableIcons) { iconResId ->
                    IconButton(
                        onClick = { onIconSelected(iconResId) },
                        modifier = Modifier
                            .size(48.dp)
                            .then(
                                if (iconResId == selectedIconResId) {
                                    Modifier.background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        MaterialTheme.shapes.medium
                                    )
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        Icon(
                            painter = painterResource(iconResId),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (iconResId == selectedIconResId) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                // Material Icons (convert to drawable resource IDs)
                items(materialIcons) { icon ->
                    IconButton(
                        onClick = {
                            // For material icons, we'll use the custom_mode as placeholder
                            // In a real app, you'd need to convert these to drawable resources
                            onIconSelected(R.drawable.ic_custom_mode)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
private fun TemplatePickerDialog(
    selectedTemplate: TemplateType,
    onTemplateSelected: (TemplateType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vorlage auswählen") },
        text = {
            Column {
                TemplateType.values().forEach { template ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTemplate == template,
                            onClick = { onTemplateSelected(template) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = template.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = template.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Beispiel: ${getTemplatePreview(template)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

// Enum für Template-Typen
enum class TemplateType(val displayName: String, val description: String) {
    SIMPLE("Simple", "Einfache 1:1 Ersetzungen"),
    EXTENDED("Extended", "Erweiterte Multi-Zeichen Ersetzungen"),
    CUSTOM("Leer", "Startet mit leerer Tabelle"),
    NUMERIC("Numerisch", "Nur Zahlen als Ersetzungen"),
    SYMBOLS("Symbole", "Nur Symbole als Ersetzungen")
}

// Helper functions
private fun applyTemplate(
    template: TemplateType,
    translationStates: List<MutableState<String>>,
    alphabet: String
) {
    alphabet.forEachIndexed { index, char ->
        translationStates[index].value = when (template) {
            TemplateType.SIMPLE -> getSimpleTranslation(char)
            TemplateType.EXTENDED -> getExtendedTranslation(char)
            TemplateType.CUSTOM -> char.toString()
            TemplateType.NUMERIC -> getNumericTranslation(char)
            TemplateType.SYMBOLS -> getSymbolTranslation(char)
        }
    }
}

private fun getTemplateShortPreview(template: TemplateType): String {
    return when (template) {
        TemplateType.SIMPLE -> "A→4"
        TemplateType.EXTENDED -> "A→4"
        TemplateType.CUSTOM -> "A→A"
        TemplateType.NUMERIC -> "A→4"
        TemplateType.SYMBOLS -> "A→@"
    }
}

private fun getTemplateExamples(template: TemplateType): List<String> {
    return when (template) {
        TemplateType.SIMPLE -> listOf(
            "A → 4",
            "E → 3",
            "O → 0",
            "S → 5",
            "T → 7"
        )
        TemplateType.EXTENDED -> listOf(
            "A → 4",
            "M → /\\/\\",
            "N → |\\|",
            "U → |_|",
            "W → \\/\\/"
        )
        TemplateType.CUSTOM -> listOf(
            "A → A",
            "B → B",
            "C → C",
            "... (leer)"
        )
        TemplateType.NUMERIC -> listOf(
            "A → 4",
            "B → 8",
            "C → 3",
            "O → 0",
            "S → 5"
        )
        TemplateType.SYMBOLS -> listOf(
            "A → @",
            "E → €",
            "S → $",
            "T → †",
            "Y → ¥"
        )
    }
}

private fun getSimpleTranslation(char: Char): String {
    return when (char) {
        'A' -> "4"
        'B' -> "8"
        'C' -> "C"
        'D' -> "D"
        'E' -> "3"
        'F' -> "F"
        'G' -> "6"
        'H' -> "#"
        'I' -> "1"
        'J' -> "J"
        'K' -> "K"
        'L' -> "L"
        'M' -> "M"
        'N' -> "N"
        'O' -> "0"
        'P' -> "P"
        'Q' -> "Q"
        'R' -> "R"
        'S' -> "5"
        'T' -> "7"
        'U' -> "U"
        'V' -> "V"
        'W' -> "W"
        'X' -> "X"
        'Y' -> "Y"
        'Z' -> "2"
        else -> char.toString()
    }
}

private fun getExtendedTranslation(char: Char): String {
    return when (char) {
        'A' -> "4"
        'B' -> "8"
        'C' -> "("
        'D' -> "|)"
        'E' -> "3"
        'F' -> "|="
        'G' -> "6"
        'H' -> "#"
        'I' -> "!"
        'J' -> "_|"
        'K' -> "|<"
        'L' -> "1"
        'M' -> "/\\/\\"
        'N' -> "|\\|"
        'O' -> "0"
        'P' -> "9"
        'Q' -> "0_"
        'R' -> "2"
        'S' -> "5"
        'T' -> "7"
        'U' -> "|_|"
        'V' -> "\\/"
        'W' -> "\\/\\/"
        'X' -> "><"
        'Y' -> "`/"
        'Z' -> "Z"
        else -> char.toString()
    }
}

private fun getNumericTranslation(char: Char): String {
    return when (char) {
        'A' -> "4"
        'B' -> "8"
        'C' -> "3"
        'D' -> "0"
        'E' -> "3"
        'F' -> "7"
        'G' -> "6"
        'H' -> "4"
        'I' -> "1"
        'J' -> "1"
        'K' -> "1"
        'L' -> "1"
        'M' -> "44"
        'N' -> "4"
        'O' -> "0"
        'P' -> "9"
        'Q' -> "0"
        'R' -> "2"
        'S' -> "5"
        'T' -> "7"
        'U' -> "0"
        'V' -> "5"
        'W' -> "5"
        'X' -> "2"
        'Y' -> "7"
        'Z' -> "2"
        else -> char.toString()
    }
}

private fun getSymbolTranslation(char: Char): String {
    return when (char) {
        'A' -> "@"
        'B' -> "β"
        'C' -> "©"
        'D' -> "Ð"
        'E' -> "€"
        'F' -> "ƒ"
        'G' -> "§"
        'H' -> "#"
        'I' -> "!"
        'J' -> "¿"
        'K' -> "₭"
        'L' -> "£"
        'M' -> "₥"
        'N' -> "ñ"
        'O' -> "ø"
        'P' -> "₱"
        'Q' -> "¤"
        'R' -> "®"
        'S' -> "$"
        'T' -> "†"
        'U' -> "µ"
        'V' -> "√"
        'W' -> "₩"
        'X' -> "×"
        'Y' -> "¥"
        'Z' -> "ƺ"
        else -> char.toString()
    }
}

private fun getTemplatePreview(template: TemplateType): String {
    return when (template) {
        TemplateType.SIMPLE -> "HELLO → #3LL0"
        TemplateType.EXTENDED -> "HELLO → #3110"
        TemplateType.CUSTOM -> "HELLO → HELLO"
        TemplateType.NUMERIC -> "HELLO → 43110"
        TemplateType.SYMBOLS -> "HELLO → #€££ø"
    }
}