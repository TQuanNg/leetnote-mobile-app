package com.example.leetnote.ui.screens.login.token

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/*
This class mangages the token whether persited of deleted across app restarts.
It uses SharedPreferences to persist the token.
 */
@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _token = MutableStateFlow<String?>(prefs.getString("id_token", null))
    val token: StateFlow<String?> = _token

    fun updateToken(newToken: String?) {
        _token.value = newToken

        // Save or clear in SharedPreferences
        with(prefs.edit()) {
            if (newToken != null) {
                putString("id_token", newToken)
            } else {
                remove("id_token")
            }
            apply()
        }
    }

    fun clearToken() {
        updateToken(null)
    }
}