package com.example.smartdrill.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

// Let a user select and confirm a disaster alert, or clear the currently active alert.
@Composable
fun EmergencyActionsScreen(
    modifier: Modifier = Modifier,
    activeDisaster: Disaster? = null,
    currentTheme: AppTheme = AppTheme.DARK_SLATE,
    onThemeChanged: (AppTheme) -> Unit = {},
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

    ScreenGradient(modifier, currentTheme) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                HomeHeader(currentTheme, onThemeChanged)
                Spacer(Modifier.height(16.dp))
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
            title = { Text("Send ${disaster.label} report?") },
            text = { Text("Your emergency report will be instantly sent to administrators for verification. No login required.") },
            confirmButton = {
                TextButton(onClick = {
                    pending = null
                    val auth = FirebaseAuth.getInstance()
                    val doSendReport = { uid: String ->
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(uid).get().addOnSuccessListener { userDoc ->
                            val lastReportTimestamp = userDoc.getTimestamp("lastReportAt")
                            if (lastReportTimestamp != null) {
                                val elapsedMinutes = (System.currentTimeMillis() - lastReportTimestamp.toDate().time) / (1000 * 60)
                                if (elapsedMinutes < 5) {
                                    Toast.makeText(context, "Please wait before sending another report (${5 - elapsedMinutes} min remaining).", Toast.LENGTH_LONG).show()
                                    return@addOnSuccessListener
                                }
                            }

                            val reportId = "rep_${System.currentTimeMillis()}"
                            val reportData = hashMapOf(
                                "userId" to uid,
                                "type" to disaster.name.lowercase(),
                                "lat" to 7.3135,
                                "lng" to 125.6703,
                                "createdAt" to FieldValue.serverTimestamp(),
                                "status" to "pending"
                            )

                            val batch = db.batch()
                            batch.set(db.collection("reports").document(reportId), reportData)
                            batch.update(db.collection("users").document(uid), "lastReportAt", FieldValue.serverTimestamp())
                            batch.commit().addOnSuccessListener {
                                Toast.makeText(context, "Report sent. Waiting for admin approval.", Toast.LENGTH_LONG).show()
                                onDisasterSelected(disaster)
                            }.addOnFailureListener { _ ->
                                db.collection("users").document(uid).set(hashMapOf("role" to "user", "lastReportAt" to FieldValue.serverTimestamp()), SetOptions.merge())
                                db.collection("reports").document(reportId).set(reportData).addOnSuccessListener {
                                    Toast.makeText(context, "Report sent. Waiting for admin approval.", Toast.LENGTH_LONG).show()
                                    onDisasterSelected(disaster)
                                }.addOnFailureListener { err ->
                                    Toast.makeText(context, "Failed to send report: ${err.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }.addOnFailureListener { _ ->
                            // Fallback if user doc doesn't exist yet
                            val reportId = "rep_${System.currentTimeMillis()}"
                            val reportData = hashMapOf(
                                "userId" to uid,
                                "type" to disaster.name.lowercase(),
                                "lat" to 7.3135,
                                "lng" to 125.6703,
                                "createdAt" to FieldValue.serverTimestamp(),
                                "status" to "pending"
                            )
                            db.collection("reports").document(reportId).set(reportData).addOnSuccessListener {
                                Toast.makeText(context, "Report sent. Waiting for admin approval.", Toast.LENGTH_LONG).show()
                                onDisasterSelected(disaster)
                            }.addOnFailureListener { err ->
                                Toast.makeText(context, "Failed to send report: ${err.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    val currentUser = auth.currentUser
                    if (currentUser == null) {
                        auth.signInAnonymously().addOnSuccessListener { result ->
                            val uid = result.user?.uid ?: "anonymous"
                            doSendReport(uid)
                        }.addOnFailureListener { e ->
                            Toast.makeText(context, "Quick authentication failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        doSendReport(currentUser.uid)
                    }
                }) { Text("Send report") }
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
                .background(Color(0xFF334155))
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
