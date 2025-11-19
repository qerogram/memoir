package com.memoir.app.domain.usecase

import com.memoir.app.domain.model.UserProfile
import com.memoir.app.domain.repository.UserRepository
import com.memoir.app.util.Result
import com.memoir.app.util.ValidationResult
import com.memoir.app.util.ValidationUtils
import javax.inject.Inject

/**
 * Use case for saving user profile
 */
class SaveProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(profile: UserProfile): Result<Unit> {
        // Validate all fields
        val nameValidation = ValidationUtils.validateKoreanName(profile.name)
        if (nameValidation is ValidationResult.Error) {
            return Result.Error(nameValidation.message)
        }

        val roleValidation = ValidationUtils.validateRole(profile.role)
        if (roleValidation is ValidationResult.Error) {
            return Result.Error(roleValidation.message)
        }

        val goalsValidation = ValidationUtils.validateGrowthGoals(profile.growthGoals)
        if (goalsValidation is ValidationResult.Error) {
            return Result.Error(goalsValidation.message)
        }

        val industryValidation = ValidationUtils.validateIndustry(profile.industryCode.name)
        if (industryValidation is ValidationResult.Error) {
            return Result.Error(industryValidation.message)
        }

        // Save profile
        return userRepository.saveProfile(profile)
    }
}
