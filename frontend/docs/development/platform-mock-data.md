# Public Catalog API Notes

## Current Data Flow

- `/`는 landing page가 아니라 공개 탐색 화면이다.
- 카테고리 탭은 `/api/public/categories?scope=DEBATE|CHARACTER`에서 가져온다.
- 공개 토론 목록은 `/api/public/debate-sessions?page=0&size=20&query=&category=`를 사용한다.
- 공개 캐릭터 목록은 `/api/public/characters?page=0&size=20&query=&category=`를 사용한다.
- 브라우저는 Spring을 직접 호출하지 않고 Next BFF route만 호출한다.

## Backend Exposure Rules

- 공개 토론은 `visibility=PUBLIC AND status=COMPLETED` 조건만 반환한다.
- 공개 캐릭터는 `visibility=PUBLIC` 조건만 반환한다.
- 카테고리 필터는 slug를 사용하며, inactive 또는 없는 slug는 backend가 `CATEGORY_NOT_FOUND`로 거부한다.

## Future Expansion

- 추천/인기/최근 정렬은 아직 없다. 첫 버전은 최신순 API 결과를 그대로 보여준다.
- 공개 세션 상세 페이지와 턴 미리보기 화면은 별도 라우트로 확장할 수 있다.
- 캐릭터/토론 수정, 카테고리 관리자 CRUD는 현재 범위 밖이다.
