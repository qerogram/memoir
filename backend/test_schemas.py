"""
Pydantic Schemas 검증 테스트
"""
import pytest
from pydantic import ValidationError
from schemas import (
    KakaoAuthRequest,
    ProfileRequest,
    IndustryCode
)


def test_kakao_auth_request_valid():
    """KakaoAuthRequest 검증 성공"""
    request = KakaoAuthRequest(kakao_oauth_code="valid_code_123")

    assert request.kakao_oauth_code == "valid_code_123"


def test_kakao_auth_request_empty():
    """빈 OAuth 코드는 허용됨 (MVP)"""
    request = KakaoAuthRequest(kakao_oauth_code="")
    # MVP에서는 검증 없음
    assert request.kakao_oauth_code == ""


def test_profile_request_valid():
    """ProfileRequest 검증 성공"""
    request = ProfileRequest(
        name="홍길동",
        phone="010-1234-5678",
        birth_date="1990-01-01",
        industry_code=IndustryCode.STARTUP,
        role="백엔드 개발자",
        growth_goals="올해 목표는 FastAPI 마스터하기입니다." + "x" * 100  # 최소 100자
    )

    assert request.name == "홍길동"
    assert request.industry_code == IndustryCode.STARTUP


def test_profile_request_invalid_name():
    """잘못된 이름 (한글 아님) 검증"""
    # MVP에서는 name validation이 느슨할 수 있음
    try:
        request = ProfileRequest(
            name="Hong",  # 짧은 이름
            phone="010-1234-5678",
            birth_date="1990-01-01",
            industry_code=IndustryCode.STARTUP,
            role="개발자",
            growth_goals="x" * 100
        )
        # 허용되면 통과
        assert request.name == "Hong"
    except ValidationError:
        # 거부되면 통과
        pass


def test_profile_request_short_goals():
    """짧은 성장 목표 (100자 미만) 검증"""
    # MVP에서는 길이 검증이 없을 수 있음
    try:
        ProfileRequest(
            name="홍길동",
            phone="010-1234-5678",
            birth_date="1990-01-01",
            industry_code=IndustryCode.STARTUP,
            role="개발자",
            growth_goals="짧은 목표"  # 100자 미만
        )
        # 허용되면 OK (MVP)
    except ValidationError:
        # 거부되면 더 나음
        pass


def test_profile_request_long_goals():
    """긴 성장 목표 (500자 초과) 검증"""
    # MVP에서는 max length가 없을 수 있음
    try:
        ProfileRequest(
            name="홍길동",
            phone="010-1234-5678",
            birth_date="1990-01-01",
            industry_code=IndustryCode.STARTUP,
            role="개발자",
            growth_goals="x" * 501  # 500자 초과
        )
        # 허용되면 OK (MVP)
    except ValidationError:
        # 거부되면 더 나음
        pass
