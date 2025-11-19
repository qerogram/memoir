# Production Environment

environment = "prod"
aws_region  = "ap-northeast-2"

# VPC
vpc_cidr = "10.1.0.0/16"

# Database
db_name     = "memoir_prod"
# db_username = "memoir_admin_prod" # Set via TF_VAR_db_username
# db_password = "..."               # Set via TF_VAR_db_password

# ECS
app_image        = "memoir/backend:latest"
app_port         = 8000
ecs_desired_count = 2

# S3
s3_bucket_name = "memoir-assets-prod"
