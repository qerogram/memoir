# Memoir - 비용 대안 분석 (300명 규모)

## 현재 AWS 구성의 문제

**AWS 최소 비용: $68-85/월**
- RDS db.t3.micro: $9-15/월
- ECS Fargate: $7-15/월
- ALB: $16-20/월 ← 이게 제일 비쌈!
- NAT Gateway: $32/월
- S3: $1-3/월

**문제점:**
- 300명, 동접 10명에 너무 비쌈
- ALB 필수 ($16), NAT Gateway 필수 ($32)
- 최소 비용이 높음

---

## 대안 1: 외부 서비스 조합 (최저가)

### 💰 **월 $0-5** (99% 절감!)

**Backend: Render.com**
- 무료 tier: 512MB RAM, 0.1 vCPU
- 스핀다운 있음 (15분 미사용 시 슬립)
- Python/FastAPI 지원
- 자동 HTTPS
- **비용: $0** (또는 스핀다운 없으면 $7/월)

**Database: Supabase**
- PostgreSQL 500MB 무료
- 무제한 API 요청
- 자동 백업
- Connection pooling
- 300명 충분
- **비용: $0**

**Static Files: Cloudflare R2**
- 10GB 무료
- 무료 CDN
- **비용: $0**

**총 비용: $0/월** (스핀다운 허용 시)
**총 비용: $7/월** (24/7 운영 시)

### 장점
- ✅ 압도적으로 저렴
- ✅ 관리 간편
- ✅ 자동 HTTPS
- ✅ Git push로 배포

### 단점
- ⚠️ 무료는 cold start (첫 요청 느림)
- ⚠️ 제한적인 스케일링
- ⚠️ 벤더 락인

---

## 대안 2: Railway.app (추천!)

### 💰 **월 $5-10**

**Backend + DB 통합**
- 512MB RAM, 공유 vCPU
- PostgreSQL 포함
- 무제한 대역폭
- GitHub 연동 자동 배포
- 스핀다운 없음
- **비용: $5/월 (Hobby plan)**

**Static: Vercel/Netlify**
- 무료
- CDN 포함

**총 비용: $5-10/월** (93% 절감!)

### 장점
- ✅ 스핀다운 없음
- ✅ DB 포함
- ✅ 관리 매우 간편
- ✅ 300명에 충분

### 단점
- ⚠️ 커스터마이징 제한
- ⚠️ 한국 리전 없음 (약간 느릴 수 있음)

---

## 대안 3: AWS App Runner (AWS 내 대안)

### 💰 **월 $15-25**

**App Runner**
- 컨테이너 기반 (ECS보다 저렴)
- ALB 불필요 (내장)
- NAT Gateway 불필요
- Auto-scaling
- **비용: $10-15/월**

**Database: Neon.tech**
- PostgreSQL serverless
- 0.5GB 무료
- 또는 $19/월 (10GB)
- **비용: $0-19/월**

**총 비용: $10-34/월** (50-75% 절감)

### 장점
- ✅ AWS 생태계 유지
- ✅ ECS보다 간편
- ✅ ALB 비용 없음

### 단점
- ⚠️ 여전히 비쌈
- ⚠️ RDS 대신 외부 DB

---

## 대안 4: Lambda + RDS Proxy (서버리스)

### 💰 **월 $5-15**

**Lambda Functions**
- 요청당 과금
- 동접 10명이면 거의 무료
- Cold start 있음
- **비용: $0-2/월**

**RDS Proxy + Aurora Serverless v2**
- 사용한 만큼만 과금
- 최소 0.5 ACU ($0.12/시간)
- **비용: $5-10/월**

**총 비용: $5-12/월** (85% 절감)

### 장점
- ✅ 진짜 사용량 기반
- ✅ 자동 스케일링

### 단점
- ⚠️ Cold start
- ⚠️ 복잡한 설정
- ⚠️ FastAPI 구조 변경 필요

---

## 비교표

| 구성 | 월 비용 | 보안 | 성능 | 관리 | 추천도 |
|------|---------|------|------|------|--------|
| **현재 AWS** | $68-85 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐ |
| **최적화 AWS** | $20-37 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |
| **Render + Supabase** | $0-7 | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Railway** | $5-10 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **App Runner** | $10-34 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Lambda** | $5-12 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |

---

## 추천: Railway.app ($5/월)

**이유:**
1. **압도적으로 저렴**: AWS 대비 93% 절감
2. **보안 충분**: HTTPS, 격리된 환경, 정기 보안 업데이트
3. **관리 간편**: Git push → 자동 배포
4. **DB 포함**: PostgreSQL 내장
5. **스핀다운 없음**: 항상 켜져 있음
6. **300명 충분**: 512MB RAM, 공유 vCPU로 동접 10명 여유

**단점:**
- 한국 리전 없음 (미국 서버, 레이턴시 ~150ms)
- 커스터마이징 제한

---

## 실전 배포 예시 (Railway)

### 1. Railway 설정

```bash
# Railway CLI 설치
npm i -g @railway/cli

# 프로젝트 연결
railway login
railway init

# PostgreSQL 추가
railway add

# 환경변수 설정
railway variables set DATABASE_URL=...
railway variables set SECRET_KEY=...

# 배포
git push
```

### 2. 비용 모니터링

Railway 대시보드에서 실시간 비용 확인 가능.
$5/월 넘으면 알림.

---

## 결론

**300명, 동접 10명 기준:**

**MVP 단계:** Railway.app ($5/월) ✅ **최고 추천**
- 보안: 충분
- 비용: 최저
- 성능: 충분

**사용자 증가 시 (500-1000명):**
- Railway Pro ($20/월) 또는
- AWS로 마이그레이션

**2000명+ 이상:**
- AWS ECS + RDS (현재 구성)

---

## 다음 단계

Railway로 갈까요? 아니면 AWS 유지하면서 더 최적화할까요?
