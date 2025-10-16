package com.example.leetnote.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leetnote.data.repository.HomeRepository
import com.example.leetnote.data.model.LeetProblem
import com.example.leetnote.data.model.PageResponse
import com.example.leetnote.data.model.ProblemListDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@HiltViewModel
class HomePageViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {
    private val _allProblems = MutableStateFlow<PageResponse<ProblemListDTO>?>(null)
    val allProblem: StateFlow<PageResponse<ProblemListDTO>?> = _allProblems.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDifficulties = MutableStateFlow<Set<String>>(emptySet())
    val selectedDifficulties: StateFlow<Set<String>> = _selectedDifficulties.asStateFlow()

    private val _filterSolved = MutableStateFlow<Boolean?>(null)
    val filterSolved: StateFlow<Boolean?> = _filterSolved.asStateFlow()

    private val _filterFavorite = MutableStateFlow<Boolean?>(null)
    val filterFavorite: StateFlow<Boolean?> = _filterFavorite.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val filteredProblems: StateFlow<List<LeetProblem>> = combine(
        _allProblems,
        _searchQuery.debounce(300),
        _selectedDifficulties,
        _filterSolved,
        _filterFavorite
    ) { pageResponse, query, difficulties, solvedFilter, favoriteFilter ->

        // take content or empty list if null
        val problems = pageResponse?.content ?: emptyList()

        problems
            .map { dto ->
                // map DTO → domain model
                LeetProblem(
                    id = dto.id,
                    title = dto.title,
                    difficulty = dto.difficulty,
                    isSolved = dto.isSolved,
                    isFavorite = dto.isFavorite
                )
            }
            .filter { problem ->
                val matchesQuery = query.isBlank() || problem.title.contains(query, ignoreCase = true)
                val matchesDifficulty = difficulties.isEmpty() || difficulties.contains(problem.difficulty)
                val matchesSolved = solvedFilter == null || problem.isSolved == solvedFilter
                val matchesFavorite = favoriteFilter == null || problem.isFavorite == favoriteFilter

                matchesQuery && matchesDifficulty && matchesSolved && matchesFavorite
            }
    }   // initial value should be an empty list
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun updateQuery(query: String) {
        _searchQuery.value = query
        refreshProblems()
    }

    fun updateFilterSolved(solved: Boolean?) {
        _filterSolved.value = solved
        refreshProblems()
    }

    fun updateDifficulties(difficulties: Set<String>) {
        _selectedDifficulties.value = difficulties
        refreshProblems()
    }

    fun updateFilterFavorite(favorite: Boolean?) {
        _filterFavorite.value = favorite
        refreshProblems()
    }

    init {
        refreshProblems()
    }

    private fun refreshProblems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val problems = repository.getAllProblems(
                    keyword = _searchQuery.value.ifBlank { null },
                    difficulties = _selectedDifficulties.value.toList(),
                    isSolved = _filterSolved.value,
                    isFavorite = _filterFavorite.value // assuming you map "favorite" to "favorited"
                )
                _allProblems.value = problems
            } catch (e: Exception) {
                // handle error
                Log.e("HomeViewModel", "Failed to fetch problems", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSolved(problemId: Long) {
        // Optimistically update UI

        // Then update backend
        viewModelScope.launch {
            val problem = _allProblems.value?.content?.find { it.id == problemId } ?: return@launch
            repository.updateProblemStatus(
                problemId = problem.id,
                isSolved = !problem.isSolved,
                isFavorite = problem.isFavorite
            )
            refreshProblems()
        }
    }

    fun toggleFavorite(problemId: Long) {
        viewModelScope.launch {
            val problem = _allProblems.value?.content?.find { it.id == problemId } ?: return@launch
            repository.updateProblemStatus(
                problemId = problem.id,
                isSolved = problem.isSolved,
                isFavorite = !problem.isFavorite
            )
            refreshProblems()
        }
    }
}