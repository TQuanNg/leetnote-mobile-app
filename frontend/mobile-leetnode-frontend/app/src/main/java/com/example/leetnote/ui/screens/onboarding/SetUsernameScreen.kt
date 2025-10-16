package com.example.leetnote.ui.screens.onboarding

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SetUsernameScreen(
    viewModel: SetUsernameViewModel = hiltViewModel(),
    token: String,
    onUsernameSet: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Set Your Username", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.username,
            onValueChange = { viewModel.onUsernameChanged(it) },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.confirmUsername(
                    token,
                    onSuccess = onUsernameSet,
                    onError = { Log.e("SetUsername", it) }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.username.isNotBlank()
        ) {
            Text("Confirm")
        }
    }
}


@Preview
@Composable
fun SetUsernameScreenPreview() {
    //SetUsernameScreen()
}