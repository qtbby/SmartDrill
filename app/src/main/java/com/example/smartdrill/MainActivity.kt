package com.example.smartdrill

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.smartdrill.ui.BottomTab
import com.example.smartdrill.ui.Disaster
import com.example.smartdrill.ui.EmergencyActionsScreen
import com.example.smartdrill.ui.HomeScreen
import com.example.smartdrill.ui.MapScreen
import com.example.smartdrill.ui.theme.SmartDrillTheme

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
    var currentUser by rememberSaveable { mutableStateOf("User") }
    var activeDisaster by rememberSaveable { mutableStateOf<Disaster?>(null) }
    var activeSenderId by rememberSaveable { mutableStateOf<String?>(null) }
    var activeLocationInfo by rememberSaveable { mutableStateOf<String?>(null) }
    var activeDeviceInfo by rememberSaveable { mutableStateOf<String?>(null) }

    // Initialize Google Nearby Connections Mesh Manager
    val meshManager = remember {
        NearbyMeshManager(context) { disasterType, senderId, locationInfo, deviceInfo ->
            val disaster = Disaster.entries.find { it.name.equals(disasterType, true) } ?: Disaster.EARTHQUAKE
            activeDisaster = disaster
            activeSenderId = senderId
            activeLocationInfo = locationInfo
            activeDeviceInfo = deviceInfo
            EmergencyNotifier.showEmergencyAlert(context, disaster, senderId, locationInfo, deviceInfo)
        }
    }

    // Request POST_NOTIFICATIONS permission on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        meshManager.startMeshService()
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
            userName = currentUser,
            activeDisaster = activeDisaster,
            senderId = activeSenderId,
            locationInfo = activeLocationInfo,
            deviceInfo = activeDeviceInfo,
            onNotificationsClick = { toast("No new notifications") },
            onRefresh = { toast("Dashboard is up to date") },
            onTabSelected = { goToTab(it) },
            onLogout = { toast("Smart Drill is active") }
        )

        Screen.MAP -> MapScreen(
            userName = currentUser,
            onNotificationsClick = { toast("No new notifications") },
            onTabSelected = { goToTab(it) },
            onLogout = { toast("Smart Drill is active") }
        )

        Screen.SOS -> EmergencyActionsScreen(
            activeDisaster = activeDisaster,
            onDisasterSelected = { disaster ->
                if (!EmergencyNotifier.isInsideCampus(context)) {
                    toast("❌ SOS Denied: You are outside campus! False alarms are strictly prohibited.")
                } else {
                    val senderId = EmergencyNotifier.getSenderId(context)
                    val deviceInfo = EmergencyNotifier.getDeviceInfo()
                    val locationInfo = "DNSC Campus Grounds (GPS Active)"
                    activeDisaster = disaster
                    activeSenderId = senderId
                    activeLocationInfo = locationInfo
                    activeDeviceInfo = deviceInfo
                    
                    // Broadcast SOS over offline Bluetooth/Wi-Fi mesh to nearby phones
                    meshManager.broadcastSOS(disaster.name, senderId, locationInfo, deviceInfo)
                    
                    EmergencyNotifier.showEmergencyAlert(context, disaster, senderId, locationInfo, deviceInfo)
                    toast("${disaster.label} alert sent by $senderId")
                    goToTab(BottomTab.HOME)
                }
            },
            onClearAlert = {
                activeDisaster = null
                activeSenderId = null
                activeLocationInfo = null
                activeDeviceInfo = null
                EmergencyNotifier.cancelAlert(context)
                toast("Alert cleared")
                goToTab(BottomTab.HOME)
            },
            onTabSelected = { goToTab(it) }
        )
    }
}
