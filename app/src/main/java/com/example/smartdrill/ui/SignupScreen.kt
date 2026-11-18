package com.example.smartdrill.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Collect new account details, validate required fields/password confirmation, then submit.
@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    onSignUp: (fullName: String, email: String, username: String, password: String) -> Unit =
        { _, _, _, _ -> },
    onGoToLogin: () -> Unit = {},
    onGoogleClick: () -> Unit = {}
) {
    // Keep form values and validation feedback when the screen is recreated.
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    AuthBackground(modifier) {
        AppHeader()

        AuthCard(title = "CREATE ACCOUNT") {
            AuthField("Full Name", fullName, { fullName = it })
            AuthField("Email", email, { email = it }, keyboardType = KeyboardType.Email)
            AuthField("Username", username, { username = it })
            AuthField("Password", password, { password = it }, isPassword = true)
            AuthField("Confirm Password", confirm, { confirm = it }, isPassword = true)

            error?.let {
                Text(it, color = Color(0xFFFFCDD2), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
            }

            AuthButton("Sign Up") {
                error = when {
                    fullName.isBlank() || email.isBlank() ||
                            username.isBlank() || password.isBlank() -> "Please fill in all fields."
                    password != confirm -> "Passwords do not match."
                    else -> null
                }
                if (error == null) onSignUp(fullName, email, username, password)
            }

            Spacer(Modifier.height(12.dp))
            GoogleButton(onGoogleClick)

            Spacer(Modifier.height(12.dp))
            Row {
                Text("Already have an account? ", color = Color.White, fontSize = 13.sp)
                Text(
                    "Login",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onGoToLogin() }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        ContactSection()
    }
}

// Preview the account-creation form in Android Studio.
@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen()
}