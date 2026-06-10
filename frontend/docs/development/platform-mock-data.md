# Platform Mock Data Backlog

## Current Hardcoded Data

- `platformCategories`: 공개 홈 카테고리 탭.
- `platformDebates`: 추천/인기 토론 카드 그리드.
- `characterSpotlights`: 인기 캐릭터 섹션.
- `recentPublicSessions`: 최근 공개 세션 목록.

## Required Future APIs

- `GET /api/public/debate-sessions?sort=featured`: 추천 토론 목록.
- `GET /api/public/debate-sessions?sort=popular`: 인기 토론 목록.
- `GET /api/public/debate-sessions?sort=recent`: 최근 공개 토론 목록.
- `GET /api/public/characters?sort=popular`: 공개 인기 캐릭터 목록.
- `GET /api/public/debate-sessions/{sessionId}`: 공개 세션 상세와 턴 미리보기.

## Mock Removal Conditions

- 공개 discovery API가 OpenAPI contract에 추가된다.
- BFF가 인증 없는 public read route를 명확히 프록시한다.
- 토론 카드에 필요한 stats, participant summary, category, visibility 필드가 API 응답에 포함된다.

## Frontend Migration Impact

- `src/lib/platform-mock.ts` import를 server/client fetch 결과로 교체한다.
- `PlatformDebateCard`, `PlatformCategory`, `PlatformCharacterSpotlight` 타입을 OpenAPI generated type 기반 adapter로 축소한다.
- 공개 홈은 API 실패 시 mock fallback을 쓸지, empty state를 보일지 별도 제품 결정을 필요로 한다.
