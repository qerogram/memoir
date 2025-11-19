"""
Auth 유틸리티 테스트
JWT 토큰 생성/검증
"""
import pytest
from auth import create_access_token, create_refresh_token, verify_token, hash_token
from datetime import datetime, timedelta


def test_create_access_token():
    """Access token 생성 테스트"""
    data = {"user_id": "test-user-123", "kakao_id": "kakao123"}
    token = create_access_token(data)

    assert token is not None
    assert isinstance(token, str)
    assert len(token) > 50  # JWT는 충분히 길어야 함


def test_create_refresh_token():
    """Refresh token 생성 테스트"""
    data = {"user_id": "test-user-123", "kakao_id": "kakao123"}
    token = create_refresh_token(data)

    assert token is not None
    assert isinstance(token, str)
    assert len(token) > 50


def test_verify_token_valid():
    """유효한 토큰 검증 테스트"""
    data = {"user_id": "test-user-123", "kakao_id": "kakao123"}
    token = create_access_token(data)

    payload = verify_token(token, "access")

    assert payload is not None
    assert payload["user_id"] == "test-user-123"
    assert payload["kakao_id"] == "kakao123"
    assert payload["type"] == "access"


def test_verify_token_invalid():
    """잘못된 토큰 검증 테스트"""
    invalid_token = "invalid.token.here"

    payload = verify_token(invalid_token, "access")

    assert payload is None


def test_hash_token():
    """토큰 해싱 테스트"""
    token = "my-refresh-token-12345"
    hashed = hash_token(token)

    assert hashed is not None
    assert hashed != token  # 해시되었음
    assert len(hashed) == 60  # bcrypt 해시 길이

    # 같은 토큰은 매번 다른 해시 (salt)
    hashed2 = hash_token(token)
    assert hashed != hashed2


def test_token_expiry():
    """토큰 만료 시간 확인"""
    data = {"user_id": "test-user-123"}
    token = create_access_token(data)
    payload = verify_token(token, "access")

    assert "exp" in payload

    # 만료 시간이 현재보다 미래인지 확인
    exp_timestamp = payload["exp"]
    assert exp_timestamp > datetime.utcnow().timestamp()
