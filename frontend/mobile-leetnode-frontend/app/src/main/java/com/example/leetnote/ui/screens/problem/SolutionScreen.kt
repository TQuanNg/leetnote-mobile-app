package com.example.leetnote.ui.screens.problem

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.leetnote.ui.components.CustomCard


@Composable
fun SolutionScreen(
    problemId: Long,
    viewModel: ProblemDetailViewModel
) {
    val problemDetail by viewModel.problemDetail.collectAsState()
    val isLoading by viewModel.isDetailLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(problemId) {
        viewModel.loadProblemDetail(problemId)
    }


    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Error: $errorMessage",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Red
                )
            }
        }

        problemDetail != null -> {
            val problem = problemDetail!!  // safe unwrap

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                val (bgColor, textColor) = getDifficultyColors(problem.difficulty)

                CustomCard(
                    title = problem.title,
                    description = problem.difficulty,
                    titleSize = 24.sp,
                    titleWeight = FontWeight.Bold,
                    shadowOffsetX = 4.dp,
                    shadowOffsetY = 4.dp,
                    backgroundColor = bgColor,
                    titleColor = textColor,
                    descriptionColor = textColor,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "🔹 Approach",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(Color(0xFFF8F7F7), RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    Text(
                        text = problem.solution.approach,
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                    )
                }

                Text(
                    text = "🔹 Solution Code",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        HighlightedCodeBlock(problem.solution.code)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "🔹 Complexity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .height(IntrinsicSize.Min), // ensure equal height based on tallest child
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CustomCard(
                        title = "Time: ${problem.solution.timeComplexity}",
                        backgroundColor = Color(0xFF90CAF9),
                        titleSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CustomCard(
                        title = "Space: ${problem.solution.spaceComplexity}",
                        backgroundColor = Color(0xFFA5D6A7),
                        titleSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

fun getDifficultyColors(difficulty: String): Pair<Color, Color> {
    return when (difficulty.lowercase()) {
        "easy" -> Color(0xFF4CAF50) to Color.White   // Green bg, white text
        "medium" -> Color(0xFFFFC107) to Color.Black // Amber bg, black text
        "hard" -> Color(0xFFF44336) to Color.White   // Red bg, white text
        else -> Color.LightGray to Color.Black       // Default fallback
    }
}

@Composable
fun HighlightedCodeBlock(code: String) {
    val keywords = listOf(
        "def", "return", "for", "while", "if", "else", "elif", "import", "from", "class", "in"
    )


    val annotatedString = buildAnnotatedString {
        val tokens = code.split(" ")
        for (token in tokens) {
            when {
                keywords.contains(token) -> withStyle(
                    style = SpanStyle(
                        color = Color(0xFF569CD6),
                        fontWeight = FontWeight.Bold
                    ) // Blue keywords
                ) {
                    append("$token ")
                }
                token.startsWith("\"") || token.startsWith("'") -> withStyle(
                    style = SpanStyle(color = Color(0xFFD69D85)) // Strings orange
                ) {
                    append("$token ")
                }
                token.all { it.isDigit() } -> withStyle(
                    style = SpanStyle(color = Color(0xFFB5CEA8)) // Numbers green
                ) {
                    append("$token ")
                }
                else -> withStyle(
                    style = SpanStyle(color = Color.White)
                ) {
                    append("$token ")
                }
            }
        }
    }

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
                text = annotatedString,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.horizontalScroll(rememberScrollState()) // horizontal scrolling for long lines
            )
        }
    }
}

data class MockComplexity(
    val time: String,
    val space: String
)

data class MockSolution(
    val approach: String,
    val code: String,
    val complexity: MockComplexity
)

data class MockProblem(
    val id: Long,
    val title: String,
    val description: String,
    val solution: MockSolution
)


@Preview(showBackground = true)
@Composable
fun SolutionScreenPreview() {
    val mockProblem = MockProblem(
        id = 1,
        title = "Two Sum",
        description = "Easy",
        solution = MockSolution(
            approach = "Hash Map (One Pass) - Store numbers and indices while checking complements.",
            code = """
                class Solution {
                    public int[] twoSum(int[] nums, int target) {
                        Map<Integer, Integer> map = new HashMap<>();
                        for (int i = 0; i < nums.length; i++) {
                            int complement = target - nums[i];
                            if (map.containsKey(complement)) {
                                return new int[] {map.get(complement), i};
                            }
                            map.put(nums[i], i);
                        }
                        throw new IllegalArgumentException("No two sum solution");
                    }
                }
            """.trimIndent(),
            complexity = MockComplexity(
                time = "O(n) - Single pass",
                space = "O(n) - Hash map"
            )
        )
    )
}