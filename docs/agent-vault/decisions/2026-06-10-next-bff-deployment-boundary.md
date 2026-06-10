# Next.js BFF And Deployment Boundary

Date: 2026-06-10

## Decision

- 브라우저는 Spring API를 직접 호출하지 않고 Next.js Route Handler만 호출한다.
- Next BFF는 Spring이 발급한 access/refresh JWT를 `aichat_access`,
  `aichat_refresh` HttpOnly cookie로 저장한다.
- cookie는 `SameSite=Lax`, `Path=/`를 사용한다. `Secure`는 필수 runtime 설정
  `AUTH_COOKIE_SECURE`로 결정한다.
- mutation Route Handler는 request `Origin`이 Next 요청 origin과 같은지 검증한다.
- Spring 오류 status와 `ErrorResponse`는 유지하고 연결 실패만
  `502 UPSTREAM_UNAVAILABLE`로 정규화한다.
- EC2에서는 Spring과 Next를 같은 Docker network에 둔다. Spring은 host
  `127.0.0.1:8080`에만 bind하고 Next만 public port 80을 사용한다.
- `BACKEND_BASE_URL=http://aichat:8080`은 server-only 환경변수이며 브라우저 bundle에
  노출하지 않는다.

## Current HTTP Risk

현재 공인 배포는 HTTP이므로 `AUTH_COOKIE_SECURE=false`다. HttpOnly는 JavaScript token
탈취를 줄이지만 네트워크 구간 암호화를 제공하지 않는다. 운영 사용자 데이터를 다루기
전에 domain과 TLS termination을 추가해야 한다.

HTTPS 전환 시 DNS와 인증서를 구성하고 public 443으로 전환한 뒤
`AUTH_COOKIE_SECURE=true`로 배포한다. 이후 HTTP를 HTTPS로 redirect하고 HSTS 적용 여부를
검토한다.

## Consequences

- 브라우저 인증 응답 JSON에는 JWT가 포함되지 않는다.
- frontend는 운영 Spring Swagger에 의존하지 않고 커밋된 `docs/api/openapi.json`을 쓴다.
- public port 8080은 닫히며 Swagger는 로컬 실행 또는 SSM 터널을 통해 확인한다.
- refresh 요청은 브라우저에서 single-flight로 합쳐지고 원 요청은 한 번만 재시도한다.
