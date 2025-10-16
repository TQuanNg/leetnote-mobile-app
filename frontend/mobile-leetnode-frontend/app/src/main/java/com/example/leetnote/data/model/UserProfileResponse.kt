package com.example.leetnote.data.model

data class UserProfileResponse(
    val userId: Long,
    val email: String,
    val displayName: String?,
    val createdAt: String?,
)