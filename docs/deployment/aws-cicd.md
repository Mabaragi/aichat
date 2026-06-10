# GitHub Actions + Terraform 배포 가이드

이 문서는 `aichat`를 `EC2 + SSM + ECR` 조합으로 배포하는 최소 운영 절차를 정리한다.

## 구성

- `infra/bootstrap`: S3 state bucket, GitHub OIDC provider, `AWS` role 2개를 만든다.
- `infra/app`: ECR repository, EC2 instance, security group, instance profile, EBS data volume을 관리한다.
- `backend`: Spring Boot source, Maven wrapper, Dockerfile을 소유한다.
- `frontend`: Next.js UI, BFF Route Handler, standalone Dockerfile을 소유한다.
- `.github/workflows/ci.yml`: backend test/OpenAPI drift, frontend codegen/lint/typecheck/test/build,
  두 Docker image와 Terraform을 검증한다.
- `.github/workflows/release.yml`: `infra/app apply` 후 backend `${SHA}`, frontend
  `frontend-${SHA}` tag를 같은 ECR repository에 push하고 SSM으로 EC2에 배포한다.
  S3 backend는 `use_lockfile = true`만 쓴다.

## Runtime Topology

EC2에는 `aichat-network` Docker network와 두 container가 있다.

```text
Internet :80
    |
    v
aichat-frontend :3000
    |
    | BACKEND_BASE_URL=http://aichat:8080
    v
aichat :8080 ---- /app/data -> persistent EBS
```

- `aichat-frontend`만 host `0.0.0.0:80`에 공개한다.
- `aichat`는 host `127.0.0.1:8080`에만 bind한다.
- Security Group ingress는 TCP 80만 허용한다.
- Swagger와 `/v3/api-docs`는 공인 8080에서 접근할 수 없다. 로컬 backend 실행 또는 SSM
  port forwarding을 사용한다.

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
Set-Location backend
.\mvnw.cmd spring-boot:run
Set-Location ..
Remove-Item Env:JWT_SECRET
```

테스트는 `application-test.yaml`의 고정 test secret을 사용하므로 운영 `JWT_SECRET` 없이 실행한다.

## Frontend Runtime Environment

- `BACKEND_BASE_URL=http://aichat:8080`: Next server만 읽는 backend 주소다.
- `AUTH_COOKIE_SECURE=false`: 현재 HTTP 배포에서 명시적으로 사용한다.

JWT는 `aichat_access`, `aichat_refresh` HttpOnly cookie에 저장되고 `SameSite=Lax`,
`Path=/`을 사용한다. 현재 HTTP에서는 `Secure=false`이므로 네트워크 구간 기밀성이 없다.
운영 사용 전 domain, 인증서, HTTPS termination을 구성하고 `AUTH_COOKIE_SECURE=true`로
전환해야 한다. 이때 public ingress는 443으로 변경하고 80은 HTTPS redirect만 제공한다.

## Generation API Key

backend의 generation provider는 `GENERATION_PROVIDER`로 선택한다. 기본 프로파일은 `mock`이며, 릴리즈 배포에서는 `infra/app` 출력값으로 선택된 provider를 사용한다. 현재 워크플로우는 `openai`와 `gemini`를 모두 지원하고, 활성 provider에 맞는 API key만 컨테이너 환경변수로 주입한다.

운영 API key는 AWS SSM Parameter Store의 SecureString에 둔다.

- `/aichat/prod/openai-api-key`
- `/aichat/prod/gemini-api-key`

Terraform은 EC2 instance role에 위 두 parameter의 `ssm:GetParameter` 권한을 부여한다. release workflow는 JWT secret을 검증한 뒤 활성 provider에 맞는 parameter를 조회하고, `docker run` 시 다음 env 중 하나를 전달한다.

- `OPENAI_API_KEY`
- `GEMINI_API_KEY`

model id는 application 설정에서 env로 주입한다. 기본값은 `backend/src/main/resources/application.yaml`, `backend/src/main/resources/application-dev.yaml`, `backend/src/test/resources/application-test.yaml`에 두고, 필요하면 `OPENAI_FAST_MODEL`, `OPENAI_BALANCED_MODEL`, `OPENAI_QUALITY_MODEL`, `GEMINI_FAST_MODEL`, `GEMINI_BALANCED_MODEL`, `GEMINI_QUALITY_MODEL`로 덮어쓴다.

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

- CI는 `backend/**`, `frontend/**`, `docs/api/**`, `infra/**`, `scripts/**`, 관련 workflow
  변경에 실행한다. 공통 문서만 변경한 push는 release를 만들지 않는다.
- CI가 `main` 브랜치에서 성공하면 `release.yml`이 실행된다.
- 수동 `workflow_dispatch`도 지원하지만, GitHub OIDC trust가 `refs/heads/main`으로 제한되어 있으므로 `main` 기준 SHA를 배포해야 한다.
- workflow는 `infra/app`을 먼저 apply해서 최신 인프라 상태를 맞춘다.
- backend와 frontend image를 모두 빌드해 ECR에 push한다. ECR lifecycle은 두 image 계열을
  합쳐 최근 40개를 보관한다.
- SSM 스크립트는 secret과 활성 provider API key를 먼저 검증하고 두 image를 모두 pull한
  뒤 기존 container를 교체한다.
- backend readiness, frontend readiness, 공인 port 80 응답을 확인한다. 별도 release
  검증에서 공인 port 8080이 닫혀 있는지도 확인한다.
- EC2 instance role이 `/aichat/prod/jwt-secret`, `/aichat/prod/openai-api-key`, `/aichat/prod/gemini-api-key`를 `ssm:GetParameter`로 조회한다. 원격 스크립트는 기존 컨테이너를 제거하기 전에 값을 복호화해 확인하고, 누락되거나 비어 있으면 배포를 중단한다.
- 조회한 값은 `docker run -e JWT_SECRET=...`와 `docker run -e OPENAI_API_KEY=...` 또는 `docker run -e GEMINI_API_KEY=...`로만 전달하며 workflow log나 SSM command payload에는 secret 원문을 기록하지 않는다.
- 배포 후에는 `docker ps`, SSM stdout/stderr, `http://<public-ip>/` 응답을 확인한다.
  EIP는 사용하지 않는다.

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
