# Frontend Agents Guide

## Project

이 디렉터리는 Next.js App Router 기반 frontend와 Spring API를 중계하는 BFF를 소유한다.
공개 API contract는 저장소 루트 `../docs/api/openapi.json`이며 generated type은
`src/api/generated/backend-schema.d.ts`에 커밋한다.

## Boundaries

- 브라우저 코드는 Spring API에 직접 요청하지 않는다.
- JWT는 Route Handler가 HttpOnly cookie로만 관리하고 client component에 전달하지 않는다.
- `BACKEND_BASE_URL`은 server-only 환경변수다. `NEXT_PUBLIC_` 접두사를 사용하지 않는다.
- mutation Route Handler는 same-origin `Origin`을 검증한다.
- Visual system 색상은 `src/app/globals.css`의 CSS variable을 source of truth로 둔다.
- OpenAPI에 없는 API 동작을 UI에서 추측해 구현하지 않는다.

## Verification

```powershell
npm ci
npm run generate:api
npm run lint
npm run typecheck
npm run test
npm run build
docker build -t aichat-frontend:local frontend
```

OpenAPI generated type을 갱신한 뒤 의도하지 않은 drift가 없는지 확인한다.

## Completion

상당한 변경 후 `docs/agent-vault/workflows/verification.md`를 따르고
`docs/agent-vault/worklogs/2026-06.md`에 기록한다.
