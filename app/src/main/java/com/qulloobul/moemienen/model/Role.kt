package com.qulloobul.moemienen.model

/**
 * Mirrors the four role strings used by the original Blazor prototype's
 * MainLayout ("BasicStaff", "SecondaryStaff", "ManagementStaff", "Admin").
 * Kept as an enum here (rather than a raw string) since Kotlin gives us
 * exhaustive `when` checking for free.
 */
enum class Role(val label: String, val hint: String) {
    BASIC_STAFF(
        label = "Basic Staff",
        hint = "Sees Home and Daily Tasks."
    ),
    SECONDARY_STAFF(
        label = "Secondary Staff",
        hint = "Sees Home, Daily Tasks, and User Management."
    ),
    MANAGEMENT_STAFF(
        label = "Management Staff",
        hint = "Sees everything except User Management."
    ),
    ADMIN(
        label = "Admin",
        hint = "Sees every tab."
    );

    // Single source of truth for tab visibility, same rules as
    // MainLayout.razor's CanSeeMeetings / CanSeeUserManagement / CanSeeEvents.
    val canSeeMeetings: Boolean get() = this == MANAGEMENT_STAFF || this == ADMIN
    val canSeeUserManagement: Boolean get() = this == SECONDARY_STAFF || this == ADMIN
    val canSeeEvents: Boolean get() = this == MANAGEMENT_STAFF || this == ADMIN
}
