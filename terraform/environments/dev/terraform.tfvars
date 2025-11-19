# Development Environment

environment = "dev"
aws_region  = "ap-northeast-2"

# VPC
vpc_cidr = "10.0.0.0/16"

# Database
db_name     = "memoir_dev"
# db_username = "memoir_admin" # Set via TF_VAR_db_username
# db_password = "..."           # Set via TF_VAR_db_password

# ECS
app_image        = "memoir/backend:latest"
app_port         = 8000
ecs_desired_count = 1

# S3
s3_bucket_name = "memoir-assets-dev"
