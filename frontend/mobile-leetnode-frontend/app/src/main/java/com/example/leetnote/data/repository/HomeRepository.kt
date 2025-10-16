package com.example.leetnote.data.repository

import com.example.leetnote.data.api.LeetnoteApiService
import com.example.leetnote.data.model.LeetProblem
import com.example.leetnote.data.model.PageResponse
import com.example.leetnote.data.model.ProblemDetailDTO
import com.example.leetnote.data.model.ProblemListDTO
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val api: LeetnoteApiService
) {
    suspend fun getAllProblems(
        keyword: String? = null,
        difficulties: List<String>? = null,
        isSolved: Boolean? = null,
        isFavorite: Boolean? =null,
        page: Int = 1,
        pageSize: Int = 20
    ): PageResponse<ProblemListDTO> {
        return api.getAllProblems(
            keyword = keyword,
            difficulty = difficulties?.joinToString(","),
            isSolved = isSolved,
            isFavorite = isFavorite,
            page = page,
            pageSize = pageSize
        )
    }

    suspend fun getProblemDetail(
        problemId: Long
    ): ProblemDetailDTO {
        val response = api.getProblemDetail(problemId)

        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Failed to load problem details: ${response.code()} ${response.message()}")
        }
    }

    suspend fun updateProblemStatus(
        problemId: Long,
        isSolved: Boolean,
        isFavorite: Boolean
    ): LeetProblem {
        val updatedDto = api.updateProblemStatus(problemId, isSolved, isFavorite)
        return LeetProblem(
            id = updatedDto.id,
            title = updatedDto.title,
            difficulty = updatedDto.difficulty,
            isSolved = updatedDto.isSolved,
            isFavorite = updatedDto.isFavorite
        )
    }
}