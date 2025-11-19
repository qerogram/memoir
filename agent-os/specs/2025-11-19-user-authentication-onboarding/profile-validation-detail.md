# Technical Specification: Profile Setup & Validation

## Overview

This document defines the detailed technical implementation for the Profile Setup & Validation module of the Memoir Android app. This module handles user profile collection during onboarding (Screen 5 of 5) with comprehensive client-side validation, server-side validation, and error handling.

**Module Scope:** Profile data collection, validation, storage, and submission during onboarding flow
**Target Platform:** Android (Kotlin + Jetpack Compose)
**Phase:** Phase 1 (MVP)

---

## 1. Field Validation Rules

### 1.1 Validation Rules Summary Table

| Field | Type | Required | Min Length | Max Length | Format | Regex | Error Message | Real-Time | Example |
|-------|------|----------|-----------|-----------|--------|-------|---------------|-----------|---------|
| **Name** | Text | Yes | 2 | 4 | Hangul only | `^[가-힣]{2,4}$` | "한글 이름 2-4글자를 입력해주세요" | Yes | "김태희" |
| **Professional Role** | Text | Yes | 2 | 48 | Korean + English + Space | `^[가-힣a-zA-Z\s]{2,48}$` | "2-48자의 직무를 입력해주세요" | Yes | "Product Manager" |
| **Industry** | Dropdown | Yes | N/A | N/A | Enum code | N/A | "산업을 선택해주세요" | No | "STARTUP" |
| **Growth Goals** | Text Area | Yes | 100 | 500 | Unicode | `^.{100,500}$` | "100-500자의 성장 목표를 입력해주세요" | Yes | Long text (100-500 chars) |
| **Profile Photo** | Image | No | N/A | 5MB | JPG, PNG | N/A | "5MB 이하의 JPG, PNG 파일을 선택해주세요" | No | image.jpg |

### 1.2 Detailed Field Specifications

#### 1.2.1 Name (이름)

**Purpose:** User's full name for display and cohort identification
**User Experience Flow:**
1. User enters text
2. Real-time validation as user types
3. Error message displays if format invalid
4. Valid state removes error message

**Validation Rules:**
- **Required:** Yes
- **Hangul Characters Only:** Accept only Korean hangul (한글)
- **Length:** 2-4 characters (typical Korean name format)
- **No Numbers, Symbols, or English:** Strict character set
- **Regex Pattern:** `^[가-힣]{2,4}$`
  - `가-힣`: Unicode range for Korean hangul (U+AC00 to U+D7A3)
  - `{2,4}`: Exactly 2 to 4 characters
- **Input Trimming:** Remove leading/trailing whitespace before validation
- **Rejection Examples:** "Kim", "김", "김태희임", "Kim태희", "김123"

**Error Messages:**
- Too short: "한글 이름 최소 2글자 이상 입력해주세요"
- Too long: "한글 이름 최대 4글자까지 입력 가능합니다"
- Invalid characters: "한글 이름만 입력 가능합니다 (특수문자, 숫자 불가)"
- Empty: "이름은 필수입니다"

**Valid Examples:**
- "김태희" (3글자) ✓
- "이준호" (3글자) ✓
- "박수연" (3글자) ✓
- "박신" (2글자) ✓
- "권석준혁" (4글자) ✓

#### 1.2.2 Professional Role (직무)

**Purpose:** User's job title or professional role for cohort matching and context
**User Experience Flow:**
1. User enters text with placeholder hint
2. Real-time validation and character count (displayed below field)
3. Error message displays if format invalid
4. Valid state removes error message

**Validation Rules:**
- **Required:** Yes
- **Character Set:** Korean hangul + English letters + Space
- **Length:** 2-48 characters
- **Regex Pattern:** `^[가-힣a-zA-Z\s]{2,48}$`
  - `[가-힣a-zA-Z\s]`: Korean hangul, English letters (upper/lower case), spaces
  - `{2,48}`: 2 to 48 characters
- **No Symbols or Numbers:** Strict character set (no @#$%, digits)
- **Space Handling:** Allow spaces between words; trim leading/trailing spaces
- **Input Trimming:** Remove leading/trailing whitespace before validation

**Error Messages:**
- Too short: "2자 이상의 직무를 입력해주세요"
- Too long: "48자 이하의 직무를 입력해주세요"
- Invalid characters: "한글, 영문, 공백만 입력 가능합니다 (숫자, 특수문자 불가)"
- Empty: "직무는 필수입니다"

**Valid Examples:**
- "Product Manager" ✓
- "마케팅 전략가" ✓
- "재무분석가" ✓
- "Software Engineer" ✓
- "UX Designer" ✓
- "PM" ✗ (only 2 chars, but acceptable at boundary)

**Invalid Examples:**
- "P" (too short)
- "Product Manager at Google Inc" (too long; 28 chars)
- "Product Manager 2" (contains number)
- "Product@Manager" (contains special character)

#### 1.2.3 Industry (산업)

**Purpose:** User's industry sector for cohort segmentation and networking
**User Experience Flow:**
1. Dropdown displays selected industry
2. User taps to expand dropdown list
3. User selects one option from list
4. Selection persists in field
5. Field displays selected Korean label and stores enum code

**Validation Rules:**
- **Required:** Yes
- **Type:** Single-select dropdown (cannot multi-select)
- **Options:** 8 predefined options (enum-based, not free text)
- **Default State:** "산업을 선택해주세요" (placeholder)
- **Storage Format:** Enum code (English), Display Format: Korean label

**Industry Options (Priority Order):**

| Display Label | Enum Code | Typical Users |
|---------------|-----------|---------------|
| 스타트업 | STARTUP | Early-stage venture founders, early employees |
| 대기업 | ENTERPRISE | Large conglomerate/corporate employees |
| 중소기업 | SME | Small/medium enterprise staff |
| 공기업/공공기관 | PUBLIC | Government, public corporation employees |
| 외국계 기업 | FOREIGN | Foreign company subsidiaries in Korea |
| 프리랜서/1인 기업 | FREELANCER | Solopreneur, independent contractor |
| 비영리/사회적 기업 | NONPROFIT | NGO, social enterprise staff |
| 기타 | OTHER | None of the above |

**Validation Rules:**
- Must select exactly one option
- Selection cannot be null when form is submitted
- Invalid selection (outside enum) rejected on server side

**Error Messages:**
- Empty selection: "산업을 선택해주세요"
- Invalid code: "유효한 산업을 선택해주세요" (server validation)

#### 1.2.4 Growth Goals (성장 목표)

**Purpose:** User's personal/professional growth objectives for cohort alignment and self-reflection
**User Experience Flow:**
1. User enters multi-line text
2. Character counter displays real-time (e.g., "156/500")
3. Counter turns red if <100 chars
4. "계속" (Continue) button disabled if <100 chars
5. Placeholder text provides guidance
6. User can expand text area as needed
7. Error message displays if submission attempted with <100 chars

**Validation Rules:**
- **Required:** Yes
- **Character Set:** Unicode characters (Korean + English + symbols allowed)
- **Length:** Minimum 100 characters, Maximum 500 characters
- **Regex Pattern:** `^.{100,500}$`
  - `.`: Any character (Unicode)
  - `{100,500}`: 100 to 500 characters
- **Newlines Allowed:** Yes (multi-line input supported)
- **Input Trimming:** Remove leading/trailing whitespace before validation
- **Length Calculation:** Count visible characters (including spaces, newlines)

**Placeholder Text:**
"일상 속 배움을 구조적으로 기록하고, 다양한 분야의 사람들과 공유하면서 시야를 넓히고 싶습니다. 특히 PM으로서 고객 이해도를 깊게 하고, 리더십 역량을 개발하는 것이 목표입니다."

**Error Messages:**
- Too short (character count < 100): "최소 100자 이상 입력해주세요"
- Too long (character count > 500): "최대 500자까지 입력 가능합니다"
- Empty: "성장 목표는 필수입니다"

**Valid Examples:**
- "일상 속 배움을 구조적으로 기록하고, 다양한 분야의 사람들과 공유하면서 시야를 넓히고 싶습니다. 특히 PM으로서 고객 이해도를 깊게 하고, 리더십 역량을 개발하는 것이 목표입니다." (127 chars) ✓

**Invalid Examples:**
- "성장하고 싶습니다" (9 chars; too short)
- "성장하고 싶습니다..." (repeated 50 times; >500 chars)

**UI Enhancements:**
- Real-time character counter: "123/500" (gray text below text area)
- Counter color change: Red when <100 chars, Gray when 100-500 chars
- Continue button state: Disabled (grayed out) when <100 chars
- Word wrap: Text area expands to show full content (auto-sizing)

#### 1.2.5 Profile Photo (프로필 사진)

**Purpose:** User's profile picture for identification within cohort
**User Experience Flow:**
1. User sees optional field with placeholder image
2. User can tap to open file picker or camera
3. User selects/captures image
4. Image preview displays
5. If image too large/wrong format, error message shows
6. User can skip this field and proceed

**Validation Rules:**
- **Required:** No (optional, can skip)
- **File Format:** JPG (JPEG) or PNG only
  - MIME types: `image/jpeg`, `image/png`
  - Extensions: `.jpg`, `.jpeg`, `.png`
- **File Size:** Maximum 5MB (5,242,880 bytes)
- **Recommended Resolution:** 400x400px (square format)
- **Aspect Ratio:** Any aspect ratio accepted, but recommend square for profile photo
- **Note for Phase 1:** Photo upload deferred to Phase 2; MVP shows placeholder avatar

**Error Messages:**
- Wrong format: "JPG 또는 PNG 파일만 업로드 가능합니다"
- File too large: "5MB 이하의 파일을 선택해주세요"
- No file selected: "(선택사항이므로 건너뛸 수 있음)"

**Valid Examples:**
- profile.jpg (2MB, 400x400px) ✓
- profile.png (3MB, 600x600px) ✓

**Invalid Examples:**
- profile.gif (wrong format)
- profile.jpg (6MB; too large)
- profile.pdf (wrong format)

**Phase 1 MVP Implementation:**
- Display "사진은 나중에 업로드할 수 있습니다" (Photo can be uploaded later)
- Show placeholder avatar or icon
- Skip button prominently displayed
- Profile photo upload endpoint not implemented until Phase 2

---

## 2. Form State Management

### 2.1 ProfileFormState Data Class (Kotlin)

```kotlin
data class ProfileFormState(
    // Name field
    val name: String = "",
    val nameError: String? = null,
    val nameIsFocused: Boolean = false,

    // Professional role field
    val role: String = "",
    val roleError: String? = null,
    val roleIsFocused: Boolean = false,

    // Industry field
    val industry: IndustryCode? = null,
    val industryError: String? = null,

    // Growth goals field
    val growthGoals: String = "",
    val growthGoalsError: String? = null,
    val growthGoalsIsFocused: Boolean = false,
    val growthGoalsCharacterCount: Int = 0,

    // Profile photo field (Phase 2)
    val photoUri: Uri? = null,
    val photoError: String? = null,

    // Form-level states
    val isSubmitting: Boolean = false,
    val isFormValid: Boolean = false,
    val submitError: String? = null,
    val submitSuccess: Boolean = false
)

// Industry enum
enum class IndustryCode {
    STARTUP,        // 스타트업
    ENTERPRISE,     // 대기업
    SME,            // 중소기업
    PUBLIC,         // 공기업/공공기관
    FOREIGN,        // 외국계 기업
    FREELANCER,     // 프리랜서/1인 기업
    NONPROFIT,      // 비영리/사회적 기업
    OTHER           // 기타
}

// Mapping for display labels
fun IndustryCode.toDisplayLabel(): String = when (this) {
    IndustryCode.STARTUP -> "스타트업"
    IndustryCode.ENTERPRISE -> "대기업"
    IndustryCode.SME -> "중소기업"
    IndustryCode.PUBLIC -> "공기업/공공기관"
    IndustryCode.FOREIGN -> "외국계 기업"
    IndustryCode.FREELANCER -> "프리랜서/1인 기업"
    IndustryCode.NONPROFIT -> "비영리/사회적 기업"
    IndustryCode.OTHER -> "기타"
}
```

### 2.2 Form State Flow Diagram

```
Initial State
    ↓
User Input Detected (name, role, role, growth goals)
    ↓
Field Validation Triggered (on focus lost or real-time)
    ↓
Update Field Error (if invalid) / Clear Error (if valid)
    ↓
Evaluate Form Validity (all required fields valid?)
    ↓
Update isFormValid Flag → Enable/Disable "계속" Button
    ↓
User Taps "계속" Button
    ↓
Set isSubmitting = true (disable button, show loading)
    ↓
Call API: POST /api/v1/users/profile
    ↓
API Response Success
    ↓
Set submitSuccess = true → Navigate to next screen
    ↓
API Response Error
    ↓
Set submitError + Show Toast Message
    ↓
Set isSubmitting = false → Re-enable form for retry
```

### 2.3 Form State Validation Logic

```kotlin
fun ProfileFormState.isFormValid(): Boolean {
    return name.isValidName() &&
           role.isValidRole() &&
           industry != null &&
           growthGoals.isValidGrowthGoals() &&
           !isSubmitting &&
           submitError == null
}

fun ProfileFormState.allFieldsPopulated(): Boolean {
    return name.isNotEmpty() &&
           role.isNotEmpty() &&
           industry != null &&
           growthGoals.isNotEmpty()
}
```

---

## 3. Client-Side Validation Logic

### 3.1 Validation Rules (Kotlin)

```kotlin
object ProfileValidators {

    // Name validation
    fun validateName(name: String): String? {
        val trimmed = name.trim()

        return when {
            trimmed.isEmpty() -> "이름은 필수입니다"
            trimmed.length < 2 -> "한글 이름 최소 2글자 이상 입력해주세요"
            trimmed.length > 4 -> "한글 이름 최대 4글자까지 입력 가능합니다"
            !trimmed.matches(Regex("^[가-힣]{2,4}$")) ->
                "한글 이름만 입력 가능합니다 (특수문자, 숫자 불가)"
            else -> null
        }
    }

    // Professional role validation
    fun validateRole(role: String): String? {
        val trimmed = role.trim()

        return when {
            trimmed.isEmpty() -> "직무는 필수입니다"
            trimmed.length < 2 -> "2자 이상의 직무를 입력해주세요"
            trimmed.length > 48 -> "48자 이하의 직무를 입력해주세요"
            !trimmed.matches(Regex("^[가-힣a-zA-Z\\s]{2,48}$")) ->
                "한글, 영문, 공백만 입력 가능합니다 (숫자, 특수문자 불가)"
            else -> null
        }
    }

    // Industry validation
    fun validateIndustry(industry: IndustryCode?): String? {
        return if (industry == null) {
            "산업을 선택해주세요"
        } else {
            null
        }
    }

    // Growth goals validation
    fun validateGrowthGoals(goals: String): String? {
        val trimmed = goals.trim()
        val charCount = trimmed.length

        return when {
            trimmed.isEmpty() -> "성장 목표는 필수입니다"
            charCount < 100 -> "최소 100자 이상 입력해주세요 (현재: $charCount자)"
            charCount > 500 -> "최대 500자까지 입력 가능합니다 (현재: $charCount자)"
            else -> null
        }
    }

    // Photo validation (Phase 2)
    fun validatePhoto(uri: Uri?, fileSize: Long): String? {
        return when {
            uri == null -> null // Optional field
            fileSize > 5_242_880 -> "5MB 이하의 파일을 선택해주세요"
            else -> null
        }
    }
}
```

### 3.2 Real-Time Validation Triggers

**Real-Time Validation Fields:**
- Name: Triggered on each character input (onChange)
- Professional Role: Triggered on each character input (onChange)
- Growth Goals: Triggered on each character input (onChange) + Character counter updated

**Deferred Validation (on Focus Loss):**
- All fields validate when user loses focus (onFocusEvent) to show errors only when field is inactive

**Validation on Form Submission:**
- All fields validated before API call
- If any field invalid, show inline errors and prevent submission

### 3.3 Character Counter for Growth Goals

```kotlin
fun ProfileFormState.getGrowthGoalsCharacterCount(): Int {
    return growthGoals.trim().length
}

fun ProfileFormState.isGrowthGoalsValid(): Boolean {
    val count = getGrowthGoalsCharacterCount()
    return count in 100..500
}

fun ProfileFormState.getCharacterCountColor(): Color {
    val count = getGrowthGoalsCharacterCount()
    return when {
        count == 0 -> Color.Gray
        count < 100 -> Color.Red
        else -> Color.Gray
    }
}
```

---

## 4. UI Components

### 4.1 TextInputField Composable

**Purpose:** Reusable text input field for name and professional role with validation display
**Props:**
- `value: String` - Current input value
- `onValueChange: (String) -> Unit` - Callback when user types
- `label: String` - Field label (e.g., "이름")
- `placeholder: String` - Placeholder text
- `maxLength: Int` - Maximum character limit
- `isRequired: Boolean` - Show required indicator (*)
- `error: String?` - Error message to display (null = no error)
- `isValid: Boolean` - Is field currently valid
- `isFocused: Boolean` - Is field currently focused
- `onFocusChange: (Boolean) -> Unit` - Callback when focus changes
- `keyboardType: KeyboardType` - Keyboard type (Text, etc.)
- `showCharacterCount: Boolean = false` - Show character counter (optional)

**UI Structure:**
```
┌─────────────────────────────────────┐
│ 이름 *                              │  (Label with required indicator)
├─────────────────────────────────────┤
│ 한글 이름 2-4글자                   │  (Placeholder text)
│ [User input here]                   │  (Input field, 48dp height)
│                                     │
├─────────────────────────────────────┤
│ 한글 이름만 입력 가능합니다         │  (Error message, red text)
│ (특수문자, 숫자 불가)              │
└─────────────────────────────────────┘
```

**Styling:**
- Material 3 TextField with custom Memoir theme
- Label color: Black (normal), Accent color (focused)
- Error text: Red (#E86221 - Memoir carrot orange)
- Border: 1dp gray (normal), 2dp accent (focused)
- Padding: 16dp horizontal, 12dp vertical
- Font: Pretendard, 16sp body size
- Touch target height: 48dp minimum

**Placeholder Styling:**
- Font: Pretendard, 14sp
- Color: Gray (#757575)
- Text: "한글 이름 2-4글자"

**Error Message Styling:**
- Font: Pretendard, 12sp
- Color: Red (#E86221)
- Padding: 4dp top from input field

### 4.2 DropdownSelector Composable

**Purpose:** Reusable dropdown for industry selection
**Props:**
- `label: String` - Field label (e.g., "산업")
- `selectedValue: IndustryCode?` - Currently selected industry code
- `onSelectionChange: (IndustryCode) -> Unit` - Callback when user selects option
- `options: List<Pair<IndustryCode, String>>` - List of (code, display label) pairs
- `isRequired: Boolean` - Show required indicator (*)
- `error: String?` - Error message if validation failed
- `placeholder: String` - Placeholder text when nothing selected

**UI Structure:**
```
┌─────────────────────────────────────┐
│ 산업 *                              │  (Label with required indicator)
├─────────────────────────────────────┤
│ 산업을 선택해주세요           ▼    │  (Closed dropdown, 48dp height)
├─────────────────────────────────────┤

[When expanded:]
├─────────────────────────────────────┤
│ ✓ 스타트업                          │  (Selected option with checkmark)
│ 대기업                              │
│ 중소기업                            │
│ 공기업/공공기관                    │
│ 외국계 기업                         │
│ 프리랜서/1인 기업                  │
│ 비영리/사회적 기업                 │
│ 기타                                │
└─────────────────────────────────────┘
```

**Styling:**
- Material 3 ExposedDropdownMenuBox
- Closed state: Same styling as TextInputField
- Open state: Dropdown menu below field, max height 300dp
- Dropdown item height: 48dp
- Checkmark icon: Accent color (#F4BA54 - Memoir mustard)
- Scroll behavior: Scrollable if >8 items

**Interactions:**
- Tap to open dropdown
- Tap option to select
- Tap outside to close
- Arrow icon rotates (180°) when opened

### 4.3 CharacterCountIndicator Composable

**Purpose:** Real-time character counter for growth goals text area
**Props:**
- `currentCount: Int` - Current character count
- `minLength: Int` - Minimum required characters (100)
- `maxLength: Int` - Maximum allowed characters (500)

**UI Structure:**
```
┌─────────────────────────────────────┐
│ 성장 목표 *                         │
├─────────────────────────────────────┤
│ [Large text input area]             │
│                                     │
│                                     │
├─────────────────────────────────────┤
│ 156/500 글자                        │  (Character counter, gray text)
└─────────────────────────────────────┘
```

**Styling:**
- Font: Pretendard, 12sp
- Color: Gray (#757575) when 100-500 chars, Red (#E86221) when <100 chars
- Alignment: Right-aligned, 12dp padding bottom
- Format: "{currentCount}/500 글자"

**Color Logic:**
```kotlin
val counterColor = when {
    currentCount == 0 -> Color.Gray
    currentCount < 100 -> Color.Red
    else -> Color.Gray
}
```

### 4.4 ProfileFormContent Composable

**Purpose:** Complete profile setup form combining all fields
**Props:**
- `state: ProfileFormState` - Current form state
- `onNameChange: (String) -> Unit`
- `onNameFocusChange: (Boolean) -> Unit`
- `onRoleChange: (String) -> Unit`
- `onRoleFocusChange: (Boolean) -> Unit`
- `onIndustryChange: (IndustryCode) -> Unit`
- `onGrowthGoalsChange: (String) -> Unit`
- `onGrowthGoalsFocusChange: (Boolean) -> Unit`
- `onPhotoSelect: (Uri) -> Unit`
- `onContinue: () -> Unit`

**Form Layout:**
```
Screen (with scrollable content area)
├── Header: "프로필 설정" (Profile Setup)
│
├── Content (Scrollable LazyColumn)
│   ├── TextInputField (Name)
│   ├── TextInputField (Professional Role)
│   ├── DropdownSelector (Industry)
│   ├── TextInputField (Growth Goals, multi-line)
│   ├── CharacterCountIndicator (Growth Goals)
│   └── [Phase 2: Profile Photo upload]
│
└── Footer
    ├── "계속" Button (enabled if form valid)
    └── [Optional: "건너뛰기" for photo - Phase 2]
```

**Scroll Behavior:**
- Content area scrollable independently from buttons
- Continue button sticky at bottom (not scrolled out of view)
- Keyboard pushes content up, doesn't cover button

---

## 5. Error Handling

### 5.1 Field-Level Validation Errors

**Display Location:** Below each input field
**Color:** Red (#E86221)
**Font Size:** 12sp (smaller than main text)
**Timing:** Display error when:
1. Field loses focus (onFocusLost) and has invalid content
2. User attempts form submission with invalid content
3. Server validation returns field-specific error

**Examples:**
```
Name field with error:
┌──────────────────────┐
│ [입력 값]             │
├──────────────────────┤
│ 한글 이름만 입력      │
│ 가능합니다            │
```

### 5.2 Form Submission Errors

**Display Type:** Toast message (temporary, auto-dismiss after 3-5 seconds)
**Message Format:** "[Error code] [User-friendly message]"
**Position:** Bottom of screen, above button

**Examples:**
- Network error: "네트워크 연결이 불안정합니다. 다시 시도해주세요."
- Server validation error: "프로필 저장에 실패했습니다. 입력값을 확인해주세요."
- Duplicate profile: "이미 프로필이 설정되어 있습니다. 지원팀에 문의해주세요."
- Server error: "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."

**Retry Mechanism:**
- Toast shows error message
- "계속" button remains enabled
- User can modify form and retry
- Exponential backoff: 1s → 2s → 4s (max 3 retries before manual intervention)

### 5.3 Continue Button State Management

**Disabled States:**
- Form invalid (any required field missing/invalid)
- Submission in progress (isSubmitting = true)

**Enabled States:**
- All required fields valid and filled
- Not currently submitting

**Visual Feedback:**
```kotlin
Button(
    enabled = state.isFormValid && !state.isSubmitting,
    onClick = { onContinue() },
    modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = if (state.isFormValid && !state.isSubmitting)
            Color(0xFFE86221) // Memoir carrot orange
        else
            Color(0xFFBDBDBD) // Light gray when disabled
    )
) {
    if (state.isSubmitting) {
        CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(20.dp)
        )
    } else {
        Text("계속")
    }
}
```

**Disabled Button Styling:**
- Background color: Light gray (#BDBDBD)
- Text color: Medium gray (#757575)
- Cursor: Not-allowed
- No ripple effect on tap

**Submitting Button Styling:**
- Background color: Memoir carrot orange (#E86221)
- Loading spinner: White, center of button
- Text: Hidden (replaced by spinner)
- Disabled: Cannot tap

---

## 6. Korean Localization

### 6.1 UI Text Strings

**Field Labels:**
```kotlin
object ProfileStrings {
    const val LABEL_NAME = "이름"
    const val LABEL_ROLE = "직무"
    const val LABEL_INDUSTRY = "산업"
    const val LABEL_GROWTH_GOALS = "성장 목표"
    const val LABEL_PROFILE_PHOTO = "프로필 사진"
    const val LABEL_REQUIRED = "*" // Shown next to required field labels

    const val PLACEHOLDER_NAME = "한글 이름 2-4글자"
    const val PLACEHOLDER_ROLE = "예: Product Manager"
    const val PLACEHOLDER_INDUSTRY = "산업을 선택해주세요"
    const val PLACEHOLDER_GROWTH_GOALS =
        "일상 속 배움을 구조적으로 기록하고, 다양한 분야의 사람들과 공유하면서 시야를 넓히고 싶습니다..."

    const val BUTTON_CONTINUE = "계속"
    const val BUTTON_SKIP = "건너뛰기" // Phase 2
    const val BUTTON_RETRY = "다시 시도"
}
```

**Industry Display Labels:**
```kotlin
object IndustryLabels {
    val STARTUP = "스타트업"
    val ENTERPRISE = "대기업"
    val SME = "중소기업"
    val PUBLIC = "공기업/공공기관"
    val FOREIGN = "외국계 기업"
    val FREELANCER = "프리랜서/1인 기업"
    val NONPROFIT = "비영리/사회적 기업"
    val OTHER = "기타"
}
```

### 6.2 Validation Error Messages (Complete List)

**Name Field Errors:**
```kotlin
const val NAME_ERROR_EMPTY = "이름은 필수입니다"
const val NAME_ERROR_TOO_SHORT = "한글 이름 최소 2글자 이상 입력해주세요"
const val NAME_ERROR_TOO_LONG = "한글 이름 최대 4글자까지 입력 가능합니다"
const val NAME_ERROR_INVALID_CHARS = "한글 이름만 입력 가능합니다 (특수문자, 숫자 불가)"
```

**Role Field Errors:**
```kotlin
const val ROLE_ERROR_EMPTY = "직무는 필수입니다"
const val ROLE_ERROR_TOO_SHORT = "2자 이상의 직무를 입력해주세요"
const val ROLE_ERROR_TOO_LONG = "48자 이하의 직무를 입력해주세요"
const val ROLE_ERROR_INVALID_CHARS = "한글, 영문, 공백만 입력 가능합니다 (숫자, 특수문자 불가)"
```

**Industry Field Errors:**
```kotlin
const val INDUSTRY_ERROR_EMPTY = "산업을 선택해주세요"
const val INDUSTRY_ERROR_INVALID = "유효한 산업을 선택해주세요"
```

**Growth Goals Field Errors:**
```kotlin
const val GROWTH_GOALS_ERROR_EMPTY = "성장 목표는 필수입니다"
const val GROWTH_GOALS_ERROR_TOO_SHORT = "최소 100자 이상 입력해주세요"
const val GROWTH_GOALS_ERROR_TOO_LONG = "최대 500자까지 입력 가능합니다"
const val GROWTH_GOALS_COUNTER_FORMAT = "%d/500 글자"
```

**Submission Errors:**
```kotlin
const val SUBMIT_ERROR_NETWORK = "네트워크 연결이 불안정합니다. 다시 시도해주세요."
const val SUBMIT_ERROR_VALIDATION = "입력값을 확인해주세요. 모든 필드를 올바르게 입력해주세요."
const val SUBMIT_ERROR_DUPLICATE = "이미 프로필이 설정되어 있습니다. 지원팀에 문의해주세요."
const val SUBMIT_ERROR_SERVER = "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
const val SUBMIT_ERROR_UNKNOWN = "프로필 저장에 실패했습니다. 다시 시도해주세요."
```

### 6.3 Speech Level and Tone

**Polite Speech (존댓말) Requirements:**
- All system messages use formal polite Korean (존댓말)
- Avoid casual/intimate speech (반말, 하게체)
- Examples:
  - ✓ "이름을 입력해주세요" (please enter your name)
  - ✗ "이름 입력해" (enter your name - too casual)

**Error Message Tone:**
- Friendly and encouraging, not accusatory
- Examples:
  - ✓ "한글 이름 2-4글자를 입력해주세요" (warm, specific instruction)
  - ✗ "한글만 입력해라" (rude, accusatory)

### 6.4 Typography for Korean

**Font Family:** Pretendard (optimized for Korean readability)
**Font Sizes:**
- Header: 20sp bold
- Label: 14sp regular
- Input: 16sp regular
- Error: 12sp regular
- Counter: 12sp regular

**Line Height:** 1.5 (comfortable spacing for Korean characters)

---

## 7. Backend Validation

### 7.1 Server-Side Validation Endpoint

**Endpoint:** `POST /api/v1/users/profile`
**Authentication:** Bearer token (JWT access token)
**Rate Limiting:** 10 requests per user per minute

**Request Body:**
```json
{
  "name": "김태희",
  "role": "Product Manager",
  "industry": "STARTUP",
  "growthGoals": "일상 속 배움을 구조적으로 기록하고, 다양한 분야의 사람들과 공유하면서 시야를 넓히고 싶습니다. 특히 PM으로서 고객 이해도를 깊게 하고, 리더십 역량을 개발하는 것이 목표입니다."
}
```

**Response (201 Created):**
```json
{
  "user_id": "550e8400-e29b-41d4-a716-446655440000",
  "profile": {
    "name": "김태희",
    "role": "Product Manager",
    "industry": "STARTUP",
    "growthGoals": "일상 속 배움을 구조적으로...",
    "created_at": "2025-01-15T10:30:00Z"
  },
  "onboarding_completed_at": "2025-01-15T10:30:00Z"
}
```

### 7.2 Server-Side Validation Rules (Prisma + TypeScript)

```typescript
// Prisma schema excerpt
model UserProfile {
  id              String   @id @default(uuid())
  userId          String   @unique
  name            String   @db.VarChar(4)     // Hangul max 4 chars = max 12 bytes in UTF-8
  role            String   @db.VarChar(48)
  industry        IndustryCode
  growthGoals     String   @db.Text           // 100-500 chars
  photoUrl        String?
  locale          String   @default("ko-KR")
  profileLocked   Boolean  @default(false)
  onboardingCompletedAt DateTime?
  createdAt       DateTime @default(now())
  updatedAt       DateTime @updatedAt

  user User @relation(fields: [userId], references: [id])
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

// Validation implementation
export async function validateProfileData(data: any): Promise<ValidationResult> {
  const errors: Record<string, string> = {};

  // Name validation
  if (!data.name) {
    errors.name = "이름은 필수입니다";
  } else if (!isValidHangul(data.name)) {
    errors.name = "한글 이름만 입력 가능합니다";
  } else if (data.name.length < 2 || data.name.length > 4) {
    errors.name = "한글 이름 2-4글자를 입력해주세요";
  }

  // Role validation
  if (!data.role) {
    errors.role = "직무는 필수입니다";
  } else if (!isValidRole(data.role)) {
    errors.role = "한글, 영문, 공백만 입력 가능합니다";
  } else if (data.role.length < 2 || data.role.length > 48) {
    errors.role = "2-48자의 직무를 입력해주세요";
  }

  // Industry validation
  if (!data.industry) {
    errors.industry = "산업을 선택해주세요";
  } else if (!Object.values(IndustryCode).includes(data.industry)) {
    errors.industry = "유효한 산업을 선택해주세요";
  }

  // Growth goals validation
  if (!data.growthGoals) {
    errors.growthGoals = "성장 목표는 필수입니다";
  } else if (data.growthGoals.trim().length < 100) {
    errors.growthGoals = "최소 100자 이상 입력해주세요";
  } else if (data.growthGoals.length > 500) {
    errors.growthGoals = "최대 500자까지 입력 가능합니다";
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
}

// Helper functions
function isValidHangul(name: string): boolean {
  // Unicode range for Korean hangul: AC00-D7A3
  const hangulRegex = /^[\uAC00-\uD7A3]{2,4}$/;
  return hangulRegex.test(name.trim());
}

function isValidRole(role: string): boolean {
  // Allow Korean, English, spaces
  const roleRegex = /^[\uAC00-\uD7A3a-zA-Z\s]{2,48}$/;
  return roleRegex.test(role.trim());
}
```

### 7.3 Error Response Codes

| HTTP Code | Error Code | Message | Description |
|-----------|-----------|---------|-------------|
| 400 | VALIDATION_ERROR | Field-specific errors | Invalid input format/length |
| 400 | MISSING_REQUIRED_FIELD | Field name required | Missing required field in request |
| 401 | UNAUTHORIZED | Invalid or expired token | Bearer token missing or invalid |
| 409 | PROFILE_ALREADY_SET | User already has profile | Prevent duplicate profile creation |
| 409 | TOKEN_EXPIRED | Token expired | JWT token has expired |
| 500 | INTERNAL_SERVER_ERROR | Server error occurred | Unexpected server error |

**Error Response Format:**
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "프로필 저장에 실패했습니다",
    "details": {
      "name": "한글 이름 2-4글자를 입력해주세요",
      "growthGoals": "최소 100자 이상 입력해주세요"
    }
  }
}
```

### 7.4 XSS Prevention

**Input Sanitization (Server):**
- Trim whitespace before validation
- HTML-escape all text fields before storage (e.g., name, role, growth goals)
- No HTML/script tags allowed in any field

```typescript
import DOMPurify from 'isomorphic-dompurify';

function sanitizeInput(input: string): string {
  return DOMPurify.sanitize(input, { ALLOWED_TAGS: [] });
}

export async function createUserProfile(userId: string, data: any) {
  const sanitizedData = {
    name: sanitizeInput(data.name),
    role: sanitizeInput(data.role),
    growthGoals: sanitizeInput(data.growthGoals),
    industry: data.industry // Enum, no sanitization needed
  };

  // Validate and save...
}
```

### 7.5 SQL Injection Prevention

**Method:** Parameterized Queries (Prisma ORM)
All database operations use Prisma, which automatically parameterizes queries:

```typescript
// Prisma prevents SQL injection automatically
const profile = await prisma.userProfile.create({
  data: {
    userId,
    name: sanitizedData.name,
    role: sanitizedData.role,
    industry: sanitizedData.industry,
    growthGoals: sanitizedData.growthGoals,
    onboardingCompletedAt: new Date()
  }
});
```

### 7.6 Duplicate Profile Prevention

**Logic:** Check if user already has profile before creation

```typescript
export async function saveUserProfile(userId: string, data: any) {
  // Check if profile already exists
  const existingProfile = await prisma.userProfile.findUnique({
    where: { userId }
  });

  if (existingProfile) {
    return {
      status: 409,
      error: {
        code: "PROFILE_ALREADY_SET",
        message: "이미 프로필이 설정되어 있습니다"
      }
    };
  }

  // Validate input
  const validation = await validateProfileData(data);
  if (!validation.isValid) {
    return {
      status: 400,
      error: {
        code: "VALIDATION_ERROR",
        message: "입력값을 확인해주세요",
        details: validation.errors
      }
    };
  }

  // Create profile
  const profile = await prisma.userProfile.create({
    data: {
      userId,
      name: sanitizeInput(data.name),
      role: sanitizeInput(data.role),
      industry: data.industry,
      growthGoals: sanitizeInput(data.growthGoals),
      onboardingCompletedAt: new Date(),
      profileLocked: true
    }
  });

  return {
    status: 201,
    data: { profile }
  };
}
```

---

## 8. Data Persistence

### 8.1 Local Storage (Android Room)

**Purpose:** Cache profile data locally for offline access and faster app startup

**Room Entity:**
```kotlin
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val role: String,
    val industry: String, // Enum code as string
    val growthGoals: String,
    val photoUrl: String?,
    val onboardingCompletedAt: Long?,
    val locale: String = "ko-KR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE user_id = :userId LIMIT 1")
    suspend fun getProfile(userId: String): UserProfileEntity?

    @Query("DELETE FROM user_profiles WHERE user_id = :userId")
    suspend fun deleteProfile(userId: String)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)
}
```

### 8.2 Remote Storage (REST API)

**Endpoint:** `POST /api/v1/users/profile`

**Request Flow:**
```
Client                          Server
  |                               |
  | POST /api/v1/users/profile   |
  |----(name, role, industry)--->|
  |   (growthGoals, photo)       |
  |                               |
  |                   Validate    |
  |                   Sanitize    |
  |                   Store       |
  |                               |
  |           201 Created         |
  |<---(profile object)-----------|
  |                               |
  Save to Room DB                 |
```

### 8.3 DataStore (Encrypted Preferences)

**Purpose:** Store session tokens securely
**Implementation:** Android DataStore (encrypted)

```kotlin
// Preferences keys
object ProfilePreferences {
    val IS_PROFILE_COMPLETED = booleanPreferencesKey("is_profile_completed")
    val PROFILE_COMPLETED_AT = stringPreferencesKey("profile_completed_at")
    val LAST_PROFILE_UPDATE = stringPreferencesKey("last_profile_update")
}

// Save profile completion status
suspend fun saveProfileCompletionStatus(dataStore: DataStore<Preferences>) {
    dataStore.edit { preferences ->
        preferences[ProfilePreferences.IS_PROFILE_COMPLETED] = true
        preferences[ProfilePreferences.PROFILE_COMPLETED_AT] =
            System.currentTimeMillis().toString()
    }
}

// Read profile completion status
val profileCompletionFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
    preferences[ProfilePreferences.IS_PROFILE_COMPLETED] ?: false
}
```

### 8.4 Sync Strategy (Online/Offline)

**Online Scenario:**
1. User fills form
2. Clicks "계속"
3. API call made
4. Success → Save to local Room DB
5. Mark in DataStore as complete
6. Navigate to next screen

**Offline Scenario (Phase 2):**
1. User fills form
2. Clicks "계속"
3. API call fails (no network)
4. Save to local Room DB (pending flag)
5. Show retry message
6. When online → Auto-sync to API
7. Mark complete in DataStore

---

## 9. Security Considerations

### 9.1 Input Validation (Client + Server)

**Defense in Depth:** Validate on both client and server
**Client:** Real-time UX feedback, prevent obvious errors
**Server:** Enforce business rules, prevent malicious input

**Examples:**
```kotlin
// Client-side: Prevent user from submitting invalid data
val isFormValid = name.isValidName() &&
                  role.isValidRole() &&
                  industry != null &&
                  growthGoals.isValidGrowthGoals()

// Server-side: Enforce same rules even if client bypassed
const validation = await validateProfileData(data);
if (!validation.isValid) {
    return error(400, "VALIDATION_ERROR", validation.errors);
}
```

### 9.2 OWASP Top 10 Mitigations

| Vulnerability | Mitigation |
|---|---|
| **A03:2021 – Injection** | Parameterized queries (Prisma), input sanitization with DOMPurify |
| **A07:2021 – XSS** | HTML-escape all text fields, Content Security Policy headers |
| **A01:2021 – Broken Access Control** | Verify JWT token, check user_id matches requester, profile locked after creation |
| **A02:2021 – Cryptographic Failures** | HTTPS-only, encrypted DataStore for tokens, bcrypt for passwords (if any) |
| **A04:2021 – Insecure Design** | Input length limits enforced at DB level, rate limiting on endpoints |

### 9.3 Data Minimization

**Principle:** Collect only necessary data
**Fields Collected:**
- Name (required for identification)
- Role (required for cohort matching)
- Industry (required for cohort segmentation)
- Growth Goals (required for profile matching)
- Photo (optional, deferred to Phase 2)

**Fields NOT Collected:**
- Email (not needed for Kakao OAuth)
- Phone number (not needed)
- Address (not needed)
- Payment info (handled separately)

### 9.4 Secure Storage

**Tokens:** Encrypted in Android Keystore via DataStore
**Profile:** Local Room DB, no sensitive data
**No Logging:** Exclude tokens and PII from logs

```kotlin
// Logging practice
Log.d("ProfileSubmit", "Submitting profile for user_id: $userId")  // OK
Log.d("ProfileSubmit", "Token: $token, Name: $name")               // BAD - logs sensitive data
```

---

## 10. Testing Strategy

### 10.1 Unit Tests (Validation Logic)

**Test File:** `ProfileValidatorsTest.kt`

```kotlin
class ProfileValidatorsTest {

    @Test
    fun validateName_ValidHangul_ReturnsNull() {
        val result = ProfileValidators.validateName("김태희")
        assertNull(result)
    }

    @Test
    fun validateName_TooShort_ReturnsError() {
        val result = ProfileValidators.validateName("김")
        assertEquals("한글 이름 최소 2글자 이상 입력해주세요", result)
    }

    @Test
    fun validateName_TooLong_ReturnsError() {
        val result = ProfileValidators.validateName("김태희임")
        assertEquals("한글 이름 최대 4글자까지 입력 가능합니다", result)
    }

    @Test
    fun validateName_WithEnglish_ReturnsError() {
        val result = ProfileValidators.validateName("Kim태희")
        assertEquals("한글 이름만 입력 가능합니다 (특수문자, 숫자 불가)", result)
    }

    @Test
    fun validateName_WithNumbers_ReturnsError() {
        val result = ProfileValidators.validateName("김1태")
        assertEquals("한글 이름만 입력 가능합니다 (특수문자, 숫자 불가)", result)
    }

    @Test
    fun validateRole_ValidRole_ReturnsNull() {
        val result = ProfileValidators.validateRole("Product Manager")
        assertNull(result)
    }

    @Test
    fun validateRole_ValidKoreanRole_ReturnsNull() {
        val result = ProfileValidators.validateRole("마케팅 전략가")
        assertNull(result)
    }

    @Test
    fun validateRole_MixedLanguage_ReturnsNull() {
        val result = ProfileValidators.validateRole("UX Designer")
        assertNull(result)
    }

    @Test
    fun validateRole_WithNumbers_ReturnsError() {
        val result = ProfileValidators.validateRole("Product Manager 2")
        assertEquals("한글, 영문, 공백만 입력 가능합니다 (숫자, 특수문자 불가)", result)
    }

    @Test
    fun validateGrowthGoals_ValidLength_ReturnsNull() {
        val validGoals = "a".repeat(100) // Exactly 100 chars
        val result = ProfileValidators.validateGrowthGoals(validGoals)
        assertNull(result)
    }

    @Test
    fun validateGrowthGoals_TooShort_ReturnsError() {
        val result = ProfileValidators.validateGrowthGoals("단 50자입니다".repeat(5)) // 55 chars
        assertEquals("최소 100자 이상 입력해주세요", result)
    }

    @Test
    fun validateGrowthGoals_TooLong_ReturnsError() {
        val result = ProfileValidators.validateGrowthGoals("a".repeat(501))
        assertEquals("최대 500자까지 입력 가능합니다", result)
    }

    @Test
    fun validateIndustry_ValidIndustry_ReturnsNull() {
        val result = ProfileValidators.validateIndustry(IndustryCode.STARTUP)
        assertNull(result)
    }

    @Test
    fun validateIndustry_NullIndustry_ReturnsError() {
        val result = ProfileValidators.validateIndustry(null)
        assertEquals("산업을 선택해주세요", result)
    }
}
```

### 10.2 Integration Tests (API & Database)

**Test File:** `ProfileSubmissionIntegrationTest.kt`

```kotlin
class ProfileSubmissionIntegrationTest {

    @Test
    fun submitProfile_ValidData_ReturnsSuccess() = runTest {
        // Arrange
        val mockOkHttpClient = mockOkHttpClient()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost:8080/api/v1/")
            .client(mockOkHttpClient)
            .addConverterFactory(KotlinxSerializationConverterFactory())
            .build()
        val apiService = retrofit.create(UserProfileService::class.java)

        val profileRequest = ProfileRequest(
            name = "김태희",
            role = "Product Manager",
            industry = "STARTUP",
            growthGoals = "성장하고 싶습니다".repeat(10) // >100 chars
        )

        // Act
        val response = apiService.submitProfile(
            token = "Bearer mock_token",
            profile = profileRequest
        )

        // Assert
        assertTrue(response.isSuccessful)
        assertEquals(201, response.code())
        assertNotNull(response.body()?.profile)
    }

    @Test
    fun submitProfile_DuplicateProfile_Returns409Error() = runTest {
        // Arrange
        val mockResponse = mockResponse(
            code = 409,
            body = """
                {
                  "error": {
                    "code": "PROFILE_ALREADY_SET",
                    "message": "이미 프로필이 설정되어 있습니다"
                  }
                }
            """.trimIndent()
        )

        // Act & Assert
        assertTrue(mockResponse.code == 409)
        assertEquals("PROFILE_ALREADY_SET", mockResponse.body?.error?.code)
    }

    @Test
    fun submitProfile_ValidationError_Returns400WithDetails() = runTest {
        // Arrange
        val invalidRequest = ProfileRequest(
            name = "K", // Too short
            role = "PM",
            industry = "STARTUP",
            growthGoals = "short" // Too short
        )

        // Act & Assert
        val errors = validateProfileData(invalidRequest)
        assertTrue(errors.isNotEmpty())
        assertNotNull(errors["name"])
        assertNotNull(errors["growthGoals"])
    }

    @Test
    fun submitProfile_NetworkTimeout_Retries() = runTest {
        // Arrange - Simulate network timeout
        val mockClient = mockClientWithDelay(5000) // 5 second timeout

        // Act - Should retry with exponential backoff
        val result = submitWithRetry(mockClient, maxRetries = 3)

        // Assert
        assertEquals(RetryResult.FAILED_AFTER_RETRIES, result)
    }
}
```

### 10.3 UI/Composable Tests

**Test File:** `ProfileFormScreenTest.kt`

```kotlin
class ProfileFormScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun profileForm_ContinueButtonDisabledWhenFormEmpty() {
        composeTestRule.setContent {
            ProfileFormScreen(
                state = ProfileFormState()
            )
        }

        composeTestRule
            .onNodeWithText("계속")
            .assertIsNotEnabled()
    }

    @Test
    fun profileForm_NameFieldShowsErrorMessage() {
        composeTestRule.setContent {
            ProfileFormScreen(
                state = ProfileFormState(
                    name = "Kim",
                    nameError = "한글 이름만 입력 가능합니다"
                )
            )
        }

        composeTestRule
            .onNodeWithText("한글 이름만 입력 가능합니다")
            .assertIsDisplayed()
    }

    @Test
    fun profileForm_GrowthGoalsCounterUpdates() {
        composeTestRule.setContent {
            var goals by remember { mutableStateOf("") }
            ProfileFormScreen(
                state = ProfileFormState(
                    growthGoals = goals
                ),
                onGrowthGoalsChange = { goals = it }
            )
        }

        composeTestRule
            .onNodeWithTag("growth_goals_input")
            .performTextInput("a".repeat(150))

        composeTestRule
            .onNodeWithText("150/500")
            .assertIsDisplayed()
    }

    @Test
    fun profileForm_ContinueButtonEnabledWhenFormValid() {
        composeTestRule.setContent {
            ProfileFormScreen(
                state = ProfileFormState(
                    name = "김태희",
                    role = "Product Manager",
                    industry = IndustryCode.STARTUP,
                    growthGoals = "a".repeat(150),
                    isFormValid = true
                )
            )
        }

        composeTestRule
            .onNodeWithText("계속")
            .assertIsEnabled()
    }

    @Test
    fun profileForm_SubmitShowsLoadingState() {
        composeTestRule.setContent {
            ProfileFormScreen(
                state = ProfileFormState(
                    isSubmitting = true
                )
            )
        }

        composeTestRule
            .onNodeWithTag("loading_spinner")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("계속")
            .assertIsNotEnabled()
    }

    @Test
    fun profileForm_IndustryDropdownSelectsOption() {
        composeTestRule.setContent {
            var selected by remember { mutableStateOf<IndustryCode?>(null) }
            ProfileFormScreen(
                state = ProfileFormState(industry = selected),
                onIndustryChange = { selected = it }
            )
        }

        // Open dropdown
        composeTestRule.onNodeWithText("산업을 선택해주세요").performClick()

        // Select option
        composeTestRule.onNodeWithText("스타트업").performClick()

        // Verify selection
        composeTestRule.onNodeWithText("스타트업").assertIsDisplayed()
    }
}
```

### 10.4 Test Scenarios Checklist

**Functional Tests:**
- [x] All fields accept valid input
- [x] Name validates hangul only
- [x] Role validates Korean + English + space
- [x] Industry dropdown shows 8 options
- [x] Growth goals counter updates in real-time
- [x] Continue button disabled with invalid form
- [x] Continue button enabled with valid form
- [x] Form submission sends correct API payload
- [x] API success → navigate to next screen
- [x] API error → show error toast
- [x] Network timeout → retry with backoff
- [x] Duplicate profile → show 409 error
- [x] Validation error → show field-specific error

**Edge Case Tests:**
- [x] Name with exactly 2 chars (boundary)
- [x] Name with exactly 4 chars (boundary)
- [x] Role with 2 chars (minimum)
- [x] Role with 48 chars (maximum)
- [x] Growth goals with exactly 100 chars (minimum)
- [x] Growth goals with exactly 500 chars (maximum)
- [x] Name with leading/trailing spaces (trim)
- [x] Role with multiple consecutive spaces (allow)
- [x] Growth goals with newlines (allow)

**Security Tests:**
- [x] XSS attempt in name field (e.g., "<script>alert('xss')</script>")
- [x] SQL injection attempt in role field (e.g., "'; DROP TABLE users; --")
- [x] Invalid JWT token → 401 Unauthorized
- [x] Expired token → force re-authentication
- [x] Reuse old refresh token → 401 invalid

**Localization Tests:**
- [x] All UI text in Korean
- [x] Error messages use polite speech (존댓말)
- [x] Industry dropdown shows Korean labels
- [x] Character counter displays in Korean (글자)

---

## 11. State Diagram

### 11.1 Form State Transitions

```
┌─────────────────────────────────────────────────────────────┐
│                    PROFILE FORM SCREEN                       │
└─────────────────────────────────────────────────────────────┘

                        ┌──────────────────┐
                        │  INITIAL STATE   │
                        │ (Empty form)     │
                        └────────┬─────────┘
                                 │
                    User enters name/role/goals
                                 │
                                 ▼
                        ┌──────────────────┐
                        │  EDITING STATE   │
                        │ (Validating)     │
                        └────────┬─────────┘
                                 │
                ┌────────────────┼────────────────┐
                │                │                │
         Form Invalid      Form Valid      Industry Selected
                │                │                │
                ▼                ▼                ▼
        ┌────────────────┐ ┌────────────────┐
        │  ERROR STATE   │ │  READY STATE   │
        │ (Show errors)  │ │ (Continue on)  │
        │ (Disable btn)  │ │ (Button ready) │
        └────┬───────────┘ └────┬───────────┘
             │                   │
          Clear error        User clicks Continue
             │                   │
             └───────────────────┼────────────────┐
                                 │                │
                        Set isSubmitting = true  │
                                 │                │
                                 ▼                │
                        ┌──────────────────┐    │
                        │ SUBMITTING STATE │    │
                        │ (Loading)        │    │
                        │ (API call)       │    │
                        └────┬─────────────┘    │
                             │                   │
                ┌────────────┴────────────┐     │
                │                        │     │
           API Success            API Error    │
                │                        │     │
                ▼                        ▼     │
      ┌──────────────────┐    ┌──────────────────┐
      │ SUCCESS STATE    │    │ ERROR STATE      │
      │ (Save to DB)     │    │ (Show toast)     │
      │ (Navigate next)  │    │ (Enable retry)   │
      └──────────────────┘    └────┬─────────────┘
                                    │
                            User corrects & retries
                                    │
                                    └──────→ EDITING STATE
```

### 11.2 Field Validation State Machine

```
FIELD: [Name]
┌─────────────────────────────────────────────────┐
│
├─ INITIAL
│  └─ User focuses field
│     └─ FOCUSED
│        ├─ User types (onChange triggered)
│        │  ├─ Real-time validation
│        │  ├─ Update error state
│        │  └─ show/hide error message
│        │
│        └─ User leaves field (onFocusLost)
│           ├─ BLURRED
│           ├─ Run validation again
│           ├─ Persist error if invalid
│           └─ Show error message below field
│
│ ─ Valid state ✓
│  └─ Remove error message
│     └─ Form re-evaluates validity
│        └─ Enable Continue button (if all valid)
│
│ ─ Invalid state ✗
│  └─ Show field error message (red)
│     └─ Continue button remains disabled
│        └─ User must correct input
│
└─────────────────────────────────────────────────┘
```

---

## 12. Summary

### 12.1 Key Implementation Points

| Component | Technology | Notes |
|-----------|-----------|-------|
| Form State | Kotlin data class + StateFlow | MVVM pattern |
| Validation | Regex patterns + custom validators | Client + server |
| UI Components | Jetpack Compose | Material 3 theme |
| Local Storage | Room Database | Encrypted DataStore for tokens |
| Remote Storage | REST API (POST /api/v1/users/profile) | Backend in Express.js |
| Error Handling | Inline field errors + Toast messages | User-friendly Korean text |
| Localization | Polite Korean (존댓말) + Industry enum labels | No English fallback |
| Security | Input sanitization, parameterized queries | XSS/SQL injection prevention |
| Testing | Unit, Integration, Composable tests | Coverage >40% for core logic |

### 12.2 Files to Create

**Frontend (Android):**
- `/app/src/main/kotlin/com/memoir/onboarding/profile/ProfileFormViewModel.kt` (State management)
- `/app/src/main/kotlin/com/memoir/onboarding/profile/ProfileFormScreen.kt` (UI Screen)
- `/app/src/main/kotlin/com/memoir/onboarding/profile/components/TextInputField.kt` (Reusable component)
- `/app/src/main/kotlin/com/memoir/onboarding/profile/components/DropdownSelector.kt` (Reusable component)
- `/app/src/main/kotlin/com/memoir/onboarding/profile/validators/ProfileValidators.kt` (Validation logic)
- `/app/src/main/kotlin/com/memoir/data/local/ProfileDao.kt` (Room DAO)
- `/app/src/main/kotlin/com/memoir/data/remote/UserProfileService.kt` (API client)

**Backend (Express.js):**
- `/src/routes/users/profile.ts` (Endpoint handler)
- `/src/validators/profileValidator.ts` (Server-side validation)
- `/src/db/migrations/[timestamp]_create_user_profiles.ts` (Database schema)

**Tests:**
- `/app/src/test/kotlin/com/memoir/onboarding/profile/ProfileValidatorsTest.kt`
- `/app/src/test/kotlin/com/memoir/onboarding/profile/ProfileFormScreenTest.kt`
- `/server/tests/profile.integration.test.ts`

### 12.3 Success Criteria

- Profile submission success rate > 95%
- All validation rules enforced on client and server
- No XSS or SQL injection vulnerabilities
- All UI text in Korean with polite speech
- Form validation real-time feedback on changes
- Error messages clear and actionable
- Unit test coverage > 40% for validators
- Load time < 2 seconds for form submission API

---

## 13. References

**Standards Compliance:**
- @agent-os/standards/global/validation.md - Validation best practices
- @agent-os/standards/global/error-handling.md - Error handling patterns
- @agent-os/standards/global/coding-style.md - Naming conventions
- @agent-os/standards/frontend/components.md - Component design principles
- @agent-os/standards/backend/api.md - API design standards
- @agent-os/standards/backend/models.md - Database model practices

**Related Specifications:**
- User Authentication & Onboarding Requirements: `/agent-os/specs/2025-11-19-user-authentication-onboarding/planning/requirements.md`

---

## Changelog

| Date | Section | Change | Reason |
|------|---------|--------|--------|
| 2025-01-15 | All | Initial draft | First version |

