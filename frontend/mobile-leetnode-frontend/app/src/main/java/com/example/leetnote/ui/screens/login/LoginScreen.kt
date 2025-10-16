package com.example.leetnote.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.leetnote.ui.components.ShadowButton
import com.example.leetnote.ui.navigation.Screen
import kotlinx.coroutines.launch
import com.example.leetnote.R

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {

    val currentUser by viewModel.currentUser.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var localError by remember { mutableStateOf<String?>(null) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true } // remove login from backstack
            }
        }
    }

    LoginContent(
        email = email,
        onEmailChange = {
            email = it
            localError = null
                        },
        password = password,
        onPasswordChange = {
            password = it
            localError = null
                           },
        onLoginClick = {
            if (email.isBlank() || password.isBlank()) {
                localError = "Please fill in both email and password"
            } else {
                viewModel.login(email, password)
            }
                       },
        onRegisterClick = { navController.navigate(Screen.Signup.route) },
        onGoogleSignInClick = {
            coroutineScope.launch { viewModel.initGoogleLogin() }
        },
        errorMessage = localError ?: errorMessage,
        currentUserEmail = currentUser?.email
    )
}

@Composable
fun LoginContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    errorMessage: String?,
    currentUserEmail: String?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00BFA5), // teal-ish
                        Color(0xFF1976D2)  // deep blue
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.leetnote_app_mobile_icon),
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp)
            )
            Text(
                text = "LeetNote",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // White card for login form
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Login")
                    }

                    ShadowButton(
                        text = "Sign up",
                        onClick = onRegisterClick,
                        modifier = Modifier.fillMaxWidth(),
                        foregroundColor = Color(0xFF34C759),
                        contentColor = Color.White
                    )

                    ShadowButton(
                        text = "Sign in with Google",
                        onClick = onGoogleSignInClick,
                        modifier = Modifier.fillMaxWidth(),
                        foregroundColor = Color(0xFF4285F4),
                        contentColor = Color.White
                    )

                    errorMessage?.let { Text(it, color = Color.Red) }
                    currentUserEmail?.let { Text("Logged in as: $it") }
                }
            }
        }
    }
}