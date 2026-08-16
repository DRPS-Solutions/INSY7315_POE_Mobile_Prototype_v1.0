@file:OptIn(ExperimentalMaterial3Api::class)

package com.qulloobul.moemienen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.qulloobul.moemienen.model.Category
import com.qulloobul.moemienen.model.Priority
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class NewTaskResult(
    val title: String,
    val date: LocalDate,
    val time: LocalTime?,
    val isAllDay: Boolean,
    val category: Category,
    val priority: Priority
)

/**
 * Compose equivalent of CreateTaskDialog.razor - collects the fields
 * needed to build a TaskItem and hands them back as a NewTaskResult.
 * Cancelling/dismissing adds nothing, same as the original.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    onConfirm: (NewTaskResult) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var isAllDay by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(Category.ADMIN) }
    var priority by remember { mutableStateOf(Priority.LOW) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var priorityMenuExpanded by remember { mutableStateOf(false) }

    val canConfirm = title.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    OutlinedTextField(
                        value = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Filled.CalendarToday, contentDescription = "Pick date")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    if (!isAllDay) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = time.format(DateTimeFormatter.ofPattern("h:mm a")),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Time") },
                            trailingIcon = {
                                IconButton(onClick = { showTimePicker = true }) {
                                    Icon(Icons.Filled.Schedule, contentDescription = "Pick time")
                                }
                            },
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Checkbox(checked = isAllDay, onCheckedChange = { isAllDay = it })
                    Text("All day")
                }

                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = it },
                ) {
                    OutlinedTextField(
                        value = category.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.menuAnchor().fillMaxWidth().padding(bottom = 12.dp)
                    )
                    this.ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        Category.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = { category = option; categoryMenuExpanded = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = priorityMenuExpanded,
                    onExpandedChange = { priorityMenuExpanded = it },
                ) {
                    OutlinedTextField(
                        value = priority.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    this.ExposedDropdownMenu(
                        expanded = priorityMenuExpanded,
                        onDismissRequest = { priorityMenuExpanded = false }
                    ) {
                        Priority.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name.lowercase().replaceFirstChar { c -> c.uppercase() }) },
                                onClick = { priority = option; priorityMenuExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canConfirm,
                onClick = {
                    onConfirm(
                        NewTaskResult(
                            title = title.trim(),
                            date = date,
                            time = if (isAllDay) null else time,
                            isAllDay = isAllDay,
                            category = category,
                            priority = priority
                        )
                    )
                }
            ) { Text("Create task") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = false
        )
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    time = LocalTime.of(state.hour, state.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        ) {
            TimePicker(state = state)
        }
    }
}

/**
 * Material3's compose library doesn't ship a TimePickerDialog (only
 * DatePickerDialog) as of this Compose BOM, so this wraps the TimePicker
 * composable in a plain Dialog + Surface with Cancel/OK actions, matching
 * DatePickerDialog's look and feel.
 */
@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dismissButton()
                    confirmButton()
                }
            }
        }
    }
}
