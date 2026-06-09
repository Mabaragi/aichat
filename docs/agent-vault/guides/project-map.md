# Project Map

## Purpose

이 프로젝트는 사용자가 설정한 주제와 AI 캐릭터 성향을 바탕으로 두 AI 캐릭터가 정해진 규칙에 따라 토론하는 MVP 서비스다. 상세 제품 범위는 루트 `ai_debate_platform_mvp_spec.md`가 기준이다.

## Stack

- Java 21
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- SQLite (`jdbc:sqlite:./data/ai-debate.db`)
- SpringDoc OpenAPI
- Lombok
- Maven Wrapper (`mvnw`, `mvnw.cmd`)

## Runtime

- 기본 server port는 `8080`이다.
- datasource는 `src/main/resources/application.yaml`에서 설정한다.
- 로컬 SQLite 파일은 `data/ai-debate.db`를 사용한다.
- JPA `ddl-auto`는 현재 `update`다.

## Package Boundaries

기준 package는 `com.example.aichat`다.

- `character`: AI 캐릭터 생성, 수정, 조회, 삭제.
- `debate`: 토론 세션, 참가자, 턴 생성, 재생성, 목록/상세 조회, 완료.
- `generation`: 여러 비즈니스 도메인이 재사용하는 provider-neutral 텍스트 생성 capability. `application`에 생성 계약을 두고 `infrastructure`에 provider adapter를 둔다.
- `share`: 생성된 토론 콘텐츠의 공유 링크 생성, 조회, 삭제.
- `user`: MVP 사용자 생성과 조회.
- `common`: 공통 exception, error response, time abstraction.

각 도메인은 기본적으로 다음 계층을 따른다.

- `domain`: entity, enum, repository contract, 순수 도메인 규칙.
- `application`: use case와 transaction boundary.
- `infrastructure`: persistence, external client adapter.
- `web`: controller, request DTO, response DTO.

`generation`은 별도 비즈니스 도메인이 아니라 지원/platform 모듈이므로 `domain` 계층을 두지 않는다. 토론 프롬프트 정책은 `debate.domain.DebateTurnPromptBuilder`가 소유하고, `generation`은 `debate` 타입을 참조하지 않는다.

## Current Notes

- `Character` 타입명은 `java.lang.Character`와 겹친다. 다른 package에서 사용할 때 import 충돌을 주의한다.
- `ai_debate_platform_mvp_spec.md`는 제품/기능 기준 명세다. 구현 흐름 문서는 `docs/development`에 둔다.
- 새 도메인 가이드는 `docs/agent-vault/guides`에 두되, 사람이 읽는 상세 구현 흐름이면 `docs/development`에 둔다.

## Useful Commands

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```
