# Agents Guide

## Repository

이 저장소는 AI 토론 플랫폼의 모노레포다. 제품 동작의 기준 명세는 `ai_debate_platform_mvp_spec.md`이며, `backend/`의 Spring Boot API와 `frontend/`의 Next.js BFF 애플리케이션을 함께 배포한다.

## Project Routing

- 저장소 전체 구조, 공통 인프라, GitHub Actions, 제품 명세 작업은 이 문서와 `docs/agent-vault/INDEX.md`를 따른다.
- Spring Boot 코드, Maven, backend 문서 작업은 먼저 `backend/AGENTS.md`를 읽고 그 규칙을 따른다.
- Next.js, React UI, BFF Route Handler 작업은 먼저 `frontend/AGENTS.md`를 읽고 그 규칙을 따른다.
- `infra/`와 `.github/workflows/`는 루트 소유다. 특정 애플리케이션 경로를 참조하더라도 배포 전체 흐름은 루트 문서에서 관리한다.

## Source Of Truth

- 제품/도메인 동작: `ai_debate_platform_mvp_spec.md`
- 모노레포 구조와 공통 workflow: `docs/agent-vault`
- Backend 구현 세부사항: `backend/docs/agent-vault`
- Frontend 구현 세부사항: `frontend/docs/agent-vault`

구현이 제품 명세와 달라져야 한다면 코드만 바꾸지 말고 같은 변경 범위 안에서 명세도 함께 갱신한다.

## Always Follow

- 작업 대상 디렉터리에서 가장 가까운 `AGENTS.md`를 함께 적용한다.
- 코드 식별자, package name, API path, command, file path는 원문 형태를 유지한다.
- 여러 프로젝트에 영향을 주는 규칙만 루트 문서에 둔다. 프로젝트별 지식은 해당 하위 프로젝트 문서에 둔다.
- 여러 에이전트가 동시에 작업할 수 있으므로 변경 범위를 가능한 좁게 유지하고 무관한 변경을 되돌리지 않는다.
- 공통 인프라, workflow, 대량 이동처럼 영향 범위가 큰 변경은 구현 전에 범위와 접근 방식을 확인한다.

## Verification

- Backend 변경은 `backend/AGENTS.md`의 검증 절차를 따른다.
- Terraform 변경은 `terraform fmt -check -recursive infra`와 대상 module의 `terraform validate`를 실행한다.
- Workflow 변경은 YAML 파싱과 관련 정적 검사를 실행한다.
- 문서만 변경한 경우 index 연결과 실제 경로를 확인한다.

## Completion Checklist

상당한 공통 설정, 인프라, workflow, 문서 구조 변경을 마친 뒤:

1. `docs/agent-vault/workflows/verification.md`에 따라 검증한다.
2. `docs/agent-vault/workflows/worklog.md`에 따라 root worklog를 남긴다.
3. 하위 프로젝트도 크게 변경했다면 해당 프로젝트 worklog도 갱신한다.
4. final response에 변경 요약, 검증 결과, worklog 위치를 포함한다.

## Git Safety

- `git status --short --branch`로 현재 브랜치와 변경 상태를 확인한다.
- 사용자가 명시적으로 요청하지 않는 한 `git reset --hard`, `git checkout --`, force push를 하지 않는다.
- 커밋이나 PR을 요청받은 경우 의도한 파일만 stage한다.
