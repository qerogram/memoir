# 상세 설계: Onboarding Flow (온보딩 플로우)

**프로젝트:** Memoir - 한국 직장인 회고 커뮤니티 플랫폼
**모듈:** User Authentication & Onboarding - Phase 1 MVP
**플랫폼:** Android (Kotlin + Jetpack Compose)
**작성일:** 2025년 11월 19일

---

## 목차

1. [개요](#개요)
2. [5단계 온보딩 스크린](#5단계-온보딩-스크린)
3. [상태 관리 & 진행 추적](#상태-관리--진행-추적)
4. [진행 표시기](#진행-표시기)
5. [사용자 이동 경로](#사용자-이동-경로)
6. [폼 검증 & 에러 처리](#폼-검증--에러-처리)
7. [한국화 고려사항](#한국화-고려사항)
8. [심리학적 설계](#심리학적-설계)
9. [약관 & 법규 준수](#약관--법규-준수)
10. [분석 이벤트](#분석-이벤트)
11. [테스트 시나리오](#테스트-시나리오)
12. [아키텍처 패턴](#아키텍처-패턴)

---

## 개요

### 목표

온보딩 플로우는 Memoir의 가치 제안을 명확히 전달하고, 사용자가 **10주 사이클 기반의 회고 커뮤니티**와 **보증금 시스템**을 이해한 후, 기본 프로필 정보를 수집하는 5단계 필수 여정입니다.

**핵심 목표:**
- 일상의 배움이 휘발되는 문제 인식 → 공감
- 다양한 전문가와의 연결 가치 → 사회적 증거
- 구조화된 10주 사이클의 이점 → 신뢰
- 투명한 보증금 시스템 설명 → 법적 보호 & 신뢰
- 프로필 정보 수집 → 코호트 매칭 데이터

### 성공 지표

- 온보딩 완료율: **65%+** (1기 참여자 기준)
- 평균 소요 시간: **3-5분**
- 보증금 시스템 이해도: **70%+** (설문조사)
- 스크린별 이탈률: **<10%**
- 프로필 제출 성공률: **95%+**

---

## 5단계 온보딩 스크린

### Screen 1: 문제 정의 (Problem Framing)

**목적:** 감정적 구매 시작 - 사용자의 통증점 인식

**와이어프레임 및 레이아웃:**

```
┌─────────────────────────────────┐
│                                 │
│   [상단 여백]                    │
│                                 │
│   💭                             │ (또는 일러스트)
│   [도표/일러스트: 시간에 따른    │
│    배움의 휘발]                   │
│                                 │
│   [중간 여백]                    │
│                                 │
│   일상의 배움이                  │
│   휘발되고 있지 않나요?           │
│                                 │
│   매주 마주하는 경험들,          │
│   좋은 인사이트들이               │
│   기억 속에서 사라지고 있습니다.  │
│                                 │
│   Memoir는 이 문제를             │
│   해결하기 위해 만들었습니다.    │
│                                 │
│   [하단 여백]                    │
│                                 │
│  [다음] (Next Button - 활성화)  │
│                                 │
└─────────────────────────────────┘
```

**텍스트 컨텐츠:**

```
헤드라인:
"일상의 배움이 휘발되고 있지 않나요?"

본문:
매주 마주하는 경험들, 좋은 인사이트들이
기억 속에서 사라지고 있습니다.

Memoir는 이 문제를 해결하기 위해 만들었습니다.
```

**디자인 요소:**

- 배경색: 흰색 (Memoir 베이스)
- 헤드라인 텍스트 크기: 24-28sp (굵은 서체)
- 본문 텍스트 크기: 16sp (일반 서체)
- 칼라: 텍스트는 #124234 (dark olive)
- 일러스트: 시간에 따라 뿌연 기억으로 변하는 경험 표현
- Next 버튼: #F4BA54 (mustard) 배경, 전폭
- 터치 타겟: 최소 48dp

**상호작용:**

- Next 버튼 탭: Screen 2로 진행
- 뒤로가기 버튼: 앱 종료 확인 다이얼로그
- 비활성 상태: 없음 (항상 활성화)

---

### Screen 2: 커뮤니티 가치 (Community Value)

**목적:** 해결책 제시 - 커뮤니티 기반의 해결안

**와이어프레임 및 레이아웃:**

```
┌─────────────────────────────────┐
│                                 │
│   [상단 여백]                    │
│                                 │
│   👥👥👥                         │ (또는 일러스트)
│   [도표/일러스트: 다양한         │
│    분야의 전문가들 연결]          │
│                                 │
│   [중간 여백]                    │
│                                 │
│   다양한 분야의                  │
│   성장하는 사람들과 함께          │
│                                 │
│   코호트 기반 학습:               │
│   • 매주 배움 공유                │
│   • 동료의 피드백 수신            │
│   • 의미 있는 관계 형성           │
│                                 │
│   1기 참여자의 65%가             │
│   전액 환급받았어요               │
│                                 │
│   [하단 여백]                    │
│                                 │
│  [이전] [다음]                   │
│                                 │
└─────────────────────────────────┘
```

**텍스트 컨텐츠:**

```
헤드라인:
"다양한 분야의 성장하는 사람들과 함께"

서브헤드라인:
"코호트 기반 학습"

주요 포인트:
• 매주 배움 공유 (Weekly Reflections)
• 동료의 피드백 수신 (Peer Feedback)
• 의미 있는 관계 형성 (Meaningful Connections)

사회적 증거:
"1기 참여자의 65%가 전액 환급받았어요"
```

**디자인 요소:**

- 배경색: 흰색
- 헤드라인: 24-28sp (굵은 서체)
- 포인트 리스트: 16sp, 불릿 포인트
- 칼라 강조: #E86221 (carrot orange)
- 사회적 증거 텍스트: 14sp, 기울임체, 회색 (#666)
- 일러스트: 다양한 직업 배경의 전문가 그룹

**상호작용:**

- Next 버튼: Screen 3로 진행
- Previous 버튼: Screen 1로 돌아가기
- 버튼 상태: 항상 활성화

---

### Screen 3: 구조 설명 (Structure Explanation)

**목적:** 서비스 구조 및 약속 명확화

**와이어프레임 및 레이아웃:**

```
┌─────────────────────────────────┐
│                                 │
│   [상단 여백]                    │
│                                 │
│   📊                             │ (또는 일러스트)
│   [도표/일러스트: 10주 사이클]    │
│                                 │
│   [중간 여백]                    │
│                                 │
│   10주 사이클 구조                │
│                                 │
│   • 8-10명의 코호트 (다양한 분야) │
│   • 매주 회고 작성 필수           │
│   • 월 3회 필수 만남             │
│     (온라인 또는 오프라인)        │
│   • 코호트원의 회고에 댓글 작성   │
│                                 │
│   기간: 10주 (약 2.5개월)        │
│   주당 소요 시간: 약 50분        │
│   (회고 30분 + 댓글 20분)        │
│                                 │
│   [하단 여백]                    │
│                                 │
│  [이전] [다음]                   │
│                                 │
└─────────────────────────────────┘
```

**텍스트 컨텐츠:**

```
헤드라인:
"10주 사이클 구조"

세부 사항:
• 8-10명의 다양한 분야 전문가
• 매주 회고 작성 (필수)
• 월 3회 필수 만남 (온라인/오프라인)
• 코호트원의 회고에 피드백 (필수)

시간 투자:
기간: 10주 (약 2.5개월)
주당 소요 시간: 약 50분
  - 회고 작성: ~30분
  - 댓글 작성: ~20분
```

**디자인 요소:**

- 배경색: 흰색
- 헤드라인: 24-28sp
- 구조도/일러스트: 10주 타임라인과 코호트 크기
- 주요 숫자: 굵은 서체로 강조 (#E86221)
- 서브 텍스트: 14sp, 회색 (#666)

**상호작용:**

- Next 버튼: Screen 4로 진행
- Previous 버튼: Screen 2로 돌아가기

---

### Screen 4: 보증금 시스템 (Accountability Mechanism)

**목적:** 투명한 재정 정보 제시 - 신뢰 구축

**와이어프레임 및 레이아웃:**

```
┌─────────────────────────────────┐
│                                 │
│   [상단 여백]                    │
│                                 │
│   💰                             │
│   [도표/일러스트: 보증금 시스템]  │
│                                 │
│   보증금 시스템                  │
│                                 │
│   초기 비용:                     │
│   ├─ 보증금: 20만 원             │
│   └─ 첫 달 수수료: 10만 원       │
│   합계: 30만 원                  │
│                                 │
│   매월 수수료: 10만 원           │
│                                 │
│   ─────────────────────────     │
│                                 │
│   환급 조건 (70%+ 완료 시):      │
│   ✓ 보증금 20만 원 전액 환급      │
│                                 │
│   벌금 (미준수 시):              │
│   ∘ 회고 미작성: 2만 원/회       │
│   ∘ 댓글 부족: 1만 원/회         │
│                                 │
│   성실히 참여하시면              │
│   보증금 전액 환급됩니다          │
│                                 │
│   약관 보기: [약관] [정책]       │
│                                 │
│   [하단 여백]                    │
│                                 │
│  [이전] [다음]                   │
│                                 │
└─────────────────────────────────┘
```

**텍스트 컨텐츠:**

```
헤드라인:
"투명한 보증금 시스템"

초기 투자:
┌──────────────────────────────┐
│ 보증금                  20만원 │
│ 첫 달 수수료            10만원 │
│ ────────────────────────────── │
│ 합계                   30만원 │
└──────────────────────────────┘

월별 수수료: 10만원

환급 조건:
성실한 참여 (70%+ 완료 시) → 보증금 전액 환급

벌금:
• 회고 미작성: 회당 2만원
• 댓글 부족: 회당 1만원

강조:
"성실히 참여하시면 보증금 전액 환급됩니다"

약관 & 정책:
[서비스 약관] [개인정보 정책] [보증금 정책]
```

**디자인 요소:**

- 배경색: 흰색
- 헤드라인: 24-28sp, 굵은 서체
- 비용 테이블: 모노스페이스 폰트, 박스 테두리
- 주요 수치: 24-28sp, 굵은 서체, #E86221 강조
- 환급 강조: #F4BA54 배경 박스
- 링크: 파란색 (#2563EB), 밑줄
- 일러스트: 화폐 기호, 환급 화살표 표현

**상호작용:**

- 약관 링크: 외부 브라우저에서 열기 (또는 in-app WebView)
- Next 버튼: 약관 동의 체크박스와 연계 (아래 참고)
- Previous 버튼: Screen 3로 돌아가기

---

### Screen 5: 프로필 설정 (Profile Setup)

**목적:** 기본 사용자 정보 수집 - 코호트 매칭 데이터

**와이어프레임 및 레이아웃:**

```
┌─────────────────────────────────┐
│   프로필 설정                    │
│                                 │
│   [스크롤 가능]                  │
│                                 │
│   이름                           │
│   [_____________ 예: 김철수]     │
│                                 │
│   직책/역할                      │
│   [_____________ 예: 제품 PM]    │
│                                 │
│   현재 산업                      │
│   [▼ 선택 - 스타트업 ▼]          │
│                                 │
│   성장 목표 (필수, 100자 이상)   │
│   [_______________________       │
│    _______________________       │
│    _______________________]      │
│   (0/500)                       │
│                                 │
│   프로필 사진 (선택사항)         │
│   [+사진 추가] 또는 [스킵]       │
│                                 │
│   ────────────────────────────   │
│                                 │
│   약관 동의 (필수):             │
│   ☑ 서비스 약관에 동의합니다     │
│   ☑ 개인정보 정책에 동의합니다   │
│   ☑ 보증금 정책에 동의합니다     │
│                                 │
│   ────────────────────────────   │
│                                 │
│   [완료] (버튼)                 │
│                                 │
└─────────────────────────────────┘
```

**텍스트 컨텐츠:**

```
레이블:
- 이름: "이름"
- 직책: "직책/역할"
- 산업: "현재 산업"
- 목표: "성장 목표"
- 사진: "프로필 사진 (선택사항)"

플레이스홀더:
- 이름: "예: 김철수"
- 직책: "예: 제품 PM, 마케팅 담당자"
- 산업: "선택하세요"
- 목표: "당신이 10주 동안 이루고 싶은 성장을
         구체적으로 설명해주세요. (최소 100자)"

성장 목표 힌트:
"예: 리더십 역량 강화를 통해 팀 문화를
개선하고, 데이터 기반 의사결정 능력을
키우는 것이 목표입니다."

약관 동의:
[ ] 서비스 약관에 동의합니다
[ ] 개인정보 정책에 동의합니다
[ ] 보증금 정책에 동의합니다

버튼:
- 완료 (조건: 모든 필수 항목 입력 & 약관 동의)
- 이전 (Screen 4로 돌아가기)
```

**데이터 검증 규칙:**

| 필드 | 요구사항 | 오류 메시지 |
|------|--------|-----------|
| 이름 | 한글 2-4자 | "한글 이름 2-4자를 입력해주세요" |
| 직책 | 2-48자, 한글+영문 | "2-48자를 입력해주세요" |
| 산업 | 필수 선택 | "산업을 선택해주세요" |
| 목표 | 100-500자 | "100자 이상 500자 이하로 입력해주세요" |
| 사진 | 선택사항 | - |
| 약관 | 모두 동의 필수 | "약관에 동의해주세요" |

**디자인 요소:**

- 배경색: 흰색
- 입력 필드: Material 3 OutlinedTextField
- 드롭다운: Material 3 Dropdown
- 체크박스: Material 3 Checkbox
- 오류 색상: #DC2626 (red)
- 성공 색상: #16A34A (green)
- 완료 버튼: 활성화 시 #E86221, 비활성화 시 #CCC
- 글자 수 표시: 16sp, 회색 (#999), 우측 정렬

**상호작용:**

- 이름 입력: 실시간 한글 검증
- 산업 선택: 드롭다운 열기 (7개 옵션)
- 성장 목표: 멀티라인 텍스트, 실시간 글자 수 계산
- 사진 추가: 카메라/갤러리 선택 또는 스킵
- 약관 링크: 탭 시 modal/WebView 열기
- 완료 버튼: 모든 검증 통과 후 활성화

---

## 상태 관리 & 진행 추적

### OnboardingState 정의

```kotlin
// Sealed class for tracking overall onboarding state
sealed class OnboardingState {
    object Idle : OnboardingState()
    object Loading : OnboardingState()
    data class ScreenDisplay(val screenIndex: Int) : OnboardingState()
    data class Error(val message: String) : OnboardingState()
    object Completed : OnboardingState()
}

// Data class for tracking current screen and form data
data class OnboardingData(
    val currentScreenIndex: Int = 0,  // 0-4 (5 screens)
    val formData: UserProfileForm = UserProfileForm(),
    val isTermsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val completedAt: Long? = null
)

// User profile form data
data class UserProfileForm(
    val name: String = "",
    val role: String = "",
    val industry: String = "",  // Industry enum code
    val growthGoals: String = "",
    val photoUrl: String? = null
)

// Validation errors
data class ValidationError(
    val field: String,
    val message: String
)
```

### ViewModel 상태 관리

```kotlin
// OnboardingViewModel implementation pattern
class OnboardingViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _onboardingState = MutableStateFlow<OnboardingState>(OnboardingState.Idle)
    val onboardingState: StateFlow<OnboardingState> = _onboardingState.asStateFlow()

    private val _onboardingData = MutableStateFlow(OnboardingData())
    val onboardingData: StateFlow<OnboardingData> = _onboardingData.asStateFlow()

    private val _validationErrors = MutableStateFlow<List<ValidationError>>(emptyList())
    val validationErrors: StateFlow<List<ValidationError>> = _validationErrors.asStateFlow()

    init {
        // 앱 시작 시 저장된 진행 상태 복원
        restoreSavedProgress()
        // 온보딩 시작 이벤트
        trackOnboardingStarted()
    }

    // Screen 진행
    fun goToNextScreen() {
        val currentIndex = _onboardingData.value.currentScreenIndex
        if (currentIndex < 4) {  // Screen 0-4
            _onboardingData.value = _onboardingData.value.copy(
                currentScreenIndex = currentIndex + 1
            )
            saveProgress()
            trackScreenViewed(currentIndex + 1)
        }
    }

    // 이전 화면 (Screen 5 제외)
    fun goToPreviousScreen() {
        val currentIndex = _onboardingData.value.currentScreenIndex
        if (currentIndex > 0) {
            _onboardingData.value = _onboardingData.value.copy(
                currentScreenIndex = currentIndex - 1
            )
            saveProgress()
        }
    }

    // 로컬 저장
    private fun saveProgress() {
        viewModelScope.launch {
            try {
                val data = _onboardingData.value
                dataStoreRepository.saveOnboardingProgress(
                    screenIndex = data.currentScreenIndex,
                    formData = data.formData,
                    timestamp = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                // 로깅
                Log.e("OnboardingViewModel", "Failed to save progress", e)
            }
        }
    }

    // 저장된 진행 상태 복원
    private fun restoreSavedProgress() {
        viewModelScope.launch {
            try {
                val savedData = dataStoreRepository.getOnboardingProgress()
                if (savedData != null) {
                    _onboardingData.value = savedData
                }
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Failed to restore progress", e)
            }
        }
    }

    // 프로필 제출
    fun submitProfile() {
        val data = _onboardingData.value
        val validationErrors = validateProfileForm(data.formData)

        if (validationErrors.isNotEmpty()) {
            _validationErrors.value = validationErrors
            return
        }

        if (!data.isTermsAccepted) {
            _validationErrors.value = listOf(
                ValidationError("terms", "약관에 동의해주세요")
            )
            return
        }

        viewModelScope.launch {
            _onboardingData.value = _onboardingData.value.copy(isLoading = true)
            _onboardingState.value = OnboardingState.Loading

            try {
                val result = userRepository.createUserProfile(
                    name = data.formData.name,
                    role = data.formData.role,
                    industry = data.formData.industry,
                    growthGoals = data.formData.growthGoals,
                    photoUrl = data.formData.photoUrl
                )

                // 온보딩 완료 플래그 저장
                dataStoreRepository.setOnboardingCompleted(true)

                _onboardingData.value = _onboardingData.value.copy(
                    isLoading = false,
                    completedAt = System.currentTimeMillis()
                )
                _onboardingState.value = OnboardingState.Completed

                trackOnboardingCompleted(data.formData.industry)

                // 로컬 진행 데이터 제거
                dataStoreRepository.clearOnboardingProgress()

            } catch (e: Exception) {
                _onboardingData.value = _onboardingData.value.copy(
                    isLoading = false,
                    errorMessage = handleProfileSubmissionError(e)
                )
                _onboardingState.value = OnboardingState.Error(e.message ?: "알 수 없는 오류")

                trackProfileSubmissionFailure(e)
            }
        }
    }

    // 폼 데이터 업데이트
    fun updateProfileField(field: String, value: String) {
        val current = _onboardingData.value.formData
        val updated = when (field) {
            "name" -> current.copy(name = value)
            "role" -> current.copy(role = value)
            "industry" -> current.copy(industry = value)
            "growthGoals" -> current.copy(growthGoals = value)
            else -> current
        }
        _onboardingData.value = _onboardingData.value.copy(formData = updated)
        saveProgress()
    }

    // 약관 동의
    fun setTermsAccepted(accepted: Boolean) {
        _onboardingData.value = _onboardingData.value.copy(isTermsAccepted = accepted)
        saveProgress()
        if (accepted) {
            trackTermsAccepted()
        }
    }

    // 유효성 검사
    private fun validateProfileForm(form: UserProfileForm): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        // 이름 검증: 한글 2-4자
        if (!isValidKoreanName(form.name)) {
            errors.add(
                ValidationError("name", "한글 이름 2-4자를 입력해주세요")
            )
        }

        // 직책 검증: 2-48자
        if (form.role.length < 2 || form.role.length > 48) {
            errors.add(
                ValidationError("role", "2-48자를 입력해주세요")
            )
        }

        // 산업 검증
        if (form.industry.isEmpty()) {
            errors.add(
                ValidationError("industry", "산업을 선택해주세요")
            )
        }

        // 성장 목표 검증: 100-500자
        if (form.growthGoals.length < 100 || form.growthGoals.length > 500) {
            errors.add(
                ValidationError("growthGoals", "100자 이상 500자 이하로 입력해주세요")
            )
        }

        return errors
    }

    // 한글 이름 검증
    private fun isValidKoreanName(name: String): Boolean {
        val koreanRegex = Regex("^[가-힣]{2,4}$")
        return koreanRegex.matches(name)
    }

    // 프로필 제출 오류 처리
    private fun handleProfileSubmissionError(exception: Exception): String {
        return when (exception) {
            is IOException -> "네트워크 연결을 확인해주세요"
            is HttpException -> when (exception.code()) {
                400 -> "입력 정보를 다시 확인해주세요"
                409 -> "이미 프로필이 설정되었습니다"
                401 -> "인증 정보가 만료되었습니다. 다시 로그인해주세요"
                else -> "프로필 설정에 실패했습니다. 다시 시도해주세요"
            }
            else -> "예상하지 못한 오류가 발생했습니다"
        }
    }

    // 분석 이벤트 추적
    private fun trackOnboardingStarted() {
        viewModelScope.launch {
            analyticsRepository.logEvent("onboarding_started")
        }
    }

    private fun trackScreenViewed(screenIndex: Int) {
        viewModelScope.launch {
            analyticsRepository.logEvent(
                "onboarding_screen_viewed",
                mapOf("screen_index" to screenIndex)
            )
        }
    }

    private fun trackOnboardingCompleted(industryCode: String) {
        viewModelScope.launch {
            analyticsRepository.logEvent(
                "onboarding_completed",
                mapOf("industry_code" to industryCode)
            )
        }
    }

    private fun trackProfileSubmissionFailure(exception: Exception) {
        viewModelScope.launch {
            analyticsRepository.logEvent(
                "profile_submission_failure",
                mapOf("error_type" to exception::class.simpleName)
            )
        }
    }

    private fun trackTermsAccepted() {
        viewModelScope.launch {
            analyticsRepository.logEvent("terms_accepted")
        }
    }

    fun trackOnboardingAbandoned() {
        viewModelScope.launch {
            analyticsRepository.logEvent(
                "onboarding_abandoned",
                mapOf("last_screen_index" to _onboardingData.value.currentScreenIndex)
            )
        }
    }
}
```

---

## 진행 표시기

### 시각적 디자인

**도트 기반 진행 표시기 (추천):**

```
Screen 1: ● ○ ○ ○ ○
Screen 2: ○ ● ○ ○ ○
Screen 3: ○ ○ ● ○ ○
Screen 4: ○ ○ ○ ● ○
Screen 5: ○ ○ ○ ○ ●

(● = 활성 도트, ○ = 비활성 도트)
```

**구현 패턴 (Jetpack Compose):**

```kotlin
@Composable
fun OnboardingProgressIndicator(
    currentScreenIndex: Int,
    totalScreens: Int = 5
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalScreens) { index ->
            val isActive = index == currentScreenIndex
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (isActive) Color(0xFFE86221) else Color(0xFFE0E0E0),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            )

            if (index < totalScreens - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
```

### 배치 및 위치

- **위치**: 화면 상단 (상태 바 아래, 콘텐츠 위)
- **간격**: 좌우 여백 16dp, 상하 여백 12dp
- **높이**: 20dp (도트 12dp + 상하 padding)
- **화면 비율**: 화면 폭의 30-50%

### 접근성 고려사항

```kotlin
@Composable
fun OnboardingProgressIndicator(
    currentScreenIndex: Int,
    totalScreens: Int = 5
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .semantics(mergeDescendants = true) {
                // 스크린 리더를 위한 설명
                contentDescription = "온보딩 진행: ${currentScreenIndex + 1}/$totalScreens"
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ...
    }
}
```

---

## 사용자 이동 경로

### 플로우 다이어그램

```
┌──────────────┐
│ 앱 시작      │
└──────┬───────┘
       │
       ▼
┌──────────────────────┐
│ 인증 상태 확인        │
│ (AccessToken 체크)   │
└──────┬────────────┬──────────┐
       │            │          │
   (유효)      (만료)      (없음)
       │            │          │
       ▼            ▼          ▼
   [메인]   [토큰 갱신]  [Kakao]
              │              [로그인]
              ▼
          [갱신 성공?]
          /         \
       (예)          (아니오)
        │            │
        ▼            ▼
    [메인]    [로그인 화면]
                    │
                    ▼
                [Kakao OAuth]
                    │
                    ▼
             [인증 성공?]
             /          \
          (예)           (아니오)
           │             │
           ▼             ▼
    [온보딩 확인]  [오류 메시지]
        │
        ▼
    [완료 확인?]
    /          \
 (아니오)      (예)
  │            │
  ▼            ▼
[Screen 1]  [메인 피드]
  │
  ▼
[Screen 2]
  │
  ▼
[Screen 3]
  │
  ▼
[Screen 4]
  │
  ▼
[Screen 5:
 프로필 설정]
  │
  ▼
[유효성 검사]
 /          \
(실패)      (성공)
 │           │
 ▼           ▼
[에러 표시] [제출]
 │           │
 └───────┬───┘
         │
         ▼
    [API 호출]
     /        \
 (성공)     (실패)
  │         │
  ▼         ▼
[완료      [재시도?]
 플래그]
  │
  ▼
[메인 피드]
```

### 상태 머신

**상태 전이:**

```
INIT
  │
  ├─→ AUTH_REQUIRED (카카오 로그인 필요)
  │     │
  │     └─→ LOGIN_SUCCESS
  │           │
  │           ▼
  │     ONBOARDING_CHECK
  │           │
  │           ├─→ ONBOARDING_COMPLETED (직진)
  │           │     │
  │           │     └─→ MAIN_FEED
  │           │
  │           └─→ ONBOARDING_REQUIRED (5단계 시작)
  │                 │
  │                 ├─→ SCREEN_1_VIEWING
  │                 │     │
  │                 │     ├─→ SCREEN_2_VIEWING
  │                 │     │     │
  │                 │     │     ├─→ SCREEN_3_VIEWING
  │                 │     │     │     │
  │                 │     │     │     ├─→ SCREEN_4_VIEWING
  │                 │     │     │     │     │
  │                 │     │     │     │     ├─→ SCREEN_5_FORM_FILLING
  │                 │     │     │     │     │     │
  │                 │     │     │     │     │     ├─→ FORM_VALIDATION_FAILED (오류)
  │                 │     │     │     │     │     │     │
  │                 │     │     │     │     │     │     └─→ SCREEN_5_FORM_FILLING (재입력)
  │                 │     │     │     │     │     │
  │                 │     │     │     │     │     └─→ PROFILE_SUBMITTING
  │                 │     │     │     │     │           │
  │                 │     │     │     │     │           ├─→ PROFILE_SUBMIT_SUCCESS
  │                 │     │     │     │     │           │     │
  │                 │     │     │     │     │           │     └─→ MAIN_FEED
  │                 │     │     │     │     │           │
  │                 │     │     │     │     │           └─→ PROFILE_SUBMIT_FAILED (재시도)
  │                 │     │     │     │     │
  │                 │     │     │     │     └─→ SCREEN_4_VIEWING (이전 버튼)
  │                 │     │     │     │
  │                 │     │     │     └─→ SCREEN_3_VIEWING (이전 버튼)
  │                 │     │     │
  │                 │     │     └─→ SCREEN_2_VIEWING (이전 버튼)
  │                 │     │
  │                 │     └─→ SCREEN_1_VIEWING (이전 버튼)
  │                 │
  │                 └─→ ONBOARDING_ABANDONED (앱 강제 종료)
  │
  └─→ AUTH_FAILED (오류)
        │
        └─→ ERROR_SCREEN
```

---

## 폼 검증 & 에러 처리

### Screen별 검증 규칙

#### Screen 1-4: 콘텐츠만

- 검증 불필요
- Next 버튼: 항상 활성화
- 오류 가능: 없음

#### Screen 5: 프로필 폼

**필드별 검증:**

| 필드 | 규칙 | 실시간 검증 | 오류 메시지 |
|------|------|---------|-----------|
| name | 한글 2-4자 | 입력 중 | "한글 이름 2-4자를 입력해주세요" |
| role | 2-48자 | 입력 후 | "2-48자를 입력해주세요" |
| industry | 필수 선택 | 선택 시 | "산업을 선택해주세요" |
| growthGoals | 100-500자 | 입력 중 (문자 수 표시) | "100자 이상 500자 이하로 입력해주세요" |
| photoUrl | 선택사항 | 없음 | - |
| terms | 모두 동의 필수 | 체크 시 | "약관에 동의해주세요" |

### 에러 메시지 표시

**위치 및 스타일:**

```kotlin
@Composable
fun FormFieldWithError(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    placeholder: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            isError = error != null,
            textStyle = TextStyle(fontSize = 16.sp)
        )

        if (error != null) {
            Text(
                text = error,
                color = Color(0xFFDC2626),  // 빨간색
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 12.dp),
                fontStyle = FontStyle.Italic
            )
        }
    }
}
```

### 네트워크 오류 처리

**프로필 제출 실패 시:**

```kotlin
// 네트워크 오류
if (exception is IOException) {
    showError("네트워크 연결을 확인해주세요")
    showRetryButton(true)
}

// 서버 오류 (4xx, 5xx)
if (exception is HttpException) {
    when (exception.code()) {
        400 -> showError("입력 정보를 다시 확인해주세요")
        401 -> {
            showError("인증이 만료되었습니다. 다시 로그인해주세요")
            navigateToLogin()
        }
        409 -> showError("이미 프로필이 설정되었습니다")
        500, 502, 503 -> {
            showError("서버에 일시적 오류가 발생했습니다. 나중에 다시 시도해주세요")
            showRetryButton(true)
        }
    }
}

// 타임아웃
if (exception is SocketTimeoutException) {
    showError("요청 시간이 초과되었습니다. 다시 시도해주세요")
    showRetryButton(true)
}
```

**재시도 로직:**

```kotlin
fun retryProfileSubmission(maxRetries: Int = 3) {
    var currentRetry = 0
    viewModelScope.launch {
        while (currentRetry < maxRetries) {
            try {
                submitProfile()
                return@launch  // 성공
            } catch (e: Exception) {
                currentRetry++
                if (currentRetry >= maxRetries) {
                    showFinalError("프로필 설정에 실패했습니다. 나중에 다시 시도해주세요")
                    return@launch
                }
                // 지수 백오프: 1초, 2초, 4초
                delay((1000L * (1 shl (currentRetry - 1))))
            }
        }
    }
}
```

### 로딩 상태

```kotlin
// 프로필 제출 중
if (onboardingData.isLoading) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(enabled = false) {},  // 배경 터치 비활성화
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color(0xFFE86221)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "프로필을 저장 중입니다...",
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
```

---

## 한국화 고려사항

### 언어 설정

- **언어**: 한국어(한글) 전용 (영어 폴백 없음)
- **존댓말**: 모든 사용자 대면 텍스트에 존댓말 사용
- **예시**:
  - ✓ "프로필을 저장 중입니다"
  - ✓ "다시 시도해주세요"
  - ✗ "saving profile..."
  - ✗ "please try again"

### 텍스트 인코딩 & 검증

**한글 이름 정규식:**

```kotlin
fun isValidKoreanName(name: String): Boolean {
    // 한글 문자: U+AC00 ~ U+D7A3
    val koreanRegex = Regex("^[가-힣]{2,4}$")
    return koreanRegex.matches(name)
}

// 예시:
// "김철수" → true (3자)
// "이" → false (1자, 최소 2자)
// "홍길동이" → false (4자 초과)
// "Kim" → false (한글 아님)
```

### 통화 표시

**원화 포맷팅:**

```kotlin
fun formatKoreanCurrency(amount: Int): String {
    return when {
        amount >= 100000 -> "${amount / 100000}${amount % 100000 / 10000}만원"
        else -> "${amount}원"
    }
}

// 예시:
// 200000 → "20만원"
// 100000 → "10만원"
// 20000 → "2만원"
// 10000 → "1만원"
// 999 → "999원"
```

**표시 방식 (선호도):**

```
금액 표시: "20만원", "10만원", "2만원"
테이블: 모노스페이스로 우측 정렬
예:
  보증금      20만원
  수수료      10만원
  ──────────────────
  합계       30만원
```

### 날짜 및 시간 포맷

**한국식 날짜 포맷:**

```kotlin
fun formatKoreanDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale("ko", "KR"))
    sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
    return sdf.format(Date(timestamp))
}

fun formatKoreanTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH시 mm분", Locale("ko", "KR"))
    sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
    return sdf.format(Date(timestamp))
}

// 예시:
// formatKoreanDate(2025-11-19 12:00:00) → "2025년 11월 19일"
// formatKoreanTime(2025-11-19 14:30:00) → "14시 30분"
```

**시간대:** UTC+9 (한국 표준시, KST)

### 산업 분류 (한글)

**드롭다운 옵션 순서 (사용자 빈도순):**

```kotlin
enum class Industry(val displayName: String, val code: String) {
    STARTUP("스타트업", "STARTUP"),
    ENTERPRISE("대기업", "ENTERPRISE"),
    SME("중소기업", "SME"),
    PUBLIC("공기업/공공기관", "PUBLIC"),
    FOREIGN("외국계 기업", "FOREIGN"),
    FREELANCER("프리랜서/1인 기업", "FREELANCER"),
    NONPROFIT("비영리/사회적 기업", "NONPROFIT"),
    OTHER("기타", "OTHER");

    companion object {
        fun fromCode(code: String): Industry? = values().find { it.code == code }
    }
}
```

### 폰트 설정

**Pretendard 폰트 (한글 최적화):**

```kotlin
// assets/fonts/Pretendard-Regular.ttf 포함

val PreendardFontFamily = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

// Material 3 Theme에 적용
val memoirTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = PreendardFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PreendardFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    ),
    // ... 기타 스타일
)
```

### 문자 길이 계산 (멀티바이트)

```kotlin
fun getKoreanCharacterCount(text: String): Int {
    // Kotlin String.length는 자동으로 유니코드 문자 수를 계산
    return text.length
}

// 하지만 바이트 수 계산이 필요한 경우:
fun getKoreanByteCount(text: String): Int {
    return text.toByteArray(Charsets.UTF_8).size
}

// 예시:
// "안녕하세요" → 5 문자, 15 바이트 (UTF-8)
```

### 입력 메소드 최적화 (IME)

```kotlin
// 한글 입력 방식에 최적화된 KeyboardType 설정
OutlinedTextField(
    value = name,
    onValueChange = { name = it },
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next
    ),
    modifier = Modifier
        .fillMaxWidth()
        .onKeyEvent { event ->
            // 한글 조합 입력 중 Enter 무시
            false
        }
)
```

---

## 심리학적 설계

### 진행 구조 (감정적 여정)

```
Screen 1: 문제 정의
├─ 감정: "내 문제를 누군가 이해한다"
├─ 기제: 공감 (Empathy)
└─ 심리학: 문제 인식 (Problem Recognition)

Screen 2: 커뮤니티 가치
├─ 감정: "다른 사람들도 성장하고 싶어 한다"
├─ 기제: 사회적 증거 (Social Proof)
├─ 심리학: 귀속 욕구 (Belonging Needs)
└─ 데이터: "1기 참여자의 65%가 전액 환급받았어요"

Screen 3: 구조 설명
├─ 감정: "이 구조라면 할 수 있을 것 같다"
├─ 기제: 단순함 (Simplicity)
├─ 심리학: 인지적 부하 감소 (Cognitive Load)
└─ 타임라인: 10주, 주당 50분 (구체적 약속)

Screen 4: 보증금 시스템
├─ 감정: "이 정도면 공정하고 투명하다"
├─ 기제: 투명성 (Transparency) + 환급 강조
├─ 심리학: 손실 회피 (Loss Aversion)
│           → 벌금보다 "환급" 강조
│           → "성실히 참여하면 전액 환급"
└─ 설계: 초기 투자 30만원 (의사결정 비용 낮음)
           월 10만원 (지속 가능한 수준)

Screen 5: 프로필 설정
├─ 감정: "내 정보를 입력하면 약속이 확정된다"
├─ 기제: 약속 효과 (Commitment Effect)
├─ 심리학: 행동적 일관성 (Behavioral Consistency)
└─ 설계: 필수 필드 최소화 (저항감 감소)
          선택적 사진 (낮은 진입 장벽)
```

### 설계 원칙

**1. 감정적 구매 우선, 논리적 정당화 후순**

```
❌ 잘못된 순서: 논리 → 감정
   "월 10만원 = 1.25배, ROI..."

✓ 올바른 순서: 감정 → 논리
   "배움이 휘발되는 것이 아까워요?"
   → "함께 해결하세요"
   → "이런 구조입니다"
   → "비용은 이렇게 됩니다"
```

**2. 손실 회피 활용 (Financial Messaging)**

```
❌ 손실 강조 (공포 마케팅):
   "회고 미작성 시 2만원 벌금!"

✓ 환급 강조 (긍정 강조):
   "성실히 참여하면 20만원 보증금 전액 환급!"

강도 비율: 환급 강조 70% : 벌금 설명 30%
```

**3. 사회적 증거 (Social Proof)**

```
구체적 수치: "1기 참여자의 65%가 전액 환급받았어요"

시간대: Screen 2 (초기 신뢰 구축 단계)에서
배치: 본문 하단 (부드럽게 소개)

효과: 사용자의 행동 가능성에 대한 확신 증가
```

**4. 점진적 공개 (Progressive Disclosure)**

```
Screen 1: 문제 (감정)
  ↓
Screen 2: 해결책 (공감)
  ↓
Screen 3: 구조 (이해)
  ↓
Screen 4: 비용 (신뢰 구축 후)
  ↓
Screen 5: 약속 (행동)

→ 각 화면이 다음 화면의 기반 제공
```

**5. 의사결정 피로 최소화 (Decision Fatigue)**

```
Screen 1-4: 선택 없음, 정보 수신만
Screen 5: 4개 필수 입력 + 약관 동의
          (Screen 1-4 대비 의사결정 최소화)
```

### 색상 심리학 (Memoir 팔레트)

```
주색상: #F4BA54 (Mustard)
├─ 심리: 따뜻함, 신뢰, 희망
├─ 사용처: 주요 버튼, 강조 요소
└─ 메시지: "우리가 함께다"

강조색: #E86221 (Carrot Orange)
├─ 심리: 에너지, 행동, 활력
├─ 사용처: 진행 표시기, 보증금 금액
└─ 메시지: "이제 행동할 시간"

배경: #FFFFFF (White)
├─ 심리: 깔끔함, 명확함
└─ 메시지: "산만하지 않고 명확하다"

텍스트: #124234 (Dark Olive)
├─ 심리: 신뢰, 안정성
└─ 메시지: "진지하고 의도적이다"
```

---

## 약관 & 법규 준수

### 필수 약관 및 정책

**1. 서비스 약관 (Terms of Service)**

**내용 요구사항:**
- 서비스 이용 규칙
- 사용자 의무 (주간 회고 작성 요청)
- 커뮤니티 가이드라인
- 부적절한 콘텐츠 정책
- 계정 생성 및 보안
- 책임 제한 (면책 조항)
- 약관 변경 절차

**법적 요구사항:**
- 한국어로 작성
- 명확한 글꼴 (최소 10pt)
- 약관 전체 텍스트 제공
- 링크 또는 in-app modal로 접근 가능

**실행 방식:**
```
Screen 5 (프로필 설정)
├─ [서비스 약관] 링크
│   └─ 탭 시 → WebView 또는 external browser
├─ [개인정보 정책] 링크
│   └─ 탭 시 → WebView 또는 external browser
└─ [보증금 정책] 링크
    └─ 탭 시 → WebView 또는 external browser

체크박스:
☐ 서비스 약관에 동의합니다
☐ 개인정보 정책에 동의합니다
☐ 보증금 정책에 동의합니다
```

**2. 개인정보 정책 (Privacy Policy)**

**내용 요구사항:**
- 수집 정보: 이름, 직책, 산업, 성장 목표, (선택) 사진
- 수집 목적: 코호트 매칭, 서비스 제공
- 보관 기간: 계정 활성 기간 + 3년 (법적 요구)
- 제3자 공유: 코호트 멤버와의 공유, 분석 목적 비공개 처리
- 사용자 권리: 접근, 정정, 삭제 요청 (Phase 2 구현)
- 보안: 암호화 저장, HTTPS 통신

**법적 요구사항 (PIPA - 개인정보보호법):**
- 명시적 동의 필수
- 개인정보 처리방침 제공
- 침해 시 알림 의무
- 정보 주체의 권리 보장

**3. 보증금 정책 (Deposit Policy)**

**내용 요구사항:**
- 보증금 개념: 참여 약속금, 벌금 아님
- 금액: 초기 20만원 + 첫달 10만원
- 월 수수료: 10만원
- 환급 조건: 70% 이상 회고 작성 시 보증금 전액 환급
- 벌금 규정:
  - 회고 미작성: 회당 2만원
  - 댓글 부족 (5개 미만): 회당 1만원
- 이의 신청: 이메일 또는 고객센터
- 분쟁 해결: 고객센터 연락처 제시

**구성 요소:**
```
[보증금 정책 전문]

1. 정의
   "보증금"은 Memoir 서비스 참여의 약속금으로,
   사용자가 성실하게 참여할 것으로 기대하는
   신뢰의 표현입니다.

2. 금액 및 결제
   - 초기 보증금: 20만원
   - 첫 달 서비스 수수료: 10만원
   - 이후 월 수수료: 10만원
   - 입금 계좌: [계좌 정보]

3. 환급 조건
   - 10주 사이클 종료 후
   - 회고 작성률 70% 이상 달성 시
   - 전액 환급 (순환 지연 없음)

4. 벌금 규정
   - 회고 미작성: 1회당 2만원
   - 댓글 부족 (5개 미만): 1회당 1만원
   - 월별로 합산하여 차감

5. 분쟁 해결
   - 이의 신청: support@memoirapp.com
   - 검토 기간: 5영업일
   - 환급 절차: 인증된 계좌로 송금

6. 변경 사항
   정책은 변경될 수 있으며, 변경 시
   사용자에게 14일 전 공지됩니다.
```

### 온보딩 내 약관 동의 UI

**Screen 5 - 약관 동의 섹션:**

```kotlin
@Composable
fun TermsAndPoliciesSection(
    isTermsAccepted: Boolean,
    onTermsChange: (Boolean) -> Unit,
    isPrivacyAccepted: Boolean,
    onPrivacyChange: (Boolean) -> Unit,
    isDepositAccepted: Boolean,
    onDepositChange: (Boolean) -> Unit,
    onOpenTerms: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            "약관 동의 (필수)",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 서비스 약관
        CheckboxWithLink(
            label = "서비스 약관에 동의합니다",
            isChecked = isTermsAccepted,
            onCheckedChange = onTermsChange,
            onLinkClick = { onOpenTerms("TERMS") }
        )

        // 개인정보 정책
        CheckboxWithLink(
            label = "개인정보 정책에 동의합니다",
            isChecked = isPrivacyAccepted,
            onCheckedChange = onPrivacyChange,
            onLinkClick = { onOpenTerms("PRIVACY") }
        )

        // 보증금 정책
        CheckboxWithLink(
            label = "보증금 정책에 동의합니다",
            isChecked = isDepositAccepted,
            onCheckedChange = onDepositChange,
            onLinkClick = { onOpenTerms("DEPOSIT") }
        )
    }
}

@Composable
fun CheckboxWithLink(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onLinkClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onCheckedChange(!isChecked) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "[보기]",
                fontSize = 12.sp,
                color = Color(0xFF2563EB),
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { onLinkClick() }
                    .padding(start = 8.dp)
            )
        }
    }
}
```

### 약관 표시 (WebView 또는 Modal)

```kotlin
// WebView를 통한 약관 표시
@Composable
fun TermsWebViewScreen(
    policyType: String,  // "TERMS", "PRIVACY", "DEPOSIT"
    onDismiss: () -> Unit
) {
    val webViewUrl = when (policyType) {
        "TERMS" -> "https://memoirapp.com/terms"
        "PRIVACY" -> "https://memoirapp.com/privacy"
        "DEPOSIT" -> "https://memoirapp.com/deposit-policy"
        else -> "https://memoirapp.com/terms"
    }

    val state = rememberWebViewState(url = webViewUrl)

    AndroidView(
        factory = {
            WebView(it).apply {
                webViewClient = WebViewClient()
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { webView ->
            webView.loadUrl(webViewUrl)
        }
    )
}

// Modal을 통한 약관 표시 (in-app)
@Composable
fun TermsModalScreen(
    policyType: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 약관 제목
                Text(
                    text = when (policyType) {
                        "TERMS" -> "서비스 약관"
                        "PRIVACY" -> "개인정보 정책"
                        "DEPOSIT" -> "보증금 정책"
                        else -> "약관"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 약관 내용 (스크롤 가능)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    item {
                        Text(
                            text = getTermsContent(policyType),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                // 닫기 버튼
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("닫기")
                }
            }
        }
    }
}
```

---

## 분석 이벤트

### 이벤트 목록 및 정의

| 이벤트 | 발동 조건 | 파라미터 | 우선순위 | 용도 |
|--------|---------|--------|--------|------|
| onboarding_started | Screen 1 표시 | - | 높음 | 온보딩 시작 추적 |
| onboarding_screen_viewed | 각 화면 진입 | screen_index (0-4) | 높음 | 이탈 분석 |
| onboarding_abandoned | 앱 강제 종료 | last_screen_index | 높음 | 이탈점 파악 |
| profile_submission_started | 완료 버튼 탭 | - | 중간 | 제출 시도 추적 |
| profile_submission_success | API 성공 | industry_code, name_chars, goals_chars | 높음 | 성공률 측정 |
| profile_submission_failure | API 실패 | error_code, error_type | 높음 | 문제 진단 |
| terms_accepted | 모든 체크박스 동의 | tos_version, privacy_version | 중간 | 약관 동의 추적 |
| form_validation_error | 필드 검증 실패 | field_name, error_type | 낮음 | UX 개선 |

### 이벤트 추적 구현

```kotlin
// Firebase Analytics를 통한 이벤트 추적
interface AnalyticsRepository {
    suspend fun logEvent(eventName: String, params: Map<String, Any>? = null)
}

class FirebaseAnalyticsRepository(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsRepository {

    override suspend fun logEvent(
        eventName: String,
        params: Map<String, Any>?
    ) {
        withContext(Dispatchers.Default) {
            val bundle = Bundle().apply {
                params?.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Boolean -> putBoolean(key, value)
                        is Double -> putDouble(key, value)
                        else -> putString(key, value.toString())
                    }
                }
            }
            firebaseAnalytics.logEvent(eventName, bundle)
        }
    }
}

// ViewModel에서 사용
private fun trackOnboardingStarted() {
    viewModelScope.launch {
        analyticsRepository.logEvent("onboarding_started")
    }
}

private fun trackScreenViewed(screenIndex: Int) {
    viewModelScope.launch {
        analyticsRepository.logEvent(
            "onboarding_screen_viewed",
            mapOf("screen_index" to screenIndex)
        )
    }
}

private fun trackProfileSubmissionSuccess(industryCode: String) {
    viewModelScope.launch {
        val data = _onboardingData.value.formData
        analyticsRepository.logEvent(
            "profile_submission_success",
            mapOf(
                "industry_code" to industryCode,
                "name_chars" to data.name.length,
                "goals_chars" to data.growthGoals.length
            )
        )
    }
}

private fun trackFormValidationError(fieldName: String, errorType: String) {
    viewModelScope.launch {
        analyticsRepository.logEvent(
            "form_validation_error",
            mapOf(
                "field_name" to fieldName,
                "error_type" to errorType
            )
        )
    }
}
```

### 대시보드 메트릭

**추적할 주요 지표:**

```
1. 온보딩 완료율
   Formula: (profile_submission_success / onboarding_started) * 100
   Target: ≥65%

2. 스크린별 이탈률
   Formula: (onboarding_abandoned at screen N / onboarding_screen_viewed at screen N) * 100
   Target: <10% per screen

3. 평균 소요 시간
   Formula: Avg(profile_submission_success timestamp - onboarding_started timestamp)
   Target: 3-5분

4. 제출 성공률 (최종)
   Formula: (profile_submission_success / profile_submission_started) * 100
   Target: ≥95%

5. 약관 동의율
   Formula: (terms_accepted / onboarding_screen_viewed at screen 5) * 100
   Target: ≥95%

6. 검증 오류 분포
   Formula: count(form_validation_error) grouped by field_name
   Target: 비율 최소화
```

---

## 테스트 시나리오

### UI 테스트 시나리오

#### TC-1: 모든 스크린 순회

**목적:** 온보딩 전체 플로우가 정상 작동하는지 확인

**단계:**
1. 앱 실행 → 카카오 로그인 완료 → Screen 1 표시 확인
2. "다음" 버튼 탭 → Screen 2 이동 확인
3. "다음" 버튼 탭 → Screen 3 이동 확인
4. "다음" 버튼 탭 → Screen 4 이동 확인
5. "다음" 버튼 탭 → Screen 5 (프로필) 이동 확인
6. 모든 필드 입력 → "완료" 버튼 활성화 확인
7. "완료" 버튼 탭 → API 호출 진행바 표시 확인
8. API 성공 → 메인 피드 이동 확인

**예상 결과:** 모든 단계 통과, 약 3-5분 소요

---

#### TC-2: 각 스크린 버튼 상태

**목적:** 버튼의 활성화/비활성화 상태 검증

**Screen 1-4:**
- ✓ Next 버튼: 항상 활성화 (노란색 배경)
- ✓ Previous 버튼 (2-4): 활성화
- ✓ 터치 가능 (호버 효과 표시)

**Screen 5:**
- ✓ 초기 상태: "완료" 버튼 비활성화 (회색)
- ✓ 이름 입력 → 검증 중
  - 한글 2-4자 미충족 → 오류 메시지 표시, 버튼 비활성화
  - 한글 2-4자 충족 → 오류 해제
- ✓ 직책 입력 → 2-48자 검증
- ✓ 산업 선택 → 필수 선택 검증
- ✓ 성장 목표 입력 → 100-500자 검증
  - 실시간 글자 수 표시 (0/500)
  - 100자 미만 → 오류 메시지, 버튼 비활성화
  - 500자 초과 → 입력 차단
- ✓ 약관 동의 → 모두 체크 필수
- ✓ 모든 조건 충족 → "완료" 버튼 활성화 (주황색)

---

#### TC-3: 폼 유효성 검사

**목적:** 각 필드의 검증 규칙 동작 확인

| 필드 | 입력값 | 예상 결과 |
|------|-------|--------|
| name | "이" | 오류: "한글 이름 2-4자를 입력해주세요" |
| name | "김철수" | ✓ 통과 |
| name | "홍길동이" | 오류: 4자 초과 |
| name | "Kim" | 오류: 한글 아님 |
| role | "P" | 오류: "2-48자를 입력해주세요" |
| role | "제품 관리자" | ✓ 통과 |
| role | "Product Manager" | ✓ 통과 |
| industry | 선택 안 함 | 오류: "산업을 선택해주세요" |
| industry | "스타트업" 선택 | ✓ 통과 |
| growthGoals | "성장하고 싶다" (15자) | 오류: "100자 이상..." |
| growthGoals | 100자 정확히 | ✓ 통과 |
| growthGoals | 500자 정확히 | ✓ 통과 |
| growthGoals | 501자 | 입력 차단 (붙여넣기도 차단) |

---

#### TC-4: 진행 저장 및 복원

**목적:** 앱 종료 후 재실행 시 진행 상태 복원 확인

**단계:**
1. Screen 1 표시 상태에서 "다음" 탭 → Screen 2 이동
2. 앱 강제 종료 (백그라운드에서 제거 또는 강제 종료)
3. 앱 재실행 → Kakao 재인증 필요시 처리
4. Screen 2가 표시되는지 확인
5. Screen 3 진행, 앱 종료
6. 재실행 → Screen 3 표시 확인
7. Screen 5 도달, 이름 및 직책 입력, 앱 종료
8. 재실행 → Screen 5, 입력된 데이터 복원 확인

**예상 결과:** 모든 진행 상태 정상 저장 및 복원

---

#### TC-5: 네트워크 중단 및 재시도

**목적:** 네트워크 오류 처리 및 재시도 로직 검증

**준비:**
- 개발자 도구에서 네트워크 throttling 설정
- 또는 비행기 모드 토글

**단계:**
1. Screen 5: 모든 필드 입력 완료
2. 완료 버튼 탭 → 로딩 진행
3. 네트워크 끊김 → "네트워크 연결을 확인해주세요" 오류 메시지 표시
4. 진행 표시기 숨김, "다시 시도" 버튼 표시
5. 네트워크 복구
6. "다시 시도" 탭 → 로딩 재개 → 성공
7. 메인 피드로 이동 확인

**예상 결과:** 재시도 로직 정상 작동, 사용자 친화적 오류 메시지

---

#### TC-6: 약관 동의 및 링크

**목적:** 약관 동의 체크박스 및 링크 동작 검증

**단계:**
1. Screen 5 도달
2. [서비스 약관] 링크 탭 → WebView 또는 external browser 열림
3. 약관 내용 표시 확인
4. 닫기/뒤로가기 → 온보딩 화면으로 돌아옴
5. 약관 체크박스 토글 → 체크/언체크 상태 변경
6. 모든 약관 체크 → "완료" 버튼 활성화
7. 약관 중 하나 언체크 → "완료" 버튼 비활성화

**예상 결과:** 약관 링크 정상 열림, 동의 상태 정상 추적

---

#### TC-7: 진행 표시기 표시

**목적:** 진행 표시기(도트) 정상 표시 확인

**단계:**
1. Screen 1 표시 → "● ○ ○ ○ ○" 표시 확인
2. "다음" → Screen 2 → "○ ● ○ ○ ○"
3. "다음" → Screen 3 → "○ ○ ● ○ ○"
4. "다음" → Screen 4 → "○ ○ ○ ● ○"
5. "다음" → Screen 5 → "○ ○ ○ ○ ●"
6. Screen 5에서 "이전" → Screen 4 → "○ ○ ○ ● ○"

**예상 결과:** 도트가 현재 화면을 정확히 반영

---

### 통합 테스트 시나리오

#### TC-8: API 통합 (Mock Server)

**목적:** 백엔드 API와의 정상 통신 검증

**준비:** Mock server 또는 테스트 API 엔드포인트 사용

**단계:**
1. Screen 5 모든 필드 입력
2. 완료 버튼 탭 → POST `/api/v1/users/profile` 호출
3. 요청 본문:
   ```json
   {
     "name": "김철수",
     "role": "제품 관리자",
     "industry_code": "STARTUP",
     "growth_goals": "..." (100+ chars)
   }
   ```
4. 응답 (201):
   ```json
   {
     "user_id": "uuid",
     "profile": { ... },
     "onboarding_completed_at": "2025-11-19T..."
   }
   ```
5. 메인 피드로 이동 확인

**예상 결과:** API 호출 정상 작동, 응답 처리 정상

---

#### TC-9: 토큰 갱신 및 세션

**목적:** 액세스 토큰 갱신 로직 검증

**준비:** 짧은 토큰 만료 시간 설정 (테스트용)

**단계:**
1. Screen 5에서 프로필 제출 시 액세스 토큰 만료
2. 자동으로 갱신 토큰 사용 → 새 액세스 토큰 획득
3. API 재시도 → 성공
4. 메인 피드 이동 확인

**예상 결과:** 토큰 갱신 자동 수행, 사용자 인지 없음

---

### 수동 테스트 체크리스트

#### 한글 입력 테스트
- [ ] 이름 입력: "김철수", "이순신", "박", "홍길동이" (각각 검증)
- [ ] IME (Input Method Editor) 조합 중 Enter 무시 확인
- [ ] 복사-붙여넣기 한글 검증 정상 작동

#### 한국 환경 테스트
- [ ] 날짜: 2025년 11월 19일 형식 표시
- [ ] 통화: "20만원", "2만원" 형식 표시
- [ ] 산업: 한글 드롭다운 표시

#### 접근성 테스트
- [ ] 터치 타겟 48dp 이상 확인
- [ ] 색상 대비: 텍스트 ≥4.5:1 (WCAG AA)
- [ ] 스크린 리더 (TalkBack) 설명 텍스트 적절한지 확인

#### 성능 테스트
- [ ] 화면 전환 <300ms (부드러움 확인)
- [ ] 프로필 제출 <2초 (p95)
- [ ] 로컬 데이터 로드 <200ms

---

## 아키텍처 패턴

### 클린 아키텍처 레이어 설계

```
┌──────────────────────────────────────┐
│         UI Layer (Presentation)      │
│  OnboardingScreen (Jetpack Compose)  │
│  OnboardingViewModel (MVVM)          │
│  Components (Reusable UI)            │
└──────────────┬───────────────────────┘
               │
┌──────────────┴───────────────────────┐
│       Domain Layer (Business Logic)   │
│  UseCases:                           │
│  - ValidateProfileUseCase            │
│  - SubmitProfileUseCase              │
│  - RestoreOnboardingProgressUseCase  │
│  - SaveOnboardingProgressUseCase     │
└──────────────┬───────────────────────┘
               │
┌──────────────┴───────────────────────┐
│       Data Layer (Repositories)      │
│  UserRepository (interface)          │
│  DataStoreRepository (interface)     │
│  AuthRepository (interface)          │
│  AnalyticsRepository (interface)     │
└──────────────┬───────────────────────┘
               │
┌──────────────┴───────────────────────┐
│   Infrastructure Layer (Impl)        │
│  UserRepositoryImpl                   │
│  DataStoreRepositoryImpl              │
│  Remote (Retrofit)                   │
│  Local (Room, DataStore)             │
│  Firebase Analytics                  │
└──────────────────────────────────────┘
```

### 의존성 주입 (Hilt)

```kotlin
// Hilt Module for Onboarding
@Module
@InstallIn(SingletonComponent::class)
object OnboardingModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        userService: UserService,
        userDao: UserDao,
        dataStore: DataStore<Preferences>
    ): UserRepository = UserRepositoryImpl(userService, userDao, dataStore)

    @Provides
    @Singleton
    fun provideDataStoreRepository(
        dataStore: DataStore<Preferences>
    ): DataStoreRepository = DataStoreRepositoryImpl(dataStore)

    @Provides
    @Singleton
    fun provideAnalyticsRepository(
        firebaseAnalytics: FirebaseAnalytics
    ): AnalyticsRepository = FirebaseAnalyticsRepository(firebaseAnalytics)
}

// ViewModel에 주입
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {
    // ...
}
```

### Composable 구조

```
OnboardingScreen (root)
├─ OnboardingProgressIndicator (도트 표시기)
├─ when (currentScreenIndex)
│  ├─ 0 → ProblemFramingScreen
│  ├─ 1 → CommunityValueScreen
│  ├─ 2 → StructureExplanationScreen
│  ├─ 3 → DepositSystemScreen
│  └─ 4 → ProfileSetupScreen
│       ├─ TextInputField
│       ├─ DropdownIndustrySelector
│       ├─ TextAreaGrowthGoals
│       ├─ TermsAndPoliciesSection
│       └─ SubmitButton
└─ NavigationButtons (Next/Previous)
```

### 상태 흐름 (StateFlow)

```
ViewModel
├─ onboardingState: StateFlow<OnboardingState>
│  └─ Idle, Loading, ScreenDisplay, Error, Completed
├─ onboardingData: StateFlow<OnboardingData>
│  ├─ currentScreenIndex
│  ├─ formData
│  ├─ isTermsAccepted
│  ├─ isLoading
│  ├─ errorMessage
│  └─ completedAt
└─ validationErrors: StateFlow<List<ValidationError>>
   ├─ field: String
   └─ message: String

Screen (Composable)
├─ collectAsState() → onboardingState
├─ collectAsState() → onboardingData
├─ collectAsState() → validationErrors
└─ UI 업데이트
```

### 로컬 저장소 (Room + DataStore)

**Room (SQL Database):**
```kotlin
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String,
    val name: String,
    val role: String,
    val industryCode: String,
    val growthGoals: String,
    val photoUrl: String?,
    val createdAt: Long
)

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getProfile(userId: String): UserProfileEntity?
}
```

**DataStore (Key-Value):**
```kotlin
// Preferences 기반
val onboardingScreenIndex = intPreferencesKey("onboarding_screen_index")
val onboardingFormData = stringPreferencesKey("onboarding_form_data")
val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
val onboardingProgressSavedAt = longPreferencesKey("onboarding_progress_saved_at")

// Encrypted Preferences (토큰 저장)
val accessToken = encryptedStringPreferencesKey("access_token")
val refreshToken = encryptedStringPreferencesKey("refresh_token")
```

---

## 코드 예시

### OnboardingViewModel (완전한 구현)

[ViewModel 구현은 "상태 관리" 섹션에서 제공됨]

### OnboardingScreen Composable

```kotlin
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onOnboardingComplete: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val onboardingState by viewModel.onboardingState.collectAsState()
    val onboardingData by viewModel.onboardingData.collectAsState()
    val validationErrors by viewModel.validationErrors.collectAsState()

    LaunchedEffect(onboardingState) {
        when (onboardingState) {
            is OnboardingState.Completed -> onOnboardingComplete()
            is OnboardingState.Error -> {
                // 에러 처리
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            OnboardingProgressIndicator(
                currentScreenIndex = onboardingData.currentScreenIndex,
                totalScreens = 5
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (onboardingData.currentScreenIndex) {
                0 -> ProblemFramingScreen(
                    onNextClick = { viewModel.goToNextScreen() }
                )
                1 -> CommunityValueScreen(
                    onNextClick = { viewModel.goToNextScreen() },
                    onPreviousClick = { viewModel.goToPreviousScreen() }
                )
                2 -> StructureExplanationScreen(
                    onNextClick = { viewModel.goToNextScreen() },
                    onPreviousClick = { viewModel.goToPreviousScreen() }
                )
                3 -> DepositSystemScreen(
                    onNextClick = { viewModel.goToNextScreen() },
                    onPreviousClick = { viewModel.goToPreviousScreen() },
                    onTermsClick = { /* 약관 링크 처리 */ }
                )
                4 -> ProfileSetupScreen(
                    formData = onboardingData.formData,
                    validationErrors = validationErrors,
                    isLoading = onboardingData.isLoading,
                    errorMessage = onboardingData.errorMessage,
                    isTermsAccepted = onboardingData.isTermsAccepted,
                    onFormFieldChange = { field, value ->
                        viewModel.updateProfileField(field, value)
                    },
                    onTermsChange = { accepted ->
                        viewModel.setTermsAccepted(accepted)
                    },
                    onPreviousClick = { viewModel.goToPreviousScreen() },
                    onSubmit = { viewModel.submitProfile() },
                    onTermsClick = { /* 약관 링크 */ }
                )
            }

            // 로딩 오버레이
            if (onboardingData.isLoading) {
                LoadingOverlay()
            }
        }
    }
}

@Composable
fun ProblemFramingScreen(
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 일러스트
        Icon(
            painter = painterResource(id = R.drawable.ic_problem_framing),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color(0xFFE86221)
        )

        // 헤드라인 및 본문
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "일상의 배움이\n휘발되고 있지 않나요?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF124234),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                "매주 마주하는 경험들, 좋은 인사이트들이\n기억 속에서 사라지고 있습니다.\n\n" +
                "Memoir는 이 문제를 해결하기 위해 만들었습니다.",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF666),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // 다음 버튼
        Button(
            onClick = onNextClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF4BA54)
            )
        ) {
            Text(
                "다음",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ProfileSetupScreen(
    formData: UserProfileForm,
    validationErrors: List<ValidationError>,
    isLoading: Boolean,
    errorMessage: String?,
    isTermsAccepted: Boolean,
    onFormFieldChange: (String, String) -> Unit,
    onTermsChange: (Boolean) -> Unit,
    onPreviousClick: () -> Unit,
    onSubmit: () -> Unit,
    onTermsClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Text(
            "프로필 설정",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 이름 입력
        val nameError = validationErrors.find { it.field == "name" }?.message
        FormFieldWithError(
            label = "이름",
            value = formData.name,
            onValueChange = { onFormFieldChange("name", it) },
            error = nameError,
            placeholder = "예: 김철수"
        )

        // 직책 입력
        val roleError = validationErrors.find { it.field == "role" }?.message
        FormFieldWithError(
            label = "직책/역할",
            value = formData.role,
            onValueChange = { onFormFieldChange("role", it) },
            error = roleError,
            placeholder = "예: 제품 PM"
        )

        // 산업 선택
        val industryError = validationErrors.find { it.field == "industry" }?.message
        IndustryDropdown(
            selectedValue = formData.industry,
            onValueChange = { onFormFieldChange("industry", it) },
            error = industryError
        )

        // 성장 목표
        val goalsError = validationErrors.find { it.field == "growthGoals" }?.message
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                "성장 목표 (필수, 100자 이상)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = formData.growthGoals,
                onValueChange = { text ->
                    if (text.length <= 500) {
                        onFormFieldChange("growthGoals", text)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("당신이 10주 동안 이루고 싶은 성장을 구체적으로...") },
                isError = goalsError != null,
                minLines = 4
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (goalsError != null) {
                    Text(
                        goalsError,
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp
                    )
                }
                Text(
                    "(${formData.growthGoals.length}/500)",
                    fontSize = 12.sp,
                    color = Color(0xFF999),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        }

        // 약관 동의
        TermsAndPoliciesSection(
            isTermsAccepted = isTermsAccepted,
            onTermsChange = onTermsChange,
            onOpenTerms = onTermsClick
        )

        // 에러 메시지
        if (errorMessage != null) {
            Text(
                errorMessage,
                color = Color(0xFFDC2626),
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(Color(0xFFFEE2E2), RoundedCornerShape(4.dp))
                    .padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPreviousClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("이전")
            }

            Button(
                onClick = onSubmit,
                enabled = formData.name.isNotEmpty() &&
                        formData.role.isNotEmpty() &&
                        formData.industry.isNotEmpty() &&
                        formData.growthGoals.length >= 100 &&
                        isTermsAccepted &&
                        !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE86221),
                    disabledContainerColor = Color(0xFFCCC)
                )
            ) {
                Text("완료", color = Color.White)
            }
        }
    }
}

// 로딩 오버레이
@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color(0xFFE86221)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "프로필을 저장 중입니다...",
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
```

---

## 요약

### 핵심 설계 원칙

1. **심리학적 여정**: 문제 → 공감 → 사회적 증명 → 신뢰 → 행동
2. **점진적 공개**: 초반 감정적 구매, 후반 재정 정보 및 약속
3. **투명성**: 보증금 시스템 명확한 설명 (벌금 아님)
4. **한국화**: 한글 전용, 존댓말, 한국 금융 포맷
5. **최소 마찰**: 필수 정보만 수집, 선택사항 최소화

### 구현 체크리스트

- [ ] OnboardingViewModel 상태 관리 구현
- [ ] 5개 Screen Composable 구현
- [ ] 프로필 폼 검증 로직 구현
- [ ] Room + DataStore 로컬 저장소
- [ ] Firebase Analytics 이벤트 추적
- [ ] 약관 & 정책 WebView/Modal 통합
- [ ] 한글 입력 검증 (정규식)
- [ ] 네트워크 오류 재시도 로직
- [ ] Unit tests (ViewModel, Validators)
- [ ] UI tests (화면 네비게이션, 폼 검증)
- [ ] 한글 IME 최적화 테스트
- [ ] 접근성 심사 (TalkBack, 색상 대비)

---

**문서 버전:** 1.0
**마지막 수정:** 2025년 11월 19일
**담당자:** Product & Design Team
**리뷰:** Phase 1 MVP 스펙 확인
