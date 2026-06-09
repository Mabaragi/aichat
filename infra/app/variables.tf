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
  default = 8080
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
