# Frontend Development

## Architecture

- App Router page는 공개 플랫폼 홈을 렌더링하고, 인증 사용자 정보는 `/api/workspace` BFF에서 선택적으로 가져온다.
- 브라우저 코드는 Spring API를 직접 호출하지 않는다. mutation은 Route Handler를 거치고, JWT는 HttpOnly cookie로만 관리한다.
- `/api/workspace`가 401을 반환해도 홈은 실패 화면으로 전환하지 않는다. 비로그인 사용자는 public BFF route가 프록시한 공개 카탈로그를 먼저 탐색한다.
- API payload type은 저장소 루트 `docs/api/openapi.json`에서 생성한 `src/api/generated/backend-schema.d.ts`를 사용한다.

## Visual System

- 현재 홈은 Machugi형 탐색 구조를 AI 토론 서비스에 맞게 재해석한 플랫폼 UI다.
- 색상은 흰색 surface와 보라 계열 기하학 배경을 중심으로 사용한다.
- 색상 token은 `src/app/globals.css`의 CSS variable에서 관리한다.
- 카드는 단순 장식이 아니라 토론 선택 affordance일 때만 사용한다.

## Public Catalog Boundary

- 공개 카탈로그 데이터는 `/api/public/*` BFF route가 Spring public API를 프록시해 가져온다.
- 카테고리 탭은 `GET /api/public/categories?scope=DEBATE|CHARACTER` 응답을 사용한다.
- 공개 토론은 backend에서 `PUBLIC + COMPLETED` 조건으로 필터링한다.
- 공개 캐릭터는 backend에서 `PUBLIC` 조건으로 필터링한다.
- 레퍼런스 원본 파일은 `frontend/references/machugi-io/raw/`에 로컬 전용으로 보관하고 Git에는 분석 README만 커밋한다.
