# GitHub Actions + Terraform 배포 가이드

이 문서는 `aichat`를 `EC2 + SSM + ECR` 조합으로 배포하는 최소 운영 절차를 정리한다.

## 구성

- `infra/bootstrap`: S3 state bucket, GitHub OIDC provider, `AWS` role 2개를 만든다.
- `infra/app`: ECR repository, EC2 instance, security group, instance profile, EBS data volume을 관리한다.
- `.github/workflows/ci.yml`: `mvn test`, `docker build`, `terraform fmt`, `terraform validate`를 실행한다.
- `.github/workflows/release.yml`: `infra/app apply` 후 SHA 태그 이미지를 ECR에 push하고, SSM으로 EC2에 배포한다. S3 backend는 `use_lockfile = true`만 쓴다.

## GitHub Repository Variables

다음 repository variables를 설정한다.

- `AWS_REGION`: 기본값은 `ap-northeast-2`로 둔다.
- `AWS_TERRAFORM_ROLE_ARN`: `infra/bootstrap`에서 만든 Terraform용 GitHub role ARN.
- `AWS_DEPLOY_ROLE_ARN`: `infra/bootstrap`에서 만든 배포용 GitHub role ARN.

## One-time Bootstrap

AWS 관리자 권한이 있는 로컬 환경에서 한 번만 실행한다.

```bash
cd infra/bootstrap
terraform init
terraform apply
```

이미 `token.actions.githubusercontent.com` OIDC provider가 계정에 있으면 import 하거나, `github_oidc_provider_arn` 변수로 기존 ARN을 지정한다.

bootstrap output에서 role ARN을 확인한 뒤, 위 repository variables에 넣는다.
S3 backend의 `use_lockfile = true`를 사용한다.

## Release Flow

- CI가 `main` 브랜치에서 성공하면 `release.yml`이 실행된다.
- 수동 `workflow_dispatch`도 지원하지만, GitHub OIDC trust가 `refs/heads/main`으로 제한되어 있으므로 `main` 기준 SHA를 배포해야 한다.
- workflow는 `infra/app`을 먼저 apply해서 최신 인프라 상태를 맞춘다.
- 그 다음 SHA 태그를 사용해 Docker image를 빌드하고 ECR에 push한다.
- 마지막으로 SSM `AWS-RunShellScript`로 EC2에 이미지를 pull하고 컨테이너를 재시작한다.
- 배포 후에는 `docker ps`, SSM stdout/stderr, 그리고 EC2 instance public IP로의 HTTP 응답을 확인한다. EIP는 사용하지 않는다.
