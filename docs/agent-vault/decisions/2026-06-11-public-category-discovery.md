# Public Category Discovery Boundary

Date: 2026-06-11

## Decision

- 공개 탐색의 카테고리는 enum이 아니라 `categories` DB 테이블로 관리한다.
- 카테고리는 `scope`로 `DEBATE`와 `CHARACTER`를 구분하고, 외부 API는 stable `slug`를 받는다.
- MVP에서는 관리자 CRUD를 만들지 않고 Flyway seed로 초기 카테고리를 관리한다.
- `Character`와 `DebateSession`은 `categoryId`를 저장한다.
- `DebateSession.topicCategory`는 호환과 표시를 위한 slug snapshot으로 유지한다.
- 공개 토론 노출 조건은 `visibility=PUBLIC AND status=COMPLETED`다.
- 완료 여부 전용 boolean은 두지 않고 기존 `status=COMPLETED`를 source of truth로 사용한다.
- 공개 목록 응답은 `items`, `page`, `size`, `totalElements`, `totalPages`, `hasNext` shape로 통일한다.

## Rationale

공개 탐색은 제품의 핵심 화면이므로 카테고리는 코드 enum보다 데이터로 두는 편이 낫다.
이름, 정렬, 활성화 여부를 migration으로 조정할 수 있고, 토론과 캐릭터가 서로 다른
카테고리 집합을 가져도 API shape를 유지할 수 있다.

완료 여부를 boolean으로 복제하면 `status`와 불일치할 수 있다. 현재 lifecycle은
`CREATED -> RUNNING -> COMPLETED`를 이미 상태로 표현하므로 공개 노출 조건에서
`COMPLETED`를 직접 사용하는 것이 더 단순하다.

## Consequences

- API 사용자는 카테고리 slug를 보내고 category summary를 받는다.
- 없는 카테고리나 inactive 카테고리는 `CATEGORY_NOT_FOUND`로 거부한다.
- seed 변경은 Flyway migration으로 처리한다.
- frontend는 category tab을 하드코딩하지 않고 `/api/public/categories`에서 읽는다.
- public API와 OpenAPI 계약이 frontend codegen의 source of truth가 된다.
