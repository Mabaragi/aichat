# Spring Security에서 인증 사용자 ID를 전달하는 원리

Date: 2026-06-10

## 핵심 구분

HTTP request body와 인증 사용자는 출처와 신뢰 수준이 다르다.

- `@RequestBody CreateDebateSessionRequest request`: 클라이언트가 보낸 JSON이다. 주제, 형식, 참가자처럼 사용자가 입력할 수 있는 값을 담는다.
- `@AuthenticationPrincipal Jwt jwt`: Spring Security가 서명과 만료 시간을 검증한 access token에서 만든 인증 principal이다.

따라서 protected API의 소유자 ID를 request DTO의 `ownerId`에서 읽으면 안 된다. 이 프로젝트의 access token은 `JwtTokenService`가 사용자 ID를 JWT `sub` claim에 저장하므로 `jwt.getSubject()`를 `Long`으로 변환해 사용한다.

## 요청 처리 순서

```text
Authorization: Bearer <access-token>
        |
        v
Spring Security filter chain
        |
        |  JwtDecoder가 서명, 만료 시간 등을 검증
        v
JwtAuthenticationToken 생성
        |
        |  SecurityContext에 Authentication 저장
        v
DispatcherServlet
        |
        +-- @RequestBody -> HttpMessageConverter가 JSON DTO 변환
        |
        +-- @AuthenticationPrincipal -> Authentication principal 주입
        v
Controller
        |
        |  Jwt.sub -> authenticatedUserId
        v
Application use case
```

`@RequestBody`와 `@AuthenticationPrincipal`은 같은 HTTP 요청에서 만들어지지만 처리 경로가 다르다.

1. Spring Security filter chain이 MVC controller보다 먼저 실행된다.
2. OAuth2 Resource Server의 JWT 인증 필터가 `Authorization` header의 bearer token을 찾는다.
3. `JwtDecoder`가 토큰을 검증하고 성공하면 `JwtAuthenticationToken`을 만든다.
4. 인증 결과가 `SecurityContext`에 저장된다.
5. 이후 `DispatcherServlet`이 controller method argument를 해석한다.
6. `@RequestBody`는 `HttpMessageConverter`가 body JSON을 DTO로 역직렬화한다.
7. `@AuthenticationPrincipal`은 현재 `Authentication`의 principal을 controller parameter에 주입한다.

body에 `"ownerId": 999`가 있더라도 JWT의 `sub`와 자동으로 비교되거나 덮어써지지 않는다. 둘은 독립적인 입력이다. 그래서 request DTO에서 `ownerId` 자체를 제거해야 모호성과 실수를 막을 수 있다.

## 권장 Controller 형태

```java
@PostMapping
public ResponseEntity<DebateSessionResponse> create(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody CreateDebateSessionRequest request
) {
    Long authenticatedUserId = Long.valueOf(jwt.getSubject());
    DebateSessionView view = createDebateSessionUseCase.execute(
            authenticatedUserId,
            request.toCommand()
    );
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(DebateSessionResponse.from(view));
}
```

더 여러 controller에서 같은 변환이 반복되면 web 계층에 `AuthenticatedUserId` argument resolver나 작은 mapper를 둘 수 있다. 다만 use case 내부에서 `SecurityContextHolder`를 읽지는 않는다.

## 내부 시그니처에 ID가 필요한 이유

인증 방식이 JWT로 바뀌어도 application 로직에는 호출자 ID가 필요하다.

- 세션의 `ownerId` 설정
- 본인 소유 `PRIVATE` 캐릭터 허용
- 다른 사용자의 `PUBLIC` 캐릭터 허용
- 다른 사용자의 `PRIVATE` 캐릭터 거부
- 향후 세션 수정, 삭제, 조회 권한 검사

차이는 ID의 존재 여부가 아니라 출처다.

```text
기존: request.ownerId -> command.ownerId -> use case
목표: verified Jwt.sub -> authenticatedUserId -> use case
```

## 테스트 기준

- controller 테스트: bearer token 또는 `JwtAuthenticationToken`의 `sub`를 설정하고 body에는 `ownerId`를 보내지 않는다.
- application 테스트: Spring Security 없이 `authenticatedUserId`를 직접 전달한다.
- 권한 테스트: body 조작이 아니라 전달된 인증 사용자 ID를 기준으로 소유권 결과를 검증한다.
- unauthenticated 요청: protected endpoint에서 `401 Unauthorized`인지 확인한다.

## 관련 파일

- `src/main/java/com/example/aichat/auth/infrastructure/SecurityConfig.java`
- `src/main/java/com/example/aichat/auth/application/JwtTokenService.java`
- `docs/agent-vault/decisions/2026-06-10-authenticated-user-boundary.md`
