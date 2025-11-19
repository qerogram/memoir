package com.memoir.app.domain.model

/**
 * Industry code enum with Korean display names
 * Aligned with Korean job market terminology
 */
enum class IndustryCode(val displayNameResId: Int) {
    STARTUP(com.memoir.app.R.string.industry_startup),           // 스타트업
    ENTERPRISE(com.memoir.app.R.string.industry_enterprise),     // 대기업
    SME(com.memoir.app.R.string.industry_sme),                   // 중소기업
    PUBLIC(com.memoir.app.R.string.industry_public),             // 공기업/공공기관
    FOREIGN(com.memoir.app.R.string.industry_foreign),           // 외국계 기업
    FREELANCER(com.memoir.app.R.string.industry_freelancer),     // 프리랜서/1인 기업
    NONPROFIT(com.memoir.app.R.string.industry_nonprofit),       // 비영리/사회적 기업
    OTHER(com.memoir.app.R.string.industry_other);               // 기타

    companion object {
        fun fromString(value: String): IndustryCode? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
