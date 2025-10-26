package com.example.leetnote.data.repository

import com.example.leetnote.data.api.LeetnoteApiService
import com.example.leetnote.data.model.LeetcodeStatsDTO
import javax.inject.Inject

class LeetcodeRepository @Inject constructor(
    private val api: LeetnoteApiService
) {
    suspend fun getUserStats(username: String): LeetcodeStatsDTO? {
        val response = api.getUserStats(username)
        return if (response.isSuccessful) response.body() else null
    }
}
