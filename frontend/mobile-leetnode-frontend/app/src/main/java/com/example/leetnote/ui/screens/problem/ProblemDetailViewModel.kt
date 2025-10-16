package com.example.leetnote.ui.screens.problem

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leetnote.data.model.ProblemDetailDTO
import com.example.leetnote.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProblemDetailViewModel @Inject constructor(
    private val repository: HomeRepository
): ViewModel() {
    private val _problemDetail = MutableStateFlow<ProblemDetailDTO?>(null)
    val problemDetail: StateFlow<ProblemDetailDTO?> = _problemDetail.asStateFlow()

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadProblemDetail(problemId: Long) {
        viewModelScope.launch {
            _isDetailLoading.value = true
            _errorMessage.value = null
            try {
                val detail = repository.getProblemDetail(problemId)
                _problemDetail.value = detail
            } catch (e: Exception) {
                Log.e("ProblemDetailModel", "Failed to fetch problem detail", e)
                _errorMessage.value = "Failed to load problem detail: ${e.message}"
            } finally {
                _isDetailLoading.value = false
            }
        }
    }
}