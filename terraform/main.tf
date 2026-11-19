data "aws_iam_role" "lab" {
  name = "LabRole"
}

data "aws_vpc" "default" {
  default = true
}

data "aws_availability_zones" "available" {
  state = "available"

  filter {
    name   = "opt-in-status"
    values = ["opt-in-not-required"]
  }
}

locals {
  eks_availability_zones = [
    for zone in data.aws_availability_zones.available.names :
    zone if zone != "us-east-1e"
  ]
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }

  filter {
    name   = "availability-zone"
    values = local.eks_availability_zones
  }
}

resource "aws_eks_cluster" "student" {
  name     = "student-eks"
  role_arn = data.aws_iam_role.lab.arn

  vpc_config {
    subnet_ids = data.aws_subnets.default.ids
  }
}

resource "aws_eks_node_group" "student" {
  cluster_name    = aws_eks_cluster.student.name
  node_group_name = "student-eks-nodes"
  node_role_arn   = data.aws_iam_role.lab.arn
  subnet_ids      = data.aws_subnets.default.ids
  instance_types  = ["t3.medium"]

  scaling_config {
    desired_size = 2
    min_size     = 1
    max_size     = 2
  }

  depends_on = [aws_eks_cluster.student]
}
