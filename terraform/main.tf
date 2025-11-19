# Memoir Infrastructure - Terraform
# 가벼운 아키텍처 (300명, 동접 10명)

terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # S3 백엔드 (상태 파일 저장)
  backend "s3" {
    bucket = "memoir-terraform-state"
    key    = "terraform.tfstate"
    region = "ap-northeast-2"
    # dynamodb_table = "memoir-terraform-locks"
    encrypt = true
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "Memoir"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# VPC Module
module "vpc" {
  source = "./modules/vpc"

  environment = var.environment
  vpc_cidr    = var.vpc_cidr
}

# RDS PostgreSQL Module
module "rds" {
  source = "./modules/rds"

  environment        = var.environment
  vpc_id            = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  db_name           = var.db_name
  db_username       = var.db_username
  db_password       = var.db_password
}

# ECS Fargate Module
module "ecs" {
  source = "./modules/ecs"

  environment         = var.environment
  vpc_id             = module.vpc.vpc_id
  public_subnet_ids  = module.vpc.public_subnet_ids
  private_subnet_ids = module.vpc.private_subnet_ids
  app_image          = var.app_image
  app_port           = var.app_port
  desired_count      = var.ecs_desired_count

  # Database connection
  db_endpoint  = module.rds.db_endpoint
  db_name      = module.rds.db_name
  db_username  = var.db_username
  db_password  = var.db_password
}

# S3 for static assets
module "s3" {
  source = "./modules/s3"

  environment = var.environment
  bucket_name = var.s3_bucket_name
}
