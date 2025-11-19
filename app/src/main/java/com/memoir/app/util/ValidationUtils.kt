package com.memoir.app.util

/**
 * Validation utilities for user input
 * Implements Korean name validation and other field validations
 */
object ValidationUtils {

    /**
     * Validates Korean name (hangul characters only, 2-4 characters)
     * Regex: ^[가-힣]{2,4}$
     */
    fun validateKoreanName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult.Error("이름을 입력해 주세요")
        }

        val koreanNameRegex = Regex("^[가-힣]{2,4}$")
        return if (koreanNameRegex.matches(name)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Error("한글 2~4자로 입력해 주세요")
        }
    }

    /**
     * Validates professional role/title (2-64 characters)
     * Accepts Korean, English letters, and spaces
     */
    fun validateRole(role: String): ValidationResult {
        if (role.isBlank()) {
            return ValidationResult.Error("직무를 입력해 주세요")
        }

        return if (role.length in 2..64) {
            ValidationResult.Valid
        } else {
            ValidationResult.Error("직무를 2~64자로 입력해 주세요")
        }
    }

    /**
     * Validates growth goals (100-500 characters)
     * Enforces minimum length to ensure quality input
     */
    fun validateGrowthGoals(goals: String): ValidationResult {
        if (goals.isBlank()) {
            return ValidationResult.Error("성장 목표를 입력해 주세요")
        }

        return when {
            goals.length < 100 -> ValidationResult.Error("100자 이상 작성해 주세요")
            goals.length > 500 -> ValidationResult.Error("500자 이하로 작성해 주세요")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates industry code (must be valid enum value)
     */
    fun validateIndustry(industryCode: String?): ValidationResult {
        return if (industryCode.isNullOrBlank()) {
            ValidationResult.Error("업종을 선택해 주세요")
        } else {
            ValidationResult.Valid
        }
    }

    /**
     * Validates that all legal agreements are accepted
     */
    fun validateLegalAgreements(
        tosAccepted: Boolean,
        privacyAccepted: Boolean,
        depositAccepted: Boolean
    ): ValidationResult {
        return if (tosAccepted && privacyAccepted && depositAccepted) {
            ValidationResult.Valid
        } else {
            ValidationResult.Error("모든 약관에 동의해 주세요")
        }
    }
}

/**
 * Validation result sealed class
 */
sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Error(val message: String) : ValidationResult()

    val isValid: Boolean
        get() = this is Valid

    fun errorOrNull(): String? = when (this) {
        is Error -> message
        else -> null
    }
}
