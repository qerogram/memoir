"""
Database 설정 - SQLAlchemy Async
"""
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from sqlalchemy.orm import declarative_base

from config import settings

# Async engine
import os
engine = create_async_engine(
    settings.DATABASE_URL,
    echo=os.getenv("SQL_ECHO", "false").lower() == "true",  # Disable in production
    future=True,
)

# Session maker
AsyncSessionLocal = async_sessionmaker(
    engine,
    class_=AsyncSession,
    expire_on_commit=False,
)

Base = declarative_base()


async def get_db():
    """
    FastAPI dependency for database session
    """
    async with AsyncSessionLocal() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()
