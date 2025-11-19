# Memoir Android App

Memoir is a weekly reflection community platform for Korean professionals. This Android app enables users to participate in 10-week cohorts, write weekly reflections, and receive peer feedback.

## Project Status

**Phase 1 MVP - User Authentication & Onboarding** ✅ In Progress

This is the foundational feature implementing:
- Kakao Login authentication
- 5-screen mandatory onboarding flow
- Profile setup with Korean localization
- Session management with JWT tokens

## Tech Stack

### Frontend (Android)
- **Language:** Kotlin 100%
- **UI:** Jetpack Compose with Material 3
- **Architecture:** MVVM + Clean Architecture
- **DI:** Hilt
- **Networking:** Retrofit + OkHttp + Kotlinx Serialization
- **Local Storage:** Room + DataStore (encrypted)
- **Authentication:** Kakao SDK
- **Analytics:** Firebase Analytics + Crashlytics
- **Image Loading:** Coil

### Backend
- Express.js + TypeScript (separate repository)
- PostgreSQL with Prisma ORM
- Firebase Authentication with Kakao provider

## Project Structure

```
app/src/main/java/com/memoir/app/
├── data/               # Data layer
│   ├── local/         # Room database + DataStore
│   ├── remote/        # API interfaces + DTOs
│   └── repository/    # Repository implementations
├── domain/            # Business logic layer
│   ├── model/         # Domain models
│   ├── repository/    # Repository interfaces
│   └── usecase/       # Use cases
├── presentation/      # UI layer
│   ├── auth/          # Login screen
│   ├── onboarding/    # Onboarding screens 1-4
│   ├── profile/       # Profile setup screen
│   ├── feed/          # Cohort feed (placeholder)
│   ├── navigation/    # Navigation graph
│   ├── components/    # Reusable UI components
│   └── ui/theme/      # Material3 theme
├── di/                # Hilt dependency injection modules
└── util/              # Utilities (validation, constants, Result)
```

## Setup Instructions

### Prerequisites
1. Android Studio Hedgehog or later
2. Kotlin 1.9+
3. Java 17+
4. Kakao Developer account

### Configuration

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd memoir
   ```

2. **Set up Kakao SDK**
   - Create an app on [Kakao Developers Console](https://developers.kakao.com)
   - Get your Kakao App Key
   - Add to `local.properties`:
     ```properties
     kakao.app.key=YOUR_KAKAO_APP_KEY_HERE
     ```

3. **Set up Firebase**
   - Create a Firebase project
   - Download `google-services.json` (replace the placeholder)
   - Enable Firebase Analytics and Crashlytics
   - Configure Kakao authentication provider in Firebase Console

4. **Configure Backend API**
   - Update `Constants.API_BASE_URL` in `app/src/main/java/com/memoir/app/util/Constants.kt`
   - Ensure backend is running and accessible

5. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

## Features

### Phase 1 (Current)
- ✅ Kakao Login integration
- ✅ Mandatory 5-screen onboarding flow
- ✅ Profile setup (name, role, industry, growth goals)
- ✅ Korean localization (한글 전용)
- ✅ Session management with token refresh
- ✅ Input validation (Korean name, growth goals length)

### Phase 2 (Planned)
- Profile editing
- Photo upload
- Biometric re-authentication
- Advanced analytics

## Design System

**Memoir Design Principles:**
- Minimalist & Distraction-Free
- Warm & Approachable
- Mobile-First

**Color Palette:**
- Primary: Mustard `#F4BA54`
- Accent: Carrot Orange `#E86221`
- Dark: Dark Olive `#124234`

**Typography:**
- Font: Pretendard (system default on Korean devices)
- All text in Korean (존댓말 - polite speech)

## Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

Test coverage target: 30-40% for Phase 1 MVP

## Korean Localization

- All UI text in Korean (no English fallback)
- Korean name validation: `^[가-힣]{2,4}$`
- Industry taxonomy aligned with Korean job market
- Currency formatting: `200,000원`
- Date/time: KST (UTC+9)

## API Endpoints

- `POST /api/v1/auth/kakao` - Exchange Kakao OAuth code for tokens
- `POST /api/v1/auth/refresh` - Refresh access token
- `GET /api/v1/users/me` - Get current user profile
- `POST /api/v1/users/profile` - Submit user profile

## Security

- HTTPS-only communication (TLS 1.3)
- Encrypted token storage using Android Keystore
- Client + server-side input validation
- No PII in logs or crash reports
- Token rotation on refresh

## Development Guidelines

See `agent-os/standards/` for detailed coding standards:
- `coding-style.md` - Kotlin coding conventions
- `error-handling.md` - Error handling patterns
- `validation.md` - Input validation standards
- `test-writing.md` - Testing strategy

## Documentation

- **Spec:** `agent-os/specs/2025-11-19-user-authentication-onboarding/spec.md`
- **Tasks:** `agent-os/specs/2025-11-19-user-authentication-onboarding/tasks.md`
- **Requirements:** `agent-os/specs/2025-11-19-user-authentication-onboarding/planning/requirements.md`

## License

Proprietary - All rights reserved

## Support

For questions or issues, contact the development team.
