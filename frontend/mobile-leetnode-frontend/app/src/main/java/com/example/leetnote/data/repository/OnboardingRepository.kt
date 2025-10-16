package com.example.leetnote.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.leetnote.data.datastore.OnboardingPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OnboardingRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[OnboardingPreferences.ONBOARDING_COMPLETED] = completed
        }
    }

    fun isOnboardingCompleted(): Flow<Boolean?> {
        return dataStore.data.map { prefs ->
            prefs[OnboardingPreferences.ONBOARDING_COMPLETED]
        }
    }
}