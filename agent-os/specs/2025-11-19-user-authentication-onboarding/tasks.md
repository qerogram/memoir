# Task Breakdown: User Authentication & Onboarding (Phase 1 MVP)

## Executive Summary

**Project:** Memoir - Weekly Reflection Community Platform
**Feature:** User Authentication & Onboarding
**Phase:** Phase 1 MVP (Weeks 3-12)
**Scope:** Kakao OAuth integration, 5-screen mandatory onboarding, profile setup, session management (30-day access / 90-day refresh tokens)
**Target Platform:** Android (Kotlin + Jetpack Compose)
**Backend:** Node.js + Express + TypeScript + PostgreSQL
**Total Tasks:** 52 sub-tasks across 8 task groups
**Estimated Timeline:** 8-10 weeks (parallel frontend + backend development)

---

## Project Overview

### Feature Goals

The User Authentication & Onboarding system establishes the entry point for all new users to access the Memoir platform. This MVP focuses on:

1. **Seamless Kakao OAuth Integration** - Leverage Kakao as the sole authentication provider (90%+ user adoption in Korea)
2. **Mandatory Guided Onboarding** - 5-screen progressive disclosure explaining app concept, community value, deposit system, and collecting initial profile
3. **Secure Session Management** - JWT tokens with 30-day access + 90-day refresh token rotation for persistent mobile sessions
4. **Complete Profile Setup** - Collect essential data (name, role, industry, growth goals) with Korean-specific validation
5. **Foundation for Phase 2** - Establish secure patterns for user data, authentication, and API integration

### Success Criteria

| Metric | Target | Validation |
|--------|--------|-----------|
| **Onboarding Completion Rate** | ≥70% of authenticated users | Firebase Analytics event tracking |
| **Profile Submission Success** | ≥95% success rate, <2% failures | API error logging + user testing |
| **Crash Rate** | <2% during onboarding flow | Firebase Crashlytics monitoring |
| **Authentication Failure Rate** | <2% (excluding user cancellation) | Server-side auth logs |
| **Token Refresh Reliability** | ≥99% automatic refresh success | Token middleware instrumentation |
| **Average Onboarding Duration** | 3-5 minutes per user | Firebase Analytics session duration |
| **Korean Name Validation** | Rejects non-Hangul, accepts 2-4 chars | Unit test coverage of regex |
| **Kakao Login Response Time** | <3 seconds (user-perceived) | Performance monitoring |

### Key Milestones

1. **Week 1-2:** Backend setup + database schema + API contracts finalized
2. **Week 2-3:** Frontend project setup + MVVM architecture + Kakao SDK integration
3. **Week 4-5:** Login screen + onboarding screens 1-3 UI complete
4. **Week 5-6:** Onboarding screens 4-5 + profile form + API integration
5. **Week 6-7:** End-to-end flow testing + error handling
6. **Week 7-8:** Performance optimization + security hardening
7. **Week 8-9:** User acceptance testing (UAT) + Korean copywriting review
8. **Week 9-10:** Soft launch + monitoring setup + bugfixes + GA launch

---

## Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Android Mobile App                       │
│                  (Kotlin + Jetpack Compose)                │
├─────────────────────────────────────────────────────────────┤
│ Presentation Layer (MVVM)                                   │
│ ├─ Login Screen ViewModel                                   │
│ ├─ Onboarding 1-5 ViewModels                               │
│ └─ Profile Setup ViewModel                                  │
├─────────────────────────────────────────────────────────────┤
│ Domain Layer (Use Cases)                                    │
│ ├─ KakaoAuthUseCase                                         │
│ ├─ SaveProfileUseCase                                       │
│ ├─ RefreshTokenUseCase                                      │
│ └─ ValidateInputUseCase                                     │
├─────────────────────────────────────────────────────────────┤
│ Data Layer (Repositories + Data Sources)                    │
│ ├─ AuthenticationRepository                                 │
│ │  ├─ RemoteAuthDataSource (Retrofit + REST API)          │
│ │  └─ LocalAuthDataSource (DataStore + encrypted prefs)    │
│ ├─ UserProfileRepository                                    │
│ │  ├─ RemoteProfileDataSource (REST API)                  │
│ │  └─ LocalProfileDataSource (Room DB)                     │
│ └─ SessionRepository                                        │
│    └─ LocalSessionDataSource (DataStore)                    │
├─────────────────────────────────────────────────────────────┤
│ Cross-Cutting Concerns (Hilt Modules)                       │
│ ├─ NetworkModule (Retrofit + OkHttp)                        │
│ ├─ DatabaseModule (Room + DataStore)                        │
│ ├─ RepositoryModule (interface bindings)                    │
│ └─ AnalyticsModule (Firebase Events)                        │
└─────────────────────────────────────────────────────────────┘
              ↓↑ HTTPS (TLS 1.3)
┌─────────────────────────────────────────────────────────────┐
│                  Backend REST API                           │
│         (Node.js + Express + TypeScript)                    │
├─────────────────────────────────────────────────────────────┤
│ Controllers Layer                                           │
│ ├─ AuthController (/api/v1/auth/*)                        │
│ └─ UserController (/api/v1/users/*)                       │
├─────────────────────────────────────────────────────────────┤
│ Middleware                                                  │
│ ├─ JWTVerificationMiddleware                              │
│ ├─ RequestValidationMiddleware                             │
│ ├─ ErrorHandlingMiddleware                                 │
│ └─ LoggingMiddleware                                        │
├─────────────────────────────────────────────────────────────┤
│ Service Layer (Business Logic)                              │
│ ├─ AuthService (token exchange, refresh, rotation)        │
│ ├─ UserService (profile creation, validation)              │
│ └─ KakaoIntegrationService (OAuth flow)                    │
├─────────────────────────────────────────────────────────────┤
│ Data Access Layer (Prisma ORM)                              │
│ ├─ User model (Kakao ID, timestamps)                       │
│ ├─ UserProfile model (name, role, industry, goals)        │
│ └─ TokenStore model (optional, for refresh token tracking) │
├─────────────────────────────────────────────────────────────┤
│ External Integrations                                       │
│ ├─ Firebase Admin SDK (OAuth verification)                 │
│ ├─ Kakao REST API (token exchange)                         │
│ └─ Firebase Analytics (event ingestion)                     │
└─────────────────────────────────────────────────────────────┘
              ↓↑ SQL Connections
┌─────────────────────────────────────────────────────────────┐
│              PostgreSQL Database (AWS RDS)                  │
│ ├─ users table (id, kakao_id, created_at, last_login_at)  │
│ ├─ user_profiles table (user_id FK, name, role, industry) │
│ └─ indices: (kakao_id UNIQUE), (user_id UNIQUE)           │
└─────────────────────────────────────────────────────────────┘
```

### MVVM + Clean Architecture Principles

**MVVM Pattern (Android):**
- **Model:** Data classes representing entities (User, UserProfile, AuthToken)
- **View:** Jetpack Compose UI components (reusable across feature screens)
- **ViewModel:** State management (StateFlow for reactive updates, sealed Result types for async operations)

**Clean Architecture Layers:**
1. **Presentation Layer:** UI screens + ViewModels (framework-dependent)
2. **Domain Layer:** Use cases + entity models (framework-independent business logic)
3. **Data Layer:** Repositories + data sources (abstracts API, database, local storage)

**Key Benefits:**
- Testability: Each layer can be tested independently
- Maintainability: Clear separation of concerns
- Reusability: Use cases can be tested without UI framework
- Scalability: Easy to add new data sources (e.g., cache layer, offline support)

### Data Flow

1. **User Initiation:** Taps "Kakao Login" button
2. **Presentation Layer:** LoginViewModel triggers KakaoAuthUseCase
3. **Domain Layer:** Use case validates inputs, calls repository
4. **Data Layer:** AuthRepository calls RemoteAuthDataSource (Retrofit)
5. **Network:** OkHttp sends request to `/api/v1/auth/kakao`
6. **Backend Processing:** AuthController exchanges Kakao code for tokens
7. **Response:** Returns access_token, refresh_token, user_exists, onboarding_completed
8. **Local Storage:** RemoteAuthDataSource deserializes response, LocalAuthDataSource saves to encrypted DataStore
9. **State Update:** LoginViewModel updates StateFlow
10. **UI Recomposition:** Jetpack Compose observes StateFlow, navigates to next screen

### Dependency Management (Hilt)

```kotlin
// NetworkModule provides Retrofit singleton
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.memoir.app")
        .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json")))
        .build()

    @Singleton
    @Provides
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
}

// RepositoryModule binds interfaces to implementations
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}

// In ViewModel, inject dependencies
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val kakaoAuthUseCase: KakaoAuthUseCase,
    private val analytics: FirebaseAnalytics
) : ViewModel() { ... }
```

---

## Phase-Based Implementation Plan

### Frontend Implementation Roadmap (Weeks 2-10)

#### **Phase 2.1: Project Setup & Architecture Foundation (Weeks 2-3)**

**Goal:** Establish robust Android project structure, dependency injection, and network layer

**Tasks:**
- [x] Set up Android Gradle project with Kotlin DSL
- [x] Configure Jetpack Compose & Material 3 dependencies
- [x] Install Hilt DI framework + dependencies
- [x] Set up Retrofit + OkHttp + serialization
- [x] Configure DataStore for secure preferences
- [x] Configure Room database
- [x] Set up Firebase SDK (Authentication, Analytics, Crashlytics)
- [x] Install Kakao SDK for Android
- [x] Create base MVVM architecture (ViewModel, Repository, DataSource base classes)
- [x] Set up logging + error handling utilities

**Deliverables:**
- `build.gradle.kts` with all dependencies configured
- `Hilt` modules for network, database, repository injection
- Retrofit API interface template
- Base ViewModel + Repository classes
- Error handling utility (sealed Result<T> type)

**Timeline:** 3 days

---

#### **Phase 2.2: Kakao OAuth Integration & Authentication Flow (Weeks 3-4)**

**Goal:** Implement Kakao login, token exchange, and session persistence

**Tasks:**
- [ ] **2.2.1 - Kakao SDK Configuration**
  - [ ] Initialize Kakao SDK in Application class
  - [ ] Configure Kakao App Key in AndroidManifest.xml + strings.xml
  - [ ] Handle Kakao callback (KakaoCallback implementation)
  - [ ] Test Kakao login flow on emulator
  - [ ] Document Kakao setup steps for team

**Acceptance Criteria:**
- Kakao SDK initializes without errors
- Kakao login button successfully initiates OAuth flow
- Callback handler receives auth code

**Test Cases:**
- Unit: KakaoCallbackHandler parses response correctly
- UI: LoginButton onClick triggers Kakao login flow

---

- [ ] **2.2.2 - Backend API Contract Definition**
  - [ ] Design POST `/api/v1/auth/kakao` endpoint
  - [ ] Define request/response schemas (JSON contract)
  - [ ] Document error responses (400, 401, 502)
  - [ ] Finalize with backend team

**Acceptance Criteria:**
- API contract approved by both teams
- Postman collection created for manual testing

---

- [ ] **2.2.3 - AuthApi (Retrofit Interface)**
  - [ ] Create AuthApi interface with @POST endpoints
  - [ ] Define request/response data classes
  - [ ] Add @Serializable annotations for kotlinx.serialization
  - [ ] Create AuthRequest (kakao_oauth_code: String)
  - [ ] Create AuthResponse (access_token, refresh_token, user_exists, onboarding_completed)

```kotlin
interface AuthApi {
    @POST("api/v1/auth/kakao")
    suspend fun exchangeKakaoToken(
        @Body request: AuthRequest
    ): AuthResponse

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshRequest
    ): AuthResponse

    @GET("api/v1/users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") bearerToken: String
    ): UserResponse
}
```

**Acceptance Criteria:**
- Interface compiles without errors
- Data classes serialize/deserialize correctly
- Retrofit mocking works in unit tests

**Test Cases:**
- Unit: Kotlinx serialization for AuthResponse (success + error cases)
- Integration: Mock Retrofit intercepts requests correctly

---

- [ ] **2.2.4 - RemoteAuthDataSource (Network Layer)**
  - [ ] Implement RemoteAuthDataSource interface
  - [ ] Handle API errors (network timeout, malformed response, Kakao errors)
  - [ ] Add retry logic (exponential backoff for transient failures)
  - [ ] Log API calls (strip tokens from logs)
  - [ ] Map backend errors to domain-level exceptions

```kotlin
class RemoteAuthDataSourceImpl(
    private val authApi: AuthApi,
    private val logger: Logger
) : RemoteAuthDataSource {
    override suspend fun exchangeKakaoToken(code: String): Result<AuthTokens> {
        return try {
            val response = authApi.exchangeKakaoToken(AuthRequest(code))
            Result.Success(response.toAuthTokens())
        } catch (e: HttpException) {
            logger.error("Kakao exchange failed: ${e.code()}")
            Result.Error(e.toAuthError())
        } catch (e: IOException) {
            Result.Error(AuthError.NetworkError)
        }
    }
}
```

**Acceptance Criteria:**
- Network calls execute successfully
- Error handling maps to appropriate domain exceptions
- Logs do not contain sensitive data (tokens, codes)

**Test Cases:**
- Unit: Mock AuthApi, verify error mapping for 400/401/502 responses
- Unit: Verify retry logic executes on transient failures
- Unit: Verify token stripping in logs

---

- [ ] **2.2.5 - LocalAuthDataSource (Secure Storage)**
  - [ ] Implement LocalAuthDataSource using DataStore
  - [ ] Store access token + refresh token encrypted
  - [ ] Store onboarding completion flag
  - [ ] Implement token retrieval with expiry check
  - [ ] Implement token clearing (logout)

```kotlin
class LocalAuthDataSourceImpl(
    private val dataStore: DataStore<AuthPreferences>
) : LocalAuthDataSource {
    override suspend fun saveTokens(tokens: AuthTokens) {
        dataStore.updateData { current ->
            current.copy(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
                accessTokenExpiresAt = tokens.accessTokenExpiresAt,
                refreshTokenExpiresAt = tokens.refreshTokenExpiresAt
            )
        }
    }

    override suspend fun getAccessToken(): String? {
        return dataStore.data.firstOrNull()?.accessToken
    }

    override suspend fun isAccessTokenExpired(): Boolean {
        val prefs = dataStore.data.firstOrNull() ?: return true
        return System.currentTimeMillis() > prefs.accessTokenExpiresAt
    }

    override suspend fun clearTokens() {
        dataStore.updateData { current -> current.copy(accessToken = "", refreshToken = "") }
    }
}
```

**Acceptance Criteria:**
- Tokens persist across app kill/restart
- Expired tokens return null
- Clear operation removes all sensitive data

**Test Cases:**
- Unit: DataStore serialization/deserialization of tokens
- Integration: Save token → kill app → restart → verify retrieval

---

- [ ] **2.2.6 - AuthRepository (Data Abstraction)**
  - [ ] Implement AuthRepository interface
  - [ ] Coordinate RemoteAuthDataSource + LocalAuthDataSource
  - [ ] Handle token refresh logic
  - [ ] Implement refresh token rotation (new refresh token replaces old)
  - [ ] Expose StateFlow<AuthState> for UI observation

```kotlin
class AuthRepositoryImpl(
    private val remoteDataSource: RemoteAuthDataSource,
    private val localDataSource: LocalAuthDataSource,
    private val logger: Logger
) : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun exchangeKakaoToken(code: String) {
        _authState.value = AuthState.Loading
        when (val result = remoteDataSource.exchangeKakaoToken(code)) {
            is Result.Success -> {
                localDataSource.saveTokens(result.data)
                _authState.value = AuthState.Authenticated(result.data)
            }
            is Result.Error -> {
                _authState.value = AuthState.Error(result.error)
            }
        }
    }

    override suspend fun refreshTokenIfNeeded() {
        val isExpired = localDataSource.isAccessTokenExpired()
        if (isExpired) {
            val refreshToken = localDataSource.getRefreshToken() ?: return
            when (val result = remoteDataSource.refreshToken(refreshToken)) {
                is Result.Success -> {
                    localDataSource.saveTokens(result.data) // New refresh token replaces old
                    _authState.value = AuthState.Authenticated(result.data)
                }
                is Result.Error -> {
                    localDataSource.clearTokens()
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    override suspend fun logout() {
        localDataSource.clearTokens()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val tokens: AuthTokens) : AuthState()
    data class Error(val error: AuthError) : AuthState()
}
```

**Acceptance Criteria:**
- Token exchange updates local storage + state
- Refresh token rotation tested (old token invalid after exchange)
- Logout clears all sensitive data

**Test Cases:**
- Unit: Mock remote/local sources, verify exchange flow
- Unit: Verify token refresh triggers on app launch if expired
- Unit: Verify logout clears StateFlow + local storage

---

- [ ] **2.2.7 - KakaoAuthUseCase (Domain Logic)**
  - [ ] Implement KakaoAuthUseCase
  - [ ] Handle Kakao SDK callback → auth code capture
  - [ ] Call AuthRepository.exchangeKakaoToken()
  - [ ] Fire analytics event (kakao_login_success, kakao_login_failure)
  - [ ] Return Result<AuthTokens> to ViewModel

```kotlin
class KakaoAuthUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val analytics: FirebaseAnalytics
) {
    suspend operator fun invoke(code: String): Result<AuthTokens> {
        return try {
            authRepository.exchangeKakaoToken(code)
            analytics.logEvent("kakao_login_success")
            Result.Success(Unit)
        } catch (e: Exception) {
            analytics.logEvent("kakao_login_failure", bundleOf("error" to e.message))
            Result.Error(e)
        }
    }
}
```

**Acceptance Criteria:**
- Use case compiles and executes
- Analytics events fire correctly

**Test Cases:**
- Unit: Mock AuthRepository, verify use case invokes exchange
- Unit: Verify analytics event fires on success/failure

---

- [ ] **2.2.8 - LoginViewModel**
  - [ ] Create LoginViewModel with StateFlow<LoginState>
  - [ ] Inject KakaoAuthUseCase + analytics
  - [ ] Implement onKakaoLoginButtonClicked() method
  - [ ] Expose loading, error, success states to UI
  - [ ] Handle navigation to onboarding/main app

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val kakaoAuthUseCase: KakaoAuthUseCase,
    private val analytics: FirebaseAnalytics
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUIState>(LoginUIState.Initial)
    val uiState: StateFlow<LoginUIState> = _uiState.asStateFlow()

    fun onKakaoLoginClicked(authCode: String) {
        viewModelScope.launch {
            _uiState.value = LoginUIState.Loading
            val result = kakaoAuthUseCase(authCode)
            when (result) {
                is Result.Success -> _uiState.value = LoginUIState.Success
                is Result.Error -> _uiState.value = LoginUIState.Error(result.error.message)
            }
        }
    }
}

sealed class LoginUIState {
    object Initial : LoginUIState()
    object Loading : LoginUIState()
    object Success : LoginUIState()
    data class Error(val message: String) : LoginUIState()
}
```

**Acceptance Criteria:**
- ViewModel observes repository state
- UI updates reflect ViewModel state changes
- Navigation triggers on success

**Test Cases:**
- Unit: Mock use case, verify ViewModel state transitions (Initial → Loading → Success)
- Unit: Verify error state on failure

---

**2.2 Subtasks Summary:**
| # | Task | Assignee | Timeline | Dependencies |
|---|------|----------|----------|--------------|
| 2.2.1 | Kakao SDK Config | Frontend Lead | Days 1-2 | None |
| 2.2.2 | API Contract | Both Teams | Day 2 | None |
| 2.2.3 | AuthApi Interface | Frontend Dev | Day 2 | 2.2.2 |
| 2.2.4 | RemoteAuthDataSource | Frontend Dev | Days 3-4 | 2.2.3 |
| 2.2.5 | LocalAuthDataSource | Frontend Dev | Days 3-4 | DataStore setup |
| 2.2.6 | AuthRepository | Frontend Dev | Days 4-5 | 2.2.4, 2.2.5 |
| 2.2.7 | KakaoAuthUseCase | Frontend Dev | Day 5 | 2.2.6 |
| 2.2.8 | LoginViewModel | Frontend Dev | Day 5 | 2.2.7 |

**Acceptance Criteria (Phase 2.2):**
- Kakao OAuth flow from SDK callback → token exchange → local storage complete
- All 8 layers implemented and tested
- No tokens logged in console/Logcat
- Token refresh rotation verified (new refresh replaces old)

---

#### **Phase 2.3: Login Screen UI & Navigation (Weeks 4-5)**

**Goal:** Build polished Kakao login screen matching Memoir design system

**Tasks:**
- [ ] **2.3.1 - Compose UI Components Setup**
  - [ ] Create reusable Compose theme file (Memoir colors, typography)
  - [ ] Define Material 3 custom color scheme (mustard #F4BA54, carrot orange #E86221, dark olive #124234)
  - [ ] Build base button component (KakaoLoginButton with branded styling)
  - [ ] Build ErrorMessage composable
  - [ ] Build LoadingOverlay composable
  - [ ] Test colors + typography on device

**Test Cases:**
- UI: Verify button colors match brand palette
- UI: Verify text size readable at device scale

---

- [ ] **2.3.2 - LoginScreen Composable**
  - [ ] Build LoginScreen composable (scaffold, column layout)
  - [ ] Add app logo + branding text
  - [ ] Add "Kakao login" button with proper styling
  - [ ] Display error message if login fails
  - [ ] Show loading overlay during token exchange
  - [ ] Add Terms + Privacy Policy checkboxes (required before login enabled)
  - [ ] Responsive layout for multiple screen sizes (320dp-480dp width)

```kotlin
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Image(
            painter = painterResource(R.drawable.ic_memoir_logo),
            contentDescription = "Memoir Logo",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "회고와 성장",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "다양한 분야의 성장하는 사람들과 함께",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Terms checkboxes
        var termsAccepted by remember { mutableStateOf(false) }
        var privacyAccepted by remember { mutableStateOf(false) }

        Row(modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it }
            )
            Text("이용약관 동의", modifier = Modifier.clickable { termsAccepted = !termsAccepted })
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = privacyAccepted,
                onCheckedChange = { privacyAccepted = it }
            )
            Text("개인정보처리방침 동의", modifier = Modifier.clickable { privacyAccepted = !privacyAccepted })
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Kakao login button
        KakaoLoginButton(
            enabled = termsAccepted && privacyAccepted && uiState !is LoginUIState.Loading,
            onClick = { viewModel.onKakaoLoginClicked() }
        )

        // Error message
        when (val state = uiState) {
            is LoginUIState.Error -> {
                Spacer(modifier = Modifier.height(16.dp))
                ErrorMessage(text = state.message)
            }
            LoginUIState.Loading -> LoadingOverlay()
            else -> {}
        }
    }
}
```

**Acceptance Criteria:**
- Login button styled with Kakao brand
- Terms checkboxes required before login enabled
- Error message displays on failure
- Loading state prevents duplicate submissions
- Responsive on 320dp, 360dp, 480dp widths

**Test Cases:**
- UI: LoginButton disabled until terms accepted
- UI: Error message visible on auth failure
- UI: Loading overlay appears during exchange

---

- [ ] **2.3.3 - Navigation Setup (Jetpack Navigation)**
  - [ ] Create navigation graph (login_graph.xml or Compose nav)
  - [ ] Define navigation destinations (LoginScreen, OnboardingScreen, MainAppScreen)
  - [ ] Set up NavHost in MainActivity
  - [ ] Handle deep linking (if launched via Firebase deeplink)
  - [ ] Implement back stack management (prevent back to login after success)

```kotlin
sealed class NavigationEvent {
    object NavigateToOnboarding : NavigationEvent()
    object NavigateToMainApp : NavigationEvent()
    object NavigateToLogin : NavigationEvent()
}

@Composable
fun MemoirNavHost(
    navController: NavHostController,
    startDestination: String = "login"
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("onboarding") }
            )
        }
        composable("onboarding") {
            OnboardingScreen(
                onCompleted = { navController.navigate("main") { popUpTo("login") } }
            )
        }
        composable("main") {
            MainAppScreen()
        }
    }
}
```

**Acceptance Criteria:**
- Navigation graph compiles
- Back button prevented from LoginScreen to previous state
- Deep linking works if applicable

---

- [ ] **2.3.4 - Kakao Button Integration**
  - [ ] Add KakaoTalkLoginButton or Kakao Web SDK callback handler
  - [ ] Parse callback auth code
  - [ ] Pass code to ViewModel.onKakaoLoginClicked(code)
  - [ ] Handle OAuth redirect URI properly

**Acceptance Criteria:**
- Button click initiates Kakao OAuth flow
- Callback handler receives auth code
- Code passed to ViewModel

---

**2.3 Subtasks Summary:**
| # | Task | Timeline | Dependencies |
|---|------|----------|--------------|
| 2.3.1 | Compose Components | Days 1-2 | Phase 2.2 |
| 2.3.2 | LoginScreen Composable | Days 2-3 | 2.3.1 |
| 2.3.3 | Navigation Setup | Days 3-4 | 2.3.2 |
| 2.3.4 | Kakao Button Integration | Days 4-5 | 2.3.2 |

---

#### **Phase 2.4: Onboarding Flow (5 Screens) (Weeks 5-6)**

**Goal:** Build mandatory 5-screen onboarding with progressive disclosure

**Screen Breakdown:**

**Screen 1: Problem Framing**
- Title: "일상의 배움이 휘발되고 있지 않나요?" (Aren't your daily learnings evaporating?)
- Body: Explain the core problem (insights fade, no structure, solo effort)
- Visual: Illustration or icon of fading memory
- Button: "다음" (Next)
- No back button (forced progression)

**Screen 2: Community Value**
- Title: "다양한 분야의 성장하는 사람들과 함께" (Together with growth-minded people from diverse fields)
- Body: Introduce cohort concept, peer learning, meaningful connections
- Visual: Avatar grid showing diverse professionals
- Button: "다음" (Next)

**Screen 3: Structure Explanation**
- Title: "10주 동안 매주 성장을 기록하세요" (Record your growth every week for 10 weeks)
- Body: Explain weekly reflections, peer feedback requirements, meetups
- Visual: Timeline showing 10-week cycle
- Button: "다음" (Next)

**Screen 4: Accountability Mechanism (Deposit System)**
- Title: "보증금 시스템으로 책임감 있게" (With commitment through the deposit system)
- Body: **Transparent breakdown** (in table format):
  - Initial: 20만원 보증금 + 10만원 첫 달 비용
  - Monthly: 10만원 (10주 주기)
  - Refund: 성실히 참여하시면 보증금 전액 환급 (Full refund for sincere participation)
  - Deduction: 회고 미기록 시 2만원, 댓글 부족 시 1만원 (penalties)
- Legal: Link to full Deposit Policy (required tap before continuing)
- Social Proof: "1기 참여자의 65%가 전액 환급받았어요" (65% of Cohort 1 received full refund)
- Button: "다음" (Next)

**Screen 5: Profile Setup**
- Collect: Name (required), Role (required), Industry (required dropdown), Growth Goals (required text area)
- Optional: Photo (deferred to Phase 2)
- Button: "완료" (Complete)

---

**Tasks:**

- [ ] **2.4.1 - Onboarding Screen Template**
  - [ ] Create OnboardingScreenTemplate composable (reusable for screens 1-4)
  - [ ] Implement top progress indicator (4 dots for screens 1-4)
  - [ ] Build navigation (no back button, disabled next until validation passes)
  - [ ] Smooth transitions between screens (300ms animation)

```kotlin
@Composable
fun OnboardingScreenTemplate(
    screenNumber: Int,
    totalScreens: Int,
    title: String,
    body: String,
    buttonText: String = "다음",
    onNext: () -> Unit,
    onBack: () -> Unit = {},
    showBackButton: Boolean = false,
    content: @Composable () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Progress indicator (dots)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(totalScreens) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (index < screenNumber) Color.Orange else Color.LightGray,
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Body
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Custom content
            content()
        }

        // Button
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(buttonText)
        }
    }
}
```

**Acceptance Criteria:**
- Progress indicator updates for each screen
- Back button hidden (no navigation backward)
- Smooth transitions between screens

---

- [ ] **2.4.2 - Screen 1 (Problem Framing)**
  - [ ] Build Screen1 composable using template
  - [ ] Add illustrative image/icon
  - [ ] Fire analytics event: `onboarding_screen_viewed` with screen_index=1

```kotlin
@Composable
fun OnboardingScreen1(onNext: () -> Unit) {
    OnboardingScreenTemplate(
        screenNumber = 1,
        totalScreens = 4,
        title = "일상의 배움이 휘발되고 있지 않나요?",
        body = "매일의 경험과 인사이트는 기록하지 않으면 며칠 안에 잊혀집니다. " +
               "Memoir는 주간 회고를 통해 일상을 의미 있는 배움으로 변환합니다.",
        onNext = onNext
    ) {
        // Image illustration
        Image(
            painter = painterResource(R.drawable.illustration_fading_memory),
            contentDescription = "Fading memory",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Fit
        )
    }
}
```

**Acceptance Criteria:**
- Screen renders without errors
- Analytics event fires on view

---

- [ ] **2.4.3 - Screen 2 (Community Value)**
  - [ ] Build Screen2 with diverse avatar grid
  - [ ] Show 6-8 avatar placeholders (representing different industries)
  - [ ] Fire analytics event: `onboarding_screen_viewed` with screen_index=2

**Acceptance Criteria:**
- Avatar grid displays
- Analytics event fires

---

- [ ] **2.4.4 - Screen 3 (Structure Explanation)**
  - [ ] Build Screen3 with timeline visual
  - [ ] Show 10 week boxes in grid (e.g., 2x5 or 3x3+1)
  - [ ] Highlight key milestones (weeks 1, 5, 10)
  - [ ] Fire analytics event: `onboarding_screen_viewed` with screen_index=3

**Acceptance Criteria:**
- Timeline renders
- Analytics event fires

---

- [ ] **2.4.5 - Screen 4 (Deposit System Explanation)**
  - [ ] Build Screen4 with deposit breakdown table
  - [ ] Display 4 rows: Initial (20만원 + 10만원), Monthly (10만원), Refund (전액 환급), Penalties (2만원/1만원)
  - [ ] Add "Terms of Service" + "Privacy Policy" + "Deposit Policy" links
  - [ ] Require tap on "Deposit Policy" before continuing
  - [ ] Display social proof: "65% 환급" message
  - [ ] Fire analytics events: `onboarding_screen_viewed` + `terms_accepted`

```kotlin
@Composable
fun OnboardingScreen4(
    onNext: () -> Unit
) {
    var tosAccepted by remember { mutableStateOf(false) }
    var privacyAccepted by remember { mutableStateOf(false) }
    var depositPolicyAccepted by remember { mutableStateOf(false) }

    OnboardingScreenTemplate(
        screenNumber = 4,
        totalScreens = 4,
        title = "보증금 시스템으로 책임감 있게",
        body = "Memoir는 보증금 시스템을 통해 참여자의 책임감을 높입니다.",
        buttonText = "다음",
        onNext = onNext,
        content = {
            // Deposit breakdown table
            DepositBreakdownTable()

            Spacer(modifier = Modifier.height(16.dp))

            // Social proof
            Text(
                text = "1기 참여자의 65%가 전액 환급받았어요",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp),
                color = Color.Green
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Legal links
            Row(modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = tosAccepted, onCheckedChange = { tosAccepted = it })
                Text("이용약관")
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(onClick = { openURL("...") }) {
                    Text("보기")
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = depositPolicyAccepted, onCheckedChange = { depositPolicyAccepted = it })
                Text("보증금정책")
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(onClick = { openURL("...") }) {
                    Text("보기")
                }
            }
        }
    )
}
```

**Acceptance Criteria:**
- Deposit table displays correctly
- Legal links clickable (open WebView or external browser)
- Next button disabled until all checkboxes checked
- Analytics events fire

---

- [ ] **2.4.6 - Screen 5 (Profile Setup Form)**
  - [ ] Build profile form with 4 required fields
  - [ ] Field 1: Name (Korean Hangul validation, 2-4 characters)
  - [ ] Field 2: Role (free text, Korean + English allowed, 2-48 chars)
  - [ ] Field 3: Industry (dropdown selector with 8 options in Korean)
  - [ ] Field 4: Growth Goals (multi-line text, 100-500 characters)
  - [ ] Show validation errors inline (below each field)
  - [ ] Disable "완료" button until all fields valid
  - [ ] Fire analytics events: `profile_submission_started` + `profile_submission_success`/`_failure`

```kotlin
@Composable
fun OnboardingScreen5(
    viewModel: ProfileSetupViewModel = hiltViewModel(),
    onCompleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val form by viewModel.form.collectAsState()

    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf<IndustryCode?>(null) }
    var growthGoals by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var roleError by remember { mutableStateOf<String?>(null) }
    var industryError by remember { mutableStateOf<String?>(null) }
    var goalsError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("프로필 설정", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(24.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = if (isValidKoreanName(it)) null else "2-4자 한글만 입력 가능합니다"
                },
                label = { Text("이름") },
                isError = nameError != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (nameError != null) {
                Text(nameError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Role field
            OutlinedTextField(
                value = role,
                onValueChange = {
                    role = it
                    roleError = if (it.length in 2..48) null else "2-48자를 입력해주세요"
                },
                label = { Text("직급/역할") },
                isError = roleError != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (roleError != null) {
                Text(roleError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Industry dropdown
            IndustryDropdown(
                selected = industry,
                onSelected = { industry = it; industryError = null },
                isError = industryError != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (industryError != null) {
                Text(industryError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Growth goals
            OutlinedTextField(
                value = growthGoals,
                onValueChange = {
                    growthGoals = it
                    goalsError = when {
                        it.length < 100 -> "최소 100자 이상 입력해주세요"
                        it.length > 500 -> "최대 500자 이하로 입력해주세요"
                        else -> null
                    }
                },
                label = { Text("성장 목표 (자유로운 형식)") },
                isError = goalsError != null,
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            if (goalsError != null) {
                Text(goalsError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }
        }

        // Submit button
        Button(
            onClick = {
                viewModel.submitProfile(name, role, industry!!, growthGoals)
                onCompleted()
            },
            enabled = nameError == null && roleError == null && industryError == null && goalsError == null &&
                    name.isNotEmpty() && role.isNotEmpty() && industry != null && growthGoals.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(if (uiState is ProfileSetupUIState.Loading) "전송 중..." else "완료")
        }
    }
}
```

**Acceptance Criteria:**
- All 4 fields display with labels
- Validation errors appear inline
- Submit button disabled until all valid
- API call triggers on submit
- Analytics events fire (started/success/failure)

**Test Cases:**
- Unit: NameValidator rejects non-Hangul (e.g., "ABC"), accepts "김철수"
- Unit: RoleValidator accepts 2-48 chars
- Unit: GrowthGoalsValidator rejects <100 chars
- Unit: IndustryValidator accepts enum values only
- UI: Submit button disabled with empty fields
- UI: Submit button enabled when all fields valid

---

**2.4 Subtasks Summary:**
| # | Task | Timeline | Dependencies |
|---|------|----------|--------------|
| 2.4.1 | Template | Day 1 | 2.3 |
| 2.4.2 | Screen 1 | Day 1 | 2.4.1 |
| 2.4.3 | Screen 2 | Day 2 | 2.4.1 |
| 2.4.4 | Screen 3 | Day 2 | 2.4.1 |
| 2.4.5 | Screen 4 | Days 3-4 | 2.4.1 |
| 2.4.6 | Screen 5 | Days 4-5 | 2.4.1 |

**Acceptance Criteria (Phase 2.4):**
- All 5 screens render without crashes
- Progressive disclosure enforced (no back button)
- Profile form validates all fields
- Analytics events fire for all screens + form submission
- Smooth transitions between screens

---

#### **Phase 2.5: Backend API Integration (Weeks 5-6)**

**Goal:** Connect frontend to backend API endpoints

**Tasks:**

- [ ] **2.5.1 - API Request/Response Handling**
  - [ ] Retrofit deserialization of profile endpoint response
  - [ ] Error handling for 400 (validation), 401 (unauthorized), 409 (profile already set)
  - [ ] Retry logic for network timeouts
  - [ ] Logging of requests (no PII/tokens)

**Acceptance Criteria:**
- Profile submission API call succeeds
- Error responses mapped to user-friendly messages

---

- [ ] **2.5.2 - ProfileRepository + DataSource**
  - [ ] Create ProfileRepository interface
  - [ ] Implement RemoteProfileDataSource (Retrofit API calls)
  - [ ] Implement LocalProfileDataSource (Room database persistence)
  - [ ] Sync API response to Room database

**Test Cases:**
- Unit: Mock Retrofit, verify profile POST call
- Integration: Save response to Room, retrieve from Room

---

- [ ] **2.5.3 - ProfileSetupViewModel**
  - [ ] Create ProfileSetupViewModel with form state
  - [ ] Inject ProfileRepository + SaveProfileUseCase
  - [ ] Implement submitProfile() method
  - [ ] Expose ProfileSetupUIState (Initial, Loading, Success, Error)

**Test Cases:**
- Unit: Mock repository, verify form submission flow
- Unit: Verify error state on API failure

---

- [ ] **2.5.4 - Kakao Callback URI Handling**
  - [ ] Configure AndroidManifest.xml intent-filter for Kakao redirect URI
  - [ ] Parse OAuth code from redirect
  - [ ] Call LoginViewModel.onKakaoLoginClicked(code)

**Acceptance Criteria:**
- Kakao redirect handled correctly
- Code extracted and passed to ViewModel

---

- [ ] **2.5.5 - End-to-End Flow Testing**
  - [ ] Test full flow: Login → Onboarding 1-5 → Profile Submit → Success
  - [ ] Test network failure scenarios (timeout, 5xx error)
  - [ ] Verify data persistence (Room + DataStore)
  - [ ] Verify analytics events fire at each step

**Test Cases:**
- Integration: Mock API endpoints, simulate full user journey
- Integration: Network timeout → verify retry logic
- Integration: Profile saved to Room → verify retrieval

---

**2.5 Subtasks Summary:**
| # | Task | Timeline |
|---|------|----------|
| 2.5.1 | Response Handling | Days 1-2 |
| 2.5.2 | ProfileRepository | Days 2-3 |
| 2.5.3 | ProfileSetupViewModel | Day 3 |
| 2.5.4 | Kakao Callback | Day 2 |
| 2.5.5 | E2E Testing | Days 4-5 |

---

#### **Phase 2.6: Error Handling & Edge Cases (Week 6)**

**Goal:** Robust handling of network failures, validation errors, token expiration

**Tasks:**

- [ ] **2.6.1 - Network Error Handling**
  - [ ] Timeout errors → "네트워크 연결을 확인해주세요" (Check network)
  - [ ] 5xx errors → "서버 오류입니다. 잠시 후 다시 시도해주세요" (Server error, try again)
  - [ ] Kakao API errors → "카카오 로그인에 실패했습니다" (Kakao login failed)
  - [ ] Exponential backoff retry (1s, 2s, 4s max)
  - [ ] Max 2-3 retries before giving up

**Test Cases:**
- Unit: Mock timeout exception, verify retry + error message
- Unit: Mock 5xx response, verify error message

---

- [ ] **2.6.2 - Form Validation Edge Cases**
  - [ ] Name: Reject "김", accept "김철수" (2-4 chars)
  - [ ] Name: Reject "Kim123" (non-Hangul)
  - [ ] Role: Accept "Product Manager" (English), "프로덕트 매니저" (Korean)
  - [ ] Role: Reject empty or <2 chars
  - [ ] Goals: Reject <100 chars with inline error
  - [ ] Goals: Show character count (99/500, 100/500 ✓)
  - [ ] Industry: Prevent submit without selection

**Test Cases:**
- Unit: NameValidator unit tests (8-10 cases: valid, too short, too long, non-Hangul, English, numbers)
- Unit: RoleValidator (valid, empty, too short, too long)
- Unit: GrowthGoalsValidator (too short, at boundary, too long)
- UI: Character counter updates as user types

---

- [ ] **2.6.3 - Token Refresh on App Relaunch**
  - [ ] On app launch, check if access token expired
  - [ ] If expired, call refresh endpoint with refresh token
  - [ ] If refresh succeeds, continue to main app
  - [ ] If refresh fails, force login again
  - [ ] Automatic background refresh (don't block UI)

```kotlin
class InitialLaunchViewModel @Inject constructor(
    private val refreshTokenUseCase: RefreshTokenUseCase
) : ViewModel() {
    init {
        viewModelScope.launch {
            val isAuthenticated = refreshTokenUseCase.refreshIfNeeded()
            if (isAuthenticated) {
                // Navigate to onboarding check or main app
            } else {
                // Navigate to login
            }
        }
    }
}
```

**Test Cases:**
- Integration: Save expired token → kill app → relaunch → verify refresh called
- Integration: Refresh fails → verify forced redirect to login

---

- [ ] **2.6.4 - Duplicate Profile Submission Prevention**
  - [ ] Button disabled during submission (shows "전송 중...")
  - [ ] ViewModel prevents concurrent submissions
  - [ ] API returns 409 if profile already set → clear error message

**Test Cases:**
- UI: Button disabled after first click
- Integration: Rapid double-click prevented by ViewModel state

---

- [ ] **2.6.5 - Session Timeout & Re-Authentication**
  - [ ] If token expired during app use, show toast: "세션이 만료되었습니다. 다시 로그인해주세요" (Session expired)
  - [ ] Redirect to login
  - [ ] Preserve navigation state if possible (resume after re-auth)

---

**2.6 Subtasks Summary:**
| # | Task | Timeline |
|---|------|----------|
| 2.6.1 | Network Errors | Days 1-2 |
| 2.6.2 | Form Validation | Days 2-3 |
| 2.6.3 | Token Refresh | Days 3-4 |
| 2.6.4 | Duplicate Prevention | Day 4 |
| 2.6.5 | Session Timeout | Day 5 |

---

#### **Phase 2.7: Analytics & Monitoring (Week 7)**

**Goal:** Instrument all critical user flows for product insights

**Tasks:**

- [ ] **2.7.1 - Firebase Analytics Events**
  - [ ] `kakao_login_initiated` - user taps login button
  - [ ] `kakao_login_success` - OAuth code exchanged
  - [ ] `kakao_login_failure` - error details (error code, message)
  - [ ] `onboarding_started` - first screen shown
  - [ ] `onboarding_screen_viewed` - each screen (screen_index: 1-4)
  - [ ] `onboarding_abandoned` - app killed during onboarding (last_screen_index)
  - [ ] `onboarding_completed` - all screens viewed + profile submitted
  - [ ] `profile_submission_started` - form submitted
  - [ ] `profile_submission_success` - API response 201
  - [ ] `profile_submission_failure` - API error (error_code, message)
  - [ ] `terms_accepted` - checkboxes ticked

```kotlin
private fun logKakaoLoginSuccess(isNewUser: Boolean) {
    analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
        param("method", "kakao")
        param("user_new", isNewUser.toString())
    }
}

private fun logProfileSubmissionSuccess(industryCode: String) {
    analytics.logEvent("profile_submission_success") {
        param("industry_code", industryCode)
        param("goals_length", growthGoals.length.toString())
    }
}
```

**Acceptance Criteria:**
- All events fire at appropriate times
- Event properties logged correctly (no PII)

**Test Cases:**
- Integration: Mock analytics, verify event fires on user action

---

- [ ] **2.7.2 - Crashlytics Instrumentation**
  - [ ] Enable automatic crash reporting
  - [ ] Log non-fatal errors (API failures, validation errors)
  - [ ] Tag errors with user_id (non-PII context)
  - [ ] Monitor crash-free percentage (target >98%)

**Test Cases:**
- Integration: Throw exception, verify Crashlytics reports it

---

- [ ] **2.7.3 - Performance Monitoring**
  - [ ] Track Kakao login latency (Firebase Performance Monitoring)
  - [ ] Track profile submission API call duration
  - [ ] Track onboarding screen render time
  - [ ] Alert if latency exceeds thresholds (>3s login, >2s API)

---

- [ ] **2.7.4 - Remote Config (Feature Flags)**
  - [ ] Set up Firebase Remote Config
  - [ ] Add flag for onboarding screens (can hide screens for testing)
  - [ ] Add flag for API endpoints (test vs production)
  - [ ] Minimal flags initially (only if needed for gating)

---

**2.7 Subtasks Summary:**
| # | Task | Timeline |
|---|------|----------|
| 2.7.1 | Analytics Events | Days 1-3 |
| 2.7.2 | Crashlytics | Days 3-4 |
| 2.7.3 | Performance | Day 4 |
| 2.7.4 | Remote Config | Day 5 |

---

#### **Phase 2.8: Frontend Testing & QA (Weeks 7-8)**

**Goal:** Comprehensive testing of Android app (unit, UI, integration)

**Tasks:**

- [ ] **2.8.1 - Unit Tests (Business Logic)**
  - [ ] **InputValidation tests** (2-3 test files):
    - `KoreanNameValidatorTest` (valid names, invalid names, edge cases)
    - `RoleValidatorTest` (valid roles, empty, too long)
    - `GrowthGoalsValidatorTest` (length validation)
    - `IndustryValidatorTest` (enum validation)
  - [ ] **Repository tests** (2 test files):
    - `AuthRepositoryTest` (token exchange, token refresh, logout)
    - `ProfileRepositoryTest` (profile submission, local persistence)
  - [ ] **ViewModel tests** (2 test files):
    - `LoginViewModelTest` (success, failure, loading states)
    - `ProfileSetupViewModelTest` (form state, submission, errors)
  - [ ] **UseCase tests** (1-2 test files):
    - `KakaoAuthUseCaseTest` (success, failure cases)
    - `RefreshTokenUseCaseTest` (token rotation, expiry)

**Test Coverage Target:** 30-40% (focus on critical business logic, skip UI state transitions)

**Expected Test Count:** 25-35 tests across 7-8 test files

```kotlin
// Example: KoreanNameValidatorTest
class KoreanNameValidatorTest {
    private val validator = KoreanNameValidator()

    @Test
    fun validKoreanName_returns_true() {
        assertTrue(validator.isValid("김철수"))  // 3 chars
        assertTrue(validator.isValid("이순신"))  // 3 chars
        assertTrue(validator.isValid("김A"))      // Reject: mixed Hangul + English
    }

    @Test
    fun invalidKoreanName_returns_false() {
        assertFalse(validator.isValid("Kim"))     // English only
        assertFalse(validator.isValid("김"))      // 1 char (too short)
        assertFalse(validator.isValid("김철수박"))  // 4 chars (acceptable) - NO this is good
        assertFalse(validator.isValid("김철수박김")) // 5 chars (too long)
    }

    @Test
    fun emptyName_returns_false() {
        assertFalse(validator.isValid(""))
    }
}
```

**Acceptance Criteria:**
- All unit tests pass
- Target 30-40% coverage for critical paths
- Code coverage report generated

**Test Cases (Summary):**
- Unit: 25-35 tests across validators, repositories, viewmodels, usecases
- Focus on happy path + error scenarios
- Skip edge cases not listed above

---

- [ ] **2.8.2 - UI Tests (Compose)**
  - [ ] **LoginScreen tests:**
    - Button disabled until terms accepted
    - Error message displays on auth failure
    - Loading overlay shows during exchange
  - [ ] **OnboardingScreen tests:**
    - Screen 1 renders title + body + next button
    - Next button navigates to Screen 2
    - Back navigation disabled
    - Progress indicator updates (dots)
  - [ ] **ProfileSetupScreen tests:**
    - Form fields render
    - Submit button disabled until all fields valid
    - Name field rejects non-Hangul (real-time error)
    - Goals character count displays

**Expected Test Count:** 8-12 UI tests

```kotlin
class LoginScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginButton_disabled_until_terms_accepted() {
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {}, onNavigateToOnboarding = {})
        }

        composeTestRule.onNodeWithText("카카오로 로그인").assertIsNotEnabled()

        composeTestRule.onNodeWithText("이용약관 동의").performClick()
        composeTestRule.onNodeWithText("개인정보처리방침 동의").performClick()

        composeTestRule.onNodeWithText("카카오로 로그인").assertIsEnabled()
    }
}
```

**Acceptance Criteria:**
- UI tests compile and run
- Critical UI behaviors tested (button states, navigation, error display)

---

- [ ] **2.8.3 - Integration Tests**
  - [ ] Full flow: Login → Onboarding → Profile Submit → Success
  - [ ] Mock Retrofit endpoints, verify API calls
  - [ ] Mock DataStore, verify token persistence
  - [ ] Mock Room, verify profile saved to DB
  - [ ] Network failure → retry logic
  - [ ] Token refresh flow

**Expected Test Count:** 5-8 integration tests

```kotlin
class AuthIntegrationTest {
    @Test
    fun fullLoginFlow_succeeds() = runTest {
        // Given
        val mockAuthApi = mock<AuthApi>()
        val mockDataStore = mock<DataStore<AuthPreferences>>()
        val authRepository = AuthRepositoryImpl(
            RemoteAuthDataSourceImpl(mockAuthApi),
            LocalAuthDataSourceImpl(mockDataStore)
        )

        // When
        authRepository.exchangeKakaoToken("valid_code")

        // Then
        verify(mockAuthApi).exchangeKakaoToken(any())
        verify(mockDataStore).updateData(any())
    }
}
```

**Acceptance Criteria:**
- Integration tests pass
- End-to-end user flow verified

---

- [ ] **2.8.4 - Manual Testing Checklist**
  - [ ] Install APK on real device (Android 8+)
  - [ ] Test Kakao login with real Kakao account
  - [ ] Test onboarding flow (5 screens, no back navigation)
  - [ ] Test profile form validation (real-time errors)
  - [ ] Test network disconnect scenario (show error, allow retry)
  - [ ] Kill app during onboarding → relaunch → check persistence
  - [ ] Verify analytics events in Firebase Console
  - [ ] Check Crashlytics console (no crashes)
  - [ ] Verify terms/privacy links open correctly
  - [ ] Verify deposit policy table displays correctly (landscape/portrait)

**Acceptance Criteria:**
- No crashes on real device
- Kakao login works
- All flows complete successfully

---

**2.8 Subtasks Summary:**
| # | Task | Tests Expected | Timeline |
|---|------|---|----------|
| 2.8.1 | Unit Tests | 25-35 | Days 1-3 |
| 2.8.2 | UI Tests | 8-12 | Days 3-4 |
| 2.8.3 | Integration Tests | 5-8 | Days 4-5 |
| 2.8.4 | Manual Testing | - | Days 5-7 |

**Total Frontend Test Count:** ~40-55 tests across all phases

---

### Backend Implementation Roadmap (Weeks 2-7)

#### **Phase 3.1: Project Setup & Database Schema (Weeks 2-3)**

**Goal:** Establish backend project structure and database models

**Tasks:**

- [ ] **3.1.1 - Express.js Project Setup**
  - [ ] Initialize Node.js project with package.json
  - [ ] Install dependencies: Express, TypeScript, Prisma, dotenv, cors, helmet
  - [ ] Set up TypeScript compiler config (tsconfig.json)
  - [ ] Configure environment variables (.env.example + .env)
  - [ ] Set up project structure:
    ```
    src/
    ├─ middleware/         (JWT, error handling, logging)
    ├─ controllers/        (route handlers)
    ├─ services/          (business logic)
    ├─ models/            (data models, types)
    ├─ utils/             (helpers, validators)
    ├─ config/            (config files)
    └─ index.ts           (app entry)
    ```

**Acceptance Criteria:**
- Server starts without errors (`npm run dev`)
- TypeScript compiles
- Environment variables loaded

---

- [ ] **3.1.2 - PostgreSQL Database Setup**
  - [ ] Create PostgreSQL database (local development + test + staging)
  - [ ] Install Prisma CLI
  - [ ] Create Prisma schema file (prisma/schema.prisma)
  - [ ] Define User + UserProfile models (below)

**Acceptance Criteria:**
- Database created
- Prisma schema valid

---

- [ ] **3.1.3 - Prisma Schema Definition**
  - [ ] **User Model:**
    ```prisma
    model User {
      id                    String    @id @default(cuid())
      kakaoId              String    @unique
      createdAt            DateTime  @default(now())
      updatedAt            DateTime  @updatedAt
      lastLoginAt          DateTime?
      status               String    @default("active")  // active, inactive, locked

      profile              UserProfile?
      @@index([kakaoId])
    }
    ```
  - [ ] **UserProfile Model:**
    ```prisma
    model UserProfile {
      id                    String    @id @default(cuid())
      userId               String    @unique
      name                 String    @db.VarChar(4)
      role                 String    @db.VarChar(64)
      industryCode         String    // enum: STARTUP, ENTERPRISE, SME, PUBLIC, FOREIGN, FREELANCER, NONPROFIT, OTHER
      growthGoals          String    @db.Text
      photoUrl             String?
      onboardingCompletedAt DateTime?
      locale               String    @default("ko")
      profileLocked        Boolean   @default(true)
      createdAt            DateTime  @default(now())
      updatedAt            DateTime  @updatedAt

      user                 User      @relation(fields: [userId], references: [id], onDelete: Cascade)
      @@index([userId])
    }
    ```

**Acceptance Criteria:**
- Schema defines all required fields
- Relationships correct
- Indexes on foreign keys

---

- [ ] **3.1.4 - Database Migration**
  - [ ] Run Prisma migration: `npx prisma migrate dev --name initial`
  - [ ] Verify tables created in database
  - [ ] Set up test database (separate schema)

**Acceptance Criteria:**
- Tables created
- Migrations recorded in _prisma_migrations

---

- [ ] **3.1.5 - TypeScript Types & DTOs**
  - [ ] Define types for User, UserProfile, AuthTokens
  - [ ] Create request/response DTOs:
    ```typescript
    // Requests
    export interface KakaoTokenExchangeRequest {
      kakao_oauth_code: string;
    }

    export interface CreateProfileRequest {
      name: string;
      role: string;
      industry_code: IndustryCode;
      growth_goals: string;
    }

    export interface RefreshTokenRequest {
      refresh_token: string;
    }

    // Responses
    export interface AuthResponse {
      access_token: string;
      refresh_token: string;
      user_exists: boolean;
      onboarding_completed: boolean;
    }

    export interface ProfileResponse {
      user_id: string;
      profile: {
        name: string;
        role: string;
        industry_code: string;
        growth_goals: string;
      };
      onboarding_completed_at: string;
    }

    // Errors
    export interface ErrorResponse {
      error: {
        code: string;
        message: string;
        details?: any;
      };
    }
    ```

**Acceptance Criteria:**
- Types compile without errors
- DTOs match API contract

---

**3.1 Subtasks Summary:**
| # | Task | Timeline | Dependencies |
|---|------|----------|--------------|
| 3.1.1 | Express Setup | Days 1-2 | None |
| 3.1.2 | DB Setup | Days 1-2 | None |
| 3.1.3 | Prisma Schema | Days 2-3 | 3.1.2 |
| 3.1.4 | Migration | Day 3 | 3.1.3 |
| 3.1.5 | TypeScript Types | Day 3 | 3.1.1 |

---

#### **Phase 3.2: Kakao OAuth Integration & Token Exchange (Weeks 3-4)**

**Goal:** Implement Kakao OAuth verification and JWT token management

**Tasks:**

- [ ] **3.2.1 - Kakao API Configuration**
  - [ ] Obtain Kakao App Key + App Secret from Kakao Developers
  - [ ] Configure OAuth redirect URI
  - [ ] Store secrets in environment variables

**Acceptance Criteria:**
- Kakao credentials available

---

- [ ] **3.2.2 - Kakao Integration Service**
  - [ ] Create KakaoIntegrationService
  - [ ] Implement token exchange: POST request to Kakao REST API
  - [ ] Verify OAuth code → receive Kakao access token
  - [ ] Extract user info (kakao_id, email, nickname)
  - [ ] Handle Kakao API errors (invalid code, rate limit, timeout)

```typescript
class KakaoIntegrationService {
  async exchangeOAuthCode(code: string): Promise<KakaoTokenResponse> {
    const response = await fetch('https://kauth.kakao.com/oauth/token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        client_id: process.env.KAKAO_APP_KEY,
        redirect_uri: process.env.KAKAO_REDIRECT_URI,
        code,
      }),
    });

    if (!response.ok) {
      throw new KakaoAPIError(`Kakao exchange failed: ${response.status}`);
    }

    return response.json();
  }

  async getUserInfo(kakaoAccessToken: string): Promise<KakaoUser> {
    const response = await fetch('https://kapi.kakao.com/v2/user/me', {
      headers: { 'Authorization': `Bearer ${kakaoAccessToken}` },
    });

    if (!response.ok) {
      throw new KakaoAPIError(`Failed to fetch user info: ${response.status}`);
    }

    return response.json();
  }
}
```

**Acceptance Criteria:**
- Kakao API calls work
- Error handling for invalid codes

**Test Cases:**
- Unit: Mock Kakao API, verify exchange flow
- Unit: Mock 401 response, verify error thrown

---

- [ ] **3.2.3 - JWT Token Generation & Refresh**
  - [ ] Install jsonwebtoken library
  - [ ] Create JWTService
  - [ ] Generate access token (30-day expiry, RS256 signature)
  - [ ] Generate refresh token (90-day expiry)
  - [ ] Implement token verification (middleware)
  - [ ] Implement token refresh (new access + new refresh with rotation)

```typescript
class JWTService {
  private readonly accessTokenSecret = process.env.JWT_SECRET;
  private readonly refreshTokenSecret = process.env.JWT_REFRESH_SECRET;

  generateAccessToken(userId: string, expiresIn = '30d'): string {
    return jwt.sign({ sub: userId }, this.accessTokenSecret, {
      expiresIn,
      algorithm: 'HS256',
    });
  }

  generateRefreshToken(userId: string, expiresIn = '90d'): string {
    return jwt.sign({ sub: userId }, this.refreshTokenSecret, {
      expiresIn,
      algorithm: 'HS256',
    });
  }

  verifyAccessToken(token: string): JWTPayload {
    return jwt.verify(token, this.accessTokenSecret) as JWTPayload;
  }

  refreshTokens(userId: string): { accessToken: string; refreshToken: string } {
    // Invalidate old refresh token (optional: track in DB or redis)
    return {
      accessToken: this.generateAccessToken(userId),
      refreshToken: this.generateRefreshToken(userId),
    };
  }
}
```

**Acceptance Criteria:**
- Tokens generate correctly
- Token verification works
- Token refresh rotation tested

**Test Cases:**
- Unit: Generate token, verify signature
- Unit: Verify expired token throws error
- Unit: Refresh token rotation generates new pair

---

- [ ] **3.2.4 - Auth Controller (POST /api/v1/auth/kakao)**
  - [ ] Create AuthController with route handlers
  - [ ] Implement POST /api/v1/auth/kakao endpoint:
    - Receive Kakao OAuth code
    - Exchange with Kakao API
    - Create/update User in database (first login)
    - Generate JWT tokens
    - Return access_token, refresh_token, user_exists, onboarding_completed
  - [ ] Handle errors (400, 401, 502)
  - [ ] Log authentication events (non-PII)

```typescript
router.post('/auth/kakao', async (req, res) => {
  try {
    const { kakao_oauth_code } = req.body;

    // Validate input
    if (!kakao_oauth_code) {
      return res.status(400).json({
        error: { code: 'INVALID_CODE', message: 'OAuth code required' }
      });
    }

    // Exchange with Kakao
    const kakaoTokens = await kakaoService.exchangeOAuthCode(kakao_oauth_code);
    const kakaoUser = await kakaoService.getUserInfo(kakaoTokens.access_token);

    // Create or find user
    const user = await prisma.user.upsert({
      where: { kakaoId: kakaoUser.id },
      update: { lastLoginAt: new Date() },
      create: { kakaoId: kakaoUser.id },
    });

    // Fetch profile to check onboarding
    const profile = await prisma.userProfile.findUnique({
      where: { userId: user.id },
    });

    // Generate tokens
    const accessToken = jwtService.generateAccessToken(user.id);
    const refreshToken = jwtService.generateRefreshToken(user.id);

    res.status(200).json({
      access_token: accessToken,
      refresh_token: refreshToken,
      user_exists: user.createdAt < new Date(Date.now() - 1000), // heuristic
      onboarding_completed: !!profile?.onboardingCompletedAt,
    });
  } catch (error) {
    logger.error('Kakao exchange failed', { error: error.message });
    res.status(502).json({
      error: { code: 'KAKAO_UPSTREAM_ERROR', message: 'Kakao API error' }
    });
  }
});
```

**Acceptance Criteria:**
- Endpoint receives code, exchanges with Kakao, returns tokens
- Error responses formatted correctly
- User created on first login

**Test Cases:**
- Integration: Mock Kakao API, verify endpoint returns tokens
- Integration: 400 on missing code, 502 on Kakao failure

---

- [ ] **3.2.5 - JWT Middleware**
  - [ ] Create JWTMiddleware
  - [ ] Extract Bearer token from Authorization header
  - [ ] Verify token signature
  - [ ] Attach user_id to request context
  - [ ] Return 401 if invalid/expired

```typescript
function jwtMiddleware(req: Request, res: Response, next: NextFunction) {
  const authHeader = req.get('Authorization');
  if (!authHeader) {
    return res.status(401).json({
      error: { code: 'UNAUTHORIZED', message: 'Missing auth header' }
    });
  }

  const token = authHeader.replace('Bearer ', '');
  try {
    const payload = jwtService.verifyAccessToken(token);
    req.userId = payload.sub;
    next();
  } catch (error) {
    return res.status(401).json({
      error: { code: 'TOKEN_INVALID', message: 'Invalid token' }
    });
  }
}

// Usage
router.post('/users/profile', jwtMiddleware, createProfileHandler);
```

**Acceptance Criteria:**
- Middleware extracts + verifies token
- Returns 401 on invalid token

**Test Cases:**
- Unit: Mock verify, test valid/invalid token scenarios

---

- [ ] **3.2.6 - Token Refresh Endpoint (POST /api/v1/auth/refresh)**
  - [ ] Implement POST /api/v1/auth/refresh
  - [ ] Receive old refresh token
  - [ ] Verify token + extract user_id
  - [ ] Invalidate old refresh token (optional: store hash in DB)
  - [ ] Return new access + refresh tokens
  - [ ] Handle 401 if token invalid/expired

```typescript
router.post('/auth/refresh', async (req, res) => {
  try {
    const { refresh_token } = req.body;

    if (!refresh_token) {
      return res.status(400).json({
        error: { code: 'INVALID_TOKEN', message: 'Refresh token required' }
      });
    }

    const payload = jwtService.verifyAccessToken(refresh_token);

    // Optional: Check if token was rotated (revoked)
    const tokenRecord = await prisma.tokenStore.findUnique({
      where: { userIdRefreshToken: { userId: payload.sub, refreshToken: refresh_token } }
    });

    if (!tokenRecord || tokenRecord.revokedAt) {
      return res.status(401).json({
        error: { code: 'TOKEN_ROTATION_CONFLICT', message: 'Token already rotated' }
      });
    }

    // Rotate token
    const newTokens = jwtService.refreshTokens(payload.sub);

    // Optionally revoke old token
    await prisma.tokenStore.update({
      where: { userIdRefreshToken: { userId: payload.sub, refreshToken: refresh_token } },
      data: { revokedAt: new Date() }
    });

    res.status(200).json({
      access_token: newTokens.accessToken,
      refresh_token: newTokens.refreshToken,
    });
  } catch (error) {
    return res.status(401).json({
      error: { code: 'TOKEN_INVALID', message: 'Invalid or expired token' }
    });
  }
});
```

**Acceptance Criteria:**
- Refresh endpoint works
- Token rotation returns new pair
- Old token invalidated

**Test Cases:**
- Integration: Refresh with valid token, get new pair
- Integration: Refresh with expired token, get 401

---

**3.2 Subtasks Summary:**
| # | Task | Timeline | Dependencies |
|---|------|----------|--------------|
| 3.2.1 | Kakao Config | Days 1-2 | None |
| 3.2.2 | KakaoIntegrationService | Days 2-3 | 3.2.1 |
| 3.2.3 | JWTService | Days 3-4 | None |
| 3.2.4 | AuthController | Days 4-5 | 3.2.2, 3.2.3 |
| 3.2.5 | JWTMiddleware | Day 5 | 3.2.3 |
| 3.2.6 | Refresh Endpoint | Day 5 | 3.2.3, 3.2.4 |

**Acceptance Criteria (Phase 3.2):**
- Kakao OAuth to JWT token flow complete
- All endpoints working (exchange, refresh)
- Token rotation tested
- JWT middleware protecting routes

---

#### **Phase 3.3: User Profile API & Validation (Weeks 4-5)**

**Goal:** Profile creation endpoint with comprehensive input validation

**Tasks:**

- [ ] **3.3.1 - Input Validation Utilities**
  - [ ] Korean name validator: regex `^[가-힣]{2,4}$`
  - [ ] Role validator: 2-48 chars, allow Korean + English + space
  - [ ] Growth goals validator: 100-500 chars
  - [ ] Industry code validator: enum validation
  - [ ] Create reusable validation function + error messages

```typescript
const validators = {
  validateName(name: string): ValidationResult {
    const regex = /^[가-힣]{2,4}$/;
    if (!regex.test(name)) {
      return { valid: false, error: '2-4자 한글만 입력 가능합니다' };
    }
    return { valid: true };
  },

  validateRole(role: string): ValidationResult {
    if (role.length < 2 || role.length > 48) {
      return { valid: false, error: '2-48자를 입력해주세요' };
    }
    return { valid: true };
  },

  validateGrowthGoals(goals: string): ValidationResult {
    if (goals.length < 100) {
      return { valid: false, error: '최소 100자 이상 입력해주세요' };
    }
    if (goals.length > 500) {
      return { valid: false, error: '최대 500자 이하로 입력해주세요' };
    }
    return { valid: true };
  },

  validateIndustryCode(code: string): ValidationResult {
    const validCodes = ['STARTUP', 'ENTERPRISE', 'SME', 'PUBLIC', 'FOREIGN', 'FREELANCER', 'NONPROFIT', 'OTHER'];
    if (!validCodes.includes(code)) {
      return { valid: false, error: '유효한 산업을 선택해주세요' };
    }
    return { valid: true };
  },
};
```

**Acceptance Criteria:**
- All validators work correctly
- Error messages in Korean

**Test Cases:**
- Unit: NameValidator accepts "김철수", rejects "Kim", "김", "김철수박"
- Unit: RoleValidator accepts "Product Manager", 2-48 chars
- Unit: GoalsValidator rejects <100, >500 chars
- Unit: IndustryValidator accepts enum values only

---

- [ ] **3.3.2 - User Service (Business Logic)**
  - [ ] Create UserService
  - [ ] Implement createProfile method:
    - Validate all inputs (name, role, industry, goals)
    - Check if profile already exists (409)
    - Create UserProfile record
    - Mark onboarding_completed_at
    - Return response DTO
  - [ ] Implement getCurrentUser method (for GET /api/v1/users/me)

```typescript
class UserService {
  async createProfile(userId: string, data: CreateProfileRequest): Promise<UserProfile> {
    // Validate inputs
    const nameValidation = validators.validateName(data.name);
    if (!nameValidation.valid) throw new ValidationError(nameValidation.error);

    const roleValidation = validators.validateRole(data.role);
    if (!roleValidation.valid) throw new ValidationError(roleValidation.error);

    const goalsValidation = validators.validateGrowthGoals(data.growth_goals);
    if (!goalsValidation.valid) throw new ValidationError(goalsValidation.error);

    const industryValidation = validators.validateIndustryCode(data.industry_code);
    if (!industryValidation.valid) throw new ValidationError(industryValidation.error);

    // Check if profile already exists
    const existingProfile = await prisma.userProfile.findUnique({
      where: { userId }
    });

    if (existingProfile) {
      throw new ConflictError('Profile already set');
    }

    // Create profile
    const profile = await prisma.userProfile.create({
      data: {
        userId,
        name: data.name,
        role: data.role,
        industryCode: data.industry_code,
        growthGoals: data.growth_goals,
        onboardingCompletedAt: new Date(),
      },
    });

    return profile;
  }

  async getCurrentUser(userId: string) {
    const user = await prisma.user.findUnique({
      where: { id: userId },
      include: { profile: true },
    });

    if (!user) throw new NotFoundError('User not found');

    return {
      user: { id: user.id, kakaoId: user.kakaoId },
      profile: user.profile,
      flags: { onboarding_completed: !!user.profile?.onboardingCompletedAt },
    };
  }
}
```

**Acceptance Criteria:**
- Service validates all inputs
- Creates profile correctly
- Returns error on duplicate profile

**Test Cases:**
- Unit: Mock Prisma, verify profile created
- Unit: Verify ValidationError thrown on invalid input
- Unit: Verify ConflictError on duplicate

---

- [ ] **3.3.3 - User Profile Controller**
  - [ ] Create UserController with route handlers
  - [ ] Implement POST /api/v1/users/profile (protected by JWTMiddleware)
    - Validate request body
    - Call UserService.createProfile
    - Return 201 with profile response
    - Handle validation errors (400), conflicts (409)
  - [ ] Implement GET /api/v1/users/me (protected)
    - Call UserService.getCurrentUser
    - Return 200 with user + profile

```typescript
router.post('/users/profile', jwtMiddleware, async (req: Request, res: Response) => {
  try {
    const userId = req.userId; // from JWTMiddleware
    const { name, role, industry_code, growth_goals } = req.body;

    // Validate request body schema
    if (!name || !role || !industry_code || !growth_goals) {
      return res.status(400).json({
        error: { code: 'VALIDATION_ERROR', message: 'Missing required fields' }
      });
    }

    const profile = await userService.createProfile(userId, {
      name,
      role,
      industry_code,
      growth_goals,
    });

    res.status(201).json({
      user_id: userId,
      profile: {
        name: profile.name,
        role: profile.role,
        industry_code: profile.industryCode,
        growth_goals: profile.growthGoals,
      },
      onboarding_completed_at: profile.onboardingCompletedAt?.toISOString(),
    });
  } catch (error) {
    if (error instanceof ValidationError) {
      return res.status(400).json({
        error: { code: 'VALIDATION_ERROR', message: error.message }
      });
    }
    if (error instanceof ConflictError) {
      return res.status(409).json({
        error: { code: 'PROFILE_ALREADY_SET', message: error.message }
      });
    }
    // Generic error
    res.status(500).json({
      error: { code: 'INTERNAL_ERROR', message: 'Failed to create profile' }
    });
  }
});

router.get('/users/me', jwtMiddleware, async (req: Request, res: Response) => {
  try {
    const user = await userService.getCurrentUser(req.userId);
    res.status(200).json(user);
  } catch (error) {
    res.status(404).json({
      error: { code: 'NOT_FOUND', message: 'User not found' }
    });
  }
});
```

**Acceptance Criteria:**
- Profile creation works
- Validation errors returned (400)
- Duplicate profile returns 409
- GET /users/me returns user + profile

**Test Cases:**
- Integration: POST valid profile, get 201
- Integration: POST invalid name, get 400
- Integration: POST twice, get 409 on second
- Integration: GET /users/me, get user + profile

---

**3.3 Subtasks Summary:**
| # | Task | Timeline | Dependencies |
|---|------|----------|--------------|
| 3.3.1 | Validators | Days 1-2 | None |
| 3.3.2 | UserService | Days 2-3 | 3.3.1 |
| 3.3.3 | UserController | Days 3-4 | 3.3.2, 3.2.5 |

---

#### **Phase 3.4: Error Handling & Logging (Week 5)**

**Goal:** Consistent error responses and comprehensive logging

**Tasks:**

- [ ] **3.4.1 - Error Handler Middleware**
  - [ ] Create custom error classes (ValidationError, ConflictError, NotFoundError, etc.)
  - [ ] Global error handling middleware
  - [ ] Catch unhandled errors, log, and return consistent response
  - [ ] No PII/tokens in logs

```typescript
class ApplicationError extends Error {
  constructor(public statusCode: number, public errorCode: string, message: string) {
    super(message);
  }
}

const errorMiddleware = (err: any, req: Request, res: Response, next: NextFunction) => {
  logger.error('Error', {
    code: err.errorCode || 'INTERNAL_ERROR',
    message: err.message,
    userId: req.userId, // non-PII context
    path: req.path,
    method: req.method,
  });

  if (err instanceof ApplicationError) {
    return res.status(err.statusCode).json({
      error: {
        code: err.errorCode,
        message: err.message,
      }
    });
  }

  res.status(500).json({
    error: {
      code: 'INTERNAL_ERROR',
      message: 'Internal server error',
    }
  });
};

app.use(errorMiddleware);
```

**Acceptance Criteria:**
- All errors return consistent format
- No sensitive data logged

---

- [ ] **3.4.2 - Logging Infrastructure**
  - [ ] Set up winston or pino logger
  - [ ] Log auth events (login success/failure, token refresh)
  - [ ] Log API calls (request/response, errors)
  - [ ] Structured JSON logs (timestamp, level, event, context)
  - [ ] Log levels: error, warn, info, debug

**Acceptance Criteria:**
- Logger configured
- All events logged with proper levels

---

- [ ] **3.4.3 - Request/Response Logging**
  - [ ] Create logging middleware
  - [ ] Log incoming requests (method, path, user_id)
  - [ ] Log outgoing responses (status, duration)
  - [ ] Skip logging sensitive endpoints (no password/token logging)

---

**3.4 Subtasks Summary:**
| # | Task | Timeline |
|---|------|----------|
| 3.4.1 | Error Handler | Days 1-2 |
| 3.4.2 | Logger Setup | Days 2-3 |
| 3.4.3 | Request Logging | Day 3 |

---

#### **Phase 3.5: Backend Testing (Week 6)**

**Goal:** Unit + integration tests for backend API

**Tasks:**

- [ ] **3.5.1 - Unit Tests**
  - [ ] **Validator tests** (4-5 test files):
    - `nameValidator.test.ts`
    - `roleValidator.test.ts`
    - `growthGoalsValidator.test.ts`
    - `industryCodeValidator.test.ts`
  - [ ] **Service tests** (2 test files):
    - `authService.test.ts` (token generation, verification)
    - `userService.test.ts` (profile creation, validation)
  - [ ] **Middleware tests** (1 test file):
    - `jwtMiddleware.test.ts` (valid/invalid tokens)

**Expected Test Count:** 15-25 unit tests

```typescript
// Example: nameValidator.test.ts
describe('KoreanNameValidator', () => {
  it('accepts valid Hangul names (2-4 chars)', () => {
    expect(validators.validateName('김철수').valid).toBe(true);
    expect(validators.validateName('이순신').valid).toBe(true);
  });

  it('rejects non-Hangul', () => {
    expect(validators.validateName('Kim').valid).toBe(false);
  });

  it('rejects <2 chars', () => {
    expect(validators.validateName('김').valid).toBe(false);
  });
});
```

**Acceptance Criteria:**
- Unit tests pass
- Validators thoroughly tested

---

- [ ] **3.5.2 - Integration Tests**
  - [ ] **Auth flow** (2-3 test files):
    - `authController.test.ts` (token exchange, refresh)
    - `kakaoIntegration.test.ts` (Kakao API mock)
  - [ ] **Profile API** (1-2 test files):
    - `profileController.test.ts` (POST /users/profile, GET /users/me)
  - [ ] **E2E flow** (1 test file):
    - Full auth → profile creation flow

**Expected Test Count:** 10-15 integration tests

```typescript
// Example: authController.test.ts
describe('POST /api/v1/auth/kakao', () => {
  it('exchanges valid code for tokens', async () => {
    const response = await request(app)
      .post('/api/v1/auth/kakao')
      .send({ kakao_oauth_code: 'valid_code' });

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('access_token');
    expect(response.body).toHaveProperty('refresh_token');
  });

  it('returns 400 on missing code', async () => {
    const response = await request(app)
      .post('/api/v1/auth/kakao')
      .send({});

    expect(response.status).toBe(400);
  });
});
```

**Acceptance Criteria:**
- Integration tests pass
- API endpoints verified

---

- [ ] **3.5.3 - Database Tests**
  - [ ] Verify User model creation
  - [ ] Verify UserProfile creation + constraints
  - [ ] Verify unique constraints (kakaoId, userId)
  - [ ] Verify cascade delete

**Expected Test Count:** 5-8 database tests

---

**3.5 Subtasks Summary:**
| # | Task | Tests Expected | Timeline |
|---|------|---|----------|
| 3.5.1 | Unit Tests | 15-25 | Days 1-2 |
| 3.5.2 | Integration Tests | 10-15 | Days 2-3 |
| 3.5.3 | Database Tests | 5-8 | Days 3-4 |

**Total Backend Test Count:** ~30-50 tests

---

#### **Phase 3.6: Security & Deployment Prep (Week 6)**

**Goal:** Secure API, prepare for deployment

**Tasks:**

- [ ] **3.6.1 - Security Hardening**
  - [ ] HTTPS/TLS configured
  - [ ] CORS configured (allow only frontend domains)
  - [ ] Helmet.js for security headers
  - [ ] Input sanitization (trim, validate types)
  - [ ] Rate limiting on auth endpoints (optional Redis)

```typescript
import helmet from 'helmet';
import cors from 'cors';

app.use(helmet());
app.use(cors({
  origin: ['https://memoir-app.com', 'https://staging.memoir-app.com'],
  credentials: true,
}));

// Rate limiting on auth
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 5, // 5 requests per window
  message: 'Too many login attempts, please try again later',
});

app.post('/api/v1/auth/kakao', authLimiter, authController);
```

**Acceptance Criteria:**
- Security headers present
- CORS restricted
- Input validated

---

- [ ] **3.6.2 - Environment Configuration**
  - [ ] .env file created with all secrets
  - [ ] .env.example provided (without secrets)
  - [ ] Database URL (test vs prod)
  - [ ] JWT secrets
  - [ ] Kakao credentials
  - [ ] Node environment (development vs production)

**Acceptance Criteria:**
- .env configured for all environments

---

- [ ] **3.6.3 - Database Backups & Migration Strategy**
  - [ ] Backup procedure documented
  - [ ] Migration rollback tested
  - [ ] Staging database configured

**Acceptance Criteria:**
- Backup/restore process verified

---

**3.6 Subtasks Summary:**
| # | Task | Timeline |
|---|------|----------|
| 3.6.1 | Security | Days 1-2 |
| 3.6.2 | Env Config | Day 2 |
| 3.6.3 | Backups | Days 3-4 |

---

---

## Integration Testing

### Frontend-Backend Integration (Week 7)

**Goal:** End-to-end testing with real API endpoints

**Tasks:**

- [ ] **Integration 1.1: Login Flow**
  - [ ] Frontend Kakao login → backend token exchange
  - [ ] Verify tokens saved locally
  - [ ] Verify onboarding state returned

**Test Case:**
- User taps login → redirected to Kakao → callback with auth code → exchanged → tokens returned → saved to DataStore → app continues to onboarding check

---

- [ ] **Integration 1.2: Onboarding to Profile Submission**
  - [ ] User progresses through 5 screens
  - [ ] Form submitted to backend
  - [ ] Profile created in database
  - [ ] onboarding_completed_at set
  - [ ] Analytics events fire

**Test Case:**
- Fill profile form → POST /api/v1/users/profile → 201 response → profile saved to Room → onboarding_completed flag set → navigate to main app

---

- [ ] **Integration 1.3: Token Refresh**
  - [ ] Generate expired access token
  - [ ] App relaunch → auto-refresh triggered
  - [ ] New tokens received
  - [ ] Old refresh token invalidated

**Test Case:**
- Login → manually set access token expiry to past → kill app → relaunch → verify refresh endpoint called → new tokens returned → continue to main app

---

- [ ] **Integration 1.4: Session Persistence**
  - [ ] Save tokens + profile to local storage
  - [ ] Kill app
  - [ ] Relaunch → verify tokens loaded from DataStore
  - [ ] Verify profile loaded from Room
  - [ ] Skip login + onboarding if already completed

**Test Case:**
- Login → complete onboarding → kill app → relaunch → skip to main app (not login/onboarding)

---

- [ ] **Integration 1.5: Error Scenarios**
  - [ ] Network timeout during login → show error + allow retry
  - [ ] Invalid profile form → show validation errors
  - [ ] Profile already set (409) → show error message
  - [ ] Token expired mid-session → redirect to login

---

### Performance Testing (Week 7)

**Goal:** Verify API response times meet targets

**Tasks:**

- [ ] **Load Test 1: Kakao Token Exchange (>50 concurrent)**
  - [ ] Target: <1.5 seconds (server-side, 95th percentile)
  - [ ] Tool: k6 or Artillery
  - [ ] Scenario: 50 concurrent POST /api/v1/auth/kakao requests

---

- [ ] **Load Test 2: Profile Creation (>50 concurrent)**
  - [ ] Target: <2 seconds (95th percentile)
  - [ ] Scenario: 50 concurrent POST /api/v1/users/profile requests

---

---

## UAT & Polish (Weeks 8-9)

### Korean Copywriting Review

- [ ] All UI text reviewed by native Korean speaker
- [ ] Tone verified (warm professional, polite)
- [ ] Terminology aligned (deposit system, penalty, refund)
- [ ] No grammatical errors

### Design System Compliance

- [ ] Colors match brand palette (mustard #F4BA54, carrot orange #E86221, dark olive #124234)
- [ ] Typography uses Pretendard font
- [ ] Touch targets 48dp minimum
- [ ] Responsive on 320-480dp widths

### Accessibility Baseline

- [ ] Content descriptions on key UI elements
- [ ] Proper heading hierarchy
- [ ] Color contrast WCAG AA (4.5:1)
- [ ] Form labels associated with inputs

### Legal Documentation

- [ ] Terms of Service finalized + Korean legally reviewed
- [ ] Privacy Policy (PIPA-compliant)
- [ ] Deposit Policy with clear refund/penalty terms
- [ ] Acceptance checkboxes in app (TOS + Privacy required)

### Android Device Testing

- [ ] Test on minimum SDK (API 26)
- [ ] Test on target SDK (API 34)
- [ ] Test on emulator + real devices
- [ ] Test on multiple screen sizes (4-6 inch phones)

---

## Deployment & Launching

### Pre-Launch Checklist

**Week 8:**
- [ ] Kakao Developer Account approved + app registered
- [ ] Firebase project configured (Authentication, Analytics, Crashlytics)
- [ ] Backend API deployed to staging environment
- [ ] SSL/TLS certificates configured
- [ ] Database backups verified
- [ ] API documentation (Swagger/OpenAPI) generated
- [ ] Monitoring dashboards set up (Sentry, CloudWatch)

**Week 9:**
- [ ] Soft launch to internal testers (employees, friends)
  - [ ] Collect feedback on onboarding clarity + tone
  - [ ] Verify deposit system explanation comprehension (target ≥70% clarity)
  - [ ] Monitor crash rates (target <2%)
  - [ ] Collect NPS (target ≥30 for MVP)
- [ ] Fix critical bugs
- [ ] Update Korean copy based on feedback
- [ ] Final security audit

**Week 10:**
- [ ] Beta launch (invite-only)
  - [ ] 50-100 users from target demographic
  - [ ] 1-week open beta period
  - [ ] Monitor onboarding completion rates (target ≥70%)
- [ ] Final QA pass
- [ ] Update App Store listing + description (Korean)
- [ ] Prepare press release (if applicable)

**Week 11:**
- [ ] General Availability (GA) launch
- [ ] Public announcement
- [ ] Monitor user flow + analytics
- [ ] Standby for critical bugfixes

### Post-Launch Monitoring

**First 48 Hours:**
- [ ] Monitor crash rate (Alert if >5%)
- [ ] Monitor login failures (Alert if >5%)
- [ ] Monitor API error rates
- [ ] Monitor onboarding funnel (screen drop-off)
- [ ] Check Firebase Crashlytics for recurring errors

**First Week:**
- [ ] Daily standup on metrics
- [ ] Onboarding completion rate tracking
- [ ] Profile submission success rate
- [ ] User feedback collection
- [ ] Prepare hotfix for any critical issues

---

## Risk Management

### Key Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|-----------|
| **Kakao API Rate Limiting** | Users unable to login | Medium | Implement exponential backoff + user error message + status page |
| **Low Onboarding Completion** (<60%) | Funnel leakage | High | A/B test copy clarity, compress screens, add progress indicator |
| **Users Confused by Deposit System** | Support burden | High | Very clear explanation (Screen 4) + social proof (65% refund) + FAQ in app |
| **Backend API Downtime** | Login failures | Low | AWS redundancy, auto-scaling, failover strategy |
| **Token Theft (Rooted Device)** | Account misuse | Medium | Secure storage in Android Keystore, device binding (Phase 2) |
| **Profile Data Quality Issues** | Weak cohort matching later | Medium | Enforce growth goals minimum 100 chars, add examples/tooltips |
| **Korean Translation Quality** | User confusion/churn | Medium | Native Korean speaker review + user testing |
| **Kakao OAuth Changes** | Integration breaks | Low | Monitor Kakao release notes, maintain backwards compatibility |
| **Legal Document Approval Delays** | Launch blocked | Medium | Start legal review process immediately (Week 1) |
| **Performance Issues Under Load** | Poor user experience | Low | Load test before launch, auto-scaling configured |

### Contingency Plans

**If Kakao API unavailable:**
- Show "카카오 서비스 이용 불가 - 잠시 후 다시 시도해주세요" (Service unavailable)
- Provide manual admin workaround (generate test tokens)
- ETA for restoration from status page

**If onboarding completion <50%:**
- A/B test shorter copy
- A/B test removing Screen 4 (deposit) to end
- Analyze abandonment screen (where users drop off)
- Rapid iteration in Week 3-4 of soft launch

**If high profile submission failures:**
- Roll back form validation changes
- Investigate API timeout issues
- Provide support email for manual profile creation

---

## Resource Allocation & Timeline

### Team Composition

**Frontend Team (2 engineers):**
- Lead: Mobile engineer (Android/Kotlin expertise)
- Secondary: UI/Compose specialist

**Backend Team (1-2 engineers):**
- Lead: Backend/Node.js engineer (API design)
- Secondary: Optional (database, DevOps support)

**Design/PM (1-2 roles):**
- Designer: Figma mockups, design system, handoff
- PM: Requirements, Korean copywriting, user research

**Legal/Ops (External):**
- Lawyer: Terms of Service, Privacy Policy, Deposit Policy
- Kakao Integration Support: Account setup

### Detailed Timeline

```
Week 1-2 (Backend Setup)
├─ Mon: Backend project setup, Postgres config, Prisma schema
├─ Tue: Kakao credentials acquired, JWT service built
├─ Wed: Auth controller + profile controller skeleton
├─ Thu: Database migration, basic error handling
├─ Fri: First tests, code review

Week 2-3 (Frontend Setup)
├─ Mon: Android project setup, Hilt DI, Retrofit config
├─ Tue: DataStore + Room config, base MVVM structure
├─ Wed: Kakao SDK integration, LoginViewModel built
├─ Thu: First Compose components (buttons, themes)
├─ Fri: Integration testing with backend

Week 3-4 (Auth Implementation)
├─ Mon-Wed: Frontend login screen + Kakao callback handler
├─ Mon-Wed: Backend Kakao token exchange, JWT generation
├─ Thu-Fri: E2E flow testing (login → tokens → persistence)

Week 4-5 (Onboarding Screens 1-3)
├─ Mon-Tue: Frontend onboarding template + screens 1-3
├─ Wed-Thu: Backend profile creation endpoint + validation
├─ Fri: Integration (onboarding → profile API)

Week 5-6 (Onboarding Screens 4-5 + Profile Form)
├─ Mon-Tue: Frontend screens 4-5, deposit table, legal links
├─ Wed-Thu: Frontend profile form, validation, submit
├─ Fri: E2E testing (full onboarding flow)

Week 6-7 (Error Handling + Monitoring)
├─ Mon-Tue: Frontend error handling, network retries, edge cases
├─ Wed-Thu: Backend logging, rate limiting, security hardening
├─ Fri: Analytics instrumentation, Firebase events

Week 7-8 (Testing + Polish)
├─ Mon-Tue: Frontend unit + UI tests, backend unit + integration tests
├─ Wed: Performance testing (load tests)
├─ Thu: Korean copywriting review, accessibility baseline
├─ Fri: Manual testing on real devices

Week 8-9 (UAT + Soft Launch)
├─ Mon-Tue: Soft launch (internal testers)
├─ Wed-Thu: Feedback collection, bugfixes
├─ Fri: Prepare beta launch

Week 9-10 (Beta + GA)
├─ Mon-Tue: Beta launch (invite-only, 50-100 users)
├─ Wed-Thu: Monitor + iterate
├─ Fri-Sat: Final QA, GA launch preparation

Week 10-11 (GA Launch + Post-Launch)
├─ Mon: General Availability launch
├─ Tue-Fri: 24/7 monitoring, hotfix standby
├─ Following week: Post-mortem, Phase 2 planning
```

### Capacity Planning

**Frontend: 280 hours (10 weeks, 2 engineers)**
- Week 1: 16h (setup)
- Week 2: 16h (setup)
- Week 3: 24h (auth)
- Week 4: 32h (onboarding 1-3)
- Week 5: 32h (onboarding 4-5, profile)
- Week 6: 24h (error handling)
- Week 7: 32h (tests)
- Week 8: 24h (UAT, polish)
- Week 9: 32h (soft launch, feedback)
- Week 10: 48h (beta, GA launch)

**Backend: 160 hours (10 weeks, 1 engineer or 2 x 80h)**
- Week 1: 16h (setup)
- Week 2: 16h (setup)
- Week 3: 16h (auth)
- Week 4: 24h (profile)
- Week 5: 16h (error handling, logging)
- Week 6: 24h (tests)
- Week 7: 16h (security, monitoring)
- Week 8: 8h (standby)
- Week 9: 16h (soft launch support)
- Week 10: 12h (GA support)

**Design: 40 hours (2 weeks, 1 designer)**
- Week 1: 8h (design system setup)
- Week 2: 24h (screen mockups: login + onboarding 1-5)
- Week 3: 8h (revisions)

---

## Success Metrics & Validation Gates

### Phase 1 Success Criteria

| Metric | Target | Measurement |
|--------|--------|-------------|
| Onboarding Completion Rate | ≥70% | Firebase Analytics funnel |
| Profile Submission Success | ≥95% | API logs, Crashlytics |
| Crash Rate | <2% | Firebase Crashlytics |
| Auth Failure Rate (excluding user cancel) | <2% | Server logs |
| User Comprehension of Deposit System | ≥70% clarity | Post-launch survey |
| Average Onboarding Time | 3-5 minutes | Firebase session duration |
| User Retention (30-day) | ≥50% | Firebase retention metric |
| App Stability (Cold Start) | <3 seconds | Firebase Performance |

### Phase Gate (Before Deposit Automation in Phase 2)

**Must achieve before proceeding to Phase 2:**
- ≥65% onboarding completion rate
- ≥70% user comprehension of deposit system (survey)
- <2% auth failure rate
- No critical crashes on top 10 devices
- Legal documents finalized

---

## Appendix: API Specification

### Authentication Endpoints

**POST /api/v1/auth/kakao**
```
Request:
{
  "kakao_oauth_code": "string"
}

Response (200):
{
  "access_token": "eyJhbGc...",
  "refresh_token": "eyJhbGc...",
  "user_exists": false,
  "onboarding_completed": false
}

Errors:
400 INVALID_CODE - Missing or invalid code
502 KAKAO_UPSTREAM_ERROR - Kakao API error
```

**POST /api/v1/auth/refresh**
```
Request:
{
  "refresh_token": "string"
}

Response (200):
{
  "access_token": "eyJhbGc...",
  "refresh_token": "eyJhbGc..."
}

Errors:
401 TOKEN_INVALID - Invalid or expired token
409 TOKEN_ROTATION_CONFLICT - Token already rotated
```

### User Endpoints

**POST /api/v1/users/profile**
```
Auth: Bearer {access_token}

Request:
{
  "name": "김철수",
  "role": "Product Manager",
  "industry_code": "STARTUP",
  "growth_goals": "개인적인 성장과 의미 있는 연결을 통해..."
}

Response (201):
{
  "user_id": "user123",
  "profile": {
    "name": "김철수",
    "role": "Product Manager",
    "industry_code": "STARTUP",
    "growth_goals": "..."
  },
  "onboarding_completed_at": "2025-11-19T12:00:00Z"
}

Errors:
400 VALIDATION_ERROR - Invalid input (name, role, goals length, industry)
401 UNAUTHORIZED - Missing or invalid token
409 PROFILE_ALREADY_SET - Profile already created
```

**GET /api/v1/users/me**
```
Auth: Bearer {access_token}

Response (200):
{
  "user": {
    "id": "user123",
    "kakao_id": "1234567890"
  },
  "profile": {
    "name": "김철수",
    "role": "Product Manager",
    "industry_code": "STARTUP",
    "growth_goals": "...",
    "onboarding_completed_at": "2025-11-19T12:00:00Z"
  },
  "flags": {
    "onboarding_completed": true
  }
}

Errors:
401 UNAUTHORIZED - Invalid token
404 NOT_FOUND - User not found
```

---

## Conclusion

This tasks.md provides a comprehensive implementation plan for the Memoir User Authentication & Onboarding feature (Phase 1 MVP). The plan is organized around:

1. **Clear phasing:** Frontend setup → Backend setup → Auth implementation → Onboarding UI → Integration → Testing → Deployment
2. **Specific, actionable tasks:** Each task has clear acceptance criteria, test cases, and dependencies
3. **Korean market considerations:** Hangul validation, Korean industry taxonomy, KST timezone, polite speech, deposit system clarity
4. **Quality baseline:** 30-40% test coverage, 40-55 tests total, focus on critical paths
5. **Risk-aware:** Identified key risks (Kakao API, low completion, user confusion) with mitigation strategies

**Estimated timeline:** 8-10 weeks with 2 frontend engineers + 1 backend engineer + 1 designer

**Success target:** ≥70% onboarding completion, ≥95% profile submission success, <2% crash rate, GA launch by Week 11

---

**Document Version:** 1.0
**Last Updated:** 2025-11-19
**Owner:** Product & Engineering Team
**Status:** Ready for Implementation
