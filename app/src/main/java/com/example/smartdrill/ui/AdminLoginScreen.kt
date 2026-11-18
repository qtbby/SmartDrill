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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

// Collect administrator credentials and pass them to the app-level authentication handler.
@Composable
fun AdminLoginScreen(
    modifier: Modifier = Modifier,
    onLogin: (username: String, password: String) -> Unit = { _, _ -> },
    onBackToLogin: () -> Unit = {}
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    AuthBackground(modifier) {
        AppHeader()

        AuthCard(title = "ADMIN PORTAL") {
            AuthField("Admin Username", username, { username = it })
            AuthField("Password", password, { password = it }, isPassword = true)

            Spacer(Modifier.height(16.dp))

            AuthButton("Login as Admin") { onLogin(username, password) }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Back to User Login",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onBackToLogin() }
            )
        }
    }
}

// Administrator dashboard with shared alerts, emergency actions, and quick hotline shortcuts.
@Composable
fun AdminHomeScreen(
    modifier: Modifier = Modifier,
    adminName: String = "Admin",
    activeDisaster: Disaster? = null,
    // Placeholder numbers: replace them with your local hotlines
    policeNumber: String = "911",
    fireNumber: String = "911",
    medicalNumber: String = "911",
    onNotificationsClick: () -> Unit = {},
    onEmergencyAction: () -> Unit = {},
    onTabSelected: (BottomTab) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current

    // Opens the phone dialer with the number filled in (does not call automatically)
    fun dial(number: String) {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
    }

    // Keep the dashboard content scrollable while the shared navigation bar stays visible.
    ScreenGradient(modifier) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                HomeHeader(adminName, onNotificationsClick, onLogout)
                Spacer(Modifier.height(20.dp))

                AdminAlertCard(activeDisaster)
                Spacer(Modifier.height(20.dp))

                EmergencyActionButton(onEmergencyAction)
                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Quick Button Emergency Hotline",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HotlineButton("👮") { dial(policeNumber) }
                    HotlineButton("🧑‍🚒") { dial(fireNumber) }
                    HotlineButton("⚕️") { dial(medicalNumber) }
                }
            }
            AppBottomBar(BottomTab.HOME, onTabSelected)
        }
    }
}

// Show either the active alert and its steps or the clear/no-disaster confirmation.
@Composable
private fun AdminAlertCard(disaster: Disaster?) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.15f))
            .border(1.dp, Color.White.copy(alpha = 0.4f), shape)
            .padding(14.dp)
    ) {
        if (disaster == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(
                    "NO DISASTER HAS BEEN DETECTED.",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        } else {
            DisasterAlertContent(disaster)
            Spacer(Modifier.height(8.dp))
        }
    }
}

// Send an administrator to the emergency alert workflow.
@Composable
private fun EmergencyActionButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.15f))
            .border(1.dp, Color.White.copy(alpha = 0.4f), shape)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🚨", fontSize = 44.sp)
        Spacer(Modifier.width(24.dp))
        Text(
            text = "EMERGENCY ACTION BUTTON",
            color = Color.White.copy(alpha = 0.95f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

// Present one hotline shortcut; its parent supplies the action for the selected service.
@Composable
private fun HotlineButton(icon: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(icon, fontSize = 34.sp)
    }
}

// Preview the administrator dashboard with an example active alert in Android Studio.
@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun AdminHomeScreenPreview() {
    AdminHomeScreen(activeDisaster = Disaster.EARTHQUAKE)
}