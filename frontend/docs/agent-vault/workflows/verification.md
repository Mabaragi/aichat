# Frontend Verification

1. `npm run generate:api`를 두 번 실행해 deterministic output을 확인한다.
2. `npm run lint`, `npm run typecheck`, `npm run test`, `npm run build`를 실행한다.
3. 저장소 루트에서 `docker build -t aichat-frontend:local frontend`를 실행한다.
4. BFF cookie, origin 검증, refresh retry 테스트를 확인한다.
5. UI 변경은 로컬 backend와 함께 브라우저에서 핵심 흐름을 확인한다.
