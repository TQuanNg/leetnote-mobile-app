package com.example.leetnote.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey


object OnboardingPreferences {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
}