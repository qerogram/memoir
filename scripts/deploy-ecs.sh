#!/bin/bash
# Deploy new image to ECS
# Usage: ./scripts/deploy-ecs.sh [dev|prod]

set -e

ENVIRONMENT=${1:-dev}
AWS_REGION=${AWS_REGION:-ap-northeast-2}
CLUSTER_NAME="memoir-cluster-$ENVIRONMENT"
SERVICE_NAME="memoir-api-service-$ENVIRONMENT"

echo "🚀 Deploying to ECS cluster: $CLUSTER_NAME"

# Force new deployment
aws ecs update-service \
  --cluster $CLUSTER_NAME \
  --service $SERVICE_NAME \
  --force-new-deployment \
  --region $AWS_REGION \
  --output json > /dev/null

echo "✅ Deployment initiated!"
echo "📊 Monitoring deployment status..."

# Wait for service to stabilize
aws ecs wait services-stable \
  --cluster $CLUSTER_NAME \
  --services $SERVICE_NAME \
  --region $AWS_REGION

echo "✅ Deployment completed successfully!"
echo "🔗 Service: $SERVICE_NAME"

# Get ALB DNS
ALB_DNS=$(aws elbv2 describe-load-balancers \
  --region $AWS_REGION \
  --query "LoadBalancers[?contains(LoadBalancerName, 'memoir-alb-$ENVIRONMENT')].DNSName" \
  --output text)

if [ -n "$ALB_DNS" ]; then
  echo "🌐 ALB URL: http://$ALB_DNS"
  echo "💚 Health check: http://$ALB_DNS/health"
fi
