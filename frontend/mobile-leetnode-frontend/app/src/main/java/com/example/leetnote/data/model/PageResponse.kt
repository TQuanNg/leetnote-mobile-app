package com.example.leetnote.data.model

data class PageResponse<T>(
    val content: List<T>,
    val totalPage: Int,
    val totalElement: Int,
    val number: Int
)