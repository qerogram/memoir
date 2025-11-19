package com.memoir.app.domain.repository

import com.memoir.app.domain.model.User
import com.memoir.app.domain.model.UserProfile
import com.memoir.app.util.Result

/**
 * User repository interface
 */
interface UserRepository {

    /**
     * Save user profile
     */
    suspend fun saveProfile(profile: UserProfile): Result<Unit>

    /**
     * Get current user
     */
    suspend fun getCurrentUser(): Result<User?>

    /**
     * Update onboarding completion status
     */
    suspend fun updateOnboardingStatus(completed: Boolean)

    /**
     * Get onboarding completion status
     */
    suspend fun getOnboardingStatus(): Boolean
}
