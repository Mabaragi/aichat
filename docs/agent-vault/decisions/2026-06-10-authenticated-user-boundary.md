# Authenticated User Boundary

Date: 2026-06-10

## Context

현재 일부 API는 HTTP request body 또는 query parameter의 `ownerId`를 application command로 전달한다. JWT 인증이 적용된 API에서 이 값을 계속 신뢰하면 클라이언트가 다른 사용자 ID를 제출해 소유권 검사를 우회할 수 있다.

반대로 application use case가 `SecurityContextHolder`를 직접 읽으면 application 계층이 Spring Security와 HTTP 실행 환경에 결합된다.

## Decision

- 인증이 필요한 API의 사용자 ID는 클라이언트 입력이 아니라 검증된 access token의 `sub` claim에서 얻는다.
- web 계층은 Spring Security가 제공하는 인증 principal에서 `authenticatedUserId`를 추출한다.
- request DTO에는 클라이언트가 결정할 수 있는 `ownerId`를 두지 않는다.
- application use case 경계에는 인증 사용자 ID를 명시적으로 전달한다.

```java
DebateSessionView execute(
        Long authenticatedUserId,
        CreateDebateSessionCommand command
)
```

- `CreateDebateSessionCommand`는 topic, format, participants 같은 사용자의 생성 의도만 가진다.
- use case는 `authenticatedUserId`로 사용자 존재 여부, 캐릭터 소유권, `PUBLIC` 접근 권한을 검사하고 `DebateSession.ownerId`를 설정한다.
- domain과 application 계층에서는 `Jwt`, `Authentication`, `SecurityContextHolder`, `@AuthenticationPrincipal`을 참조하지 않는다.
- 인증이 없는 CLI 또는 내부 호출은 동일 use case에 신뢰할 수 있는 호출자 ID를 명시적으로 전달하거나 별도 adapter에서 인증 사용자 컨텍스트로 변환한다.

## Consequences

- 요청 JSON의 사용자 ID 위조로 다른 사용자의 리소스를 생성하거나 변경할 수 없다.
- application 로직은 HTTP와 Spring Security 없이 단위 테스트할 수 있다.
- controller 테스트에서는 인증 principal과 request body를 서로 독립적으로 구성해야 한다.
- 현재 `ownerId`를 받는 protected API는 JWT 연결 작업에서 DTO, controller mapping, API 예시를 함께 변경해야 한다.
- 응답의 `ownerId`는 리소스 소유자를 보여 주는 서버 데이터이므로 유지할 수 있다.

## Links

- `docs/learnings/topics/spring-security-authenticated-user.md`
- `src/main/java/com/example/aichat/auth/infrastructure/SecurityConfig.java`
- `src/main/java/com/example/aichat/auth/application/JwtTokenService.java`
- `src/main/java/com/example/aichat/debate/web/DebateSessionController.java`
- `src/main/java/com/example/aichat/debate/application/CreateDebateSessionUseCase.java`
