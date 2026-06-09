# Backend TDD Guideline

이 프로젝트의 백엔드 작업은 가능한 한 TDD를 선호한다. 새 기능, 도메인 규칙, 버그 수정, 회귀 방지 작업은 먼저 실패하는 테스트로 기대 동작을 고정한 뒤 구현한다.

## 원칙

- 구현 전에 `ai_debate_platform_mvp_spec.md`에서 기대 동작을 확인한다.
- 테스트는 변경하려는 계층과 가장 가까운 곳에서 시작한다.
- 도메인 규칙은 domain test로 먼저 고정한다.
- use case 흐름은 application test로 검증한다.
- persistence mapping과 query는 repository/infrastructure test로 검증한다.
- HTTP request/response, validation, status code는 controller test로 검증한다.
- 테스트가 구현 세부사항보다 관찰 가능한 동작을 설명하게 한다.

## 기본 흐름

1. 스펙이나 버그 설명에서 기대 동작을 한 문장으로 정리한다.
2. 가장 좁은 테스트를 추가하고 실패를 확인한다.
3. 통과에 필요한 최소 구현을 추가한다.
4. 중복과 이름을 정리한다.
5. 관련 범위의 테스트를 다시 실행한다.

## 계층별 우선순위

### Domain

순수 규칙은 domain test를 먼저 작성한다.

예:

- 필수값 검증
- 길이 제한
- 상태 전이 가능/불가능 조건
- value object 정규화와 동등성
- aggregate invariant

### Application

여러 domain object와 repository contract가 함께 움직이는 흐름은 application test로 작성한다.

예:

- 생성, 수정, 삭제 use case
- 존재하지 않는 ID 처리
- repository 저장 호출 이후 반환 shape
- clock/time provider 사용

### Infrastructure

저장소 구현이나 JPA mapping은 infrastructure test로 검증한다. domain 규칙을 infrastructure test에서 반복 검증하지 않는다.

예:

- entity 저장/조회
- ownerId 기준 목록 조회
- update persistence
- test profile datasource 설정

### Web

Controller는 request/response 경계를 검증한다. 비즈니스 규칙 자체는 domain/application test에서 먼저 검증하고, controller test는 HTTP contract에 집중한다.

예:

- request validation
- status code
- response body shape
- error response mapping

## 예외

다음 경우에는 테스트를 나중에 붙일 수 있다.

- 빠른 spike로 설계 가능성을 확인하는 경우
- 문서만 수정하는 경우
- 테스트 환경을 먼저 세팅해야 해서 바로 red test를 만들 수 없는 경우

예외를 사용했다면 final response나 worklog에 테스트를 늦춘 이유와 후속 테스트 범위를 남긴다.

## 완료 기준

- 새 동작 또는 수정된 동작을 검증하는 테스트가 있다.
- `.\mvnw.cmd test` 또는 변경 범위에 맞는 더 좁은 테스트 명령이 통과한다.
- 스펙과 구현이 달라졌다면 `ai_debate_platform_mvp_spec.md`도 함께 수정한다.
