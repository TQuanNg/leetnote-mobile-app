package com.example.leetnote.data.repository

import com.example.leetnote.data.api.LeetnoteApiService
import com.example.leetnote.data.model.SubmissionDTO
import javax.inject.Inject

data class EvaluationDTO(
    val id: Long,
    val version: Int,
    val evaluation: EvaluationDetail,
    val createdAt: String
)

data class EvaluationDetail(
    val rating: Int,
    val issue: List<String>,
    val feedback: List<String>
)

class EvaluationRepository @Inject constructor(
    private val api: LeetnoteApiService
) {
    suspend fun getLastSubmission(problemId: Long): SubmissionDTO.Submission? {
        val response = api.getLastSubmission(problemId)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun createEvaluation(request: SubmissionDTO.SubmissionRequest): EvaluationDTO {
        return api.createEvaluation(request)
    }

    suspend fun getLastEvaluation(problemId: Long): EvaluationDTO? {
        val response = api.getLastEvaluation(problemId)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getAllEvaluations(problemId: Long): List<EvaluationDTO> {
        return api.getAllEvaluations(problemId)
    }

}