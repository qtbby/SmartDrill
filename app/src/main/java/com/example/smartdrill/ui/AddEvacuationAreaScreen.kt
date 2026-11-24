package com.example.smartdrill.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartdrill.admin.EvacuationArea
import com.example.smartdrill.admin.Location
import com.example.smartdrill.admin.AdminRepository
import kotlinx.coroutines.launch

/**
 * Screen for adding a new evacuation area.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEvacuationAreaScreen(
    modifier: Modifier = Modifier,
    onSave: (EvacuationArea) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { AdminRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }
    var lng by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isAccessible by remember { mutableStateOf(true) }

    var showMapPicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val errors = mutableMapOf<String, String>()
    fun validate(): Boolean {
        errors.clear()
        if (name.isBlank()) errors["name"] = "Name is required"
        if (capacity.isBlank()) errors["capacity"] = "Capacity is required" else {
            try {
                capacity.toInt()
            } catch (e: Exception) {
                errors["capacity"] = "Capacity must be a number"
            }
        }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Evacuation Area", fontWeight = FontWeight(700)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (validate()) {
                            isLoading = true
                            val area = EvacuationArea(
                                name = name,
                                capacity = capacity.toInt(),
                                position = Location(lat.toDouble(), lng.toDouble()),
                                description = description.ifBlank { null },
                                isAccessible = isAccessible
                            )
                            scope.launch {
                                repository.addEvacuationArea(area)
                                onSave(area)
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
            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Area Name", color = Color(0xFFB0BEC5)) },
                isError = errors.containsKey("name"),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE53935),
                    unfocusedBorderColor = Color(0xFF424242),
                    errorBorderColor = Color(0xFFEF5350),
                    focusedContainerColor = Color(0xFF1A1A2E),
                    unfocusedContainerColor = Color(0xFF1A1A2E)
                )
            )
            if (errors["name"] != null) {
                Text(errors["name"]!!, color = Color(0xFFEF5350), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Capacity field
            OutlinedTextField(
                value = capacity,
                onValueChange = { capacity = it },
                label = { Text("Capacity (People)", color = Color(0xFFB0BEC5)) },
                isError = errors.containsKey("capacity"),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE53935),
                    unfocusedBorderColor = Color(0xFF424242),
                    errorBorderColor = Color(0xFFEF5350),
                    focusedContainerColor = Color(0xFF1A1A2E),
                    unfocusedContainerColor = Color(0xFF1A1A2E)
                )
            )
            if (errors["capacity"] != null) {
                Text(errors["capacity"]!!, color = Color(0xFFEF5350), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Coordinates row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = lat,
                    onValueChange = { lat = it },
                    label = { Text("Latitude", color = Color(0xFFB0BEC5)) },
                    isError = errors.containsKey("location"),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE53935),
                        unfocusedBorderColor = Color(0xFF424242),
                        focusedContainerColor = Color(0xFF1A1A2E),
                        unfocusedContainerColor = Color(0xFF1A1A2E)
                    )
                )
                OutlinedTextField(
                    value = lng,
                    onValueChange = { lng = it },
                    label = { Text("Longitude", color = Color(0xFFB0BEC5)) },
                    isError = errors.containsKey("location"),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE53935),
                        unfocusedBorderColor = Color(0xFF424242),
                        focusedContainerColor = Color(0xFF1A1A2E),
                        unfocusedContainerColor = Color(0xFF1A1A2E)
                    )
                )
            }
            if (errors["location"] != null) {
                Text(errors["location"]!!, color = Color(0xFFEF5350), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(8.dp))

            // Map picker button
            OutlinedButton(
                onClick = { showMapPicker = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF90CAF9))
            ) {
                Icon(Icons.Default.Map, contentDescription = "Map", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Pick Location on Map", color = Color(0xFF90CAF9))
            }

            Spacer(Modifier.height(16.dp))

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)", color = Color(0xFFB0BEC5)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE53935),
                    unfocusedBorderColor = Color(0xFF424242),
                    focusedContainerColor = Color(0xFF1A1A2E),
                    unfocusedContainerColor = Color(0xFF1A1A2E)
                )
            )

            Spacer(Modifier.height(16.dp))

            // Accessibility toggle
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Accessible", color = Color.White, fontWeight = FontWeight(700))
                    Text("Available for regular evacuation", color = Color(0xFFB0BEC5), fontSize = 12.sp)
                }
                Switch(
                    checked = isAccessible,
                    onCheckedChange = { isAccessible = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFE53935),
                        uncheckedThumbColor = Color(0xFFB0BEC5),
                        uncheckedTrackColor = Color(0xFF424242)
                    )
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Map picker dialog (simplified - would open actual map viewer)
    if (showMapPicker) {
        AlertDialog(
            onDismissRequest = { showMapPicker = false },
            title = { Text("Select Location") },
            text = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFF262637), shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF90CAF9), modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Map Viewer", color = Color.White)
                        Text("Tap to pick location (coming soon)", color = Color(0xFFB0BEC5), fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMapPicker = false }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMapPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Preview
@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun AddEvacuationAreaScreenPreview() {
    AddEvacuationAreaScreen()
}