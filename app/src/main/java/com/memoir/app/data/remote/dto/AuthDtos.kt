package com.memoir.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for Kakao authentication request
 */
@Serializable
data class KakaoAuthRequest(
    @SerialName("kakao_oauth_code")
    val kakaoOauthCode: String
)

/**
 * DTO for authentication response
 */
@Serializable
data class AuthResponse(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("refresh_token")
    val refreshToken: String,

    @SerialName("user_exists")
    val userExists: Boolean,

    @SerialName("onboarding_completed")
    val onboardingCompleted: Boolean
)

/**
 * DTO for token refresh request
 */
@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)

/**
 * DTO for error response
 */
@Serializable
data class ErrorResponse(
    @SerialName("error")
    val error: ErrorDetail
)

@Serializable
data class ErrorDetail(
    @SerialName("code")
    val code: String,

    @SerialName("message")
    val message: String,

    @SerialName("details")
    val details: Map<String, String>? = null
)
