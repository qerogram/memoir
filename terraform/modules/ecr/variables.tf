variable "environment" {
  description = "Environment name"
  type        = string
}

variable "repository_name" {
  description = "ECR repository name"
  type        = string
  default     = "memoir-backend"
}

variable "image_tag_mutability" {
  description = "Image tag mutability setting"
  type        = string
  default     = "MUTABLE"
}

variable "scan_on_push" {
  description = "Enable image scanning on push"
  type        = bool
  default     = true
}

variable "expiration_days" {
  description = "Days to keep untagged images"
  type        = number
  default     = 7
}
