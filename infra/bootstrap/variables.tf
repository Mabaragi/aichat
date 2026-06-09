variable "aws_region" {
  type    = string
  default = "ap-northeast-2"
}

variable "github_repository" {
  type    = string
  default = "Mabaragi/aichat"
}

variable "github_branch" {
  type    = string
  default = "main"
}

variable "github_oidc_provider_arn" {
  type        = string
  default     = null
  nullable    = true
  description = "Existing GitHub OIDC provider ARN. Leave null to create one in bootstrap."
}

variable "state_bucket_prefix" {
  type    = string
  default = "aichat-tfstate"
}

variable "terraform_role_name" {
  type    = string
  default = "aichat-gha-terraform"
}

variable "deploy_role_name" {
  type    = string
  default = "aichat-gha-deploy"
}
