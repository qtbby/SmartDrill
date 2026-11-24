package com.example.smartdrill.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartdrill.admin.EvacuationArea
import com.example.smartdrill.admin.HazardZone
import com.example.smartdrill.admin.EmergencyEvent
import com.example.smartdrill.admin.EventStatus
import com.example.smartdrill.admin.AdminRepository
import com.example.smartdrill.admin.AdminAuthManager
import kotlinx.coroutines.launch

/**
 * Admin Home Screen - Dashboard for managing evacuation areas and emergency events.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    modifier: Modifier = Modifier,
    navController: AdminNavController? = null,
    onLogout: () -> Unit = {},
    onTabSelected: (BottomTab) -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { AdminRepository.getInstance(context) }
    val authManager = remember { AdminAuthManager.getInstance(context) }

    var selectedTab by remember { mutableStateOf(0) }

    val allEvents by repository.emergencyEvents.collectAsState(initial = emptyList())
    val activeEvents = allEvents.filter { it.status == EventStatus.ACTIVE || it.isActive }
    val pendingEvents = allEvents.filter { it.status == EventStatus.PENDING }

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        // App Bar
        TopAppBar(
            title = {
                Text(
                    "Admin Portal",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF0F3460)
            ),
            actions = {
                TextButton(onClick = { onLogout() }) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("Logout", color = Color.White)
                }
            }
        )

        // Stats Bar
        AdminStatsBar(
            activeEvents = activeEvents.size,
            pendingApprovals = pendingEvents.size
        )

        // Navigation Tabs
        AdminTabRow(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            when (selectedTab) {
                0 -> AdminDashboardContent(
                    navController = navController,
                    activeEvents = activeEvents,
                    pendingEvents = pendingEvents,
                    onEventClick = { /* Handle event click */ },
                    onTabSelected = onTabSelected
                )
                1 -> EvacuationAreasScreen(
                    modifier = Modifier.fillMaxSize(),
                    onBack = { selectedTab = 0 },
                    onAddClick = { navController?.navigateTo(AdminScreen.ADD_EVACUATION_AREA) }
                )
                2 -> HazardZonesScreen(
                    modifier = Modifier.fillMaxSize(),
                    onBack = { selectedTab = 0 },
                    onAddClick = { navController?.navigateTo(AdminScreen.ADD_HAZARD_ZONE) }
                )
                3 -> EmergencyEventsScreen(
                    modifier = Modifier.fillMaxSize(),
                    onBack = { selectedTab = 0 },
                    onAddClick = { navController?.navigateTo(AdminScreen.ADD_EMERGENCY_EVENT) }
                )
            }
        }
    }
}

@Composable
private fun AdminStatsBar(
    activeEvents: Int,
    pendingApprovals: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF16213E))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem("Active Events", activeEvents)
        StatDivider()
        StatItem("Pending", pendingApprovals)
    }
}

@Composable
private fun StatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            color = Color(0xFFE53935),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color(0xFFB0BEC5),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun StatDivider() {
    VerticalDivider(
        color = Color(0xFF262637),
        thickness = 1.dp,
        modifier = Modifier.width(40.dp)
    )
}

@Composable
private fun AdminTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Dashboard", "Evacuation Areas", "Hazard Zones", "Events")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F3460))
    ) {
        tabs.forEachIndexed { index, title ->
            TextButton(
                onClick = { onTabSelected(index) },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = if (index == selectedTab) Color(0xFFE53935) else Color(0xFFB0BEC5),
                    fontWeight = if (index == selectedTab) FontWeight.Bold else FontWeight.Normal
                )
            }
            if (index < tabs.size - 1) {
                VerticalDivider(color = Color(0xFF262637), thickness = 1.dp, modifier = Modifier.width(1.dp))
            }
        }
    }
}

@Composable
private fun AdminDashboardContent(
    navController: AdminNavController?,
    activeEvents: List<EmergencyEvent>,
    pendingEvents: List<EmergencyEvent>,
    onEventClick: (EmergencyEvent) -> Unit,
    onTabSelected: (BottomTab) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(Modifier.height(16.dp))

            // Active Events Section
            Text(
                text = "Active Emergency Events",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (activeEvents.isEmpty()) {
                Text(
                    text = "✓ All clear - no active emergency events",
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp
                )
            } else {
                activeEvents.forEach { event ->
                    ActiveEventCard(event)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Quick Actions
            Text(
                text = "Quick Actions",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(Modifier.height(8.dp))
        }

        item {
            QuickActionCard(
                icon = Icons.Default.Event,
                title = "Start Drill",
                subtitle = "Initiate supervised emergency drill",
                onClick = { navController?.navigateTo(AdminScreen.ADD_EMERGENCY_EVENT) }
            )
        }

        item {
            QuickActionCard(
                icon = Icons.Default.Flag,
                title = "Report Hazard",
                subtitle = "Mark road/building as hazardous",
                onClick = { navController?.navigateTo(AdminScreen.ADD_HAZARD_ZONE) }
            )
        }

        item {
            QuickActionCard(
                icon = Icons.Default.Map,
                title = "Edit Evacuation Areas",
                subtitle = "Modify designated safe zones",
                onClick = { navController?.navigateTo(AdminScreen.EVACUATION_AREAS) }
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActiveEventCard(event: EmergencyEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.disasterType.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Started: ${event.createdAt}",
                    color = Color(0xFFE0E0E0),
                    fontSize = 12.sp
                )
            }
            Icon(
                Icons.Default.Verified,
                contentDescription = "Active",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF262637)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF90CAF9), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Color(0xFFB0BEC5), fontSize = 12.sp)
            }
        }
    }
}

// Preview
@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun AdminHomeScreenPreview() {
    AdminHomeScreen()
}
