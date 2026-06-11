# AI Debate Frontend

Next.js 16 App Router, React 19, TypeScript, Tailwind CSS 4로 구성한 최소 frontend다.
Route Handler가 Spring API의 BFF로 동작하며 JWT는 HttpOnly cookie에 저장한다.

## Local Development

```powershell
Copy-Item .env.example .env.local
npm ci
npm run generate:api
npm run dev
```

`npm run dev`는 프론트 단독 개발을 위해 BFF mock mode로 실행한다. Spring backend와
연결하려면 backend를 `http://localhost:8080`에서 띄운 뒤 `npm run dev:backend`를
사용한다. frontend는 `http://localhost:3000`에서 실행한다.

## Checks

```powershell
npm run lint
npm run typecheck
npm run test
npm run build
```

API contract 변경 시 저장소 루트 `docs/api/openapi.json`을 먼저 갱신한 뒤
`npm run generate:api`를 실행한다.
