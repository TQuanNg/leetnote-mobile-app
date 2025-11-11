package com.example.leetnote.ui

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LeetnoteApp(
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier,
) {
    MainScreen(
        modifier = modifier
    )
}