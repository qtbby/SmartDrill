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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartdrill.admin.EvacuationArea
import com.example.smartdrill.admin.AdminRepository
import kotlinx.coroutines.launch

/**
 * Screen for managing evacuation areas.
 * Allows admins to add, edit, and remove evacuation points.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvacuationAreasScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onAddClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val repository = remember { AdminRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    var showDeleteConfirm by remember { mutableStateOf<Int?>(null) }
    var showDetailsDialog by remember { mutableStateOf<EvacuationArea?>(null) }

    val evacuationAreas by repository.evacuationAreas.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evacuation Areas", fontWeight = FontWeight(700)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onAddClick?.invoke() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Area")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (evacuationAreas.isEmpty()) {
            EmptyEvacuationState(
                modifier = Modifier.padding(paddingValues),
                onAddClick = { onAddClick?.invoke() }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                items(evacuationAreas) { area ->
                    EvacuationAreaCard(
                        area = area,
                        onClick = { showDetailsDialog = area },
                        onEdit = { /* TODO: Navigate to edit */ },
                        onDelete = { showDeleteConfirm = area.id },
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
            title = { Text("Delete Evacuation Area") },
            text = { Text("Are you sure you want to delete this evacuation area? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        repository.removeEvacuationArea(showDeleteConfirm!!)
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
            text = { EvacuationAreaDetails(area = showDetailsDialog!!) },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun EmptyEvacuationState(
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
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFFB0BEC5),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No Evacuation Areas",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight(500)
            )
            Text(
                text = "Add designated safe zones for emergency evacuation",
                color = Color(0xFFB0BEC5),
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onAddClick() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Evacuation Area")
            }
        }
    }
}

@Composable
private fun EvacuationAreaCard(
    area: EvacuationArea,
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
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(area.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight(700))
                    Text("Capacity: ${area.capacity} people", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                    Text("Lat: ${"%.5f".format(area.position.lat)}, Lng: ${"%.5f".format(area.position.lng)}",
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
private fun EvacuationAreaDetails(area: EvacuationArea) {
    Column {
        Text("Name", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        Text(area.name, color = Color.White, modifier = Modifier.padding(vertical = 4.dp))

        Spacer(Modifier.height(12.dp))

        Text("Capacity", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        Text("${area.capacity} people", color = Color.White, modifier = Modifier.padding(vertical = 4.dp))

        Spacer(Modifier.height(12.dp))

        Text("Coordinates", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        Text("Lat: ${"%.6f".format(area.position.lat)}", color = Color.White, modifier = Modifier.padding(vertical = 2.dp))
        Text("Lng: ${"%.6f".format(area.position.lng)}", color = Color.White, modifier = Modifier.padding(vertical = 2.dp))

        Spacer(Modifier.height(12.dp))

        Text("Status", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        Text(if (area.isAccessible) "Accessible" else "Restricted",
             color = if (area.isAccessible) Color(0xFF4CAF50) else Color(0xFFEF5350),
             modifier = Modifier.padding(vertical = 4.dp))

        Spacer(Modifier.height(12.dp))

        Text("Last Updated", fontWeight = FontWeight(700), color = Color(0xFFB0BEC5))
        Text(area.lastUpdated.toString(), color = Color(0xFF90CAF9), modifier = Modifier.padding(vertical = 4.dp))
    }
}

// Preview
@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun EvacuationAreasScreenPreview() {
    EvacuationAreasScreen()
}