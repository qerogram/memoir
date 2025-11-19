# Specification: User Authentication & Onboarding

## Goal
Build the foundational authentication and onboarding system for the Memoir Android app using Kakao Login as the sole authentication provider, with a mandatory 5-screen onboarding flow that educates users about the cohort-based reflection model and deposit-penalty system, culminating in Korean-localized profile setup.

## User Stories
- As a new user, I want to log in with my Kakao account so that I can quickly access the app without creating separate credentials
- As a first-time user, I want to understand the app's unique deposit-penalty model before committing so that I can make an informed decision
- As a Korean professional, I want to set up my profile with Korean industry terminology so that my professional context is accurately represented
- As a returning user, I want to stay logged in across app sessions so that I don't have to re-authenticate constantly
- As a growth-minded professional, I want clear expectations about time commitment and financial accountability upfront so that I can assess if this community aligns with my goals

## Core Requirements

### Authentication System
- Kakao Login SDK integration as the sole authentication method
- OAuth 2.0 flow with automatic account creation on first successful login
- JWT-based session management with 30-day access tokens and 90-day refresh tokens
- Automatic token refresh on app launch if within refresh window
- Secure token storage using encrypted Android DataStore
- Logout functionality clearing local tokens and session state

### Mandatory Onboarding Flow
**5-screen progressive disclosure sequence (cannot skip):**

1. **Problem Framing Screen**
   - Headline: "일상의 배움이 휘발되고 있지 않나요?"
   - Explain how daily learnings evaporate without structured reflection
   - Visual: Simple illustration of fading insights

2. **Community Value Screen**
   - Headline: "다양한 분야의 성장하는 사람들과 함께"
   - Highlight diverse professional community and meaningful connections
   - Show cohort-based model benefits

3. **Structure Explanation Screen**
   - Headline: "10주 주기, 매주 한 번의 성찰"
   - Explain 10-week cohorts, weekly reflection rhythm, peer feedback system
   - Time commitment transparency: ~30 min writing + 20 min reading weekly

4. **Accountability Mechanism Screen**
   - Headline: "보증금 시스템으로 함께 성장하기"
   - Transparent breakdown:
     - Initial: 200,000원 deposit + 100,000원 first month
     - Monthly: 100,000원 fee
     - Refund: Full deposit if 70%+ completion
     - Deductions: 20,000원 per missed reflection, 10,000원 insufficient commenting
   - Emphasize: "성실히 참여하시면 보증금 전액 환급"
   - Social proof: "1기 참여자의 65%가 전액 환급받았어요"
   - Link to full Deposit Policy
   - Note: "입금 계좌 안내는 이메일로 발송됩니다" (manual for Phase 1)

5. **Profile Setup Screen**
   - Collect required profile information before app access
   - Terms of Service and Privacy Policy agreement checkboxes

- Progress indicator showing current screen (e.g., "3/5")
- No back button on screens 1-4 (forward-only progression)
- "다음" (Next) button to advance screens
- Persistent completion flag prevents re-showing

### Profile Setup Requirements
**Required Fields:**
- Full name: Korean hangul validation (2-4 characters typical, regex: `^[가-힣]{2,4}$`)
- Professional role/title: Free text (accepts Korean and English, e.g., "Product Manager")
- Industry: Dropdown selector with Korean taxonomy
- Personal growth goals: Multi-line text field (100-500 characters enforced)

**Industry Dropdown Options:**
- 스타트업 (Startup)
- 대기업 (Large Corporation)
- 중소기업 (SME)
- 공기업/공공기관 (Public Corporation/Government)
- 외국계 기업 (Foreign Company)
- 프리랜서/1인 기업 (Freelancer/Solopreneur)
- 비영리/사회적 기업 (Non-profit/Social Enterprise)
- 기타 (Other)

**Optional Fields:**
- Profile photo: Deferred to Phase 2; use placeholder avatar

**Legal Requirements:**
- Terms of Service agreement checkbox (required)
- Privacy Policy agreement checkbox (required)
- Deposit Policy acknowledgment checkbox (required)
- Links to full legal documents

### Korean Localization
- All UI text in Korean language (no English fallback)
- Polite speech forms (존댓말) for all system messages
- Korean name input validation (hangul character set only)
- Currency formatting: "200,000원" or "20만원"
- Date/time: Korean Standard Time (KST, UTC+9)
- Typography: Pretendard font family optimized for Korean readability
- Number formatting with comma separators

### Navigation Flow
1. App launch → Check authentication state (DataStore)
2. Not authenticated → Kakao Login screen
3. Successful Kakao login → Check onboarding completion status (API call)
4. Onboarding not completed → Screen 1 of 5
5. Progress through screens 1→2→3→4→5 (forward only)
6. Complete profile setup → Submit to backend API
7. Success → Mark onboarding complete (local + backend) → Navigate to Cohort Feed
8. Subsequent launches → If authenticated + onboarding complete → Skip to Cohort Feed

### Data Persistence
- Session tokens: Encrypted DataStore (Android Keystore)
- Onboarding completion flag: DataStore boolean
- User profile: Room database (local cache)
- Profile submission: POST to backend API → synced to PostgreSQL

### Error Handling
- Kakao login failure: User-friendly message with retry option
- Network timeout: Exponential backoff retry (max 2 retries) then error toast
- Validation errors: Inline field-level error messages in Korean
- Backend API errors: Map HTTP codes to user-friendly Korean messages
- Token expiration: Automatic refresh attempt; force re-login if refresh fails

## Visual Design

### Design System Alignment
Per Memoir mission.md design principles:

**Color Palette:**
- Primary: Mustard #F4BA54
- Accent: Carrot Orange #E86221
- Dark: Dark Olive #124234
- Background: Light neutral (off-white)
- Text: Dark gray/black for readability

**Typography:**
- Font Family: Pretendard (Google Fonts)
- Headings: Pretendard Bold, 24-32sp
- Body: Pretendard Regular, 16-18sp
- Captions: Pretendard Medium, 14sp

**Layout Principles:**
- Minimalist & distraction-free aesthetic
- Generous whitespace between elements
- Mobile-first: Portrait orientation optimized
- Thumb-friendly touch targets (minimum 48dp)
- One-handed use prioritization

### Screen Components
**Kakao Login Screen:**
- App logo centered at top third
- Kakao login button (official Kakao yellow branded button)
- Terms/Privacy checkboxes below button
- "로그인" confirmation button

**Onboarding Screens 1-4:**
- Large headline (Korean text, 28-32sp)
- Supporting body text (16-18sp, left-aligned)
- Optional illustration/icon area
- Progress dots indicator (bottom)
- "다음" button (bottom, fixed position)

**Profile Setup Screen (Screen 5):**
- Form fields with Material 3 text field styling
- Dropdown for industry (Material 3 exposed dropdown menu)
- Multi-line text area for growth goals (with character counter)
- Legal agreement checkboxes (compact, scrollable text)
- "완료" (Complete) button (enabled only when all required fields valid)

**Loading States:**
- Full-screen loading overlay during API calls
- Circular progress indicator (Material 3)
- "처리 중..." (Processing) text

**Error States:**
- Error messages in Korean with actionable next steps
- "다시 시도" (Retry) button for transient failures

### Responsive Breakpoints
- Target: Portrait phones (360dp - 420dp width)
- Tablet support: Deferred to Phase 2
- Landscape: Show warning to rotate to portrait for optimal experience

## Reusable Components

### New Components Required
**Why New:** This is a greenfield project with no existing Android codebase.

**Jetpack Compose Components to Build:**

1. **MemoirButton** (Primary and Secondary variants)
   - Primary: Filled button with mustard yellow background
   - Secondary: Outlined button with carrot orange border
   - Props: `text: String`, `onClick: () -> Unit`, `enabled: Boolean`, `isLoading: Boolean`, `modifier: Modifier`
   - Material 3 Button with Memoir theme customization

2. **MemoirTextField**
   - Material 3 OutlinedTextField with Memoir styling
   - Props: `value: String`, `onValueChange: (String) -> Unit`, `label: String`, `placeholder: String`, `errorMessage: String?`, `isError: Boolean`, `keyboardType: KeyboardType`, `maxLines: Int`
   - Korean input method editor (IME) support

3. **MemoirDropdown**
   - Material 3 ExposedDropdownMenuBox
   - Props: `selectedValue: String`, `options: List<String>`, `onValueChange: (String) -> Unit`, `label: String`, `errorMessage: String?`
   - Korean text rendering

4. **OnboardingProgressIndicator**
   - Custom horizontal dots indicator (1-5)
   - Props: `currentStep: Int`, `totalSteps: Int`
   - Active dot: Mustard yellow, Inactive: Light gray

5. **LoadingOverlay**
   - Full-screen dimmed background with circular progress
   - Props: `isLoading: Boolean`, `message: String`
   - Blocks user interaction when visible

6. **ErrorMessage**
   - Inline error text component
   - Props: `message: String`, `onRetry: (() -> Unit)?`
   - Korean error message display with optional retry action

7. **KakaoLoginButton**
   - Official Kakao SDK button wrapper
   - Branded Kakao yellow with "카카오로 시작하기" text
   - Props: `onClick: () -> Unit`, `isLoading: Boolean`

8. **LegalCheckbox**
   - Checkbox with linked text ("이용약관 동의" with underlined clickable link)
   - Props: `checked: Boolean`, `onCheckedChange: (Boolean) -> Unit`, `label: String`, `linkText: String`, `onLinkClick: () -> Unit`

## Technical Approach

### Architecture: MVVM + Clean Architecture

**Layer Structure:**

```
app/
├── presentation/        # UI Layer (Jetpack Compose + ViewModels)
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   ├── LoginViewModel.kt
│   ├── onboarding/
│   │   ├── OnboardingScreen.kt
│   │   ├── OnboardingViewModel.kt
│   │   ├── ProfileSetupScreen.kt
│   │   ├── ProfileSetupViewModel.kt
│   └── components/     # Reusable UI components
│       ├── MemoirButton.kt
│       ├── MemoirTextField.kt
│       └── ...
├── domain/             # Business Logic Layer
│   ├── model/
│   │   ├── User.kt
│   │   ├── UserProfile.kt
│   │   ├── AuthState.kt
│   ├── usecase/
│   │   ├── LoginWithKakaoUseCase.kt
│   │   ├── SaveProfileUseCase.kt
│   │   ├── CheckOnboardingStatusUseCase.kt
│   │   ├── RefreshTokenUseCase.kt
│   ├── repository/
│   │   ├── AuthRepository.kt (interface)
│   │   ├── UserRepository.kt (interface)
├── data/               # Data Layer
│   ├── repository/
│   │   ├── AuthRepositoryImpl.kt
│   │   ├── UserRepositoryImpl.kt
│   ├── remote/
│   │   ├── api/
│   │   │   ├── AuthApi.kt (Retrofit interface)
│   │   │   ├── UserApi.kt
│   │   ├── dto/
│   │   │   ├── KakaoAuthRequest.kt
│   │   │   ├── ProfileRequest.kt
│   │   │   ├── AuthResponse.kt
│   │   │   ├── ErrorResponse.kt
│   ├── local/
│   │   ├── database/
│   │   │   ├── MemoirDatabase.kt (Room)
│   │   │   ├── UserDao.kt
│   │   │   ├── UserEntity.kt
│   │   ├── datastore/
│   │   │   ├── AuthDataStore.kt (Tokens + onboarding flag)
│   ├── mapper/
│   │   ├── UserMapper.kt (DTO ↔ Domain ↔ Entity)
├── di/                 # Dependency Injection (Hilt modules)
│   ├── NetworkModule.kt
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   ├── UseCaseModule.kt
└── util/
    ├── ValidationUtils.kt
    ├── Constants.kt
```

### State Management with StateFlow

**AuthViewModel State:**
```kotlin
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val isOnboardingComplete: Boolean) : AuthState()
    data class Error(val message: String) : AuthState()
}
```

**OnboardingViewModel State:**
```kotlin
data class OnboardingUiState(
    val currentScreen: Int = 1,
    val isLoading: Boolean = false
)
```

**ProfileSetupViewModel State:**
```kotlin
data class ProfileSetupState(
    val name: String = "",
    val nameError: String? = null,
    val role: String = "",
    val roleError: String? = null,
    val industry: String = "",
    val industryError: String? = null,
    val growthGoals: String = "",
    val growthGoalsError: String? = null,
    val tosAccepted: Boolean = false,
    val privacyAccepted: Boolean = false,
    val depositAccepted: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val isFormValid: Boolean = false
)
```

### API Integration Details

**Base Configuration (Retrofit + OkHttp):**
- Base URL: `https://api.memoir.app/v1/` (environment-configurable)
- Timeout: 30s connect, 30s read, 30s write
- Interceptors: AuthTokenInterceptor (adds Bearer token), LoggingInterceptor (debug only)
- Serialization: Kotlinx Serialization (JSON)

**Endpoints:**

1. **POST `/api/v1/auth/kakao`**
   - Request: `{ "kakao_oauth_code": "string" }`
   - Response 200: `{ "access_token": "string", "refresh_token": "string", "user_exists": boolean, "onboarding_completed": boolean }`
   - Errors: `400 INVALID_CODE`, `502 KAKAO_UPSTREAM_ERROR`

2. **GET `/api/v1/users/me`**
   - Headers: `Authorization: Bearer {access_token}`
   - Response 200: `{ "user": { "id": "uuid", "kakao_id": "string" }, "profile": {...}, "flags": { "onboarding_completed": boolean } }`
   - Errors: `401 UNAUTHORIZED`, `404 NOT_FOUND`

3. **POST `/api/v1/users/profile`**
   - Headers: `Authorization: Bearer {access_token}`
   - Request: `{ "name": "string", "role": "string", "industry_code": "string", "growth_goals": "string" }`
   - Response 201: `{ "user_id": "uuid", "profile": {...}, "onboarding_completed_at": "timestamp" }`
   - Errors: `400 VALIDATION_ERROR`, `409 PROFILE_ALREADY_SET`, `401 UNAUTHORIZED`

4. **POST `/api/v1/auth/refresh`**
   - Request: `{ "refresh_token": "string" }`
   - Response 200: `{ "access_token": "string", "refresh_token": "string" }`
   - Errors: `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`

**Error Response Format:**
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "사용자 정보가 올바르지 않습니다",
    "details": {
      "field": "name",
      "reason": "한글 이름만 입력 가능합니다"
    }
  }
}
```

**Retry Strategy:**
- Network timeouts: Exponential backoff (1s, 2s, 4s max 2 retries)
- 5xx server errors: Retry once after 2s delay
- 4xx client errors: No retry, show validation errors
- 401 Unauthorized: Attempt token refresh once, then force re-login

### Data Flow

**Login Flow:**
1. User taps Kakao Login button
2. LoginViewModel calls `LoginWithKakaoUseCase`
3. UseCase invokes Kakao SDK → Receives OAuth code
4. UseCase calls `AuthRepository.loginWithKakao(code)`
5. Repository makes API call `POST /auth/kakao`
6. API returns `access_token`, `refresh_token`, `onboarding_completed`
7. Repository stores tokens in encrypted DataStore
8. Repository caches onboarding status
9. ViewModel emits `AuthState.Success(isOnboardingComplete)`
10. UI navigates to Onboarding or Cohort Feed based on flag

**Profile Submission Flow:**
1. User fills profile form, taps "완료"
2. ProfileSetupViewModel validates all fields client-side
3. If valid, calls `SaveProfileUseCase`
4. UseCase calls `UserRepository.saveProfile(profile)`
5. Repository makes API call `POST /users/profile` with Bearer token
6. API returns user profile + sets `onboarding_completed = true`
7. Repository updates local Room database
8. Repository updates onboarding flag in DataStore
9. ViewModel emits success state
10. UI navigates to Cohort Feed

**Token Refresh Flow:**
1. App cold start → Check access token expiry in DataStore
2. If expired but refresh token valid, call `RefreshTokenUseCase`
3. UseCase calls `AuthRepository.refreshToken()`
4. Repository makes API call `POST /auth/refresh`
5. API returns new token pair (access + refresh rotated)
6. Repository stores new tokens, invalidates old refresh token
7. App proceeds to main screen with fresh session

### Local Database Schema (Room)

**UserEntity:**
```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val kakaoId: String,
    val name: String?,
    val role: String?,
    val industryCode: String?,
    val growthGoals: String?,
    val photoUrl: String?,
    val onboardingCompletedAt: Long?,
    val createdAt: Long,
    val lastLoginAt: Long
)
```

**DataStore Keys:**
- `access_token: String?`
- `refresh_token: String?`
- `access_token_expiry: Long`
- `refresh_token_expiry: Long`
- `onboarding_completed: Boolean`
- `user_id: String?`

### Dependency Injection (Hilt Modules)

**NetworkModule:**
- Provides Retrofit instance
- Provides OkHttpClient with interceptors
- Provides API interfaces (AuthApi, UserApi)

**DatabaseModule:**
- Provides Room database instance
- Provides DAOs (UserDao)

**DataStoreModule:**
- Provides DataStore instance (encrypted)

**RepositoryModule:**
- Binds repository interfaces to implementations
- Provides AuthRepository, UserRepository

**UseCaseModule:**
- Provides use case instances
- Injects repositories into use cases

### Korean Localization Implementation

**Strings Resource (`res/values/strings.xml`):**
```xml
<resources>
    <string name="kakao_login_button">카카오로 시작하기</string>
    <string name="onboarding_screen1_headline">일상의 배움이 휘발되고 있지 않나요?</string>
    <string name="onboarding_screen2_headline">다양한 분야의 성장하는 사람들과 함께</string>
    <string name="profile_field_name">이름</string>
    <string name="profile_field_role">직무/직책</string>
    <string name="profile_field_industry">업종</string>
    <string name="profile_field_growth_goals">성장 목표</string>
    <string name="error_name_invalid">한글 이름만 입력 가능합니다 (2-4자)</string>
    <string name="error_growth_goals_too_short">성장 목표를 100자 이상 입력해주세요</string>
    <string name="button_next">다음</string>
    <string name="button_complete">완료</string>
    <!-- ... more strings -->
</resources>
```

**Validation Messages (Korean):**
- Name: "한글 이름만 입력 가능합니다 (2-4자)"
- Role: "직무/직책을 입력해주세요"
- Industry: "업종을 선택해주세요"
- Growth Goals (too short): "성장 목표를 100자 이상 입력해주세요"
- Growth Goals (too long): "성장 목표는 500자 이하로 입력해주세요"
- Network error: "네트워크 연결을 확인해주세요"
- Server error: "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요"

**Number/Currency Formatting:**
```kotlin
// Use DecimalFormat with Korean locale
val formatter = DecimalFormat("#,###")
val formatted = "${formatter.format(200000)}원" // "200,000원"
```

**Date/Time Formatting:**
```kotlin
// Use SimpleDateFormat with Korean locale
val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN)
val timeZone = TimeZone.getTimeZone("Asia/Seoul")
dateFormat.timeZone = timeZone
```

### Security Implementation

**Token Storage (Encrypted DataStore):**
- Use Android Keystore for encryption keys
- EncryptedSharedPreferences (AndroidX Security library)
- Keys stored in hardware-backed keystore when available
- No tokens in SharedPreferences or plain files

**Input Validation:**
- Client-side: Regex validation for Korean name `^[가-힣]{2,4}$`
- Client-side: Length checks (100-500 chars for growth goals)
- Server-side: Backend validates all inputs (never trust client)
- SQL injection prevention: Room uses parameterized queries

**Network Security:**
- HTTPS-only (enforce TLS 1.3)
- Certificate pinning: Deferred to Phase 2
- No sensitive data in logs (strip tokens, PII)
- Request signing: Deferred to Phase 2

**Session Security:**
- 30-day access token expiry (short-lived)
- 90-day refresh token expiry with rotation
- Old refresh token invalidated after rotation
- Force re-login if refresh fails

**PII Handling:**
- Name and growth goals are PII
- No PII in analytics events (use user_id only)
- No PII in crash logs (scrubbed by Crashlytics filters)

## Out of Scope

### Excluded from Phase 1 MVP
- Profile editing functionality (profiles locked until Phase 2)
- Account deletion feature (manual support process; automated in Phase 3)
- Multiple authentication providers (Google, Naver, Apple - wait for demand)
- Email verification and email-based authentication
- Phone number verification (not needed with Kakao OAuth)
- Two-factor authentication and biometric re-authentication
- Profile photo upload during onboarding (optional field deferred; placeholder used)
- Onboarding re-play or tutorial mode
- Language selection and internationalization (Korean only)
- Advanced accessibility (screen reader optimization, high contrast mode - Phase 4)
- Animated onboarding transitions (static screens acceptable)
- Detailed analytics funnel (basic Firebase events only; Mixpanel in Phase 3)
- Password reset flows (no passwords; Kakao handles recovery)
- Social sharing or referral codes during registration
- Deposit payment integration (manual email process for Phase 1)

### Future Enhancements
- Profile editing UI (Phase 2)
- Photo upload with S3 integration (Phase 2)
- Account deletion with GDPR/PIPA compliance (Phase 3)
- Biometric re-authentication for sensitive actions (Phase 2)
- Additional authentication providers if demand >15% of support inquiries
- Mixpanel analytics integration (Phase 3)
- Accessibility improvements (Phase 4)

## Success Criteria

### Functional Success
- User can authenticate with Kakao and receive valid session tokens
- Profile with invalid Korean name (non-hangul) triggers inline error message
- Growth goals <100 characters prevents form submission with validation message
- Refresh token rotation returns new token pair; old refresh token becomes invalid
- Profile cannot be overwritten (second POST returns `409 PROFILE_ALREADY_SET`)
- Onboarding completion persists; app relaunch skips to Cohort Feed

### Performance Success
- Kakao login response time: <3 seconds (network dependent)
- Profile submission API call: <2 seconds (p95)
- App cold start with auth check: <3 seconds
- Token refresh: <1.5 seconds (non-blocking background)
- Onboarding screen transitions: <300ms (smooth)

### Quality Success
- Crash rate <2% on onboarding flow
- Authentication failure rate <2% (excluding user cancellation)
- Profile submission success rate >95%
- Onboarding completion rate: 65-70% (pilot cohort target)
- Average onboarding duration: 3-5 minutes

### User Comprehension
- Post-onboarding survey: >70% correctly understand deposit system
- Support inquiries about deposit mechanics: <10% of users
- Terms acceptance rate: >95% (clear presentation)

## Analytics Events

### Events to Track (Firebase Analytics)

| Event Name | Properties | Trigger Point |
|------------|-----------|---------------|
| `kakao_login_initiated` | `source: "button"` | User taps Kakao login button |
| `kakao_login_success` | `user_new: boolean` | OAuth flow completed |
| `kakao_login_failure` | `error_code: string` | Kakao login error |
| `onboarding_started` | `entry_point: "auto"` | First screen shown |
| `onboarding_screen_viewed` | `screen_index: int` | Each screen transition |
| `onboarding_abandoned` | `last_screen_index: int` | App force-quit during onboarding |
| `onboarding_completed` | - | All screens viewed, profile submitted |
| `profile_submission_started` | `growth_goals_length: int` | User taps "완료" button |
| `profile_submission_success` | `industry_code: string` | Profile API call success |
| `profile_submission_failure` | `error_code: string` | Profile API call failure |
| `terms_accepted` | `tos_version: string, privacy_version: string` | Checkboxes ticked |
| `session_token_refreshed` | `rotation: boolean` | Token refresh API call |

## Testing Strategy

### Unit Tests (Target: 30-40% coverage)
- `NameValidator`: Test hangul regex validation
- `GrowthGoalsValidator`: Test length boundaries (99, 100, 500, 501 chars)
- `TokenRefresher`: Test rotation logic, expiry calculation
- `LoginWithKakaoUseCase`: Mock repository, verify token storage
- `SaveProfileUseCase`: Mock repository, verify validation before save

### UI Tests (Jetpack Compose)
- OnboardingScreen: Verify 5 screens rendered, progress indicator updates
- ProfileSetupScreen: Verify form validation, error messages appear
- Button states: "완료" button disabled until all fields valid
- Dropdown selection: Industry dropdown shows all 8 options

### Integration Tests
- Kakao SDK mock: Simulate successful login, token exchange
- API integration: Mock server responses (200, 400, 401, 409, 500)
- Token refresh: Verify old token invalidated after rotation
- Database persistence: Profile saved to Room, retrievable after app restart

### Manual Testing (Korean Locale)
- Real Kakao login with test accounts
- Korean text input (hangul name, growth goals with Korean characters)
- Network interruption scenarios (airplane mode mid-submission)
- Token expiry handling (manually expire tokens in DataStore)

## Dependencies & Prerequisites

### External Services Setup
- Kakao Developer account and app registration (2-3 business days approval)
- Firebase project with Kakao provider enabled
- Backend API deployed and accessible
- Legal documents finalized (TOS, Privacy Policy, Deposit Policy)

### Development Dependencies (build.gradle.kts)
```kotlin
// Jetpack Compose
implementation("androidx.compose.ui:ui:1.6.0")
implementation("androidx.compose.material3:material3:1.2.0")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

// Kakao SDK
implementation("com.kakao.sdk:v2-user:2.19.0")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

// Local Storage
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.datastore:datastore-preferences:1.0.0")
implementation("androidx.security:security-crypto:1.1.0-alpha06")

// Dependency Injection
implementation("com.google.dagger:hilt-android:2.50")
kapt("com.google.dagger:hilt-compiler:2.50")

// Firebase
implementation("com.google.firebase:firebase-analytics:21.5.0")
implementation("com.google.firebase:firebase-crashlytics:18.6.0")

// Image Loading
implementation("io.coil-kt:coil-compose:2.5.0")

// Testing
testImplementation("junit:junit:4.13.2")
testImplementation("io.mockk:mockk:1.13.8")
androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
```

### Content Requirements
- Korean copywriting for 5 onboarding screens (professionally written)
- Figma designs for all screens (login + 5 onboarding + profile setup)
- Backend API OpenAPI/Swagger documentation
- Legal documents in Korean (TOS, Privacy, Deposit Policy) with version numbers

## Risk Mitigation

| Risk | Impact | Mitigation Strategy |
|------|--------|-------------------|
| Kakao API downtime or rate limiting | Auth blocked | Exponential backoff, clear error messages, status page fallback |
| Low onboarding completion (<65%) | Funnel leakage | Compress copy, progress indicator, measure abandonment per screen |
| Growth goals low quality (shallow input) | Weak cohort matching data | Enforce 100-char minimum, add example tooltip |
| Token theft on rooted device | Account misuse | Phase 2: Device binding, manual logout all sessions |
| Manual deposit confusion | User distrust | Clear copy: "24h email SLA", manual status updates |
| Backend API latency (>2s p95) | Poor UX | Loading states, timeout handling, retry logic with feedback |
| Korean translation quality issues | User confusion | Native Korean speaker review, pilot user testing |

## Open Questions for Product Team

1. **Terms Versioning:** Should we version TOS/Privacy in API headers or database table? (Needed for audit trail)
2. **Profile Edit Audit:** When profile editing unlocks in Phase 2, do we soft-delete old values or maintain full audit log?
3. **User Record Creation:** Should we create user record before Kakao exchange or lazily on profile submit? (Current assumption: create on first login with `onboarding_completed=false`)
4. **Industry "기타" Handling:** If user selects "기타" (Other), should we collect free text for manual categorization?

## Appendix: Validation Rules Summary

### Client-Side Validation
- **Name:** Regex `^[가-힣]{2,4}$` (2-4 hangul characters)
- **Role:** 2-64 characters, allow Korean, English, spaces
- **Industry:** Must be one of 8 enum codes
- **Growth Goals:** 100-500 characters enforced
- **All Fields:** Required (except profile photo)

### Server-Side Validation
- Duplicate Kakao ID: Return `409 CONFLICT`
- Invalid hangul name: Return `400 VALIDATION_ERROR`
- Profile already set: Return `409 PROFILE_ALREADY_SET`
- Missing required fields: Return `400 VALIDATION_ERROR` with details

### Industry Enum Codes
```kotlin
enum class IndustryCode(val displayName: String) {
    STARTUP("스타트업"),
    ENTERPRISE("대기업"),
    SME("중소기업"),
    PUBLIC("공기업/공공기관"),
    FOREIGN("외국계 기업"),
    FREELANCER("프리랜서/1인 기업"),
    NONPROFIT("비영리/사회적 기업"),
    OTHER("기타")
}
```

---

**Spec Version:** 1.0
**Last Updated:** 2025-11-19
**Status:** Ready for Implementation
**Estimated Effort:** 3-4 weeks (1 developer)
