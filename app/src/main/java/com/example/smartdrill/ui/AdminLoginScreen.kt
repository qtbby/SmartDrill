package com.example.smartdrill.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.smartdrill.admin.AdminAuthManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Admin login screen for accessing the admin module.
 * Provides authentication for safety officers and administrators.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val authManager = remember { AdminAuthManager.getInstance(context) }

    LaunchedEffect(isLoading) {
        if (!isLoading && errorMessage == null && username.isNotEmpty() && password.isNotEmpty()) {
            delay(300) // Brief delay to show loading
            val result = authManager.login(username, password)
            if (result != null) {
                isLoading = false
                errorMessage = null
                onLoginSuccess()
            } else {
                errorMessage = "Invalid username or password"
                isLoading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button
        Row(
            modifier = Modifier.align(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.clickable { onBack() }
            )
            Spacer(Modifier.width(8.dp))
            Text("Back to App", color = Color.White, fontSize = 14.sp)
        }

        Spacer(Modifier.height(40.dp))

        // Logo/Title
        Text(
            text = "Smart Drill Admin",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Safety Officer Portal",
            color = Color(0xFFE0E0E0),
            fontSize = 14.sp
        )

        Spacer(Modifier.height(40.dp))

        // Login Form
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username", color = Color(0xFFB0BEC5)) },
            placeholder = { Text("Enter username", color = Color(0xFFB0BEC5)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE53935),
                unfocusedBorderColor = Color(0xFF424242),
                focusedLabelColor = Color(0xFFE53935),
                unfocusedLabelColor = Color(0xFFB0BEC5),
                focusedContainerColor = Color(0xFF1A1A2E),
                unfocusedContainerColor = Color(0xFF1A1A2E)
            )
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color(0xFFB0BEC5)) },
            placeholder = { Text("Enter password", color = Color(0xFFB0BEC5)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE53935),
                unfocusedBorderColor = Color(0xFF424242),
                focusedLabelColor = Color(0xFFE53935),
                unfocusedLabelColor = Color(0xFFB0BEC5),
                focusedContainerColor = Color(0xFF1A1A2E),
                unfocusedContainerColor = Color(0xFF1A1A2E)
            )
        )

        Spacer(Modifier.height(12.dp))

        // Error message
        errorMessage?.let {
            Text(
                text = it,
                color = Color(0xFFEF5350),
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(24.dp))

        // Login button
        Button(
            onClick = {
                isLoading = true
                errorMessage = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
            enabled = !isLoading && username.isNotEmpty() && password.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "LOGIN",
                    color = Color.White,
                    fontWeight = FontWeight(700),
                    fontSize = 16.sp
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Demo credentials
        Text(
            text = "Demo Credentials:",
            color = Color(0xFFB0BEC5),
            fontSize = 12.sp,
            fontWeight = FontWeight(500)
        )
        Spacer(Modifier.height(8.dp))
        CredentialCard("admin / admin123", "Super Admin")
        CredentialCard("officer / officer123", "Safety Officer")
        CredentialCard("manager / manager123", "Campus Manager")
    }
}

@Composable
private fun CredentialCard(credentials: String, role: String) {
    Text(
        text = "$credentials — $role",
        color = Color(0xFF90CAF9),
        fontSize = 12.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

// Preview
@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun AdminLoginScreenPreview() {
    AdminLoginScreen()
}