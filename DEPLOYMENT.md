# Memoir - 배포 가이드

완전 자동화된 AWS 배포 시스템 (Terraform + GitHub Actions)

## 🏗️ 인프라 아키텍처

```
GitHub Actions (CI/CD)
    ↓
ECR (Docker Registry)
    ↓
ECS Fargate (0.5-1 vCPU, 1-2GB)
    ↓
RDS PostgreSQL (db.t4g.micro/small)
```

## 📋 사전 요구사항

1. **AWS CLI 설치 및 구성**
```bash
aws configure
# AWS Access Key ID, Secret Access Key, Region (ap-northeast-2) 입력
```

2. **Terraform 설치**
```bash
# macOS
brew install terraform

# Linux
wget https://releases.hashicorp.com/terraform/1.7.0/terraform_1.7.0_linux_amd64.zip
unzip terraform_1.7.0_linux_amd64.zip
sudo mv terraform /usr/local/bin/
```

3. **Docker 설치**
```bash
# macOS
brew install --cask docker

# Linux
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
```

## 🚀 초기 배포 (처음 한 번만)

### 1단계: S3 백엔드 생성

```bash
# Terraform 상태 파일 저장용 S3 버킷 생성
aws s3 mb s3://memoir-terraform-state --region ap-northeast-2

# 버전 관리 활성화
aws s3api put-bucket-versioning \
  --bucket memoir-terraform-state \
  --versioning-configuration Status=Enabled

# 암호화 활성화
aws s3api put-bucket-encryption \
  --bucket memoir-terraform-state \
  --server-side-encryption-configuration '{
    "Rules": [{
      "ApplyServerSideEncryptionByDefault": {
        "SSEAlgorithm": "AES256"
      }
    }]
  }'
```

### 2단계: Development 환경 배포

```bash
cd terraform/environments/dev

# Terraform 초기화
terraform init

# 데이터베이스 자격증명 설정
export TF_VAR_db_username="memoir_admin"
export TF_VAR_db_password="$(openssl rand -base64 32)"

# 변경사항 확인
terraform plan

# 인프라 생성
terraform apply

# 출력 확인
terraform output
```

### 3단계: Docker 이미지 빌드 및 배포

```bash
# 루트 디렉토리로 이동
cd /path/to/memoir

# 이미지 빌드 & ECR 푸시
./scripts/build-and-push.sh dev

# ECS에 배포
./scripts/deploy-ecs.sh dev

# 또는 한 번에 (빌드 + 배포)
./scripts/full-deploy.sh dev
```

### 4단계: 배포 확인

```bash
# ALB DNS 가져오기
ALB_DNS=$(terraform -chdir=terraform/environments/dev output -raw alb_dns_name)

# Health check
curl http://$ALB_DNS/health

# API 테스트
curl http://$ALB_DNS/api/v1/health
```

## 🔄 GitHub Actions 자동 배포 설정

### 1단계: GitHub OIDC 활성화

```bash
cd terraform/environments/dev

# GitHub OIDC 활성화
export TF_VAR_enable_github_oidc=true
export TF_VAR_github_org="qerogram"  # 또는 본인의 GitHub 조직/사용자명
export TF_VAR_github_repo="memoir"

terraform apply

# IAM Role ARN 확인
terraform output github_role_arn
```

### 2단계: GitHub Secrets 설정

GitHub 리포지토리 → Settings → Secrets and variables → Actions

**필수 Secret 추가:**
- `AWS_ROLE_ARN`: Terraform 출력의 `github_role_arn` 값

### 3단계: 자동 배포 테스트

```bash
# develop 브랜치에 푸시 → dev 환경 자동 배포
git checkout develop
git add .
git commit -m "feat: 새 기능 추가"
git push origin develop

# main 브랜치에 푸시 → prod 환경 자동 배포
git checkout main
git merge develop
git push origin main
```

## 🏭 Production 환경 배포

```bash
cd terraform/environments/prod

# Terraform 초기화
terraform init

# Production DB 자격증명 (더 강력한 비밀번호 사용)
export TF_VAR_db_username="memoir_admin_prod"
export TF_VAR_db_password="$(openssl rand -base64 48)"

# Production 인프라 생성
terraform apply

# 이미지 배포
cd /path/to/memoir
./scripts/full-deploy.sh prod
```

## 📊 모니터링

### CloudWatch Logs

```bash
# ECS 로그 실시간 확인
aws logs tail /ecs/memoir-api-dev --follow

# 에러 로그만 확인
aws logs tail /ecs/memoir-api-dev --follow --filter-pattern "ERROR"
```

### ECS 상태 확인

```bash
# 서비스 상태
aws ecs describe-services \
  --cluster memoir-cluster-dev \
  --services memoir-api-service-dev

# 실행 중인 태스크
aws ecs list-tasks \
  --cluster memoir-cluster-dev \
  --service-name memoir-api-service-dev
```

### RDS 연결 테스트

```bash
# Bastion Host를 통한 연결 (필요 시)
DB_ENDPOINT=$(terraform -chdir=terraform/environments/dev output -raw db_endpoint)
psql -h $DB_ENDPOINT -U memoir_admin -d memoir_dev
```

## 🔧 트러블슈팅

### ECS 태스크가 시작되지 않음

```bash
# 태스크 로그 확인
aws ecs describe-tasks \
  --cluster memoir-cluster-dev \
  --tasks $(aws ecs list-tasks --cluster memoir-cluster-dev --query 'taskArns[0]' --output text)

# CloudWatch Logs 확인
aws logs tail /ecs/memoir-api-dev --follow
```

### ECR 푸시 실패

```bash
# ECR 로그인 재시도
aws ecr get-login-password --region ap-northeast-2 | \
  docker login --username AWS --password-stdin $(aws sts get-caller-identity --query Account --output text).dkr.ecr.ap-northeast-2.amazonaws.com

# Docker 이미지 확인
docker images | grep memoir
```

### RDS 연결 실패

```bash
# Security Group 확인
aws ec2 describe-security-groups \
  --filters "Name=tag:Name,Values=memoir-rds-sg-dev"

# RDS 상태 확인
aws rds describe-db-instances \
  --db-instance-identifier memoir-db-dev
```

## 💰 비용 최적화

### Development 환경 스케줄링 (선택사항)

```bash
# 업무 시간에만 실행 (평일 9-18시)
# EventBridge + Lambda로 자동 시작/중지 구현 가능

# 수동으로 RDS 중지 (최대 7일)
aws rds stop-db-instance --db-instance-identifier memoir-db-dev

# 수동으로 RDS 시작
aws rds start-db-instance --db-instance-identifier memoir-db-dev
```

### ECS 태스크 수 조절

```bash
# Dev 환경 태스크 0으로 설정 (사용하지 않을 때)
aws ecs update-service \
  --cluster memoir-cluster-dev \
  --service memoir-api-service-dev \
  --desired-count 0

# 다시 1로 설정
aws ecs update-service \
  --cluster memoir-cluster-dev \
  --service memoir-api-service-dev \
  --desired-count 1
```

## 🗑️ 인프라 삭제

**⚠️ 주의: 모든 데이터가 삭제됩니다!**

```bash
cd terraform/environments/dev

# Production 보호 비활성화 (prod인 경우)
aws rds modify-db-instance \
  --db-instance-identifier memoir-db-prod \
  --no-deletion-protection

# 인프라 삭제
terraform destroy

# S3 백엔드 삭제 (선택사항)
aws s3 rb s3://memoir-terraform-state --force
```

## 📚 주요 명령어 요약

```bash
# 빌드 & 푸시
./scripts/build-and-push.sh [dev|prod]

# ECS 배포
./scripts/deploy-ecs.sh [dev|prod]

# 전체 배포 (빌드 + 푸시 + 배포)
./scripts/full-deploy.sh [dev|prod]

# 로그 확인
aws logs tail /ecs/memoir-api-[dev|prod] --follow

# 인프라 업데이트
cd terraform/environments/[dev|prod]
terraform plan
terraform apply
```

## 🔗 관련 문서

- [Terraform README](terraform/README.md) - 인프라 상세 설명
- [Backend README](backend/README.md) - API 문서
- [GitHub Actions](.github/workflows/deploy.yml) - CI/CD 워크플로우

## 📞 지원

문제가 발생하면 다음을 확인하세요:
1. CloudWatch Logs: `/ecs/memoir-api-[env]`
2. ECS 서비스 이벤트
3. RDS 상태 및 연결
4. Security Groups 설정
