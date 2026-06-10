# Debate Character Snapshot Boundary

Date: 2026-06-10

## Context

토론 참가자가 `ParticipantModel`만 가지면 사용자가 만든 `Character`가 실제 토론에 사용되지 않는다. 반대로 `debate.domain`이 `character.domain.Character`를 직접 참조하면 aggregate와 도메인 경계가 강하게 결합되고, 원본 캐릭터 수정이 기존 토론의 의미를 바꿀 수 있다.

## Decision

- `CreateDebateSessionUseCase`가 `UserRepository`, `CharacterRepository`, `DebateSessionRepository`를 조율한다.
- 참가자는 세션 생성 시 `Character`의 `id`, `name`, `description`, `personality`, `speechStyle`을 `DebateParticipant`로 복사한다.
- `sourceCharacterId`는 원본 추적용 값이며 `debate.domain`은 `Character` 타입을 import하지 않는다.
- 세션 소유자가 소유한 캐릭터 또는 `PUBLIC` 캐릭터만 선택할 수 있다.
- 같은 캐릭터를 두 위치에 선택하는 것은 허용하며 `position`과 `ParticipantModel`로 구분한다.
- `DebateSession`이 participant persistence를 소유하고 별도 `DebateParticipantRepository`는 두지 않는다.

## Consequences

- 원본 캐릭터 변경이나 삭제가 기존 토론 참가자 설정에 영향을 주지 않는다.
- 토론 프롬프트는 저장된 participant snapshot을 사용한다.
- 참가자 배열 순서는 `position` 0, 1로 영속화한다.
- character 접근 정책 변경은 세션 생성 application 계층에서 처리한다.
- SQLite MVP의 session ID는 단일 애플리케이션 인스턴스에서 persistence adapter가 원자적으로 할당한다. 다중 인스턴스 또는 다른 DB로 전환할 때는 DB sequence나 migration 기반 식별자 전략으로 교체한다.

## Links

- `ai_debate_platform_mvp_spec.md`
- `src/main/java/com/example/aichat/debate/application/CreateDebateSessionUseCase.java`
- `src/main/java/com/example/aichat/debate/domain/DebateParticipant.java`
