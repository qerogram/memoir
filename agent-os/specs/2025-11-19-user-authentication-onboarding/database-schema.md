# Database Schema Design: User Authentication & Onboarding

## Executive Summary

This document specifies the PostgreSQL 15 database schema for the Memoir Phase 1 MVP user authentication and onboarding system. The design supports Kakao OAuth integration, user profile management, session token storage, and authentication audit logging. The schema is optimized for 100+ concurrent users with <50ms query response times and includes comprehensive indexing, referential integrity constraints, and compliance audit trails.

**Key Characteristics:**
- PostgreSQL 15 with Prisma 5.x ORM
- 4 core tables: Users, UserProfiles, RefreshTokenStore, AuthenticationLogs
- UUID primary keys with timestamp-based sequencing
- Enum-driven categorical data (UserStatus, IndustryCode, EventType)
- Foreign key cascading for referential integrity
- Strategic indexing on frequently queried columns
- Built-in audit timestamps (createdAt, updatedAt)
- Security-first approach with bcrypt-hashed refresh tokens

---

## 1. Entity-Relationship Diagram (ERD)

```
┌─────────────────────────┐
│        users            │
├─────────────────────────┤
│ id (PK, UUID)          │
│ kakao_id (UNIQUE)      │
│ created_at (TIMESTAMPTZ│
│ last_login_at          │
│ status (ENUM)          │
└─────────────────────────┘
          │
          │ 1:1
          │ (CASCADE)
          ├─────────────────────────────┐
          │                             │
          ▼                             ▼
┌─────────────────────────┐   ┌──────────────────────────┐
│   user_profiles         │   │ refresh_token_store      │
├─────────────────────────┤   ├──────────────────────────┤
│ id (PK, UUID)          │   │ id (PK, UUID)           │
│ user_id (FK, UNIQUE)   │   │ user_id (FK)            │
│ name (VARCHAR 8)       │   │ token_hash (VARCHAR 256)│
│ role (VARCHAR 64)      │   │ rotation_counter        │
│ industry_code (ENUM)   │   │ expires_at (TIMESTAMPTZ)
│ growth_goals (TEXT)    │   │ created_at (TIMESTAMPTZ)
│ profile_photo_url      │   │ revoked_at (nullable)   │
│ onboarding_completed_at│   │                         │
│ profile_locked (BOOL)  │   │ Index: (user_id, exp...)
│ created_at (TIMESTAMPTZ│   │ Index: (token_hash)     │
│ updated_at (TIMESTAMPTZ│   └──────────────────────────┘
└─────────────────────────┘

          │ 1:N
          │ (SET NULL)
          │
          ▼
┌──────────────────────────┐
│ authentication_logs      │
├──────────────────────────┤
│ id (PK, UUID)           │
│ user_id (FK, nullable)  │
│ event_type (ENUM)       │
│ ip_address (INET)       │
│ user_agent (TEXT)       │
│ error_code (VARCHAR 64) │
│ created_at (TIMESTAMPTZ)│
│ Index: (user_id, created_at)
│ Index: (created_at)     │
└──────────────────────────┘
```

---

## 2. Table Schema Design

### 2.1 `users` Table

**Purpose:** Core user account table linked to Kakao OAuth. Stores minimal authentication metadata.

```sql
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  kakao_id VARCHAR(255) NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_login_at TIMESTAMPTZ,
  status ENUM ('ACTIVE', 'INACTIVE', 'LOCKED') NOT NULL DEFAULT 'ACTIVE'
);

-- Indexes
CREATE UNIQUE INDEX idx_users_kakao_id ON users(kakao_id);
CREATE INDEX idx_users_created_at ON users(created_at DESC);
```

**Columns:**
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Auto-generated unique identifier using `gen_random_uuid()` |
| `kakao_id` | VARCHAR(255) | NOT NULL, UNIQUE | Kakao OAuth provider ID; used for first-login duplicate detection |
| `created_at` | TIMESTAMPTZ | NOT NULL, DEFAULT now() | Account creation timestamp (UTC+0 in DB, convert to KST on app) |
| `last_login_at` | TIMESTAMPTZ | NULL | Timestamp of most recent successful authentication; null until first login |
| `status` | ENUM | NOT NULL, DEFAULT 'ACTIVE' | Account state: ACTIVE (normal), INACTIVE (user-deactivated, Phase 3), LOCKED (abuse/violation) |

**Rationale:**
- UUID for distributed system compatibility and privacy (harder to enumerate user IDs)
- `kakao_id` UNIQUE ensures one Kakao account = one user record (prevents duplicate registrations)
- `status` enum prevents invalid state values and enables future account suspension without deleting data
- Timestamps in TIMESTAMPTZ (UTC) for server consistency; app converts to KST for display
- Minimal column set reduces storage and JOIN complexity

**Data Lifecycle:**
- Record created on first Kakao OAuth success
- `last_login_at` updated on every successful login (token refresh does not update)
- Soft-delete pattern: set `status=INACTIVE` for user-requested deletions (Phase 3 per PIPA requirements)
- Never physically delete; audit trail preserved for compliance

---

### 2.2 `user_profiles` Table

**Purpose:** Extended user profile data collected during mandatory onboarding. Linked 1:1 to users.

```sql
CREATE TABLE user_profiles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL UNIQUE,
  name VARCHAR(8) NOT NULL,
  role VARCHAR(64) NOT NULL,
  industry_code VARCHAR(32) NOT NULL,
  growth_goals TEXT NOT NULL,
  profile_photo_url VARCHAR(512),
  onboarding_completed_at TIMESTAMPTZ NOT NULL,
  profile_locked BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_user_profiles_user_id
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes
CREATE UNIQUE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_created_at ON user_profiles(created_at DESC);
CREATE INDEX idx_user_profiles_industry_code ON user_profiles(industry_code);
```

**Columns:**
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique profile identifier |
| `user_id` | UUID | NOT NULL, UNIQUE, FK | Foreign key to users table; 1:1 relationship; cascade delete |
| `name` | VARCHAR(8) | NOT NULL | Korean name (2-4 hangul characters); validated server-side regex `^[\uac00-\ud7a3]{2,4}$` |
| `role` | VARCHAR(64) | NOT NULL | Professional title/role; accepts Korean (hangul) and English; examples: "Product Manager", "개발자", "디자이너" |
| `industry_code` | VARCHAR(32) | NOT NULL | Categorical code (see Enum section); stores STARTUP, ENTERPRISE, SME, PUBLIC, FOREIGN, FREELANCER, NONPROFIT, OTHER |
| `growth_goals` | TEXT | NOT NULL | Personal growth aspirations (100-500 characters); free text for future semantic analysis; example: "스타트업 문화 이해, 리더십 스킬 개발" |
| `profile_photo_url` | VARCHAR(512) | NULL | S3 or CDN URL for avatar; phase 2 feature (optional during MVP); placeholder avatar used if null |
| `onboarding_completed_at` | TIMESTAMPTZ | NOT NULL | Timestamp when user completed all 5 onboarding screens and submitted profile; triggers app navigation to main feed |
| `profile_locked` | BOOLEAN | NOT NULL, DEFAULT true | Controls whether user can edit profile; locked=true until Phase 2 edit feature (enforces thoughtful initial setup) |
| `created_at` | TIMESTAMPTZ | NOT NULL, DEFAULT now() | Record creation timestamp |
| `updated_at` | TIMESTAMPTZ | NOT NULL, DEFAULT now() | Last modification timestamp; Prisma auto-updates on each save |

**Validation Rules:**
- `name`: Server-side regex enforcement + client-side hangul input method
  - Valid: "김철수" (2-4 chars), "정순신" (4 chars)
  - Invalid: "김" (1 char), "김철수박" (5 chars), "Kim Chulsu" (English)
- `role`: 2-48 characters; allow hangul, English, space; strip leading/trailing whitespace
- `industry_code`: Must match enum values; database-level check via foreign key (future) or app validation
- `growth_goals`: Min 100 chars (enforces thoughtful input), max 500 chars (reasonable bounds for UX/storage)

**Rationale:**
- `user_id` UNIQUE ensures 1:1 profile:user mapping
- Locked profile prevents unintended changes; can edit only via admin or Phase 2 unlock feature
- `onboarding_completed_at` serves as completion flag; nullable initially, set on profile submission
- TEXT for growth_goals allows future ML/semantic tagging without preprocessing
- Separate table from users allows optional profile extension in future cohorts/preferences tables

**Data Lifecycle:**
- Created immediately after user submits profile (5th onboarding screen)
- `onboarding_completed_at` set to submission timestamp; never null after creation
- `updated_at` remains creation timestamp until Phase 2 edit feature
- Cascading delete: if user deleted, profile auto-deleted (preserves referential integrity)

---

### 2.3 `refresh_token_store` Table

**Purpose:** Server-side refresh token storage for stateful session management. Enables token rotation, revocation, and security auditing.

```sql
CREATE TABLE refresh_token_store (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  token_hash VARCHAR(256) NOT NULL,
  rotation_counter INTEGER NOT NULL DEFAULT 0,
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  revoked_at TIMESTAMPTZ,
  CONSTRAINT fk_refresh_token_user_id
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT check_rotation_counter CHECK (rotation_counter >= 0)
);

-- Indexes for efficient token validation and cleanup
CREATE INDEX idx_refresh_token_user_id_expires ON refresh_token_store(user_id, expires_at DESC);
CREATE INDEX idx_refresh_token_hash ON refresh_token_store(token_hash);
CREATE INDEX idx_refresh_token_expires_at ON refresh_token_store(expires_at);
```

**Columns:**
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique record identifier |
| `user_id` | UUID | NOT NULL, FK | Foreign key to users; enables per-user token lifecycle queries |
| `token_hash` | VARCHAR(256) | NOT NULL | bcrypt-hashed refresh token (cost=12) received from client; never store plaintext token |
| `rotation_counter` | INTEGER | NOT NULL, DEFAULT 0 | Tracks rotations for security monitoring; increments each refresh; detects token reuse attacks |
| `expires_at` | TIMESTAMPTZ | NOT NULL | Token absolute expiry (90 days from issuance for MVP); enables scheduled cleanup |
| `created_at` | TIMESTAMPTZ | NOT NULL, DEFAULT now() | Token issuance timestamp |
| `revoked_at` | TIMESTAMPTZ | NULL | Logout/revocation timestamp; null if active; if set, token is invalid (used for "logout all sessions") |

**Token Rotation Mechanism:**
1. Client presents `old_refresh_token` + `old_hash` to `/auth/refresh` endpoint
2. Server verifies: hash matches record, `revoked_at` null, current time < `expires_at`
3. If valid:
   - Generate new `access_token` (30-day expiry)
   - Generate new `refresh_token`
   - Create new `refresh_token_store` row with `rotation_counter+1`
   - Mark old record: `revoked_at = now()` (soft revocation)
4. Client caches new pair; old pair is now invalid
5. Replay attack: if client attempts old token after rotation, server finds `revoked_at != null` → 401 Unauthorized

**Rationale:**
- Bcrypt hashing prevents token leak if DB compromised (no plaintext refresh tokens)
- Soft revocation via `revoked_at` enables audit trail; queries exclude revoked tokens
- `rotation_counter` enables fraud detection (e.g., alert if counter jumps unexpectedly)
- Compound index `(user_id, expires_at)` optimizes "find all active tokens for user" queries (logout all)
- Expiration-based cleanup: scheduled jobs DELETE rows where `expires_at < now()` (configurable TTL)

**Data Lifecycle:**
- Created at successful Kakao OAuth exchange or token refresh
- `revoked_at` set on user logout (explicit) or manual session revocation (admin)
- Auto-cleanup: PostgreSQL TTL via periodic `DELETE WHERE expires_at < now() - interval '7 days'` (grace period)
- Cascading delete: if user deleted, all token records auto-deleted

**Security Implications:**
- Long-lived tokens (90 days) optimize mobile UX; mitigated by refresh rotation and device binding (Phase 2)
- Bcrypt cost=12 adds ~250ms per hash computation (acceptable for <10 logins/day per user); prevents offline cracking
- `rotation_counter` limits damage if token leaked (old tokens silently rejected)
- No rotation required on app kill (unlike some implementations); user re-authenticates on app reopen if access token expired

---

### 2.4 `authentication_logs` Table

**Purpose:** Immutable audit trail for authentication events. Enables security monitoring, compliance reporting, and troubleshooting.

```sql
CREATE TABLE authentication_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID,
  event_type VARCHAR(64) NOT NULL,
  ip_address INET,
  user_agent TEXT,
  error_code VARCHAR(64),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_auth_logs_user_id
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes for querying and compliance reporting
CREATE INDEX idx_auth_logs_user_id_created ON authentication_logs(user_id, created_at DESC);
CREATE INDEX idx_auth_logs_created_at ON authentication_logs(created_at DESC);
CREATE INDEX idx_auth_logs_event_type ON authentication_logs(event_type);
```

**Columns:**
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique log entry identifier |
| `user_id` | UUID | NULL, FK | Foreign key to users; null for pre-auth failures (Kakao errors, invalid codes); SET NULL on user delete |
| `event_type` | VARCHAR(64) | NOT NULL | Enum-style code: LOGIN_SUCCESS, LOGIN_FAILURE, KAKAO_ERROR, TOKEN_REFRESH, LOGOUT, PROFILE_CREATED |
| `ip_address` | INET | NULL | Client IP address (IPv4 or IPv6); null for server-initiated events; used for geographic anomaly detection (Phase 3) |
| `user_agent` | TEXT | NULL | HTTP User-Agent header; identifies device/app version for debugging; preserved for security analysis |
| `error_code` | VARCHAR(64) | NULL | Application error code (e.g., "INVALID_CODE", "KAKAO_TIMEOUT", "TOKEN_EXPIRED"); null for success events |
| `created_at` | TIMESTAMPTZ | NOT NULL, DEFAULT now() | Event timestamp (UTC); enables time-series analysis |

**Event Types:**
| Event Type | Trigger | Typical Columns | Use Case |
|------------|---------|-----------------|----------|
| LOGIN_SUCCESS | Kakao OAuth → token issued | user_id, ip_address, user_agent | Success rate tracking, user login frequency |
| LOGIN_FAILURE | Invalid OAuth code or Kakao error | user_id (nullable), ip_address, error_code | Failed login investigation, brute-force detection |
| KAKAO_ERROR | Kakao API unreachable/timeout | ip_address, error_code | Service health monitoring |
| TOKEN_REFRESH | Successful refresh token rotation | user_id, ip_address | Token rotation audit |
| LOGOUT | Explicit logout or session revocation | user_id | Session lifecycle tracking |
| PROFILE_CREATED | User submits onboarding profile | user_id | Onboarding completion audit |

**Rationale:**
- Immutable table (no UPDATE allowed; DELETE only for retention policies)
- `user_id` nullable enables logging pre-auth failures (e.g., Kakao OAuth errors)
- `ip_address` INET type supports both IPv4 and IPv6; enables geo-blocking (Phase 2)
- `created_at` DESC index enables efficient "recent events" queries for dashboards
- Compound `(user_id, created_at)` index optimizes "user login history" queries
- Retention policy: auto-delete entries >90 days old (configurable) to manage storage

**Compliance & Security:**
- No PII in logs (never log name, email, growth_goals); user_id only
- Tokens never logged (access_token, refresh_token, kakao_code excluded)
- IP address logged for security analysis but sensitive; consider pseudonymization (Phase 3)
- Supports PIPA audit requirements: who logged in, when, from where, success/failure

**Data Lifecycle:**
- Inserted on every authentication-related event (real-time, fire-and-forget)
- Immutable; no updates
- Auto-cleanup: scheduled job `DELETE WHERE created_at < now() - interval '90 days'`
- Cascading SET NULL: if user deleted, user_id becomes null but log entry preserved

---

## 3. Enums (Categorical Data)

### 3.1 UserStatus

```sql
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'LOCKED');
```

| Value | Use Case | Notes |
|-------|----------|-------|
| ACTIVE | Normal operation | Default state for all new users |
| INACTIVE | User-deactivated account | Phase 3: User requests account pause; they can reactivate within 30 days |
| LOCKED | Admin suspension | Abuse/violation; requires admin intervention to unlock |

### 3.2 IndustryCode

```sql
CREATE TYPE industry_code AS ENUM (
  'STARTUP',
  'ENTERPRISE',
  'SME',
  'PUBLIC',
  'FOREIGN',
  'FREELANCER',
  'NONPROFIT',
  'OTHER'
);
```

| Code | Korean | Use Case |
|------|--------|----------|
| STARTUP | 스타트업 | Early-stage companies, venture-backed |
| ENTERPRISE | 대기업 | Large Korean conglomerates (Samsung, LG, Hyundai) |
| SME | 중소기업 | Small-to-medium enterprises |
| PUBLIC | 공기업/공공기관 | Government agencies, state-owned companies |
| FOREIGN | 외국계 기업 | Multinational corporations |
| FREELANCER | 프리랜서/1인 기업 | Self-employed, independent contractors |
| NONPROFIT | 비영리/사회적 기업 | NGOs, social enterprises |
| OTHER | 기타 | Uncategorized industries |

**Rationale:**
- Aligns with Korean job market terminology (user familiarity)
- Enables future cohort matching by industry (networking, shared context)
- Enum prevents invalid values at DB level
- Fixed set reduces cardinality for analytics aggregation

### 3.3 EventType

```sql
CREATE TYPE event_type AS ENUM (
  'LOGIN_SUCCESS',
  'LOGIN_FAILURE',
  'KAKAO_ERROR',
  'TOKEN_REFRESH',
  'LOGOUT',
  'PROFILE_CREATED'
);
```

(See authentication_logs section for details)

---

## 4. Prisma Schema Definition

**File:** `/prisma/schema.prisma`

```prisma
// https://www.prisma.io/docs/reference/api-reference/prisma-schema-reference

datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
}

generator client {
  provider = "prisma-client-js"
}

// Core user account table
model User {
  id          String   @id @default(cuid())
  kakaoId     String   @unique @db.VarChar(255)
  profile     UserProfile?
  refreshTokens RefreshTokenStore[]
  authLogs    AuthenticationLog[]
  createdAt   DateTime @default(now()) @db.Timestamptz()
  lastLoginAt DateTime? @db.Timestamptz()
  status      UserStatus @default(ACTIVE)

  @@index([createdAt(sort: Desc)])
  @@map("users")
}

enum UserStatus {
  ACTIVE
  INACTIVE
  LOCKED
}

// User profile collected during onboarding
model UserProfile {
  id                    String   @id @default(cuid())
  userId                String   @unique
  user                  User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  name                  String   @db.VarChar(8)
  role                  String   @db.VarChar(64)
  industryCode          IndustryCode
  growthGoals           String   @db.Text
  profilePhotoUrl       String?  @db.VarChar(512)
  onboardingCompletedAt DateTime @db.Timestamptz()
  profileLocked         Boolean  @default(true)
  createdAt             DateTime @default(now()) @db.Timestamptz()
  updatedAt             DateTime @updatedAt @db.Timestamptz()

  @@index([createdAt(sort: Desc)])
  @@index([industryCode])
  @@map("user_profiles")
}

enum IndustryCode {
  STARTUP
  ENTERPRISE
  SME
  PUBLIC
  FOREIGN
  FREELANCER
  NONPROFIT
  OTHER
}

// Refresh token store for stateful session management
model RefreshTokenStore {
  id              String   @id @default(cuid())
  userId          String
  user            User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  tokenHash       String   @db.VarChar(256)
  rotationCounter Int      @default(0)
  expiresAt       DateTime @db.Timestamptz()
  createdAt       DateTime @default(now()) @db.Timestamptz()
  revokedAt       DateTime? @db.Timestamptz()

  @@index([userId, expiresAt(sort: Desc)])
  @@index([tokenHash])
  @@index([expiresAt])
  @@map("refresh_token_store")
}

// Audit trail for authentication events
model AuthenticationLog {
  id        String   @id @default(cuid())
  userId    String?
  user      User?    @relation(fields: [userId], references: [id], onDelete: SetNull)
  eventType EventType
  ipAddress String?  @db.Inet()
  userAgent String?  @db.Text
  errorCode String?  @db.VarChar(64)
  createdAt DateTime @default(now()) @db.Timestamptz()

  @@index([userId, createdAt(sort: Desc)])
  @@index([createdAt(sort: Desc)])
  @@index([eventType])
  @@map("authentication_logs")
}

enum EventType {
  LOGIN_SUCCESS
  LOGIN_FAILURE
  KAKAO_ERROR
  TOKEN_REFRESH
  LOGOUT
  PROFILE_CREATED
}
```

**Key Design Decisions:**

1. **CUID vs UUID:** Using `@default(cuid())` for shorter, collision-resistant IDs; compatible with distributed systems
2. **@db.Timestamptz():** All timestamps in UTC (timezone-aware); app layer converts to KST for display
3. **Cascade Relationships:**
   - User → Profile: `onDelete: Cascade` (delete user = delete profile)
   - User → RefreshTokens: `onDelete: Cascade` (logout = delete all tokens)
   - User → AuthLogs: `onDelete: SetNull` (delete user = preserve logs with null user_id)
4. **Enum Types:** Prisma enums validate at ORM level + PostgreSQL ENUM type for DB-level constraints
5. **Indexes:** Defined in Prisma for auto-migration generation; follows query patterns (see Section 6)

---

## 5. Migration Strategy

### 5.1 Initial Schema Migration

**File:** `/prisma/migrations/YYYYMMDDHHMMSS_init/migration.sql`

Example: `/prisma/migrations/20250119120000_init/migration.sql`

```sql
-- CreateEnum for UserStatus
CREATE TYPE "UserStatus" AS ENUM ('ACTIVE', 'INACTIVE', 'LOCKED');

-- CreateEnum for IndustryCode
CREATE TYPE "IndustryCode" AS ENUM ('STARTUP', 'ENTERPRISE', 'SME', 'PUBLIC', 'FOREIGN', 'FREELANCER', 'NONPROFIT', 'OTHER');

-- CreateEnum for EventType
CREATE TYPE "EventType" AS ENUM ('LOGIN_SUCCESS', 'LOGIN_FAILURE', 'KAKAO_ERROR', 'TOKEN_REFRESH', 'LOGOUT', 'PROFILE_CREATED');

-- CreateTable users
CREATE TABLE "users" (
    "id" TEXT NOT NULL,
    "kakaoId" VARCHAR(255) NOT NULL,
    "createdAt" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "lastLoginAt" TIMESTAMPTZ,
    "status" "UserStatus" NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT "users_pkey" PRIMARY KEY ("id")
);

-- CreateIndex for kakaoId (unique)
CREATE UNIQUE INDEX "users_kakaoId_key" ON "users"("kakaoId");

-- CreateIndex for createdAt
CREATE INDEX "users_createdAt_idx" ON "users"("createdAt" DESC);

-- CreateTable user_profiles
CREATE TABLE "user_profiles" (
    "id" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "name" VARCHAR(8) NOT NULL,
    "role" VARCHAR(64) NOT NULL,
    "industryCode" "IndustryCode" NOT NULL,
    "growthGoals" TEXT NOT NULL,
    "profilePhotoUrl" VARCHAR(512),
    "onboardingCompletedAt" TIMESTAMPTZ NOT NULL,
    "profileLocked" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "user_profiles_pkey" PRIMARY KEY ("id")
);

-- CreateIndex for userId (unique)
CREATE UNIQUE INDEX "user_profiles_userId_key" ON "user_profiles"("userId");

-- CreateIndex for createdAt
CREATE INDEX "user_profiles_createdAt_idx" ON "user_profiles"("createdAt" DESC);

-- CreateIndex for industryCode
CREATE INDEX "user_profiles_industryCode_idx" ON "user_profiles"("industryCode");

-- AddForeignKey: user_profiles.userId -> users.id
ALTER TABLE "user_profiles" ADD CONSTRAINT "user_profiles_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- CreateTable refresh_token_store
CREATE TABLE "refresh_token_store" (
    "id" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "tokenHash" VARCHAR(256) NOT NULL,
    "rotationCounter" INTEGER NOT NULL DEFAULT 0,
    "expiresAt" TIMESTAMPTZ NOT NULL,
    "createdAt" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "revokedAt" TIMESTAMPTZ,

    CONSTRAINT "refresh_token_store_pkey" PRIMARY KEY ("id")
);

-- CreateIndex for (userId, expiresAt)
CREATE INDEX "refresh_token_store_userId_expiresAt_idx" ON "refresh_token_store"("userId", "expiresAt" DESC);

-- CreateIndex for tokenHash
CREATE INDEX "refresh_token_store_tokenHash_idx" ON "refresh_token_store"("tokenHash");

-- CreateIndex for expiresAt (cleanup queries)
CREATE INDEX "refresh_token_store_expiresAt_idx" ON "refresh_token_store"("expiresAt");

-- AddForeignKey: refresh_token_store.userId -> users.id
ALTER TABLE "refresh_token_store" ADD CONSTRAINT "refresh_token_store_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- CreateTable authentication_logs
CREATE TABLE "authentication_logs" (
    "id" TEXT NOT NULL,
    "userId" TEXT,
    "eventType" "EventType" NOT NULL,
    "ipAddress" INET,
    "userAgent" TEXT,
    "errorCode" VARCHAR(64),
    "createdAt" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "authentication_logs_pkey" PRIMARY KEY ("id")
);

-- CreateIndex for (userId, createdAt)
CREATE INDEX "authentication_logs_userId_createdAt_idx" ON "authentication_logs"("userId", "createdAt" DESC);

-- CreateIndex for createdAt
CREATE INDEX "authentication_logs_createdAt_idx" ON "authentication_logs"("createdAt" DESC);

-- CreateIndex for eventType
CREATE INDEX "authentication_logs_eventType_idx" ON "authentication_logs"("eventType");

-- AddForeignKey: authentication_logs.userId -> users.id
ALTER TABLE "authentication_logs" ADD CONSTRAINT "authentication_logs_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE SET NULL ON UPDATE CASCADE;
```

### 5.2 Migration Execution

**Development Environment:**
```bash
# Generate migration from schema changes
npx prisma migrate dev --name init

# Apply pending migrations
npx prisma migrate deploy

# Verify schema
npx prisma db push
```

**Staging Environment:**
```bash
# Apply migrations with preview features (optional)
npx prisma migrate deploy --preview-features

# Verify schema matches schema.prisma
npx prisma db push --preview-features
```

**Production Environment:**
```bash
# Automated deployment via CI/CD pipeline
npx prisma migrate deploy

# Verify migration history
npx prisma migrate status
```

### 5.3 Rollback Strategy

**Important:** Prisma does not auto-generate DOWN migrations. Manual reversal required:

1. Identify the migration to revert: `npx prisma migrate status`
2. Create a down migration manually (revert schema to previous state)
3. Resolve the issue in schema.prisma
4. Create a new UP migration

**Example Down Migration:**
```bash
npx prisma migrate resolve --rolled-back YYYYMMDDHHMMSS_init
```

### 5.4 Zero-Downtime Migrations (Future Phases)

For large tables (Phase 3+), implement zero-downtime patterns:
1. Add column with default, no NOT NULL constraint
2. Deploy code to write to both columns
3. Backfill existing data
4. Remove old column in follow-up migration
5. Make new column NOT NULL if needed

---

## 6. Indexing Strategy

### 6.1 Index Summary

| Table | Column(s) | Type | Purpose | Cardinality |
|-------|-----------|------|---------|-------------|
| users | id | PRIMARY KEY (B-tree) | Primary key lookup | Unique |
| users | kakao_id | UNIQUE (B-tree) | Prevent duplicate Kakao registrations | Unique |
| users | created_at DESC | (B-tree) | New user queries, reporting | Low (1000s per day) |
| user_profiles | user_id | UNIQUE (B-tree) | 1:1 profile lookup | Unique |
| user_profiles | created_at DESC | (B-tree) | Recent profile queries | Low |
| user_profiles | industry_code | (B-tree) | Cohort matching by industry | Low (8 categories) |
| refresh_token_store | user_id, expires_at DESC | COMPOUND (B-tree) | Active tokens per user | Medium (1-10 per user) |
| refresh_token_store | token_hash | (B-tree) | Token validation | Medium |
| refresh_token_store | expires_at | (B-tree) | Cleanup queries | Medium |
| authentication_logs | user_id, created_at DESC | COMPOUND (B-tree) | User login history | High (100+ per user) |
| authentication_logs | created_at DESC | (B-tree) | Recent events, dashboards | High (1000+ per day) |
| authentication_logs | event_type | (B-tree) | Event aggregation | Low (6 types) |

### 6.2 Index Rationale

**Foreign Key Indexes:**
- Automatically created for PK lookups; explicitly indexed for JOINs
- Example: `user_profiles.user_id` indexed twice (UNIQUE + FK lookup)

**Compound Indexes:**
- `(user_id, expires_at DESC)`: Optimizes "find active tokens for user" queries
  - Query: `SELECT * FROM refresh_token_store WHERE user_id=? AND expires_at > NOW()`
  - Execution: Index scan from leaf (user_id=?) → filter by expires_at → no additional table scan
- `(user_id, created_at DESC)`: Optimizes "user login history" queries
  - Query: `SELECT * FROM authentication_logs WHERE user_id=? ORDER BY created_at DESC LIMIT 10`
  - Execution: Index range scan (user_id=?) → already sorted by created_at DESC → instant retrieval

**DESC Indexes:**
- PostgreSQL can use ASC indexes in reverse (slower); explicit DESC indexes optimize `ORDER BY DESC` queries
- Commonly needed for "most recent" queries (onboarding events, logins)

### 6.3 Index Maintenance

**Monitoring:**
```sql
-- Check index usage
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

**Unused Indexes (>60 days):**
```sql
-- Find indexes with zero reads (remove if safe)
SELECT * FROM pg_stat_user_indexes WHERE idx_scan = 0;
```

**Index Bloat (vacuum):**
```sql
-- Manual vacuum (auto-run weekly via autovacuum)
VACUUM ANALYZE;
```

---

## 7. Performance Optimization

### 7.1 Query Performance Targets

| Query Pattern | Target p95 | Validation Method |
|---------------|-----------|-------------------|
| User lookup by kakao_id | <20ms | `SELECT * FROM users WHERE kakao_id=?` |
| Profile lookup by user_id | <15ms | `SELECT * FROM user_profiles WHERE user_id=?` |
| Token validation (hash lookup) | <30ms | `SELECT * FROM refresh_token_store WHERE token_hash=?` |
| Recent login history (100 entries) | <80ms | `SELECT * FROM authentication_logs WHERE user_id=? ORDER BY created_at DESC LIMIT 100` |
| All active tokens for user | <25ms | `SELECT * FROM refresh_token_store WHERE user_id=? AND expires_at > NOW()` |
| Event aggregation (24h) | <150ms | `SELECT event_type, COUNT(*) FROM authentication_logs WHERE created_at > NOW() - interval '1 day' GROUP BY event_type` |

### 7.2 Scaling Projections

| Phase | User Count | Approx Table Size | Notes |
|-------|-----------|-------------------|-------|
| Phase 1 (MVP) | 100–300 | Users: 50KB, Profiles: 60KB, Tokens: 30KB, Logs: 500KB | Single node, no replication needed |
| Phase 2 (Launch) | 1,000 | Users: 150KB, Profiles: 200KB, Tokens: 100KB, Logs: 5MB | ReadReplica optional for reporting |
| Phase 3+ (Growth) | 10,000+ | Users: 1.5MB, Profiles: 2MB, Tokens: 1MB, Logs: 50MB+ | Sharding by user_id, log archival (S3) |

### 7.3 Optimization Techniques

**Connection Pooling:**
- Use PgBouncer or similar to reduce connection overhead
- Pool size: 20-30 connections (prevent resource exhaustion)
- Timeout: 30 minutes (recycle long-idle connections)

**Query Optimization:**
- Use EXPLAIN ANALYZE to inspect query plans
- Example (token validation):
  ```sql
  EXPLAIN ANALYZE
  SELECT * FROM refresh_token_store
  WHERE token_hash = 'hash_value'
  AND revoked_at IS NULL
  AND expires_at > NOW();
  ```
  Expected plan: Index Scan on refresh_token_store_tokenHash_idx → early termination after match

**Caching Layer (Phase 2):**
- Redis for frequently accessed data
  - User profile by user_id (cache TTL 1 hour)
  - Industry counts (cache TTL 1 day)
  - Recent events (cache TTL 5 minutes)
- Cache invalidation: On profile update, delete user key; on event insert, invalidate event aggregation

**Batch Operations:**
- Bulk insert logs: collect 100 events, batch insert via Prisma `createMany()`
- Reduces network round-trips, improves throughput

### 7.4 Autovacuum Configuration

```sql
-- Enable autovacuum (default: ON)
ALTER TABLE users SET (autovacuum_enabled = ON);

-- Adjust vacuum frequency for high-write tables (authentication_logs)
ALTER TABLE authentication_logs SET (
  autovacuum_vacuum_scale_factor = 0.05,  -- trigger at 5% changes
  autovacuum_analyze_scale_factor = 0.02  -- analyze at 2% changes
);

-- Monitor autovacuum runs
SELECT schemaname, relname, last_vacuum, last_autovacuum, vacuum_count, autovacuum_count
FROM pg_stat_user_tables
ORDER BY last_vacuum DESC;
```

---

## 8. Data Integrity & Constraints

### 8.1 Referential Integrity

**ON DELETE Cascade:**
- `user_profiles.user_id → users.id`: Delete user → delete profile (1:1 profile is user-specific)
- `refresh_token_store.user_id → users.id`: Delete user → delete tokens (invalidate sessions on user deletion)

**ON DELETE Set NULL:**
- `authentication_logs.user_id → users.id`: Delete user → log entry preserved with user_id=null (audit trail integrity)

**Implementation (Prisma):**
```prisma
model UserProfile {
  user User @relation(fields: [userId], references: [id], onDelete: Cascade)
}

model RefreshTokenStore {
  user User @relation(fields: [userId], references: [id], onDelete: Cascade)
}

model AuthenticationLog {
  user User? @relation(fields: [userId], references: [id], onDelete: SetNull)
}
```

### 8.2 Unique Constraints

| Column | Constraint | Purpose |
|--------|-----------|---------|
| users.kakao_id | UNIQUE | Prevent duplicate Kakao accounts |
| user_profiles.user_id | UNIQUE | 1:1 user:profile mapping |
| refresh_token_store.token_hash | (not unique; multiple tokens valid) | Supports token rotation |

### 8.3 Check Constraints

```sql
-- Validate rotation counter (non-negative)
ALTER TABLE refresh_token_store
ADD CONSTRAINT check_rotation_counter CHECK (rotation_counter >= 0);

-- Validate onboarding_completed_at is in past (Phase 2: preventive)
ALTER TABLE user_profiles
ADD CONSTRAINT check_onboarding_future CHECK (onboarding_completed_at <= NOW());
```

### 8.4 Application-Level Validation

Prisma does not enforce CHECK constraints at ORM level; validate in application:

```typescript
// Example: Prisma validation before save
if (growthGoals.length < 100 || growthGoals.length > 500) {
  throw new ValidationError('Growth goals must be 100-500 characters');
}

if (!hangulRegex.test(name)) {
  throw new ValidationError('Name must be 2-4 Korean characters');
}
```

---

## 9. Audit Trail & Compliance

### 9.1 Audit Logging

**Automatic Timestamps:**
- `users.created_at`: Tracks account creation (regulatory requirement)
- `users.last_login_at`: Tracks login behavior (security monitoring)
- `user_profiles.created_at`: Tracks profile submission (onboarding audit)
- `user_profiles.updated_at`: Tracks profile edits (Phase 2; currently always locked)
- `refresh_token_store.created_at`: Token issuance time
- `refresh_token_store.revoked_at`: Logout/revocation time
- `authentication_logs.created_at`: Event timestamp (immutable audit trail)

### 9.2 Compliance Requirements

**PIPA (Personal Information Protection Act - Korea):**
- Collect only necessary data: name, role, industry, growth goals (no email, phone)
- Purpose statement on onboarding (required; linked TOS)
- Retention policy: Delete user data 30 days after account deletion (Phase 3 automation)
- Access logs: Store authentication_logs for 90 days (auditable)

**GDPR (if expanding to EU):**
- Right to deletion: Soft-delete user (status=INACTIVE) → hard-delete after 30-day waiting period
- Data portability: Export user profile + posts (Phase 2 feature)
- Privacy by design: No PII in logs, no profile photos in Phase 1

**Data Retention:**
```sql
-- Scheduled job (daily)
DELETE FROM authentication_logs
WHERE created_at < NOW() - INTERVAL '90 days';

DELETE FROM refresh_token_store
WHERE expires_at < NOW() - INTERVAL '7 days'  -- grace period before delete
AND revoked_at IS NOT NULL;
```

### 9.3 Audit Query Examples

```sql
-- Who logged in today?
SELECT u.kakao_id, al.created_at, al.event_type
FROM authentication_logs al
JOIN users u ON al.user_id = u.id
WHERE al.created_at > NOW() - INTERVAL '1 day'
AND al.event_type = 'LOGIN_SUCCESS'
ORDER BY al.created_at DESC;

-- Failed login attempts (potential breach investigation)
SELECT u.kakao_id, al.ip_address, COUNT(*) as attempts
FROM authentication_logs al
LEFT JOIN users u ON al.user_id = u.id
WHERE al.event_type = 'LOGIN_FAILURE'
AND al.created_at > NOW() - INTERVAL '1 hour'
GROUP BY u.kakao_id, al.ip_address
HAVING COUNT(*) > 3;

-- User account lifecycle
SELECT u.id, u.kakao_id, u.created_at, u.last_login_at, u.status,
       up.onboarding_completed_at
FROM users u
LEFT JOIN user_profiles up ON u.id = up.user_id
WHERE u.created_at > NOW() - INTERVAL '7 days'
ORDER BY u.created_at DESC;
```

---

## 10. Backup & Recovery Strategy

### 10.1 Backup Approach

**AWS RDS (Recommended):**
- Automated daily backups (retention: 7 days default; adjust to 30 days for compliance)
- Multi-AZ deployment for high availability
- WAL archiving: enabled for point-in-time recovery (PITR)
- Snapshot cost: ~5-10% of instance cost/month

**Manual Backups:**
```bash
# Local PostgreSQL backup
pg_dump postgresql://user:password@localhost:5432/memoir > backup_$(date +%Y%m%d).sql

# Restore from backup
psql postgresql://user:password@localhost:5432/memoir < backup_20250119.sql
```

### 10.2 Recovery Objectives

| Metric | Target | Justification |
|--------|--------|---------------|
| RPO (Recovery Point Objective) | 1 hour | Accept up to 1 hour of data loss (audit logs, tokens); infrequent with WAL archiving |
| RTO (Recovery Time Objective) | 30 minutes | Restore from snapshot + verify schema; acceptable for Phase 1 MVP |
| Availability SLA | 99.5% (Phase 1) | Allow ~3.6 hours downtime/month; upgrade to 99.9% Phase 3 |

### 10.3 Disaster Recovery Playbook

**Scenario 1: Accidental Table Drop**
1. Identify drop time from WAL logs
2. Use PITR to restore to 1 minute before drop
3. Expected recovery time: 5-10 minutes
4. Data loss: Minimal (up to 1 minute)

**Scenario 2: Database Corruption**
1. Restore latest clean snapshot from previous day
2. Disable autovacuum to prevent aggressive cleanup
3. Run `REINDEX` to rebuild indexes
4. Expected recovery time: 15-30 minutes
5. Data loss: Up to 24 hours (acceptable for MVP; improve Phase 2)

**Scenario 3: Regional Disaster (Entire AZ Down)**
1. Failover to Multi-AZ standby (if enabled): automatic, <1 minute
2. Or: Restore to new instance in different region from snapshot
3. Update DNS to new endpoint
4. Expected recovery time: 5 minutes (automatic) or 30 minutes (manual)
5. Data loss: None (synchronous replication) or 1 hour (snapshot-based)

---

## 11. Security Considerations

### 11.1 Data Sensitivity Classification

| Column | Sensitivity | Handling |
|--------|-------------|----------|
| users.id, users.kakao_id | Medium | Unique identifiers; logged in audit trails |
| user_profiles.name | High | PII; never logged; indexed for user lookup only |
| user_profiles.role | Medium | Job title; helps with cohort matching; ok in logs |
| user_profiles.growth_goals | High | Personal aspirations; never logged; stored plaintext (future: encrypted at-rest Phase 3) |
| refresh_token_store.token_hash | High | Bcrypt hash; never log plaintext token; hash is safe |
| authentication_logs.ip_address | Medium | Location data; could enable tracking; mask last octet (Phase 3) |
| authentication_logs.user_agent | Low | Device/browser info; safe for debugging |

### 11.2 Encryption

**In Transit (TLS 1.3):**
- All API endpoints require HTTPS
- Database connections: Use SSL/TLS for RDS (AWS default)
- Prisma connection string: `postgresql://user:password@host/db?sslmode=require`

**At Rest (RDS Encryption):**
- AWS RDS: Enable AES-256 encryption (default for new instances)
- Backup encryption: Inherited from source instance
- Encryption key: AWS KMS (managed keys); rotate annually

**Application-Level (Phase 2+):**
- Growth goals: Consider AES-256 encryption before storing (decrypt on read)
- User names: PII encryption unnecessary if DB encryption enabled

### 11.3 Token Security

**Refresh Token Storage:**
```typescript
// Never store plaintext tokens in DB
// Always bcrypt hash before persistence

import bcrypt from 'bcryptjs';

const tokenHash = await bcrypt.hash(refreshToken, 12);
await prisma.refreshTokenStore.create({
  data: {
    userId,
    tokenHash,  // bcrypt($2b$12$...) 60-char string
    expiresAt: new Date(Date.now() + 90 * 24 * 60 * 60 * 1000),
  },
});

// Verification on refresh endpoint
const storedRecord = await prisma.refreshTokenStore.findUnique({
  where: { tokenHash: bcrypt.hashSync(token) },  // WARNING: WRONG, bcrypt hashing is not deterministic
});

// CORRECT approach: store token+hash, or use constant-time comparison
const match = await bcrypt.compare(refreshToken, storedRecord.tokenHash);
```

### 11.4 SQL Injection Prevention

Prisma parameterizes all queries automatically:
```typescript
// Safe: Prisma parameterizes kakao_id
const user = await prisma.user.findUnique({
  where: { kakaoId: kakaoId }, // Input safely parameterized
});

// Unsafe: Raw SQL (avoid)
const user = await prisma.$queryRaw(`
  SELECT * FROM users WHERE kakao_id = '${kakaoId}'  // VULNERABLE
`);

// Safe raw SQL
const user = await prisma.$queryRaw`
  SELECT * FROM users WHERE kakao_id = ${kakaoId}  // Parameterized
`;
```

### 11.5 Access Control

**Database User Permissions (Production):**
```sql
-- Create minimal-privilege DB user for app
CREATE USER memoir_app WITH PASSWORD 'secure_random_password';

-- Grant only necessary tables
GRANT USAGE ON SCHEMA public TO memoir_app;
GRANT SELECT, INSERT, UPDATE ON users, user_profiles, refresh_token_store, authentication_logs TO memoir_app;
REVOKE DELETE ON users FROM memoir_app;  -- Prevent accidental deletion

-- Create admin user for maintenance (separate password)
CREATE USER memoir_admin WITH PASSWORD 'different_secure_password';
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO memoir_admin;
```

---

## 12. Scalability & Future Phases

### 12.1 Phase-Based Scaling

**Phase 1 (100–500 users):**
- Single PostgreSQL instance (t3.small on RDS: 2GB RAM, 1 vCPU)
- No read replicas needed
- Connection pool: 10-20
- Expected growth: 100 users/month

**Phase 2 (500–2,000 users):**
- Single instance (t3.medium: 4GB RAM, 2 vCPU)
- Add read replica for reporting (read-only queries)
- Connection pool: 20-30
- Caching layer: Redis for user profiles, industry stats
- Expected growth: 300 users/month

**Phase 3 (2,000–10,000 users):**
- Primary-replica setup with pgBouncer
- Vertical scaling: Dedicated instance (c6i.xlarge or larger)
- Horizontal sharding: Partition by user_id % 4 (4 logical shards)
- Connection pooling: PgBouncer 100+ connections
- Log archival: Move old authentication_logs to S3 + Redshift for analytics
- Expected growth: 500+ users/month

**Phase 4+ (10,000+ users):**
- Multi-region deployment (Primary: Seoul; Replica: Tokyo, Singapore)
- Cross-region replication with logical replication
- Dedicated analytics warehouse (Redshift/BigQuery)
- Geo-sharding: Shard by region for low latency
- Expected growth: 1000+ users/month

### 12.2 Sharding Strategy (Phase 3+)

**Shard Key:** `user_id` (natural partitioning)

**Sharding Formula:** `shard_id = user_id.hash % shard_count`

```sql
-- Shard 0 (Users: user_id % 4 = 0)
CREATE TABLE users_shard0 AS
SELECT * FROM users WHERE (user_id::text::bigint) % 4 = 0;

-- Repeat for shards 1, 2, 3
-- Migrate app routing layer to hit correct shard
```

**Challenges:**
- Cross-shard queries (e.g., "all failed logins in 24h") require scatter-gather + merge
- Rebalancing if shard count increases (complex; avoid if possible)
- Transaction isolation across shards (eventual consistency)

### 12.3 Caching Layer (Phase 2)

**Redis Integration:**
```typescript
// Cache user profile by user_id (1-hour TTL)
const getCachedProfile = async (userId: string) => {
  const cached = await redis.get(`profile:${userId}`);
  if (cached) return JSON.parse(cached);

  const profile = await prisma.userProfile.findUnique({
    where: { userId },
  });

  await redis.setex(`profile:${userId}`, 3600, JSON.stringify(profile));
  return profile;
};

// Invalidate cache on profile update
await prisma.userProfile.update({
  where: { userId },
  data: { growthGoals: newGoals },
});
await redis.del(`profile:${userId}`);
```

**Cache Keys:**
- `profile:{user_id}` (TTL: 1 hour)
- `industry_stats` (TTL: 1 day)
- `recent_logins:{user_id}` (TTL: 5 minutes)

---

## 13. Common Query Patterns & Optimization

### 13.1 Query Examples (Prisma Client)

**Authentication Flow:**

```typescript
// 1. First-login: Create user + check onboarding status
const user = await prisma.user.upsert({
  where: { kakaoId },
  update: { lastLoginAt: new Date() },
  create: {
    kakaoId,
    createdAt: new Date(),
  },
});

const profile = await prisma.userProfile.findUnique({
  where: { userId: user.id },
});

return {
  userId: user.id,
  userExists: true,  // or false if just created
  onboardingCompleted: !!profile?.onboardingCompletedAt,
};

// 2. Refresh token validation
const tokenRecord = await prisma.refreshTokenStore.findFirst({
  where: {
    userId,
    revokedAt: null,
    expiresAt: { gt: new Date() },
  },
});

const valid = await bcrypt.compare(refreshToken, tokenRecord.tokenHash);

// 3. Profile submission
const profile = await prisma.userProfile.create({
  data: {
    userId,
    name: validatedName,
    role: validatedRole,
    industryCode,
    growthGoals: validatedGoals,
    onboardingCompletedAt: new Date(),
    profileLocked: true,
  },
});

// Log event
await prisma.authenticationLog.create({
  data: {
    userId,
    eventType: 'PROFILE_CREATED',
    createdAt: new Date(),
  },
});
```

**Analytics Queries:**

```typescript
// User signup trend (daily)
const signups = await prisma.user.groupBy({
  by: ['createdAt'],
  _count: true,
  orderBy: { createdAt: 'desc' },
});

// Industry distribution
const industryStats = await prisma.userProfile.groupBy({
  by: ['industryCode'],
  _count: true,
});

// Failed login attempts (potential security issue)
const failedLogins = await prisma.authenticationLog.findMany({
  where: {
    eventType: 'LOGIN_FAILURE',
    createdAt: { gt: new Date(Date.now() - 24 * 60 * 60 * 1000) },
  },
  include: { user: true },
  orderBy: { createdAt: 'desc' },
  take: 100,
});
```

**Cleanup Jobs:**

```typescript
// Revoke expired tokens (daily)
await prisma.refreshTokenStore.deleteMany({
  where: {
    expiresAt: { lt: new Date() },
  },
});

// Delete old audit logs (weekly)
await prisma.authenticationLog.deleteMany({
  where: {
    createdAt: { lt: new Date(Date.now() - 90 * 24 * 60 * 60 * 1000) },
  },
});
```

### 13.2 Query Performance Analysis

**Slow Query Logging (PostgreSQL):**
```sql
-- Enable logging for queries >1s
ALTER SYSTEM SET log_min_duration_statement = 1000;
SELECT pg_reload_conf();

-- Check slow query log
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
WHERE mean_time > 1000
ORDER BY mean_time DESC;
```

**EXPLAIN ANALYZE Examples:**

```sql
-- User lookup by kakao_id (should use index)
EXPLAIN ANALYZE
SELECT * FROM users WHERE kakao_id = 'kakao12345';
-- Expected: Index Scan on users_kakao_id_key, cost: <1ms

-- Active tokens for user (compound index)
EXPLAIN ANALYZE
SELECT * FROM refresh_token_store
WHERE user_id = 'user_uuid'
AND expires_at > NOW()
ORDER BY expires_at DESC;
-- Expected: Index Scan on refresh_token_store_userId_expiresAt_idx, cost: <5ms

-- Recent login history (compound index + ORDER BY)
EXPLAIN ANALYZE
SELECT * FROM authentication_logs
WHERE user_id = 'user_uuid'
ORDER BY created_at DESC
LIMIT 10;
-- Expected: Index Scan on authentication_logs_userId_created_at_idx, cost: <10ms
```

---

## 14. Testing Database Schema

### 14.1 Unit Test Database Setup

```typescript
// jest.config.js: Use test database URL
process.env.DATABASE_URL = 'postgresql://test_user:password@localhost:5432/memoir_test';

// test/setup.ts: Reset DB before each test
beforeAll(async () => {
  // Create test schema from migrations
  await execSync('npx prisma migrate deploy');
});

afterEach(async () => {
  // Reset all tables
  await prisma.$executeRawUnsafe('TRUNCATE TABLE authentication_logs CASCADE');
  await prisma.$executeRawUnsafe('TRUNCATE TABLE refresh_token_store CASCADE');
  await prisma.$executeRawUnsafe('TRUNCATE TABLE user_profiles CASCADE');
  await prisma.$executeRawUnsafe('TRUNCATE TABLE users CASCADE');
});

afterAll(async () => {
  await prisma.$disconnect();
});
```

### 14.2 Data Fixtures

```typescript
// test/fixtures.ts
export async function createTestUser(kakaoId = 'test_kakao_' + Date.now()) {
  return prisma.user.create({
    data: {
      kakaoId,
      status: 'ACTIVE',
    },
  });
}

export async function createTestProfile(userId: string) {
  return prisma.userProfile.create({
    data: {
      userId,
      name: '테스트',
      role: 'Product Manager',
      industryCode: 'STARTUP',
      growthGoals: '스타트업에서 성공적인 제품 개발자가 되기를 원합니다.',
      onboardingCompletedAt: new Date(),
      profileLocked: true,
    },
  });
}

export async function createTestToken(userId: string) {
  const tokenHash = await bcrypt.hash('test_token', 12);
  return prisma.refreshTokenStore.create({
    data: {
      userId,
      tokenHash,
      expiresAt: new Date(Date.now() + 90 * 24 * 60 * 60 * 1000),
    },
  });
}
```

### 14.3 Integration Tests

```typescript
// test/auth.test.ts
describe('Authentication Flow', () => {
  it('should create user on first Kakao login', async () => {
    const kakaoId = 'kakao_' + Date.now();
    const user = await createUserFromKakao(kakaoId);

    expect(user).toBeDefined();
    expect(user.kakaoId).toBe(kakaoId);
    expect(user.status).toBe('ACTIVE');

    // Verify record in DB
    const dbUser = await prisma.user.findUnique({ where: { kakaoId } });
    expect(dbUser).toBeDefined();
  });

  it('should reject duplicate Kakao registration', async () => {
    const kakaoId = 'kakao_existing';
    await prisma.user.create({ data: { kakaoId } });

    expect(() => createUserFromKakao(kakaoId)).toThrow('DUPLICATE_ACCOUNT');
  });

  it('should rotate refresh tokens correctly', async () => {
    const user = await createTestUser();
    const oldToken = await createTestToken(user.id);

    const newTokenPair = await refreshTokenRotation(oldToken.tokenHash);

    // Old token should be revoked
    const revokedRecord = await prisma.refreshTokenStore.findUnique({
      where: { id: oldToken.id },
    });
    expect(revokedRecord.revokedAt).toBeDefined();

    // New token should exist
    const newRecord = await prisma.refreshTokenStore.findFirst({
      where: { userId: user.id, revokedAt: null },
    });
    expect(newRecord.rotationCounter).toBe(oldToken.rotationCounter + 1);
  });
});
```

---

## 15. Deployment Checklist

Before deploying to production, verify:

**Pre-Deployment:**
- [ ] Schema migrations tested in staging (run `npx prisma migrate deploy`)
- [ ] Backup strategy configured (AWS RDS retention set to 30 days)
- [ ] Connection pooling configured (PgBouncer or RDS Proxy)
- [ ] Monitoring alerts set up (slow query logs, replication lag)
- [ ] Disaster recovery plan documented and tested
- [ ] Security review completed (encryption, access control, PII handling)
- [ ] Performance baseline established (query latencies, connection pool usage)
- [ ] Test data cleanup: No test users (kakao_id like 'test_%') in production

**Deployment:**
- [ ] Run migrations: `npx prisma migrate deploy`
- [ ] Verify schema: `npx prisma db push --dry-run` (should show no pending changes)
- [ ] Health check: Query users, profiles, logs tables (ensure accessibility)
- [ ] Smoke test: Test authentication flow end-to-end
- [ ] Monitor: Watch slow query logs for first 1 hour post-deploy

**Post-Deployment:**
- [ ] Verify backups are running (check RDS backup history)
- [ ] Monitor query performance (p95 latency <50ms)
- [ ] Review authentication logs for errors
- [ ] Document any schema deviations from this spec

---

## 16. Known Limitations & Future Work

### 16.1 Phase 1 Limitations

1. **No Encryption at Rest:** Growth goals stored plaintext (acceptable for MVP; add Phase 3)
2. **No Read Replicas:** All queries hit primary (acceptable for 300 users; add Phase 2)
3. **Manual Token Cleanup:** Expired tokens not auto-deleted (manual weekly job)
4. **No Sharding:** Single DB instance (acceptable up to 10K users)
5. **No PII Masking:** Logs store IP addresses (mask last octet Phase 3 for GDPR)

### 16.2 Phase 2 Enhancements

- [ ] Add `user_preferences` table (notification settings, cohort preferences)
- [ ] Add `profile_edit_history` table (audit trail when profile_locked unlocked)
- [ ] Implement Redis caching layer (user profiles, industry stats)
- [ ] Add read replica for reporting queries
- [ ] Enable column-level encryption for growth_goals (PII protection)
- [ ] Implement automated token cleanup job

### 16.3 Phase 3+ Enhancements

- [ ] Implement database sharding (by user_id)
- [ ] Add `user_analytics` fact table (denormalized for BigQuery)
- [ ] Implement GDPR right-to-delete (automated 30-day hard delete)
- [ ] Multi-region replication (Seoul primary, Tokyo + Singapore replicas)
- [ ] IP address masking (PIPA compliance enhancement)

---

## Appendix A: SQL DDL Script

**Complete schema creation (single file for reference):**

```sql
-- Memoir User Authentication & Onboarding Schema (PostgreSQL 15)

-- Enums
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'LOCKED');
CREATE TYPE industry_code AS ENUM ('STARTUP', 'ENTERPRISE', 'SME', 'PUBLIC', 'FOREIGN', 'FREELANCER', 'NONPROFIT', 'OTHER');
CREATE TYPE event_type AS ENUM ('LOGIN_SUCCESS', 'LOGIN_FAILURE', 'KAKAO_ERROR', 'TOKEN_REFRESH', 'LOGOUT', 'PROFILE_CREATED');

-- Core tables
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  kakao_id VARCHAR(255) NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_login_at TIMESTAMPTZ,
  status user_status NOT NULL DEFAULT 'ACTIVE'
);

CREATE UNIQUE INDEX idx_users_kakao_id ON users(kakao_id);
CREATE INDEX idx_users_created_at ON users(created_at DESC);

CREATE TABLE user_profiles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL UNIQUE,
  name VARCHAR(8) NOT NULL,
  role VARCHAR(64) NOT NULL,
  industry_code industry_code NOT NULL,
  growth_goals TEXT NOT NULL,
  profile_photo_url VARCHAR(512),
  onboarding_completed_at TIMESTAMPTZ NOT NULL,
  profile_locked BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_user_profiles_user_id
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_created_at ON user_profiles(created_at DESC);
CREATE INDEX idx_user_profiles_industry_code ON user_profiles(industry_code);

CREATE TABLE refresh_token_store (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  token_hash VARCHAR(256) NOT NULL,
  rotation_counter INTEGER NOT NULL DEFAULT 0,
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  revoked_at TIMESTAMPTZ,
  CONSTRAINT fk_refresh_token_user_id
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT check_rotation_counter CHECK (rotation_counter >= 0)
);

CREATE INDEX idx_refresh_token_user_id_expires ON refresh_token_store(user_id, expires_at DESC);
CREATE INDEX idx_refresh_token_hash ON refresh_token_store(token_hash);
CREATE INDEX idx_refresh_token_expires_at ON refresh_token_store(expires_at);

CREATE TABLE authentication_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID,
  event_type event_type NOT NULL,
  ip_address INET,
  user_agent TEXT,
  error_code VARCHAR(64),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_auth_logs_user_id
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_auth_logs_user_id_created ON authentication_logs(user_id, created_at DESC);
CREATE INDEX idx_auth_logs_created_at ON authentication_logs(created_at DESC);
CREATE INDEX idx_auth_logs_event_type ON authentication_logs(event_type);
```

---

## Appendix B: Prisma Migrate Commands Reference

```bash
# Initialize Prisma (first time only)
npx prisma init

# Create a migration from schema changes
npx prisma migrate dev --name init

# Apply pending migrations (CI/CD)
npx prisma migrate deploy

# Check migration status
npx prisma migrate status

# Reset database (dev only; destroys data)
npx prisma migrate reset

# Resolve migration issues
npx prisma migrate resolve --rolled-back YYYYMMDDHHMMSS_init

# Generate Prisma Client (after schema changes)
npx prisma generate

# Verify schema against DB
npx prisma db push --preview-features
```

---

## Appendix C: PostgreSQL Configuration Tuning

**For 100–300 concurrent users:**

```sql
-- Max connections (default 100; increase for pooling)
ALTER SYSTEM SET max_connections = 200;

-- Autovacuum settings (aggressive for high-write tables)
ALTER SYSTEM SET autovacuum = on;
ALTER SYSTEM SET autovacuum_naptime = '10s';

-- Memory settings (for t3.small: 2GB)
ALTER SYSTEM SET shared_buffers = '512MB';
ALTER SYSTEM SET effective_cache_size = '1536MB';
ALTER SYSTEM SET work_mem = '4MB';
ALTER SYSTEM SET maintenance_work_mem = '128MB';

-- Logging (for performance analysis)
ALTER SYSTEM SET log_min_duration_statement = 1000;  -- Log queries >1s
ALTER SYSTEM SET log_connections = on;
ALTER SYSTEM SET log_disconnections = on;

-- Apply changes
SELECT pg_reload_conf();
```

---

## Appendix D: Compliance Checklist

- [ ] **PIPA (Korea):** User consent on first login (TOS checkbox), minimal data collection, 90-day audit logs, 30-day data retention post-deletion
- [ ] **GDPR (if EU expansion):** Consent mechanism, right-to-delete, data portability, DPA with cloud provider
- [ ] **Security:** Bcrypt token hashing, TLS in transit, encryption at-rest (RDS AES-256), no plaintext secrets in DB
- [ ] **Audit Trail:** authentication_logs immutable, user_profiles timestamps, token lifecycle tracked
- [ ] **Access Control:** DB user with minimal privileges (SELECT, INSERT, UPDATE only), no DELETE on users table
- [ ] **Backup:** Daily automated backups, 7-day retention, PITR enabled, disaster recovery tested

---

## Summary

This comprehensive database schema design supports Memoir's Phase 1 MVP with:

- **4 core tables** (users, user_profiles, refresh_token_store, authentication_logs) optimized for authentication and onboarding workflows
- **Strategic indexing** for <50ms query response times at 100+ concurrent users
- **Referential integrity** with cascading deletes and soft-delete patterns
- **Token rotation mechanism** for secure session management
- **Immutable audit trail** for compliance and security monitoring
- **Scalability path** from Phase 1 (300 users) to Phase 3+ (10,000+ users)
- **Security-first design** with bcrypt hashing, TLS encryption, and PII protection

All schema migrations are generated via Prisma and version-controlled for reproducibility and rollback capability.

---

**Document Version:** 1.0
**Date:** 2025-11-19
**Author:** Spec Writer Agent
**Status:** Ready for Implementation
**Next Steps:** Create Prisma schema file + run initial migration in development environment
