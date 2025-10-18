package com.example.leetnote.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.leetnote.R
import com.example.leetnote.ui.components.ShadowButton
import com.example.leetnote.ui.navigation.Screen

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

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
            solvedEasy = state.solvedEasy,
            solvedMedium = state.solvedMedium,
            solvedHard = state.solvedHard,
            easyTotal = 907,
            mediumTotal = 1933,
            hardTotal = 876,
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
    solvedEasy: Int?,
    solvedMedium: Int?,
    solvedHard: Int?,
    easyTotal: Int?,
    mediumTotal: Int?,
    hardTotal: Int?,
    onLeetCodeConfirm: (String) -> Unit,
    onUploadProfileImage: (String) -> Unit,
    onDeleteProfileImage: () -> Unit,
    onUsernameChange: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }

    // Bottom sheet visibility
    var showImageSheet by remember { mutableStateOf(false) }

    // Image picker for uploading a new photo
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onUploadProfileImage(it.toString()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Profile row (image + username + level)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!profileImageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(72.dp)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clip(CircleShape)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { showImageSheet = true }
                        )
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_profile_photo),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(72.dp)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clip(CircleShape)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { showImageSheet = true }
                        )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
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
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                            .padding(2.dp)
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

            ShadowButton(
                text = "Confirm",
                onClick = { onLeetCodeConfirm(input) },
                modifier = Modifier.align(Alignment.End),
                foregroundColor = Color(0xFF7B9EFF),
                contentColor = Color.White
            )
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Difficulty circular indicators (share of total)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DifficultyIndicator(
                            label = "Easy",
                            count = solvedEasy,
                            total = easyTotal,
                            progressColor = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                        DifficultyIndicator(
                            label = "Medium",
                            count = solvedMedium,
                            total = mediumTotal,
                            progressColor = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f)
                        )
                        DifficultyIndicator(
                            label = "Hard",
                            count = solvedHard,
                            total = hardTotal,
                            progressColor = Color(0xFFF44336),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (showEditDialog) {
            UsernameEditDialog(
                currentUsername = username,
                onDismiss = { showEditDialog = false },
                onUpdate = { newUsername -> onUsernameChange(newUsername) }
            )
        }
    }

    // Bottom sheet for image actions
    ProfileImageActionSheet(
        visible = showImageSheet,
        hasImage = !profileImageUrl.isNullOrEmpty(),
        onDismiss = { showImageSheet = false },
        onUpload = {
            showImageSheet = false
            imagePickerLauncher.launch("image/*")
        },
        onRemove = {
            showImageSheet = false
            onDeleteProfileImage()
        }
    )
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

@Composable
private fun DifficultyIndicator(
    label: String,
    count: Int?,
    total: Int?,
    progressColor: Color,
    modifier: Modifier = Modifier
) {
    val c = (count ?: 0).coerceAtLeast(0)
    val tRaw = total ?: c
    val t = if (tRaw <= 0) 1 else tRaw
    val progress = (c.toFloat() / t.toFloat()).coerceIn(0f, 1f)
    val totalText = total?.toString() ?: "?"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                strokeWidth = 6.dp,
                color = progressColor,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "$c/$totalText",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ProfileImageActionSheet(
    visible: Boolean,
    hasImage: Boolean,
    onDismiss: () -> Unit,
    onUpload: () -> Unit,
    onRemove: () -> Unit,
) {
    if (!visible) return

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Button(onClick = onUpload, modifier = Modifier.fillMaxWidth()) {
                Text("Upload photo")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRemove,
                enabled = hasImage,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Remove current photo")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
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
            solvedEasy = 150,
            solvedMedium = 150,
            solvedHard = 50,
            easyTotal = 300,
            mediumTotal = 500,
            hardTotal = 100,
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
            profileImageUrl = null,
            username = "New User",
            level = 1,
            progress = 0.1f,
            leetcodeUsername = null,
            solvedCount = null,
            solvedEasy = null,
            solvedMedium = null,
            solvedHard = null,
            easyTotal = null,
            mediumTotal = null,
            hardTotal = null,
            onLeetCodeConfirm = {},
            onUploadProfileImage = {},
            onDeleteProfileImage = {},
            onUsernameChange = {}
        )
    }
}