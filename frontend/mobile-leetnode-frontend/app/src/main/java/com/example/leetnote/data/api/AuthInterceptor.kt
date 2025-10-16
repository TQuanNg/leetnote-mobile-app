package com.example.leetnote.data.api

import com.example.leetnote.ui.screens.login.token.TokenProvider
import com.example.leetnote.ui.screens.login.token.TokenStorage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton  // use Hilt to inject token instead of ViewModel
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
    private val auth: FirebaseAuth,
    private val tokenStorage: TokenStorage,
): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        /*
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        val currentToken = runBlocking { tokenProvider.getToken() }
        currentToken?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        return chain.proceed(requestBuilder.build())
    }

         */
        var request = chain.request()

        val token = runBlocking { getValidToken() }
        token?.let {
            request = request.newBuilder()
                .header("Authorization", "Bearer $it")
                .build()
        }

        var response = chain.proceed(request)

        // 2️⃣ Retry once if unauthorized
        if (response.code == 401 || response.code == 403) {
            response.close()
            val refreshedToken = runBlocking { refreshToken() }
            refreshedToken?.let {
                val newRequest = request.newBuilder()
                    .header("Authorization", "Bearer $it")
                    .build()
                response = chain.proceed(newRequest)
            }
        }

        return response
    }

    // Get token from storage, refresh if null or expired
    private suspend fun getValidToken(): String? {
        val storedToken = tokenProvider.getToken()
        if (storedToken.isNullOrEmpty()) {
            return refreshToken()
        }
        return storedToken
    }

    // Force refresh from Firebase
    private suspend fun refreshToken(): String? = suspendCancellableCoroutine { cont ->
        auth.currentUser?.getIdToken(true)
            ?.addOnSuccessListener { result ->
                val newToken = result.token
                tokenStorage.updateToken(newToken)
                cont.resume(newToken) {}
            }
            ?.addOnFailureListener {
                cont.resume(null) {}
            }
    }
}