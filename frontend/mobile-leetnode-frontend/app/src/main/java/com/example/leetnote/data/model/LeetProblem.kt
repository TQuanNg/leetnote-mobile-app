package com.example.leetnote.data.model

data class LeetProblem(
    val id: Long,
    val title: String,
    val difficulty: String,
    val isSolved: Boolean,
    val isFavorite: Boolean
)