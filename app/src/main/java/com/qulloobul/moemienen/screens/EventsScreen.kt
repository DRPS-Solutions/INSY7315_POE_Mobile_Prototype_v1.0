@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.qulloobul.moemienen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.qulloobul.moemienen.data.AppViewModel
import com.qulloobul.moemienen.model.EventModel
import com.qulloobul.moemienen.ui.theme.NavyGradientEnd
import com.qulloobul.moemienen.ui.theme.NavyGradientStart
import com.qulloobul.moemienen.ui.theme.TintBlue
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Which of the three mutually-exclusive right-hand-panel states is showing. */
private enum class FormMode { NONE, CREATE, EDIT }

/**
 * Compose equivalent of Events.razor. Since this is a single-column phone
 * layout rather than the original's two-column md=5/7 grid, the roster and
 * the detail/edit panel are stacked in one scrollable column instead of
 * side-by-side, but the state machine (roster selection vs. create vs.
 * edit vs. empty) is the same as the Blazor original.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventsScreen(viewModel: AppViewModel) {
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    var formMode by remember { mutableStateOf(FormMode.NONE) }

    // Working copy fields for the create/edit form - mirror EventModel's
    // fields the same way editingEvent did in the Blazor original.
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var time by remember { mutableStateOf<LocalTime?>(LocalTime.of(9, 0)) }
    var coordinator by remember { mutableStateOf("") }
    var budgetText by remember { mutableStateOf("") }
    var capacityText by remember { mutableStateOf("") }
    var staffSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    var resources by remember { mutableStateOf<List<String>>(emptyList()) }
    var newResourceInput by remember { mutableStateOf("") }

    fun resetForm() {
        title = ""; description = ""; location = ""
        date = LocalDate.now(); time = LocalTime.of(9, 0)
        coordinator = ""; budgetText = ""; capacityText = ""
        staffSet = emptySet(); resources = emptyList(); newResourceInput = ""
    }

    fun loadIntoForm(evt: EventModel) {
        title = evt.title; description = evt.description; location = evt.location
        date = evt.date; time = evt.time; coordinator = evt.coordinator
        budgetText = evt.estimatedBudget?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: ""
        capacityText = evt.estimatedCapacity?.toString() ?: ""
        staffSet = evt.staffMembers.toSet()
        resources = evt.requiredResources
        newResourceInput = ""
    }

    val selectedEvent = viewModel.plannedEvents.firstOrNull { it.id == selectedEventId }
    val canSave = title.isNotBlank() && date != null

    LazyColumn(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { EventsHeader() }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(
                                text = "All Events",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Button(
                            onClick = {
                                resetForm()
                                formMode = FormMode.CREATE
                                selectedEventId = null
                            }
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("New")
                        }
                    }

                    if (viewModel.plannedEvents.isEmpty()) {
                        Text(
                            text = "No events scheduled yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                        )
                    } else {
                        viewModel.plannedEvents.sortedBy { it.date }.forEach { evt ->
                            RosterItem(
                                event = evt,
                                selected = evt.id == selectedEventId && formMode == FormMode.NONE,
                                onClick = {
                                    selectedEventId = evt.id
                                    formMode = FormMode.NONE
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    when {
                        formMode == FormMode.CREATE || formMode == FormMode.EDIT -> {
                            EventForm(
                                isCreating = formMode == FormMode.CREATE,
                                employees = viewModel.staffMembers,
                                title = title, onTitleChange = { title = it },
                                description = description, onDescriptionChange = { description = it },
                                location = location, onLocationChange = { location = it },
                                date = date, onDateChange = { date = it },
                                time = time, onTimeChange = { time = it },
                                coordinator = coordinator, onCoordinatorChange = { coordinator = it },
                                budgetText = budgetText, onBudgetChange = { budgetText = it },
                                capacityText = capacityText, onCapacityChange = { capacityText = it },
                                staffSet = staffSet,
                                onToggleStaff = { emp, checked ->
                                    staffSet = if (checked) staffSet + emp else staffSet - emp
                                },
                                resources = resources,
                                newResourceInput = newResourceInput,
                                onNewResourceChange = { newResourceInput = it },
                                onAddResource = {
                                    val value = newResourceInput.trim()
                                    if (value.isNotBlank() && resources.none { it.equals(value, ignoreCase = true) }) {
                                        resources = resources + value
                                    }
                                    newResourceInput = ""
                                },
                                onRemoveResource = { resources = resources - it },
                                canSave = canSave,
                                onSave = {
                                    val budget = budgetText.toDoubleOrNull()
                                    val capacity = capacityText.toIntOrNull()
                                    if (formMode == FormMode.CREATE) {
                                        val newEvent = EventModel(
                                            title = title.trim(),
                                            description = description,
                                            date = date,
                                            time = time,
                                            location = location,
                                            coordinator = coordinator,
                                            estimatedBudget = budget,
                                            estimatedCapacity = capacity,
                                            staffMembers = staffSet.toList(),
                                            requiredResources = resources
                                        )
                                        viewModel.addEvent(newEvent)
                                        selectedEventId = newEvent.id
                                    } else if (selectedEvent != null) {
                                        viewModel.updateEvent(
                                            selectedEvent.copy(
                                                title = title.trim(),
                                                description = description,
                                                date = date,
                                                time = time,
                                                location = location,
                                                coordinator = coordinator,
                                                estimatedBudget = budget,
                                                estimatedCapacity = capacity,
                                                staffMembers = staffSet.toList(),
                                                requiredResources = resources
                                            )
                                        )
                                    }
                                    formMode = FormMode.NONE
                                },
                                onCancel = { formMode = FormMode.NONE }
                            )
                        }

                        selectedEvent != null -> {
                            EventDetail(
                                event = selectedEvent,
                                onEdit = {
                                    loadIntoForm(selectedEvent)
                                    formMode = FormMode.EDIT
                                }
                            )
                        }

                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 50.dp)
                            ) {
                                Icon(
                                    Icons.Filled.EventNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "Select an event above to view its details,\nor tap \"New\" to plan one.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(NavyGradientStart, NavyGradientEnd)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Text(
            text = "Event & Project Planning",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun RosterItem(event: EventModel, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) TintBlue else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(event.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 3.dp)) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp))
                Text(
                    text = " ${dateTimeLabel(event)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 1.dp)) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp))
                Text(
                    text = " ${event.location.ifBlank { "No location set" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EventDetail(event: EventModel, onEdit: () -> Unit) {
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Edit")
            }
        }

        Column(modifier = Modifier.padding(top = 8.dp, bottom = 14.dp)) {
            DetailMetaRow(Icons.Filled.CalendarToday, dateTimeLabel(event))
            DetailMetaRow(Icons.Filled.LocationOn, event.location.ifBlank { "No location set" })
            DetailMetaRow(Icons.Filled.Person, "Lead: ${event.coordinator.ifBlank { "Unassigned" }}")
        }

        if (event.description.isNotBlank()) {
            SectionLabel("Description")
            Text(event.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
        }

        SectionLabel("Budget & Capacity")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(
                value = event.estimatedBudget?.let { "R ${"%,.0f".format(it)}" } ?: "\u2014",
                label = "Estimated Budget",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = event.estimatedCapacity?.toString() ?: "\u2014",
                label = "Estimated Capacity",
                modifier = Modifier.weight(1f)
            )
        }

        SectionLabel("Staff Helping (${event.staffMembers.size})")
        if (event.staffMembers.isEmpty()) {
            Text("No staff assigned yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            TagRow(event.staffMembers)
        }

        SectionLabel("Required Resources")
        if (event.requiredResources.isEmpty()) {
            Text("No resources listed yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            TagRow(event.requiredResources)
        }
    }
}

@Composable
private fun DetailMetaRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
        Text(
            text = " $text",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
    )
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .width(4.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TagRow(tags: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            Surface(color = TintBlue, shape = RoundedCornerShape(50)) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventForm(
    isCreating: Boolean,
    employees: List<String>,
    title: String, onTitleChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    location: String, onLocationChange: (String) -> Unit,
    date: LocalDate?, onDateChange: (LocalDate?) -> Unit,
    time: LocalTime?, onTimeChange: (LocalTime?) -> Unit,
    coordinator: String, onCoordinatorChange: (String) -> Unit,
    budgetText: String, onBudgetChange: (String) -> Unit,
    capacityText: String, onCapacityChange: (String) -> Unit,
    staffSet: Set<String>,
    onToggleStaff: (String, Boolean) -> Unit,
    resources: List<String>,
    newResourceInput: String,
    onNewResourceChange: (String) -> Unit,
    onAddResource: () -> Unit,
    onRemoveResource: (String) -> Unit,
    canSave: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var coordinatorMenuExpanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 14.dp)) {
        Icon(if (isCreating) Icons.Filled.AddBox else Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(
            text = if (isCreating) "Create New Event" else "Edit Event",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }

    OutlinedTextField(
        value = title, onValueChange = onTitleChange,
        label = { Text("Event Title") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )
    OutlinedTextField(
        value = description, onValueChange = onDescriptionChange,
        label = { Text("Description") }, minLines = 3,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )
    OutlinedTextField(
        value = location, onValueChange = onLocationChange,
        label = { Text("Location") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    Row(modifier = Modifier.padding(bottom = 12.dp)) {
        OutlinedTextField(
            value = date?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "",
            onValueChange = {}, readOnly = true,
            label = { Text("Date") },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = "Pick date")
                }
            },
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = time?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "",
            onValueChange = {}, readOnly = true,
            label = { Text("Time") },
            trailingIcon = {
                IconButton(onClick = { showTimePicker = true }) {
                    Icon(Icons.Filled.Schedule, contentDescription = "Pick time")
                }
            },
            modifier = Modifier.weight(1f)
        )
    }

    ExposedDropdownMenuBox(
        expanded = coordinatorMenuExpanded,
        onExpandedChange = { coordinatorMenuExpanded = it }
    ) {
        OutlinedTextField(
            value = coordinator.ifBlank { "" },
            onValueChange = {}, readOnly = true,
            label = { Text("Lead Coordinator") },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.menuAnchor().fillMaxWidth().padding(bottom = 12.dp)
        )
        this.ExposedDropdownMenu(
            expanded = coordinatorMenuExpanded,
            onDismissRequest = { coordinatorMenuExpanded = false }
        ) {
            employees.forEach { emp ->
                DropdownMenuItem(
                    text = { Text(emp) },
                    onClick = { onCoordinatorChange(emp); coordinatorMenuExpanded = false }
                )
            }
        }
    }

    Row(modifier = Modifier.padding(bottom = 12.dp)) {
        OutlinedTextField(
            value = budgetText,
            onValueChange = { s -> if (s.all { it.isDigit() || it == '.' }) onBudgetChange(s) },
            label = { Text("Estimated Budget (R)") },
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = capacityText,
            onValueChange = { s -> if (s.all { it.isDigit() }) onCapacityChange(s) },
            label = { Text("Estimated Capacity") },
            modifier = Modifier.weight(1f)
        )
    }

    SectionLabel("Staff Helping")
    Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            employees.forEach { emp ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = emp in staffSet,
                        onCheckedChange = { checked -> onToggleStaff(emp, checked) }
                    )
                    Text(emp, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    SectionLabel("Required Resources")
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        OutlinedTextField(
            value = newResourceInput,
            onValueChange = onNewResourceChange,
            label = { Text("Add a resource") },
            placeholder = { Text("e.g. Projector, 6 folding tables") },
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        OutlinedButton(onClick = onAddResource) { Text("Add") }
    }
    if (resources.isEmpty()) {
        Text(
            "No resources added yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            resources.forEach { resource ->
                Surface(color = TintBlue, shape = RoundedCornerShape(50)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp, end = 2.dp)) {
                        Text(
                            resource,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { onRemoveResource(resource) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = onSave, enabled = canSave) {
            Text(if (isCreating) "Schedule Event" else "Save Changes")
        }
        OutlinedButton(onClick = onCancel) { Text("Cancel") }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (date ?: LocalDate.now()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onDateChange(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    if (showTimePicker) {
        val current = time ?: LocalTime.of(9, 0)
        val state = rememberTimePickerState(initialHour = current.hour, initialMinute = current.minute, is24Hour = false)
        EventTimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChange(LocalTime.of(state.hour, state.minute))
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }
        ) { TimePicker(state = state) }
    }
}

/**
 * Material3's compose library doesn't ship a TimePickerDialog, so this
 * wraps TimePicker in a plain Dialog + Surface - same approach as
 * CreateTaskDialog.kt's private TimePickerDialog helper, duplicated here
 * since that one isn't exported from its file.
 */
@Composable
private fun EventTimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dismissButton()
                    confirmButton()
                }
            }
        }
    }
}

private fun dateTimeLabel(evt: EventModel): String {
    val datePart = evt.date?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "TBD"
    val timePart = evt.time?.format(DateTimeFormatter.ofPattern("h:mm a"))
    return if (timePart != null) "$datePart at $timePart" else datePart
}
