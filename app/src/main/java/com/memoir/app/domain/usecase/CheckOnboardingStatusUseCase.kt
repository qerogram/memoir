package com.memoir.app.domain.usecase

import com.memoir.app.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case for checking onboarding completion status
 */
class CheckOnboardingStatusUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(): Boolean {
        return userRepository.getOnboardingStatus()
    }
}
