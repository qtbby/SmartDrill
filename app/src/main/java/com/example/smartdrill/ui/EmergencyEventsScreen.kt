package com.example.smartdrill.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartdrill.admin.EmergencyEvent
import com.example.smartdrill.admin.Disaster
import com.example.smartdrill.admin.AdminRepository
import com.example.smartdrill.admin.AdminAuthManager
import com.example.smartdrill.admin.EventStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Screen for managing emergency events.
 * Allows admins to view, approve, and manage emergency events.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyEventsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onAddClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val repository = remember { AdminRepository.getInstance(context) }
    val authManager = remember { AdminAuthManager.getInstance(context) }
    val scope = rememberCoroutineScope()

    var showCancelConfirm by remember { mutableStateOf<String?>(null) }
    var showDetailsDialog by remember { mutableStateOf<EmergencyEvent?>(null) }

    val allEvents by repository.emergencyEvents.collectAsState(initial = emptyList())
    val pendingEvents = allEvents.filter { it.status == EventStatus.PENDING }
    val activeEvents = allEvents.filter { it.status == EventStatus.ACTIVE || it.isActive }
    val completedEvents = allEvents.filter { it.status == EventStatus.COMPLETED || it.status == EventStatus.CANCELLED }

    var selectedFilter by remember { mutableStateOf(0) } // 0=All, 1=Pending, 2=Active, 3=Completed

    val eventsToShow = when (selectedFilter) {
        1 -> pendingEvents
        2 -> activeEvents
        3 -> completedEvents
        else -> allEvents
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Events", fontWeight = FontWeight(700)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onAddClick?.invoke() }) {
                        Icon(Icons.Default.Add, contentDescription = "Report Event")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFF1A1A2E))
        ) {
            // Filter tabs
            EventFilterTabs(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                pendingCount = pendingEvents.size,
                activeCount = activeEvents.size
            )

            // Events list
            if (eventsToShow.isEmpty()) {
                EmptyEventsState(
                    filterIndex = selectedFilter,
                    onAddClick = { onAddClick?.invoke() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(eventsToShow) { event ->
                        EventCard(
                            event = event,
                            onDetailsClick = { showDetailsDialog = event },
                            onApprove = {
                                if (event.status == EventStatus.PENDING) {
                                    scope.launch { repository.approveEvent(event.id, authManager.getCurrentAdmin()?.username ?: "system") }
                                }
                            },
                            onActivate = {
                                if (event.status == EventStatus.APPROVED) {
                                    scope.launch { repository.activateEvent(event.id) }
                                }
                            },
                            onCancel = { showCancelConfirm = event.id }
                        )
                    }
                }
            }
        }
    }

    // Cancel confirmation dialog
    if (showCancelConfirm != null) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = null },
            title = { Text("Cancel Event") },
            text = { Text("Are you sure you want to cancel this event?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repository.cancelEvent(showCancelConfirm!!) }
                    showCancelConfirm = null
                }) {
                    Text("Cancel", color = Color(0xFFEF5350))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = null }) {
                    Text("Back")
                }
            }
        )
    }

    // Details dialog
    if (showDetailsDialog != null) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = null },
            title = { Text(showDetailsDialog!!.disasterType.name) },
            text = { EventDetails(event = showDetailsDialog!!) },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun EventFilterTabs(
    selectedFilter: Int,
    onFilterSelected: (Int) -> Unit,
    pendingCount: Int,
    activeCount: Int
) {
    val tabs = listOf("All", "Pending ($pendingCount)", "Active ($activeCount)", "History")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF16213E))
            .padding(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            TextButton(
                onClick = { onFilterSelected(index) },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = if (index == selectedFilter) Color(0xFFE53935) else Color(0xFFB0BEC5),
                    fontWeight = if (index == selectedFilter) FontWeight(700) else FontWeight(400)
                )
            }
        }
    }
}

@Composable
private fun EmptyEventsState(filterIndex: Int, onAddClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.ReportProblem,
                contentDescription = null,
                tint = Color(0xFFB0BEC5),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = when (filterIndex) {
                    1 -> "No Pending Events"
                    2 -> "No Active Events"
                    3 -> "No Event History"
                    else -> "No Events"
                },
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight(500)
            )
            Text(
                text = when (filterIndex) {
                    1 -> "No events require approval at this time"
                    2 -> "No active emergency events"
                    3 -> "No historical events to show"
                    else -> "View all emergency events here"
                },
                color = Color(0xFFB0BEC5),
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onAddClick() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Report New Event")
            }
        }
    }
}

@Composable
private fun EventCard(
    event: EmergencyEvent,
    onDetailsClick: () -> Unit,
    onApprove: () -> Unit,
    onActivate: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF262637)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(getEventIcon(event.status), contentDescription = null, tint = getStatusColor(event.status), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(event.disasterType.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight(700))
                        Text(formatTimestamp(event.createdAt), color = Color(0xFFB0BEC5), fontSize = 12.sp)
                    }
                }

                Row {
                    if (event.status == EventStatus.PENDING) {
                        TextButton(onClick = onApprove) {
                            Icon(Icons.Default.Check, contentDescription = "Approve", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                        }
                        TextButton(onClick = onCancel) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp))
                        }
                    } else if (event.status == EventStatus.APPROVED) {
                        TextButton(onClick = onActivate) {
                            Icon(Icons.Default.Verified, contentDescription = "Activate", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                        }
                    }
                    TextButton(onClick = onDetailsClick) {
                        Icon(Icons.Default.Info, contentDescription = "Details", tint = Color(0xFF90CAF9), modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Progress bar for active events
            if (event.status == EventStatus.ACTIVE) {
                LinearProgressIndicator(
                    progress = 1.0f,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )
            }
        }
    }
}

@Composable
private fun EventDetails(event: EmergencyEvent) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(vertical = 8.dp)
    ) {
        Text("Disaster Type", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        Text(event.disasterType.name, color = Color.White, modifier = Modifier.padding(vertical = 4.dp))

        Spacer(Modifier.height(12.dp))

        Text("Status", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        Text(event.status.name, color = getStatusColor(event.status), modifier = Modifier.padding(vertical = 4.dp))

        Spacer(Modifier.height(12.dp))

        Text("Reported By", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        Text(event.createdAtSenderId, color = Color.White, modifier = Modifier.padding(vertical = 4.dp))

        Spacer(Modifier.height(12.dp))

        Text("Coordinates", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        event.location?.let {
            Text("Lat: ${"%.6f".format(it.lat)}", color = Color.White, modifier = Modifier.padding(vertical = 2.dp))
            Text("Lng: ${"%.6f".format(it.lng)}", color = Color.White, modifier = Modifier.padding(vertical = 2.dp))
        }

        Spacer(Modifier.height(12.dp))

        Text("Created", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        Text(event.createdAt.toString(), color = Color(0xFF90CAF9), modifier = Modifier.padding(vertical = 4.dp))

        Spacer(Modifier.height(12.dp))

        event.description?.let {
            Text("Description", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
            Text(it, color = Color.White, modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(Modifier.height(12.dp))

        Text("Approver", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        event.approver?.username?.let {
            Text(it, color = Color.White, modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

private fun formatTimestamp(date: java.util.Date): String {
    return try {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        date.toString()
    }
}

private fun getEventIcon(status: EventStatus): androidx.compose.ui.graphics.vector.ImageVector {
    return when (status) {
        EventStatus.PENDING -> Icons.Default.ReportProblem
        EventStatus.APPROVED -> Icons.Default.Check
        EventStatus.ACTIVE -> Icons.Default.Verified
        EventStatus.CANCELLED -> Icons.Default.Close
        EventStatus.COMPLETED -> Icons.Default.Check
        EventStatus.REJECTED -> Icons.Default.Close
    }
}

private fun getStatusColor(status: EventStatus): Color {
    return when (status) {
        EventStatus.PENDING -> Color(0xFFFFC107)
        EventStatus.APPROVED -> Color(0xFF2196F3)
        EventStatus.ACTIVE -> Color(0xFFF44336)
        EventStatus.CANCELLED -> Color(0xFF9E9E9E)
        EventStatus.COMPLETED -> Color(0xFF4CAF50)
        EventStatus.REJECTED -> Color(0xFF9E9E9E)
    }
}

// Preview
@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun EmergencyEventsScreenPreview() {
    EmergencyEventsScreen()
}