package com.example.leetnote.data.auth

interface TokenProvider {
    suspend fun getToken(): String?
}