# Task Breakdown: User Authentication & Onboarding

## Overview
Total Tasks: 8 task groups
Estimated Effort: 3-4 weeks (1 developer)

This is a greenfield Android project implementing Kakao-based authentication with a mandatory 5-screen onboarding flow in Korean language.

## Task List

### Project Initialization

#### Task Group 1: Android Project Setup & Dependencies
**Dependencies:** None

- [ ] 1.0 Initialize Android project and configure dependencies
  - [ ] 1.1 Create new Android Studio project
    - Application name: "Memoir"
    - Package name: `com.memoir.app`
    - Minimum SDK: API 26 (Android 8.0 Oreo)
    - Target SDK: API 34 (Android 14)
    - Language: Kotlin
    - Build configuration: Kotlin DSL (build.gradle.kts)
  - [ ] 1.2 Configure Gradle dependencies
    - Jetpack Compose (UI 1.6.0, Material3 1.2.0, ViewModel Compose 2.7.0)
    - Kakao SDK (v2-user 2.19.0)
    - Networking: Retrofit 2.9.0, OkHttp 4.12.0, Kotlinx Serialization 1.6.0
    - Local Storage: Room 2.6.1, DataStore Preferences 1.0.0, Security Crypto 1.1.0-alpha06
    - Dependency Injection: Hilt 2.50
    - Firebase: Analytics 21.5.0, Crashlytics 18.6.0
    - Image Loading: Coil Compose 2.5.0
    - Testing: JUnit 4.13.2, Mockk 1.13.8, Compose UI Test 1.6.0
  - [ ] 1.3 Set up Kakao SDK configuration
    - Register app on Kakao Developers console
    - Add Kakao app key to `local.properties`
    - Configure Kakao redirect scheme in AndroidManifest.xml
    - Initialize Kakao SDK in Application class
  - [ ] 1.4 Set up Firebase project
    - Create Firebase project for Memoir
    - Download and add `google-services.json`
    - Configure Firebase Analytics and Crashlytics plugins
    - Enable Kakao authentication provider in Firebase Console
  - [ ] 1.5 Create project package structure
    - `/presentation` (UI Layer: Screens, ViewModels, Components)
    - `/domain` (Business Logic: Models, Use Cases, Repository Interfaces)
    - `/data` (Data Layer: Repository Implementations, API, Database, DataStore)
    - `/di` (Dependency Injection: Hilt Modules)
    - `/util` (Utilities: Validation, Constants, Extensions)
  - [ ] 1.6 Configure Hilt for dependency injection
    - Create Application class with @HiltAndroidApp
    - Add Hilt Gradle plugin and dependencies
    - Create placeholder network, database, and repository modules

**Acceptance Criteria:**
- Project builds successfully with all dependencies resolved
- Kakao SDK initializes without errors
- Firebase Analytics connection verified in Firebase Console
- Package structure follows MVVM + Clean Architecture pattern
- Hilt DI configured and compiling

---

### Core Architecture

#### Task Group 2: Core Architecture & Design System
**Dependencies:** Task Group 1

- [ ] 2.0 Set up core architecture patterns and design system
  - [ ] 2.1 Write 2-8 focused tests for core utilities
    - Limit to 2-8 highly focused tests maximum
    - Test only critical validation logic (e.g., Korean name regex, growth goals length)
    - Skip exhaustive testing of all utility methods
  - [ ] 2.2 Create domain models
    - `User.kt`: `id`, `kakaoId`, `createdAt`, `lastLoginAt`, `status`
    - `UserProfile.kt`: `userId`, `name`, `role`, `industryCode`, `growthGoals`, `photoUrl`, `onboardingCompletedAt`
    - `AuthState.kt`: Sealed class (Idle, Loading, Success, Error)
    - `IndustryCode.kt`: Enum with Korean display names (STARTUP, ENTERPRISE, SME, PUBLIC, FOREIGN, FREELANCER, NONPROFIT, OTHER)
  - [ ] 2.3 Create validation utilities
    - `ValidationUtils.kt`:
      - `validateKoreanName(name: String): ValidationResult` - Regex `^[가-힣]{2,4}$`
      - `validateRole(role: String): ValidationResult` - 2-64 characters
      - `validateGrowthGoals(goals: String): ValidationResult` - 100-500 characters
      - `validateIndustry(code: String): ValidationResult` - Must be valid enum
  - [ ] 2.4 Create Material3 theme with Memoir design system
    - `ui/theme/Color.kt`: Define color palette (Mustard #F4BA54, Carrot Orange #E86221, Dark Olive #124234)
    - `ui/theme/Typography.kt`: Configure Pretendard font family
    - `ui/theme/Theme.kt`: Material3 theme configuration
  - [ ] 2.5 Create constants file
    - `Constants.kt`: API base URL, token expiry durations, DataStore keys, analytics event names
  - [ ] 2.6 Create Result sealed class for API responses
    - `Result.kt`: Sealed class (Success, Error, Loading)
  - [ ] 2.7 Ensure core architecture tests pass
    - Run ONLY the 2-8 tests written in 2.1
    - Verify validation logic works correctly
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-8 tests written in 2.1 pass
- Domain models compile and follow Kotlin data class best practices
- Validation utilities correctly validate Korean name, role, industry, growth goals
- Material3 theme displays correct Memoir colors
- Pretendard font loads successfully

---

### Authentication Layer

#### Task Group 3: Kakao Authentication & API Integration
**Dependencies:** Task Group 2

- [ ] 3.0 Implement Kakao authentication and backend API integration
  - [ ] 3.1 Write 2-8 focused tests for authentication use cases
    - Limit to 2-8 highly focused tests maximum
    - Test only critical auth flows (e.g., successful login, token storage, token refresh)
    - Skip exhaustive testing of all error scenarios
  - [ ] 3.2 Create API data transfer objects (DTOs)
    - `data/remote/dto/KakaoAuthRequest.kt`: `kakao_oauth_code`
    - `data/remote/dto/AuthResponse.kt`: `access_token`, `refresh_token`, `user_exists`, `onboarding_completed`
    - `data/remote/dto/ProfileRequest.kt`: `name`, `role`, `industry_code`, `growth_goals`
    - `data/remote/dto/ProfileResponse.kt`: `user_id`, `profile`, `onboarding_completed_at`
    - `data/remote/dto/ErrorResponse.kt`: `error.code`, `error.message`, `error.details`
  - [ ] 3.3 Create Retrofit API interfaces
    - `data/remote/api/AuthApi.kt`:
      - `POST /api/v1/auth/kakao`: Exchange Kakao OAuth code for tokens
      - `POST /api/v1/auth/refresh`: Refresh access token
    - `data/remote/api/UserApi.kt`:
      - `GET /api/v1/users/me`: Get current user profile
      - `POST /api/v1/users/profile`: Submit user profile
  - [ ] 3.4 Create Hilt network module
    - `di/NetworkModule.kt`:
      - Provide Retrofit instance with base URL
      - Provide OkHttpClient with interceptors (AuthTokenInterceptor, LoggingInterceptor)
      - Provide Kotlinx Serialization converter
      - Provide API interface instances
  - [ ] 3.5 Create repository interfaces and implementations
    - `domain/repository/AuthRepository.kt` (interface):
      - `loginWithKakao(code: String): Result<AuthResponse>`
      - `refreshToken(): Result<AuthResponse>`
      - `logout()`
    - `data/repository/AuthRepositoryImpl.kt`:
      - Implement using AuthApi and local token storage
      - Handle error mapping to domain errors
  - [ ] 3.6 Create authentication use cases
    - `domain/usecase/LoginWithKakaoUseCase.kt`:
      - Invoke Kakao SDK to get OAuth code
      - Call AuthRepository.loginWithKakao()
      - Store tokens in DataStore
      - Return authentication state
    - `domain/usecase/RefreshTokenUseCase.kt`:
      - Check token expiry
      - Call AuthRepository.refreshToken()
      - Update stored tokens
    - `domain/usecase/LogoutUseCase.kt`:
      - Clear tokens from DataStore
      - Clear local user data
  - [ ] 3.7 Ensure authentication layer tests pass
    - Run ONLY the 2-8 tests written in 3.1
    - Verify critical auth flows work
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-8 tests written in 3.1 pass
- Kakao SDK returns OAuth code when user logs in
- AuthApi successfully exchanges code for tokens
- Tokens stored securely in encrypted DataStore
- Token refresh works correctly with rotation
- Error responses mapped to user-friendly Korean messages

---

### Data Layer

#### Task Group 4: Local Data Storage (Room + DataStore)
**Dependencies:** Task Group 2

- [ ] 4.0 Set up local data persistence layer
  - [ ] 4.1 Write 2-8 focused tests for data layer
    - Limit to 2-8 highly focused tests maximum
    - Test only critical database operations (e.g., user insert, profile retrieval, token storage)
    - Skip exhaustive testing of all database scenarios
  - [ ] 4.2 Create Room database entities
    - `data/local/database/UserEntity.kt`:
      - Fields: `id`, `kakaoId`, `name`, `role`, `industryCode`, `growthGoals`, `photoUrl`, `onboardingCompletedAt`, `createdAt`, `lastLoginAt`
      - Table: `users`
  - [ ] 4.3 Create Room DAO
    - `data/local/database/UserDao.kt`:
      - `insertUser(user: UserEntity)`
      - `getUserById(id: String): UserEntity?`
      - `updateUser(user: UserEntity)`
      - `deleteUser(id: String)`
  - [ ] 4.4 Create Room database
    - `data/local/database/MemoirDatabase.kt`:
      - Configure database with UserDao
      - Set up database migrations strategy
  - [ ] 4.5 Create encrypted DataStore for tokens
    - `data/local/datastore/AuthDataStore.kt`:
      - Store/retrieve: `access_token`, `refresh_token`, `access_token_expiry`, `refresh_token_expiry`
      - Store/retrieve: `onboarding_completed` flag, `user_id`
      - Use EncryptedSharedPreferences with Android Keystore
  - [ ] 4.6 Create data mappers
    - `data/mapper/UserMapper.kt`:
      - `toEntity(domain: User): UserEntity`
      - `toDomain(entity: UserEntity): User`
      - `toEntity(dto: ProfileResponse): UserEntity`
  - [ ] 4.7 Create Hilt database module
    - `di/DatabaseModule.kt`:
      - Provide Room database instance
      - Provide UserDao
      - Provide DataStore instance
  - [ ] 4.8 Implement UserRepository
    - `domain/repository/UserRepository.kt` (interface):
      - `saveProfile(profile: UserProfile): Result<Unit>`
      - `getCurrentUser(): Result<User>`
      - `updateOnboardingStatus(completed: Boolean)`
    - `data/repository/UserRepositoryImpl.kt`:
      - Implement using UserApi, Room, and DataStore
      - Cache API responses locally
  - [ ] 4.9 Ensure data layer tests pass
    - Run ONLY the 2-8 tests written in 4.1
    - Verify critical database operations work
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-8 tests written in 4.1 pass
- Room database created successfully with UserEntity table
- UserDao operations work correctly (insert, retrieve, update)
- Tokens stored securely in encrypted DataStore
- Data mappers correctly convert between layers
- UserRepository caches data locally and syncs with API

---

### Onboarding UI

#### Task Group 5: Reusable UI Components & Onboarding Screens
**Dependencies:** Task Group 2

- [ ] 5.0 Build reusable UI components and onboarding screens
  - [ ] 5.1 Write 2-8 focused tests for UI components
    - Limit to 2-8 highly focused tests maximum
    - Test only critical component behaviors (e.g., button click, text input, screen navigation)
    - Skip exhaustive testing of all component states
  - [ ] 5.2 Create reusable Jetpack Compose components
    - `presentation/components/MemoirButton.kt`:
      - Primary variant (filled, mustard yellow background)
      - Secondary variant (outlined, carrot orange border)
      - Props: `text`, `onClick`, `enabled`, `isLoading`, `modifier`
    - `presentation/components/MemoirTextField.kt`:
      - Material3 OutlinedTextField with Memoir styling
      - Props: `value`, `onValueChange`, `label`, `placeholder`, `errorMessage`, `isError`, `keyboardType`, `maxLines`
      - Korean IME support
    - `presentation/components/MemoirDropdown.kt`:
      - Material3 ExposedDropdownMenuBox
      - Props: `selectedValue`, `options`, `onValueChange`, `label`, `errorMessage`
    - `presentation/components/OnboardingProgressIndicator.kt`:
      - Horizontal dots (1-5)
      - Props: `currentStep`, `totalSteps`
      - Active: mustard yellow, Inactive: light gray
    - `presentation/components/LoadingOverlay.kt`:
      - Full-screen dimmed background with circular progress
      - Props: `isLoading`, `message`
    - `presentation/components/ErrorMessage.kt`:
      - Inline error text with optional retry button
      - Props: `message`, `onRetry`
    - `presentation/components/KakaoLoginButton.kt`:
      - Official Kakao branded button
      - Props: `onClick`, `isLoading`
    - `presentation/components/LegalCheckbox.kt`:
      - Checkbox with linked text
      - Props: `checked`, `onCheckedChange`, `label`, `linkText`, `onLinkClick`
  - [ ] 5.3 Create Korean string resources
    - `res/values/strings.xml`:
      - All onboarding screen text in Korean
      - Validation error messages in Korean
      - Button labels ("다음", "완료", "다시 시도")
      - Legal document labels
      - Follow polite speech forms (존댓말)
  - [ ] 5.4 Create onboarding screen 1: Problem Framing
    - `presentation/onboarding/screens/OnboardingScreen1.kt`:
      - Headline: "일상의 배움이 휘발되고 있지 않나요?"
      - Body text explaining how daily learnings evaporate
      - Simple illustration placeholder
      - "다음" button at bottom
  - [ ] 5.5 Create onboarding screen 2: Community Value
    - `presentation/onboarding/screens/OnboardingScreen2.kt`:
      - Headline: "다양한 분야의 성장하는 사람들과 함께"
      - Body text about diverse professional community
      - Cohort model benefits
      - "다음" button
  - [ ] 5.6 Create onboarding screen 3: Structure Explanation
    - `presentation/onboarding/screens/OnboardingScreen3.kt`:
      - Headline: "10주 주기, 매주 한 번의 성찰"
      - Body text: 10-week cohorts, weekly reflection rhythm, peer feedback
      - Time commitment: ~30 min writing + 20 min reading weekly
      - "다음" button
  - [ ] 5.7 Create onboarding screen 4: Accountability Mechanism
    - `presentation/onboarding/screens/OnboardingScreen4.kt`:
      - Headline: "보증금 시스템으로 함께 성장하기"
      - Transparent breakdown:
        - Initial: 200,000원 deposit + 100,000원 first month
        - Monthly: 100,000원 fee
        - Refund: Full deposit if 70%+ completion
        - Deductions: 20,000원 per missed reflection, 10,000원 insufficient commenting
      - Emphasize: "성실히 참여하시면 보증금 전액 환급"
      - Social proof: "1기 참여자의 65%가 전액 환급받았어요"
      - Link to Deposit Policy
      - Note: "입금 계좌 안내는 이메일로 발송됩니다"
      - "다음" button
  - [ ] 5.8 Create OnboardingViewModel
    - `presentation/onboarding/OnboardingViewModel.kt`:
      - State: `OnboardingUiState(currentScreen, isLoading)`
      - Functions: `nextScreen()`, `completeOnboarding()`
      - Update onboarding completion flag in DataStore
  - [ ] 5.9 Create main onboarding container screen
    - `presentation/onboarding/OnboardingScreen.kt`:
      - Container that displays screens 1-4 based on current step
      - Progress indicator showing current screen (1/5 through 4/5)
      - No back button (forward-only progression)
      - Navigate to ProfileSetupScreen after screen 4
  - [ ] 5.10 Ensure onboarding UI tests pass
    - Run ONLY the 2-8 tests written in 5.1
    - Verify critical component behaviors work
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-8 tests written in 5.1 pass
- Reusable components render correctly with Memoir theme
- All 4 onboarding screens display Korean text correctly
- Progress indicator updates as user advances through screens
- No back navigation possible on screens 1-4
- Pretendard font displays Korean characters clearly

---

### Profile Setup

#### Task Group 6: Profile Setup Screen & Validation
**Dependencies:** Task Groups 2, 4, 5

- [ ] 6.0 Implement profile setup screen with validation
  - [ ] 6.1 Write 2-8 focused tests for profile setup
    - Limit to 2-8 highly focused tests maximum
    - Test only critical validation scenarios (e.g., invalid Korean name, growth goals too short, form submission)
    - Skip exhaustive testing of all validation rules
  - [ ] 6.2 Create ProfileSetupState data class
    - `presentation/profile/ProfileSetupState.kt`:
      - Fields: `name`, `nameError`, `role`, `roleError`, `industry`, `industryError`, `growthGoals`, `growthGoalsError`
      - Flags: `tosAccepted`, `privacyAccepted`, `depositAccepted`, `isSubmitting`, `submitError`, `isFormValid`
  - [ ] 6.3 Create ProfileSetupViewModel
    - `presentation/profile/ProfileSetupViewModel.kt`:
      - StateFlow: `ProfileSetupState`
      - Functions: `updateName()`, `updateRole()`, `updateIndustry()`, `updateGrowthGoals()`, `toggleTosAcceptance()`, `togglePrivacyAcceptance()`, `toggleDepositAcceptance()`
      - Validation: Real-time validation on field changes
      - Function: `submitProfile()` - calls SaveProfileUseCase
  - [ ] 6.4 Create SaveProfileUseCase
    - `domain/usecase/SaveProfileUseCase.kt`:
      - Validate all fields (Korean name, role, industry, growth goals length)
      - Call UserRepository.saveProfile()
      - Update onboarding completion status
      - Return Result with success or error
  - [ ] 6.5 Create Profile Setup Screen (Screen 5)
    - `presentation/profile/ProfileSetupScreen.kt`:
      - MemoirTextField for name (Korean hangul validation)
      - MemoirTextField for role (accepts Korean and English)
      - MemoirDropdown for industry (8 Korean options)
      - Multi-line MemoirTextField for growth goals (100-500 character counter)
      - LegalCheckbox for Terms of Service (with link)
      - LegalCheckbox for Privacy Policy (with link)
      - LegalCheckbox for Deposit Policy (with link)
      - "완료" (Complete) button (enabled only when all required fields valid and all checkboxes checked)
      - Progress indicator showing 5/5
      - LoadingOverlay during API submission
  - [ ] 6.6 Create industry dropdown options
    - Define list in Korean: "스타트업", "대기업", "중소기업", "공기업/공공기관", "외국계 기업", "프리랜서/1인 기업", "비영리/사회적 기업", "기타"
    - Map to IndustryCode enum values
  - [ ] 6.7 Implement real-time validation
    - Name: Show error immediately if non-hangul characters entered
    - Growth goals: Show character count (100-500), disable submit if outside range
    - All fields: Show inline error messages in Korean
  - [ ] 6.8 Handle profile submission
    - Show loading overlay during API call
    - On success: Mark onboarding complete, navigate to Cohort Feed placeholder
    - On error: Display error message with retry option
    - Handle 409 PROFILE_ALREADY_SET error
  - [ ] 6.9 Ensure profile setup tests pass
    - Run ONLY the 2-8 tests written in 6.1
    - Verify critical validation scenarios work
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-8 tests written in 6.1 pass
- Profile form displays all required fields correctly
- Korean name validation rejects non-hangul characters with inline error
- Growth goals character counter shows correctly (100-500 range)
- "완료" button disabled until all validations pass and checkboxes checked
- Profile submission API call succeeds and updates onboarding status
- Error handling works for network errors and validation failures
- Legal checkboxes link to policy documents (placeholder links for MVP)

---

### Authentication UI & Navigation

#### Task Group 7: Login Screen & App Navigation Flow
**Dependencies:** Task Groups 3, 5, 6

- [ ] 7.0 Implement login screen and complete navigation flow
  - [ ] 7.1 Write 2-8 focused tests for navigation flow
    - Limit to 2-8 highly focused tests maximum
    - Test only critical navigation scenarios (e.g., login to onboarding, onboarding to profile, profile to main app)
    - Skip exhaustive testing of all navigation paths
  - [ ] 7.2 Create LoginViewModel
    - `presentation/auth/LoginViewModel.kt`:
      - StateFlow: `AuthState` (Idle, Loading, Success, Error)
      - Function: `loginWithKakao()` - calls LoginWithKakaoUseCase
      - Handle Kakao SDK callbacks
      - Navigate based on onboarding completion status
  - [ ] 7.3 Create Login Screen
    - `presentation/auth/LoginScreen.kt`:
      - App logo centered at top third
      - KakaoLoginButton (official Kakao yellow branded button)
      - Loading state during authentication
      - Error message display with retry option
  - [ ] 7.4 Create navigation graph
    - `presentation/navigation/NavGraph.kt`:
      - Define routes: Login, Onboarding, ProfileSetup, CohortFeed (placeholder)
      - Set up NavHost with Compose Navigation
  - [ ] 7.5 Implement authentication check on app launch
    - `MainActivity.kt`:
      - Check if user is authenticated (tokens exist in DataStore)
      - Check if onboarding completed
      - Navigate to appropriate screen:
        - Not authenticated → Login
        - Authenticated + onboarding incomplete → Onboarding
        - Authenticated + onboarding complete → Cohort Feed
  - [ ] 7.6 Create Cohort Feed placeholder screen
    - `presentation/feed/CohortFeedScreen.kt`:
      - Simple placeholder screen with "환영합니다!" (Welcome) text
      - Confirms successful onboarding completion
  - [ ] 7.7 Implement CheckOnboardingStatusUseCase
    - `domain/usecase/CheckOnboardingStatusUseCase.kt`:
      - Check local DataStore flag
      - Optionally verify with backend API call
      - Return onboarding completion status
  - [ ] 7.8 Handle token refresh on app launch
    - In MainActivity or AuthViewModel
    - Check access token expiry
    - If expired but refresh token valid, call RefreshTokenUseCase
    - On refresh failure, force re-login
  - [ ] 7.9 Implement logout functionality (for testing)
    - Add logout button in Cohort Feed (dev mode only)
    - Calls LogoutUseCase
    - Navigates back to Login screen
  - [ ] 7.10 Ensure navigation flow tests pass
    - Run ONLY the 2-8 tests written in 7.1
    - Verify critical navigation scenarios work
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-8 tests written in 7.1 pass
- Login screen displays correctly with Kakao login button
- Successful Kakao login triggers token storage and navigation
- App launch checks authentication and navigates appropriately
- Onboarding flow progresses sequentially: Login → Onboarding (screens 1-4) → ProfileSetup → CohortFeed
- Token refresh happens automatically on app launch if needed
- Logout clears tokens and returns to login screen
- Navigation prevents going back during onboarding (no back button)

---

### Testing & Integration

#### Task Group 8: Integration Testing & Analytics
**Dependencies:** Task Groups 1-7

- [ ] 8.0 Complete integration testing and analytics implementation
  - [ ] 8.1 Review existing tests from Task Groups 1-7
    - Review the 2-8 tests written by each previous task group
    - Review tests from: core utilities (2.1), auth layer (3.1), data layer (4.1), UI components (5.1), profile setup (6.1), navigation (7.1)
    - Total existing tests: approximately 12-48 tests
  - [ ] 8.2 Analyze test coverage gaps for this feature only
    - Identify critical user workflows that lack test coverage
    - Focus ONLY on gaps related to authentication and onboarding feature
    - Do NOT assess entire application test coverage
    - Prioritize end-to-end workflows over unit test gaps
  - [ ] 8.3 Write up to 10 additional strategic tests maximum
    - Add maximum of 10 new integration tests to fill identified critical gaps
    - Focus on end-to-end scenarios:
      - Complete onboarding flow: Login → Onboarding → Profile → CohortFeed
      - Token refresh on app restart
      - Error handling: Network timeout during profile submission
      - Validation: Korean name with non-hangul characters rejected
      - Duplicate profile submission returns 409 error
    - Do NOT write comprehensive coverage for all scenarios
    - Skip performance tests and edge cases unless business-critical
  - [ ] 8.4 Implement Firebase Analytics events
    - `util/AnalyticsTracker.kt`:
      - Helper functions to track events
    - Implement event tracking:
      - `kakao_login_initiated` - User taps Kakao login button
      - `kakao_login_success` - OAuth flow completed (with `user_new` property)
      - `kakao_login_failure` - Kakao login error (with `error_code` property)
      - `onboarding_started` - First onboarding screen shown
      - `onboarding_screen_viewed` - Each screen transition (with `screen_index` property)
      - `onboarding_completed` - All screens viewed, profile submitted
      - `profile_submission_started` - User taps "완료" button (with `growth_goals_length` property)
      - `profile_submission_success` - Profile API call success (with `industry_code` property)
      - `profile_submission_failure` - Profile API call failure (with `error_code` property)
      - `terms_accepted` - Legal checkboxes ticked (with `tos_version`, `privacy_version` properties)
      - `session_token_refreshed` - Token refresh completed (with `rotation` property)
  - [ ] 8.5 Add error tracking with Firebase Crashlytics
    - Configure Crashlytics in Application class
    - Add non-fatal exception logging for:
      - Kakao login failures
      - API errors (4xx, 5xx)
      - Token refresh failures
    - Ensure PII scrubbed from crash logs (no tokens, no names)
  - [ ] 8.6 Test manual scenarios
    - Real Kakao login with test account
    - Korean text input (hangul name, growth goals with Korean characters)
    - Network interruption (toggle airplane mode during profile submission)
    - Token expiry handling (manually set expired token in DataStore)
    - App kill and restart (verify session persistence)
  - [ ] 8.7 Run feature-specific tests only
    - Run ONLY tests related to authentication and onboarding feature
    - Expected total: approximately 22-58 tests maximum (12-48 existing + up to 10 new)
    - Do NOT run the entire application test suite
    - Verify all critical workflows pass
  - [ ] 8.8 Create legal document placeholders
    - Create placeholder screens for:
      - Terms of Service (Korean)
      - Privacy Policy (Korean)
      - Deposit Policy (Korean)
    - Link from LegalCheckbox components
    - Mark as "TO BE FINALIZED" for MVP

**Acceptance Criteria:**
- All feature-specific tests pass (approximately 22-58 tests total)
- Critical user workflows covered: Complete onboarding flow, token refresh, error scenarios
- No more than 10 additional tests added when filling in testing gaps
- Firebase Analytics events firing correctly for all key user actions
- Crashlytics logging errors without exposing PII
- Manual testing scenarios completed successfully
- Korean text displays correctly throughout the app
- Session persistence works across app restarts
- Legal document placeholders created and linked

---

## Execution Order

Recommended implementation sequence:
1. **Project Initialization** (Task Group 1) - Set up Android project, dependencies, Firebase, Kakao SDK
2. **Core Architecture** (Task Group 2) - Domain models, validation utilities, design system
3. **Authentication Layer** (Task Group 3) - Kakao integration, API interfaces, auth use cases
4. **Data Layer** (Task Group 4) - Room database, DataStore, repositories
5. **Onboarding UI** (Task Group 5) - Reusable components, onboarding screens 1-4
6. **Profile Setup** (Task Group 6) - Profile setup screen (screen 5), validation, submission
7. **Authentication UI & Navigation** (Task Group 7) - Login screen, navigation graph, complete flow
8. **Testing & Integration** (Task Group 8) - Integration tests, analytics, manual testing

## Technical Notes

### Key Design Patterns
- **MVVM + Clean Architecture**: Separation of presentation, domain, and data layers
- **Repository Pattern**: Abstract data sources (API + local storage)
- **Use Case Pattern**: Encapsulate business logic in single-responsibility use cases
- **StateFlow**: Reactive UI updates with Jetpack Compose
- **Sealed Classes**: Type-safe state management (AuthState, Result)

### Security Considerations
- Tokens stored in encrypted DataStore using Android Keystore
- HTTPS-only API communication (TLS 1.3)
- Input validation on client and server side
- No PII in logs or crash reports
- Token rotation on refresh

### Korean Localization Requirements
- All UI text in Korean (존댓말 - polite speech)
- Korean name validation: Regex `^[가-힣]{2,4}$`
- Pretendard font family for Korean readability
- Currency formatting: "200,000원" with comma separators
- Date/time in KST (UTC+9)

### Performance Targets
- Kakao login response: <3 seconds
- Profile submission API: <2 seconds (p95)
- App cold start: <3 seconds
- Screen transitions: <300ms

### Test Coverage Strategy
- **During Development**: Write only 2-8 focused tests per task group
- **Test Execution**: Run only newly written tests, not entire suite
- **Integration Phase**: Add maximum 10 strategic tests to fill critical gaps
- **Total Expected**: 22-58 tests for complete authentication & onboarding feature
- **Focus**: Critical user workflows, not exhaustive coverage

## Success Metrics

### Functional Success
- User can authenticate with Kakao and receive valid session tokens
- Invalid Korean name (non-hangul) triggers inline error message
- Growth goals <100 characters prevents form submission with validation message
- Token refresh returns new token pair; old refresh token invalidated
- Profile cannot be overwritten (second POST returns 409 PROFILE_ALREADY_SET)
- Onboarding completion persists; app relaunch skips to Cohort Feed

### Quality Success
- Crash rate <2% on onboarding flow
- Authentication failure rate <2% (excluding user cancellation)
- Profile submission success rate >95%
- Onboarding completion rate: 65-70% (pilot cohort target)
- Average onboarding duration: 3-5 minutes

## Risk Mitigation

| Risk | Mitigation Strategy |
|------|-------------------|
| Kakao API downtime | Exponential backoff, clear error messages, retry logic |
| Low onboarding completion (<65%) | Compress copy, progress indicator, measure abandonment per screen |
| Backend API latency (>2s p95) | Loading states, timeout handling, retry with feedback |
| Korean translation quality issues | Native Korean speaker review, pilot user testing |
| Manual deposit process confusion | Clear copy explaining email notification within 24h |

## Dependencies & Prerequisites

### External Services
- Kakao Developer account and app registration (2-3 business days approval)
- Firebase project with billing enabled
- Backend API deployed and accessible at configured base URL
- Legal documents finalized (TOS, Privacy Policy, Deposit Policy)

### Content Requirements
- Korean copywriting for 5 onboarding screens (professionally written)
- Figma designs for all screens (optional but recommended)
- Backend API documentation (OpenAPI/Swagger)

### Development Environment
- Android Studio Hedgehog or later
- Kotlin 1.9+
- Java 17+
- Physical Android device or emulator (API 26+)
- Kakao app installed for testing (or Kakao SDK emulator support)

---

**Document Version:** 1.0
**Last Updated:** 2025-11-19
**Status:** Ready for Implementation
