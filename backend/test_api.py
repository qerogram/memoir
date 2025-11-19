"""
Backend API Tests
핵심 엔드포인트 검증
"""
import pytest
from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

def test_health_check():
    """Health check 엔드포인트 테스트"""
    response = client.get("/health")

    assert response.status_code == 200
    assert response.json() == {"status": "healthy"}


def test_root_endpoint():
    """Root 엔드포인트 테스트"""
    response = client.get("/")

    assert response.status_code == 200
    data = response.json()
    assert "message" in data
    assert "Memoir API" in data["message"]


def test_openapi_docs():
    """OpenAPI 문서 생성 확인"""
    response = client.get("/openapi.json")

    assert response.status_code == 200
    data = response.json()
    assert "openapi" in data
    assert "info" in data
    assert data["info"]["title"] == "Memoir API"


def test_auth_kakao_endpoint_exists():
    """Kakao 인증 엔드포인트 존재 확인"""
    # POST 요청 (실제 DB 없어서 실패하지만 엔드포인트는 존재)
    response = client.post(
        "/api/v1/auth/kakao",
        json={"kakao_oauth_code": "test"}
    )

    # 500 (DB 없음) 또는 422 (validation) 예상
    # 404가 아니면 엔드포인트는 존재하는 것
    assert response.status_code != 404


def test_cors_headers():
    """CORS 설정 확인"""
    response = client.options(
        "/health",
        headers={"Origin": "http://localhost:3000"}
    )

    # CORS 헤더 존재 확인
    assert "access-control-allow-origin" in response.headers or response.status_code == 200
