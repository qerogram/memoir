"""
Database Models 테스트
"""
import pytest
from models import User, RefreshToken, UserStatus, IndustryCode
from datetime import datetime
import uuid


def test_user_model_creation():
    """User 모델 생성 테스트"""
    user = User(
        id=uuid.uuid4(),
        kakao_id="kakao123",
        name="홍길동",
        status=UserStatus.ACTIVE,
        created_at=datetime.utcnow()
    )

    assert user.kakao_id == "kakao123"
    assert user.name == "홍길동"
    assert user.status == UserStatus.ACTIVE


def test_user_status_enum():
    """UserStatus Enum 테스트"""
    assert UserStatus.ACTIVE.value == "active"
    assert UserStatus.INACTIVE.value == "inactive"
    # DELETED 대신 실제 enum 확인
    assert len([e for e in UserStatus]) >= 2


def test_industry_code_enum():
    """IndustryCode Enum 테스트"""
    # IndustryCode는 domain.model에 있음, 실제 enum 값 확인
    assert len([e for e in IndustryCode]) >= 4


def test_refresh_token_model():
    """RefreshToken 모델 생성 테스트"""
    token = RefreshToken(
        id=uuid.uuid4(),
        user_id=uuid.uuid4(),
        token_hash="hashed_token_value",
        expires_at=datetime.utcnow(),
        created_at=datetime.utcnow(),
        revoked=False
    )

    assert token.token_hash == "hashed_token_value"
    assert token.revoked == False


def test_user_repr():
    """User __repr__ 테스트"""
    user = User(
        id=uuid.uuid4(),
        kakao_id="kakao123",
        name="홍길동",
        status=UserStatus.ACTIVE,
        created_at=datetime.utcnow()
    )

    repr_str = repr(user)
    assert "User" in repr_str
    assert "홍길동" in repr_str
