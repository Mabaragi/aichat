# Monorepo Backend Boundary

Date: 2026-06-10

## Decision

- Spring Boot 애플리케이션과 전용 문서는 `backend/`가 소유한다.
- 제품 명세, AWS/Terraform, GitHub Actions, 배포 운영 문서는 저장소 루트가 소유한다.
- 루트 `AGENTS.md`는 프로젝트 라우팅과 공통 규칙만 제공하고 backend 구현 규칙은 `backend/AGENTS.md`에 둔다.
- Backend 명령은 `backend/`에서 실행하며 Docker build context는 저장소 루트 기준 `backend`다.

## Consequences

- 새 하위 프로젝트는 자체 `AGENTS.md`와 docs index를 가질 수 있다.
- 공통 문서 변경만으로 backend CI와 release가 실행되지 않는다.
- Workflow와 배포 script가 backend 경로를 명시적으로 참조해야 한다.
