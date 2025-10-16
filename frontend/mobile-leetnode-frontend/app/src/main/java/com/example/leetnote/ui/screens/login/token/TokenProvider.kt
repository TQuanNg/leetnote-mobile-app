package com.example.leetnote.ui.screens.login.token

interface TokenProvider {
    suspend fun getToken(): String?
}