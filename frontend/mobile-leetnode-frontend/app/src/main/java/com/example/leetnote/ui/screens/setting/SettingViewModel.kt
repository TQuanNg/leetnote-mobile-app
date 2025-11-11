package com.example.leetnote.ui.screens.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leetnote.ui.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val themeManager: ThemeManager
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = themeManager.isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun toggleTheme() {
        viewModelScope.launch {
            themeManager.setDarkTheme(!isDarkTheme.value)
        }
    }

    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            themeManager.setDarkTheme(isDark)
        }
    }
}