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
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onGoToLogin: () -> Unit
) {
    val repository = remember { UserRepository() }
    val scope = rememberCoroutineScope()

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

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
                text = "SMART DRILL",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Create Account",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(24.dp))

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
                        text = "Sign Up",
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
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        label = { Text("Full Name", color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF34D399),
                            unfocusedBorderColor = Color(0xFF424242),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        label = { Text("Username", color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF34D399),
                            unfocusedBorderColor = Color(0xFF424242),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email", color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF34D399),
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
                        label = { Text("Password (6+ characters)", color = Color(0xFF94A3B8)) },
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
                            focusedBorderColor = Color(0xFF34D399),
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
                            if (name.isBlank() || email.isBlank() || username.isBlank() || password.isBlank()) {
                                errorMessage = "Please fill in all fields."
                                return@Button
                            }
                            if (password.length < 6) {
                                errorMessage = "Password must be at least 6 characters."
                                return@Button
                            }
                            isLoading = true
                            scope.launch {
                                val result = repository.signUp(email.trim(), password, name.trim(), username.trim())
                                isLoading = false
                                result.fold(
                                    onSuccess = { onSignUpSuccess() },
                                    onFailure = { err ->
                                        errorMessage = err.localizedMessage ?: "Sign up failed. Please try again."
                                    }
                                )
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(text = "Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF064E3B))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already have an account? ", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(
                            text = "Sign In",
                            color = Color(0xFF90CAF9),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onGoToLogin() }
                        )
                    }
                }
            }
        }
    }
}
