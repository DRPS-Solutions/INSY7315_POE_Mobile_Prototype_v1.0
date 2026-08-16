package com.qulloobul.moemienen.model

import java.time.LocalDateTime
import java.util.UUID

/** Urgency level for a scheduled task - drives the colored accent on its card. */
enum class Priority { LOW, MEDIUM, HIGH }

/** Category of a scheduled task, used to pick an icon (mirrors GetCategoryIcon). */
enum class Category(val label: String) {
    ADMIN("Admin"),
    MEETING("Meeting"),
    FIELDWORK("Fieldwork"),
    OTHER("Other")
}

/**
 * A scheduled task on the Daily Tasks (timeline) screen. Equivalent to
 * Tasks.razor's private TaskItem class.
 */
data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val scheduledStart: LocalDateTime,
    val category: Category,
    val priority: Priority = Priority.LOW,
    val isAllDay: Boolean = false,
    val isDone: Boolean = false,
    val completedBy: String? = null
)

/**
 * A simple today-only task shown on the Home dashboard. Equivalent to
 * Home.razor's private DailyTask class - intentionally separate from
 * TaskItem, same as in the original prototype.
 */
data class DailyTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val isCompleted: Boolean = false,
    val completedBy: String? = null
)
