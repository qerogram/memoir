# API Specification: User Authentication & Onboarding

## Table of Contents

1. [API Overview](#api-overview)
2. [Authentication & Authorization](#authentication--authorization)
3. [Request/Response Format](#requestresponse-format)
4. [Endpoints](#endpoints)
5. [Error Handling](#error-handling)
6. [Rate Limiting](#rate-limiting)
7. [Token Structure](#token-structure)
8. [TypeScript Types](#typescript-types)
9. [cURL Examples](#curl-examples)
10. [HTTP Status Codes](#http-status-codes)
11. [Security Headers](#security-headers)
12. [Performance Targets](#performance-targets)
13. [Sequence Diagrams](#sequence-diagrams)
14. [Test Cases](#test-cases)

---

## API Overview

### Base URL
```
https://api.memoir.kr/api/v1
```

### General Information
- **Content-Type:** `application/json` (all requests and responses)
- **Character Encoding:** UTF-8
- **API Version:** v1 (URL versioning strategy)
- **Protocol:** HTTPS/TLS 1.3 (mandatory)
- **Timezone:** All timestamps in ISO 8601 format (UTC)

### API Principles
- RESTful design with resource-based URLs
- Consistent, lowercase, hyphenated endpoint naming
- Plural nouns for resource endpoints (e.g., `/users`)
- Appropriate HTTP methods (GET, POST, PUT, PATCH, DELETE)
- Clear, actionable error messages
- Server-side validation on all inputs
- Request tracking via X-Request-ID header

---

## Authentication & Authorization

### Bearer Token Authentication
All authenticated endpoints require the `Authorization` header with a valid JWT access token:

```
Authorization: Bearer <access_token>
```

### Token Types
- **Access Token:** Short-lived (30 days), used for API requests
- **Refresh Token:** Long-lived (90 days), used to obtain new access tokens
- **Token Rotation:** Old refresh tokens are invalidated after rotation (security)

### Public vs. Protected Endpoints
| Endpoint | Auth Required | Description |
|----------|---------------|-------------|
| `POST /auth/kakao` | No | Kakao OAuth token exchange (public) |
| `POST /auth/refresh` | No | Token refresh endpoint (public) |
| `POST /users/profile` | **Yes** | Create user profile (requires access_token) |
| `GET /users/me` | **Yes** | Get authenticated user info (requires access_token) |

### Invalid/Expired Tokens
- **Invalid Format:** Returns `401 UNAUTHORIZED`
- **Expired Access Token:** Returns `401 UNAUTHORIZED` (client must use refresh token)
- **Expired Refresh Token:** Returns `401 TOKEN_EXPIRED` (client must re-authenticate via Kakao)

---

## Request/Response Format

### Standard Request Header
```http
POST /api/v1/users/profile HTTP/1.1
Host: api.memoir.kr
Content-Type: application/json
Authorization: Bearer eyJhbGc...
X-Request-ID: req_abc123xyz789
User-Agent: Memoir-Android/1.0.0
Accept: application/json
```

### Standard Response Envelope
All responses follow a consistent JSON envelope structure:

#### Success Response (2xx)
```json
{
  "data": {
    "key": "value",
    "nested": {
      "field": "data"
    }
  }
}
```

#### Error Response (4xx, 5xx)
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": {
      "field_name": "specific error info (optional)"
    },
    "request_id": "req_abc123xyz"
  }
}
```

### Response Headers (All Responses)
```http
Content-Type: application/json
X-Request-ID: req_abc123xyz
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 9
X-RateLimit-Reset: 1700425200
```

---

## Endpoints

### 1. Kakao OAuth Token Exchange

**Endpoint:** `POST /auth/kakao`

**Authentication:** None (Public endpoint)

**Purpose:** Exchange Kakao authorization code for Memoir access/refresh tokens. Creates new user record on first login.

#### Request

```json
{
  "kakao_code": "string (required, 1-500 chars)",
  "device_id": "string (optional, 1-128 chars, for future analytics)"
}
```

**Request Headers:**
```http
Content-Type: application/json
X-Request-ID: req_abc123xyz (optional)
User-Agent: Memoir-Android/1.0.0
```

**Query Parameters:** None

**Path Parameters:** None

**Validation Rules:**
- `kakao_code`: Required, non-empty string
- `device_id`: Optional, alphanumeric + hyphen allowed

#### Success Response (200 OK)

```json
{
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "access_token_expires_in": 2592000,
    "refresh_token_expires_in": 7776000,
    "user_id": "550e8400-e29b-41d4-a716-446655440000",
    "user_exists": false,
    "onboarding_completed": false,
    "kakao_id": "1234567890"
  }
}
```

**Response Field Descriptions:**
- `access_token`: JWT access token for API requests (30 days)
- `refresh_token`: JWT refresh token for obtaining new access tokens (90 days)
- `access_token_expires_in`: Seconds until access token expiry
- `refresh_token_expires_in`: Seconds until refresh token expiry
- `user_id`: UUID of the user (v4 format)
- `user_exists`: `false` if newly created, `true` if existing user
- `onboarding_completed`: `true` if user has completed onboarding, `false` otherwise
- `kakao_id`: Kakao user ID (numeric string)

#### Error Responses

**400 INVALID_CODE**
```json
{
  "error": {
    "code": "INVALID_CODE",
    "message": "Kakao authorization code is invalid or expired",
    "request_id": "req_abc123xyz"
  }
}
```
- Cause: Code format invalid, code expired (>10 min old), code already used
- Action: User should retry Kakao login

**400 INVALID_REQUEST**
```json
{
  "error": {
    "code": "INVALID_REQUEST",
    "message": "Request body is invalid",
    "details": {
      "kakao_code": "kakao_code field is required"
    },
    "request_id": "req_abc123xyz"
  }
}
```
- Cause: Missing or malformed request body
- Action: Check request JSON format

**429 RATE_LIMITED**
```json
{
  "error": {
    "code": "RATE_LIMITED",
    "message": "Too many requests. Please wait before trying again.",
    "request_id": "req_abc123xyz"
  }
}
```
- Cause: Exceeded rate limit (>10 req/min per IP)
- Action: Wait before retrying (see `Retry-After` header)

**502 KAKAO_UPSTREAM_ERROR**
```json
{
  "error": {
    "code": "KAKAO_UPSTREAM_ERROR",
    "message": "Kakao service temporarily unavailable. Please try again.",
    "request_id": "req_abc123xyz"
  }
}
```
- Cause: Kakao API error, timeout, or service unavailability
- Action: Implement exponential backoff retry (max 3 attempts)

**503 SERVICE_UNAVAILABLE**
```json
{
  "error": {
    "code": "SERVICE_UNAVAILABLE",
    "message": "Memoir backend service is temporarily unavailable.",
    "request_id": "req_abc123xyz"
  }
}
```
- Cause: Database unavailable, internal service failure
- Action: Retry after waiting

---

### 2. Create User Profile

**Endpoint:** `POST /users/profile`

**Authentication:** Required (Bearer access_token in Authorization header)

**Purpose:** Save user profile information during onboarding. Sets `onboarding_completed` flag when successful.

#### Request

```json
{
  "name": "김태희",
  "role": "Product Manager",
  "industry_code": "STARTUP",
  "growth_goals": "일상 속 배움을 구조적으로 기록하고 다양한 분야의 전문가들과 나누고 싶습니다. 특히 사용자 중심의 제품 철학을 배우고 싶어요."
}
```

**Request Headers:**
```http
Content-Type: application/json
Authorization: Bearer eyJhbGc...
X-Request-ID: req_abc123xyz (optional)
```

**Validation Rules:**

| Field | Type | Length | Required | Validation |
|-------|------|--------|----------|-----------|
| `name` | String | 2-4 | Yes | Hangul only (regex: `^[\uac00-\ud7a3]{2,4}$`) |
| `role` | String | 2-48 | Yes | Korean + English letters, space, hyphen allowed |
| `industry_code` | String | - | Yes | Must be one of enum values (see below) |
| `growth_goals` | String | 100-500 | Yes | Unicode text, no null bytes |

**Enum: Industry Codes**
```
STARTUP              스타트업
ENTERPRISE           대기업
SME                  중소기업
PUBLIC               공기업/공공기관
FOREIGN              외국계 기업
FREELANCER           프리랜서/1인 기업
NONPROFIT            비영리/사회적 기업
OTHER                기타
```

#### Success Response (201 CREATED)

```json
{
  "data": {
    "user_id": "550e8400-e29b-41d4-a716-446655440000",
    "profile": {
      "id": "550e8401-e29b-41d4-a716-446655440001",
      "user_id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "김태희",
      "role": "Product Manager",
      "industry_code": "STARTUP",
      "growth_goals": "일상 속 배움을 구조적으로 기록하고 다양한 분야의 전문가들과 나누고 싶습니다. 특히 사용자 중심의 제품 철학을 배우고 싶어요.",
      "profile_photo_url": null,
      "onboarding_completed_at": "2025-11-19T14:23:45Z",
      "profile_locked": true,
      "created_at": "2025-11-19T14:23:45Z",
      "updated_at": "2025-11-19T14:23:45Z"
    }
  }
}
```

**Response Field Descriptions:**
- `profile_photo_url`: Null (Phase 1 MVP); will contain S3 URL in Phase 2
- `onboarding_completed_at`: Timestamp when profile was submitted (marks onboarding complete)
- `profile_locked`: Always `true` (Phase 1); prevents editing

#### Error Responses

**400 VALIDATION_ERROR**
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Input validation failed",
    "details": {
      "name": "이름은 한글 2-4글자만 허용됩니다",
      "growth_goals": "성장 목표는 최소 100자 이상이어야 합니다"
    },
    "request_id": "req_abc123xyz"
  }
}
```

**Validation Errors by Field:**

| Field | Error Message | Condition |
|-------|---------------|-----------|
| `name` | 이름은 한글 2-4글자만 허용됩니다 | Not hangul OR length ≠ 2-4 |
| `role` | 역할은 2-48글자여야 합니다 | Length < 2 OR > 48 |
| `industry_code` | 유효한 산업을 선택해주세요 | Not in enum list |
| `growth_goals` | 성장 목표는 100-500글자여야 합니다 | Length < 100 OR > 500 |

**401 UNAUTHORIZED**
```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or expired access token",
    "request_id": "req_abc123xyz"
  }
}
```
- Cause: Missing Authorization header, invalid token format, token expired
- Action: Client should refresh token via `/auth/refresh` or re-authenticate

**409 PROFILE_ALREADY_SET**
```json
{
  "error": {
    "code": "PROFILE_ALREADY_SET",
    "message": "User profile already exists. Cannot update (Phase 1: profiles are locked)",
    "request_id": "req_abc123xyz"
  }
}
```
- Cause: User has already submitted profile during onboarding
- Action: Inform user profile is locked; profile editing available in Phase 2

**429 RATE_LIMITED**
```json
{
  "error": {
    "code": "RATE_LIMITED",
    "message": "Too many profile creation attempts. Please wait before trying again.",
    "request_id": "req_abc123xyz"
  }
}
```
- Cause: Exceeded rate limit (>5 req/min per user)
- Action: Wait before retrying

**500 INTERNAL_SERVER_ERROR**
```json
{
  "error": {
    "code": "INTERNAL_SERVER_ERROR",
    "message": "An unexpected error occurred while creating profile",
    "request_id": "req_abc123xyz"
  }
}
```
- Cause: Database failure, internal service error
- Action: Retry after waiting

---

### 3. Get Authenticated User Information

**Endpoint:** `GET /users/me`

**Authentication:** Required (Bearer access_token in Authorization header)

**Purpose:** Retrieve complete user information and profile. Used to check onboarding status and fetch user data.

#### Request

```http
GET /api/v1/users/me HTTP/1.1
Host: api.memoir.kr
Authorization: Bearer eyJhbGc...
X-Request-ID: req_abc123xyz (optional)
```

**Query Parameters:** None

**Request Body:** None

#### Success Response (200 OK)

```json
{
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "kakao_id": "1234567890",
      "created_at": "2025-11-19T14:20:00Z",
      "last_login_at": "2025-11-19T14:23:00Z",
      "status": "active"
    },
    "profile": {
      "id": "550e8401-e29b-41d4-a716-446655440001",
      "user_id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "김태희",
      "role": "Product Manager",
      "industry_code": "STARTUP",
      "growth_goals": "일상 속 배움을 구조적으로 기록하고 다양한 분야의 전문가들과 나누고 싶습니다.",
      "profile_photo_url": null,
      "onboarding_completed_at": "2025-11-19T14:23:45Z",
      "profile_locked": true,
      "created_at": "2025-11-19T14:23:45Z"
    },
    "flags": {
      "onboarding_completed": true,
      "profile_locked": true
    }
  }
}
```

**Response Field Descriptions:**
- `user.status`: Account status (`active`, `inactive`, `locked`)
- `profile`: Null if onboarding not completed; populated if completed
- `flags.onboarding_completed`: UI decision flag; frontend navigates based on this

#### Error Responses

**401 UNAUTHORIZED**
```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or expired access token",
    "request_id": "req_abc123xyz"
  }
}
```

**404 NOT_FOUND**
```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "User not found",
    "request_id": "req_abc123xyz"
  }
}
```
- Cause: Very rare; indicates user record was deleted or corrupted
- Action: Force re-authentication

---

### 4. Refresh Access Token

**Endpoint:** `POST /auth/refresh`

**Authentication:** None (Public endpoint)

**Purpose:** Exchange expired access token for a new access/refresh token pair. Implements token rotation for security.

#### Request

```json
{
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Request Headers:**
```http
Content-Type: application/json
X-Request-ID: req_abc123xyz (optional)
```

**Validation Rules:**
- `refresh_token`: Required, must be valid JWT format

#### Success Response (200 OK)

```json
{
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "access_token_expires_in": 2592000,
    "refresh_token_expires_in": 7776000
  }
}
```

**Important Notes on Token Rotation:**
- Old refresh token is immediately invalidated
- Client must store new tokens and discard old ones
- Rotation counter incremented server-side for audit trail

#### Error Responses

**400 INVALID_REQUEST**
```json
{
  "error": {
    "code": "INVALID_REQUEST",
    "message": "Request body is invalid",
    "details": {
      "refresh_token": "refresh_token field is required"
    },
    "request_id": "req_abc123xyz"
  }
}
```

**401 TOKEN_INVALID**
```json
{
  "error": {
    "code": "TOKEN_INVALID",
    "message": "Refresh token is invalid or malformed",
    "request_id": "req_abc123xyz"
  }
}
```
- Cause: Token format invalid, signature verification failed
- Action: Force re-authentication via Kakao login

**401 TOKEN_EXPIRED**
```json
{
  "error": {
    "code": "TOKEN_EXPIRED",
    "message": "Refresh token has expired. Please re-authenticate.",
    "request_id": "req_abc123xyz"
  }
}
```
- Cause: Refresh token older than 90 days
- Action: User must re-authenticate via Kakao login (show login screen)

**409 TOKEN_ROTATION_CONFLICT**
```json
{
  "error": {
    "code": "TOKEN_ROTATION_CONFLICT",
    "message": "This refresh token has already been rotated. Please re-authenticate.",
    "request_id": "req_abc123xyz"
  }
}
```
- Cause: Same refresh token used twice (token theft detected or client bug)
- Action: Force re-authentication via Kakao (security measure)

**429 RATE_LIMITED**
```json
{
  "error": {
    "code": "RATE_LIMITED",
    "message": "Too many refresh requests. Please wait.",
    "request_id": "req_abc123xyz"
  }
}
```
- Cause: Exceeded rate limit (>50 req/min per user)
- Action: Implement exponential backoff

---

## Error Handling

### Global Error Response Format

All errors follow the same structure:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable message (in Korean for MVPn)",
    "details": {
      "field_name": "field-specific error (optional)",
      "another_field": "another error message (optional)"
    },
    "request_id": "req_abc123xyz"
  }
}
```

### Error Handling Principles

1. **Fail Fast:** Validate input immediately; reject before processing
2. **Specific Codes:** Use precise error codes for programmatic handling
3. **User-Friendly Messages:** Korean language, clear, actionable guidance
4. **No Sensitive Data:** Never expose token values, SQL queries, internal paths
5. **Include Request ID:** All errors include X-Request-ID for server-side debugging
6. **Structured Validation Errors:** Field-level error details in `details` object

### Retry Logic (Client Responsibility)

**Retryable Errors (implement exponential backoff):**
- `502 KAKAO_UPSTREAM_ERROR` (Kakao service down)
- `503 SERVICE_UNAVAILABLE` (temporary service outage)
- Network timeout (HTTP level)

**Non-Retryable Errors (fail immediately):**
- `400 INVALID_CODE` (code will never become valid)
- `400 VALIDATION_ERROR` (user input issue)
- `401 UNAUTHORIZED` (token refresh or re-auth needed)
- `409 PROFILE_ALREADY_SET` (idempotent, safe to ignore)

**Rate Limit Retry:**
- Check `Retry-After` response header
- Default backoff: 2^attempt seconds (max 300 seconds)

---

## Rate Limiting

### Limits by Endpoint

| Endpoint | Limit | Window | Per | Notes |
|----------|-------|--------|-----|-------|
| `POST /auth/kakao` | 10 | 1 minute | IP | Prevents brute force code guessing |
| `POST /auth/refresh` | 50 | 1 minute | User | Prevents token refresh spam |
| `POST /users/profile` | 5 | 1 minute | User | Prevents duplicate profile creation |
| `GET /users/me` | 100 | 1 minute | User | Allows frequent profile checks |

### Rate Limit Response Headers (All Responses)

```http
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 9
X-RateLimit-Reset: 1700425200
```

**Header Descriptions:**
- `X-RateLimit-Limit`: Max requests allowed in window
- `X-RateLimit-Remaining`: Requests remaining until reset
- `X-RateLimit-Reset`: Unix timestamp when counter resets

### Rate Limit Exceeded Response

**HTTP 429 TOO_MANY_REQUESTS**
```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/json
Retry-After: 60
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1700425200

{
  "error": {
    "code": "RATE_LIMITED",
    "message": "Too many requests. Please wait before trying again.",
    "request_id": "req_abc123xyz"
  }
}
```

**Header Fields:**
- `Retry-After`: Seconds to wait before retrying (always <= 300)
- `X-RateLimit-Reset`: Unix timestamp when limit resets

---

## Token Structure

### Access Token

**Type:** JWT (JSON Web Token)

**Expiration:** 30 days (2,592,000 seconds)

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "kakao_id": "1234567890",
  "iat": 1700400225,
  "exp": 1703078625,
  "aud": "memoir-mobile-app",
  "iss": "memoir-backend",
  "type": "access"
}
```

**Payload Fields:**
| Field | Type | Description |
|-------|------|-------------|
| `sub` | UUID | Subject (user_id) |
| `kakao_id` | String | Kakao user ID for cross-reference |
| `iat` | Number | Issued at (Unix timestamp) |
| `exp` | Number | Expiration time (Unix timestamp) |
| `aud` | String | Audience (memoir-mobile-app) |
| `iss` | String | Issuer (memoir-backend) |
| `type` | String | Token type (access) |

### Refresh Token

**Type:** JWT (JSON Web Token)

**Expiration:** 90 days (7,776,000 seconds)

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "type": "refresh",
  "rotation_counter": 1,
  "iat": 1700400225,
  "exp": 1708264225,
  "aud": "memoir-mobile-app",
  "iss": "memoir-backend"
}
```

**Payload Fields:**
| Field | Type | Description |
|-------|------|-------------|
| `sub` | UUID | Subject (user_id) |
| `type` | String | Token type (refresh) |
| `rotation_counter` | Number | Incremented on each rotation (detect reuse) |
| `iat` | Number | Issued at (Unix timestamp) |
| `exp` | Number | Expiration time (Unix timestamp) |
| `aud` | String | Audience (memoir-mobile-app) |
| `iss` | String | Issuer (memoir-backend) |

### Token Security

1. **Signing Algorithm:** HS256 (HMAC-SHA256)
2. **Secret Key:** Stored in environment variable `JWT_SECRET` (never in code)
3. **Encryption:** Tokens transmitted over HTTPS only
4. **Client Storage:** Encrypted DataStore (Android Keystore encryption)
5. **Server Storage:** Refresh tokens hashed with bcrypt before database persistence
6. **Rotation:** Old refresh tokens invalidated immediately after new pair issued
7. **Blacklist:** Optional implementation for logout (Phase 2)

---

## TypeScript Types

### Request Types

```typescript
// POST /auth/kakao
export interface KakaoTokenExchangeRequest {
  kakao_code: string;
  device_id?: string;
}

// POST /users/profile
export interface CreateUserProfileRequest {
  name: string;           // Hangul 2-4 chars
  role: string;          // 2-48 chars
  industry_code: IndustryCode;
  growth_goals: string;  // 100-500 chars
}

// POST /auth/refresh
export interface RefreshTokenRequest {
  refresh_token: string;
}
```

### Response Types

```typescript
// POST /auth/kakao - 200 OK
export interface KakaoTokenExchangeResponse {
  data: {
    access_token: string;
    refresh_token: string;
    access_token_expires_in: number;      // seconds
    refresh_token_expires_in: number;     // seconds
    user_id: string;                      // UUID
    user_exists: boolean;
    onboarding_completed: boolean;
    kakao_id: string;
  };
}

// POST /users/profile - 201 CREATED
export interface CreateUserProfileResponse {
  data: {
    user_id: string;
    profile: UserProfile;
  };
}

// GET /users/me - 200 OK
export interface GetUserMeResponse {
  data: {
    user: User;
    profile: UserProfile | null;
    flags: {
      onboarding_completed: boolean;
      profile_locked: boolean;
    };
  };
}

// POST /auth/refresh - 200 OK
export interface RefreshTokenResponse {
  data: {
    access_token: string;
    refresh_token: string;
    access_token_expires_in: number;
    refresh_token_expires_in: number;
  };
}

// Error Response - All endpoints (4xx, 5xx)
export interface ErrorResponse {
  error: {
    code: string;
    message: string;
    details?: Record<string, string>;
    request_id: string;
  };
}
```

### Data Models

```typescript
export enum IndustryCode {
  STARTUP = "STARTUP",
  ENTERPRISE = "ENTERPRISE",
  SME = "SME",
  PUBLIC = "PUBLIC",
  FOREIGN = "FOREIGN",
  FREELANCER = "FREELANCER",
  NONPROFIT = "NONPROFIT",
  OTHER = "OTHER"
}

export interface User {
  id: string;              // UUID
  kakao_id: string;        // Kakao user ID
  created_at: string;      // ISO 8601 timestamp
  last_login_at: string;   // ISO 8601 timestamp
  status: "active" | "inactive" | "locked";
}

export interface UserProfile {
  id: string;              // UUID
  user_id: string;         // FK to User
  name: string;            // Hangul name
  role: string;            // Job title
  industry_code: IndustryCode;
  growth_goals: string;    // Free text
  profile_photo_url: string | null;
  onboarding_completed_at: string | null;  // ISO 8601 timestamp
  profile_locked: boolean;
  created_at: string;      // ISO 8601 timestamp
  updated_at: string;      // ISO 8601 timestamp
}

// JWT Payload Types (for decode/verify)
export interface AccessTokenPayload {
  sub: string;             // user_id
  kakao_id: string;
  iat: number;
  exp: number;
  aud: "memoir-mobile-app";
  iss: "memoir-backend";
  type: "access";
}

export interface RefreshTokenPayload {
  sub: string;             // user_id
  type: "refresh";
  rotation_counter: number;
  iat: number;
  exp: number;
  aud: "memoir-mobile-app";
  iss: "memoir-backend";
}
```

---

## cURL Examples

### 1. Kakao OAuth Token Exchange

**Request:**
```bash
curl -X POST https://api.memoir.kr/api/v1/auth/kakao \
  -H "Content-Type: application/json" \
  -H "X-Request-ID: req_abc123xyz" \
  -d '{
    "kakao_code": "KakaoOAuthCodeFromSDK",
    "device_id": "device_uuid_12345"
  }'
```

**Success Response (200 OK):**
```json
{
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJrYWthb19pZCI6IjEyMzQ1Njc4OTAiLCJpYXQiOjE3MDA0MDAyMjUsImV4cCI6MTcwMzA3ODYyNSwiYXVkIjoibWVtb2lyLW1vYmlsZS1hcHAiLCJpc3MiOiJtZW1vaXItYmFja2VuZCIsInR5cGUiOiJhY2Nlc3MifQ.Sig123...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJ0eXBlIjoicmVmcmVzaCIsInJvdGF0aW9uX2NvdW50ZXIiOjEsImlhdCI6MTcwMDQwMDIyNSwiZXhwIjoxNzA4MjY0MjI1LCJhdWQiOiJtZW1vaXItbW9iaWxlLWFwcCIsImlzcyI6Im1lbW9pci1iYWNrZW5kIn0.Sig456...",
    "access_token_expires_in": 2592000,
    "refresh_token_expires_in": 7776000,
    "user_id": "550e8400-e29b-41d4-a716-446655440000",
    "user_exists": false,
    "onboarding_completed": false,
    "kakao_id": "1234567890"
  }
}
```

**Error Response (429 RATE_LIMITED):**
```bash
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1700425200
Retry-After: 60

{
  "error": {
    "code": "RATE_LIMITED",
    "message": "Too many requests. Please wait before trying again.",
    "request_id": "req_abc123xyz"
  }
}
```

---

### 2. Create User Profile

**Request:**
```bash
curl -X POST https://api.memoir.kr/api/v1/users/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "X-Request-ID: req_def456uvw" \
  -d '{
    "name": "김태희",
    "role": "Product Manager",
    "industry_code": "STARTUP",
    "growth_goals": "일상 속 배움을 구조적으로 기록하고 다양한 분야의 전문가들과 나누고 싶습니다. 특히 사용자 중심의 제품 철학을 배우고 싶어요."
  }'
```

**Success Response (201 CREATED):**
```json
{
  "data": {
    "user_id": "550e8400-e29b-41d4-a716-446655440000",
    "profile": {
      "id": "550e8401-e29b-41d4-a716-446655440001",
      "user_id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "김태희",
      "role": "Product Manager",
      "industry_code": "STARTUP",
      "growth_goals": "일상 속 배움을 구조적으로 기록하고 다양한 분야의 전문가들과 나누고 싶습니다. 특히 사용자 중심의 제품 철학을 배우고 싶어요.",
      "profile_photo_url": null,
      "onboarding_completed_at": "2025-11-19T14:23:45Z",
      "profile_locked": true,
      "created_at": "2025-11-19T14:23:45Z",
      "updated_at": "2025-11-19T14:23:45Z"
    }
  }
}
```

**Error Response (400 VALIDATION_ERROR):**
```bash
HTTP/1.1 400 Bad Request

{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Input validation failed",
    "details": {
      "name": "이름은 한글 2-4글자만 허용됩니다",
      "growth_goals": "성장 목표는 100-500글자여야 합니다"
    },
    "request_id": "req_def456uvw"
  }
}
```

**Error Response (409 PROFILE_ALREADY_SET):**
```bash
HTTP/1.1 409 Conflict

{
  "error": {
    "code": "PROFILE_ALREADY_SET",
    "message": "User profile already exists. Cannot update (Phase 1: profiles are locked)",
    "request_id": "req_def456uvw"
  }
}
```

---

### 3. Get User Information

**Request:**
```bash
curl -X GET https://api.memoir.kr/api/v1/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "X-Request-ID: req_ghi789xyz"
```

**Success Response (200 OK):**
```json
{
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "kakao_id": "1234567890",
      "created_at": "2025-11-19T14:20:00Z",
      "last_login_at": "2025-11-19T14:23:00Z",
      "status": "active"
    },
    "profile": {
      "id": "550e8401-e29b-41d4-a716-446655440001",
      "user_id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "김태희",
      "role": "Product Manager",
      "industry_code": "STARTUP",
      "growth_goals": "일상 속 배움을 구조적으로 기록하고 다양한 분야의 전문가들과 나누고 싶습니다.",
      "profile_photo_url": null,
      "onboarding_completed_at": "2025-11-19T14:23:45Z",
      "profile_locked": true,
      "created_at": "2025-11-19T14:23:45Z"
    },
    "flags": {
      "onboarding_completed": true,
      "profile_locked": true
    }
  }
}
```

**Error Response (401 UNAUTHORIZED):**
```bash
HTTP/1.1 401 Unauthorized

{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or expired access token",
    "request_id": "req_ghi789xyz"
  }
}
```

---

### 4. Refresh Access Token

**Request:**
```bash
curl -X POST https://api.memoir.kr/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -H "X-Request-ID: req_jkl012mno" \
  -d '{
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJ0eXBlIjoicmVmcmVzaCIsInJvdGF0aW9uX2NvdW50ZXIiOjEsImlhdCI6MTcwMDQwMDIyNSwiZXhwIjoxNzA4MjY0MjI1LCJhdWQiOiJtZW1vaXItbW9iaWxlLWFwcCIsImlzcyI6Im1lbW9pci1iYWNrZW5kIn0.Sig456..."
  }'
```

**Success Response (200 OK):**
```json
{
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJrYWthb19pZCI6IjEyMzQ1Njc4OTAiLCJpYXQiOjE3MDA0ODY2MjUsImV4cCI6MTcwMzE2NTAyNSwiYXVkIjoibWVtb2lyLW1vYmlsZS1hcHAiLCJpc3MiOiJtZW1vaXItYmFja2VuZCIsInR5cGUiOiJhY2Nlc3MifQ.NewSig...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJ0eXBlIjoicmVmcmVzaCIsInJvdGF0aW9uX2NvdW50ZXIiOjIsImlhdCI6MTcwMDQ4NjYyNSwiZXhwIjoxNzA4MzUwNjI1LCJhdWQiOiJtZW1vaXItbW9iaWxlLWFwcCIsImlzcyI6Im1lbW9pci1iYWNrZW5kIn0.NewSig...",
    "access_token_expires_in": 2592000,
    "refresh_token_expires_in": 7776000
  }
}
```

**Error Response (401 TOKEN_EXPIRED):**
```bash
HTTP/1.1 401 Unauthorized

{
  "error": {
    "code": "TOKEN_EXPIRED",
    "message": "Refresh token has expired. Please re-authenticate.",
    "request_id": "req_jkl012mno"
  }
}
```

---

## HTTP Status Codes

### Comprehensive Status Code Table

| Code | Status | Endpoints | Condition | Client Action |
|------|--------|-----------|-----------|----------------|
| **200** | OK | GET /users/me, POST /auth/refresh | Request successful | Continue |
| **201** | Created | POST /users/profile | Resource created successfully | Continue |
| **400** | Bad Request | All | Invalid request format, validation error, missing field | Fix request, retry |
| **401** | Unauthorized | All protected, /auth/refresh | Invalid/expired token, missing auth header | Refresh token or re-login |
| **409** | Conflict | POST /users/profile, POST /auth/refresh | Resource conflict, token rotation conflict | Show user message, don't retry |
| **429** | Too Many Requests | All | Rate limit exceeded | Wait, then retry with backoff |
| **500** | Internal Server Error | All | Unexpected server error | Retry after waiting |
| **502** | Bad Gateway | POST /auth/kakao | Upstream service error (Kakao) | Retry with backoff |
| **503** | Service Unavailable | All | Backend temporarily down | Retry after waiting |

### Specific Status Codes by Endpoint

**POST /auth/kakao**
```
200 - Success
400 - Invalid code, invalid request body
429 - Rate limited (>10 req/min per IP)
502 - Kakao API error
503 - Service unavailable
```

**POST /users/profile**
```
201 - Profile created successfully
400 - Validation error (invalid name, role, goals, industry)
401 - Unauthorized (missing/invalid access token)
409 - Profile already exists (idempotent)
429 - Rate limited (>5 req/min per user)
500 - Server error
```

**GET /users/me**
```
200 - Success
401 - Unauthorized
404 - Not found (rare)
```

**POST /auth/refresh**
```
200 - New tokens issued
400 - Invalid request format
401 - Invalid/expired refresh token
409 - Token rotation conflict (security)
429 - Rate limited (>50 req/min per user)
```

---

## Security Headers

### Request Security Headers (Client sends)

All authenticated requests should include:

```http
Authorization: Bearer <access_token>
X-Request-ID: <unique-request-id>
Content-Type: application/json
User-Agent: Memoir-Android/1.0.0
Accept: application/json
```

### Response Security Headers (Server sends)

All API responses include:

```http
Content-Type: application/json; charset=utf-8
X-Request-ID: <unique-request-id>
X-RateLimit-Limit: <limit>
X-RateLimit-Remaining: <remaining>
X-RateLimit-Reset: <unix-timestamp>
Cache-Control: no-store, no-cache, must-revalidate, max-age=0
Pragma: no-cache
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

### Authentication Header Format

**Valid:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Invalid (will reject):**
```
Authorization: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...  (missing "Bearer ")
Authorization: Basic base64string...  (wrong scheme)
Authorization: Bearer  (empty token)
X-Access-Token: eyJhbGc...  (wrong header name)
```

---

## Performance Targets

### Response Time Goals (p95 latency)

| Endpoint | Target | Notes |
|----------|--------|-------|
| POST /auth/kakao | <1.5s | Network-dependent; Kakao API roundtrip |
| POST /users/profile | <800ms | Database write + email trigger |
| GET /users/me | <500ms | Single database query |
| POST /auth/refresh | <300ms | Lightweight token generation |

### Throughput Goals (Phase 1 Pilot)

- Concurrent login requests: 50+
- Database connections: 20
- Token refresh capacity: 100 req/sec

### Monitoring & Alerting

**Key Metrics to Monitor:**
- Auth endpoint response time (p50, p95, p99)
- Error rate by endpoint (target: <0.5%)
- Rate limit hit rate (target: <1%)
- Kakao upstream error rate (target: <2%)
- Token refresh success rate (target: >99%)

**Critical Alerts:**
- Auth endpoint error rate >5% in 5 minutes -> Slack alert
- Kakao API unreachable for >2 minutes -> Slack alert
- Response time p95 >2s -> Log warning

---

## Sequence Diagrams

### Sequence 1: Kakao Login & Token Exchange

```
User/Client                     Backend API           Kakao API
     |                               |                     |
     |--[1] Tap Kakao Login--------->|                     |
     |                               |                     |
     |<--[2] Kakao Auth Dialog-------|                     |
     |        (redirects to Kakao)   |                     |
     |                               |                     |
     |                          (User authorizes on Kakao)
     |                               |                     |
     |--[3] kakao_code + payload--->|                     |
     |     (from Kakao SDK)          |                     |
     |                               |--[4] Exchange-------->|
     |                               |     code -> token    |
     |                               |                     |
     |                               |<--[5] Return----------|
     |                               |     access_token      |
     |                               |                     |
     |                          (Backend:                  |
     |                           Validate token,
     |                           Create/Get User,
     |                           Issue JWT pair)
     |                               |
     |<--[6] Success Response--------|
     |      {access_token,           |
     |       refresh_token,          |
     |       user_exists,            |
     |       onboarding_completed}   |
     |                               |
    [Client stores tokens in encrypted DataStore]
     |                               |
    [If onboarding_completed=false, show onboarding flow]
     |                               |
```

**Timing:**
- Step 4-5 (Kakao exchange): 200-800ms
- Step 6 (Database + JWT generation): 100-300ms
- Total: 300-1100ms (target p95: <1500ms)

---

### Sequence 2: Profile Creation with Token Auth

```
User/Client                  Backend API              Database
     |                            |                        |
     |--[1] Submit Profile------->|                        |
     |      Bearer access_token   |                        |
     |      {name, role,          |                        |
     |       industry, goals}      |                        |
     |                            |                        |
     |                       (Backend:                     |
     |                        [2a] Verify JWT             |
     |                        [2b] Extract user_id        |
     |                        [2c] Validate inputs)       |
     |                            |                        |
     |                            |--[3] Check profile--->|
     |                            |     exists?            |
     |                            |                        |
     |                            |<--[4] Row exists?-----|
     |                            |     (409 conflict)     |
     |                            |                    OR  |
     |                            |<--[4b] No profile-----|
     |                            |     (proceed)          |
     |                            |                        |
     |                            |--[5] Insert profile--->|
     |                            |     with timestamp     |
     |                            |                        |
     |                            |<--[6] Success---------|
     |                            |                        |
     |<--[7] 201 Created---------|
     |      {user_id,            |
     |       profile,            |
     |       onboarding_         |
     |        completed_at}       |
     |                            |
   [Client marks onboarding done locally]
     |                            |
```

**Timing:**
- Token verification: 10-20ms
- Input validation: 20-50ms
- Database check: 30-100ms
- Database insert: 40-150ms
- Total: 100-320ms (target p95: <800ms)

---

### Sequence 3: Token Refresh with Rotation

```
User/Client                  Backend API              Database
     |                            |                        |
     |--[1] POST /refresh-------->|                        |
     |      {refresh_token}       |                        |
     |                            |                        |
     |                       (Backend:                     |
     |                        [2a] Parse JWT
     |                        [2b] Verify signature
     |                        [2c] Check expiration
     |                        [2d] Extract user_id)
     |                            |                        |
     |                            |--[3] Query token----->|
     |                            |     rotation_counter   |
     |                            |                        |
     |                            |<--[4] Get current------|
     |                            |     rotation_counter   |
     |                            |                        |
     |                       (Backend:                     |
     |                        [5a] Compare counters
     |                        [5b] If mismatch:
     |                             409 CONFLICT (theft!)
     |                        [5c] If match:
     |                             Increment counter)
     |                            |                        |
     |                            |--[6] Update rotation->|
     |                            |     counter+1          |
     |                            |                        |
     |                       (Backend:                     |
     |                        [7a] Generate new
     |                             access_token
     |                        [7b] Generate new
     |                             refresh_token
     |                        [7c] Hash new refresh)
     |                            |                        |
     |<--[8] 200 OK------------|
     |      {new_access_token,   |
     |       new_refresh_token,   |
     |       expires_in}          |
     |                            |
   [Client stores new tokens,
    discards old tokens]
     |                            |
```

**Security Note:** If same refresh token used twice:
- First request: 409 TOKEN_ROTATION_CONFLICT
- Indicates token theft or client bug
- User should re-authenticate immediately

---

### Sequence 4: Unauthorized Request (Expired Token)

```
User/Client                  Backend API
     |                            |
     |--[1] GET /users/me-------->|
     |      Bearer expired_token  |
     |                            |
     |                       (Backend:
     |                        [2a] Parse JWT
     |                        [2b] Verify signature ✓
     |                        [2c] Check exp < now
     |                             Expired! ✗)
     |                            |
     |<--[3] 401 UNAUTHORIZED----|
     |      {code: UNAUTHORIZED,  |
     |       message: "Invalid or |
     |        expired token"}     |
     |                            |
   [Client should:
    1. Try refresh_token
    2. If refresh fails: redirect to login]
     |                            |
```

---

## Test Cases

### Unit Test Cases

#### Test Group 1: Input Validation

**TC1.1 - Valid Korean Name**
- Input: `name: "김태희"`
- Expected: Pass validation
- Status: Success (201 Created)

**TC1.2 - Valid Korean Name (4 chars)**
- Input: `name: "이순신장군"`
- Expected: Pass validation
- Status: Success (201 Created)

**TC1.3 - Invalid Name (English)**
- Input: `name: "KimTaeHee"`
- Expected: Fail validation
- Error: `400 VALIDATION_ERROR` with message "이름은 한글 2-4글자만 허용됩니다"

**TC1.4 - Invalid Name (Too Short)**
- Input: `name: "김"`
- Expected: Fail validation
- Error: `400 VALIDATION_ERROR`

**TC1.5 - Invalid Name (Too Long)**
- Input: `name: "김태희이순"`
- Expected: Fail validation
- Error: `400 VALIDATION_ERROR`

**TC1.6 - Valid Role (Korean)**
- Input: `role: "상품 매니저"`
- Expected: Pass validation
- Status: Success

**TC1.7 - Valid Role (English)**
- Input: `role: "Product Manager"`
- Expected: Pass validation
- Status: Success

**TC1.8 - Valid Industry Code**
- Input: `industry_code: "STARTUP"`
- Expected: Pass validation
- Status: Success

**TC1.9 - Invalid Industry Code**
- Input: `industry_code: "INVALID_CODE"`
- Expected: Fail validation
- Error: `400 VALIDATION_ERROR`

**TC1.10 - Valid Growth Goals (100 chars)**
- Input: `growth_goals: "일상 속 배움을 구조적으로 기록하고 다양한 분야의 전문가들과 나누고 싶습니다. 특히 사용자 중심의 제품 철학을 배우고 싶어요."`
- Expected: Pass validation
- Status: Success

**TC1.11 - Invalid Growth Goals (99 chars)**
- Input: `growth_goals: "일상 속 배움을 구조적으로 기록하고 다양한 분야의 전문가들과 나누고 싶습니다. 특히 사용자 중심의 제품 철"`
- Expected: Fail validation
- Error: `400 VALIDATION_ERROR` with message "성장 목표는 100-500글자여야 합니다"

**TC1.12 - Invalid Growth Goals (>500 chars)**
- Input: `growth_goals: "[501+ chars]"`
- Expected: Fail validation
- Error: `400 VALIDATION_ERROR`

---

#### Test Group 2: Authentication

**TC2.1 - Valid Access Token**
- Request: `GET /users/me` with valid access token
- Expected: 200 OK with user data
- Status: Pass

**TC2.2 - Missing Authorization Header**
- Request: `GET /users/me` without Authorization header
- Expected: 401 UNAUTHORIZED
- Error: "Invalid or expired access token"

**TC2.3 - Invalid Token Format**
- Request: `GET /users/me` with `Authorization: InvalidToken`
- Expected: 401 UNAUTHORIZED
- Error: "Invalid or expired access token"

**TC2.4 - Expired Access Token**
- Request: `GET /users/me` with expired access token (exp < now)
- Expected: 401 UNAUTHORIZED
- Error: "Invalid or expired access token"

**TC2.5 - Refresh Token Success**
- Request: `POST /auth/refresh` with valid refresh token
- Expected: 200 OK with new token pair
- Status: Pass

**TC2.6 - Expired Refresh Token**
- Request: `POST /auth/refresh` with expired refresh token (exp < now, >90 days old)
- Expected: 401 TOKEN_EXPIRED
- Error: "Refresh token has expired. Please re-authenticate."

---

#### Test Group 3: Idempotency & Conflicts

**TC3.1 - First Profile Creation**
- Request: `POST /users/profile` for new user
- Expected: 201 CREATED
- Status: Success

**TC3.2 - Duplicate Profile Creation (Same User)**
- Request: `POST /users/profile` second time for same user
- Expected: 409 PROFILE_ALREADY_SET
- Error: "User profile already exists. Cannot update"

**TC3.3 - Token Rotation Conflict**
- Request: `POST /auth/refresh` using same refresh token twice
- Expected: 409 TOKEN_ROTATION_CONFLICT on second request
- Error: "This refresh token has already been rotated"

---

#### Test Group 4: Rate Limiting

**TC4.1 - Within Rate Limit**
- Request: 1st request to `POST /auth/kakao`
- Expected: 200 OK
- Headers: `X-RateLimit-Remaining: 9`

**TC4.2 - Exceed Rate Limit**
- Request: 11th request to `POST /auth/kakao` within 60 seconds
- Expected: 429 TOO_MANY_REQUESTS
- Error: "Too many requests. Please wait before trying again."
- Header: `Retry-After: 60`

**TC4.3 - Rate Limit Per User**
- Setup: Different users, same IP
- Request: 11 requests from User A to `POST /auth/kakao`
- Expected: User B can still make requests (rate limit is per-IP for this endpoint)

**TC4.4 - Rate Limit Reset**
- Request: Hit rate limit, wait 61 seconds, try again
- Expected: 200 OK (counter reset)

---

#### Test Group 5: Kakao Integration

**TC5.1 - Valid Kakao Code**
- Request: `POST /auth/kakao` with valid code from Kakao SDK
- Expected: 200 OK with tokens
- Status: Success

**TC5.2 - Invalid Kakao Code**
- Request: `POST /auth/kakao` with fake/malformed code
- Expected: 400 INVALID_CODE
- Error: "Kakao authorization code is invalid or expired"

**TC5.3 - Expired Kakao Code**
- Request: `POST /auth/kakao` with code >10 minutes old
- Expected: 400 INVALID_CODE

**TC5.4 - Kakao API Downtime**
- Setup: Mock Kakao service to return 502
- Request: `POST /auth/kakao`
- Expected: 502 KAKAO_UPSTREAM_ERROR
- Error: "Kakao service temporarily unavailable"
- Action: Client should retry with exponential backoff

**TC5.5 - Kakao API Timeout**
- Setup: Mock Kakao service timeout (>5s)
- Request: `POST /auth/kakao`
- Expected: 502 KAKAO_UPSTREAM_ERROR
- Error: "Kakao service temporarily unavailable"

**TC5.6 - New User Creation**
- Request: `POST /auth/kakao` with new Kakao account
- Expected: 200 OK with `user_exists: false`
- Database: New User record created

**TC5.7 - Existing User Login**
- Setup: User already exists in database
- Request: `POST /auth/kakao` with same Kakao account
- Expected: 200 OK with `user_exists: true`
- Database: No new record; `last_login_at` updated

---

#### Test Group 6: Token Structure & Expiry

**TC6.1 - Access Token Expiry**
- Setup: Issue access token
- Expected: Expires in 30 days (2,592,000 seconds)
- Validation: `exp` claim = current_time + 2,592,000

**TC6.2 - Refresh Token Expiry**
- Setup: Issue refresh token
- Expected: Expires in 90 days (7,776,000 seconds)
- Validation: `exp` claim = current_time + 7,776,000

**TC6.3 - Token Rotation Counter**
- Setup: Refresh token initially issued
- Expected: `rotation_counter: 1` in payload
- After refresh: New token has `rotation_counter: 2`

**TC6.4 - Token Claims (Access)**
- Decode access token
- Expected claims:
  - `sub`: user_id (UUID)
  - `kakao_id`: Kakao user ID
  - `aud`: "memoir-mobile-app"
  - `iss`: "memoir-backend"
  - `type`: "access"

**TC6.5 - Token Claims (Refresh)**
- Decode refresh token
- Expected claims:
  - `sub`: user_id (UUID)
  - `type`: "refresh"
  - `rotation_counter`: number >= 1
  - `aud`: "memoir-mobile-app"
  - `iss`: "memoir-backend"

---

#### Test Group 7: Error Response Format

**TC7.1 - Validation Error Details**
- Request: `POST /users/profile` with invalid name and goals
- Expected: Error includes `details` object with both field errors
- Validation: `details.name` and `details.growth_goals` both present

**TC7.2 - Request ID Tracking**
- Request: Any request with custom `X-Request-ID`
- Expected: Response includes same `request_id` in error/success
- Validation: Can trace request in logs

**TC7.3 - User-Friendly Error Message**
- Request: Invalid input
- Expected: Error message is in Korean, actionable (not "TypeError")
- Validation: No technical jargon in message

---

#### Test Group 8: Response Envelope

**TC8.1 - Success Response Envelope**
- Request: Any successful request
- Expected: Response has `{ data: {...} }` wrapper
- Validation: No error field present

**TC8.2 - Error Response Envelope**
- Request: Any failed request
- Expected: Response has `{ error: {...} }` wrapper
- Validation: No data field present

**TC8.3 - Rate Limit Headers**
- Request: Any request
- Expected: Response includes:
  - `X-RateLimit-Limit`
  - `X-RateLimit-Remaining`
  - `X-RateLimit-Reset`

**TC8.4 - Cache Control Headers**
- Request: Any request
- Expected: Response includes:
  - `Cache-Control: no-store, no-cache, must-revalidate`
  - `Pragma: no-cache`

---

### Integration Test Cases

**ITC1 - Complete Onboarding Flow**
1. Client: `POST /auth/kakao` with valid code
2. Verify: Get tokens + `user_exists=false`, `onboarding_completed=false`
3. Client: `GET /users/me` with access token
4. Verify: `profile=null`, `flags.onboarding_completed=false`
5. Client: `POST /users/profile` with valid data
6. Verify: 201 CREATED, `onboarding_completed_at` set
7. Client: `GET /users/me` again
8. Verify: `profile` now populated, `flags.onboarding_completed=true`

**ITC2 - Token Refresh Flow**
1. Client: `POST /auth/kakao` to get initial tokens
2. Wait: Simulate >30 days passing (or mock expiration)
3. Client: Access token should be expired
4. Client: `POST /auth/refresh` with old refresh token
5. Verify: 200 OK with new access + refresh tokens
6. Verify: Old refresh token is invalid (409 if reused)
7. Client: Use new access token on `GET /users/me`
8. Verify: 200 OK (new token works)

**ITC3 - Concurrent Requests (Load Test)**
- Setup: 50 concurrent users doing `POST /auth/kakao`
- Expected: All requests complete within <1.5s
- Verify: No dropped connections, all get valid tokens

**ITC4 - Error Recovery**
- Setup: Kakao service is down (502)
- Client: Implement exponential backoff
  - Attempt 1: Fail immediately
  - Wait 1s, Attempt 2: Fail
  - Wait 2s, Attempt 3: Fail
  - Attempt 4: Service recovered, success
- Expected: Total time ~3 seconds for 3 retries

---

### End-to-End (E2E) Test Cases

**E2E1 - Full User Journey**
1. User taps "Kakao로 로그인"
2. Kakao login dialog appears
3. User authorizes on Kakao
4. App receives code
5. App calls `POST /auth/kakao`
6. Get tokens + `onboarding_completed=false`
7. Render onboarding flow
8. User progresses through 5 screens
9. User fills profile form
10. App validates locally (quick feedback)
11. App calls `POST /users/profile`
12. Backend validates again
13. Profile created
14. App calls `GET /users/me`
15. App navigates to main feed
16. App close and reopen
17. App checks `GET /users/me`
18. Verifies `onboarding_completed=true`
19. Goes directly to main feed (no onboarding re-shown)

**E2E2 - Session Expiry & Refresh**
1. User has valid session (access token)
2. After 30 days, token expires
3. User opens app
4. App attempts `GET /users/me` with expired token
5. Backend returns 401
6. App intercepts, calls `POST /auth/refresh`
7. Gets new token pair
8. App retries `GET /users/me` with new token
9. Success (transparent to user)
10. User continues uninterrupted

**E2E3 - Token Theft Detection**
1. Attacker steals refresh token
2. Attacker calls `POST /auth/refresh`
3. Success - attacker gets new token pair
4. Original device tries to refresh with old token
5. Backend detects: `rotation_counter` mismatch
6. Returns 409 TOKEN_ROTATION_CONFLICT
7. Legitimate user forced to re-authenticate
8. Attacker's new token pair is also compromised (rotate again next refresh)

---

## Implementation Checklist

### Backend Implementation

- [ ] Set up Express.js API server
- [ ] Configure JWT library (jsonwebtoken)
- [ ] Implement Kakao OAuth integration
- [ ] Create authentication middleware
- [ ] Set up database schema (User, UserProfile)
- [ ] Implement validation layer (server-side)
- [ ] Implement rate limiting middleware
- [ ] Add request ID tracking (X-Request-ID)
- [ ] Configure CORS for mobile client
- [ ] Implement error handling & response formatting
- [ ] Set up logging (structured JSON logs)
- [ ] Add monitoring/alerting
- [ ] Write unit tests (auth, validation, token logic)
- [ ] Write integration tests (endpoints + database)
- [ ] Load test auth endpoints (50+ concurrent)

### Client Implementation (Android)

- [ ] Integrate Kakao SDK
- [ ] Implement Retrofit API client
- [ ] Add token storage (encrypted DataStore)
- [ ] Implement token refresh logic
- [ ] Add request interceptor (Bearer token injection)
- [ ] Create error handling layer
- [ ] Implement retry logic with exponential backoff
- [ ] Add request ID generation & logging
- [ ] Build onboarding UI screens (5 screens)
- [ ] Implement profile form with validation
- [ ] Add loading states & error messages
- [ ] Write unit tests (validation, API types)
- [ ] Write UI tests (Compose preview, screenshot tests)
- [ ] Implement Firebase Analytics events
- [ ] Add crash tracking (Crashlytics)

---

## References

### Standards Compliance

This API specification adheres to:

- **RESTful Design:** Per `/agent-os/standards/backend/api.md`
- **Error Handling:** Per `/agent-os/standards/global/error-handling.md`
- **Validation:** Per `/agent-os/standards/global/validation.md`
- **API Conventions:** Consistent naming (lowercase, hyphenated), plural resources, appropriate HTTP methods
- **Tech Stack:** Node.js + Express + TypeScript + PostgreSQL per `/agent-os/standards/global/tech-stack.md`

### Related Documents

- Requirements: `/agent-os/specs/2025-11-19-user-authentication-onboarding/planning/requirements.md`
- Architecture: TBD (Phase 2 detailed design)
- Database Schema: TBD (migrations spec)

---

## Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-11-19 | API Spec Writer | Initial API specification for Phase 1 MVP |

---

## Appendix A: Common Error Scenarios

### Scenario: User Closes App During Profile Submission

1. User is on profile form
2. User taps "완료" (Submit)
3. App calls `POST /users/profile`
4. Response in progress, app force-closed
5. Profile may or may not be created on backend
6. Next app launch: App tries to load profile
7. `GET /users/me` shows `profile != null`
8. App navigates to main feed (idempotent)
9. No double submission (409 PROFILE_ALREADY_SET prevents duplicates)

### Scenario: Network Flaky During Token Refresh

1. App detects access token expired
2. App calls `POST /auth/refresh`
3. Request sent, server generates new tokens
4. Response packet lost (flaky network)
5. Client doesn't receive response
6. Client retries (with backoff)
7. Server has already rotated token counter
8. Old refresh_token now invalid
9. Client calls again with old token
10. Gets 409 TOKEN_ROTATION_CONFLICT
11. App must force re-authentication (show login)

### Scenario: Kakao Service Down

1. User taps "Kakao로 로그인"
2. Kakao SDK shows authorization dialog (cached)
3. User authorizes
4. App sends code to backend
5. Backend tries to exchange with Kakao (502 error)
6. App receives 502 KAKAO_UPSTREAM_ERROR
7. App shows toast: "Kakao 서비스를 이용할 수 없습니다. 잠시 후 다시 시도해주세요."
8. App implements exponential backoff
9. User can manually tap "Kakao로 로그인" again
10. If Kakao recovers, next attempt succeeds

---

## Appendix B: CORS Configuration

For Mobile Client (Android Retrofit):

```
CORS headers typically NOT needed for native mobile apps (no browser Same-Origin Policy)
If testing from web client or Postman:

Access-Control-Allow-Origin: * (or specific origin)
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Authorization, Content-Type, X-Request-ID
Access-Control-Max-Age: 86400
```

---

## Appendix C: Example Request/Response Cycle (Detailed)

### Step-by-Step: Kakao Login to Profile Submission

**1. Client Calls POST /auth/kakao**

```
Request:
POST /api/v1/auth/kakao HTTP/1.1
Host: api.memoir.kr
Content-Type: application/json
X-Request-ID: req_user123_001

{
  "kakao_code": "KakaoOAuthCodeFromSDK_xyz",
  "device_id": "android_device_uuid_123"
}

Response (200 OK):
{
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "access_token_expires_in": 2592000,
    "refresh_token_expires_in": 7776000,
    "user_id": "550e8400-e29b-41d4-a716-446655440000",
    "user_exists": false,
    "onboarding_completed": false,
    "kakao_id": "1234567890"
  }
}
```

**Client Actions:**
- Store `access_token` and `refresh_token` in encrypted DataStore
- Parse response: `onboarding_completed = false`
- Decision: Show onboarding flow

---

**2. Client Calls GET /users/me**

```
Request:
GET /api/v1/users/me HTTP/1.1
Host: api.memoir.kr
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
X-Request-ID: req_user123_002

Response (200 OK):
{
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "kakao_id": "1234567890",
      "created_at": "2025-11-19T14:20:00Z",
      "last_login_at": "2025-11-19T14:20:05Z",
      "status": "active"
    },
    "profile": null,
    "flags": {
      "onboarding_completed": false,
      "profile_locked": false
    }
  }
}
```

**Client Actions:**
- Confirm: `profile = null`
- Confirm: `onboarding_completed = false`
- Show onboarding screen 1 of 5

---

**3-5. User Views Onboarding Screens 1-5**

Client renders screens locally (no API calls)

---

**6. User Fills Profile Form**

Client validates locally:
- Name: "김태희" (Hangul, 2-4 chars) ✓
- Role: "상품 매니저" (2-48 chars) ✓
- Industry: "STARTUP" (valid enum) ✓
- Growth Goals: "일상 속 배움을..." (>100 chars) ✓

All local validations pass.

---

**7. Client Calls POST /users/profile**

```
Request:
POST /api/v1/users/profile HTTP/1.1
Host: api.memoir.kr
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
X-Request-ID: req_user123_003

{
  "name": "김태희",
  "role": "상품 매니저",
  "industry_code": "STARTUP",
  "growth_goals": "일상 속 배움을 구조적으로 기록하고 다양한 분야의 전문가들과 나누고 싶습니다. 특히 사용자 중심의 제품 철학을 배우고 싶어요."
}

Response (201 CREATED):
{
  "data": {
    "user_id": "550e8400-e29b-41d4-a716-446655440000",
    "profile": {
      "id": "550e8401-e29b-41d4-a716-446655440001",
      "user_id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "김태희",
      "role": "상품 매니저",
      "industry_code": "STARTUP",
      "growth_goals": "일상 속 배움을 구조적으로 기록하고 다양한 분야의 전문가들과 나누고 싶습니다. 특히 사용자 중심의 제품 철학을 배우고 싶어요.",
      "profile_photo_url": null,
      "onboarding_completed_at": "2025-11-19T14:23:45Z",
      "profile_locked": true,
      "created_at": "2025-11-19T14:23:45Z",
      "updated_at": "2025-11-19T14:23:45Z"
    }
  }
}
```

**Client Actions:**
- Mark onboarding complete locally (DataStore)
- Store profile in local Room database
- Record event: `onboarding_completed`
- Navigate to main feed (Cohort Feed screen)

---

**8. App Closed & Reopened (Next Day)**

**Client Checks Onboarding Status:**
- Read from local DataStore: `onboarding_completed = true`
- Skip login/onboarding screens
- Navigate directly to main feed

**Optional: Refresh Session Token**
- Check access token expiry
- If exp < now: Call `POST /auth/refresh`
- Otherwise: Skip refresh, use existing token
- Call `GET /users/me` to load user data
- Render main feed with user profile context

---

End of API Specification Document
