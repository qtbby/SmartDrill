package com.example.smartdrill

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.smartdrill.ui.AppTheme
import com.example.smartdrill.ui.BottomTab
import com.example.smartdrill.ui.Disaster
import com.example.smartdrill.ui.EmergencyActionsScreen
import com.example.smartdrill.ui.HomeScreen
import com.example.smartdrill.ui.MapScreen
import com.example.smartdrill.ui.theme.SmartDrillTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartDrillTheme {
                SmartDrillApp()
            }
        }
    }
}

enum class Screen { HOME, MAP, SOS }

@Composable
fun SmartDrillApp() {
    val context = LocalContext.current

    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var currentTheme by rememberSaveable { mutableStateOf(AppTheme.DARK_SLATE) }
    var activeDisaster by rememberSaveable { mutableStateOf<Disaster?>(null) }
    var activeSenderId by rememberSaveable { mutableStateOf<String?>(null) }
    var activeLocationInfo by rememberSaveable { mutableStateOf<String?>(null) }
    var activeDeviceInfo by rememberSaveable { mutableStateOf<String?>(null) }
    var lastActiveAlertId by rememberSaveable { mutableStateOf<String?>(null) }

    // Firestore live listener for active alerts from admin website (active == true)
    // Reports errors to Logcat and avoids duplicate alarm triggers on snapshot refreshes.
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("alerts")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreAlerts", "Alert listener failed with error: ${error.localizedMessage}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val activeDoc = snapshot.documents.firstOrNull { doc ->
                        val activeVal = doc.get("active")
                        activeVal == true || activeVal.toString().lowercase() == "true" || activeVal.toString() == "1"
                    }

                    if (activeDoc != null) {
                        val alertId = activeDoc.id
                        val typeStr = activeDoc.getString("type") ?: "earthquake"
                        val disaster = Disaster.entries.find { it.name.equals(typeStr, true) } ?: Disaster.EARTHQUAKE
                        val msg = activeDoc.getString("message") ?: "Emergency Alert Active"
                        val stepsList = activeDoc.get("steps") as? List<*>?
                        val customSteps = stepsList?.mapNotNull { it?.toString() }
                        val approvedBy = activeDoc.getString("approvedBy") ?: "Admin EOC"
                        val lat = activeDoc.getDouble("lat") ?: 7.313721
                        val lng = activeDoc.getDouble("lng") ?: 125.670175

                        activeDisaster = disaster
                        activeSenderId = approvedBy
                        activeLocationInfo = "$msg (Lat: %.4f, Lng: %.4f)".format(lat, lng)
                        activeDeviceInfo = EmergencyNotifier.getDeviceInfo()

                        // Trigger alarm sound and system notification ONLY ONCE per new active alert ID
                        if (lastActiveAlertId != alertId) {
                            lastActiveAlertId = alertId
                            EmergencyNotifier.showEmergencyAlert(
                                context = context,
                                disaster = disaster,
                                senderId = approvedBy,
                                locationInfo = activeLocationInfo ?: "Campus",
                                deviceInfo = activeDeviceInfo ?: "Device",
                                customSteps = customSteps
                            )
                        }
                    } else {
                        activeDisaster = null
                        activeSenderId = null
                        activeLocationInfo = null
                        activeDeviceInfo = null
                        lastActiveAlertId = null
                        EmergencyNotifier.cancelAlert(context)
                    }
                } else {
                    activeDisaster = null
                    activeSenderId = null
                    activeLocationInfo = null
                    activeDeviceInfo = null
                    lastActiveAlertId = null
                    EmergencyNotifier.cancelAlert(context)
                }
            }
        onDispose {
            listener.remove()
        }
    }

    // Request POST_NOTIFICATIONS permission on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    val db = FirebaseFirestore.getInstance()
                    val userDoc = hashMapOf(
                        "uid" to uid,
                        "role" to "user",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                    db.collection("users").document(uid).set(userDoc, SetOptions.merge())
                }
            }.addOnFailureListener { e ->
                Log.e("FirebaseAuth", "Anonymous sign-in failed: ${e.localizedMessage}", e)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun goToTab(tab: BottomTab) {
        screen = when (tab) {
            BottomTab.MAP -> Screen.MAP
            BottomTab.HOME -> Screen.HOME
            BottomTab.SOS -> Screen.SOS
        }
    }

    BackHandler(enabled = screen == Screen.MAP || screen == Screen.SOS) {
        goToTab(BottomTab.HOME)
    }

    when (screen) {
        Screen.HOME -> HomeScreen(
            activeDisaster = activeDisaster,
            senderId = activeSenderId,
            locationInfo = activeLocationInfo,
            deviceInfo = activeDeviceInfo,
            currentTheme = currentTheme,
            onThemeChanged = { currentTheme = it },
            onRefresh = { toast("Dashboard is up to date") },
            onTabSelected = { goToTab(it) }
        )

        Screen.MAP -> MapScreen(
            hazardActive = false,
            currentTheme = currentTheme,
            onThemeChanged = { currentTheme = it },
            onTabSelected = { goToTab(it) }
        )

        Screen.SOS -> EmergencyActionsScreen(
            activeDisaster = activeDisaster,
            currentTheme = currentTheme,
            onThemeChanged = { currentTheme = it },
            onDisasterSelected = { disaster ->
                toast("${disaster.label} report submitted. Waiting for admin approval.")
                goToTab(BottomTab.HOME)
            },
            onTabSelected = { goToTab(it) }
        )
    }
}
