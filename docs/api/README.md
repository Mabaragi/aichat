# OpenAPI Contract

`openapi.json`은 backend와 frontend가 같은 commit에서 공유하는 HTTP API contract다.
운영 서버의 Swagger endpoint가 아니라 이 파일을 frontend type/client 생성의 source of
truth로 사용한다.

## Backend에서 갱신

저장소 루트에서 다음 명령을 실행한다.

```powershell
cd backend
.\mvnw.cmd -Dtest=OpenApiContractExportTest "-Dopenapi.output=../docs/api/openapi.json" test
cd ..
```

Controller, request/response DTO, validation, security annotation처럼 공개 API shape에 영향을
주는 변경은 backend 코드와 `openapi.json`을 같은 commit에 포함한다. 일반
`.\mvnw.cmd test`는 contract 파일을 쓰지 않는다.

## CI 정책

CI는 `test` profile의 Spring context에서 `/v3/api-docs`를 다시 생성한다.

1. 생성 결과와 커밋된 `openapi.json`을 byte 단위로 비교한다.
2. 생성 결과를 GitHub Actions artifact로 보관한다.
3. Pull request에서는 base branch의 contract와 `oasdiff`로 비교해 `ERR` 등급 breaking
   change를 차단한다.
4. base branch에 contract가 없는 최초 도입 PR은 breaking-change 검사만 건너뛴다.

CI는 contract를 자동 commit하지 않는다. Drift가 발생하면 위 명령으로 갱신한 뒤 backend
변경과 함께 commit한다.

## Frontend 연동

`frontend`는 같은 commit의 `docs/api/openapi.json`에서 TypeScript type을 생성한다.

```powershell
cd frontend
npm run generate:api
```

생성 결과는 `frontend/src/api/generated/backend-schema.d.ts`에 커밋한다. CI 순서는
다음과 같다.

```text
OpenAPI type/client 생성 -> 생성물 drift 검사 -> frontend test -> frontend build
```

GitHub Actions artifact는 검토와 workflow 간 전달을 위한 보조 산출물이며, source of truth는
Git에 커밋된 `openapi.json`이다.
