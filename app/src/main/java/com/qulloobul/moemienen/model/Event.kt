package com.qulloobul.moemienen.model

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * A planned event. Equivalent to Events.razor's private EventModel class.
 * Kept as an immutable data class (rather than the original's mutable
 * class with Clone()/CopyFrom()) since that's a more natural fit for
 * Compose state - the edit form builds up its own working values locally
 * and produces a new EventModel via `copy()` on Save, which plays the
 * same "don't mutate the roster until Save" role as Clone/CopyFrom did.
 */
data class EventModel(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val date: LocalDate? = LocalDate.now(),
    val time: LocalTime? = LocalTime.of(9, 0),
    val location: String = "",
    val coordinator: String = "",
    val estimatedBudget: Double? = null,
    val estimatedCapacity: Int? = null,
    val staffMembers: List<String> = emptyList(),
    val requiredResources: List<String> = emptyList()
)
