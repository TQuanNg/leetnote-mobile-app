package com.example.leetnote.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import com.example.leetnote.utils.PermissionUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import com.example.leetnote.data.model.EvaluationListItemDTO

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    navController: NavController
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
            selectedTabIndex = state.selectedTabIndex,
            evaluations = state.evaluations,
            onLeetCodeConnect = { viewModel.setLeetCodeUsername(it) },
            onLeetCodeUpdate = { viewModel.updateLeetCodeUsername(it) },
            onLeetCodeRefresh = { viewModel.refreshLeetCodeStats() },
            onUploadProfileImage = { newUrl -> viewModel.uploadProfileImage(newUrl) },
            onDeleteProfileImage = { viewModel.deleteProfileImage() },
            onUsernameChange = { newUsername -> viewModel.updateUsername(newUsername) },
            onTabSelected = { newIndex ->
                viewModel.updateTabIndex(newIndex)
                // Load evaluations when Evaluations tab is selected
                if (newIndex == 1) {
                    viewModel.loadAllUserEvaluations()
                }
            },
            onEvaluationClick = { evaluationId ->
                // Find the evaluation to get problemId
                val evaluation = state.evaluations.find { it.evaluationId == evaluationId }
                evaluation?.let {
                    navController.navigate(Screen.EvaluationDetail.createRoute(it.problemId, evaluationId))
                }
            }
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
    selectedTabIndex: Int,
    evaluations: List<EvaluationListItemDTO>,
    onLeetCodeConnect: (String) -> Unit,
    onLeetCodeUpdate: (String) -> Unit,
    onLeetCodeRefresh: () -> Unit,
    onUploadProfileImage: (String) -> Unit,
    onDeleteProfileImage: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onTabSelected: (Int) -> Unit,
    onEvaluationClick: (Long) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Profile header section
        ProfileHeader(
            profileImageUrl = profileImageUrl,
            username = username,
            level = level,
            progress = progress,
            onUploadProfileImage = onUploadProfileImage,
            onDeleteProfileImage = onDeleteProfileImage,
            onEditUsername = { showEditDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Neo-brutalism styled tab row
        NeoBrutalismTabRow(
            selectedTabIndex = selectedTabIndex,
            onTabSelected = onTabSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on selected tab
        when (selectedTabIndex) {
            0 -> {
                // LeetCode content
                if (leetcodeUsername == null) {
                    LeetCodeConnectionSection(
                        onLeetCodeConnect = onLeetCodeConnect
                    )
                } else {
                    LeetCodeStatsSection(
                        leetcodeUsername = leetcodeUsername,
                        solvedCount = solvedCount,
                        solvedEasy = solvedEasy,
                        solvedMedium = solvedMedium,
                        solvedHard = solvedHard,
                        easyTotal = easyTotal,
                        mediumTotal = mediumTotal,
                        hardTotal = hardTotal,
                        onLeetCodeUpdate = onLeetCodeUpdate,
                        onLeetCodeRefresh = onLeetCodeRefresh
                    )
                }
            }
            1 -> {
                // Evaluations content
                EvaluationsSection(
                    evaluations = evaluations,
                    onEvaluationClick = onEvaluationClick
                )
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
}

@Composable
private fun ProfileHeader(
    profileImageUrl: String?,
    username: String,
    level: Int,
    progress: Float,
    onUploadProfileImage: (String) -> Unit,
    onDeleteProfileImage: () -> Unit,
    onEditUsername: () -> Unit
) {
    var showImageSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Pass the URI as string to the ViewModel
            onUploadProfileImage(it.toString())
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        }
    }

    fun launchImagePicker() {
        if (PermissionUtils.hasImagePermission(context)) {
            imagePickerLauncher.launch("image/*")
        } else {
            permissionLauncher.launch(PermissionUtils.getImagePermission())
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileImage(
            profileImageUrl = profileImageUrl,
            onLongClick = { showImageSheet = true }
        )

        Spacer(modifier = Modifier.width(16.dp))

        UserInfoCard(
            username = username,
            level = level,
            progress = progress,
            onEditUsername = onEditUsername
        )
    }

    ProfileImageActionSheet(
        visible = showImageSheet,
        hasImage = !profileImageUrl.isNullOrEmpty(),
        onDismiss = { showImageSheet = false },
        onUpload = {
            showImageSheet = false
            launchImagePicker()
        },
        onRemove = {
            showImageSheet = false
            onDeleteProfileImage()
        }
    )
}

@Composable
private fun ProfileImage(
    profileImageUrl: String?,
    onLongClick: () -> Unit
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
                    onLongClick = onLongClick
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
                    onLongClick = onLongClick
                )
        )
    }
}

@Composable
private fun UserInfoCard(
    username: String,
    level: Int,
    progress: Float,
    onEditUsername: () -> Unit
) {
    Column(
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
                    .clickable { onEditUsername() }
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

@Composable
private fun LeetCodeConnectionSection(
    onLeetCodeConnect: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }

    Text(text = "Connect your LeetCode profile", style = MaterialTheme.typography.titleMedium)
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        label = { Text("LeetCode Username") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    ShadowButton(
        text = "Connect",
        onClick = { onLeetCodeConnect(input) },
        modifier = Modifier.fillMaxWidth(),
        foregroundColor = Color(0xFF7B9EFF),
        contentColor = Color.White
    )
}

@Composable
private fun LeetCodeStatsSection(
    leetcodeUsername: String,
    solvedCount: Int?,
    solvedEasy: Int?,
    solvedMedium: Int?,
    solvedHard: Int?,
    easyTotal: Int?,
    mediumTotal: Int?,
    hardTotal: Int?,
    onLeetCodeUpdate: (String) -> Unit,
    onLeetCodeRefresh: () -> Unit
) {
    var showUpdateDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        // Shadow Card
        Card(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {}

        // Main Card
        Card(
            modifier = Modifier
                .border(2.dp, Color.Black, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LeetCode: $leetcodeUsername",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Update username",
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { showUpdateDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OverallProgressIndicator(solvedCount = solvedCount)

                Spacer(modifier = Modifier.height(16.dp))

                DifficultyIndicatorsRow(
                    solvedEasy = solvedEasy,
                    solvedMedium = solvedMedium,
                    solvedHard = solvedHard,
                    easyTotal = easyTotal,
                    mediumTotal = mediumTotal,
                    hardTotal = hardTotal
                )

                Spacer(modifier = Modifier.height(16.dp))

                ShadowButton(
                    text = "Refresh Stats",
                    onClick = onLeetCodeRefresh,
                    modifier = Modifier.fillMaxWidth(),
                    foregroundColor = Color(0xFF7B9EFF),
                    contentColor = Color.White
                )
            }
        }
    }

    if (showUpdateDialog) {
        LeetCodeUsernameUpdateDialog(
            currentUsername = leetcodeUsername,
            onDismiss = { showUpdateDialog = false },
            onUpdate = { newUsername ->
                onLeetCodeUpdate(newUsername)
                showUpdateDialog = false
            }
        )
    }
}

@Composable
private fun OverallProgressIndicator(
    solvedCount: Int?
) {
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

@Composable
private fun DifficultyIndicatorsRow(
    solvedEasy: Int?,
    solvedMedium: Int?,
    solvedHard: Int?,
    easyTotal: Int?,
    mediumTotal: Int?,
    hardTotal: Int?
) {
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
fun LeetCodeUsernameUpdateDialog(
    currentUsername: String,
    onDismiss: () -> Unit,
    onUpdate: (String) -> Unit
) {
    var newUsername by remember { mutableStateOf(currentUsername) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Update LeetCode Username") },
        text = {
            OutlinedTextField(
                value = newUsername,
                onValueChange = { newUsername = it },
                label = { Text("LeetCode Username") },
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

@Composable
fun EvaluationsSection(
    evaluations: List<EvaluationListItemDTO>,
    onEvaluationClick: (Long) -> Unit
) {
    if (evaluations.isEmpty()) {
        // Empty state
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No evaluations yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your solution evaluations will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        // Display the list of evaluations
        LazyColumn {
            items(evaluations) { evaluation ->
                EvaluationItem(
                    evaluation = evaluation,
                    onClick = { onEvaluationClick(evaluation.evaluationId) }
                )
            }
        }
    }
}

@Composable
fun EvaluationItem(
    evaluation: EvaluationListItemDTO,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 12.dp,
    shadowOffsetX: androidx.compose.ui.unit.Dp = 4.dp,
    shadowOffsetY: androidx.compose.ui.unit.Dp = 4.dp,
    shadowColor: Color = Color.Black
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        // Shadow Card
        Card(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffsetX, y = shadowOffsetY),
            colors = CardDefaults.cardColors(containerColor = shadowColor),
            shape = RoundedCornerShape(cornerRadius),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {}

        // Main Card
        Card(
            modifier = Modifier
                .clickable { onClick() }
                .border(2.dp, Color.Black, RoundedCornerShape(cornerRadius)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(cornerRadius),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = evaluation.problemTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF7B9EFF)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = evaluation.createdAt.split("T")[0], // Extract only the date part
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun NeoBrutalismTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("LeetCode", "History")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            NeoBrutalismTab(
                text = tab,
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun NeoBrutalismTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) Color(0xFF7B9EFF) else Color.White
    val textColor = if (selected) Color.White else Color.Black
    val shadowColor = if (selected) Color(0xFF5A7CD8) else Color.Gray

    Box(
        modifier = modifier
            .height(48.dp)
    ) {
        // Tab shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 2.dp)
                .background(
                    color = shadowColor,
                    shape = RoundedCornerShape(8.dp)
                )
        )

        // Main tab
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
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
            selectedTabIndex = 0,
            evaluations = listOf(),
            onLeetCodeConnect = {},
            onLeetCodeUpdate = {},
            onLeetCodeRefresh = {},
            onUploadProfileImage = {},
            onDeleteProfileImage = {},
            onUsernameChange = {},
            onTabSelected = {},
            onEvaluationClick = {}
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
            selectedTabIndex = 0,
            evaluations = listOf(),
            onLeetCodeConnect = {},
            onLeetCodeUpdate = {},
            onLeetCodeRefresh = {},
            onUploadProfileImage = {},
            onDeleteProfileImage = {},
            onUsernameChange = {},
            onTabSelected = {},
            onEvaluationClick = {}
        )
    }
}
