package com.example.smartdrill.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Assemble the user dashboard, showing either clear status or an active disaster response.
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    activeDisaster: Disaster? = null,    // null = Dashboard, otherwise = Situation
    senderId: String? = null,
    locationInfo: String? = null,
    deviceInfo: String? = null,
    currentTheme: AppTheme = AppTheme.DARK_SLATE,
    onThemeChanged: (AppTheme) -> Unit = {},
    onRefresh: () -> Unit = {},
    onTabSelected: (BottomTab) -> Unit = {}
) {
    ScreenGradient(modifier, currentTheme) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                HomeHeader(currentTheme, onThemeChanged)
                Spacer(Modifier.height(20.dp))
                StatusCard(activeDisaster, senderId, locationInfo, deviceInfo, onRefresh)
                Spacer(Modifier.height(16.dp))
                MapCard(Modifier.height(300.dp))
            }
            AppBottomBar(BottomTab.HOME, onTabSelected)
        }
    }
}

// Display the no-alert state or disaster steps, with refresh action kept in the card header.
@Composable
private fun StatusCard(disaster: Disaster?, senderId: String?, locationInfo: String?, deviceInfo: String?, onRefresh: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (disaster == null) 210.dp else 0.dp)
            .clip(shape)
            .background(
                if (disaster == null) Color(0xFF1E293B).copy(alpha = 0.85f)
                else Color(0xFF7F1D1D).copy(alpha = 0.90f)
            )
            .border(
                1.dp,
                if (disaster == null) Color(0xFF334155) else Color(0xFFEF4444),
                shape
            )
            .padding(16.dp)
    ) {
        if (disaster == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34D399))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "ALL CAMPUS ZONES SECURE",
                        color = Color(0xFF34D399),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF334155)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "System active. Monitoring peer-to-peer mesh network for emergency broadcasts.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        } else {
            Column(Modifier.fillMaxWidth().padding(end = 28.dp)) {
                DisasterAlertContent(disaster, senderId, locationInfo, deviceInfo)
                Spacer(Modifier.height(4.dp))
            }
        }

        IconButton(
            onClick = onRefresh,
            modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
        ) {
            Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
        }
    }
}

// Preview the dashboard when no disaster is active.
@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}

// Preview the dashboard's active-disaster variation.
@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun HomeScreenSituationPreview() {
    HomeScreen(activeDisaster = Disaster.EARTHQUAKE)
}
