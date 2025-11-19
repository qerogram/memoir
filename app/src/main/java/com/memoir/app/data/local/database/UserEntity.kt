package com.memoir.app.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for User table
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "kakao_id")
    val kakaoId: String,

    @ColumnInfo(name = "name")
    val name: String? = null,

    @ColumnInfo(name = "role")
    val role: String? = null,

    @ColumnInfo(name = "industry_code")
    val industryCode: String? = null,

    @ColumnInfo(name = "growth_goals")
    val growthGoals: String? = null,

    @ColumnInfo(name = "photo_url")
    val photoUrl: String? = null,

    @ColumnInfo(name = "onboarding_completed_at")
    val onboardingCompletedAt: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "last_login_at")
    val lastLoginAt: String
)
