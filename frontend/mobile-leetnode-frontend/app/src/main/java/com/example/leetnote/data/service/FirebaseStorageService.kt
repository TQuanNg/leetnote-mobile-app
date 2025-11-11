package com.example.leetnote.data.service

import android.content.Context
import android.net.Uri
import com.example.leetnote.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) {

    /**
     * Upload profile image to Firebase Storage
     * @param imageUri Local image URI from device
     * @return Download URL of uploaded image
     */
    suspend fun uploadProfileImage(imageUri: Uri): String {
        val currentUser = firebaseAuth.currentUser
            ?: throw IllegalStateException("User must be logged in to upload images")

        val userId = currentUser.uid
        val imageId = UUID.randomUUID().toString()
        val fileName = "user_uploads/${userId}/${imageId}.jpg"

        // Compress and resize image before upload
        val compressedImageData = ImageUtils.resizeAndCompressImage(context, imageUri)
            ?: throw IllegalArgumentException("Failed to process image")

        val storageRef = firebaseStorage.reference.child(fileName)
        val uploadTask = storageRef.putBytes(compressedImageData)

        // Wait for upload to complete
        uploadTask.await()

        // Get download URL
        return storageRef.downloadUrl.await().toString()
    }

    /**
     * Delete profile image from Firebase Storage
     * @param imageUrl The download URL of the image to delete
     */
    suspend fun deleteProfileImage(imageUrl: String) {
        try {
            val storageRef = firebaseStorage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
        } catch (e: Exception) {
            // Log error but don't throw - image might already be deleted or URL invalid
            android.util.Log.w("FirebaseStorageService", "Failed to delete image: ${e.message}")
        }
    }

    /**
     * Delete all profile images for current user
     * This can be used when user deletes their account
     */
    suspend fun deleteAllUserImages() {
        val currentUser = firebaseAuth.currentUser
            ?: throw IllegalStateException("User must be logged in")

        val userId = currentUser.uid
        val userImagesRef = firebaseStorage.reference.child("profile_images/$userId")

        try {
            val listResult = userImagesRef.listAll().await()
            listResult.items.forEach { item ->
                item.delete().await()
            }
        } catch (e: Exception) {
            android.util.Log.w(
                "FirebaseStorageService",
                "Failed to delete user images: ${e.message}"
            )
        }
    }
}
