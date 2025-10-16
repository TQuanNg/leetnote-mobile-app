package com.example.leetnote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leetnote.ui.theme.backgroundLight

@Composable
fun testScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundLight)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        ShadowButton(
            text = "Click Me",
            onClick = { /* handle click */ },
            modifier = Modifier.padding(16.dp)
        )
        CustomCard(
            title = "Sample Title",
            description = "This is a sample description for the custom card component.",
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            cornerRadius = 16.dp,
            shadowOffsetX = 4.dp,
            shadowOffsetY = 4.dp,
            backgroundColor = Color.White,
            shadowColor = Color.Gray,
            paddingContent = 16.dp
        )
    }
}

@Composable
fun CustomCard(
    title: String,
    description: String = "",
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    shadowOffsetX: Dp = 6.dp,
    shadowOffsetY: Dp = 6.dp,
    backgroundColor: Color = Color.White,
    shadowColor: Color = Color.Black,
    paddingContent: Dp = 16.dp,
    titleSize: TextUnit = 18.sp,
    descriptionSize: TextUnit = 14.sp,
    titleWeight: FontWeight? = null,
    descriptionWeight: FontWeight? = null,
    titleColor: Color = Color.Black,
    descriptionColor: Color = Color.Black,
    onClick: () -> Unit = {}
) {
    Box(modifier = modifier.padding(bottom = 16.dp) ) {
        // Shadow card
        Card(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffsetX, y = shadowOffsetY),
            colors = CardDefaults.cardColors(containerColor = shadowColor),
            shape = RoundedCornerShape(cornerRadius),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {}

        // Foreground card
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .border(1.dp, Color.Black, RoundedCornerShape(cornerRadius)),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            shape = RoundedCornerShape(cornerRadius),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(paddingContent)) {
                Text(text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = titleSize,
                    fontWeight = titleWeight ?: FontWeight.Normal,
                        color = titleColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                StyledDescriptionText(
                    description = description,
                    descriptionSize = descriptionSize,
                    descriptionWeight = descriptionWeight,
                    descriptionColor = descriptionColor
                )
            }
        }
    }
}

@Composable
fun StyledDescriptionText(
    description: String,
    descriptionSize: TextUnit = 14.sp,
    descriptionWeight: FontWeight? = null,
    descriptionColor: Color = Color.Black
) {
    val styledDescription = buildAnnotatedString {
        val lines = description.split("\n")

        lines.forEachIndexed { index, line ->
            when {
                line.trim().startsWith("Example") ||
                        line.trim().startsWith("Constraints") -> {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(line)
                    }
                }

                line.trim().startsWith("Input:") -> {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Input:")
                    }
                    append(line.removePrefix("Input:"))
                }

                line.trim().startsWith("Output:") -> {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Output:")
                    }
                    append(line.removePrefix("Output:"))
                }

                line.trim().startsWith("Explanation:") -> {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Explanation:")
                    }
                    append(line.removePrefix("Explanation:"))
                }

                else -> {
                    append(line)
                }
            }

            if (index != lines.lastIndex) append("\n")
        }
    }

    Text(
        text = styledDescription,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = descriptionSize,
            fontWeight = descriptionWeight ?: FontWeight.Normal,
            color = descriptionColor
        )
    )
}



@Preview
@Composable
fun CustomCardPreview() {
    testScreen()
}
