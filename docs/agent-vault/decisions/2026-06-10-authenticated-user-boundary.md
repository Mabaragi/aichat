# Authenticated User Boundary

Date: 2026-06-10

## Context

현재 일부 API는 HTTP request body 또는 query parameter의 `ownerId`를 application command로 전달한다. JWT 인증이 적용된 API에서 이 값을 계속 신뢰하면 클라이언트가 다른 사용자 ID를 제출해 소유권 검사를 우회할 수 있다.

반대로 application use case가 `SecurityContextHolder`를 직접 읽으면 application 계층이 Spring Security와 HTTP 실행 환경에 결합된다.

## Decision

- 인증이 필요한 API의 사용자 ID는 클라이언트 입력이 아니라 검증된 access token의 `sub` claim에서 얻는다.
- web 계층은 Spring Security가 제공하는 인증 principal을 `RequestActor`로 변환한다.
- request DTO에는 클라이언트가 결정할 수 있는 `ownerId`를 두지 않는다.
- application use case 경계에는 `RequestActor`를 명시적으로 전달한다.

```java
DebateSessionView execute(CreateDebateSessionCommand command)
```

- protected web adapter는 `RequestActor.authenticated(userId)`와 token의 `sub`에서 얻은
  `ownerId`를 command에 넣는다. 클라이언트 request body의 `ownerId`는 사용하지 않는다.
- use case는 actor로 사용자 소유권과 `PUBLIC` 접근 권한을 검사한다.
- domain과 application 계층에서는 `Jwt`, `Authentication`, `SecurityContextHolder`, `@AuthenticationPrincipal`을 참조하지 않는다.
- CLI adapter는 `RequestActor.system()`을 사용한다.

## Consequences

- 요청 JSON의 사용자 ID 위조로 다른 사용자의 리소스를 생성하거나 변경할 수 없다.
- application 로직은 HTTP와 Spring Security 없이 단위 테스트할 수 있다.
- controller 테스트에서는 인증 principal과 request body를 서로 독립적으로 구성해야 한다.
- protected API의 request DTO와 API 예시에서는 `ownerId`를 제거했고, web adapter가 JWT `sub`로 command의 소유자를 설정한다.
- 응답의 `ownerId`는 리소스 소유자를 보여 주는 서버 데이터이므로 유지할 수 있다.

## Links

- `docs/learnings/topics/spring-security-authenticated-user.md`
- `src/main/java/com/example/aichat/auth/infrastructure/SecurityConfig.java`
- `src/main/java/com/example/aichat/auth/application/JwtTokenService.java`
- `src/main/java/com/example/aichat/debate/web/DebateSessionController.java`
- `src/main/java/com/example/aichat/debate/application/CreateDebateSessionUseCase.java`
