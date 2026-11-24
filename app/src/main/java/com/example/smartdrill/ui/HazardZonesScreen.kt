package com.example.smartdrill.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
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
import com.example.smartdrill.admin.HazardZone
import com.example.smartdrill.admin.HazardSeverity
import com.example.smartdrill.admin.AdminRepository
import kotlinx.coroutines.launch

/**
 * Screen for managing hazard zones.
 * Allows admins to add, edit, and remove hazardous areas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HazardZonesScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onAddClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val repository = remember { AdminRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }
    var showDetailsDialog by remember { mutableStateOf<HazardZone?>(null) }

    val hazardZones by repository.hazardZones.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hazard Zones", fontWeight = FontWeight(700)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onAddClick?.invoke() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Zone")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (hazardZones.isEmpty()) {
            EmptyHazardState(
                modifier = Modifier.padding(paddingValues),
                onAddClick = { onAddClick?.invoke() }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                items(hazardZones) { zone ->
                    HazardZoneCard(
                        zone = zone,
                        onClick = { showDetailsDialog = zone },
                        onEdit = { /* TODO: Navigate to edit */ },
                        onDelete = { showDeleteConfirm = zone.id },
                        onLocationClick = {
                            // Open location on map
                        }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Hazard Zone") },
            text = { Text("Are you sure you want to delete this hazard zone? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        repository.removeHazardZone(showDeleteConfirm!!)
                    }
                    showDeleteConfirm = null
                }) {
                    Text("Delete", color = Color(0xFFEF5350))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Details dialog
    if (showDetailsDialog != null) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = null },
            title = { Text(showDetailsDialog!!.name) },
            text = { HazardZoneDetails(zone = showDetailsDialog!!) },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun EmptyHazardState(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFB0BEC5),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No Hazard Zones",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight(500)
            )
            Text(
                text = "Add hazardous areas to warn users about danger zones",
                color = Color(0xFFB0BEC5),
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onAddClick() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Hazard Zone")
            }
        }
    }
}

@Composable
private fun HazardZoneCard(
    zone: HazardZone,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLocationClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF262637)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = getSeverityColor(zone.severity), modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(zone.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight(700))
                    Text("Severity: ${zone.severity.name}", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                    Text("Lat: ${"%.5f".format(zone.position.lat)}, Lng: ${"%.5f".format(zone.position.lng)}",
                         color = Color(0xFF90CAF9), fontSize = 12.sp)
                }
            }

            Row {
                TextButton(onClick = { onLocationClick() }) {
                    Icon(Icons.Default.Info, contentDescription = "Location", tint = Color(0xFF90CAF9), modifier = Modifier.size(20.dp))
                }
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF90CAF9), modifier = Modifier.size(20.dp))
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun HazardZoneDetails(zone: HazardZone) {
    Column {
        Text("Zone Name", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        Text(zone.name, color = Color.White, modifier = Modifier.padding(vertical = 4.dp))

        Spacer(Modifier.height(12.dp))

        Text("Severity Level", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        Text(zone.severity.name, color = getSeverityColor(zone.severity), modifier = Modifier.padding(vertical = 4.dp))

        Spacer(Modifier.height(12.dp))

        Text("Coordinates", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        Text("Lat: ${"%.6f".format(zone.position.lat)}", color = Color.White, modifier = Modifier.padding(vertical = 2.dp))
        Text("Lng: ${"%.6f".format(zone.position.lng)}", color = Color.White, modifier = Modifier.padding(vertical = 2.dp))

        Spacer(Modifier.height(12.dp))

        Text("Description", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        Text(zone.description ?: "No description", color = Color.White, modifier = Modifier.padding(vertical = 4.dp))

        Spacer(Modifier.height(12.dp))

        Text("Last Updated", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        Text(zone.lastUpdated.toString(), color = Color(0xFF90CAF9), modifier = Modifier.padding(vertical = 4.dp))
    }
}

private fun getSeverityColor(severity: HazardSeverity): Color {
    return when (severity) {
        HazardSeverity.LOW -> Color(0xFF4CAF50)
        HazardSeverity.MEDIUM -> Color(0xFFFFC107)
        HazardSeverity.HIGH -> Color(0xFFFF9800)
        HazardSeverity.CRITICAL -> Color(0xFFF44336)
    }
}

// Preview
@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun HazardZonesScreenPreview() {
    HazardZonesScreen()
}