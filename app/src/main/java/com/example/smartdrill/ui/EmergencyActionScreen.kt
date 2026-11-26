package com.example.smartdrill.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.*
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
import androidx.core.content.ContextCompat
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

    // Editable emergency contacts from Firestore with fallbacks
    var contacts by remember {
        mutableStateOf(
            mapOf(
                "police" to Pair("PNP", "09985987104"),
                "fire" to Pair("Fire", "09284587586"),
                "medical" to Pair("CDRRMO", "09302385937"),
                "power" to Pair("Power", "09190563572"),
                "coast-guard" to Pair("Coast Guard", "09692151869"),
                "mayors-office" to Pair("Mayor's Office", "09988107142")
            )
        )
    }

    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("emergencyContacts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && !snapshot.isEmpty) {
                    val map = contacts.toMutableMap()
                    for (doc in snapshot.documents) {
                        val id = doc.id
                        val label = doc.getString("label") ?: doc.getString("name") ?: id.uppercase()
                        val phone = doc.getString("phone") ?: "911"
                        map[id] = Pair(label, phone)
                    }
                    contacts = map
                }
            }
        onDispose {
            listener.remove()
        }
    }

    // Location permission state & launcher
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
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
                Spacer(Modifier.height(24.dp))

                Disaster.entries.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowItems.forEach { disaster ->
                            DisasterTile(
                                disaster = disaster,
                                selected = disaster == activeDisaster,
                                onClick = {
                                    if (!hasLocationPermission) {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                        Toast.makeText(context, "Location permission is required to send emergency reports.", Toast.LENGTH_LONG).show()
                                    } else {
                                        pending = disaster
                                    }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                if (activeDisaster != null) {
                    Button(
                        onClick = onClearAlert,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear alert (all safe)", color = Color.White)
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Emergency Instructions Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.9f))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "📌 EMERGENCY INSTRUCTIONS",
                            color = Color(0xFF34D399),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "1. Tap the emergency type (Flood, Earthquake, Storm, or Fire).\n2. Your precise GPS location is submitted as a PENDING report (alarms do NOT sound until admin approval).\n3. Reports are rate-limited (5-minute cooldown per user).\n4. Once verified and approved on the admin website, public alerts and evacuation routes appear instantly.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Emergency Hotlines (Panabo City)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    contacts.entries.take(4).forEach { (id, pair) ->
                        val (label, phone) = pair
                        val icon = when (id) {
                            "police" -> "👮"
                            "fire" -> "🧑‍🚒"
                            "medical" -> "🚨"
                            "power" -> "⚡"
                            "coast-guard" -> "⚓"
                            "mayors-office" -> "🏛️"
                            else -> "📞"
                        }
                        HotlineButton(icon, label) { dial(phone) }
                    }
                }
            }
            AppBottomBar(BottomTab.SOS, onTabSelected)
        }
    }

    pending?.let { disaster ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("Send ${disaster.label} report?") },
            text = { Text("Your emergency report along with your current GPS location will be sent as PENDING for administrator review. Note: Phone alarms will NOT sound until an admin approves this report.") },
            confirmButton = {
                TextButton(onClick = {
                    pending = null

                    if (!hasLocationPermission) {
                        Toast.makeText(context, "Location permission denied. Cannot submit report without coordinates.", Toast.LENGTH_LONG).show()
                        return@TextButton
                    }

                    var currentLat: Double? = null
                    var currentLng: Double? = null
                    try {
                        val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
                        val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (loc != null) {
                            currentLat = loc.latitude
                            currentLng = loc.longitude
                        }
                    } catch (_: SecurityException) {
                    } catch (_: Exception) {
                    }

                    if (currentLat == null || currentLng == null) {
                        Toast.makeText(context, "Current location unavailable. Please ensure GPS is enabled.", Toast.LENGTH_LONG).show()
                        return@TextButton
                    }

                    val auth = FirebaseAuth.getInstance()
                    val doSendReport = { uid: String ->
                        val db = FirebaseFirestore.getInstance()
                        val userRef = db.collection("users").document(uid)

                        userRef.get().addOnSuccessListener { userDoc ->
                            // Admins must not send SOS reports
                            val role = userDoc.getString("role")
                            if (role == "admin") {
                                Toast.makeText(context, "Administrators cannot submit emergency reports.", Toast.LENGTH_LONG).show()
                                return@addOnSuccessListener
                            }

                            // 5-minute cooldown check based on lastReportAt
                            val lastReportTimestamp = userDoc.getTimestamp("lastReportAt")
                            if (lastReportTimestamp != null) {
                                val elapsedMinutes = (System.currentTimeMillis() - lastReportTimestamp.toDate().time) / (1000 * 60)
                                if (elapsedMinutes < 5) {
                                    Toast.makeText(context, "Please wait before sending another report (${5 - elapsedMinutes} min remaining).", Toast.LENGTH_LONG).show()
                                    return@addOnSuccessListener
                                }
                            }

                            val reportId = db.collection("reports").document().id
                            val reportData = hashMapOf(
                                "userId" to uid,
                                "type" to disaster.name.lowercase(),
                                "lat" to currentLat,
                                "lng" to currentLng,
                                "createdAt" to FieldValue.serverTimestamp(),
                                "status" to "pending"
                            )

                            val batch = db.batch()
                            batch.set(db.collection("reports").document(reportId), reportData)
                            
                            val profileUpdates = hashMapOf<String, Any>(
                                "role" to "user",
                                "lastReportAt" to FieldValue.serverTimestamp()
                            )
                            batch.set(userRef, profileUpdates, SetOptions.merge())

                            batch.commit().addOnSuccessListener {
                                Toast.makeText(context, "Report submitted. Waiting for admin approval.", Toast.LENGTH_LONG).show()
                                // Note: DO NOT trigger alarm or onDisasterSelected here. Alarm only sounds when admin approves into alerts collection.
                            }.addOnFailureListener { err ->
                                Toast.makeText(context, "Failed to send report: ${err.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener { _ ->
                            val reportId = db.collection("reports").document().id
                            val reportData = hashMapOf(
                                "userId" to uid,
                                "type" to disaster.name.lowercase(),
                                "lat" to currentLat,
                                "lng" to currentLng,
                                "createdAt" to FieldValue.serverTimestamp(),
                                "status" to "pending"
                            )

                            val batch = db.batch()
                            batch.set(db.collection("reports").document(reportId), reportData)
                            val profileUpdates = hashMapOf<String, Any>(
                                "role" to "user",
                                "lastReportAt" to FieldValue.serverTimestamp()
                            )
                            batch.set(userRef, profileUpdates, SetOptions.merge())

                            batch.commit().addOnSuccessListener {
                                Toast.makeText(context, "Report submitted. Waiting for admin approval.", Toast.LENGTH_LONG).show()
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
            Text(icon, fontSize = 26.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

// Preview the SOS destination without an active alert.
@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun EmergencyActionsPreview() {
    EmergencyActionsScreen()
}
