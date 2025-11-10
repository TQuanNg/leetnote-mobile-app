package com.example.leetnote.ui.screens.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leetnote.ui.components.CustomCard

@Composable
fun LearningItemScreen(
    pattern: PatternItem
) {
    LearningItemContent(pattern)
}

@Composable
fun LearningItemContent(
    pattern: PatternItem
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with gradient background
        PatternHeader(pattern.name)

        // Content sections
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CustomCard(
                title = "💡 Concept",
                description = pattern.concept,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                titleSize = 18.sp,
                titleWeight = FontWeight.SemiBold,
                titleColor = MaterialTheme.colorScheme.primary
            )

            CustomCard(
                title = "✅ When to Use",
                description = pattern.whenToUse.joinToString("\n") { "• $it" },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFFE8F5E8), // Light green
                titleSize = 18.sp,
                titleWeight = FontWeight.SemiBold,
                titleColor = MaterialTheme.colorScheme.onSurface
            )

            CustomCard(
                title = "🔧 Approach",
                description = pattern.approach.joinToString("\n") { "• $it" },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFFE3F2FD), // Light blue
                titleSize = 18.sp,
                titleWeight = FontWeight.SemiBold,
                titleColor = MaterialTheme.colorScheme.onSurface
            )

            ComplexityCard(pattern.complexity)

            CustomCard(
                title = "📝 Examples",
                description = pattern.examples.joinToString("\n") { "• $it" },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFFFFF3E0), // Light orange
                titleSize = 18.sp,
                titleWeight = FontWeight.SemiBold,
                titleColor = MaterialTheme.colorScheme.onSurface
            )

            CustomCard(
                title = "⭐ Pro Tips",
                description = pattern.tips.joinToString("\n") { "• $it" },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFFFCE4EC), // Light pink
                titleSize = 18.sp,
                titleWeight = FontWeight.SemiBold,
                titleColor = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun PatternHeader(name: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Decorative circle instead of icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                        CircleShape
                    )
            ) {
                Text(
                    text = "📚",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
fun ComplexityCard(complexity: Complexity) {
    CustomCard(
        title = "⏱️ Complexity Analysis",
        description = buildComplexityDescription(complexity),
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        titleSize = 18.sp,
        titleWeight = FontWeight.SemiBold,
        titleColor = MaterialTheme.colorScheme.onSurface
    )
}

fun buildComplexityDescription(complexity: Complexity): String {
    return """
        Time Complexity: ${complexity.time}
        Space Complexity: ${complexity.space}
        
        Time complexity measures how the algorithm's runtime grows with input size.
        Space complexity measures how much extra memory the algorithm uses.
    """.trimIndent()
}


@Preview(showBackground = true)
@Composable
fun LearningItemScreenPreview() {
    MaterialTheme {
        val sample = PatternItem(
            id = 1,
            name = "Two Pointers",
            concept = "Use two indices moving through an array or string to solve problems efficiently." +
                    " This technique is particularly useful for problems involving pairs, triplets," +
                    " or when you need to compare elements from different positions.",
            whenToUse = listOf(
                "Finding pairs with a specific sum in a sorted array",
                "Checking if a string is a palindrome",
                "Removing duplicates from a sorted array",
                "Finding the container with most water"
            ),
            approach = listOf(
                "Initialize two pointers at different positions (usually start and end)",
                "Move pointers based on problem conditions",
                "Continue until pointers meet or cross each other",
                "Process elements at pointer positions"
            ),
            complexity = Complexity("O(n)", "O(1)"),
            examples = listOf(
                "167. Two Sum II - Input Array Is Sorted",
                "125. Valid Palindrome",
                "11. Container With Most Water",
                "15. 3Sum"
            ),
            tips = listOf(
                "Works best when the array is sorted",
                "Can reduce time complexity from O(n²) to O(n)",
                "Consider the direction of pointer movement carefully",
                "Great alternative to nested loops"
            )
        )
        LearningItemScreen(sample)
    }
}