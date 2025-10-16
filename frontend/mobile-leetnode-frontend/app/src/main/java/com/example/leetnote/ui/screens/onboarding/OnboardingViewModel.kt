package com.example.leetnote.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leetnote.data.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: OnboardingRepository
): ViewModel() {
    val isOnboardingCompleted: StateFlow<Boolean?> =
        repository.isOnboardingCompleted()
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
        }
    }
}