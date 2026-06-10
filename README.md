# AI Debate Platform

AI 캐릭터 기반 토론 플랫폼 모노레포다. 제품 범위와 도메인 계약은 [MVP spec](./ai_debate_platform_mvp_spec.md)을 기준으로 한다.

## Projects

- [`backend/`](./backend/): Spring Boot API와 CLI
- [`frontend/`](./frontend/): Next.js UI와 JWT cookie BFF
- [`infra/`](./infra/): AWS 배포 인프라
- [`.github/workflows/`](./.github/workflows/): CI와 release workflow
- [`docs/`](./docs/): 모노레포 공통 문서

Backend 개발 명령과 구조는 [backend README](./backend/README.md)를 참고한다.
Frontend 개발 명령과 구조는 [frontend README](./frontend/README.md)를 참고한다.
