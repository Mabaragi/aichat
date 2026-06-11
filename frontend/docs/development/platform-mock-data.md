# Platform Mock Data Notes

## Dev Mock Mode

프론트엔드만 독립적으로 개발할 때는 Next BFF가 Spring API 대신 in-memory mock
응답을 반환하게 할 수 있다.

```powershell
npm run dev
```

- `npm run dev`는 `FRONTEND_API_MODE=mock`을 기본으로 주입한다.
- 실제 Spring API로 붙이고 싶으면 `npm run dev:backend`를 사용한다.
- `mock` mode는 `NODE_ENV=production`에서 허용하지 않는다.
- `BACKEND_BASE_URL`은 backend mode에서만 필수다.
- 데이터는 dev server 메모리에만 보관되며 dev server를 재시작하면 seed로 리셋된다.

샘플 로그인 계정:

```text
email: demo@example.com
password: password123
```

Mock mode에서 지원하는 흐름:

- 공개 카테고리, 공개 토론, 공개 캐릭터 목록 조회
- 공개 토론/캐릭터 단건 조회와 공개 토론 턴 조회
- 로그인, 회원가입, refresh, logout
- `/api/workspace` 조회
- 캐릭터 생성
- 토론 세션 생성, 시작, 턴 생성, 완료

샘플 데이터는 `src/server/mock-data/fixtures.ts`에 있고 generated OpenAPI type을
사용한다. `docs/api/openapi.json` contract가 바뀌면 mock fixture/store도 같은 변경
범위에서 갱신해야 한다.

초기 seed:

- `DEBATE`: `food`, `culture`, `tech`, `life`, `society`, `fun`, `other`
- `CHARACTER`: `expert`, `critic`, `creator`, `storyteller`, `comedy`,
  `utility`, `other`
- 공개 토론 3개
- 공개 캐릭터 4개
- 데모 사용자의 비공개 캐릭터 2개

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
