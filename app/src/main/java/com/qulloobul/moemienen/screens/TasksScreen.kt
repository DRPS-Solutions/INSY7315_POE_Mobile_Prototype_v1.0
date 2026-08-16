package com.qulloobul.moemienen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.qulloobul.moemienen.components.CompletedByDialog
import com.qulloobul.moemienen.components.CreateTaskDialog
import com.qulloobul.moemienen.data.AppViewModel
import com.qulloobul.moemienen.model.Category
import com.qulloobul.moemienen.model.Priority
import com.qulloobul.moemienen.model.TaskItem
import com.qulloobul.moemienen.ui.theme.NavyGradientEnd
import com.qulloobul.moemienen.ui.theme.NavyGradientStart
import com.qulloobul.moemienen.ui.theme.PriorityHigh
import com.qulloobul.moemienen.ui.theme.PriorityLow
import com.qulloobul.moemienen.ui.theme.PriorityMedium
import com.qulloobul.moemienen.ui.theme.TintAmber
import com.qulloobul.moemienen.ui.theme.TintBlue
import com.qulloobul.moemienen.ui.theme.TintRed
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

private const val START_HOUR = 6
private const val END_HOUR = 20

/** Compose equivalent of Tasks.razor - the day/week schedule timeline. */
@Composable
fun TasksScreen(viewModel: AppViewModel) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var weekStart by remember { mutableStateOf(startOfWeek(LocalDate.now())) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var taskPendingAttribution by remember { mutableStateOf<TaskItem?>(null) }

    val tasks = viewModel.tasks
    val tasksForSelectedDay = tasks.filter { it.scheduledStart.toLocalDate() == selectedDate }
        .sortedBy { it.scheduledStart }
    val allDayTasks = tasksForSelectedDay.filter { it.isAllDay }
    val timedTasks = tasksForSelectedDay.filter { !it.isAllDay }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            WeekHeader(
                weekStart = weekStart,
                selectedDate = selectedDate,
                hasTasks = { day -> tasks.any { it.scheduledStart.toLocalDate() == day } },
                onPrevWeek = { weekStart = weekStart.minusWeeks(1) },
                onNextWeek = { weekStart = weekStart.plusWeeks(1) },
                onSelectDay = { selectedDate = it }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Button(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New Task")
                }
            }
        }

        item {
            val summary = if (tasksForSelectedDay.isEmpty()) {
                "No tasks scheduled"
            } else {
                "${tasksForSelectedDay.count { it.isDone }} of ${tasksForSelectedDay.size} completed"
            }
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            PriorityLegend()
        }

        if (allDayTasks.isNotEmpty()) {
            item {
                AllDayBar(
                    tasks = allDayTasks,
                    onToggle = { task, checked ->
                        if (checked) {
                            taskPendingAttribution = task
                        } else {
                            viewModel.setTaskCompletion(task, isChecked = false, completedBy = null)
                        }
                    }
                )
            }
        }

        item {
            Timeline(
                hours = START_HOUR..END_HOUR,
                timedTasks = timedTasks,
                onToggle = { task, checked ->
                    if (checked) {
                        taskPendingAttribution = task
                    } else {
                        viewModel.setTaskCompletion(task, isChecked = false, completedBy = null)
                    }
                }
            )
        }
    }

    if (showCreateDialog) {
        CreateTaskDialog(
            onConfirm = { result ->
                val newTask = viewModel.addTask(
                    title = result.title,
                    date = result.date,
                    time = result.time,
                    isAllDay = result.isAllDay,
                    category = result.category,
                    priority = result.priority
                )
                selectedDate = newTask.scheduledStart.toLocalDate()
                weekStart = startOfWeek(selectedDate)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    val pending = taskPendingAttribution
    if (pending != null) {
        CompletedByDialog(
            taskTitle = pending.title,
            employees = viewModel.staffMembers,
            onConfirm = { employee ->
                viewModel.setTaskCompletion(pending, isChecked = true, completedBy = employee)
                taskPendingAttribution = null
            },
            onDismiss = { taskPendingAttribution = null }
        )
    }
}

@Composable
private fun WeekHeader(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    hasTasks: (LocalDate) -> Boolean,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectDay: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(NavyGradientStart, NavyGradientEnd)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevWeek) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous week", tint = Color.White)
            }
            Text(
                text = weekStart.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onNextWeek) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next week", tint = Color.White)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 0..6) {
                val day = weekStart.plusDays(i.toLong())
                DayCell(
                    day = day,
                    isSelected = day == selectedDate,
                    isToday = day == LocalDate.now(),
                    hasTasks = hasTasks(day),
                    onClick = { onSelectDay(day) }
                )
            }
        }
    }
}

@Composable
private fun DayCell(day: LocalDate, isSelected: Boolean, isToday: Boolean, hasTasks: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = day.dayOfWeek.getDisplayName(JTextStyle.SHORT, Locale.getDefault()).uppercase(),
            color = Color.White.copy(alpha = 0.75f),
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (isSelected) NavyGradientEnd else Color.Transparent,
                    shape = CircleShape
                )
                .border(
                    width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                    color = if (isToday && !isSelected) Color.White else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (hasTasks) {
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(if (isSelected) Color.White else NavyGradientEnd, CircleShape)
            )
        }
    }
}

@Composable
private fun PriorityLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendItem(color = PriorityHigh, label = "High")
        LegendItem(color = PriorityMedium, label = "Medium")
        LegendItem(color = PriorityLow, label = "Low")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun AllDayBar(tasks: List<TaskItem>, onToggle: (TaskItem, Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "ALL DAY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            tasks.forEach { task ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .background(tintFor(task.priority), RoundedCornerShape(10.dp))
                        .border(
                            width = 0.dp,
                            color = Color.Transparent
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        categoryIcon(task.category),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                        )
                        if (task.isDone && !task.completedBy.isNullOrEmpty()) {
                            Text(
                                text = "Completed by ${task.completedBy}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Checkbox(
                        checked = task.isDone,
                        onCheckedChange = { onToggle(task, it) },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun Timeline(hours: IntRange, timedTasks: List<TaskItem>, onToggle: (TaskItem, Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            hours.forEach { hour ->
                val hourTasks = timedTasks.filter { it.scheduledStart.hour == hour }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        .padding(vertical = 8.dp, horizontal = 8.dp)
                ) {
                    Text(
                        text = formatHour(hour),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(56.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        if (hourTasks.isEmpty()) {
                            Spacer(Modifier.height(1.dp))
                        } else {
                            hourTasks.forEach { task ->
                                TaskCard(task = task, onToggle = { checked -> onToggle(task, checked) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(task: TaskItem, onToggle: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(tintFor(task.priority), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    categoryIcon(task.category),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = " ${task.scheduledStart.toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a"))} \u00B7 ${task.category.label}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (task.isDone && !task.completedBy.isNullOrEmpty()) {
                Text(
                    text = "Completed by ${task.completedBy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Checkbox(
            checked = task.isDone,
            onCheckedChange = onToggle,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
    }
}

private fun tintFor(priority: Priority): Color = when (priority) {
    Priority.HIGH -> TintRed
    Priority.MEDIUM -> TintAmber
    Priority.LOW -> TintBlue
}

private fun categoryIcon(category: Category): ImageVector = when (category) {
    Category.MEETING -> Icons.Filled.Groups
    Category.ADMIN -> Icons.Filled.Description
    Category.FIELDWORK -> Icons.Filled.LocationOn
    Category.OTHER -> Icons.Filled.Task
}

private fun formatHour(hour: Int): String =
    LocalTime.of(hour, 0).format(DateTimeFormatter.ofPattern("h a"))

private fun startOfWeek(date: LocalDate): LocalDate {
    val diff = (date.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    return date.minusDays(diff.toLong())
}
