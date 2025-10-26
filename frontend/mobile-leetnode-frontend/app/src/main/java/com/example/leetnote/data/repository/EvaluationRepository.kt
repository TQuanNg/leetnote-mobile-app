package com.example.leetnote.data.repository

import com.example.leetnote.data.api.LeetnoteApiService
import com.example.leetnote.data.model.EvaluationDetail
import com.example.leetnote.data.model.EvaluationDetailDTO
import com.example.leetnote.data.model.EvaluationListItemDTO
import com.example.leetnote.data.model.SubmissionDTO
import javax.inject.Inject


class EvaluationRepository @Inject constructor(
    private val api: LeetnoteApiService
) {
    suspend fun getLastSubmission(problemId: Long): SubmissionDTO.Submission? {
        val response = api.getLastSubmission(problemId)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun createEvaluation(request: SubmissionDTO.SubmissionRequest): EvaluationDetail {
        return api.createEvaluation(request)
    }

    suspend fun getLastEvaluation(
        evaluationId: Long? = null,
        problemId: Long? = null
    ): EvaluationDetailDTO? {
        val response = api.getLastEvaluation(evaluationId, problemId)
        return if (response.isSuccessful) response.body() else null
    }

    // used for submission and evaluation
    suspend fun getNewEvaluation(problemId: Long): EvaluationDetail? {
        val response = api.getNewEvaluation(problemId)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getEvaluationById(evaluationId: Long): EvaluationDetailDTO? {
        return getLastEvaluation(evaluationId = evaluationId)
    }

    suspend fun getAllUserEvaluations(): List<EvaluationListItemDTO> {
        return api.getAllUserEvaluations()
    }
}