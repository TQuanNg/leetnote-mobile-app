package com.example.leetnote.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.leetnote.ui.components.CustomCard
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationDetailScreen(
    problemId: Long,
    evaluationId: Long,
    onNavigateBack: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Load evaluation detail when screen is opened
    LaunchedEffect(evaluationId) {
        viewModel.getEvaluationDetail(evaluationId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evaluation Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = error ?: "Unknown error occurred",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                uiState.selectedEvaluationDetail != null -> {
                    EvaluationDetailContent(
                        evaluationDetail = uiState.selectedEvaluationDetail!!,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Text(
                        text = "No evaluation details found",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EvaluationDetailContent(
    evaluationDetail: com.example.leetnote.data.repository.EvaluationDetailDTO,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Problem Info Header - Using CustomCard like SolutionScreen
        val (bgColor, textColor) = getDifficultyColors(evaluationDetail.difficulty)

        CustomCard(
            title = evaluationDetail.problemTitle,
            description = evaluationDetail.difficulty,
            titleSize = 24.sp,
            titleWeight = FontWeight.Bold,
            shadowOffsetX = 4.dp,
            shadowOffsetY = 4.dp,
            backgroundColor = bgColor,
            titleColor = textColor,
            descriptionColor = textColor,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Date display - only showing date part
        Text(
            text = "Evaluated on: ${evaluationDetail.createdAt.split("T")[0]}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Evaluation Card with neo brutalism style
        NeoBrutalismCard(
            title = "📊 Evaluation Results",
            titleColor = Color(0xFF4CAF50)
        ) {
            Column {
                // Rating Section
                Text(
                    text = "Rating",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < evaluationDetail.evaluation.rating) {
                                Color(0xFFFFD700) // Gold
                            } else {
                                Color.Gray
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${evaluationDetail.evaluation.rating}/5",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Issues Section
                if (evaluationDetail.evaluation.issue.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Issues Found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    evaluationDetail.evaluation.issue.forEach { issue ->
                        Text(
                            text = "• $issue",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                // Feedback Section
                if (evaluationDetail.evaluation.feedback.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Feedback",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    evaluationDetail.evaluation.feedback.forEach { feedback ->
                        Text(
                            text = "• $feedback",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Solution Code Card - styled like SolutionScreen
        NeoBrutalismCard(
            title = "💻 Your Solution",
            titleColor = Color(0xFF7B9EFF)
        ) {
            CodeBlock(code = evaluationDetail.solutionText)
        }
    }
}

@Composable
private fun NeoBrutalismCard(
    title: String,
    titleColor: Color = Color.Black,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Shadow Card
        Card(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {}

        // Main Card
        Card(
            modifier = Modifier
                .border(2.dp, Color.Black, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            // Fake macOS-style traffic lights
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFFFF5F56), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFFFFBD2E), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF27CA3F), shape = CircleShape)
                )
            }

            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            )
        }
    }
}

private fun getDifficultyColors(difficulty: String): Pair<Color, Color> {
    return when (difficulty.lowercase()) {
        "easy" -> Color(0xFF4CAF50) to Color.White   // Green bg, white text
        "medium" -> Color(0xFFFFC107) to Color.Black // Amber bg, black text
        "hard" -> Color(0xFFF44336) to Color.White   // Red bg, white text
        else -> Color.LightGray to Color.Black       // Default fallback
    }
}