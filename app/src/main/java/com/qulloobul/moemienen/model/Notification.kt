package com.qulloobul.moemienen.model

import java.time.LocalDateTime
import java.util.UUID

/** Mirrors NotificationService.cs's NotificationType enum. */
enum class NotificationType { RESOURCE_REQUEST, EVENT_ASSIGNMENT }

/**
 * A single in-app notification. Equivalent to NotificationService.cs's
 * NotificationItem class.
 */
data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val isRead: Boolean = false,
    // Optional name of the event/request this notification refers to -
    // shown as a small chip and used to decide which page "View" opens.
    val linkedName: String? = null
)
