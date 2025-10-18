package com.example.leetnote.data.api

import com.example.leetnote.data.model.PageResponse
import com.example.leetnote.data.model.ProblemDetailDTO
import com.example.leetnote.data.model.ProblemListDTO
import com.example.leetnote.data.model.SetUsernameRequest
import com.example.leetnote.data.model.SubmissionDTO
import com.example.leetnote.data.model.UpdateProfileRequest
import com.example.leetnote.data.model.UserProfileDTO
import com.example.leetnote.data.repository.EvaluationDTO
import com.example.leetnote.data.repository.LeetcodeStatsDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LeetnoteApiService {

    @GET("api/users/profile")
    suspend fun getUserProfile(
    ): UserProfileDTO

    @PUT("api/users/username")
    suspend fun setUsername(
        @Body request: SetUsernameRequest
    ): UserProfileDTO

    @PUT("api/users/profile-picture")
    suspend fun uploadProfilePicture(
        @Body request: UpdateProfileRequest
    ): String

    @DELETE("api/users/profile-picture")
    suspend fun deleteProfilePicture(): Response<Unit>

    @GET("problems")
    suspend fun getAllProblems(
        @Query("keyword") keyword: String? = null,
        @Query("difficulties") difficulty: String? = null,
        @Query("isSolved") isSolved: Boolean? = null,
        @Query("isFavorite") isFavorite: Boolean? = null,
        @Query("page") page: Int = 0,
        @Query("size") pageSize: Int = 20
    ): PageResponse<ProblemListDTO>

    @GET("problems/detail")
    suspend fun getProblemDetail(
        @Query("problemId") problemId: Long,
    ): Response<ProblemDetailDTO>

    @PUT("problems/{problemId}/status")
    suspend fun updateProblemStatus(
        @Path("problemId") problemId: Long,
        @Query("isSolved") isSolved: Boolean,
        @Query("isFavorite") isFavorite: Boolean
    ): ProblemListDTO

    @GET("submissions/last")
    suspend fun getLastSubmission(
        @Query("problemId") problemId: Long,
    ): Response<SubmissionDTO.Submission>

    @POST("evaluations")
    suspend fun createEvaluation(
        @Body request: SubmissionDTO.SubmissionRequest
    ): EvaluationDTO

    @GET("evaluations/last")
    suspend fun getLastEvaluation(
        @Query("problemId") problemId: Long
    ): Response<EvaluationDTO>

    @GET("evaluations/all")
    suspend fun getAllEvaluations(
        @Query("problemId") problemId: Long
    ): List<EvaluationDTO>

    @GET("api/leetcode/{username}")
    suspend fun getUserStats(
        @Path("username") username: String
    ): Response<LeetcodeStatsDTO>
}