@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.qulloobul.moemienen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qulloobul.moemienen.components.DenyRequestDialog
import com.qulloobul.moemienen.data.AppViewModel
import com.qulloobul.moemienen.model.RequestLinkType
import com.qulloobul.moemienen.model.RequestStatus
import com.qulloobul.moemienen.model.ResourceRequest
import com.qulloobul.moemienen.model.ResourceType
import com.qulloobul.moemienen.model.Role
import com.qulloobul.moemienen.ui.theme.NavyGradientEnd
import com.qulloobul.moemienen.ui.theme.NavyGradientStart
import com.qulloobul.moemienen.ui.theme.StatusApproved
import com.qulloobul.moemienen.ui.theme.StatusDenied
import com.qulloobul.moemienen.ui.theme.StatusPending
import com.qulloobul.moemienen.ui.theme.TintAccentBlue
import com.qulloobul.moemienen.ui.theme.TintAmber
import com.qulloobul.moemienen.ui.theme.TintGreen
import com.qulloobul.moemienen.ui.theme.TintRed
import com.qulloobul.moemienen.ui.theme.AccentBlue
import java.time.format.DateTimeFormatter

/**
 * Compose equivalent of Requests.razor - a submission form plus the full
 * request list with a status filter and (Admin-only) Approve/Deny
 * controls. Every role can submit and see the list; only Admin (mirrored
 * via viewModel.currentRole == Role.ADMIN, the same check as
 * Requests.razor's IsAdmin) gets the review actions.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RequestsScreen(viewModel: AppViewModel) {
    val isAdmin = viewModel.currentRole == Role.ADMIN
    var activeFilter by remember { mutableStateOf<RequestStatus?>(null) }
    var denyTarget by remember { mutableStateOf<ResourceRequest?>(null) }

    val requests = viewModel.requests
    val filtered = (if (activeFilter == null) requests else requests.filter { it.status == activeFilter })
        .sortedByDescending { it.requestedDate }

    LazyColumn(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { RequestsHeader() }

        item { NewRequestForm(viewModel) }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                        Icon(Icons.Filled.Inbox, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            text = "All Requests",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        FilterChip(
                            label = "All (${requests.size})",
                            selected = activeFilter == null,
                            onClick = { activeFilter = null }
                        )
                        RequestStatus.entries.forEach { status ->
                            FilterChip(
                                label = "${status.displayName()} (${requests.count { it.status == status }})",
                                selected = activeFilter == status,
                                onClick = { activeFilter = status }
                            )
                        }
                    }

                    if (filtered.isEmpty()) {
                        Text(
                            text = "No requests here yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        )
                    } else {
                        filtered.forEach { req ->
                            RequestCard(
                                request = req,
                                isAdmin = isAdmin,
                                onApprove = { viewModel.approveRequest(req) },
                                onDeny = { denyTarget = req }
                            )
                        }
                    }
                }
            }
        }
    }

    val target = denyTarget
    if (target != null) {
        DenyRequestDialog(
            requestTitle = target.title,
            onConfirm = { reason ->
                viewModel.denyRequest(target, reason)
                denyTarget = null
            },
            onDismiss = { denyTarget = null }
        )
    }
}

@Composable
private fun RequestsHeader() {
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
            text = "Resource Requests",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Request equipment, budget, or people \u2014 optionally tie it to an event or task.",
            color = Color.White.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewRequestForm(viewModel: AppViewModel) {
    var title by remember { mutableStateOf("") }
    var resourceType by remember { mutableStateOf(ResourceType.EQUIPMENT) }
    var amount by remember { mutableStateOf("") }
    var linkType by remember { mutableStateOf(RequestLinkType.NONE) }
    var linkedName by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }

    var resourceMenuExpanded by remember { mutableStateOf(false) }
    var linkMenuExpanded by remember { mutableStateOf(false) }
    var linkedNameMenuExpanded by remember { mutableStateOf(false) }

    val canSubmit = title.isNotBlank() && (linkType == RequestLinkType.NONE || !linkedName.isNullOrBlank())

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 14.dp)) {
                Icon(Icons.Filled.AddBox, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = "New Request",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("What do you need?") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            ExposedDropdownMenuBox(
                expanded = resourceMenuExpanded,
                onExpandedChange = { resourceMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = resourceType.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Resource Type") },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.menuAnchor().fillMaxWidth().padding(bottom = 12.dp)
                )
                this.ExposedDropdownMenu(
                    expanded = resourceMenuExpanded,
                    onDismissRequest = { resourceMenuExpanded = false }
                ) {
                    ResourceType.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = { resourceType = option; resourceMenuExpanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Quantity / Amount (optional)") },
                placeholder = { Text("e.g. 2 laptops, R 5,000") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            ExposedDropdownMenuBox(
                expanded = linkMenuExpanded,
                onExpandedChange = { linkMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = linkType.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Link to (optional)") },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.menuAnchor().fillMaxWidth().padding(bottom = 12.dp)
                )
                this.ExposedDropdownMenu(
                    expanded = linkMenuExpanded,
                    onDismissRequest = { linkMenuExpanded = false }
                ) {
                    RequestLinkType.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                linkType = option
                                linkedName = null
                                linkMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Linking to an Event or Task is optional - "None" is the
            // default and skips this second dropdown entirely.
            if (linkType != RequestLinkType.NONE) {
                val options = if (linkType == RequestLinkType.EVENT) viewModel.eventOptions else viewModel.taskOptions
                ExposedDropdownMenuBox(
                    expanded = linkedNameMenuExpanded,
                    onExpandedChange = { linkedNameMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = linkedName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (linkType == RequestLinkType.EVENT) "Which event?" else "Which task?") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.menuAnchor().fillMaxWidth().padding(bottom = 12.dp)
                    )
                    this.ExposedDropdownMenu(
                        expanded = linkedNameMenuExpanded,
                        onDismissRequest = { linkedNameMenuExpanded = false }
                    ) {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { linkedName = option; linkedNameMenuExpanded = false }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Justification / notes") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    viewModel.submitRequest(
                        title = title,
                        resourceType = resourceType,
                        amount = amount.ifBlank { null },
                        notes = notes.ifBlank { null },
                        linkType = linkType,
                        linkedName = linkedName
                    )
                    // Reset the form, but keep the last-used resource type
                    // since people often submit a few similar requests in
                    // a row - same as the Blazor original.
                    title = ""
                    amount = ""
                    notes = ""
                    linkType = RequestLinkType.NONE
                    linkedName = null
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Request")
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
private fun RequestCard(
    request: ResourceRequest,
    isAdmin: Boolean,
    onApprove: () -> Unit,
    onDeny: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(resourceTypeIcon(request.resourceType), contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        val amountSuffix = if (request.amount.isNullOrBlank()) "" else " \u00B7 ${request.amount}"
                        Text(
                            text = "${request.resourceType.label}$amountSuffix",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = request.requestedBy,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(10.dp))
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = request.requestedDate.format(DateTimeFormatter.ofPattern("d MMM, h:mm a")),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusChip(request.status)
            }

            if (request.linkType != RequestLinkType.NONE && !request.linkedName.isNullOrBlank()) {
                Surface(
                    color = TintAccentBlue,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.wrapContentWidth().padding(top = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            if (request.linkType == RequestLinkType.EVENT) Icons.Filled.CalendarToday else Icons.Filled.Task,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${request.linkType.name.lowercase().replaceFirstChar { it.uppercase() }}: ${request.linkedName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (!request.notes.isNullOrBlank()) {
                Text(
                    text = request.notes,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Once reviewed, show who by and (if denied) why - visible to
            // everyone, not just Admins.
            if (request.status != RequestStatus.PENDING && !request.reviewedBy.isNullOrBlank()) {
                val reasonSuffix = if (request.status == RequestStatus.DENIED && !request.denyReason.isNullOrBlank()) {
                    " \u2014 \u201c${request.denyReason}\u201d"
                } else ""
                Text(
                    text = "${request.status.displayName()} by ${request.reviewedBy} on " +
                        "${request.reviewedDate?.format(DateTimeFormatter.ofPattern("d MMM"))}$reasonSuffix",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // Approve/Deny only render for Admins, and only while the
            // request is still Pending.
            if (isAdmin && request.status == RequestStatus.PENDING) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                    Button(onClick = onApprove, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = StatusApproved)) {
                        Text("Approve")
                    }
                    OutlinedButton(onClick = onDeny) {
                        Text("Deny", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: RequestStatus) {
    val (bg, fg) = when (status) {
        RequestStatus.PENDING -> TintAmber to StatusPending
        RequestStatus.APPROVED -> TintGreen to StatusApproved
        RequestStatus.DENIED -> TintRed to StatusDenied
    }
    Surface(color = bg, shape = RoundedCornerShape(50)) {
        Text(
            text = status.displayName().uppercase(),
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private fun resourceTypeIcon(type: ResourceType): ImageVector = when (type) {
    ResourceType.EQUIPMENT -> Icons.Filled.Build
    ResourceType.BUDGET -> Icons.Filled.AttachMoney
    ResourceType.PERSONNEL -> Icons.Filled.Groups
    ResourceType.VENUE -> Icons.Filled.LocationOn
    ResourceType.OTHER -> Icons.Filled.Inventory2
}

private fun RequestStatus.displayName(): String = when (this) {
    RequestStatus.PENDING -> "Pending"
    RequestStatus.APPROVED -> "Approved"
    RequestStatus.DENIED -> "Denied"
}
