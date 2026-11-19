output "cluster_name" {
  description = "Nom du cluster EKS"
  value       = aws_eks_cluster.student.name
}

output "cluster_endpoint" {
  description = "Endpoint API du cluster EKS"
  value       = aws_eks_cluster.student.endpoint
}
