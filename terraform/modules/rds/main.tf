# RDS PostgreSQL Module (경량 버전)

resource "aws_db_subnet_group" "main" {
  name       = "memoir-db-subnet-${var.environment}"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "memoir-db-subnet-group-${var.environment}"
  }
}

resource "aws_security_group" "rds" {
  name        = "memoir-rds-sg-${var.environment}"
  description = "Security group for RDS"
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"] # VPC 내부에서만 접근
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "memoir-rds-sg-${var.environment}"
  }
}

resource "aws_db_instance" "main" {
  identifier = "memoir-db-${var.environment}"

  # Engine
  engine         = "postgres"
  engine_version = "15.5"

  # Instance class (가장 작은 인스턴스)
  # db.t4g.micro: 2 vCPU, 1GB RAM - 300명에 충분
  instance_class = var.environment == "prod" ? "db.t4g.small" : "db.t4g.micro"

  # Storage
  allocated_storage     = 20  # GB (최소값)
  max_allocated_storage = 100 # Auto-scaling 상한
  storage_type          = "gp3"
  storage_encrypted     = true

  # Database
  db_name  = var.db_name
  username = var.db_username
  password = var.db_password
  port     = 5432

  # Network
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false

  # Backup
  backup_retention_period = var.environment == "prod" ? 7 : 1
  backup_window           = "03:00-04:00" # KST 12:00-13:00
  maintenance_window      = "mon:04:00-mon:05:00"

  # High Availability (prod only)
  multi_az = var.environment == "prod" ? true : false

  # Performance
  performance_insights_enabled = false # 비용 절감

  # Deletion protection
  deletion_protection      = var.environment == "prod" ? true : false
  skip_final_snapshot      = var.environment != "prod"
  final_snapshot_identifier = var.environment == "prod" ? "memoir-db-final-${var.environment}" : null

  tags = {
    Name = "memoir-db-${var.environment}"
  }
}
