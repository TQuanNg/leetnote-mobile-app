package com.example.leetnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.leetnote.ui.LeetnoteApp
import com.example.leetnote.ui.screens.setting.SettingViewModel
import com.example.leetnote.ui.theme.LeetNoteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingViewModel: SettingViewModel = hiltViewModel()
            val isDarkTheme by settingViewModel.isDarkTheme.collectAsState()
            
            LeetNoteTheme(darkTheme = isDarkTheme) {
                val windowSize = calculateWindowSizeClass(this)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LeetnoteApp(windowSize = windowSize.widthSizeClass)
                }
            }
        }
    }
}
