"""
Database Models
"""
from datetime import datetime
from sqlalchemy import Column, String, Text, DateTime, Boolean, Enum as SQLEnum, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.dialects.postgresql import UUID
import uuid
import enum

from database import Base


class UserStatus(str, enum.Enum):
    ACTIVE = "active"
    INACTIVE = "inactive"
    LOCKED = "locked"


class IndustryCode(str, enum.Enum):
    STARTUP = "STARTUP"
    ENTERPRISE = "ENTERPRISE"
    SME = "SME"
    PUBLIC = "PUBLIC"
    FOREIGN = "FOREIGN"
    FREELANCER = "FREELANCER"
    NONPROFIT = "NONPROFIT"
    OTHER = "OTHER"


class User(Base):
    __tablename__ = "users"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    kakao_id = Column(String(255), unique=True, nullable=False, index=True)

    # Profile
    name = Column(String(10), nullable=True)
    role = Column(String(64), nullable=True)
    industry_code = Column(SQLEnum(IndustryCode), nullable=True)
    growth_goals = Column(Text, nullable=True)
    photo_url = Column(String(512), nullable=True)

    # Metadata
    status = Column(SQLEnum(UserStatus), default=UserStatus.ACTIVE, nullable=False)
    onboarding_completed_at = Column(DateTime, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    last_login_at = Column(DateTime, default=datetime.utcnow, nullable=False)

    def __repr__(self):
        return f"<User {self.id} - {self.name or 'No name'}>"


class RefreshToken(Base):
    __tablename__ = "refresh_tokens"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = Column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
        index=True
    )
    token_hash = Column(String(255), nullable=False, unique=True, index=True)
    expires_at = Column(DateTime, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    revoked = Column(Boolean, default=False, nullable=False)

    # Relationship
    user = relationship("User", backref="refresh_tokens")

    def __repr__(self):
        return f"<RefreshToken {self.id} for user {self.user_id}>"
