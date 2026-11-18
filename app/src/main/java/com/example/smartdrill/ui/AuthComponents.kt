package com.example.smartdrill.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

val DarkRed = Color(0xFF8B1A1A)

// Shared scrollable gradient surface used by authentication and account-recovery screens.
@Composable
fun AuthBackground(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE53935), Color(0xFFD32F2F), Color(0xFF388E3C))
                )
            )
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

// Show the application name and safety tagline above each authentication form.
@Composable
fun AppHeader() {
    AppLogo()
    Spacer(Modifier.height(12.dp))
    Text(
        text = "SMART DRILL",
        color = Color.White,
        fontSize = 34.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp
    )
    Text(
        text = "Stay Smart, Stay Safe",
        color = Color.White,
        fontSize = 14.sp
    )
    Spacer(Modifier.height(32.dp))
}

// Place a titled form and its caller-provided controls inside the shared dark card.
@Composable
fun AuthCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(16.dp))
        content()
    }
}
// Render a labeled input, optionally masking a password and allowing it to be revealed.
@Composable
fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var showPassword by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation()
            else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType
            ),
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                            tint = Color.Gray
                        )
                    }
                }
            } else null,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(Modifier.height(12.dp))
    }
}

// Consistent full-width primary action for authentication screens.
@Composable
fun AuthButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(46.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DarkRed)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

// Present the Google sign-in affordance and forward taps to its caller.
@Composable
fun GoogleButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(46.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
    ) {
        Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Text("Continue with Google", color = Color.Black, fontWeight = FontWeight.Medium)
    }
}

// Show local contact details and icon shortcuts beneath account forms.
@Composable
fun ContactSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Contact Us:", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("0907-312-2991", color = Color.White, textAlign = TextAlign.Center)
        Text("0998-598-7104", color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text("Or", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ContactCircle { Text("f", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp) }
            ContactCircle { Icon(Icons.Default.Email, "Email", tint = Color.White) }
            ContactCircle { Icon(Icons.Default.Phone, "Phone", tint = Color.White) }
        }
    }
}

// Small circular background that keeps the contact icons visually consistent.
@Composable
private fun ContactCircle(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Black),
        contentAlignment = Alignment.Center
    ) { content() }
}