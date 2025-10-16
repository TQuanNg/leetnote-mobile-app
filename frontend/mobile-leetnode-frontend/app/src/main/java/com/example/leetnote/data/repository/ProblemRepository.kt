package com.example.leetnote.data.repository

import com.example.leetnote.data.api.LeetnoteApiService
import com.example.leetnote.data.model.ProblemDetailDTO
import javax.inject.Inject

class ProblemRepository @Inject constructor(
    private val api: LeetnoteApiService
) {

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
}