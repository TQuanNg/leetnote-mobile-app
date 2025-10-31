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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.leetnote.ui.components.CustomCard
import com.example.leetnote.ui.components.ShadowButton
import com.example.leetnote.ui.navigation.Screen

@Composable
fun ProblemScreen(
    modifier: Modifier = Modifier,
    problemId: Long,
    navController: NavController,
) {
    val viewModel: ProblemDetailViewModel = hiltViewModel()
    val problemDetail by viewModel.problemDetail.collectAsState()
    val isDetailLoading by viewModel.isDetailLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(problemId) {
        viewModel.loadProblemDetail(problemId)
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            isDetailLoading -> {
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
                    title = problemDetail!!.title,
                    titleSize = 24.sp,
                    titleWeight = FontWeight.Bold,
                    description = buildString {
                        append(problemDetail!!.description)
                        append("\n\n")
                    },
                    modifier = Modifier.fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                )

                ShadowButton(
                    text = "Solve It",
                    onClick = { navController.navigate(Screen.Solving.createRoute(problemId)) },
                    modifier = Modifier.fillMaxWidth(),
                    foregroundColor = Color(0xFF7B9EFF), // example blue color
                    contentColor = Color.White
                )

                ShadowButton(
                    text = "View Solutions",
                    onClick = { navController.navigate(Screen.Solution.createRoute(problemId)) },
                    modifier = Modifier.fillMaxWidth(),
                    foregroundColor = Color(0xFF34C759), // example green
                    contentColor = Color.White
                )
            }
        }
    }
}


@Preview
@Composable
fun ProblemScreenPreview() {
    ProblemScreen(
        problemId = 1,
        navController = rememberNavController()
    )
}