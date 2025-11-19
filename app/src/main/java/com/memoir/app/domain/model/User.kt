package com.memoir.app.domain.model

import java.time.ZonedDateTime

/**
 * User domain model
 */
data class User(
    val id: String,
    val kakaoId: String,
    val createdAt: ZonedDateTime,
    val lastLoginAt: ZonedDateTime,
    val status: UserStatus
)

enum class UserStatus {
    ACTIVE,
    INACTIVE,
    LOCKED
}
