package com.example.smartdrill.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartdrill.data.UserRepository
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (isAdmin: Boolean) -> Unit,
    onGoToSignUp: () -> Unit
) {
    val repository = remember { UserRepository() }
    val scope = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var isAdminMode by rememberSaveable { mutableStateOf(false) }

    ScreenGradient {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isAdminMode) "ADMIN PORTAL" else "SMART DRILL",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Stay Smart, Stay Safe",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = if (isAdminMode) "Administrator Sign In" else "Sign In",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(16.dp))

                    if (errorMessage != null) {
                        Surface(
                            color = Color(0xFF7F1D1D),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email", color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE53935),
                            unfocusedBorderColor = Color(0xFF424242),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Password", color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(image, contentDescription = "Toggle Password", tint = Color(0xFF94A3B8))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE53935),
                            unfocusedBorderColor = Color(0xFF424242),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter email and password."
                                return@Button
                            }
                            isLoading = true
                            scope.launch {
                                val result = repository.login(email.trim(), password)
                                isLoading = false
                                result.fold(
                                    onSuccess = { profile ->
                                        if (isAdminMode && profile.role != "admin") {
                                            errorMessage = "Access denied: Administrator privileges required."
                                            repository.logout()
                                        } else {
                                            onLoginSuccess(profile.role == "admin")
                                        }
                                    },
                                    onFailure = { err ->
                                        errorMessage = err.localizedMessage ?: "Login failed. Please check credentials."
                                    }
                                )
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(text = if (isAdminMode) "Sign In as Admin" else "Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAdminMode) "Switch to User Login" else "Admin Login",
                            color = Color(0xFF90CAF9),
                            fontSize = 12.sp,
                            modifier = Modifier.clickable {
                                isAdminMode = !isAdminMode
                                errorMessage = null
                            }
                        )

                        if (!isAdminMode) {
                            Text(
                                text = "Create Account",
                                color = Color(0xFF34D399),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onGoToSignUp() }
                            )
                        }
                    }
                }
            }
        }
    }
}
