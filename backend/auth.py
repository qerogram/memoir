"""
JWT 토큰 유틸리티
"""
from datetime import datetime, timedelta
from jose import JWTError, jwt
from passlib.context import CryptContext
from config import settings

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def create_access_token(data: dict) -> str:
    """
    액세스 토큰 생성 (30일)
    """
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(days=settings.ACCESS_TOKEN_EXPIRE_DAYS)
    to_encode.update({"exp": expire, "type": "access"})
    return jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)


def create_refresh_token(data: dict) -> str:
    """
    리프레시 토큰 생성 (90일)
    """
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)
    to_encode.update({"exp": expire, "type": "refresh"})
    return jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)


def verify_token(token: str, token_type: str = "access") -> dict | None:
    """
    토큰 검증
    """
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
        if payload.get("type") != token_type:
            return None
        return payload
    except JWTError:
        return None


def hash_token(token: str) -> str:
    """
    토큰 해시 (DB 저장용)
    """
    return pwd_context.hash(token)


def verify_hashed_token(plain_token: str, hashed_token: str) -> bool:
    """
    해시된 토큰 검증
    """
    return pwd_context.verify(plain_token, hashed_token)
