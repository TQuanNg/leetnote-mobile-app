package com.example.leetnote.ui.screens.problem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.leetnote.data.model.ProblemDetailDTO
import com.example.leetnote.data.model.SolutionDTO
import com.example.leetnote.ui.components.CustomCard
import com.example.leetnote.ui.components.ShadowButton
import com.example.leetnote.ui.navigation.Screen

@Composable
fun ProblemScreen(
    modifier: Modifier = Modifier,
    problemId: Long,
    navController: NavController,
    viewModel: ProblemDetailViewModel
) {
    val problemDetail by viewModel.problemDetail.collectAsState()
    val isDetailLoading by viewModel.isDetailLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(problemId) {
        viewModel.loadProblemDetail(problemId)
    }

    ProblemContent(
        modifier = modifier,
        problemDetail = problemDetail,
        isLoading = isDetailLoading,
        errorMessage = errorMessage,
        onSolveClick = { navController.navigate(Screen.Solving.createRoute(problemId)) },
        onViewSolutionsClick = { navController.navigate(Screen.Solution.createRoute(problemId)) }
    )
}

@Composable
fun ProblemContent(
    modifier: Modifier = Modifier,
    problemDetail: ProblemDetailDTO?,
    isLoading: Boolean,
    errorMessage: String?,
    onSolveClick: () -> Unit,
    onViewSolutionsClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            isLoading -> {
                Text(
                    "Loading...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            errorMessage != null -> {
                Text(
                    "Error: $errorMessage",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Red
                )
            }

            problemDetail != null -> {
                CustomCard(
                    title = problemDetail.title,
                    titleSize = 24.sp,
                    titleWeight = FontWeight.Bold,
                    description = buildString {
                        append(problemDetail.description)
                        append("\n\n")
                    },
                    modifier = Modifier.fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                )

                ShadowButton(
                    text = "Solve It",
                    onClick = onSolveClick,
                    modifier = Modifier.fillMaxWidth(),
                    foregroundColor = Color(0xFF7B9EFF),
                    contentColor = Color.White
                )

                ShadowButton(
                    text = "View Solutions",
                    onClick = onViewSolutionsClick,
                    modifier = Modifier.fillMaxWidth(),
                    foregroundColor = Color(0xFF34C759),
                    contentColor = Color.White
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ProblemContentPreview() {
    val mockSolution = SolutionDTO(
        approach = "Hash Map",
        code = "fun twoSum(nums: IntArray, target: Int): IntArray { ... }",
        timeComplexity = "O(n)",
        spaceComplexity = "O(n)"
    )

    val mockProblemDetail = ProblemDetailDTO(
        id = 1L,
        title = "Two Sum",
        description = "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target." +
                "\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice." +
                "\n\nYou can return the answer in any order.\n\nExample 1:\nInput: nums = [2,7,11,15], target = 9\nOutput: [0,1]",
        difficulty = "Easy",
        isFavorite = false,
        isSolved = false,
        solution = mockSolution
    )

    ProblemContent(
        problemDetail = mockProblemDetail,
        isLoading = false,
        errorMessage = null,
        onSolveClick = { },
        onViewSolutionsClick = { }
    )
}

@Preview(showBackground = true)
@Composable
fun ProblemContentLoadingPreview() {
    ProblemContent(
        problemDetail = null,
        isLoading = true,
        errorMessage = null,
        onSolveClick = { },
        onViewSolutionsClick = { }
    )
}

@Preview(showBackground = true)
@Composable
fun ProblemContentErrorPreview() {
    ProblemContent(
        problemDetail = null,
        isLoading = false,
        errorMessage = "Failed to load problem details",
        onSolveClick = { },
        onViewSolutionsClick = { }
    )
}