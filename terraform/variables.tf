# Variables

variable "aws_region" {
  description = "AWS Region"
  type        = string
  default     = "ap-northeast-2" # Seoul
}

variable "environment" {
  description = "Environment (dev, prod)"
  type        = string
}

variable "vpc_cidr" {
  description = "VPC CIDR block"
  type        = string
  default     = "10.0.0.0/16"
}

# Database
variable "db_name" {
  description = "Database name"
  type        = string
  default     = "memoir"
}

variable "db_username" {
  description = "Database username"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Database password"
  type        = string
  sensitive   = true
}

# ECS
variable "app_image" {
  description = "Docker image for app"
  type        = string
}

variable "app_port" {
  description = "Application port"
  type        = number
  default     = 8000
}

variable "ecs_desired_count" {
  description = "Desired number of ECS tasks"
  type        = number
  default     = 1 # 300명 규모에는 1개로 충분
}

# S3
variable "s3_bucket_name" {
  description = "S3 bucket name for static assets"
  type        = string
}

# GitHub OIDC (Optional)
variable "github_org" {
  description = "GitHub organization or username"
  type        = string
  default     = ""
}

variable "github_repo" {
  description = "GitHub repository name"
  type        = string
  default     = ""
}

variable "enable_github_oidc" {
  description = "Enable GitHub OIDC for CI/CD"
  type        = bool
  default     = false
}
