package com.example.leetnote.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {

    /**
     * Resize and compress image to reduce file size
     * @param context Application context
     * @param imageUri URI of the selected image
     * @param maxWidth Maximum width in pixels (default 800px)
     * @param maxHeight Maximum height in pixels (default 800px)
     * @param quality JPEG compression quality 0-100 (default 80)
     * @return ByteArray of compressed image
     */
    fun resizeAndCompressImage(
        context: Context,
        imageUri: Uri,
        maxWidth: Int = 800,
        maxHeight: Int = 800,
        quality: Int = 80
    ): ByteArray? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Get image orientation to handle rotation
            val rotatedBitmap = rotateImageIfRequired(context, originalBitmap, imageUri)

            // Calculate resize dimensions while maintaining aspect ratio
            val (newWidth, newHeight) = calculateResizeDimensions(
                rotatedBitmap.width,
                rotatedBitmap.height,
                maxWidth,
                maxHeight
            )

            // Resize the bitmap
            val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, newWidth, newHeight, true)

            // Compress to byte array
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            // Clean up
            if (rotatedBitmap != originalBitmap) {
                rotatedBitmap.recycle()
            }
            originalBitmap.recycle()
            resizedBitmap.recycle()

            outputStream.toByteArray()
        } catch (e: Exception) {
            android.util.Log.e("ImageUtils", "Error processing image", e)
            null
        }
    }

    private fun calculateResizeDimensions(
        originalWidth: Int,
        originalHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Pair<Int, Int> {
        var newWidth = originalWidth
        var newHeight = originalHeight

        // If image is larger than max dimensions, scale it down
        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()

            if (originalWidth > originalHeight) {
                newWidth = maxWidth
                newHeight = (maxWidth / aspectRatio).toInt()
            } else {
                newHeight = maxHeight
                newWidth = (maxHeight * aspectRatio).toInt()
            }
        }

        return Pair(newWidth, newHeight)
    }

    private fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {
        return try {
            val input = context.contentResolver.openInputStream(selectedImage)
            val ei = ExifInterface(input!!)
            val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            input.close()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
                else -> img
            }
        } catch (e: Exception) {
            android.util.Log.w("ImageUtils", "Could not get image orientation", e)
            img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        if (rotatedImg != img) {
            img.recycle()
        }
        return rotatedImg
    }
}
