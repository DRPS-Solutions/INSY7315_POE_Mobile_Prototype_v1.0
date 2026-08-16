@file:OptIn(ExperimentalLayoutApi::class)

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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.qulloobul.moemienen.data.AppViewModel
import com.qulloobul.moemienen.model.NotificationItem
import com.qulloobul.moemienen.model.NotificationType
import com.qulloobul.moemienen.ui.theme.AccentBlue
import com.qulloobul.moemienen.ui.theme.NavyGradientEnd
import com.qulloobul.moemienen.ui.theme.NavyGradientStart
import com.qulloobul.moemienen.ui.theme.StatusApproved
import com.qulloobul.moemienen.ui.theme.StatusPending
import com.qulloobul.moemienen.ui.theme.TintAccentBlue
import com.qulloobul.moemienen.ui.theme.TintAmber
import com.qulloobul.moemienen.ui.theme.TintBlue
import com.qulloobul.moemienen.ui.theme.TintGreen
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Compose equivalent of Notifications.razor. Clicking a notification marks
 * it read and, since this is a prototype without per-item deep links,
 * navigates to the page that owns that kind of data - same behaviour as
 * the Blazor original's OpenNotification.
 */
@Composable
fun NotificationsScreen(viewModel: AppViewModel, onNavigateToRequests: () -> Unit, onNavigateToEvents: () -> Unit) {
    var activeFilter by remember { mutableStateOf<NotificationType?>(null) }
    var unreadOnly by remember { mutableStateOf(false) }

    val all = viewModel.notifications
    val filtered = all
        .filter { activeFilter == null || it.type == activeFilter }
        .filter { !unreadOnly || !it.isRead }

    LazyColumn(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NotificationsHeader(
                unreadCount = viewModel.unreadNotificationCount,
                onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
                onClearAll = { viewModel.clearAllNotifications() },
                canMarkAllRead = viewModel.unreadNotificationCount > 0,
                canClearAll = all.isNotEmpty()
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        NotifFilterChip(
                            label = "All (${all.size})",
                            selected = activeFilter == null,
                            onClick = { activeFilter = null }
                        )
                        NotifFilterChip(
                            label = "Resource Requests (${all.count { it.type == NotificationType.RESOURCE_REQUEST }})",
                            selected = activeFilter == NotificationType.RESOURCE_REQUEST,
                            onClick = { activeFilter = NotificationType.RESOURCE_REQUEST }
                        )
                        NotifFilterChip(
                            label = "Event Assignments (${all.count { it.type == NotificationType.EVENT_ASSIGNMENT }})",
                            selected = activeFilter == NotificationType.EVENT_ASSIGNMENT,
                            onClick = { activeFilter = NotificationType.EVENT_ASSIGNMENT }
                        )
                        NotifFilterChip(
                            label = "Unread only",
                            selected = unreadOnly,
                            onClick = { unreadOnly = !unreadOnly }
                        )
                    }

                    if (filtered.isEmpty()) {
                        Text(
                            text = if (all.isEmpty()) "No notifications yet." else "Nothing matches this filter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp)
                        )
                    } else {
                        filtered.forEach { n ->
                            NotificationRow(
                                notification = n,
                                onClick = {
                                    viewModel.markNotificationAsRead(n)
                                    if (n.type == NotificationType.RESOURCE_REQUEST) onNavigateToRequests() else onNavigateToEvents()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsHeader(
    unreadCount: Int,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit,
    canMarkAllRead: Boolean,
    canClearAll: Boolean
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
            .padding(20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notifications",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Resource request updates and event assignments. " +
                        if (unreadCount > 0) "$unreadCount unread." else "You're all caught up.",
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onMarkAllRead, enabled = canMarkAllRead) {
                Text("Mark all as read", color = Color.White)
            }
            TextButton(onClick = onClearAll, enabled = canClearAll) {
                Text("Clear all", color = Color.White.copy(alpha = 0.75f))
            }
        }
    }
}

@Composable
private fun NotifFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(label) },
        colors = if (selected) {
            SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.primary,
                labelColor = Color.White
            )
        } else {
            SuggestionChipDefaults.suggestionChipColors()
        }
    )
}

@Composable
private fun NotificationRow(notification: NotificationItem, onClick: () -> Unit) {
    Surface(
        color = if (notification.isRead) MaterialTheme.colorScheme.surface else TintBlue,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(14.dp)) {
            val (bg, fg, icon) = if (notification.type == NotificationType.RESOURCE_REQUEST) {
                Triple(TintAmber, StatusPending, Icons.Filled.Inventory2)
            } else {
                Triple(TintGreen, StatusApproved, Icons.Filled.EventAvailable)
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(bg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
            }

            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = relativeTime(notification.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    if (!notification.linkedName.isNullOrBlank()) {
                        Surface(color = TintAccentBlue, shape = RoundedCornerShape(6.dp), modifier = Modifier.wrapContentWidth()) {
                            Text(
                                text = notification.linkedName,
                                color = AccentBlue,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = typeLabel(notification.type),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = typeLabel(notification.type),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp, top = 6.dp)
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}

private fun typeLabel(type: NotificationType): String =
    if (type == NotificationType.RESOURCE_REQUEST) "Resource Request" else "Event Assignment"

private fun relativeTime(timestamp: LocalDateTime): String {
    val duration = Duration.between(timestamp, LocalDateTime.now())
    val minutes = duration.toMinutes()
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        duration.toHours() < 24 -> "${duration.toHours()}h ago"
        duration.toDays() < 7 -> "${duration.toDays()}d ago"
        else -> timestamp.format(DateTimeFormatter.ofPattern("dd MMM"))
    }
}
