# Generation Module Boundary

Date: 2026-06-09

## Context

기존 `generation` 패키지는 `PromptBuilder`가 `DebateFormat`, `ParticipantModel`을 직접 참조하면서 토론 도메인 정책과 공용 LLM 호출 기능을 함께 소유했다. 동시에 `LlmClient`, request/result contract는 `generation.domain`에 있었지만 비즈니스 entity나 aggregate가 아니라 외부 생성 capability의 호출 계약이었다.

## Decision

- 토론 프롬프트 구성 규칙은 `debate.domain.DebateTurnPromptBuilder`가 소유한다.
- `generation`은 여러 비즈니스 도메인이 재사용할 수 있는 지원/platform 모듈로 둔다.
- 공용 생성 계약은 `generation.application`의 `TextGenerator`, `GenerationRequest`, `GenerationResult`에 둔다.
- mock, OpenAI, Gemini, local model 같은 provider 구현은 `generation.infrastructure`에 둔다.
- `generation`은 `debate`를 비롯한 특정 비즈니스 도메인 타입을 참조하지 않는다.
- `ParticipantModel`은 토론 참가자의 비즈니스 선택이므로 `debate.domain`에 유지한다.
- provider model ID, 인증, timeout, retry, request schema 매핑은 `generation.infrastructure`가 담당한다.

## Consequences

- 다른 도메인은 자체 prompt 정책을 소유하면서 같은 `TextGenerator`를 재사용할 수 있다.
- 토론 정책 변경이 provider adapter에 전파되지 않는다.
- 실제 provider 연동 시 `GenerationRequest` 확장이 필요하면 특정 도메인 타입 대신 provider-neutral 값으로 추가해야 한다.

## Links

- `ai_debate_platform_mvp_spec.md`
- `src/main/java/com/example/aichat/debate/domain/DebateTurnPromptBuilder.java`
- `src/main/java/com/example/aichat/generation/application/TextGenerator.java`
