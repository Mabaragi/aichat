# Frontend Development

## Architecture

- App Router page와 client workspace가 사용자 화면을 구성한다.
- Route Handler는 backend URL, bearer token, cookie rotation을 캡슐화한다.
- 브라우저 fetch wrapper는 401에서 refresh를 single-flight로 한 번 수행한 뒤 원 요청을
  한 번만 재시도한다.
- API payload type은 `docs/api/openapi.json`에서 생성한다.

## Visual System

색상은 `#F3EBDD`, `#E5D6C3`, `#CDB99F`, `#7A6651`, `#4A3A2A`, `#FFFFFF`만 사용한다.
상태와 오류는 새 색상을 추가하지 않고 선, 명도, 굵기, 문구로 구분한다.
