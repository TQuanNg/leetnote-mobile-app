package com.example.leetnote.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.leetnote.R
import com.example.leetnote.ui.navigation.Screen
import com.example.leetnote.ui.screens.login.AuthViewModel
import com.example.leetnote.ui.screens.onboarding.OnboardingViewModel

@Composable
fun SplashScreen(
    navController: NavController,
    onboardingViewModel: OnboardingViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isOnboardingCompleted by onboardingViewModel.isOnboardingCompleted.collectAsState(initial = null)
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(isOnboardingCompleted, currentUser) {
        when (isOnboardingCompleted) {
            null -> { /* still loading, stay on splash */
            }

            false -> {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }

            true -> {
                if (currentUser != null) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent // prevent white background
    ) {
        SplashContent()
    }
}

@Composable
fun SplashContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00BFA5), // deep navy blue
                        Color(0xFF1976D2)  // slightly lighter deep blue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.leetnote_app_mobile_icon), // your icon
            contentDescription = "App Icon",
            modifier = Modifier.size(120.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashContentPreview() {
    SplashContent()
}