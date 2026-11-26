
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
# 3. VERIFY AWS CONNECTION
# ==========================================

data "aws_caller_identity" "current" {}

# ==========================================
# 4. DISPLAY AWS ACCOUNT INFORMATION
# ==========================================

output "aws_account_id" {
  value = data.aws_caller_identity.current.account_id
}

output "aws_user_arn" {
  value = data.aws_caller_identity.current.arn
}
