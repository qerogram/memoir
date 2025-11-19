package com.memoir.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for ValidationUtils
 */
class ValidationUtilsTest {

    @Test
    fun `validateKoreanName should accept valid 2-character Korean name`() {
        // When
        val result = ValidationUtils.validateKoreanName("김철수")

        // Then
        assertTrue(result.isValid)
        assertNull(result.errorOrNull())
    }

    @Test
    fun `validateKoreanName should accept valid 4-character Korean name`() {
        // When
        val result = ValidationUtils.validateKoreanName("홍길동순")

        // Then
        assertTrue(result.isValid)
        assertNull(result.errorOrNull())
    }

    @Test
    fun `validateKoreanName should reject single character name`() {
        // When
        val result = ValidationUtils.validateKoreanName("김")

        // Then
        assertFalse(result.isValid)
        assertNotNull(result.errorOrNull())
        assertEquals("한글 2~4자로 입력해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateKoreanName should reject name longer than 4 characters`() {
        // When
        val result = ValidationUtils.validateKoreanName("홍길동순자")

        // Then
        assertFalse(result.isValid)
        assertEquals("한글 2~4자로 입력해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateKoreanName should reject English name`() {
        // When
        val result = ValidationUtils.validateKoreanName("John")

        // Then
        assertFalse(result.isValid)
        assertEquals("한글 2~4자로 입력해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateKoreanName should reject blank name`() {
        // When
        val result = ValidationUtils.validateKoreanName("")

        // Then
        assertFalse(result.isValid)
        assertEquals("이름을 입력해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateKoreanName should reject name with numbers`() {
        // When
        val result = ValidationUtils.validateKoreanName("홍길동1")

        // Then
        assertFalse(result.isValid)
        assertEquals("한글 2~4자로 입력해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateRole should accept valid role`() {
        // When
        val result = ValidationUtils.validateRole("백엔드 개발자")

        // Then
        assertTrue(result.isValid)
        assertNull(result.errorOrNull())
    }

    @Test
    fun `validateRole should accept role with English and Korean`() {
        // When
        val result = ValidationUtils.validateRole("Senior Backend Developer")

        // Then
        assertTrue(result.isValid)
        assertNull(result.errorOrNull())
    }

    @Test
    fun `validateRole should reject blank role`() {
        // When
        val result = ValidationUtils.validateRole("")

        // Then
        assertFalse(result.isValid)
        assertEquals("직무를 입력해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateRole should reject single character role`() {
        // When
        val result = ValidationUtils.validateRole("개")

        // Then
        assertFalse(result.isValid)
        assertEquals("직무를 2~64자로 입력해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateRole should reject role longer than 64 characters`() {
        // When
        val role = "a".repeat(65)
        val result = ValidationUtils.validateRole(role)

        // Then
        assertFalse(result.isValid)
        assertEquals("직무를 2~64자로 입력해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateGrowthGoals should accept valid 100+ character goals`() {
        // When
        val goals = "올해 목표는 FastAPI와 Kotlin을 마스터하는 것입니다. " +
                "클린 아키텍처를 적용한 실무 프로젝트를 완성하고, " +
                "테스트 커버리지 80% 이상을 달성하겠습니다."
        val result = ValidationUtils.validateGrowthGoals(goals)

        // Then
        assertTrue(result.isValid)
        assertNull(result.errorOrNull())
    }

    @Test
    fun `validateGrowthGoals should reject goals shorter than 100 characters`() {
        // When
        val goals = "짧은 목표입니다"
        val result = ValidationUtils.validateGrowthGoals(goals)

        // Then
        assertFalse(result.isValid)
        assertEquals("100자 이상 작성해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateGrowthGoals should reject goals longer than 500 characters`() {
        // When
        val goals = "a".repeat(501)
        val result = ValidationUtils.validateGrowthGoals(goals)

        // Then
        assertFalse(result.isValid)
        assertEquals("500자 이하로 작성해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateGrowthGoals should reject blank goals`() {
        // When
        val result = ValidationUtils.validateGrowthGoals("")

        // Then
        assertFalse(result.isValid)
        assertEquals("성장 목표를 입력해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateIndustry should accept valid industry code`() {
        // When
        val result = ValidationUtils.validateIndustry("STARTUP")

        // Then
        assertTrue(result.isValid)
        assertNull(result.errorOrNull())
    }

    @Test
    fun `validateIndustry should reject null industry code`() {
        // When
        val result = ValidationUtils.validateIndustry(null)

        // Then
        assertFalse(result.isValid)
        assertEquals("업종을 선택해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateIndustry should reject blank industry code`() {
        // When
        val result = ValidationUtils.validateIndustry("")

        // Then
        assertFalse(result.isValid)
        assertEquals("업종을 선택해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateLegalAgreements should accept when all agreements are true`() {
        // When
        val result = ValidationUtils.validateLegalAgreements(
            tosAccepted = true,
            privacyAccepted = true,
            depositAccepted = true
        )

        // Then
        assertTrue(result.isValid)
        assertNull(result.errorOrNull())
    }

    @Test
    fun `validateLegalAgreements should reject when TOS is not accepted`() {
        // When
        val result = ValidationUtils.validateLegalAgreements(
            tosAccepted = false,
            privacyAccepted = true,
            depositAccepted = true
        )

        // Then
        assertFalse(result.isValid)
        assertEquals("모든 약관에 동의해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateLegalAgreements should reject when Privacy is not accepted`() {
        // When
        val result = ValidationUtils.validateLegalAgreements(
            tosAccepted = true,
            privacyAccepted = false,
            depositAccepted = true
        )

        // Then
        assertFalse(result.isValid)
        assertEquals("모든 약관에 동의해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateLegalAgreements should reject when Deposit is not accepted`() {
        // When
        val result = ValidationUtils.validateLegalAgreements(
            tosAccepted = true,
            privacyAccepted = true,
            depositAccepted = false
        )

        // Then
        assertFalse(result.isValid)
        assertEquals("모든 약관에 동의해 주세요", result.errorOrNull())
    }

    @Test
    fun `validateLegalAgreements should reject when all agreements are false`() {
        // When
        val result = ValidationUtils.validateLegalAgreements(
            tosAccepted = false,
            privacyAccepted = false,
            depositAccepted = false
        )

        // Then
        assertFalse(result.isValid)
        assertEquals("모든 약관에 동의해 주세요", result.errorOrNull())
    }
}
