package com.beigel.leetSpeak_Generator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.beigel.leetSpeak_Generator.R


@Composable
fun ClearInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (shouldClear: Boolean, dontAskAgain: Boolean) -> Unit,  // ✅ Neue Signatur
    isReverseMode: Boolean
) {
    var dontAskAgain by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = stringResource(R.string.clear_input_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Message
                Text(
                    text = if (isReverseMode) {
                        stringResource(R.string.clear_input_dialog_message_reverse)
                    } else {
                        stringResource(R.string.clear_input_dialog_message_normal)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // "Nicht mehr fragen" Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = dontAskAgain,
                        onCheckedChange = { dontAskAgain = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.clear_input_dialog_dont_ask),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // BEHALTEN Button
                    OutlinedButton(
                        onClick = {
                            onConfirm(false, dontAskAgain)  // ✅ shouldClear = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.clear_input_dialog_keep))
                    }

                    // LÖSCHEN Button
                    Button(
                        onClick = {
                            onConfirm(true, dontAskAgain)  // ✅ shouldClear = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.clear_input_dialog_clear))
                    }
                }
            }
        }
    }
}