package com.example.leetnote.ui.screens.solving

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.leetnote.data.model.EvaluationDetail
import com.example.leetnote.ui.components.CustomCard

@Composable
fun EvaluationScreen(
    problemId: Long,
    viewModel: SolvingPageViewModel
) {
    val lastEvaluation by viewModel.lastEvaluation.collectAsState()

    // Fetch evaluation when screen opens
    LaunchedEffect(problemId) {
        viewModel.fetchLastEvaluation(problemId)
    }

    EvaluationContent(lastEvaluation = lastEvaluation)
}

@Composable
fun EvaluationContent(
    lastEvaluation: EvaluationDetail?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Code Evaluation",
            style = MaterialTheme.typography.headlineSmall
        )

        if (lastEvaluation != null) {
            CustomCard(
                title = "⭐ Rating",
                description = lastEvaluation.evaluation.rating.toString(),
                titleSize = 20.sp,
                titleWeight = FontWeight.Bold,
                shadowOffsetX = 2.dp,
                shadowOffsetY = 2.dp,
                backgroundColor = Color(0xFFE3F2FD),
                titleColor = Color.Black,
                descriptionColor = Color.DarkGray
            )

            CustomCard(
                title = "💡 Issues",
                description = lastEvaluation.evaluation.issue.joinToString(
                    separator = "\n- ",
                    prefix = "- "
                ),
                titleSize = 20.sp,
                titleWeight = FontWeight.Bold,
                shadowOffsetX = 2.dp,
                shadowOffsetY = 2.dp,
                backgroundColor = Color(0xFFFFF9C4),
                titleColor = Color.Black,
                descriptionColor = Color.DarkGray
            )

            CustomCard(
                title = "💬 Feedback",
                description = lastEvaluation.evaluation.feedback.joinToString(
                    separator = "\n- ",
                    prefix = "- "
                ),
                titleSize = 20.sp,
                titleWeight = FontWeight.Bold,
                shadowOffsetX = 2.dp,
                shadowOffsetY = 2.dp,
                backgroundColor = Color(0xFFA9E8AB),
                titleColor = Color.Black,
                descriptionColor = Color.DarkGray
            )
        } else {
            Spacer(Modifier.height(16.dp))
            Text("No evaluation available yet.")
        }
    }
}
