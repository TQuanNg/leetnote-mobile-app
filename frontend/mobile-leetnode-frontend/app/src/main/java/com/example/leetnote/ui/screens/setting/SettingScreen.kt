package com.example.leetnote.ui.screens.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.leetnote.ui.screens.login.AuthViewModel
import com.google.firebase.auth.FirebaseUser
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    settingViewModel: SettingViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate("login") {
                popUpTo("profile") { inclusive = true }
            }
        }
    }

    SettingContent(
        currentUser = currentUser,
        onLogout = { authViewModel.logout() },
        settingViewModel = settingViewModel
    )
}

@Composable
fun SettingContent(
    currentUser: FirebaseUser?,
    onLogout: () -> Unit,
    settingViewModel: SettingViewModel
) {
    val isDarkTheme by settingViewModel.isDarkTheme.collectAsState()
    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // Account info
        if (currentUser != null) {
            SettingItem(
                title = "Email",
                value = currentUser.email ?: "Unknown",
                onClick = { /* show dialog or copy email */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onLogout) {
                Text("Sign Out")
            }
        } else {
            Text("You are not signed in")
        }

        Spacer(modifier = Modifier.height(32.dp))

        ThemeToggle(
            isDarkTheme = isDarkTheme,
            onThemeChange = { settingViewModel.setTheme(it) }
        )

        NotificationToggle(
            enabled = notificationsEnabled,
            onToggle = { notificationsEnabled = it }
        )
    }
}

@Composable
fun SettingItem(title: String, value: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ThemeToggle(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onThemeChange(!isDarkTheme) }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Dark Theme")
        Switch(
            checked = isDarkTheme,
            onCheckedChange = onThemeChange
        )
    }
}

@Composable
fun NotificationToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!enabled) }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Enable Notifications")
        Switch(
            checked = enabled,
            onCheckedChange = onToggle
        )
    }
}