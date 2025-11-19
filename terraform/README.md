# Memoir Infrastructure - Terraform

**경량 아키텍처** (300명, 동접 10명 대응)

## 아키텍처 개요

```
┌─────────────────────────────────────────┐
│           CloudFront (Optional)         │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│     Application Load Balancer (ALB)     │
│         (Public Subnets)                │
└────────────────┬────────────────────────┘
                 │
        ┌────────┴─────────┐
        │                  │
┌───────▼────────┐ ┌──────▼───────┐
│ ECS Task (1)   │ │ ECS Task (2) │ (Auto-scaling)
│ FastAPI        │ │ FastAPI      │
│ (Fargate)      │ │ (Fargate)    │
│ 0.5 vCPU       │ │ 0.5 vCPU     │
│ 1GB RAM        │ │ 1GB RAM      │
│ Private Subnet │ │ Private Sub  │
└───────┬────────┘ └──────┬───────┘
        │                  │
        └────────┬─────────┘
                 │
        ┌────────▼─────────┐
        │   RDS PostgreSQL │
        │   db.t4g.micro   │
        │   1GB RAM, 20GB  │
        │   Private Subnet │
        └──────────────────┘
```

## 비용 추정

### Dev Environment
- **ECS Fargate**: $7-15/월 (0.5 vCPU, 1GB, 24/7)
- **RDS db.t4g.micro**: $12-15/월 (단일 AZ)
- **ALB**: $16-20/월
- **NAT Gateway**: $32/월
- **S3**: $1-3/월
- **Total**: **$68-85/월**

### Prod Environment
- **ECS Fargate**: $15-30/월 (1 vCPU, 2GB)
- **RDS db.t4g.small**: $25-30/월 (Multi-AZ)
- **ALB**: $20-25/월
- **NAT Gateway**: $32/월
- **S3 + CloudFront**: $5-10/월
- **Total**: **$97-127/월**

### 비용 절감 팁
1. **NAT Gateway 제거**: ECS에서 외부 API 호출 불필요 시
2. **RDS 스케줄링**: 개발 환경은 업무 시간에만 실행
3. **Fargate Spot**: 비-중요 환경에서 최대 70% 절감
4. **ALB → CloudFront**: 정적 자산 캐싱으로 트래픽 절감

## 사용 방법

### 1. 초기 설정

```bash
cd terraform

# S3 백엔드 생성 (처음 한 번만)
aws s3 mb s3://memoir-terraform-state --region ap-northeast-2
aws s3api put-bucket-versioning \
  --bucket memoir-terraform-state \
  --versioning-configuration Status=Enabled

# Terraform 초기화
terraform init
```

### 2. Development 배포

```bash
cd environments/dev

# 변수 설정
export TF_VAR_db_username="memoir_admin"
export TF_VAR_db_password="secure-password-here"

# Plan 확인
terraform plan

# 배포
terraform apply
```

### 3. Production 배포

```bash
cd environments/prod

export TF_VAR_db_username="memoir_admin_prod"
export TF_VAR_db_password="very-secure-password"

terraform apply
```

## 모듈 구조

- **vpc**: VPC, Subnets, NAT Gateway, Internet Gateway
- **rds**: PostgreSQL RDS (db.t4g.micro/small)
- **ecs**: Fargate cluster, ALB, Auto-scaling
- **s3**: Static assets bucket

## 리소스 상세

### ECS Fargate
- **Task CPU**: 0.5 vCPU (dev), 1 vCPU (prod)
- **Task Memory**: 1GB (dev), 2GB (prod)
- **Desired Count**: 1 (Auto-scaling: 1-3)
- **Health Check**: `/health` endpoint

### RDS PostgreSQL
- **Instance**: db.t4g.micro (dev), db.t4g.small (prod)
- **Storage**: 20GB GP3 (Auto-scaling up to 100GB)
- **Backup**: 1 day (dev), 7 days (prod)
- **Multi-AZ**: No (dev), Yes (prod)

### 네트워크
- **VPC CIDR**: 10.0.0.0/16
- **Public Subnets**: 10.0.0.0/24, 10.0.1.0/24 (ALB)
- **Private Subnets**: 10.0.2.0/24, 10.0.3.0/24 (ECS, RDS)

## 보안

- ✅ RDS는 Private Subnet에만 배치
- ✅ Security Group으로 포트 제한
- ✅ RDS 암호화 활성화
- ✅ Secrets는 환경 변수로 관리
- ✅ S3 버킷 암호화
- ✅ ALB HTTPS 리스너 (ACM 인증서)

## 모니터링

- CloudWatch Logs: ECS 로그 자동 수집
- CloudWatch Metrics: CPU, Memory, Request Count
- RDS Performance Insights: 비활성화 (비용 절감)

## 확장 시나리오

### 500-1,000명 (동접 50명)
- ECS Tasks: 1 → 2-3 (Auto-scaling)
- RDS: db.t4g.micro → db.t4g.small
- Redis 추가 (ElastiCache)

### 2,000-5,000명 (동접 200명)
- ECS Tasks: 3-5
- RDS: db.t4g.small → db.t4g.medium
- Multi-AZ 활성화
- CloudFront CDN 추가

## 트러블슈팅

### ECS Task가 시작되지 않음
```bash
# ECS 로그 확인
aws logs tail /ecs/memoir-api --follow

# Task 정의 확인
aws ecs describe-task-definition --task-definition memoir-api
```

### RDS 연결 실패
```bash
# Security Group 확인
aws ec2 describe-security-groups --group-ids sg-xxx

# RDS 상태 확인
aws rds describe-db-instances --db-instance-identifier memoir-db-dev
```

## 정리 (Destroy)

```bash
# ⚠️ 주의: 모든 리소스 삭제됨
terraform destroy

# 특정 리소스만 삭제
terraform destroy -target=module.ecs
```

## 다음 단계

1. [ ] ACM 인증서 생성 (HTTPS)
2. [ ] Route53 도메인 연결
3. [ ] CloudWatch Alarms 설정
4. [ ] CI/CD 파이프라인 (GitHub Actions)
5. [ ] Secrets Manager 연동
