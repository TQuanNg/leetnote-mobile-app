package com.example.leetnote.data.repository

import android.net.Uri
import com.example.leetnote.data.api.LeetnoteApiService
import com.example.leetnote.data.model.SetUsernameRequest
import com.example.leetnote.data.model.UserProfileDTO
import com.example.leetnote.data.service.FirebaseStorageService
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: LeetnoteApiService,
    private val firebaseStorageService: FirebaseStorageService
) {
    suspend fun getUserProfile(): UserProfileDTO {
        return api.getUserProfile()
    }

    suspend fun setUsername(username: String): UserProfileDTO {
        return api.setUsername(SetUsernameRequest(username))
    }

    suspend fun uploadProfileImage(imageUri: String): String {
        // Parse string URI to Uri object
        val uri = Uri.parse(imageUri)

        // Upload image to Firebase Storage only
        return firebaseStorageService.uploadProfileImage(uri)
    }

    suspend fun deleteProfileImage(imageUrl: String) {
        // Delete from Firebase Storage only
        firebaseStorageService.deleteProfileImage(imageUrl)
    }
}