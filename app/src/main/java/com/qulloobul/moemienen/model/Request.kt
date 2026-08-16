package com.qulloobul.moemienen.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Lifecycle for a submitted resource request: created Pending, then an
 * Admin moves it to Approved or Denied. There's no "back to Pending" -
 * a wrong call gets resubmitted as a new request. Mirrors Requests.razor's
 * private RequestStatus enum.
 */
enum class RequestStatus { PENDING, APPROVED, DENIED }

/** What a request is asking for. Mirrors the ResourceType strings used in Requests.razor. */
enum class ResourceType(val label: String) {
    EQUIPMENT("Equipment"),
    BUDGET("Budget"),
    PERSONNEL("Personnel"),
    VENUE("Venue"),
    OTHER("Other")
}

/** What (if anything) a request is linked to. Mirrors the LinkType strings used in Requests.razor. */
enum class RequestLinkType(val label: String) {
    NONE("Not linked"),
    EVENT("An event"),
    TASK("A task")
}

/**
 * A submitted resource request. Equivalent to Requests.razor's private
 * ResourceRequest class.
 */
data class ResourceRequest(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val resourceType: ResourceType,
    val amount: String? = null,
    val notes: String? = null,
    val linkType: RequestLinkType = RequestLinkType.NONE,
    val linkedName: String? = null,
    val requestedBy: String,
    val requestedDate: LocalDateTime = LocalDateTime.now(),
    val status: RequestStatus = RequestStatus.PENDING,
    val reviewedBy: String? = null,
    val reviewedDate: LocalDateTime? = null,
    val denyReason: String? = null
)
