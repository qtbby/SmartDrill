package com.example.smartdrill.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Let a user select and confirm a disaster alert, or clear the currently active alert.
@Composable
fun EmergencyActionsScreen(
    modifier: Modifier = Modifier,
    activeDisaster: Disaster? = null,
    onDisasterSelected: (Disaster) -> Unit = {},
    onClearAlert: () -> Unit = {},
    onTabSelected: (BottomTab) -> Unit = {}
) {
    val context = LocalContext.current

    fun dial(number: String) {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
    }

    // Keep the selection pending until the user confirms the broadcast in the dialog.
    var pending by remember { mutableStateOf<Disaster?>(null) }

    ScreenGradient(modifier) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "EMERGENCY ACTIONS!",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Watch your dashboard!",
                    color = Color.White,
                    fontSize = 11.sp,
                    textDecoration = TextDecoration.Underline
                )
                Spacer(Modifier.height(28.dp))

                Disaster.entries.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowItems.forEach { disaster ->
                            DisasterTile(
                                disaster = disaster,
                                selected = disaster == activeDisaster,
                                onClick = { pending = disaster }
                            )
                        }
                    }
                    Spacer(Modifier.height(28.dp))
                }

                if (activeDisaster != null) {
                    Button(
                        onClick = onClearAlert,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear alert (all safe)", color = Color.White)
                    }
                    Spacer(Modifier.height(20.dp))
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Emergency Hotlines",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HotlineButton("👮", "PNP") { dial("09985987104") }
                    HotlineButton("🧑‍🚒", "Fire") { dial("09284587586") }
                    HotlineButton("🚨", "CDRRMO") { dial("09302385937") }
                    HotlineButton("⚡", "Power") { dial("09190563572") }
                }
            }
            AppBottomBar(BottomTab.SOS, onTabSelected)
        }
    }

    pending?.let { disaster ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("Send ${disaster.label} alert?") },
            text = { Text("Users will see this alert and the safety steps on their dashboard.") },
            confirmButton = {
                TextButton(onClick = {
                    pending = null
                    onDisasterSelected(disaster)
                }) { Text("Send alert") }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) { Text("Cancel") }
            }
        )
    }
}

// Render one disaster choice and highlight it when it matches the active alert.
@Composable
private fun DisasterTile(disaster: Disaster, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier.width(150.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(shape)
                .background(Color(0xFF9E2A2B))
                .border(
                    if (selected) 3.dp else 1.dp,
                    if (selected) Color.White else Color.White.copy(alpha = 0.25f),
                    shape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(disaster.emoji, fontSize = 56.sp)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = disaster.label,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp
        )
    }
}

// Present one hotline shortcut.
@Composable
private fun HotlineButton(icon: String, label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(shape)
                .background(Color.White.copy(alpha = 0.15f))
                .border(1.dp, Color.White.copy(alpha = 0.3f), shape),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 28.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// Preview the SOS destination without an active alert.
@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun EmergencyActionsPreview() {
    EmergencyActionsScreen()
}
