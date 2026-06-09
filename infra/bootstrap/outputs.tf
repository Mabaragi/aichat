output "state_bucket_name" {
  value = aws_s3_bucket.state.bucket
}

output "github_oidc_provider_arn" {
  value = local.oidc_provider_arn
}

output "terraform_role_arn" {
  value = aws_iam_role.terraform.arn
}

output "deploy_role_arn" {
  value = aws_iam_role.deploy.arn
}
