# ==========================================
# 1. TERRAFORM CONFIGURATION
# ==========================================

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

# ==========================================
# 2. AWS PROVIDER CONFIGURATION
# ==========================================

provider "aws" {
  region = "us-east-1"
}

# ==========================================
# 3. VERIFY AWS CONNECTION & DEFAULT VPC
# ==========================================

data "aws_caller_identity" "current" {}

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
  filter {
    name   = "availability-zone"
    values = ["us-east-1a", "us-east-1b", "us-east-1c"]
  }
}

locals {
  lab_role_arn = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/LabRole"
}

# ==========================================
# 4. AWS EKS CLUSTER & NODE GROUP
# ==========================================

resource "aws_eks_cluster" "student_eks" {
  name     = "student-management-eks"
  role_arn = local.lab_role_arn

  vpc_config {
    subnet_ids = data.aws_subnets.default.ids
  }
}

resource "aws_eks_node_group" "student_nodes" {
  cluster_name    = aws_eks_cluster.student_eks.name
  node_group_name = "student-nodes"
  node_role_arn   = local.lab_role_arn
  subnet_ids      = data.aws_subnets.default.ids

  scaling_config {
    desired_size = 2
    max_size     = 2
    min_size     = 1
  }

  instance_types = ["t3.medium"]
}

# ==========================================
# 5. OUTPUTS
# ==========================================

output "aws_account_id" {
  value = data.aws_caller_identity.current.account_id
}

output "aws_user_arn" {
  value = data.aws_caller_identity.current.arn
}

output "cluster_name" {
  value = aws_eks_cluster.student_eks.name
}

output "cluster_endpoint" {
  value = aws_eks_cluster.student_eks.endpoint
}