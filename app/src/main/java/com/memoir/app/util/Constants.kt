package com.memoir.app.util

/**
 * Application constants
 */
object Constants {

    // API Configuration
    const val API_BASE_URL = "https://api.memoir.app/" // TODO: Update with actual backend URL
    const val API_TIMEOUT_SECONDS = 30L

    // Token Expiry (in days)
    const val ACCESS_TOKEN_EXPIRY_DAYS = 30
    const val REFRESH_TOKEN_EXPIRY_DAYS = 90

    // DataStore Keys
    object DataStoreKeys {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val ACCESS_TOKEN_EXPIRY = "access_token_expiry"
        const val REFRESH_TOKEN_EXPIRY = "refresh_token_expiry"
        const val ONBOARDING_COMPLETED = "onboarding_completed"
        const val USER_ID = "user_id"
    }

    // Analytics Event Names
    object Analytics {
        const val KAKAO_LOGIN_INITIATED = "kakao_login_initiated"
        const val KAKAO_LOGIN_SUCCESS = "kakao_login_success"
        const val KAKAO_LOGIN_FAILURE = "kakao_login_failure"
        const val ONBOARDING_STARTED = "onboarding_started"
        const val ONBOARDING_SCREEN_VIEWED = "onboarding_screen_viewed"
        const val ONBOARDING_ABANDONED = "onboarding_abandoned"
        const val ONBOARDING_COMPLETED = "onboarding_completed"
        const val PROFILE_SUBMISSION_STARTED = "profile_submission_started"
        const val PROFILE_SUBMISSION_SUCCESS = "profile_submission_success"
        const val PROFILE_SUBMISSION_FAILURE = "profile_submission_failure"
        const val TERMS_ACCEPTED = "terms_accepted"
        const val SESSION_TOKEN_REFRESHED = "session_token_refreshed"
    }

    // Analytics Event Properties
    object AnalyticsProperties {
        const val SOURCE = "source"
        const val USER_NEW = "user_new"
        const val REASON = "reason"
        const val ERROR_CODE = "error_code"
        const val SCREEN_INDEX = "screen_index"
        const val LAST_SCREEN_INDEX = "last_screen_index"
        const val GROWTH_GOALS_LENGTH = "growth_goals_length"
        const val INDUSTRY_CODE = "industry_code"
        const val TOS_VERSION = "tos_version"
        const val PRIVACY_VERSION = "privacy_version"
        const val ROTATION = "rotation"
    }

    // Onboarding
    const val TOTAL_ONBOARDING_SCREENS = 5
    const val MIN_GROWTH_GOALS_LENGTH = 100
    const val MAX_GROWTH_GOALS_LENGTH = 500

    // Error Codes
    object ErrorCodes {
        const val INVALID_CODE = "INVALID_CODE"
        const val KAKAO_UPSTREAM_ERROR = "KAKAO_UPSTREAM_ERROR"
        const val VALIDATION_ERROR = "VALIDATION_ERROR"
        const val PROFILE_ALREADY_SET = "PROFILE_ALREADY_SET"
        const val UNAUTHORIZED = "UNAUTHORIZED"
        const val TOKEN_INVALID = "TOKEN_INVALID"
        const val TOKEN_EXPIRED = "TOKEN_EXPIRED"
        const val TOKEN_ROTATION_CONFLICT = "TOKEN_ROTATION_CONFLICT"
        const val NETWORK_ERROR = "NETWORK_ERROR"
        const val UNKNOWN_ERROR = "UNKNOWN_ERROR"
    }
}
