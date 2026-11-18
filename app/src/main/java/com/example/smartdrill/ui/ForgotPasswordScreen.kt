package com.example.smartdrill.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Validate the recovery form locally, then ask the parent to verify identity and update the password.
@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    onReset: (fullName: String, email: String, username: String, newPassword: String) -> Unit =
        { _, _, _, _ -> },
    onBackToLogin: () -> Unit = {}
) {
    // Keep entered account details and inline validation feedback across configuration changes.
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    AuthBackground(modifier) {
        AppHeader()

        AuthCard(title = "RESET PASSWORD") {
            Text(
                text = "Enter the details you used when you signed up. They must match your account.",
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))

            AuthField("Full Name", fullName, { fullName = it })
            AuthField("Email", email, { email = it }, keyboardType = KeyboardType.Email)
            AuthField("Username", username, { username = it })
            AuthField("New Password", newPassword, { newPassword = it }, isPassword = true)
            AuthField("Confirm New Password", confirm, { confirm = it }, isPassword = true)

            error?.let {
                Text(it, color = Color(0xFFFFCDD2), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
            }

            AuthButton("Reset Password") {
                error = when {
                    fullName.isBlank() || email.isBlank() ||
                            username.isBlank() || newPassword.isBlank() -> "Please fill in all fields."
                    newPassword != confirm -> "Passwords do not match."
                    else -> null
                }
                if (error == null) {
                    onReset(fullName.trim(), email.trim(), username.trim(), newPassword)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = "Back to Login",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.clickable { onBackToLogin() }
        )

        Spacer(Modifier.height(24.dp))
        ContactSection()
    }
}

// Preview the password-recovery form layout in Android Studio.
@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen()
}