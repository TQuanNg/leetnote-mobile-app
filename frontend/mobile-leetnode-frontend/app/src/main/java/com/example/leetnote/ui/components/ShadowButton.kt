package com.example.leetnote.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ShadowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    shadowOffsetX: Dp = 4.dp,
    shadowOffsetY: Dp = 6.dp,
    foregroundColor: Color = Color.White,
    shadowColor: Color = Color.Black,
    contentColor: Color = Color.Black,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = 4.dp) // reduce shadow at bottom
                .offset(x = shadowOffsetX, y = shadowOffsetY)
        ) {
            Button(
                onClick = {},
                shape = RoundedCornerShape(cornerRadius),
                colors = ButtonDefaults.buttonColors(containerColor = shadowColor),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                modifier = Modifier.fillMaxSize()
            ) {}
        }

        // Foreground button
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(cornerRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = foregroundColor,
                contentColor = contentColor
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text)
        }
    }
}