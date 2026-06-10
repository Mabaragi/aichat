# Monorepo Docs

이 디렉터리는 저장소 전체에 적용되는 문서를 둔다.

## Structure

- `agent-vault/`: 모노레포 구조, 공통 workflow, root worklog를 위한 agent context.
- `api/`: backend와 frontend가 공유하는 API contract와 사용 가이드.
- `deployment/`: 여러 프로젝트와 공통 인프라를 연결하는 배포 운영 문서.

## Documents

- [GitHub Actions + Terraform 배포 가이드](./deployment/aws-cicd.md)
- [OpenAPI contract](./api/README.md)
- [Next.js BFF와 배포 경계 결정](./agent-vault/decisions/2026-06-10-next-bff-deployment-boundary.md)

Spring Boot 구현 문서는 `backend/docs`, Next.js 구현 문서는 `frontend/docs`에 둔다.
