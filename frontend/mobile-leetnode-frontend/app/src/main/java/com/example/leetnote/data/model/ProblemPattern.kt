package com.example.leetnote.data.model

data class ProblemPattern (
    val id: Long,
    val title: String,
    val description: String? = null,
    val iconRes: Int? = null,
)