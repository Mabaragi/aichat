output "ecr_repository_url" {
  value = aws_ecr_repository.app.repository_url
}

output "ec2_instance_id" {
  value = aws_instance.app.id
}

output "ec2_public_ip" {
  value = aws_instance.app.public_ip
}

output "ec2_private_ip" {
  value = aws_instance.app.private_ip
}

output "data_volume_id" {
  value = aws_ebs_volume.data.id
}

output "availability_zone" {
  value = data.aws_subnet.selected.availability_zone
}

output "instance_profile_name" {
  value = aws_iam_instance_profile.ec2.name
}

output "ec2_role_arn" {
  value = aws_iam_role.ec2.arn
}

output "jwt_secret_parameter_name" {
  value = var.jwt_secret_parameter_name
}
