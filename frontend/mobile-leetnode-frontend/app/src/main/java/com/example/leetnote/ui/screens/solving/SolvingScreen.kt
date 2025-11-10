package com.example.leetnote.ui.screens.solving

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leetnote.ui.components.CustomCard
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.leetnote.data.model.ProblemDetailDTO
import com.example.leetnote.data.model.SolutionDTO
import com.example.leetnote.ui.components.ShadowButton
import com.example.leetnote.ui.navigation.Screen

@Composable
fun SolvingScreen(
    modifier: Modifier = Modifier,
    problemId: Long,
    navController: NavController,
    viewModel: SolvingPageViewModel
) {
    val problemDetail by viewModel.problemDetail.collectAsState()
    val solutionText by viewModel.solutionText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val navigateToEvaluation = viewModel.navigateToEvaluation.collectAsState(initial = null)

    val context = LocalContext.current
    LaunchedEffect(problemId) {
        viewModel.fetchLastSubmission(problemId = problemId)
    }

    LaunchedEffect(problemId) {
        viewModel.loadProblemDetail(problemId)
    }

    LaunchedEffect(navigateToEvaluation.value) {
        navigateToEvaluation.value?.let {
            navController.navigate(Screen.Evaluation.createRoute(problemId))
        }
    }

    // Show error toast
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main solving content
        SolvingContent(
            problemDetail = problemDetail,
            solutionText = solutionText,
            isLoading = isLoading,
            onTextChange = { viewModel.onSolutionTextChange(it) },
            onSubmit = { viewModel.submitSolution(problemId) },
            modifier = modifier
        )

        // Overlay loading indicator
        if (isLoading) {
            LoadingScreen()
        }

        // Error AlertDialog
        val currentError = error
        if (currentError != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Submission Error") },
                text = { Text(currentError) }, // use local variable
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun SolvingContent (
    problemDetail: ProblemDetailDTO?,
    solutionText: String,
    isLoading: Boolean,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }


    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CustomCard(
            title = problemDetail?.title ?: "Loading title...",
            description = problemDetail?.description ?: "Loading description...",
            titleSize = 24.sp,
            titleWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
                .verticalScroll(rememberScrollState())
        )

        OutlinedTextField(
            value = solutionText,
            onValueChange = onTextChange,
            placeholder = { Text("Write your solution here...") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = Color(0xFF00FF7F)
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E1E1E),
                unfocusedContainerColor = Color(0xFF1E1E1E),
                focusedTextColor = Color(0xFF00FF7F),
                unfocusedTextColor = Color(0xFF00FF7F),
                cursorColor = Color(0xFF00FF7F),
                focusedIndicatorColor = Color(0xFF7B9EFF),
                unfocusedIndicatorColor = Color.Gray
            ),
            singleLine = false,
            maxLines = Int.MAX_VALUE,
            shape = RoundedCornerShape(12.dp)
        )

        ShadowButton(
            text = "Submit",
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            foregroundColor = Color(0xFF7B9EFF),
            contentColor = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SolvingContentPreview() {
    val fakeProblem = ProblemDetailDTO(
        id = 1L,
        title = "Two Sum",
        description = "Given an array of integers, return indices of the two numbers such that they add up to a specific target.",
        difficulty = "Easy",
        isFavorite = true,
        isSolved = false,
        solution = SolutionDTO(
            approach = "Use a hash map to store values and check for complement.",
            code = "fun twoSum(nums: IntArray, target: Int): IntArray { ... }",
            timeComplexity = "O(n)",
            spaceComplexity = "O(n)"
        )
    )

    SolvingContent(
        problemDetail = fakeProblem,
        solutionText = "",
        isLoading = false,
        onTextChange = {},
        onSubmit = {}
    )
}