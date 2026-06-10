# Root Verification Workflow

## Backend

```powershell
cd backend
.\mvnw.cmd test
cd ..
docker build -t aichat:ci backend
```

## Frontend

```powershell
cd frontend
npm ci
npm run generate:api
npm run lint
npm run typecheck
npm run test
npm run build
cd ..
docker build -t aichat-frontend:ci frontend
```

## Infrastructure

```powershell
terraform fmt -check -recursive infra
terraform -chdir=infra/app validate
```

## Workflow And Docs

- GitHub workflow YAML을 파싱한다.
- `python scripts/verify_release_secret_flow.py`를 실행한다.
- `python scripts/verify_openapi_ci_flow.py`를 실행한다.
- `python -m unittest scripts/test_verify_openapi_contract.py`를 실행한다.
- Backend API 변경 시 생성한 OpenAPI와 `docs/api/openapi.json`의 byte 일치를 확인한다.
- Frontend generated API type과 `docs/api/openapi.json`의 drift가 없는지 확인한다.
- 이전 root-level Maven, `src`, Docker build context 참조가 남지 않았는지 검색한다.
- root와 하위 프로젝트 docs index의 링크 대상이 존재하는지 확인한다.
