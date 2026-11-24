package com.example.smartdrill.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    onRefresh: () -> Unit = {},
    onTabSelected: (BottomTab) -> Unit = {}
) {
    ScreenGradient(modifier) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                HomeHeader()
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
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (disaster == null) 210.dp else 0.dp)
            .clip(shape)
            .background(
                if (disaster == null) Color.White.copy(alpha = 0.15f)
                else Color(0xFFB71C1C).copy(alpha = 0.35f)
            )
            .border(1.dp, Color.White.copy(alpha = 0.3f), shape)
            .padding(14.dp)
    ) {
        if (disaster == null) {
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "NO DISASTER HAS BEEN DETECTED.",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f).padding(end = 28.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 56.dp, height = 70.dp)
                    .border(3.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    ":)",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
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
            modifier = Modifier.align(Alignment.TopEnd).size(28.dp)
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
