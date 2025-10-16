package com.example.leetnote.ui.screens.solving

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leetnote.R

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.loading_image),
                contentDescription = "Loading",
                modifier = Modifier.size(240.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = "Evaluating...",
                fontSize = 18.sp,
            )

            Spacer(modifier = Modifier.size(24.dp))

            CircularProgressIndicator(
                color = Color(0xFF4285F4),
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )
        }

    }
}

@Preview
@Composable
fun LoadingScreenPreview() {
    LoadingScreen()
}