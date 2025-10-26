package com.example.leetnote.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.leetnote.data.repository.paging.ProblemPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import com.example.leetnote.data.model.LeetProblem
import com.example.leetnote.data.repository.HomeRepository
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FilterParams(
    val query: String,
    val difficulties: List<String>,
    val solved: Boolean?,
    val favorite: Boolean?
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
): ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val _selectedDifficulties = MutableStateFlow<List<String>>(emptyList())
    private val _filterSolved = MutableStateFlow<Boolean?>(null)
    private val _filterFavorite = MutableStateFlow<Boolean?>(null)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val selectedDifficulties: StateFlow<List<String>> = _selectedDifficulties.asStateFlow()
    val filterSolved: StateFlow<Boolean?> = _filterSolved.asStateFlow()
    val filterFavorite: StateFlow<Boolean?> = _filterFavorite.asStateFlow()

    private val filtersFlow: Flow<FilterParams> = combine(
        _searchQuery.debounce(300),
        _selectedDifficulties,
        _filterSolved,
        _filterFavorite
    ) { query, difficulties, solved, favorite ->
        FilterParams(query, difficulties, solved, favorite)
    }

    private val _pagedProblemsMutable = MutableStateFlow<PagingData<LeetProblem>>(PagingData.empty())
    val pagedProblems: StateFlow<PagingData<LeetProblem>> = _pagedProblemsMutable.asStateFlow()


    init {
        viewModelScope.launch {
            filtersFlow.flatMapLatest { filters ->
                Pager(
                    PagingConfig(
                        pageSize = 20,
                        initialLoadSize = 20,
                        enablePlaceholders = false
                    )
                ) {
                    ProblemPagingSource(
                        repository = repository,
                        filters = filters
                    )
                }.flow.cachedIn(viewModelScope)
            }.collect {
                _pagedProblemsMutable.value = it
            }
        }
    }

    fun updateQuery(query: String) { _searchQuery.value = query }
    fun updateDifficulties(difficulties: List<String>) { _selectedDifficulties.value = difficulties }
    fun updateFilterSolved(solved: Boolean?) { _filterSolved.value = solved }
    fun updateFilterFavorite(favorite: Boolean?) { _filterFavorite.value = favorite }

    fun toggleSolved(problem: LeetProblem) {
        viewModelScope.launch {
            _pagedProblemsMutable.update { pagingData ->
                pagingData.map { if (it.id == problem.id) it.copy(isSolved = !problem.isSolved) else it }
            }

            repository.updateProblemStatus(
                problemId = problem.id,
                isSolved = !problem.isSolved,
                isFavorite = problem.isFavorite
            )
        }
    }

    fun toggleFavorite(problem: LeetProblem) {
        viewModelScope.launch {
            _pagedProblemsMutable.update { pagingData ->
                pagingData.map { if (it.id == problem.id) it.copy(isFavorite = !problem.isFavorite) else it }
            }

            repository.updateProblemStatus(
                problemId = problem.id,
                isSolved = problem.isSolved,
                isFavorite = !problem.isFavorite
            )
        }
    }
}

