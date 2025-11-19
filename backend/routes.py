"""
API 라우터
"""
from fastapi import APIRouter, Depends, HTTPException, Header
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
import requests
from datetime import datetime

from database import get_db
from models import User, RefreshToken, UserStatus
from schemas import (
    KakaoAuthRequest,
    AuthResponse,
    RefreshTokenRequest,
    ProfileRequest,
    ProfileResponse,
    ProfileData,
    UserMeResponse,
    UserData,
    UserFlags,
    ErrorResponse,
    ErrorDetail
)
from auth import (
    create_access_token,
    create_refresh_token,
    verify_token,
    hash_token
)
from config import settings

router = APIRouter(prefix="/api/v1", tags=["api"])


@router.post("/auth/kakao", response_model=AuthResponse)
async def exchange_kakao_token(
    request: KakaoAuthRequest,
    db: AsyncSession = Depends(get_db)
):
    """
    카카오 OAuth 코드를 백엔드 토큰으로 교환
    """
    try:
        # 카카오 API로 사용자 정보 조회 (실제 구현에서는 OAuth 코드 검증)
        # 여기서는 간단히 카카오 ID를 받아서 처리
        kakao_id = request.kakao_oauth_code  # MVP: OAuth 코드를 ID로 사용

        # 사용자 조회 또는 생성
        result = await db.execute(
            select(User).where(User.kakao_id == kakao_id)
        )
        user = result.scalar_one_or_none()

        user_exists = user is not None

        if not user:
            # 신규 사용자 생성
            user = User(
                kakao_id=kakao_id,
                status=UserStatus.ACTIVE,
                created_at=datetime.utcnow(),
                last_login_at=datetime.utcnow()
            )
            db.add(user)
            await db.flush()
        else:
            # 기존 사용자 로그인 시간 업데이트
            user.last_login_at = datetime.utcnow()

        # JWT 토큰 생성
        token_data = {"user_id": str(user.id), "kakao_id": kakao_id}
        access_token = create_access_token(token_data)
        refresh_token_str = create_refresh_token(token_data)

        # 리프레시 토큰 DB 저장
        refresh_token = RefreshToken(
            user_id=user.id,
            token_hash=hash_token(refresh_token_str),
            expires_at=datetime.utcnow() + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS),
            revoked=False
        )
        db.add(refresh_token)
        await db.commit()

        return AuthResponse(
            access_token=access_token,
            refresh_token=refresh_token_str,
            user_exists=user_exists,
            onboarding_completed=user.onboarding_completed_at is not None
        )

    except Exception as e:
        await db.rollback()
        raise HTTPException(status_code=502, detail=f"Kakao authentication failed: {str(e)}")


@router.post("/auth/refresh", response_model=AuthResponse)
async def refresh_access_token(
    request: RefreshTokenRequest,
    db: AsyncSession = Depends(get_db)
):
    """
    리프레시 토큰으로 액세스 토큰 갱신
    """
    # 리프레시 토큰 검증
    payload = verify_token(request.refresh_token, token_type="refresh")
    if not payload:
        raise HTTPException(status_code=401, detail="Invalid refresh token")

    user_id = payload.get("user_id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload")

    # 사용자 조회
    result = await db.execute(
        select(User).where(User.id == user_id)
    )
    user = result.scalar_one_or_none()
    if not user:
        raise HTTPException(status_code=401, detail="User not found")

    # 새 토큰 생성
    token_data = {"user_id": str(user.id), "kakao_id": user.kakao_id}
    access_token = create_access_token(token_data)
    new_refresh_token_str = create_refresh_token(token_data)

    # 기존 리프레시 토큰 폐기하고 새 토큰 저장
    await db.execute(
        select(RefreshToken).where(
            RefreshToken.user_id == user_id,
            RefreshToken.revoked == False
        ).update({"revoked": True})
    )

    refresh_token = RefreshToken(
        user_id=user.id,
        token_hash=hash_token(new_refresh_token_str),
        expires_at=datetime.utcnow() + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS),
        revoked=False
    )
    db.add(refresh_token)
    await db.commit()

    return AuthResponse(
        access_token=access_token,
        refresh_token=new_refresh_token_str,
        user_exists=True,
        onboarding_completed=user.onboarding_completed_at is not None
    )


async def get_current_user(
    authorization: str = Header(...),
    db: AsyncSession = Depends(get_db)
) -> User:
    """
    JWT 토큰으로 현재 사용자 조회
    """
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Invalid authorization header")

    token = authorization.replace("Bearer ", "")
    payload = verify_token(token, token_type="access")

    if not payload:
        raise HTTPException(status_code=401, detail="Invalid or expired token")

    user_id = payload.get("user_id")
    result = await db.execute(select(User).where(User.id == user_id))
    user = result.scalar_one_or_none()

    if not user:
        raise HTTPException(status_code=401, detail="User not found")

    return user


@router.get("/users/me", response_model=UserMeResponse)
async def get_current_user_info(
    current_user: User = Depends(get_current_user)
):
    """
    현재 사용자 정보 조회
    """
    user_data = UserData(
        id=str(current_user.id),
        kakao_id=current_user.kakao_id,
        created_at=current_user.created_at.isoformat(),
        last_login_at=current_user.last_login_at.isoformat(),
        status=current_user.status
    )

    profile = None
    if current_user.name:
        profile = ProfileData(
            name=current_user.name,
            role=current_user.role or "",
            industry_code=current_user.industry_code,
            growth_goals=current_user.growth_goals or "",
            photo_url=current_user.photo_url
        )

    flags = UserFlags(
        onboarding_completed=current_user.onboarding_completed_at is not None
    )

    return UserMeResponse(
        user=user_data,
        profile=profile,
        flags=flags
    )


@router.post("/users/profile", response_model=ProfileResponse)
async def create_user_profile(
    request: ProfileRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    사용자 프로필 생성
    """
    # 이미 프로필이 있는지 확인
    if current_user.onboarding_completed_at:
        raise HTTPException(status_code=409, detail="Profile already set")

    # 프로필 업데이트
    current_user.name = request.name
    current_user.role = request.role
    current_user.industry_code = request.industry_code
    current_user.growth_goals = request.growth_goals
    current_user.onboarding_completed_at = datetime.utcnow()

    await db.commit()
    await db.refresh(current_user)

    profile = ProfileData(
        name=current_user.name,
        role=current_user.role,
        industry_code=current_user.industry_code,
        growth_goals=current_user.growth_goals,
        photo_url=current_user.photo_url
    )

    return ProfileResponse(
        user_id=str(current_user.id),
        profile=profile,
        onboarding_completed_at=current_user.onboarding_completed_at.isoformat()
    )
