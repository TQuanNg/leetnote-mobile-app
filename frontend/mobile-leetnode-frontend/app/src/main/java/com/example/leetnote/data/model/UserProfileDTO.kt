package com.example.leetnote.data.model

data class UserProfileDTO(
    val id: Long,
    val email: String,
    val username: String,
    val profileUrl: String? = null
)


data class SetUsernameRequest(
    val username: String
)

data class UpdateProfileRequest(
    val profileUrl: String
)
