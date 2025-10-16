package com.example.leetnote.data.model

import com.google.gson.annotations.SerializedName

data class SolutionDTO(
    val approach: String,
    val code: String,
    val timeComplexity: String,
    val spaceComplexity: String
)

data class ProblemDetailDTO (
    @SerializedName("problemId") val id: Long,
    val title: String,
    val description: String,
    val difficulty: String,
    val isFavorite: Boolean,
    val isSolved: Boolean,
    val solution: SolutionDTO
)