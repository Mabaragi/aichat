# Frontend Development

## Architecture

- App Router page는 공개 플랫폼 홈을 렌더링하고, 인증 사용자 정보는 `/api/workspace` BFF에서 선택적으로 가져온다.
- 브라우저 코드는 Spring API를 직접 호출하지 않는다. mutation은 Route Handler를 거치고, JWT는 HttpOnly cookie로만 관리한다.
- `/api/workspace`가 401을 반환해도 홈은 실패 화면으로 전환하지 않는다. 비로그인 사용자는 mock catalog를 먼저 탐색한다.
- API payload type은 저장소 루트 `docs/api/openapi.json`에서 생성한 `src/api/generated/backend-schema.d.ts`를 사용한다.

## Visual System

- 현재 홈은 Machugi형 탐색 구조를 AI 토론 서비스에 맞게 재해석한 플랫폼 UI다.
- 색상은 parchment 기반 표면, black ink, orange accent, mint/sky/rose 보조 accent를 사용한다.
- beige-only palette 제한은 폐기했다. 새 색상은 `src/app/globals.css`의 CSS variable에서 관리한다.
- 카드는 단순 장식이 아니라 토론 선택 affordance일 때만 사용한다.

## Mock Catalog Boundary

- 공개 카탈로그 데이터는 `src/lib/platform-mock.ts`에 하드코딩한다.
- 이 mock은 아직 없는 API를 BFF route로 가짜 구현하지 않기 위한 임시 데이터다.
- 향후 API 전환 계획은 [Platform mock data backlog](./platform-mock-data.md)를 따른다.
