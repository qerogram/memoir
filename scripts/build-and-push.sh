#!/bin/bash
# Build and push Docker image to ECR
# Usage: ./scripts/build-and-push.sh [dev|prod]

set -e

ENVIRONMENT=${1:-dev}
AWS_REGION=${AWS_REGION:-ap-northeast-2}
ECR_REPOSITORY=memoir-backend

echo "🚀 Building and pushing Docker image for $ENVIRONMENT environment"

# Get AWS account ID
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REGISTRY="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"

echo "📦 ECR Registry: $ECR_REGISTRY"

# Login to ECR
echo "🔐 Logging in to ECR..."
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin $ECR_REGISTRY

# Build image
echo "🏗️  Building Docker image..."
cd backend
docker build \
  --platform linux/amd64 \
  -t $ECR_REPOSITORY:latest \
  -t $ECR_REPOSITORY:$ENVIRONMENT-latest \
  -t $ECR_REPOSITORY:$(git rev-parse --short HEAD) \
  .

# Tag for ECR
docker tag $ECR_REPOSITORY:latest $ECR_REGISTRY/$ECR_REPOSITORY:latest
docker tag $ECR_REPOSITORY:latest $ECR_REGISTRY/$ECR_REPOSITORY:$ENVIRONMENT-latest
docker tag $ECR_REPOSITORY:latest $ECR_REGISTRY/$ECR_REPOSITORY:$(git rev-parse --short HEAD)

# Push to ECR
echo "⬆️  Pushing to ECR..."
docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest
docker push $ECR_REGISTRY/$ECR_REPOSITORY:$ENVIRONMENT-latest
docker push $ECR_REGISTRY/$ECR_REPOSITORY:$(git rev-parse --short HEAD)

echo "✅ Successfully pushed image to ECR!"
echo "📍 Image: $ECR_REGISTRY/$ECR_REPOSITORY:$ENVIRONMENT-latest"
