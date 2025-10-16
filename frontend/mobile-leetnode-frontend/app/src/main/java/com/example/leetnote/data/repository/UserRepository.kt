package com.example.leetnote.data.repository

import com.example.leetnote.data.api.LeetnoteApiService
import com.example.leetnote.data.model.SetUsernameRequest
import com.example.leetnote.data.model.UpdateProfileRequest
import com.example.leetnote.data.model.UserProfileDTO
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: LeetnoteApiService
) {
    suspend fun getUserProfile(): UserProfileDTO {
        return api.getUserProfile()
    }

    suspend fun setUsername(username: String): UserProfileDTO {
        return api.setUsername(SetUsernameRequest(username))
    }

    suspend fun uploadProfileImage(imageUrl: String): String {
        val request = UpdateProfileRequest(profileUrl = imageUrl)
        return api.uploadProfilePicture(request)
    }

    suspend fun deleteProfileImage() {
        api.deleteProfilePicture()
    }
}