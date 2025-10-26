package com.example.leetnote.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leetnote.data.repository.UserRepository
import com.example.leetnote.data.repository.LeetcodeRepository
import com.example.leetnote.data.repository.EvaluationRepository
import com.example.leetnote.data.model.EvaluationDetailDTO
import com.example.leetnote.data.model.EvaluationListItemDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profileImageUrl: String? = null,
    val username: String = "",
    val email: String = "",
    val level: Int = 0,
    val progress: Float = 0f,
    val leetcodeUsername: String? = null,
    val solvedCount: Int? = null,
    val solvedEasy: Int? = null,
    val solvedMedium: Int? = null,
    val solvedHard: Int? = null,
    val easyTotal: Int? = 907,
    val mediumTotal: Int? = 1933,
    val hardTotal: Int? = 876,
    val selectedTabIndex: Int = 0,
    val evaluations: List<EvaluationListItemDTO> = emptyList(),
    val selectedEvaluationDetail: EvaluationDetailDTO? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val leetcodeRepository: LeetcodeRepository,
    private val evaluationRepository: EvaluationRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserProfile()
        // loadAllEvaluations()
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTabIndex = tabIndex) }
    }

    fun updateTabIndex(tabIndex: Int) {
        _uiState.update { it.copy(selectedTabIndex = tabIndex) }
    }

    fun loadAllUserEvaluations() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val evaluations = evaluationRepository.getAllUserEvaluations()
                _uiState.update { it.copy(evaluations = evaluations) }
            } catch (e: Exception) {
                _error.value = "Failed to load evaluations: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getEvaluationDetail(evaluationId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val evaluationDetail = evaluationRepository.getEvaluationById(evaluationId)
                _uiState.update { it.copy(selectedEvaluationDetail = evaluationDetail) }
            } catch (e: Exception) {
                _error.value = "Failed to load evaluation detail: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

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

    fun connectLeetCode(username: String) {
        if (username.isBlank()) {
            _error.value = "LeetCode username can't be empty"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val stats = leetcodeRepository.getUserStats(username)
                if (stats == null) {
                    _error.value = "Failed to fetch LeetCode stats"
                } else {
                    _uiState.update {
                        it.copy(
                            leetcodeUsername = stats.username,
                            solvedCount = stats.totalSolved,
                            solvedEasy = stats.easySolved,
                            solvedMedium = stats.mediumSolved,
                            solvedHard = stats.hardSolved,
                        )
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}