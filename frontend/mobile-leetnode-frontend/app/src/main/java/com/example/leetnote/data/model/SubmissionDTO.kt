package com.example.leetnote.data.model

class SubmissionDTO {
    data class SubmissionRequest(
        val problemId: Long,
        val solutionText: String
    )

    data class Submission(
        val id: Long,
        val problemId: Long,
        val solutionText: String,
        val createdAt: String,
        val evaluations: List<Evaluation> = emptyList()
    )

    data class Evaluation(
        val id: Long,
        val submissionId: Long,
        val feedback: String,
        val score: Int,
        val createdAt: String
    )
}