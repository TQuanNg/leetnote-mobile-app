package com.example.leetnote.ui.screens.learning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.leetnote.R
import com.example.leetnote.data.model.LearningTopic
import com.example.leetnote.ui.components.PatternCard
import com.example.leetnote.ui.navigation.Screen

// Function to map pattern IDs to specific Material Icons
fun getPatternIcon(patternId: Int): ImageVector {
    return when (patternId) {
        1 -> Icons.Filled.Code // Two Pointers
        2 -> Icons.Filled.Timeline // Merge Intervals
        3 -> Icons.Filled.Hub // Two Heaps
        4 -> Icons.Filled.Search // Modified Binary Search
        5 -> Icons.AutoMirrored.Filled.List // Top K Elements
        6 -> Icons.Filled.Route // Backtracking
        7 -> Icons.Filled.Memory // Dynamic Programming
        8 -> Icons.Filled.Layers // Tree BFS
        9 -> Icons.Filled.DataObject // Tree DFS
        10 -> Icons.Filled.Speed // Sliding Window
        11 -> Icons.Filled.Functions // Prefix Sum
        12 -> Icons.Filled.Transform // Greedy
        13 -> Icons.Filled.FindInPage // Bit Manipulation
        else -> Icons.Filled.Code // Default icon
    }
}

@Composable
fun LearningResourcesScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: LearningResViewModel
) {
    val patterns by viewModel.patterns.collectAsState()

    val mockTopics = listOf(
        LearningTopic(1, "Data Structures", R.drawable.home_icon),
        LearningTopic(2, "Algorithms", R.drawable.home_icon),
        LearningTopic(3, "Databases", R.drawable.home_icon),
        LearningTopic(4, "Networking", R.drawable.home_icon),
        LearningTopic(5, "Machine Learning", R.drawable.home_icon)
    )

    // Pass data and callbacks to content composable
    LearningResourcesContent(
        patterns = patterns,
        topics = mockTopics,
        onPatternClick = { pattern ->
            navController.navigate(Screen.LearningItem.createRoute(pattern.id))
        },
        onTopicClick = {

        },
        modifier = modifier
    )
}

@Composable
fun LearningResourcesContent(
    patterns: List<PatternItem>,
    topics: List<LearningTopic>,
    onPatternClick: (PatternItem) -> Unit,
    onTopicClick: (LearningTopic) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Problem Patterns",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(patterns.chunked(2)) { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { pattern ->
                    PatternCard(
                        title = pattern.name,
                        icon = getPatternIcon(pattern.id),
                        onClick = { onPatternClick(pattern) },
                        modifier = Modifier
                            .weight(1f)
                            .height(160.dp)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f)) // keep grid alignment
                }
            }
        }

        /*
        // Learning Topics Header
        item {
            Text(
                text = "Learning Topics",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            )
        }


        // Learning Topics grid
        items(topics.chunked(2)) { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { topic ->
                    CustomCard(
                        title = topic.title,
                        description = "",
                        onClick = { onTopicClick(topic) },
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }*/
    }
}

@Preview(showBackground = true)
@Composable
fun LearningResourcesScreenPreview() {
    val samplePatterns = listOf(
        PatternItem(
            id = 1,
            name = "Two Pointers",
            concept = "Use two indices moving through an array...",
            whenToUse = listOf("Finding pairs", "Checking palindrome"),
            approach = listOf("Init two pointers", "Move based on condition"),
            complexity = Complexity("O(n)", "O(1)"),
            examples = listOf("167. Two Sum II"),
            tips = listOf("Works best when sorted")
        ),
        PatternItem(
            id = 2,
            name = "Merge Intervals",
            concept = "Sort by start, merge overlaps...",
            whenToUse = listOf("Scheduling", "Finding free slots"),
            approach = listOf("Sort", "Merge", "Push result"),
            complexity = Complexity("O(n log n)", "O(n)"),
            examples = listOf("56. Merge Intervals"),
            tips = listOf("Sorting first is key")
        )
    )

    val sampleTopics = listOf(
        LearningTopic(1, "Data Structures", R.drawable.home_icon),
        LearningTopic(2, "Algorithms", R.drawable.home_icon)
    )

    LearningResourcesContent(
        patterns = samplePatterns,
        topics = sampleTopics,
        onPatternClick = {},
        onTopicClick = {},
        modifier = Modifier.fillMaxSize()
    )
}

