# Backend Agents Guide

## Project

이 디렉터리는 Spring Boot 기반 AI 토론 플랫폼 backend다. 제품 기준 명세는 저장소 루트 `../ai_debate_platform_mvp_spec.md`이며, 구현 코드는 `src/main/java/com/example/aichat` 아래 도메인별 package로 나뉜다.

## Context Vault

Backend 장기 컨텍스트는 `docs/agent-vault`에 둔다. 먼저 `docs/agent-vault/INDEX.md`를 읽고 현재 작업에 필요한 문서만 추가로 연다.

사람 개발자가 읽는 개발 흐름과 학습 기록은 각각 `docs/development`와 `docs/learnings`에 둔다.

## Suggested Reading Order

1. 저장소 루트 `../AGENTS.md`
2. `AGENTS.md`
3. `docs/agent-vault/INDEX.md`
4. 작업 관련 guide 또는 workflow
5. 대상 코드와 관련 테스트
6. 관련 decision note

## Always Follow

- 구현 전 기존 `domain`, `application`, `infrastructure`, `web` 경계를 확인한다.
- 비즈니스 규칙은 가능한 `domain` 또는 `application`에 두고 `web`은 HTTP DTO 변환에 집중한다.
- backend 코드 변경 전 `docs/development/backend-tdd-guideline.md`를 읽고 기능, 버그 수정, 도메인 규칙 변경은 가능한 TDD로 진행한다.
- Backend 세부 지식은 `docs/agent-vault/guides`, 사람용 구현 흐름은 `docs/development`, 학습 기록은 `docs/learnings`에 둔다.
- 구현이 `../ai_debate_platform_mvp_spec.md`와 달라지면 같은 변경 범위에서 명세도 갱신한다.
- 변경 범위와 무관한 사용자 변경을 되돌리지 않는다.

## Verification

기본 검증:

```powershell
.\mvnw.cmd test
```

Docker build는 저장소 루트에서 실행한다.

```powershell
docker build -t aichat:local backend
```

HTTP API contract를 변경했다면 저장소 루트의 `docs/api/openapi.json`도 같은 변경에서 갱신한다.

```powershell
.\mvnw.cmd -Dtest=OpenApiContractExportTest "-Dopenapi.output=../docs/api/openapi.json" test
```

## Completion Checklist

상당한 backend 변경을 마친 뒤:

1. `docs/agent-vault/workflows/verification.md`에 따라 검증한다.
2. `docs/agent-vault/workflows/worklog.md`에 따라 backend worklog를 남긴다.
3. 반복 수정 지시는 가장 작은 durable document에 반영한다.
4. final response에 변경 요약, 검증 결과, worklog 위치를 포함한다.
