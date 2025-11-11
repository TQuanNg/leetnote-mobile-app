package com.example.leetnote.data.model

data class EvaluationDetail(
    val id: Long,
    val version: Int,
    val evaluation: EvaluationDTO,
    val createdAt: String
)

data class EvaluationDTO(
    val rating: Int,
    val issue: List<String>,
    val feedback: List<String>
)

data class EvaluationDetailDTO(
    val evaluationId: Long,
    val problemId: Long,
    val problemTitle: String,
    val difficulty: String,
    val createdAt: String,
    val evaluation: EvaluationDTO,
    val solutionText: String
)

data class EvaluationListItemDTO(
    val evaluationId: Long,
    val problemId: Long,
    val problemTitle: String,
    val createdAt: String,
)