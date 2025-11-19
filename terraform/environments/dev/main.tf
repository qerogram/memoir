# Development Environment Configuration

terraform {
  backend "s3" {
    bucket = "memoir-terraform-state"
    key    = "dev/terraform.tfstate"
    region = "ap-northeast-2"
    encrypt = true
  }
}

# Use the root module
module "memoir_dev" {
  source = "../.."

  environment      = var.environment
  aws_region       = var.aws_region
  vpc_cidr         = var.vpc_cidr
  db_name          = var.db_name
  db_username      = var.db_username
  db_password      = var.db_password
  app_image        = var.app_image
  app_port         = var.app_port
  ecs_desired_count = var.ecs_desired_count
  s3_bucket_name   = var.s3_bucket_name
}

output "alb_dns_name" {
  description = "ALB DNS name"
  value       = module.memoir_dev.alb_dns_name
}

output "db_endpoint" {
  description = "RDS endpoint"
  value       = module.memoir_dev.db_endpoint
  sensitive   = true
}
