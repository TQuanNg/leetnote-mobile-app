package com.example.leetnote.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leetnote.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetUsernameViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetUsernameUiState())
    val uiState: StateFlow<SetUsernameUiState> = _uiState.asStateFlow()

    fun onUsernameChanged(newValue: String) {
        _uiState.update { it.copy(username = newValue) }
    }

    fun confirmUsername(token: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val updatedProfile = userRepository.setUsername(_uiState.value.username)
                _uiState.update { it.copy(isLoading = false, username = updatedProfile.username ?: "") }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Something went wrong")
            }
        }
    }
}

data class SetUsernameUiState(
    val username: String = "",
    val isLoading: Boolean = false
)