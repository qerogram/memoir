variable "environment" {
  description = "Environment name"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "public_subnet_ids" {
  description = "Public subnet IDs for ALB"
  type        = list(string)
}

variable "private_subnet_ids" {
  description = "Private subnet IDs for ECS tasks (prod only)"
  type        = list(string)
}

variable "app_image" {
  description = "Docker image for the application"
  type        = string
}

variable "app_port" {
  description = "Port the application listens on"
  type        = number
  default     = 8000
}

variable "desired_count" {
  description = "Desired number of ECS tasks"
  type        = number
  default     = 1
}

variable "cpu" {
  description = "CPU units for the task (256 = 0.25 vCPU)"
  type        = string
  default     = "256" # 0.25 vCPU - 충분함 (300명, 동접 10명)
}

variable "memory" {
  description = "Memory for the task in MB"
  type        = string
  default     = "512" # 0.5GB - FastAPI는 메모리 적게 씀
}

variable "db_endpoint" {
  description = "RDS endpoint"
  type        = string
}

variable "db_name" {
  description = "Database name"
  type        = string
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
