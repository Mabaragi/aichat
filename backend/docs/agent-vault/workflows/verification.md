# Verification Workflow

## Default

코드를 변경했으면 기본 검증으로 다음 명령을 실행한다.

```powershell
.\mvnw.cmd test
```

테스트가 실패하면 실패한 테스트, 핵심 오류, 수정 여부를 final response에 적는다.

## When To Broaden Verification

- Controller 또는 request/response DTO를 바꿨다면 관련 API 흐름과 HTTP status를 확인한다.
- 공개 HTTP API contract를 바꿨다면 `OpenApiContractExportTest`로
  `../../../../docs/api/openapi.json`을 갱신하고 생성 결과와 커밋 파일의 일치를 확인한다.
- Persistence 설정, repository, entity mapping을 바꿨다면 JPA/SQLite 저장과 조회 경로를 확인한다.
- 텍스트 생성 흐름을 바꿨다면 `DebateTurnPromptBuilder`, `TextGenerator`, provider adapter, 관련 debate turn 생성 테스트를 확인한다.
- `pom.xml`, `application.yaml`, Maven wrapper 관련 파일을 바꿨다면 compile 또는 test로 설정 오류를 확인한다.

## Docs-Only Changes

문서만 변경한 경우 코드 테스트는 생략할 수 있다. 대신 다음을 확인한다.

1. 새 문서가 적절한 index에서 연결되어 있다.
2. 경로가 실제 파일 위치와 일치한다.
3. agent-facing 문서와 human-facing 문서가 섞이지 않았다.

필요한 빠른 확인 명령:

```powershell
rg "docs/agent-vault|docs/learnings|docs/development|AGENTS.md" AGENTS.md docs
git status --short
```

명령은 `backend/`를 current directory로 두고 실행한다.
