# Agents Guide

## Project

이 저장소는 Spring Boot 기반 AI 토론 플랫폼 MVP다. 기준 명세는 `ai_debate_platform_mvp_spec.md`이며, 구현 코드는 `src/main/java/com/example/aichat` 아래 도메인별 패키지로 나뉜다.

## Source Of Truth

`ai_debate_platform_mvp_spec.md`를 제품/도메인 동작의 진실의 근원으로 본다. 구현이 스펙과 달라져야 한다면 코드만 바꾸지 말고 같은 변경 범위 안에서 스펙 문서도 함께 갱신한다.

## Context Vault

에이전트용 장기 컨텍스트는 `docs/agent-vault`에 둔다. 먼저 `docs/agent-vault/INDEX.md`를 읽고, 현재 작업에 필요한 문서만 추가로 연다.

사람 개발자가 읽는 개발 흐름과 학습 기록은 각각 `docs/development`와 `docs/learnings`에 둔다. 이 문서는 필요할 때만 참고하고 항상 읽는 컨텍스트로 취급하지 않는다.

## Suggested Reading Order

1. `AGENTS.md`
2. `docs/agent-vault/INDEX.md`
3. 작업과 관련된 vault guide 또는 workflow
4. 대상 코드와 관련 테스트
5. 관련 decision note

## Always Follow

- 코드 식별자, package name, API path, command, file path는 원문 형태를 유지한다.
- 루트 문서에는 항상 따라야 하는 규칙만 둔다. 도메인별 세부 지식은 `docs/agent-vault/guides`에 둔다.
- 사람이 읽을 디버깅 기록, 학습 정리, 비교/트레이드오프 설명은 `docs/learnings`에 둔다.
- 구현 전에는 기존 계층 구조를 확인한다. 이 프로젝트는 `domain`, `application`, `infrastructure`, `web` 경계를 기본으로 한다.
- 비즈니스 규칙은 가능한 `domain` 또는 `application`에 두고, `web` 계층은 HTTP 요청/응답과 DTO 변환을 중심으로 유지한다.
- 백엔드 코드 변경을 시작하기 전에는 반드시 `docs/development/backend-tdd-guideline.md`를 먼저 읽는다. 백엔드 기능, 버그 수정, 도메인 규칙 변경은 가능한 TDD로 진행한다.
- 여러 에이전트가 동시에 작업할 수 있으므로 변경 범위를 가능한 좁게 유지한다.
- 변경 범위가 여러 도메인, 공통 계층, 설정, 빌드 파일, 대량 리네임/이동으로 커질 경우 구현 전에 범위와 접근 방식을 사용자에게 확인받는다.
- 구현 중 `ai_debate_platform_mvp_spec.md`와 충돌하는 요구나 코드 변경을 발견하면 스펙을 우선 확인하고, 의도적으로 달라지는 경우 스펙을 함께 수정한다.
- 변경 범위와 무관한 사용자 변경을 되돌리지 않는다.

## Verification

코드를 변경한 경우 기본 검증은 다음 명령이다.

```powershell
.\mvnw.cmd test
```

문서만 변경한 경우 테스트 실행은 생략할 수 있다. 대신 새 문서가 `docs/agent-vault/INDEX.md`, `docs/README.md`, 또는 관련 하위 index에서 연결되는지 확인한다.

## Completion Checklist

상당한 코드, 문서, 설정, 테스트, 빌드 산출물 변경을 마친 뒤 final response 전에 확인한다.

1. `docs/agent-vault/workflows/verification.md`에 따라 검증을 실행하거나 생략 사유를 적는다.
2. `docs/agent-vault/workflows/worklog.md`에 따라 worklog를 남긴다.
3. 반복 수정 지시가 나온 경우 가장 작은 durable document를 갱신한다.
4. final response에 변경 요약, 검증 결과, worklog 위치를 포함한다.

## Closing The Loop

같은 지적이나 수정 지시가 두 번 이상 반복되면 채팅에만 남기지 않는다. 다음 작업자가 같은 실수를 피할 수 있도록 root guide, vault guide, workflow, decision note, 또는 재사용 skill 중 가장 좁은 문서를 갱신한다.

## Git Safety

- `git status --short --branch`로 현재 브랜치와 변경 상태를 확인하고 작업한다.
- 사용자가 명시적으로 요청하지 않는 한 `git reset --hard`, `git checkout --`, 강제 push 같은 파괴적 작업을 하지 않는다.
- 커밋이나 PR을 요청받은 경우 변경 파일을 먼저 설명하고, 의도한 파일만 stage한다.
