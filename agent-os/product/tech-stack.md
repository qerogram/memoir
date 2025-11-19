# Tech Stack

## Mobile Platform

### Native Android Development
- **Language:** Kotlin (100% Kotlin codebase for type safety, null safety, and modern language features)
- **Minimum SDK:** API 26 (Android 8.0 Oreo) - covers 95%+ of active Android devices in Korea
- **Target SDK:** API 34 (Android 14) - latest stable Android version
- **Build System:** Gradle with Kotlin DSL for build configuration

### UI Framework & Architecture
- **UI Framework:** Jetpack Compose (modern declarative UI toolkit for native Android)
- **Material Design:** Material 3 (Material You) components with custom theming for Memoir brand colors
- **Architecture Pattern:** MVVM (Model-View-ViewModel) with Clean Architecture principles
- **State Management:** Kotlin StateFlow and Compose State for reactive UI updates
- **Navigation:** Jetpack Navigation Component with Compose integration for type-safe navigation

### Android Jetpack Libraries
- **Lifecycle:** Lifecycle-aware components (ViewModel, LiveData integration)
- **Room:** Local database for offline caching of reflections, comments, and user data
- **WorkManager:** Background task scheduling for sync operations and periodic notifications
- **DataStore:** Preferences storage using Protocol Buffers for user settings
- **Paging 3:** Efficient pagination for cohort feeds and member directories
- **Hilt:** Dependency injection framework built on Dagger for compile-time DI

### Networking & Data Layer
- **HTTP Client:** Retrofit with OkHttp for RESTful API communication
- **JSON Serialization:** Kotlinx Serialization for type-safe JSON parsing
- **Image Loading:** Coil (Kotlin-first image loading library) with caching and transformation support
- **API Architecture:** Repository pattern with data sources (remote API + local Room database)

## Backend Infrastructure

### Application Framework
- **Backend Framework:** Node.js with Express.js for RESTful API server
- **Language/Runtime:** Node.js v20 LTS with TypeScript for type safety
- **Package Manager:** npm for dependency management
- **API Design:** RESTful API with versioning (/api/v1/) and JSON responses

### Database & Storage
- **Primary Database:** PostgreSQL 15 (managed via AWS RDS or Supabase)
- **ORM:** Prisma for type-safe database queries and migrations
- **Caching Layer:** Redis for session management, API response caching, and rate limiting
- **File Storage:** AWS S3 for user-uploaded photos in reflections and profile images
- **CDN:** CloudFront for fast image delivery across Korea

### Authentication & Authorization
- **Authentication:** Firebase Authentication with phone number verification (Korean mobile carriers)
- **Session Management:** JWT tokens with refresh token rotation
- **Authorization:** Role-based access control (user, facilitator, admin roles)
- **Korean Phone Verification:** Integrate with Korean mobile carrier APIs (KT, SK Telecom, LG U+)

### Payment Processing
- **Primary Gateway:** Toss Payments for deposit collection, subscription billing, and refunds
- **Fallback Gateway:** KG Inicis for enterprise customers or alternative payment methods
- **Subscription Management:** Custom billing engine tracking 10-week cycles, penalties, and refunds
- **PG Integration:** RESTful API integration with webhook handling for payment confirmations

## Cloud Services & DevOps

### Hosting & Infrastructure
- **Backend Hosting:** AWS EC2 with Auto Scaling Group or AWS Elastic Beanstalk
- **Database Hosting:** AWS RDS PostgreSQL with automated backups and read replicas
- **Cache Hosting:** AWS ElastiCache for Redis
- **Alternative:** Supabase (PostgreSQL + Auth + Storage + Realtime) for faster initial deployment

### Push Notifications & Real-time
- **Push Notifications:** Firebase Cloud Messaging (FCM) for Android push notifications
- **Notification Scheduling:** Firebase Cloud Functions or AWS Lambda with CloudWatch Events
- **Real-time Updates:** WebSocket connections via Socket.io for live comment notifications (optional Phase 2)

### Analytics & Monitoring
- **Mobile Analytics:** Firebase Analytics for user behavior tracking and conversion funnels
- **Product Analytics:** Mixpanel for cohort analysis, retention metrics, and user segmentation
- **Crash Reporting:** Firebase Crashlytics for crash tracking and diagnostics
- **Backend Monitoring:** Sentry for error tracking and performance monitoring
- **Application Performance:** Firebase Performance Monitoring for network and app startup metrics
- **Logging:** CloudWatch Logs or LogDNA for centralized backend logging

### CI/CD & Development Tools
- **Version Control:** Git with GitHub for source code management
- **CI/CD Pipeline:** GitHub Actions for automated testing, building, and deployment
- **Android Build:** GitHub Actions workflow for APK/AAB generation with signing
- **Alternative CI/CD:** Bitrise (Android-specialized CI/CD platform) for mobile builds
- **API Testing:** Postman for API endpoint testing and documentation
- **Code Quality:** ktlint for Kotlin code formatting, detekt for static analysis

## Development & Design Tools

### Design & Prototyping
- **UI/UX Design:** Figma for screen mockups, design system, and developer handoff
- **Design System:** Figma components matching Material 3 with Memoir custom theming
- **Icon Set:** Material Icons with custom SVG icons for brand-specific elements
- **Typography:** Pretendard font family (optimized Korean web font)

### Local Development
- **IDE:** Android Studio (latest stable) with Kotlin plugin
- **Backend IDE:** Visual Studio Code with TypeScript, Prisma, and REST Client extensions
- **API Documentation:** Swagger/OpenAPI for backend API documentation
- **Database Client:** TablePlus or DBeaver for PostgreSQL database management
- **Emulators:** Android Emulator with Google Play Services for local testing

## Testing Strategy

### Android Testing
- **Unit Testing:** JUnit 5 with Mockk for mocking dependencies
- **UI Testing:** Jetpack Compose Testing framework for composable testing
- **Integration Testing:** Robolectric for fast JVM-based Android tests
- **End-to-End Testing:** Espresso for critical user flows (optional - CI/CD heavy)
- **Test Coverage:** JaCoCo for code coverage reporting (target 70%+ for business logic)

### Backend Testing
- **Unit Testing:** Jest for TypeScript unit tests
- **API Testing:** Supertest for HTTP endpoint testing
- **Database Testing:** In-memory PostgreSQL or test database for integration tests
- **Load Testing:** k6 or Artillery for performance testing payment and feed endpoints

## Third-Party Integrations

### Communication & Video
- **Video Calls:** Zoom SDK or Google Meet API for coffee chat video integration
- **In-App Chat:** (Future) SendBird or Stream Chat for direct messaging between members

### Calendar & Scheduling
- **Calendar Integration:** Android Calendar Provider for adding meetup events to device calendar
- **Timezone Handling:** Kotlinx-datetime for Korea Standard Time (KST) handling

### Email & Notifications
- **Transactional Email:** SendGrid for welcome emails, payment receipts, and weekly digests
- **Email Templates:** MJML for responsive email template generation

### Content Moderation (Future)
- **Text Moderation:** Google Cloud Natural Language API for toxic content detection
- **Image Moderation:** AWS Rekognition for inappropriate image detection in reflections

## Security & Compliance

### Security Measures
- **API Security:** HTTPS-only with SSL/TLS certificates via AWS Certificate Manager
- **Data Encryption:** AES-256 encryption for sensitive data at rest, TLS 1.3 for data in transit
- **Input Validation:** Server-side validation for all API inputs with sanitization
- **Rate Limiting:** Redis-based rate limiting on authentication and payment endpoints
- **SQL Injection Prevention:** Parameterized queries via Prisma ORM

### Compliance & Privacy
- **Data Privacy:** GDPR-compliant data handling with user data export/deletion features
- **Korean Privacy Law:** Compliance with Personal Information Protection Act (PIPA)
- **Payment Security:** PCI-DSS compliance via certified payment gateways (Toss Payments)
- **Terms of Service:** Legal framework for deposit-penalty system and user agreements

## Development Phases & Tooling Priorities (Reality-Adjusted)

### Phase 0 (Pilot Weeks 0–2)
- Mobile: Jetpack Compose + basic local persistence (in-memory / simple Room schema) for draft saving.
- Backend: Minimal Express + PostgreSQL (Reflection, Comment, CohortMembership tables only).
- Auth: Firebase Phone Auth.
- Ops: Admin script / simple dashboard for cohort assignment & manual penalty logging.
- Analytics: Firebase basic events (reflection_submitted, comment_posted) + manual Google Sheet augment.

### Phase 1 (MVP Weeks 3–12)
- Mobile Core: Compose + Retrofit + Room + Hilt + DataStore (preferences).
- Backend Core: Express + Prisma + S3 (images) + basic validation layer.
- Payments: Monthly subscription via Toss (no deposit automation yet).
- Notifications: FCM scheduled (Cloud Functions / simple cron Lambda).
- Testing: Unit tests (Kotlin/JUnit, Jest) for critical business logic; target 30–40% coverage.
- Monitoring: Crashlytics + basic server logging.

### Phase 2 (Accountability Months 4–5)
- Deposit Automation: Extend payment module (ledger + penalty calculator) BEFORE adding multiple gateways.
- Cohort Dashboard: Aggregation endpoints + Room caching.
- Paging 3 introduction for feed scaling.
- Meetup Scheduling: Basic events persisted in DB (no external calendar sync yet).
- Security Hardening: Rate limiting (Redis optional; can simulate in-memory if load low).

### Phase 3 (Social Layer Months 6–8)
- Coffee Chat Matching: Simple query-based matching (no ML) + scheduling via internal endpoints.
- Email: SendGrid integration (welcome, weekly digest, penalty notices).
- Clubs Beta: Reuse feed/comment models with type discriminator.
- Analytics Expansion: Mixpanel integration (cohort retention funnels) post event schema stabilization.
- Testing: Raise coverage to ~55–60%; introduce component tests for Compose UI.

### Phase 4 (Scale & Reliability Months 9–10)
- Performance: Redis cache for frequent feed queries only if P95 >1.5s; CloudFront CDN for images.
- Monitoring: Sentry backend + performance metrics; structured logging (JSON) for penalty audits.
- Optimizations: R8 shrinking, image compression pipeline.
- Resilience: Automated penalty appeal workflow + idempotent payment handlers.

### Phase 5 (Advanced Insights Months 11–12)
- AI Layer: GPT-based summarization & theme extraction (opt-in) after ≥3,000 reflections; prompt engineering with privacy filters.
- Offline Enhancement: Conflict resolution strategy (last-write + merge suggestions for text areas).
- Growth Dashboards: Charting (client-side MPAndroidChart) pulling aggregated endpoints.

### Deferred / Conditional
- WebSockets (Socket.io): Only after polling proves insufficient (comment latency complaints or active concurrent editing > threshold).
- Multiple Payment Gateways (KG Inicis/PortOne): Triggered by >15% payment failure or alternative method demand.
- Video Reflections / MediaConvert: Post evidence that multimedia boosts engagement (A/B with image-only).
- Real-time DM (SendBird/Stream): After community saturation & explicit demand; ensure moderation tooling first.

### Ops & Admin Tooling Evolution
- v0: Scripts + minimal dashboard for status & penalties.
- v1 (Phase 2): Web UI for cohort status, manual overrides, refund export (CSV).
- v2 (Phase 4): Role-based access, audit logs, penalty appeal queue.

### Compliance & Privacy Steps
- TOS / Deposit Policy finalized Phase 1, reviewed Phase 2 before automation.
- Pseudonymization routine (export reflections without personal identifiers) before AI processing.
- Data Deletion & Export endpoints implemented Phase 3.

### Testing Maturity Targets
- Business Logic Coverage: 30% → 55–60% → 70% (Phase 4) for penalty & payment critical paths.
- Performance Tests: k6 scenarios for feed & penalty deduction endpoints start Phase 3.

### Measurement & Event Schema (Incremental)
- Base: reflection_started, reflection_submitted, comment_posted, missed_deadline.
- Phase 2 Add: penalty_assessed, meetup_rsvp, meetup_attended.
- Phase 3 Add: coffee_chat_requested, coffee_chat_completed, club_joined.
- Phase 5 Add: ai_summary_generated (with consent flag).
