@file:OptIn(ExperimentalMaterial3Api::class)

package com.qulloobul.moemienen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp

/**
 * Compose equivalent of CompletedByDialog.razor - shown whenever a task's
 * checkbox is ticked so the completion can be attributed to a staff
 * member. Cancelling (or dismissing) leaves the task unchanged, same as
 * the original.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedByDialog(
    taskTitle: String,
    employees: List<String>,
    onConfirm: (employee: String) -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedEmployee by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark task complete", fontWeight = MaterialTheme.typography.titleMedium.fontWeight) },
        text = {
            Column {
                Text(
                    text = taskTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedEmployee ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Completed by") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    this.ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        employees.forEach { employee ->
                            DropdownMenuItem(
                                text = { Text(employee) },
                                onClick = {
                                    selectedEmployee = employee
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !selectedEmployee.isNullOrEmpty(),
                onClick = { selectedEmployee?.let(onConfirm) }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
