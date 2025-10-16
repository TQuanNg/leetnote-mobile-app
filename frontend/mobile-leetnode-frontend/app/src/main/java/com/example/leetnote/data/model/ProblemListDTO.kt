package com.example.leetnote.data.model

import com.google.gson.annotations.SerializedName

data class ProblemListDTO (
    @SerializedName("problemId") val id: Long,
    val title: String,
    val difficulty: String,
    @SerializedName("solved")val isSolved: Boolean,
    @SerializedName("favorite")val isFavorite: Boolean
)