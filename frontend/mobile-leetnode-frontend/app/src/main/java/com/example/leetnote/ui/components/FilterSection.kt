package com.example.leetnote.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.leetnote.R

@Composable
fun FilterSection(
    selectedDifficulties: List<String>,
    filterSolved: Boolean?,
    filterFavorite: Boolean?,
    onDifficultiesSelected: (List<String>) -> Unit,
    onSolvedFilterChanged: (Boolean?) -> Unit,
    onFavoriteFilterChange: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) +
               scaleIn(
                   animationSpec = spring(
                       dampingRatio = Spring.DampingRatioMediumBouncy,
                       stiffness = Spring.StiffnessLow
                   ),
                   initialScale = 0.8f
               ),
        exit = fadeOut(animationSpec = tween(200)) +
               scaleOut(
                   animationSpec = tween(200),
                   targetScale = 0.8f
               )
    ) {
        Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.filter_icon),
                                contentDescription = "Filter Icon",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.filter),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.cancel_icon),
                                contentDescription = "Close Icon",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Difficulty Section
                    Column {
                        Text(
                            text = stringResource(R.string.difficulty),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DifficultyCheckbox("Easy", selectedDifficulties, onDifficultiesSelected)
                        DifficultyCheckbox("Medium", selectedDifficulties, onDifficultiesSelected)
                        DifficultyCheckbox("Hard", selectedDifficulties, onDifficultiesSelected)
                    }

                    // Status Section
                    Column {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        StatusCheckbox(
                            label = stringResource(R.string.status_solved),
                            isChecked = filterSolved == true,
                            onCheckedChange = { checked ->
                                val newValue = if (checked) true else null
                                onSolvedFilterChanged(newValue)
                            }
                        )

                        StatusCheckbox(
                            label = stringResource(R.string.status_favorite),
                            isChecked = filterFavorite ?: false,
                            onCheckedChange = onFavoriteFilterChange
                        )
                    }

                    // Apply/Close Button
                    Button(
                        onClick = onClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.close),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DifficultyCheckbox(
    label: String,
    selectedDifficulties: List<String>,
    onSelectionChange: (List<String>) -> Unit
) {
    val isChecked = selectedDifficulties.contains(label)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { checked ->
                val updated = if (checked) {
                    selectedDifficulties + label
                } else {
                    selectedDifficulties - label
                }
                onSelectionChange(updated)
            }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun StatusCheckbox(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


/*
@Preview
@Composable
fun FilterSectionPreview() {
    FilterSection(
        title = "Easy",
        isSelected = true,
        onApply ={},
    )
}

 */