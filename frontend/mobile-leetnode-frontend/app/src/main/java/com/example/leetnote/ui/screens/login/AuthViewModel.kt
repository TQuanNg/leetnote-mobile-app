package com.example.leetnote.ui.screens.login

import android.content.ContentValues.TAG
import android.content.Context
import android.credentials.GetCredentialException
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leetnote.R
import com.example.leetnote.data.auth.TokenStorage
import com.example.leetnote.data.repository.UserRepository
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenStorage: TokenStorage,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _displayName = MutableStateFlow<String?>(auth.currentUser?.displayName)
    val displayName: StateFlow<String?> = _displayName.asStateFlow()

    private val _idToken = MutableStateFlow<String?>(null)
    val idToken: StateFlow<String?> = _idToken.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _currentUser.value = auth.currentUser
                    _displayName.value = auth.currentUser?.displayName
                    _errorMessage.value = null
                    fetchIdToken()
                } else {
                    _errorMessage.value = task.exception?.message
                }
            }
    }

    fun signup(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _currentUser.value = auth.currentUser
                    _errorMessage.value = null
                    fetchIdToken()

                    viewModelScope.launch {
                        val token = _idToken.value
                        token?.let {
                            userRepository.setUsername(displayName.value ?: "New User")
                        }
                    }
                } else {
                    _errorMessage.value = task.exception?.message
                }
            }
    }


    suspend fun initGoogleLogin() {
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
            serverClientId = context.getString(R.string.web_client_id)
        ).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        coroutineScope {
            try {
                val credentialManager = CredentialManager.create(context)

                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                handleSignIn(result)
            } catch (e: GetCredentialCancellationException) {
                Log.e(TAG, "User cancelled the credential request", e)
            } catch (e: GetCredentialException) {
                e.printStackTrace()
            }
        }
    }

    fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        Log.d(TAG, "Received Google ID token: $idToken")

                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    _currentUser.value = auth.currentUser
                                    _errorMessage.value = null
                                    fetchIdToken()
                                } else {
                                    _errorMessage.value = task.exception?.message
                                }
                            }


                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    Log.e(TAG, "Unexpected type of credential")
                }
            }

            else -> {
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    fun fetchIdToken() {
        val user = auth.currentUser
        user?.getIdToken(true)?.addOnSuccessListener { result ->
            val token = result.token
            _idToken.value = token
            tokenStorage.updateToken(token)
            Log.d("AuthViewModel", "Fetched ID token: ${result.token}")
        }?.addOnFailureListener {
            Log.e("AuthViewModel", "Failed to fetch ID token", it)
        }
    }

    fun logout() {
        auth.signOut()
        _idToken.value = null
        _currentUser.value = null
        tokenStorage.clearToken()
    }
}