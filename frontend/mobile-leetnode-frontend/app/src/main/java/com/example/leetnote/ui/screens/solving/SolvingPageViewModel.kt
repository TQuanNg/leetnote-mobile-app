package com.example.leetnote.ui.screens.solving

import android.net.http.HttpException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leetnote.data.model.ProblemDetailDTO
import com.example.leetnote.data.model.SubmissionDTO
import com.example.leetnote.data.model.EvaluationDetail
import com.example.leetnote.data.model.EvaluationListItemDTO
import com.example.leetnote.data.repository.EvaluationRepository
import com.example.leetnote.data.repository.ProblemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException

@HiltViewModel
class SolvingPageViewModel @Inject constructor(
    private val evaluationRepository: EvaluationRepository,
    private val problemRepository: ProblemRepository
): ViewModel() {

    private val _problemDetail = MutableStateFlow<ProblemDetailDTO?>(null)
    val problemDetail: StateFlow<ProblemDetailDTO?> = _problemDetail.asStateFlow()

    private val _solutionText = MutableStateFlow("")
    val solutionText: StateFlow<String> = _solutionText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastSubmission = MutableStateFlow<SubmissionDTO.Submission?>(null)
    val lastSubmission: StateFlow<SubmissionDTO.Submission?> = _lastSubmission.asStateFlow()

    private val _lastEvaluation = MutableStateFlow<EvaluationDetail?>(null)
    val lastEvaluation: StateFlow<EvaluationDetail?> = _lastEvaluation.asStateFlow()

    private val _allEvaluations = MutableStateFlow<List<EvaluationListItemDTO>>(emptyList())
    val allEvaluations: StateFlow<List<EvaluationListItemDTO>> = _allEvaluations.asStateFlow()

    private val _evaluationResult = MutableStateFlow<EvaluationDetail?>(null)
    val evaluationResult: StateFlow<EvaluationDetail?> = _evaluationResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _navigateToEvaluation = MutableSharedFlow<EvaluationDetail>()
    val navigateToEvaluation = _navigateToEvaluation.asSharedFlow()

    fun onSolutionTextChange(newText: String) {
        _solutionText.value = newText
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun loadProblemDetail(problemId: Long) {
        viewModelScope.launch {
            _error.value = null
            try {
                val detail = problemRepository.getProblemDetail(problemId)
                _problemDetail.value = detail
            } catch (e: Exception) {
                Log.e("ProblemDetailModel", "Failed to fetch problem detail", e)
                _error.value = "Failed to load problem detail: ${e.message}"
            }
        }
    }

    fun submitSolution(problemId: Long) {
        val currentSolution = _solutionText.value
        if (currentSolution.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val request = SubmissionDTO.SubmissionRequest(problemId, currentSolution)

                val result = withContext(Dispatchers.IO) {
                    try {
                        withTimeout(10000L) {
                            evaluationRepository.createEvaluation(request)
                        }
                    } catch (e: TimeoutCancellationException) {
                        null
                    }
                }

                if (result != null) {
                    _evaluationResult.value = result
                    _navigateToEvaluation.emit(result)
                } else {
                    _error.value = "Submission timed out. Please try again."
                }

            } catch (e: TimeoutCancellationException) {
                _error.value = e.message
            } catch (e: HttpException) {
                _error.value = "Submission failed: ${e.message}"
            } catch (e: IOException) {
                _error.value = "Cannot connect to server. Please check your network."
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchLastSubmission(problemId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = evaluationRepository.getLastSubmission(problemId)
                _lastSubmission.value = result

                if (_solutionText.value.isBlank() && result != null) {
                    _solutionText.value = result.solutionText
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _lastSubmission.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchLastEvaluation(problemId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _lastEvaluation.value = evaluationRepository.getNewEvaluation(problemId)
            } catch (e: Exception) {
                e.printStackTrace()
                _lastEvaluation.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}