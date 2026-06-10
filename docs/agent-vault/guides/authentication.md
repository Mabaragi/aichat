# Authentication Guide

## Contract

- HTTP API는 Spring Security Bearer JWT를 사용한다.
- Access token은 15분, Refresh token은 14일이다.
- Access token은 `Authorization: Bearer <token>` header로 전달한다.
- Refresh token은 원문을 저장하지 않고 `refresh_tokens.token_hash`에 SHA-256 해시로 저장한다.
- Refresh token 갱신은 rotation 방식이며 사용된 토큰의 재사용을 감지하면 같은 family를 폐기한다.
- 운영 실행에는 Base64로 인코딩된 32바이트 이상의 `JWT_SECRET`이 필요하다.

## Public And Protected APIs

- 공개: `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, 캐릭터 GET.
- 보호: 그 외 API.
- 공개 캐릭터 GET도 application 계층에서 `PUBLIC` 여부를 검사한다.
- `PRIVATE` 리소스에 대한 비소유자 접근은 `404`로 존재를 숨긴다.

## Actor Boundary

- web 계층은 검증된 JWT `sub`를 `RequestActor.authenticated(userId)`로 변환한다.
- application 계층은 `RequestActor`로 객체 단위 소유권을 검사한다.
- protected request DTO에서 `ownerId`를 받지 않는다.
- CLI는 HTTP 인증을 우회하는 외부 API가 아니라 신뢰된 로컬 도구이며 기존 command adapter가 `RequestActor.system()`을 사용한다.
- domain/application 계층은 `Jwt`, `Authentication`, `SecurityContextHolder`를 참조하지 않는다.

## Verification

인증 변경 시 최소한 다음을 확인한다.

```powershell
.\mvnw.cmd -Dtest=AuthUseCaseTest,AuthSecurityIntegrationTest test
.\mvnw.cmd test
```

- 잘못된 access token과 무인증 protected 요청은 `401`.
- 타 사용자의 `PRIVATE` 리소스 조회·수정·삭제는 `404`.
- Refresh token rotation, replay family revoke, logout 멱등성.
- CLI context는 `JWT_SECRET` 없이 시작 가능.
