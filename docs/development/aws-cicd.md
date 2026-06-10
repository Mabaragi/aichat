# GitHub Actions + Terraform 배포 가이드

이 문서는 `aichat`를 `EC2 + SSM + ECR` 조합으로 배포하는 최소 운영 절차를 정리한다.

## 구성

- `infra/bootstrap`: S3 state bucket, GitHub OIDC provider, `AWS` role 2개를 만든다.
- `infra/app`: ECR repository, EC2 instance, security group, instance profile, EBS data volume을 관리한다.
- `.github/workflows/ci.yml`: `mvn test`, `docker build`, `terraform fmt`, `terraform validate`를 실행한다.
- `.github/workflows/release.yml`: `infra/app apply` 후 SHA 태그 이미지를 ECR에 push하고, SSM으로 EC2에 배포한다. S3 backend는 `use_lockfile = true`만 쓴다.

## JWT Secret

운영 JWT 서명 키는 AWS SSM Parameter Store의 SecureString `/aichat/prod/jwt-secret`에 둔다. Terraform은 parameter 이름과 EC2 조회 권한만 관리하며 secret resource나 값은 만들지 않는다. 따라서 secret 원문은 Terraform state와 GitHub Secrets에 저장되지 않는다.

AWS 권한이 있는 운영자 PowerShell에서 32 random bytes를 Base64로 인코딩해 최초 parameter를 생성한다.

```powershell
$jwtSecret = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
aws ssm put-parameter `
  --region ap-northeast-2 `
  --name /aichat/prod/jwt-secret `
  --type SecureString `
  --value $jwtSecret
Remove-Variable jwtSecret
```

로컬에서 운영 profile로 Spring Boot를 실행할 때도 현재 shell에만 환경변수를 설정한다.

```powershell
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
.\mvnw.cmd spring-boot:run
Remove-Item Env:JWT_SECRET
```

테스트는 `application-test.yaml`의 고정 test secret을 사용하므로 운영 `JWT_SECRET` 없이 실행한다.

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
- EC2 instance role이 `/aichat/prod/jwt-secret`만 `ssm:GetParameter`로 조회한다. 원격 스크립트는 기존 컨테이너를 제거하기 전에 값을 복호화해 확인하고, 누락되거나 비어 있으면 배포를 중단한다.
- 조회한 값은 `docker run -e JWT_SECRET=...`로만 전달하며 workflow log나 SSM command payload에는 secret 원문을 기록하지 않는다.
- 배포 후에는 `docker ps`, SSM stdout/stderr, 그리고 EC2 instance public IP로의 HTTP 응답을 확인한다. EIP는 사용하지 않는다.

## JWT Secret Rotation

새 값을 생성해 같은 parameter를 덮어쓴 뒤 release workflow를 다시 실행한다.

```powershell
$jwtSecret = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
aws ssm put-parameter `
  --region ap-northeast-2 `
  --name /aichat/prod/jwt-secret `
  --type SecureString `
  --value $jwtSecret `
  --overwrite
Remove-Variable jwtSecret
```

키가 바뀌면 기존 access token과 refresh token은 모두 검증할 수 없으므로 전체 사용자가 다시 로그인해야 한다. Parameter 갱신만으로 실행 중 컨테이너의 환경변수는 바뀌지 않으므로 반드시 release workflow를 재실행한다.
