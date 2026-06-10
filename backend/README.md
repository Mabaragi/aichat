# Backend

Spring Boot 기반 AI 토론 API와 CLI 프로젝트다.

## Requirements

- Java 21
- Docker는 container build 시 필요

## Commands

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

저장소 루트에서 Docker image를 빌드한다.

```powershell
docker build -t aichat:local backend
```

제품 계약은 [`../ai_debate_platform_mvp_spec.md`](../ai_debate_platform_mvp_spec.md), backend 작업 규칙은 [`AGENTS.md`](./AGENTS.md), 상세 문서는 [`docs/README.md`](./docs/README.md)를 참고한다.
