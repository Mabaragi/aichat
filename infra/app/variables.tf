variable "aws_region" {
  type    = string
  default = "ap-northeast-2"
}

variable "github_repository" {
  type    = string
  default = "Mabaragi/aichat"
}

variable "repository_name" {
  type    = string
  default = "aichat"
}

variable "instance_name" {
  type    = string
  default = "aichat-app"
}

variable "ec2_role_name" {
  type    = string
  default = "aichat-ec2"
}

variable "instance_profile_name" {
  type    = string
  default = "aichat-ec2"
}

variable "instance_type" {
  type    = string
  default = "t3.small"
}

variable "app_port" {
  type    = number
  default = 80
}

variable "root_volume_size_gb" {
  type    = number
  default = 30
}

variable "data_volume_size_gb" {
  type    = number
  default = 10
}

variable "host_user" {
  type    = string
  default = "ubuntu"
}

variable "mount_path" {
  type    = string
  default = "/home/ubuntu/aichat/data"
}

variable "jwt_secret_parameter_name" {
  type        = string
  description = "SSM SecureString parameter name containing the production JWT secret"
  default     = "/aichat/prod/jwt-secret"

  validation {
    condition     = startswith(var.jwt_secret_parameter_name, "/")
    error_message = "jwt_secret_parameter_name must be an absolute SSM parameter name starting with /."
  }
}

variable "generation_provider" {
  type        = string
  description = "Active generation provider used by the backend container"
  default     = "openai"

  validation {
    condition     = contains(["mock", "openai", "gemini"], lower(var.generation_provider))
    error_message = "generation_provider must be mock, openai, or gemini."
  }
}

variable "openai_api_key_parameter_name" {
  type        = string
  description = "SSM SecureString parameter name containing the OpenAI API key"
  default     = "/aichat/prod/openai-api-key"

  validation {
    condition     = startswith(var.openai_api_key_parameter_name, "/")
    error_message = "openai_api_key_parameter_name must be an absolute SSM parameter name starting with /."
  }
}

variable "gemini_api_key_parameter_name" {
  type        = string
  description = "SSM SecureString parameter name containing the Gemini API key"
  default     = "/aichat/prod/gemini-api-key"

  validation {
    condition     = startswith(var.gemini_api_key_parameter_name, "/")
    error_message = "gemini_api_key_parameter_name must be an absolute SSM parameter name starting with /."
  }
}
