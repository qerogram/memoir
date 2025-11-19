package com.memoir.app.data.remote.api

import com.memoir.app.data.remote.dto.ProfileRequest
import com.memoir.app.data.remote.dto.ProfileResponse
import com.memoir.app.data.remote.dto.UserMeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * User API interface
 */
interface UserApi {

    /**
     * Get current user profile
     * GET /api/v1/users/me
     */
    @GET("api/v1/users/me")
    suspend fun getCurrentUser(): Response<UserMeResponse>

    /**
     * Submit user profile
     * POST /api/v1/users/profile
     */
    @POST("api/v1/users/profile")
    suspend fun submitProfile(
        @Body request: ProfileRequest
    ): Response<ProfileResponse>
}
