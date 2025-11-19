"""
Pydantic schemas for request/response validation
"""
from pydantic import BaseModel, Field
from datetime import datetime
from typing import Optional
from models import UserStatus, IndustryCode


# Auth schemas
class KakaoAuthRequest(BaseModel):
    kakao_oauth_code: str = Field(..., description="Kakao OAuth authorization code")


class AuthResponse(BaseModel):
    access_token: str
    refresh_token: str
    user_exists: bool
    onboarding_completed: bool


class RefreshTokenRequest(BaseModel):
    refresh_token: str


# User schemas
class ProfileRequest(BaseModel):
    name: str = Field(..., min_length=2, max_length=10, description="한글 이름 2-4자")
    role: str = Field(..., min_length=2, max_length=64, description="직무/역할")
    industry_code: IndustryCode
    growth_goals: str = Field(..., min_length=100, max_length=500, description="성장 목표")


class ProfileData(BaseModel):
    name: str
    role: str
    industry_code: IndustryCode
    growth_goals: str
    photo_url: Optional[str] = None


class ProfileResponse(BaseModel):
    user_id: str
    profile: ProfileData
    onboarding_completed_at: str


class UserData(BaseModel):
    id: str
    kakao_id: str
    created_at: str
    last_login_at: str
    status: UserStatus


class UserFlags(BaseModel):
    onboarding_completed: bool


class UserMeResponse(BaseModel):
    user: UserData
    profile: Optional[ProfileData]
    flags: UserFlags


# Error schemas
class ErrorDetail(BaseModel):
    code: str
    message: str
    details: Optional[dict] = None


class ErrorResponse(BaseModel):
    error: ErrorDetail
