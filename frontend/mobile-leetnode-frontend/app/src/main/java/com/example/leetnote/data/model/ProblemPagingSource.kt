package com.example.leetnote.data.model

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.leetnote.data.repository.HomeRepository
import com.example.leetnote.ui.screens.home.FilterParams

class ProblemPagingSource(
    private val repository: HomeRepository,
    private val filters: FilterParams
) : PagingSource<Int, LeetProblem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LeetProblem> {
        return try {
            val page = params.key ?: 0  // backend is 0-based
            val pageSize = params.loadSize

            val response = repository.getAllProblems(
                keyword = filters.query,
                difficulties = filters.difficulties,
                isSolved = filters.solved,
                isFavorite = filters.favorite,
                page = page,
                pageSize = pageSize
            )

            val problems = response.content.map { dto ->
                LeetProblem(
                    id = dto.id,
                    title = dto.title,
                    difficulty = dto.difficulty,
                    isSolved = dto.isSolved,
                    isFavorite = dto.isFavorite
                )
            }

            LoadResult.Page(
                data = problems,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (response.content.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LeetProblem>): Int? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }
}