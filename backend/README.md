# Memoir Backend - Python FastAPI

**가벼운 아키텍처** (300명, 동접 10명 대응)

## 왜 FastAPI?

### Node.js Express 대비 장점
- ✅ **더 빠른 개발**: Python의 간결함
- ✅ **타입 안정성**: Pydantic으로 자동 검증
- ✅ **자동 문서화**: Swagger UI 내장
- ✅ **비동기 지원**: async/await 네이티브
- ✅ **가벼움**: 300명 규모에 딱 맞는 심플함

### 아키텍처 철학
- **YAGNI** (You Aren't Gonna Need It)
- **단순함 우선**: Redis, Celery, Kafka 등 불필요
- **점진적 확장**: 필요할 때만 추가

## 기술 스택

### Core
- **FastAPI**: 웹 프레임워크
- **SQLAlchemy**: ORM (async)
- **PostgreSQL**: 데이터베이스 (Supabase 무료 플랜으로 충분)
- **Alembic**: DB 마이그레이션

### 인증
- **JWT**: Access/Refresh token
- **Kakao OAuth**: 카카오 로그인

### 배포
- **Render** (무료 플랜): 동접 10명에 충분
- **Railway** (대안)
- **Fly.io** (대안)

## 설치 및 실행

```bash
cd backend

# 가상환경 생성
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 의존성 설치
pip install -r requirements.txt

# 환경 변수 설정
cp .env.example .env
# .env 파일 수정 (DATABASE_URL, KAKAO_REST_API_KEY 등)

# 데이터베이스 마이그레이션
alembic upgrade head

# 서버 실행
python main.py
```

서버: http://localhost:8000
Swagger 문서: http://localhost:8000/docs

## API 엔드포인트

### 인증
- `POST /api/v1/auth/kakao` - 카카오 OAuth 토큰 교환
- `POST /api/v1/auth/refresh` - 토큰 갱신

### 사용자
- `GET /api/v1/users/me` - 현재 사용자 조회
- `POST /api/v1/users/profile` - 프로필 생성

## 성능 목표

### 현재 규모 (300명, 동접 10명)
- ✅ 응답 시간: <200ms (p95)
- ✅ CPU: <10%
- ✅ 메모리: <512MB
- ✅ 비용: $0-7/월 (Render 무료 플랜 or 최소 유료)

### 확장 시점 (1,000명, 동접 50명)
- Redis 캐싱 추가
- PostgreSQL 인덱스 최적화
- Horizontal scaling (2-3 인스�스)

## 미사용 기술 (의도적)

### ❌ 지금 불필요한 것들
- **Redis**: 캐싱 필요 없음 (PostgreSQL로 충분)
- **Celery**: 백그라운드 작업 거의 없음
- **Docker**: 로컬 개발 복잡도만 증가
- **Kubernetes**: 오버킬
- **Microservices**: 단일 모놀리스로 충분
- **GraphQL**: REST API로 충분

### 추가 고려 시점
- **Redis**: 동접 100명 이상 또는 응답 시간 >500ms
- **Background jobs**: 배치 작업 필요 시 (패널티 계산 등)
- **CDN**: 이미지 업로드 많아질 때 (S3 + CloudFront)

## 비용 추정

### 무료 시작 (0-100명)
- **Render Free**: $0
- **Supabase Free**: $0
- **Total**: $0/월

### 최소 유료 (100-500명)
- **Render Starter**: $7/월
- **Supabase Pro**: $25/월 (선택사항)
- **Total**: $7-32/월

### 확장 시 (500-2,000명)
- **Render Standard**: $25/월
- **Supabase Pro**: $25/월
- **Total**: $50/월

## 개발 우선순위

### Phase 0 (MVP - 1주)
- [x] FastAPI 기본 설정
- [ ] 데이터베이스 모델 (User)
- [ ] 카카오 OAuth 통합
- [ ] JWT 인증
- [ ] 프로필 API

### Phase 1 (코어 - 2주)
- [ ] 코호트 관리
- [ ] 리플렉션 CRUD
- [ ] 댓글 시스템
- [ ] 이미지 업로드 (로컬 저장)

### Phase 2 (확장 - 4주)
- [ ] 보증금 시스템
- [ ] 패널티 계산
- [ ] 알림 (이메일)
- [ ] 관리자 대시보드

## 모니터링

### 개발
- FastAPI 기본 로깅
- SQLAlchemy echo (쿼리 로그)

### 프로덕션
- Sentry (무료 플랜)
- PostgreSQL slow query log
- Render 기본 메트릭

## 마이그레이션 계획

### Node.js → Python 전환 이유
1. **개발 속도**: Python이 더 빠름
2. **단순함**: 타입스크립트 설정 복잡도 제거
3. **생태계**: SQLAlchemy, Pydantic 강력
4. **비용**: 무료 호스팅 옵션 많음

### 롤백 계획
- API 스펙은 동일 (RESTful JSON)
- 클라이언트 코드 변경 없음
- 필요 시 Node.js로 다시 전환 가능
