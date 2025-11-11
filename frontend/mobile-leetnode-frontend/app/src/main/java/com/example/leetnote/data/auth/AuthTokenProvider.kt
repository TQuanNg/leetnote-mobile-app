package com.example.leetnote.data.auth

import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenProvider @Inject constructor(
    private val tokenStorage: TokenStorage
) : TokenProvider {
    override suspend fun getToken(): String? = tokenStorage.token.firstOrNull()
}