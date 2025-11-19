package com.memoir.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for profile creation request
 */
@Serializable
data class ProfileRequest(
    @SerialName("name")
    val name: String,

    @SerialName("role")
    val role: String,

    @SerialName("industry_code")
    val industryCode: String,

    @SerialName("growth_goals")
    val growthGoals: String
)

/**
 * DTO for profile response
 */
@Serializable
data class ProfileResponse(
    @SerialName("user_id")
    val userId: String,

    @SerialName("profile")
    val profile: ProfileData,

    @SerialName("onboarding_completed_at")
    val onboardingCompletedAt: String
)

@Serializable
data class ProfileData(
    @SerialName("name")
    val name: String,

    @SerialName("role")
    val role: String,

    @SerialName("industry_code")
    val industryCode: String,

    @SerialName("growth_goals")
    val growthGoals: String,

    @SerialName("photo_url")
    val photoUrl: String? = null
)

/**
 * DTO for user info response
 */
@Serializable
data class UserMeResponse(
    @SerialName("user")
    val user: UserData,

    @SerialName("profile")
    val profile: ProfileData?,

    @SerialName("flags")
    val flags: UserFlags
)

@Serializable
data class UserData(
    @SerialName("id")
    val id: String,

    @SerialName("kakao_id")
    val kakaoId: String,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("last_login_at")
    val lastLoginAt: String,

    @SerialName("status")
    val status: String
)

@Serializable
data class UserFlags(
    @SerialName("onboarding_completed")
    val onboardingCompleted: Boolean
)
