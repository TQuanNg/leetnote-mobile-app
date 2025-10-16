package com.example.leetnote.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.leetnote.R

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    /*
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadProfileImage(it.toString())
        }
    }

     */

    Column(modifier = Modifier.fillMaxSize()) {

        // Show loading indicator
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // Show error message
        error?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }

        // Profile content
        ProfileContent(
            profileImageUrl = state.profileImageUrl,
            username = state.username,
            level = state.level,
            progress = state.progress,
            leetcodeUsername = state.leetcodeUsername,
            solvedCount = state.solvedCount,
            onLeetCodeConfirm = { viewModel.connectLeetCode(it) },
            onUploadProfileImage = { newUrl -> viewModel.uploadProfileImage(newUrl) },
            onDeleteProfileImage = { viewModel.deleteProfileImage() },
            onUsernameChange = { newUsername -> viewModel.updateUsername(newUsername) }
        )
    }
}


@Composable
fun ProfileContent(
    profileImageUrl: String?,
    username: String,
    level: Int,
    progress: Float,
    leetcodeUsername: String?,
    solvedCount: Int?,
    onLeetCodeConfirm: (String) -> Unit,
    onUploadProfileImage: (String) -> Unit,
    onDeleteProfileImage: () -> Unit,
    onUsernameChange: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDevDialog by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Profile row (image + username + level)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.default_profile_photo),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
            )
            /*
            AsyncImage(
                model = profileImageUrl,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = { /* TODO: View larger image */ },
                        onLongClick = {
                            // Example: change image URL (replace with file picker logic)
                            onUploadProfileImage("https://example.com/new_image.png")
                        }
                    )
            )

             */

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = username.ifEmpty { "No username" },
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit username",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { showEditDialog = true }
                    )
                }

                // Level + progress bar
                Text(text = "Level $level", style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                                            .fillMaxWidth(0.6f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                    color = ProgressIndicatorDefaults.linearColor,
                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // LeetCode connection section
        if (leetcodeUsername == null) {
            // First-time user: ask for LeetCode username
            Text(text = "Connect your LeetCode profile", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("LeetCode Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    //onLeetCodeConfirm(input)
                    showDevDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Confirm")
            }
        } else {
            // Show LeetCode profile
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "LeetCode: $leetcodeUsername",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Circular solved count
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { (solvedCount?.coerceAtMost(2000) ?: 0) / 2000f },
                            strokeWidth = 8.dp,
                            modifier = Modifier.size(120.dp)
                        )
                        Text(
                            text = "${solvedCount ?: 0}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("Problems Solved")
                }
            }
        }

        if (showDevDialog) {
            InDevelopmentDialog(onDismiss = { showDevDialog = false })
        }

        if (showEditDialog) {
            UsernameEditDialog(
                currentUsername = username,
                onDismiss = { showEditDialog = false },
                onUpdate = { newUsername -> onUsernameChange(newUsername) }
            )
        }
    }
}

@Composable
fun UsernameEditDialog(
    currentUsername: String,
    onDismiss: () -> Unit,
    onUpdate: (String) -> Unit
) {
    var newUsername by remember { mutableStateOf(currentUsername) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Edit Username") },
        text = {
            OutlinedTextField(
                value = newUsername,
                onValueChange = { newUsername = it },
                label = { Text("Username") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate(newUsername)
                    onDismiss()
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InDevelopmentDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Feature in Development") },
        text = { Text("This feature is currently under development. Please check back later!") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileContent(
            profileImageUrl = "https://avatars.githubusercontent.com/u/1?v=4",
            username = "Nguyen Tan",
            level = 5,
            progress = 0.65f,
            leetcodeUsername = "tanNguyen123",
            solvedCount = 350,
            onLeetCodeConfirm = {},
            onUploadProfileImage = {},
            onDeleteProfileImage = {},
            onUsernameChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenFirstTimePreview() {
    MaterialTheme {
        ProfileContent(
            profileImageUrl = "https://avatars.githubusercontent.com/u/1?v=4",
            username = "New User",
            level = 1,
            progress = 0.1f,
            leetcodeUsername = null, // 👈 triggers first-time UI
            solvedCount = null,
            onLeetCodeConfirm = {},
            onUploadProfileImage = {},
            onDeleteProfileImage = {},
            onUsernameChange = {}
        )
    }
}