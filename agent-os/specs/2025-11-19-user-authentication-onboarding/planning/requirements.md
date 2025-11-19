# Spec Requirements: User Authentication & Onboarding

## Initial Description
Build an Android app inspired by https://memoirapp.com/ - a weekly reflection community platform.

The first feature to build is:

**Feature 1: User Authentication & Onboarding**
- User registration and login system
- Phone number verification (Korean mobile carriers)
- Onboarding flow introducing the app concept
- Basic user profile setup

This is the foundational feature for the Memoir Android app project. It establishes the entry point for users to access the platform and sets up their initial account and profile.

## Requirements Discussion

### First Round Questions

**Q1: Authentication Method - I'm assuming we'll use Firebase phone number authentication with SMS verification for Korean mobile carriers. However, I notice the product serves Korean professionals. Should we also support Kakao Login, Naver Login, or Google Sign-In as alternatives (very common in Korea)?**

**Answer:** Use Kakao login only (no phone verification, no email, no Google)

**Q2: Onboarding Flow Structure - I'm thinking we should have a mandatory one-time onboarding flow after first login that walks users through the app concept, cohort structure, and deposit system. Should onboarding be skippable, or mandatory before accessing the main app?**

**Answer:** Make best judgment

**Decision Made:** Mandatory one-time onboarding (cannot skip)

**Rationale:**
- The deposit-penalty system (200,000 KRW deposit + 20,000 KRW penalties) requires informed consent - users MUST understand financial commitments before proceeding
- Cohort-based model is unfamiliar to most users; skipping education would lead to confusion and poor engagement
- Product principle of "Intentional Friction" supports slowing users down at critical moments
- Korean app users expect comprehensive onboarding for premium services (100,000 KRW monthly fee signals premium positioning)
- Success metrics depend on "Time to First Reflection" - proper onboarding sets context for why reflection matters
- Legal protection: Clear explanation of deposit system mitigates future disputes

**Q3: Profile Setup Fields - For the minimal profile during onboarding, I'm assuming we collect: full name, professional role/title, industry, personal growth goals (free text), and optional profile photo. Is this the right balance of minimal friction vs. useful data for cohort matching?**

**Answer:** Use the suggested fields (name, role, industry, growth goals, profile photo optional)

**Q4: Korean Market Specifics - Since this targets Korean users, should we implement Korean-specific features like: Korean name input validation (hangul characters), industry taxonomy familiar to Korean professionals (e.g., "대기업", "스타트업", "공기업"), and date/time formatting in Korean locale?**

**Answer:** Make best judgment

**Decision Made:** Full Korean localization with market-specific features

**Rationale:**
- All UI text and system messages in Korean language only (no English fallback for MVP)
- Korean name validation: Accept hangul (한글) characters only; typical format is 2-4 characters
- Industry taxonomy aligned with Korean job market terminology:
  - "스타트업" (Startup)
  - "대기업" (Large Corporation/Conglomerate)
  - "중소기업" (SME - Small/Medium Enterprise)
  - "공기업/공공기관" (Public Corporation/Government Agency)
  - "외국계 기업" (Foreign Company)
  - "프리랜서/1인 기업" (Freelancer/Solopreneur)
  - "비영리/사회적 기업" (Non-profit/Social Enterprise)
- Professional role field accepts Korean and English input (many Korean job titles use English terms like "Product Manager")
- Date/time formatting: Korean Standard Time (KST, UTC+9) with Korean locale formatting
- Phone number format: Korean mobile format (010-XXXX-XXXX) if needed for display
- Typography: Pretendard font family (per design system) optimized for Korean readability

**Q5: Session Management - For user sessions, should we use short-lived JWT tokens requiring frequent re-authentication (more secure), or longer sessions with refresh tokens (better UX for mobile app where users expect to stay logged in)?**

**Answer:** Make best judgment

**Decision Made:** Long-lived sessions with refresh token rotation

**Rationale:**
- Mobile app UX expectation: Users expect "set and forget" login on personal devices
- Security balanced with UX: 30-day access tokens + 90-day refresh tokens with rotation
- Logout only on explicit user action or token revocation (account security issue)
- Aligns with Kakao SDK session management patterns Korean users are familiar with
- Reduces friction for daily app usage (checking cohort feed, writing reflections)
- MVP priority: Optimize for engagement over paranoid security (not handling banking data)
- Biometric re-authentication can be added in Phase 2 for sensitive actions (payment changes)

**Q6: Onboarding Content & Tone - For the onboarding screens explaining the app concept, should we use a friendly/casual tone or professional/aspirational tone? And should we focus on community benefits ("meet diverse professionals") or accountability benefits ("build lasting habits with financial commitment")?**

**Answer:** Make best judgment

**Decision Made:** Warm professional tone emphasizing both accountability AND community

**Rationale:**
- Tone: "Warm professional" (not overly casual, not corporate cold)
  - Matches Memoir's design principle: "Warm & Approachable" with mustard/carrot orange color palette
  - Respects Korean professional context (users aged 25-40 expect some formality)
  - Use polite speech forms (존댓말) but not overly formal bureaucratic language
- Content balance: Lead with community, reinforce with accountability
  - Screen 1: Problem framing ("일상의 배움이 휘발되고 있지 않나요?" - "Aren't your daily learnings evaporating?")
  - Screen 2: Community solution ("다양한 분야의 성장하는 사람들과 함께" - "Together with growth-minded people from diverse fields")
  - Screen 3: Structure explanation (10-week cohorts, weekly reflections, peer feedback)
  - Screen 4: Accountability mechanism (deposit system explained clearly with financial transparency)
  - Screen 5: Expected commitment (time investment: ~30 min writing + 20 min reading weekly)
- Emphasize both benefits per product mission: "meaningful connections" AND "habit formation"
- Use real user persona language: "일상이 의미 있는 배움으로" (Daily experiences become meaningful learning)

**Q7: Deposit System Explanation in Onboarding - The deposit-penalty system (200,000 KRW deposit + 100,000 KRW monthly fee with 20,000 KRW penalties) is core to the product but could scare users away. Should we introduce this gently in later onboarding screens, or be upfront about it immediately?**

**Answer:** Make best judgment

**Decision Made:** Progressive disclosure with transparent breakdown (Screen 4 of 5)

**Rationale:**
- Timing: Screen 4 (after community value established, before commitment ask)
  - First build emotional buy-in (Screens 1-3: problem, community, structure)
  - Then introduce financial commitment with context already established
  - Final screen (5) asks for profile setup as lower-friction commitment after financial transparency
- Presentation style: Transparent breakdown with refund emphasis
  - Frame as "보증금 시스템" (Deposit System) not "벌금" (Penalty/Fine)
  - Visual breakdown:
    - Initial: 200,000 KRW deposit + 100,000 KRW first month fee
    - Per cycle: 100,000 KRW monthly fee (10-week commitment)
    - Refund: Full deposit returned if weekly reflections completed (70%+ completion = partial refund)
    - Deduction: 20,000 KRW per missed reflection, 10,000 KRW for insufficient commenting
  - Emphasize refund > penalty: "성실히 참여하시면 보증금 전액 환급" (Full deposit refund for sincere participation)
- Legal clarity: Link to full Terms of Service and Deposit Policy (required tap before proceeding)
- Social proof element: "1기 참여자의 65%가 전액 환급받았어요" (65% of Cohort 1 received full refund) - builds confidence
- MVP caveat: Since deposit automation is Phase 2 per roadmap, onboarding should mention "입금 계좌 안내는 이메일로 발송됩니다" (Deposit account details will be sent via email) for manual processing

**Q8: Scope Exclusions - What should we explicitly exclude from this MVP to ship quickly? For example: password reset flows (if using phone auth), account deletion, profile editing (lock after initial setup), social login with multiple providers, email verification, etc.**

**Answer:** Make best judgment

**Decision Made:** Exclude the following from MVP scope

**Out of Scope for Phase 1 (MVP):**
- Password reset flow (Kakao handles authentication, no password to reset)
- Account deletion feature (manual support process for MVP; GDPR/PIPA compliance added Phase 3)
- Profile editing after initial setup (profiles locked until Phase 2; reduces complexity and encourages thoughtful initial setup)
- Multiple social login providers (Kakao only; Google/Naver deferred until user demand >15%)
- Email verification (no email collection in MVP)
- Phone number verification (not needed with Kakao OAuth)
- Two-factor authentication (overkill for MVP; biometric re-auth for payments added Phase 2)
- Avatar photo upload during onboarding (profile photos marked optional; can upload later via admin or Phase 2 edit feature)
- Onboarding re-play feature ("see tour again" button - not needed if onboarding is clear)
- Language selection (Korean only; internationalization deferred indefinitely)
- Accessibility features beyond basic (screen reader support, high contrast mode - Phase 4)
- Animated onboarding transitions (nice-to-have; static screens acceptable for MVP)
- Onboarding completion tracking/analytics beyond basic Firebase events (detailed funnel analysis Phase 3)

**In Scope Clarifications:**
- Kakao Login SDK integration only
- One-time mandatory onboarding flow (5 screens)
- Basic profile setup (name, role, industry dropdown, growth goals text field)
- Session persistence with refresh tokens
- Korean language UI only
- Firebase Authentication backend integration
- Basic error handling (network errors, Kakao login failures)
- Terms of Service and Privacy Policy agreement checkboxes
- Deposit system explanation (informational only; payment collection is manual for Phase 1)

### Existing Code to Reference

**Similar Features Identified:**
No similar existing features identified for reference. This is the foundational first feature for the Memoir Android app.

**Tech Stack Alignment:**
Per `agent-os/product/tech-stack.md`:
- Use Firebase Authentication with Kakao provider integration
- Jetpack Compose for onboarding UI screens
- Material 3 components with Memoir custom theming (mustard #F4BA54, carrot orange #E86221, dark olive #124234)
- Retrofit + OkHttp for backend API communication
- Room database for caching user profile locally
- DataStore for storing session tokens and onboarding completion flag
- Hilt for dependency injection

### Follow-up Questions

No follow-up questions required. All decisions have been made with clear rationale based on product context, Korean market standards, and MVP scope priorities.

## Visual Assets

### Files Provided:
No visual assets provided.

### Visual Insights:
No visual assets to analyze. Design implementation should follow:
- Memoir design system from `agent-os/product/mission.md`: Minimalist & Distraction-Free, Warm & Approachable, Mobile-First
- Material 3 components with custom Memoir color palette
- Pretendard font family for Korean typography
- Portrait orientation optimized for one-handed use
- Thumb-friendly touch targets

**Recommendation for Designer:** Create Figma mockups for:
1. Kakao Login screen (branded button, terms agreement checkboxes)
2. Onboarding screens 1-5 (problem, community, structure, deposit, profile setup)
3. Profile setup form (name, role dropdown, industry selector, growth goals text area)
4. Loading states and error messages

## Requirements Summary

### Functional Requirements

**Authentication:**
- Kakao Login SDK integration as sole authentication method
- OAuth 2.0 flow with Kakao account linking
- No email, phone number, or Google login options
- Session management with JWT access tokens (30-day expiry) and refresh tokens (90-day expiry with rotation)
- Automatic token refresh on app launch if within refresh window
- Logout functionality (explicit user action only)

**Onboarding Flow:**
- Mandatory one-time flow (cannot skip or dismiss)
- 5-screen progressive disclosure sequence:
  1. Problem framing: Daily learnings evaporating without reflection
  2. Community value: Diverse professionals, peer learning, meaningful connections
  3. Structure explanation: 10-week cohorts, weekly reflections, feedback requirements
  4. Accountability mechanism: Deposit system breakdown (200K deposit, 100K monthly, refund structure, penalty deductions)
  5. Profile setup: Collect user information before app access
- Terms of Service and Privacy Policy agreement (required checkboxes before completion)
- Deposit Policy explanation with link to full legal document
- Onboarding completion flag stored locally (DataStore) and synced to backend

**Profile Setup:**
- Full name (required): Korean hangul validation (2-4 characters typical)
- Professional role/title (required): Free text field accepting Korean and English
- Industry (required): Dropdown selector with Korean job market taxonomy:
  - 스타트업 (Startup)
  - 대기업 (Large Corporation)
  - 중소기업 (SME)
  - 공기업/공공기관 (Public Corporation/Government)
  - 외국계 기업 (Foreign Company)
  - 프리랜서/1인 기업 (Freelancer/Solopreneur)
  - 비영리/사회적 기업 (Non-profit/Social Enterprise)
  - 기타 (Other)
- Personal growth goals (required): Multi-line text field (100-500 characters recommended)
- Profile photo (optional): Deferred to Phase 2 or manual upload; placeholder avatar used
- All fields except photo required to complete onboarding

**Korean Localization:**
- All UI text in Korean language (no English fallback)
- Korean name input validation (hangul character set)
- Industry taxonomy using Korean professional terminology
- Date/time formatting in Korean locale (KST timezone)
- Polite speech forms (존댓말) for all user-facing copy
- Number formatting for currency (200,000 KRW → "200,000원" or "20만원")

**Data Management:**
- User profile data sent to backend API on onboarding completion
- Local caching of profile in Room database
- Session tokens stored securely in DataStore (encrypted preferences)
- Onboarding completion status persisted locally and synced
- Network error handling with retry logic and user-friendly messages

**Navigation Flow:**
1. App launch → Check authentication state
2. If not authenticated → Kakao Login screen
3. After successful Kakao login → Check onboarding completion status
4. If onboarding not completed → Start onboarding flow (Screen 1 of 5)
5. Progress through onboarding screens sequentially (no back button on screens 1-4)
6. Complete profile setup (Screen 5)
7. Submit profile → Backend API call
8. On success → Mark onboarding complete → Navigate to main app (Cohort Feed)
9. On subsequent launches → Skip directly to main app if authenticated + onboarding complete

### Reusability Opportunities

**Components to build (reusable across app):**
- PrimaryButton composable (Memoir branded button with custom colors)
- SecondaryButton composable (outlined variant)
- TextInputField composable (Material 3 text field with Memoir styling)
- DropdownSelector composable (industry selection pattern reusable for other forms)
- ProgressIndicator composable (onboarding step indicator - dots or progress bar)
- ErrorMessage composable (network error, validation error display)
- LoadingOverlay composable (full-screen loading state during API calls)

**Backend patterns to establish:**
- User registration endpoint: `POST /api/v1/auth/register`
- User profile creation endpoint: `POST /api/v1/users/profile`
- Token refresh endpoint: `POST /api/v1/auth/refresh`
- Session validation middleware (JWT verification)
- Request/response validation schemas (using backend validation standards)
- Error response formatting (consistent JSON structure)

**Architecture patterns to establish:**
- MVVM with Clean Architecture (per tech stack standards)
- Repository pattern for authentication data source (remote API + local Room cache)
- Use case classes for business logic (LoginUseCase, RegisterUserUseCase, SaveProfileUseCase)
- StateFlow for reactive UI updates (AuthenticationState, OnboardingState, ProfileSetupState)
- Sealed classes for Result types (Success, Error, Loading states)
- Hilt dependency injection modules (NetworkModule, DatabaseModule, AuthModule)

### Scope Boundaries

**In Scope (Phase 1 MVP):**
- Kakao Login integration (Kakao SDK for Android)
- One-time mandatory onboarding flow (5 screens with progressive disclosure)
- Profile setup form with validation (name, role, industry, growth goals)
- Korean language UI with localized content
- Session management (JWT with refresh tokens)
- Terms of Service and Privacy Policy agreement
- Deposit system explanation (informational; payment is manual for Phase 1)
- Basic error handling (network errors, authentication failures, validation errors)
- Local data persistence (Room for profile, DataStore for tokens)
- Backend API integration for user registration and profile creation
- Navigation to main app after onboarding completion

**Out of Scope (Deferred to Phase 2+):**
- Profile editing functionality (profiles locked after initial setup until Phase 2)
- Account deletion feature (manual support process; automated feature in Phase 3)
- Multiple authentication providers (Google, Naver, Apple - wait for user demand)
- Email verification and email-based authentication
- Phone number verification (not needed with Kakao OAuth)
- Two-factor authentication and biometric re-authentication
- Profile photo upload during onboarding (optional field deferred; placeholder used)
- Onboarding re-play or tutorial mode ("see tour again" feature)
- Language selection and internationalization (Korean only for foreseeable future)
- Advanced accessibility features (screen reader optimization, high contrast mode - Phase 4)
- Animated onboarding transitions (static screens acceptable for MVP)
- Detailed onboarding analytics funnel (basic Firebase events only; Mixpanel integration Phase 3)
- Password reset flows (no passwords in system; Kakao handles auth recovery)
- Social sharing of profile or onboarding invitation
- Referral code input during registration (referral system is Phase 3 per roadmap)

**Future Enhancements Mentioned:**
- Profile editing UI (Phase 2 per roadmap)
- Photo upload functionality (Phase 2 after S3 integration established)
- Account deletion with GDPR/PIPA compliance (Phase 3)
- Biometric re-authentication for sensitive actions (Phase 2 payment features)
- Multiple authentication providers if demand exceeds 15% of support inquiries
- Advanced onboarding analytics with Mixpanel (Phase 3)
- Accessibility improvements for screen readers (Phase 4)

### Technical Considerations

**Integration Points:**
- Kakao Login SDK for Android (official Kakao SDK)
- Firebase Authentication (Kakao provider configuration)
- Backend REST API (Express.js + TypeScript per tech stack)
- PostgreSQL database via Prisma ORM (user table, profile table)
- Firebase Analytics (track: kakao_login_success, onboarding_started, onboarding_completed, profile_submitted)
- Crashlytics (error tracking for authentication failures)

**Existing System Constraints:**
- Minimum Android SDK: API 26 (Android 8.0 Oreo)
- Target Android SDK: API 34 (Android 14)
- Kotlin-only codebase (100% Kotlin)
- Jetpack Compose for all UI (no XML layouts)
- Material 3 design system with Memoir custom theme
- MVVM architecture with Clean Architecture separation

**Technology Preferences:**
- Retrofit + OkHttp for HTTP networking (per tech stack)
- Kotlinx Serialization for JSON parsing (per tech stack)
- Room for local database (per tech stack)
- DataStore for preferences and tokens (per tech stack)
- Hilt for dependency injection (per tech stack)
- Coil for image loading if profile photos added (per tech stack)
- JUnit 5 + Mockk for unit testing (per tech stack)

**Security Requirements:**
- HTTPS-only API communication (TLS 1.3)
- Secure storage of tokens in encrypted DataStore
- Input validation on client side (Korean name format, required fields)
- Server-side validation on all API endpoints (prevent malformed data)
- Rate limiting on authentication endpoints (backend responsibility)
- SQL injection prevention via Prisma parameterized queries (backend)
- No sensitive data logged (PII, tokens excluded from logs)

**Performance Requirements:**
- Kakao login response time: <3 seconds (network dependent)
- Onboarding screen transitions: <300ms (smooth animations)
- Profile submission API call: <2 seconds (target p95)
- App cold start with authentication check: <3 seconds (per tech stack standard)
- Local data load (Room queries): <200ms
- Token refresh: <1.5 seconds (background, non-blocking)

**Error Handling Scenarios:**
- Kakao login failure (user cancellation, network error, Kakao API error)
- Network timeout during profile submission (retry with exponential backoff)
- Invalid input validation errors (display inline field errors)
- Backend API errors (4xx client errors, 5xx server errors with user-friendly messages)
- Token expiration and refresh failures (force re-authentication)
- Concurrent request handling (loading states prevent duplicate submissions)

**Analytics Events to Track:**
- `kakao_login_initiated` (user taps Kakao login button)
- `kakao_login_success` (OAuth flow completed successfully)
- `kakao_login_failure` (error type as parameter)
- `onboarding_started` (first screen shown)
- `onboarding_screen_viewed` (screen number as parameter)
- `onboarding_abandoned` (screen where user force-quit app)
- `onboarding_completed` (all screens viewed, profile submitted)
- `profile_submission_started`
- `profile_submission_success`
- `profile_submission_failure` (error type as parameter)
- `terms_accepted` (TOS and Privacy Policy checkboxes ticked)
- `session_token_refreshed`

**Testing Considerations:**
- Unit tests: LoginUseCase, RegisterUserUseCase, SaveProfileUseCase, input validation logic
- UI tests: Onboarding screen navigation, profile form validation, button states
- Integration tests: Kakao SDK mock integration, API endpoint calls with test server
- Manual testing: Real Kakao login on test accounts, Korean text input, network interruption scenarios
- Target coverage: 30-40% for Phase 1 (per tech stack standards)

**Compliance Requirements:**
- Terms of Service document (Korean language, legally reviewed)
- Privacy Policy document (PIPA compliant, Korean language)
- Deposit Policy document (clear refund/penalty rules, legally reviewed)
- User consent checkboxes (required before proceeding)
- Data privacy: User profile data handling per PIPA standards
- Payment disclosure: Inform users of manual deposit process for Phase 1

**Korean Market Specific Implementations:**
- Kakao Login is expected by Korean users (90%+ of smartphones have Kakao installed)
- Korean name validation regex: `^[가-힣]{2,4}$` (2-4 hangul characters)
- Industry dropdown order: Prioritize most common (스타트업, 대기업, 중소기업 first)
- Currency display: Use Korean won symbol (원) and comma separators (200,000원)
- Polite speech level: Use 존댓말 (formal polite) for all system messages
- Date format: YYYY년 MM월 DD일 (e.g., 2025년 11월 19일)
- Time format: 24-hour format with 시/분 (e.g., 14시 30분)

**Accessibility Baseline (MVP):**
- Content descriptions for important UI elements (login button, form fields)
- Proper heading hierarchy for screen readers
- Touch target sizes: Minimum 48dp (Material 3 standard)
- Color contrast ratios: WCAG AA compliance for text (4.5:1 for normal text)
- Form field labels properly associated with inputs
- Error messages announced to screen readers
- Loading states communicated to assistive technologies

**Handoff to Development:**
- Requires Figma designs for all 7 screens (login + 5 onboarding + main app entry)
- Requires backend API specification (OpenAPI/Swagger docs)
- Requires Kakao Developer account setup and app registration
- Requires Firebase project configuration with Kakao provider enabled
- Requires legal documents finalized (TOS, Privacy Policy, Deposit Policy)
- Requires Korean copywriting for all onboarding content (professionally written)
- Requires industry taxonomy final approval (stakeholder validation)

**Dependencies & Blockers:**
- Legal documents must be finalized before MVP launch (TOS, Privacy, Deposit Policy)
- Kakao Developer account approval (can take 2-3 business days)
- Firebase project setup with billing enabled (for API quotas)
- Backend API development must be parallel to mobile development
- Korean copywriting requires native Korean speaker (professional tone)
- Design mockups needed before implementation starts

**Migration & Data Considerations:**
- No data migration needed (greenfield project)
- User data schema must support future expansion (profile fields, preferences)
- Database design should allow for profile editing in Phase 2 (audit trail for changes)
- Consider soft delete pattern for future account deletion feature
- Token storage strategy must support future biometric auth (re-use refresh tokens)

**Monitoring & Observability:**
- Firebase Crashlytics for crash tracking (all unhandled exceptions)
- Firebase Analytics for user behavior events (onboarding funnel)
- Backend logging for authentication events (login success/failure, registration)
- Performance monitoring: Track Kakao login latency, API response times
- Error rate monitoring: Track authentication failures, API errors
- Weekly review of onboarding completion rates (success metrics)

**Assumptions & Risks:**

**Assumptions:**
- Users have Kakao accounts (90%+ smartphone penetration in Korea)
- Users have stable internet connection during onboarding (mobile data or WiFi)
- Backend API will be available and performant (<2s response time)
- Legal documents will be approved and published before launch
- Korean copywriting will be culturally appropriate and engaging
- Users will accept deposit system after seeing explanation

**Risks:**
- Kakao API downtime or rate limiting (mitigation: clear error messages, retry logic)
- Users abandoning onboarding due to length (mitigation: progress indicator, compelling content)
- Users confused by deposit system (mitigation: very clear explanation with examples, social proof)
- Backend API slow or unstable (mitigation: loading states, timeout handling, retry logic)
- Korean translation quality issues (mitigation: native Korean speaker review, user testing)
- Profile editing requests before Phase 2 (mitigation: support process, manual updates via admin)

**Success Criteria for MVP:**
- 70%+ of authenticated users complete onboarding (per roadmap target)
- <5% crash rate on onboarding flow
- <2% authentication failures (excluding user cancellation)
- Profile submission success rate >95%
- Avg time to complete onboarding: 3-5 minutes (user testing validation)
- User comprehension of deposit system: >70% (post-onboarding survey)

## Design Decisions Summary

### 1. Onboarding Flow: Mandatory One-Time
- **Decision:** Cannot skip; required before app access
- **Rationale:** Legal protection for deposit system (200K KRW), unfamiliar cohort model needs education, aligns with "Intentional Friction" design principle

### 2. Korean Localization: Full Market Adaptation
- **Decision:** Korean-only UI, hangul name validation, Korean industry taxonomy, KST formatting
- **Rationale:** 100% Korean user base, professional terminology alignment, cultural expectations for premium service

### 3. Session Management: Long-Lived with Refresh
- **Decision:** 30-day access tokens + 90-day refresh tokens with rotation
- **Rationale:** Mobile UX expectations, aligns with Kakao SDK patterns, prioritizes engagement over security paranoia in MVP

### 4. Onboarding Tone: Warm Professional with Dual Benefits
- **Decision:** Polite Korean (존댓말), lead with community value + reinforce with accountability
- **Rationale:** Matches "Warm & Approachable" design principle, respects professional context, appeals to both intrinsic and extrinsic motivation

### 5. Deposit Explanation: Progressive Disclosure (Screen 4 of 5)
- **Decision:** Transparent breakdown after community value established, emphasize refund over penalty
- **Rationale:** Build emotional buy-in first, legal clarity required, social proof builds confidence, manages sticker shock

### 6. MVP Scope: Kakao-Only, Profile-Locked, Manual Payment
- **Decision:** Single auth provider, no profile editing, deposit info only (payment is manual for Phase 1)
- **Rationale:** Ship faster, reduce complexity, align with Phase 1 roadmap constraints, validate core value before scaling features

All decisions optimize for MVP speed while ensuring legal compliance, cultural fit, and user comprehension of the unique deposit-penalty accountability model.

## Domain Model (Phase 1 Scope)

### Entities
- User: `id (UUID)`, `kakao_id (string)`, `created_at (timestamptz)`, `last_login_at (timestamptz)`, `status (active|inactive|locked)`
- UserProfile: `user_id (FK)`, `name (varchar 4)`, `role (varchar 64)`, `industry_code (enum)`, `growth_goals (text 100-500 chars)`, `photo_url (nullable)`, `onboarding_completed_at (timestamptz)`, `locale (kr)`, `profile_locked (boolean)`
- CohortCycle (placeholder for future link): `id`, `label`, `start_date`, `end_date`, `status`
- CohortMembership (Phase 2+): `id`, `user_id`, `cohort_cycle_id`, `joined_at`, `status (active|dropped)`
- TokenStore (server-side optional): `user_id`, `refresh_token_hash`, `expires_at`, `rotated_at`

### Industry Enum Codes
`STARTUP`, `ENTERPRISE`, `SME`, `PUBLIC`, `FOREIGN`, `FREELANCER`, `NONPROFIT`, `OTHER`

### Validation Rules
- Name: Hangul regex `^[\uac00-\ud7a3]{2,4}$`
- Role: 2-48 chars, allow Korean, English letters, space
- Growth Goals: 100-500 chars; enforce min to avoid shallow input
- Industry: must be one of enum codes
- Duplicate Kakao ID: reject registration (`409 CONFLICT`)

## API Contract (Phase 1)

### POST `/api/v1/auth/kakao` (Exchange Kakao token)
Request: `{ kakao_oauth_code: string }`
Response 200: `{ access_token, refresh_token, user_exists: boolean, onboarding_completed: boolean }`
Errors: `400 INVALID_CODE`, `502 KAKAO_UPSTREAM_ERROR`

### POST `/api/v1/users/profile`
Auth: Bearer access token
Body: `{ name, role, industry_code, growth_goals }`
Response 201: `{ user_id, profile: { ... }, onboarding_completed_at }`
Errors: `400 VALIDATION_ERROR`, `409 PROFILE_ALREADY_SET`, `401 UNAUTHORIZED`

### GET `/api/v1/users/me`
Response 200: `{ user: { id, kakao_id }, profile, flags: { onboarding_completed } }`

### POST `/api/v1/auth/refresh`
Body: `{ refresh_token }`
Response 200: `{ access_token, refresh_token }`
Errors: `401 TOKEN_INVALID`, `401 TOKEN_EXPIRED`, `409 TOKEN_ROTATION_CONFLICT`

### Error Response Format
`{ error: { code: string, message: string, details?: object } }`

## Security & Privacy Enhancements
- Encrypted DataStore (Android Keystore) for token pair
- Refresh token hashed server-side (bcrypt) before persistence (defense if DB leak)
- PII scope: name + growth goals (text); growth goals scanned for accidental sensitive data (manual review early)
- Logging: strip tokens; user_id only for auth logs

## Event Schema (Analytics Mapping)
| Event | Props | Phase |
|-------|-------|------|
| kakao_login_initiated | source=button | 1 |
| kakao_login_success | user_new (bool) | 1 |
| kakao_login_failure | reason | 1 |
| onboarding_started | entry_point (auto) | 1 |
| onboarding_screen_viewed | screen_index | 1 |
| onboarding_abandoned | last_screen_index | 1 |
| profile_submission_started | chars_growth_goals | 1 |
| profile_submission_success | industry_code | 1 |
| profile_submission_failure | error_code | 1 |
| terms_accepted | tos_version, privacy_version | 1 |
| session_token_refreshed | rotation (bool) | 1 |

Future (Phase 2+): `deposit_policy_viewed`, `deposit_acknowledged`.

## Acceptance Criteria (Key)
- AC1: User with valid Kakao code gets tokens and `user_exists=false` on first login.
- AC2: Profile POST rejects invalid hangul name with proper inline error.
- AC3: Growth goals <100 chars triggers validation message; cannot proceed.
- AC4: Refresh token rotation returns new pair; old refresh invalid thereafter.
- AC5: Profile cannot be overwritten (second POST returns `409 PROFILE_ALREADY_SET`).
- AC6: Onboarding flow persists completion; app relaunch goes directly to main feed.
- AC7: Analytics events fire once per screen visit (dedup on rapid back/forward blocked by no back navigation design).
- AC8: Tokens survive app kill; cold start auto refresh if access expired and refresh valid.

## Test Case Outline
- Unit: NameValidator, GrowthGoalsValidator, TokenRotator.
- Integration: Kakao mock exchange -> profile creation -> refresh rotation.
- UI Compose: Screen progression, disabled continue until valid form, error message visibility.
- Resilience: Network timeout on profile submission (retry 2x then fail with error toast).
- Security: Attempt to reuse old refresh token after rotation -> 401.

## Risks & Mitigations (Expanded)
| Risk | Impact | Mitigation |
|------|--------|-----------|
| Kakao rate limit | Auth blockage | Exponential backoff + user message + fallback status page |
| Low onboarding completion | Funnel leakage | Compress copy, add progress indicator, measure abandonment screen |
| Growth goals low quality | Weak cohort matching data later | Enforce min length + example tooltip |
| Token theft (rooted device) | Account misuse | Optionally add device binding Phase 2 + allow manual logout all sessions |
| Manual deposit confusion (Phase 1) | User distrust | Clear copy: manual email within 24h; SLA metric |

## Open Questions
- Should we soft-delete profile edits or maintain audit log when unlocks in Phase 2? (Pending ops decision)
- Need Terms versioning strategy (Header or DB table?)
- Do we pre-create user record before Kakao exchange or lazily on profile submit? (Current assumption: create on first login, mark `onboarding_completed=false`)

## Phase Gates Alignment
- Gate A (before Deposit automation): 65%+ onboarding completion, ≥70% comprehension of deposit (survey), refresh reliability error rate <2%.

## Success Metrics (Adjusted to Staged Targets)
- Onboarding completion (Cohort 1): ≥65%
- Profile submission success: ≥95%
- Average onboarding duration: 3–5 min
- Early reflection adoption correlation: ≥70% of onboarding-complete users start first reflection within 5 days (tracked later)

## Non-Functional Requirements
- Availability (auth endpoints): 99% during Phase 1 (soft target)
- Throughput: Support 50 concurrent logins (pilot) without degradation
- Latency: 95p auth exchange <1200ms server-side
- Storage: UserProfile row insert <50ms DB time

## Logging & Monitoring Detail
- Structured JSON logs: `timestamp, level, event, user_id, code, latency_ms`
- Alert rules: Kakao login failure ratio >10% in 15min window -> Slack alert.

## Future Extensibility Notes
- Industry taxonomy: store as enum now; consider separate table for user-defined industries later.
- Growth goals: plan semantic tagging (AI) Phase 5; keep text raw + avoid preprocessing that removes meaning.
- Token strategy: ready for adding biometric gating wrapper around sensitive endpoints.

## Copy Guidelines (Korean)
- Use concise sentences (<32 chars where possible for mobile width)
- Avoid loanwords where plain Korean acceptable unless job titles (e.g., "Product Manager")
- Tone: Polite, encouraging, non-salesy.

## Accessibility Enhancements (Planned Phases)
- Phase 2: Dynamic type scaling support.
- Phase 3: Screen reader audit + focus order refinement.
- Phase 4: High contrast theme toggle.

## Deviation Log
Any deviations from this spec (e.g., removing min length for growth goals) must be appended here with date, owner, rationale.
