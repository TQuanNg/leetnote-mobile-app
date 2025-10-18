package com.example.leetnote.data.repository

import com.example.leetnote.data.api.LeetnoteApiService
import javax.inject.Inject

class LeetcodeRepository @Inject constructor(
    private val api: LeetnoteApiService
) {
    suspend fun getUserStats(username: String): LeetcodeStatsDTO? {
        val response = api.getUserStats(username)
        return if (response.isSuccessful) response.body() else null
    }
}

data class LeetcodeStatsDTO(
    val username: String,
    val totalSolved: Int,
    val easySolved: Int,
    val mediumSolved: Int,
    val hardSolved: Int,
)
