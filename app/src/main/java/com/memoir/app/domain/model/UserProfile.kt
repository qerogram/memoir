package com.memoir.app.domain.model

import java.time.ZonedDateTime

/**
 * User profile domain model
 */
data class UserProfile(
    val userId: String,
    val name: String,
    val role: String,
    val industryCode: IndustryCode,
    val growthGoals: String,
    val photoUrl: String? = null,
    val onboardingCompletedAt: ZonedDateTime? = null,
    val locale: String = "kr",
    val profileLocked: Boolean = false
)
