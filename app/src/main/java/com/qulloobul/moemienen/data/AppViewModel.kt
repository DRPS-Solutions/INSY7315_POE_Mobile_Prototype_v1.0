package com.qulloobul.moemienen.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.qulloobul.moemienen.model.Category
import com.qulloobul.moemienen.model.DailyTask
import com.qulloobul.moemienen.model.EventModel
import com.qulloobul.moemienen.model.NotificationItem
import com.qulloobul.moemienen.model.NotificationType
import com.qulloobul.moemienen.model.Priority
import com.qulloobul.moemienen.model.RequestLinkType
import com.qulloobul.moemienen.model.RequestStatus
import com.qulloobul.moemienen.model.ResourceRequest
import com.qulloobul.moemienen.model.ResourceType
import com.qulloobul.moemienen.model.Role
import com.qulloobul.moemienen.model.TaskItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Single in-memory source of truth for the app - the Android analogue of
 * MainLayout's login state + each page's own placeholder demo lists in
 * the Blazor prototype. Everything here resets when the process dies,
 * matching the original prototype's "no real backend yet" behaviour.
 */
class AppViewModel : ViewModel() {

    // ----- Session (mirrors MainLayout.razor's currentRole/currentUserName) -----

    var currentRole by mutableStateOf<Role?>(null)
        private set

    var currentUserName by mutableStateOf("")
        private set

    fun login(name: String, role: Role) {
        currentRole = role
        currentUserName = name.ifBlank { role.label }
    }

    fun logout() {
        currentRole = null
        currentUserName = ""
    }

    // ----- Shared staff/employee list (used by both Home and Tasks "completed by" dialogs) -----

    val staffMembers = listOf(
        "Naledi Dube",
        "Sipho Mokoena",
        "Aisha Patel",
        "Liam Fourie",
        "Grace Adeyemi"
    )

    // ----- Home dashboard demo data (mirrors Home.razor's todayTasks) -----

    val todayTasks = mutableStateListOf(
        DailyTask(title = "Review Morning Dispatch Logs", category = "Operations"),
        DailyTask(title = "Submit Weekly Safety Checklist", category = "Compliance"),
        DailyTask(title = "Confirm Driver Schedules", category = "Logistics"),
        DailyTask(title = "Client Status Update Call", category = "Management")
    )

    fun setDailyTaskCompletion(task: DailyTask, isChecked: Boolean, completedBy: String?) {
        val index = todayTasks.indexOf(task)
        if (index == -1) return
        todayTasks[index] = if (isChecked) {
            task.copy(isCompleted = true, completedBy = completedBy)
        } else {
            task.copy(isCompleted = false, completedBy = null)
        }
    }

    // ----- Daily Tasks / timeline demo data (mirrors Tasks.razor's tasks list) -----

    val tasks = mutableStateListOf<TaskItem>().apply {
        val today = LocalDate.now()
        addAll(
            listOf(
                TaskItem(
                    title = "Volunteer check-in call",
                    scheduledStart = today.atTime(8, 0),
                    category = Category.ADMIN,
                    priority = Priority.LOW,
                    isDone = true,
                    completedBy = "Naledi Dube"
                ),
                TaskItem(
                    title = "Prepare donor report",
                    scheduledStart = today.atTime(9, 30),
                    category = Category.ADMIN,
                    priority = Priority.MEDIUM
                ),
                TaskItem(
                    title = "Site visit \u2014 Alexandra shelter",
                    scheduledStart = today.atTime(11, 0),
                    category = Category.FIELDWORK,
                    priority = Priority.HIGH
                ),
                TaskItem(
                    title = "Team stand-up",
                    scheduledStart = today.atTime(13, 0),
                    category = Category.MEETING,
                    priority = Priority.LOW
                ),
                TaskItem(
                    title = "Grant proposal review",
                    scheduledStart = today.atTime(15, 30),
                    category = Category.ADMIN,
                    priority = Priority.HIGH
                ),
                TaskItem(
                    title = "Community workshop setup",
                    scheduledStart = today.plusDays(1).atTime(9, 0),
                    category = Category.FIELDWORK,
                    priority = Priority.MEDIUM
                ),
                TaskItem(
                    title = "Board meeting",
                    scheduledStart = today.plusDays(1).atTime(14, 0),
                    category = Category.MEETING,
                    priority = Priority.HIGH
                ),
                TaskItem(
                    title = "Inventory count",
                    scheduledStart = today.plusDays(2).atTime(10, 0),
                    category = Category.FIELDWORK,
                    priority = Priority.LOW
                ),
                TaskItem(
                    title = "Submit grant report",
                    scheduledStart = today.atStartOfDay(),
                    category = Category.ADMIN,
                    priority = Priority.HIGH,
                    isAllDay = true
                ),
                TaskItem(
                    title = "Renew office lease paperwork",
                    scheduledStart = today.plusDays(1).atStartOfDay(),
                    category = Category.ADMIN,
                    priority = Priority.MEDIUM,
                    isAllDay = true
                )
            )
        )
    }

    fun setTaskCompletion(task: TaskItem, isChecked: Boolean, completedBy: String?) {
        val index = tasks.indexOf(task)
        if (index == -1) return
        tasks[index] = if (isChecked) {
            task.copy(isDone = true, completedBy = completedBy)
        } else {
            task.copy(isDone = false, completedBy = null)
        }
    }

    fun addTask(
        title: String,
        date: LocalDate,
        time: LocalTime?,
        isAllDay: Boolean,
        category: Category,
        priority: Priority
    ): TaskItem {
        val scheduledStart: LocalDateTime =
            if (isAllDay || time == null) date.atStartOfDay() else date.atTime(time)
        val newTask = TaskItem(
            title = title,
            scheduledStart = scheduledStart,
            category = category,
            priority = priority,
            isAllDay = isAllDay
        )
        tasks.add(newTask)
        return newTask
    }

    // ----- Notifications (mirrors NotificationService.cs) -----
    // Kept here on the shared ViewModel rather than a separate scoped
    // service, since AppViewModel already plays that "one session's
    // shared state" role in this port.

    val notifications = mutableStateListOf<NotificationItem>().apply {
        addAll(
            listOf(
                NotificationItem(
                    type = NotificationType.EVENT_ASSIGNMENT,
                    title = "Added to Annual Donor Gala",
                    message = "You've been added to the staff list for \"Annual Donor Gala\".",
                    linkedName = "Annual Donor Gala",
                    timestamp = LocalDateTime.now().minusHours(3)
                ),
                NotificationItem(
                    type = NotificationType.RESOURCE_REQUEST,
                    title = "New resource request",
                    message = "Aisha Patel requested a projector for \"Annual Donor Gala\".",
                    linkedName = "Annual Donor Gala",
                    timestamp = LocalDateTime.now().minusDays(2)
                ),
                NotificationItem(
                    type = NotificationType.RESOURCE_REQUEST,
                    title = "Request approved",
                    message = "Naledi Dube approved your \"Printing budget\" request.",
                    linkedName = "Community Workshop",
                    timestamp = LocalDateTime.now().minusDays(4),
                    isRead = true
                )
            )
        )
    }

    val unreadNotificationCount: Int get() = notifications.count { !it.isRead }

    fun addNotification(type: NotificationType, title: String, message: String, linkedName: String? = null) {
        notifications.add(0, NotificationItem(type = type, title = title, message = message, linkedName = linkedName))
    }

    fun markNotificationAsRead(item: NotificationItem) {
        val index = notifications.indexOf(item)
        if (index == -1 || item.isRead) return
        notifications[index] = item.copy(isRead = true)
    }

    fun markAllNotificationsAsRead() {
        for (i in notifications.indices) {
            if (!notifications[i].isRead) notifications[i] = notifications[i].copy(isRead = true)
        }
    }

    fun clearAllNotifications() {
        notifications.clear()
    }

    // ----- Resource Requests (mirrors Requests.razor) -----

    // Placeholder options for the "Link to" dropdowns - in the real app
    // these would come from wherever Events/Tasks persist their data,
    // rather than being duplicated here.
    val eventOptions = listOf("Annual Donor Gala", "Community Workshop", "Board Meeting")

    val taskOptions = listOf(
        "Site visit \u2014 Alexandra shelter",
        "Grant proposal review",
        "Community workshop setup",
        "Inventory count"
    )

    // Placeholder seed data, mixing all three statuses so the filter
    // chips and Admin-only controls are visible without needing to
    // submit anything first.
    val requests = mutableStateListOf(
        ResourceRequest(
            title = "Projector for donor gala",
            resourceType = ResourceType.EQUIPMENT,
            amount = "1 projector",
            notes = "Needed for the sponsor highlight reel.",
            linkType = RequestLinkType.EVENT,
            linkedName = "Annual Donor Gala",
            requestedBy = "Aisha Patel",
            requestedDate = LocalDateTime.now().minusDays(2),
            status = RequestStatus.PENDING
        ),
        ResourceRequest(
            title = "Additional volunteers",
            resourceType = ResourceType.PERSONNEL,
            amount = "4 volunteers",
            notes = "Site visit needs extra hands for the day.",
            linkType = RequestLinkType.TASK,
            linkedName = "Site visit \u2014 Alexandra shelter",
            requestedBy = "Sipho Mokoena",
            requestedDate = LocalDateTime.now().minusDays(1),
            status = RequestStatus.PENDING
        ),
        ResourceRequest(
            title = "Printing budget",
            resourceType = ResourceType.BUDGET,
            amount = "R 1,200",
            notes = "Workshop handouts and signage.",
            linkType = RequestLinkType.EVENT,
            linkedName = "Community Workshop",
            requestedBy = "Grace Adeyemi",
            requestedDate = LocalDateTime.now().minusDays(5),
            status = RequestStatus.APPROVED,
            reviewedBy = "Naledi Dube",
            reviewedDate = LocalDateTime.now().minusDays(4)
        ),
        ResourceRequest(
            title = "New laptop",
            resourceType = ResourceType.EQUIPMENT,
            amount = "1 laptop",
            notes = "Current one won't hold a charge.",
            linkType = RequestLinkType.NONE,
            requestedBy = "Liam Fourie",
            requestedDate = LocalDateTime.now().minusDays(7),
            status = RequestStatus.DENIED,
            reviewedBy = "Naledi Dube",
            reviewedDate = LocalDateTime.now().minusDays(6),
            denyReason = "Budget's earmarked for Q3 - resubmit then."
        )
    )

    fun submitRequest(
        title: String,
        resourceType: ResourceType,
        amount: String?,
        notes: String?,
        linkType: RequestLinkType,
        linkedName: String?
    ) {
        val requestedBy = currentUserName.ifBlank { "You" }
        val trimmedTitle = title.trim()
        val effectiveLinkedName = if (linkType == RequestLinkType.NONE) null else linkedName

        requests.add(
            ResourceRequest(
                title = trimmedTitle,
                resourceType = resourceType,
                amount = amount,
                notes = notes,
                linkType = linkType,
                linkedName = effectiveLinkedName,
                requestedBy = requestedBy,
                requestedDate = LocalDateTime.now(),
                status = RequestStatus.PENDING
            )
        )

        // Notify (in this prototype, notifications aren't scoped to a
        // specific recipient - Admins would be the real audience for a
        // "new request awaiting review" notification).
        val suffix = if (effectiveLinkedName != null) " for $effectiveLinkedName." else "."
        addNotification(
            type = NotificationType.RESOURCE_REQUEST,
            title = "New resource request",
            message = "$requestedBy requested \"$trimmedTitle\"$suffix",
            linkedName = effectiveLinkedName
        )
    }

    // Approve needs no extra input, so it applies immediately.
    fun approveRequest(request: ResourceRequest) {
        val index = requests.indexOf(request)
        if (index == -1) return
        val reviewedBy = currentUserName.ifBlank { "Admin" }
        requests[index] = request.copy(
            status = RequestStatus.APPROVED,
            reviewedBy = reviewedBy,
            reviewedDate = LocalDateTime.now()
        )

        addNotification(
            type = NotificationType.RESOURCE_REQUEST,
            title = "Request approved",
            message = "$reviewedBy approved your \"${request.title}\" request.",
            linkedName = request.linkedName
        )
    }

    // Deny is driven from a confirmation dialog in the UI so the admin can
    // optionally leave a reason; cancelling the dialog leaves the request
    // Pending, so this is only called once a reason (possibly blank) is
    // confirmed.
    fun denyRequest(request: ResourceRequest, reason: String?) {
        val index = requests.indexOf(request)
        if (index == -1) return
        val reviewedBy = currentUserName.ifBlank { "Admin" }
        requests[index] = request.copy(
            status = RequestStatus.DENIED,
            reviewedBy = reviewedBy,
            reviewedDate = LocalDateTime.now(),
            denyReason = reason
        )

        val reasonSuffix = if (reason.isNullOrBlank()) "" else " Reason: $reason"
        addNotification(
            type = NotificationType.RESOURCE_REQUEST,
            title = "Request denied",
            message = "$reviewedBy denied your \"${request.title}\" request.$reasonSuffix",
            linkedName = request.linkedName
        )
    }

    // ----- Event Planning (mirrors Events.razor) -----

    // Placeholder list of existing events.
    val plannedEvents = mutableStateListOf(
        EventModel(
            title = "Annual Donor Gala",
            description = "Formal evening event to thank major donors and showcase this year's impact numbers.",
            date = LocalDate.now().plusDays(14),
            time = LocalTime.of(18, 0),
            location = "Cape Town Convention Centre",
            coordinator = "Aisha Patel",
            estimatedBudget = 85000.0,
            estimatedCapacity = 220,
            staffMembers = listOf("Aisha Patel", "Grace Adeyemi", "Naledi Dube"),
            requiredResources = listOf("Stage & PA system", "Catering (220 covers)", "Photographer")
        ),
        EventModel(
            title = "Community Workshop",
            description = "Half-day skills workshop for local youth, run out of the Alexandra community hall.",
            date = LocalDate.now().plusDays(21),
            time = LocalTime.of(9, 30),
            location = "Alexandra Community Hall",
            coordinator = "Sipho Mokoena",
            estimatedBudget = 12000.0,
            estimatedCapacity = 60,
            staffMembers = listOf("Sipho Mokoena", "Liam Fourie"),
            requiredResources = listOf("Projector", "Folding chairs (60)", "Printed handouts")
        )
    )

    // "+ New" -> Save. Adds the event and notifies every staff member on
    // it (there's no "original" to diff against for a brand-new event).
    fun addEvent(event: EventModel) {
        plannedEvents.add(event)
        notifyAssignedStaff(event, event.staffMembers)
    }

    // "Edit" -> Save. Diffs staff lists before overwriting so only
    // newly-added people (not everyone already on the event) get a
    // notification, same as SaveEvent's isEditing branch.
    fun updateEvent(updated: EventModel) {
        val index = plannedEvents.indexOfFirst { it.id == updated.id }
        if (index == -1) return
        val original = plannedEvents[index]
        val newlyAdded = updated.staffMembers.filterNot { it in original.staffMembers }
        plannedEvents[index] = updated
        notifyAssignedStaff(updated, newlyAdded)
    }

    // Fires one "you've been assigned" notification per staff member
    // passed in - either the full staff list on create, or just the
    // newly added names on edit (see the callers above).
    private fun notifyAssignedStaff(evt: EventModel, staff: List<String>) {
        val dateLabel = evt.date?.format(DateTimeFormatter.ofPattern("dd MMM")) ?: "TBD"
        staff.forEach { name ->
            addNotification(
                type = NotificationType.EVENT_ASSIGNMENT,
                title = "Assigned to ${evt.title}",
                message = "$name was added to the staff list for \"${evt.title}\" on $dateLabel.",
                linkedName = evt.title
            )
        }
    }
}
