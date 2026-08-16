package com.qulloobul.moemienen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Compose equivalent of DenyRequestDialog.razor - shown from RequestsScreen
 * when an Admin taps "Deny" on a pending request. Optionally collects a
 * short reason and hands it back via onConfirm. Dismissing/cancelling
 * leaves the request untouched (still Pending).
 */
@Composable
fun DenyRequestDialog(
    requestTitle: String,
    onConfirm: (reason: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deny request", fontWeight = MaterialTheme.typography.titleMedium.fontWeight) },
        text = {
            Column {
                Text(
                    text = requestTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (optional)") },
                    placeholder = { Text("Let the requester know why, if useful.") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(reason.ifBlank { null }) }) {
                Text("Deny request", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
