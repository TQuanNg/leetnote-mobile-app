package com.example.leetnote.data.repository

import com.example.leetnote.ui.screens.login.token.TokenStorage
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SessionManager @Inject constructor(
    private val tokenStorage: TokenStorage
) {
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    fun updateUser(user: FirebaseUser?) {
        _currentUser.value = user
        user?.let { tokenStorage.updateToken(it.uid) }
    }
}


