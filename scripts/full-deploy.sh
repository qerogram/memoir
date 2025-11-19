#!/bin/bash
# Full deployment: Build, Push, and Deploy
# Usage: ./scripts/full-deploy.sh [dev|prod]

set -e

ENVIRONMENT=${1:-dev}

echo "🚀 Starting full deployment for $ENVIRONMENT environment"
echo "=================================================="

# Step 1: Build and push
echo ""
echo "📦 Step 1: Building and pushing Docker image..."
./scripts/build-and-push.sh $ENVIRONMENT

# Step 2: Deploy to ECS
echo ""
echo "🚢 Step 2: Deploying to ECS..."
./scripts/deploy-ecs.sh $ENVIRONMENT

echo ""
echo "=================================================="
echo "✅ Full deployment completed successfully!"
