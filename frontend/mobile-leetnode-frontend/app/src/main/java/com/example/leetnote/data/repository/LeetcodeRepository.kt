package com.example.leetnote.data.repository

import com.example.leetnote.data.api.LeetnoteApiService
import com.example.leetnote.data.model.LeetcodeStatsDTO
import com.example.leetnote.data.model.SetUsernameRequest
import javax.inject.Inject

class LeetcodeRepository @Inject constructor(
    private val api: LeetnoteApiService
) {
    /**
     * Get user's stored LeetCode stats from database
     * Returns null if user hasn't set their LeetCode username yet
     */
    suspend fun getLeetcodeProfile(): LeetcodeStatsDTO? {
        val response = api.getLeetcodeProfile()
        return if (response.isSuccessful) response.body() else null
    }

    /**
     * Set or update LeetCode username and fetch fresh stats
     * This will save the username and stats to database
     */
    suspend fun setLeetcodeUsername(username: String): LeetcodeStatsDTO? {
        val request = SetUsernameRequest(username)
        val response = api.setLeetcodeUsername(request)
        return if (response.isSuccessful) response.body() else null
    }

    /**
     * Update/change LeetCode username to a new one
     */
    suspend fun updateLeetcodeUsername(username: String): LeetcodeStatsDTO? {
        val request = SetUsernameRequest(username)
        val response = api.updateLeetcodeUsername(request)
        return if (response.isSuccessful) response.body() else null
    }

    /**
     * Refresh stats from LeetCode API (for existing username)
     * Use this when user wants to update their stats manually
     */
    suspend fun refreshLeetcodeStats(): LeetcodeStatsDTO? {
        val response = api.refreshLeetcodeStats()
        return if (response.isSuccessful) response.body() else null
    }
}
