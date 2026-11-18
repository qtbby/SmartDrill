package com.example.smartdrill.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Collect user credentials and expose navigation to registration, recovery, admin, and demo flows.
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLogin: (username: String, password: String) -> Unit = { _, _ -> },
    onGoToSignUp: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onAdminLogin: () -> Unit = {},
    onForgotPassword: () -> Unit = {}
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    AuthBackground(modifier) {
        AppHeader()

        AuthCard(title = "DRILLSMART") {
            AuthField("Username", username, { username = it })
            AuthField("Password", password, { password = it }, isPassword = true)

            Text(
                text = "Forgot Password?",
                color = Color.White,
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable {onForgotPassword()}
            )
            Spacer(Modifier.height(12.dp))

            AuthButton("Login") { onLogin(username, password) }

            Spacer(Modifier.height(12.dp))
            Text(
                text = "Sign Up",
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.clickable { onGoToSignUp() }
            )
            Spacer(Modifier.height(12.dp))

            GoogleButton(onGoogleClick)
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = "Login as Admin",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onAdminLogin() }
        )

        Spacer(Modifier.height(24.dp))
        ContactSection()
    }
}

// Preview the user login screen in Android Studio.
@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}