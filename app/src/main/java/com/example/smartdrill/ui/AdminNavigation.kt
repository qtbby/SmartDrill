package com.example.smartdrill.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Context
import androidx.compose.ui.unit.sp
import com.example.smartdrill.admin.EvacuationArea
import com.example.smartdrill.admin.HazardZone
import com.example.smartdrill.admin.EmergencyEvent
import com.example.smartdrill.admin.HazardSeverity
import com.example.smartdrill.admin.AdminRepository
import com.example.smartdrill.admin.AdminAuthManager
import kotlinx.coroutines.launch

/**
 * Admin Module Navigation State
 * Manages the navigation flow within the admin module.
 */
enum class AdminScreen {
    LOGIN,
    HOME,
    EVACUATION_AREAS,
    HAZARD_ZONES,
    EMERGENCY_EVENTS,
    ADD_EVACUATION_AREA,
    ADD_HAZARD_ZONE,
    ADD_EMERGENCY_EVENT
}

/**
 * Admin Navigation Controller
 * Centralized navigation for admin screens.
 */
class AdminNavController {
    private val screenStack = mutableListOf<AdminScreen>()
    var currentScreen: AdminScreen = AdminScreen.LOGIN
        private set

    fun navigateTo(screen: AdminScreen) {
        screenStack.add(currentScreen)
        currentScreen = screen
    }

    fun goBack(): Boolean {
        if (screenStack.isNotEmpty()) {
            currentScreen = screenStack.removeLast()
            return true
        }
        // If no previous screen, go to LOGIN
        currentScreen = AdminScreen.LOGIN
        return false
    }

    fun navigateToLogin() {
        screenStack.clear()
        currentScreen = AdminScreen.LOGIN
    }

    fun navigateToHome() {
        screenStack.clear()
        currentScreen = AdminScreen.HOME
    }
}

/**
 * Admin Module Router
 * Main entry point for the admin module navigation.
 */
@Composable
fun AdminModule(
    modifier: Modifier = Modifier,
    navController: AdminNavController = remember { AdminNavController() },
    onLogout: () -> Unit = {},
    onTabSelected: (BottomTab) -> Unit = {}
) {
    when (navController.currentScreen) {
        AdminScreen.LOGIN -> AdminLoginScreen(
            modifier = modifier,
            onLoginSuccess = { navController.navigateToHome() },
            onBack = { onLogout() }
        )

        AdminScreen.HOME -> {
            val context = LocalContext.current
            AdminHomeScreen(
                modifier = modifier,
                navController = navController,
                onLogout = {
                    val authManager = AdminAuthManager.getInstance(context)
                    authManager.logout()
                    onLogout()
                },
                onTabSelected = onTabSelected
            )
        }

        AdminScreen.EVACUATION_AREAS -> {
            EvacuationAreasScreen(
                modifier = modifier,
                onBack = { navController.goBack() }
            )
        }

        AdminScreen.HAZARD_ZONES -> {
            HazardZonesScreen(
                modifier = modifier,
                onBack = { navController.goBack() }
            )
        }

        AdminScreen.EMERGENCY_EVENTS -> {
            EmergencyEventsScreen(
                modifier = modifier,
                onBack = { navController.goBack() }
            )
        }

        AdminScreen.ADD_EVACUATION_AREA -> {
            AddEvacuationAreaScreen(
                modifier = modifier,
                onSave = {
                    navController.navigateToHome()
                },
                onCancel = { navController.goBack() }
            )
        }

        AdminScreen.ADD_HAZARD_ZONE -> {
            AddHazardZoneScreen(
                modifier = modifier,
                onSave = {
                    navController.navigateToHome()
                },
                onCancel = { navController.goBack() }
            )
        }

        AdminScreen.ADD_EMERGENCY_EVENT -> {
            AddEmergencyEventScreen(
                modifier = modifier,
                onSave = {
                    navController.navigateToHome()
                },
                onCancel = { navController.goBack() }
            )
        }
    }
}

/**
 * Check if the user is authenticated and should see the admin module.
 */
fun isAdminAuthenticated(context: android.content.Context): Boolean {
    val authManager = AdminAuthManager.getInstance(context)
    return authManager.isLoggedIn()
}

/**
 * Get the current admin user.
 */
fun getCurrentAdmin(context: android.content.Context) = AdminAuthManager.getInstance(context).getCurrentAdmin()