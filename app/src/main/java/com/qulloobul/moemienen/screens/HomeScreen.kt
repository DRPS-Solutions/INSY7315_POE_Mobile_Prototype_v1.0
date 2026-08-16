package com.qulloobul.moemienen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.qulloobul.moemienen.components.CompletedByDialog
import com.qulloobul.moemienen.data.AppViewModel
import com.qulloobul.moemienen.model.DailyTask
import com.qulloobul.moemienen.ui.theme.NavyGradientEnd
import com.qulloobul.moemienen.ui.theme.NavyGradientStart
import com.qulloobul.moemienen.ui.theme.TintGreen
import com.qulloobul.moemienen.ui.theme.TintGreenBorder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Compose equivalent of Home.razor's "Today's Tasks" dashboard. */
@Composable
fun HomeScreen(viewModel: AppViewModel) {
    var taskPendingAttribution by remember { mutableStateOf<DailyTask?>(null) }

    val tasks = viewModel.todayTasks
    val completedCount = tasks.count { it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted && !it.completedBy.isNullOrEmpty() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderBanner(completedCount = completedCount, total = tasks.size)
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Today's Active Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    tasks.forEach { task ->
                        DailyTaskRow(
                            task = task,
                            onToggle = { checked ->
                                if (checked) {
                                    taskPendingAttribution = task
                                } else {
                                    viewModel.setDailyTaskCompletion(task, isChecked = false, completedBy = null)
                                }
                            }
                        )
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.FactCheck,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Completed Today",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    if (completedTasks.isEmpty()) {
                        Text(
                            text = "Nothing completed yet today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        )
                    } else {
                        completedTasks.forEach { task ->
                            CompletedSummaryRow(task)
                        }
                    }
                }
            }
        }
    }

    val pending = taskPendingAttribution
    if (pending != null) {
        CompletedByDialog(
            taskTitle = pending.title,
            employees = viewModel.staffMembers,
            onConfirm = { employee ->
                viewModel.setDailyTaskCompletion(pending, isChecked = true, completedBy = employee)
                taskPendingAttribution = null
            },
            onDismiss = { taskPendingAttribution = null }
        )
    }
}

@Composable
private fun HeaderBanner(completedCount: Int, total: Int) {
    Box(
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Daily Schedule",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            SuggestionChip(
                onClick = {},
                label = { Text("$completedCount of $total completed today") },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    labelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun DailyTaskRow(task: DailyTask, onToggle: (Boolean) -> Unit) {
    Surface(
        color = if (task.isCompleted) TintGreen else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (task.isCompleted) TintGreenBorder else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium
                )
                Text(
                    text = task.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (task.isCompleted && !task.completedBy.isNullOrEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Completed by ${task.completedBy}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedSummaryRow(task: DailyTask) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(NavyGradientEnd, NavyGradientStart)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsOf(task.completedBy),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${task.completedBy} \u00B7 ${task.category}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun initialsOf(name: String?): String {
    if (name.isNullOrBlank()) return "?"
    return name.trim().split(Regex("\\s+"))
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString(separator = "")
}
