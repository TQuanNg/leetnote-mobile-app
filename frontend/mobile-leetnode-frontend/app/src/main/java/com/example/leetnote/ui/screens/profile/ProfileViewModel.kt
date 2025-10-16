package com.example.leetnote.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leetnote.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ProfileUiState(
    val profileImageUrl: String? = null,
    val username: String = "",
    val email: String = "",
    val level: Int = 0,
    val progress: Float = 0f,
    val leetcodeUsername: String? = null,
    val solvedCount: Int? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserProfile()
    }

    /** Load the user profile from repository */
    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val profile = userRepository.getUserProfile()
                _uiState.update {
                    it.copy(
                        email = profile.email,
                        username = profile.username,
                        profileImageUrl = profile.profileUrl
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Update username */
    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val updatedProfile = userRepository.setUsername(newUsername)
                _uiState.update {
                    it.copy(
                        username = updatedProfile.username
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Upload new profile image */
    fun uploadProfileImage(imageUrl: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val uploadedUrl = userRepository.uploadProfileImage(imageUrl)
                _uiState.update { it.copy(profileImageUrl = uploadedUrl) }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Delete profile image */
    fun deleteProfileImage() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                userRepository.deleteProfileImage()
                _uiState.update { it.copy(profileImageUrl = null) }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Example: connect LeetCode username */
    fun connectLeetCode(username: String) {
        _uiState.update { it.copy(leetcodeUsername = username, solvedCount = 0) }
    }

    fun Uri.toFile(context: Context): File {
        val inputStream = context.contentResolver.openInputStream(this)!!
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
        inputStream.use { input -> tempFile.outputStream().use { output -> input.copyTo(output) } }
        return tempFile
    }
}