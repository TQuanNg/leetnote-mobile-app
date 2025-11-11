package com.example.leetnote.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.leetnote.R
import com.example.leetnote.data.model.LeetProblem
import com.example.leetnote.ui.components.FilterSection
import com.example.leetnote.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navHostController: NavHostController,
    viewModel: HomeViewModel
) {

    val searchQuery by viewModel.searchQuery.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val shouldShowSearchBar by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    // Use FilterSection's built-in dialog instead of ModalBottomSheet
    if (showFilterSheet) {
        FilterSection(
            selectedDifficulties = viewModel.selectedDifficulties.collectAsState().value,
            filterSolved = viewModel.filterSolved.collectAsState().value,
            filterFavorite = viewModel.filterFavorite.collectAsState().value,
            onDifficultiesSelected = { viewModel.updateDifficulties(it) },
            onSolvedFilterChanged = { viewModel.updateFilterSolved(it) },
            onFavoriteFilterChange = { viewModel.updateFilterFavorite(it) },
            onClose = { showFilterSheet = false },
            isVisible = showFilterSheet
        )
    }
    val pagedProblems = viewModel.pagedProblems.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        state = listState
    ) {

        // ✅ Collapsible header area
        item {
            AnimatedVisibility(visible = shouldShowSearchBar) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Image(
                                painter = painterResource(id = R.drawable.filter_icon),
                                contentDescription = "Filter Icon",
                                modifier = Modifier.size(36.dp),
                                colorFilter = ColorFilter.tint(Color(0xFF7B9EFF))
                            )
                        }
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateQuery(it) },
                            label = { Text("Search Problems") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // ✅ Problems list
        when (val refreshState = pagedProblems.loadState.refresh) {
            is LoadState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is LoadState.Error -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getFriendlyErrorMessage(refreshState.error),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            else -> {
                items(
                    count = pagedProblems.itemCount,
                    key = pagedProblems.itemKey { it.id }
                ) { index ->
                    val problem = pagedProblems[index]
                    problem?.let {
                        ProblemItem(
                            problem = it,
                            onClick = { navHostController.navigate(Screen.Problem.createRoute(it.id)) },
                            onSolvedToggle = { viewModel.toggleSolved(it) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                when (pagedProblems.loadState.append) {
                    is LoadState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    is LoadState.Error -> {
                        item {
                            Text(
                                text = "Failed to load more problems",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}

@Composable
fun ProblemItem(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    shadowOffsetX: Dp = 6.dp,
    shadowOffsetY: Dp = 6.dp,
    shadowColor: Color = Color.Black,
    problem: LeetProblem,
    onClick: () -> Unit = {},
    onSolvedToggle: (LeetProblem) -> Unit,
    onFavoriteToggle: (LeetProblem) -> Unit
) {
    Box(modifier = modifier) {
        // Shadow Card
        Card(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffsetX, y = shadowOffsetY),
            colors = CardDefaults.cardColors(containerColor = shadowColor),
            shape = RoundedCornerShape(cornerRadius),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {}

        Card(
            modifier = Modifier
                .clickable { onClick() }
                .border(1.dp, Color.Black, RoundedCornerShape(cornerRadius)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(cornerRadius),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                {
                    Text(
                        text = problem.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = problem.difficulty,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = problem.difficulty.toDifficultyColor()
                    )
                }

                Row {
                    Spacer(modifier = Modifier.width(4.dp))
                    SolvedToggleButton(
                        isSolved = problem.isSolved,
                        onToggle = { onSolvedToggle(problem) }
                    )

                    FavoriteToggleButton(
                        isFavorite = problem.isFavorite,
                        onToggle = { onFavoriteToggle(problem) }
                    )
                }
            }
        }
    }
}

fun String.toDifficultyColor(): Color = when (this.lowercase()) {
    "easy" -> Color(0xFF4CAF50)
    "medium" -> Color(0xFFFFC107)
    "hard" -> Color(0xFFF44336)
    else -> Color.Black
}

@Composable
fun FavoriteToggleButton(
    isFavorite: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    IconToggleButton(
        checked = isFavorite,
        onCheckedChange = { onToggle(it) }
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Unfavorite" else "favorite",
            tint = if (isFavorite) Color.Red else Color.Gray
        )
    }
}

@Composable
fun SolvedToggleButton(
    isSolved: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    IconToggleButton(
        checked = isSolved,
        onCheckedChange = { onToggle(it) }
    ) {
        Icon(
            imageVector = if (isSolved) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
            contentDescription = if (isSolved) "Mark as Unsolved" else "Mark as Solved",
            tint = if (isSolved) Color(0xFF4CAF50) else Color.Gray // Green when solved
        )
    }
}

fun getFriendlyErrorMessage(error: Throwable): String {
    return when (error) {
        is java.net.UnknownHostException -> "Cannot connect to the server. Please check your internet connection."
        is java.net.ConnectException -> "Server is unreachable. Please try again later."
        is java.net.SocketTimeoutException -> "Connection timed out. Please try again."
        else -> "Something went wrong. Please try again."
    }
}
