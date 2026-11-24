package com.example.smartdrill.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartdrill.admin.EmergencyEvent
import com.example.smartdrill.admin.Location
import com.example.smartdrill.admin.EventStatus
import com.example.smartdrill.admin.Disaster
import com.example.smartdrill.admin.AdminRepository
import com.example.smartdrill.admin.AdminAuthManager
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Screen for reporting/inserting a new emergency event.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmergencyEventScreen(
    modifier: Modifier = Modifier,
    onSave: (EmergencyEvent) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { AdminRepository.getInstance(context) }
    val authManager = remember { AdminAuthManager.getInstance(context) }
    val scope = rememberCoroutineScope()

    var selectedDisaster by remember { mutableStateOf<Disaster?>(null) }
    var description by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }
    var lng by remember { mutableStateOf("") }
    var isReportedFromApp by remember { mutableStateOf(false) }

    var showDisasterSelector by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val errors = mutableMapOf<String, String>()
    fun validate(): Boolean {
        errors.clear()
        if (selectedDisaster == null) errors["disaster"] = "Select a disaster type"
        if (lat.isBlank() || lng.isBlank()) errors["location"] = "Location is required" else {
            try {
                lat.toDouble()
                lng.toDouble()
            } catch (e: Exception) {
                errors["location"] = "Invalid coordinates"
            }
        }
        return errors.isEmpty()
    }

    val currentUser = authManager.getCurrentAdmin()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Emergency Event", fontWeight = FontWeight(700)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (validate()) {
                            isLoading = true
                            val event = EmergencyEvent(
                                disasterType = selectedDisaster!!,
                                createdAt = Date(),
                                createdAtSenderId = "admin_${currentUser?.id ?: "system"}",
                                location = Location(lat.toDouble(), lng.toDouble()),
                                description = description.ifBlank { null },
                                status = EventStatus.PENDING,
                                isReportedFromApp = isReportedFromApp
                            )
                            scope.launch {
                                repository.insertEvent(event)
                                onSave(event)
                            }
                        }
                    }, enabled = !isLoading) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Add, contentDescription = "Save")
                        }
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
                .padding(16.dp)
        ) {
            // Disaster type selector
            Text(
                text = "Disaster Type *",
                color = Color(0xFFB0BEC5),
                fontSize = 12.sp,
                fontWeight = FontWeight(500)
            )
            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showDisasterSelector = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF1A1A2E),
                    contentColor = Color(0xFFE53935)
                )
            ) {
                Icon(Icons.Default.Event, contentDescription = "Event", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = selectedDisaster?.name ?: "Select disaster type",
                    color = if (selectedDisaster != null) Color.White else Color(0xFFB0BEC5)
                )
            }
            if (errors["disaster"] != null) {
                Text(errors["disaster"]!!, color = Color(0xFFEF5350), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Coordinates row
            Text(
                text = "Location *",
                color = Color(0xFFB0BEC5),
                fontSize = 12.sp,
                fontWeight = FontWeight(500)
            )
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = lat,
                    onValueChange = { lat = it },
                    label = { Text("Latitude", color = Color(0xFFB0BEC5)) },
                    isError = errors.containsKey("location"),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1A1A2E),
                        unfocusedContainerColor = Color(0xFF1A1A2E),
                        focusedBorderColor = Color(0xFFE53935),
                        unfocusedBorderColor = Color(0xFF424242)
                    )
                )
                OutlinedTextField(
                    value = lng,
                    onValueChange = { lng = it },
                    label = { Text("Longitude", color = Color(0xFFB0BEC5)) },
                    isError = errors.containsKey("location"),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1A1A2E),
                        unfocusedContainerColor = Color(0xFF1A1A2E),
                        focusedBorderColor = Color(0xFFE53935),
                        unfocusedBorderColor = Color(0xFF424242)
                    )
                )
            }
            if (errors["location"] != null) {
                Text(errors["location"]!!, color = Color(0xFFEF5350), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Report source
            Text(
                text = "Report Source",
                color = Color(0xFFB0BEC5),
                fontSize = 12.sp,
                fontWeight = FontWeight(500)
            )
            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = isReportedFromApp,
                        onClick = { isReportedFromApp = true },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFFE53935),
                            unselectedColor = Color(0xFFB0BEC5)
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Reported via App", color = Color.White)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = !isReportedFromApp,
                        onClick = { isReportedFromApp = false },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFFE53935),
                            unselectedColor = Color(0xFFB0BEC5)
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("On-site Report", color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)", color = Color(0xFFB0BEC5)) },
                placeholder = { Text("Additional details about the emergency...", color = Color(0xFFB0BEC5)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1A1A2E),
                    unfocusedContainerColor = Color(0xFF1A1A2E),
                    focusedBorderColor = Color(0xFFE53935),
                    unfocusedBorderColor = Color(0xFF424242)
                )
            )

            Spacer(Modifier.height(24.dp))

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF262637)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Processing Instructions", color = Color.White, fontWeight = FontWeight(700))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("• Event will be marked as PENDING approval", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                    Text("• Notification will be sent to safety officers", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                    Text("• Review and approve before activating public alerts", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Action buttons
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = Color(0xFFB0BEC5), fontWeight = FontWeight(700))
                }

                Button(
                    onClick = {
                        if (validate()) {
                            isLoading = true
                            val event = EmergencyEvent(
                                disasterType = selectedDisaster!!,
                                createdAt = Date(),
                                createdAtSenderId = "admin_${currentUser?.id ?: "system"}",
                                location = Location(lat.toDouble(), lng.toDouble()),
                                description = description.ifBlank { null },
                                status = EventStatus.PENDING,
                                isReportedFromApp = isReportedFromApp
                            )
                            scope.launch {
                                repository.insertEvent(event)
                                onSave(event)
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Submit Event", color = Color.White, fontWeight = FontWeight(700), fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // Disaster type selector dialog
    if (showDisasterSelector) {
        AlertDialog(
            onDismissRequest = { showDisasterSelector = false },
            title = { Text("Select Disaster Type") },
            text = {
                Column {
                    Disaster.values().forEach { disaster ->
                        TextButton(
                            onClick = {
                                selectedDisaster = disaster
                                showDisasterSelector = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(disaster.name, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

// Preview
@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun AddEmergencyEventScreenPreview() {
    AddEmergencyEventScreen()
}