package com.example.smartdrill.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Destinations available from the app's persistent bottom navigation bar.
enum class BottomTab { MAP, HOME, SOS }

// Alert categories and their presentation copy, used consistently by dashboards and SOS flows.
enum class Disaster(val label: String, val emoji: String, val steps: List<String>) {
    FLOOD(
        "FLOOD", "🌊",
        listOf(
            "Move to higher ground immediately",
            "Avoid walking or driving through floodwater",
            "Follow the nearest safety PATHWAY to the Evacuation Area"
        )
    ),
    EARTHQUAKE(
        "EARTHQUAKE", "🏚️",
        listOf(
            "Don't Panic, Duck, Cover, and Hold",
            "Go to the designated Evacuation Area",
            "Follow the nearest safety PATHWAY."
        )
    ),
    STORM(
        "STORM", "⛈️",
        listOf(
            "Stay indoors and away from windows",
            "Keep your phone charged and your emergency kit ready",
            "Evacuate to the designated Evacuation Area if told to"
        )
    ),
    FIRE(
        "FIRE", "🔥",
        listOf(
            "Stay low and cover your nose and mouth",
            "Leave the building using the nearest safe exit",
            "Go to the designated Evacuation Area and call the fire hotline"
        )
    );

    val alertTitle: String
        // Convert the enum label into the title displayed on an active disaster alert.
        get() = label.lowercase().replaceFirstChar { it.uppercase() } + " Detected"
}

// Shared full-screen background wrapper for the main dashboard and emergency destinations.
@Composable
fun ScreenGradient(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
                )
            ),
        content = content
    )
}

// App header showing the app name.
@Composable
fun HomeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Smart Drill",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Emergency Alert System",
                color = Color.White,
                fontSize = 11.sp
            )
        }
    }
}

// Route taps from the persistent navigation bar to the selected app destination.
@Composable
fun AppBottomBar(selected: BottomTab, onTabSelected: (BottomTab) -> Unit) {
    val shape = RoundedCornerShape(32.dp)
    val active = Color(0xFF4A0E0E)
    val idle = Color.White.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(shape)
            .background(Color(0xFF8B1A1A).copy(alpha = 0.75f))
            .border(1.dp, Color.White.copy(alpha = 0.25f), shape)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.LocationOn, "Map",
            tint = if (selected == BottomTab.MAP) active else idle,
            modifier = Modifier
                .size(if (selected == BottomTab.MAP) 44.dp else 36.dp)
                .clickable { onTabSelected(BottomTab.MAP) }
        )
        Icon(
            Icons.Default.Home, "Home",
            tint = if (selected == BottomTab.HOME) active else idle,
            modifier = Modifier
                .size(if (selected == BottomTab.HOME) 44.dp else 36.dp)
                .clickable { onTabSelected(BottomTab.HOME) }
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .border(
                    3.dp,
                    if (selected == BottomTab.SOS) active else Color.White.copy(alpha = 0.85f),
                    CircleShape
                )
                .clip(CircleShape)
                .clickable { onTabSelected(BottomTab.SOS) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "SOS",
                color = if (selected == BottomTab.SOS) active else Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
    }
}

// Alert title + safety steps (used by the user Dashboard and the Admin Home)
// Call it inside a Column.
@Composable
fun DisasterAlertContent(disaster: Disaster, senderId: String? = null, locationInfo: String? = null, deviceInfo: String? = null) {
    // Pair the alert title with a warning marker, then list the recommended response steps.
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(Color(0xFFE53935))
                .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = disaster.alertTitle,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
        )
    }
    if (senderId != null) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "👤 Sender ID: $senderId",
            color = Color(0xFFFFEB3B),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 30.dp)
        )
    }
    if (deviceInfo != null) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = "📱 Device: $deviceInfo",
            color = Color.White.copy(alpha = 0.95f),
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 30.dp)
        )
    }
    if (locationInfo != null) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = "📍 Location: $locationInfo",
            color = Color.White.copy(alpha = 0.95f),
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 30.dp)
        )
    }
    disaster.steps.forEach { step ->
        Spacer(Modifier.height(12.dp))
        Row(Modifier.padding(start = 30.dp)) {
            Text("•", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.width(10.dp))
            Text(
                text = step,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}