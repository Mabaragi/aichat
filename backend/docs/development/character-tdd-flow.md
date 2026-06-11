# Character TDD 전체 개발 흐름

이 문서는 `Character` 기능을 MVP 수준으로 구현할 때 따를 TDD 작업 순서를 정리한다. 기준 명세는 저장소 루트의 `../ai_debate_platform_mvp_spec.md`이고, 도메인 규칙 요약은 [Character 도메인 개발 흐름](./character-domain-flow.md)을 함께 참고한다.

Java 코드를 먼저 넓게 작성하지 않는다. 각 단계는 실패하는 테스트를 하나 추가하고, 그 테스트를 통과시키는 최소 구현을 만든 뒤, 중복과 계층 침범을 정리하는 순서로 진행한다.

## TDD 기본 루프

1. Red: 지금 구현할 동작 하나를 검증하는 테스트를 먼저 작성한다.
2. Green: 테스트를 통과시키는 최소 구현만 추가한다.
3. Refactor: 테스트가 통과한 상태에서 이름, 중복, 계층 경계를 정리한다.

단계별 실행 명령은 좁게 시작하고 마지막에 전체 테스트로 닫는다.

```powershell
.\mvnw.cmd -Dtest=CharacterTest test
.\mvnw.cmd -Dtest=CharacterRepositoryAdapterTest test
.\mvnw.cmd -Dtest=CreateCharacterUseCaseTest test
.\mvnw.cmd -Dtest=CharacterControllerTest test
.\mvnw.cmd test
```

테스트 이름은 실제 작성 파일명에 맞춰 조정해도 되지만, 각 단계에서는 관련 테스트만 먼저 실행한다.

## 1. Domain

대상은 `com.example.aichat.character.domain.Character`다. 이 단계에서는 Spring, JPA, HTTP를 끌어오지 않는다.

먼저 실패시킬 테스트:

- `ownerId`가 없으면 생성할 수 없다.
- `name`이 없거나 비어 있으면 생성할 수 없다.
- `name`은 1자 이상 50자 이하여야 한다.
- `description`은 없을 수 있지만, 값이 있으면 1000자 이하여야 한다.
- `persona`는 필수 구조화 JSON 값 객체이며 토론 중 모델이 따를 의사결정 규칙을 담는다.
- 생성 시 `visibility` 기본값은 `PRIVATE`다.
- 생성 시 `createdAt`, `updatedAt`이 기록되고, 수정 시 `updatedAt`만 새 값으로 바뀐다.

최소 구현:

- `Character`는 `id`, `ownerId`, `categoryId`, `name`, `description`, `persona`, `visibility`, `createdAt`, `updatedAt`을 가진다.
- 생성 진입점은 필수값과 길이 규칙을 한 번 검증한다.
- 수정 진입점은 부분 수정 입력을 반영하되, `name`과 `description` 규칙을 다시 검증한다.
- 시간은 도메인 내부에서 `LocalDateTime.now()`로 만들지 않고, application 계층에서 받은 값을 사용한다.

리팩터링 기준:

- 검증 로직은 생성과 수정에서 같은 기준을 쓰도록 정리한다.
- `Character`에는 JPA annotation, Spring annotation, request/response DTO를 넣지 않는다.
- `java.lang.Character`와 이름이 겹치므로 다른 패키지에서 사용할 때 import를 명확히 한다.

완료 기준:

- `.\mvnw.cmd -Dtest=CharacterTest test`가 통과한다.
- `Character` 단위 테스트만 읽어도 MVP 도메인 규칙을 이해할 수 있다.

## 2. CharacterRepository 계약

대상은 `com.example.aichat.character.domain.CharacterRepository`다. 이 인터페이스는 domain 계층에 남기고, Spring Data나 JPA 타입을 노출하지 않는다.

먼저 실패시킬 테스트:

- application 테스트에서 저장된 `Character`를 다시 조회할 수 있어야 한다.
- application 테스트에서 `ownerId` 기준 목록을 조회할 수 있어야 한다.
- application 테스트에서 존재하지 않는 ID를 조회하면 use case가 `CHARACTER_NOT_FOUND`로 변환해야 한다.
- application 테스트에서 삭제 후 같은 ID를 조회하면 찾을 수 없어야 한다.

최소 계약:

```java
Character save(Character character);
Optional<Character> findById(Long id);
List<Character> findAllByOwnerId(Long ownerId);
void delete(Character character);
```

계약 기준:

- repository는 저장과 조회만 담당한다.
- 없는 ID를 어떤 HTTP 오류로 바꿀지는 repository가 아니라 application use case가 담당한다.
- 삭제는 먼저 `findById`로 aggregate를 찾은 뒤 `delete(Character character)`로 수행한다.
- infrastructure 구현체가 필요해서 계약을 넓히지 않는다. application이 실제로 쓰는 동작만 추가한다.

완료 기준:

- application 테스트에서 fake repository로 위 계약을 사용할 수 있다.
- domain 인터페이스가 Spring Data의 `JpaRepository`, `Pageable`, `EntityManager` 같은 타입에 의존하지 않는다.

## 3. Infrastructure

대상은 `com.example.aichat.character.infrastructure`다. 여기서만 JPA 전용 타입을 둔다.

먼저 실패시킬 테스트:

- 저장한 캐릭터를 ID로 다시 조회할 수 있다.
- 같은 `ownerId`의 캐릭터 목록만 조회된다.
- `persona` JSON 문자열이 구조를 유지한 채 저장되고 조회된다.
- `createdAt`, `updatedAt`, `visibility`가 `characters` 테이블 매핑으로 유지된다.
- 삭제한 캐릭터는 다시 조회되지 않는다.

최소 구현:

- JPA 전용 entity는 `characters` 테이블 초안과 맞춘다.
- 컬럼명은 명세의 SQL 초안을 따른다: `owner_id`, `category_id`, `name`, `description`, `persona`, `visibility`, `created_at`, `updated_at`.
- `personality`, `speech_style` legacy 컬럼은 후속 cleanup 전까지 호환용으로 남아 있을 수 있지만 Java/API 구현에서는 사용하지 않는다.
- Spring Data repository와 domain `CharacterRepository` 구현 adapter를 infrastructure 안에 둔다.
- entity와 domain 사이 변환은 infrastructure 내부 mapper 또는 adapter 메서드에서 처리한다.

리팩터링 기준:

- domain `Character`가 JPA entity 역할을 겸하지 않게 한다.
- adapter 밖으로 JPA entity가 새어 나가지 않게 한다.
- SQLite 방언과 저장 방식에 맞춘 세부 처리는 infrastructure 내부에만 둔다.

완료 기준:

- repository adapter 테스트가 실제 JPA 매핑을 통해 통과한다.
- domain과 application 테스트는 infrastructure 구현 없이도 계속 통과한다.

## 4. Application Use Case

대상은 `com.example.aichat.character.application`의 `CreateCharacterUseCase`, `UpdateCharacterUseCase`, `GetCharacterUseCase`, `ListCharactersUseCase`, `DeleteCharacterUseCase`다.

먼저 실패시킬 테스트:

- `CreateCharacterUseCase`는 입력을 받아 `Character`를 생성하고 저장한다.
- `UpdateCharacterUseCase`는 기존 캐릭터를 찾고 수정한 뒤 저장한다.
- `GetCharacterUseCase`는 ID로 캐릭터를 반환한다.
- `ListCharactersUseCase`는 `ownerId` 기준 목록을 반환한다.
- `DeleteCharacterUseCase`는 기존 캐릭터를 삭제한다.
- 조회, 수정, 삭제 대상이 없으면 `CHARACTER_NOT_FOUND`로 실패한다.

최소 구현:

- 각 use case는 하나의 public 실행 메서드를 가진 독립 진입점으로 둔다.
- 시간은 `TimeProvider`에서 받아 `Character` 생성과 수정에 전달한다.
- 트랜잭션 경계는 application 계층에 둔다.
- 없는 ID는 `BusinessException`과 `ErrorCode.CHARACTER_NOT_FOUND`를 사용해 표현한다. 공통 예외 구조가 부족하면 Character 구현 전에 공통 예외 테스트를 먼저 보강한다.

리팩터링 기준:

- controller request DTO를 application 메서드 시그니처에 직접 노출하지 않는다.
- application은 JPA entity를 알지 않는다.
- 유스케이스 간 중복이 생기면 private helper보다 명확한 작은 메서드와 테스트 fixture를 우선 사용한다.

완료 기준:

- application 테스트는 fake `CharacterRepository`와 fake `TimeProvider`만으로 통과한다.
- 모든 not found 흐름이 `CHARACTER_NOT_FOUND` 하나로 일관된다.

## 5. Web Controller

대상은 `com.example.aichat.character.web`의 `CharacterController`, `CreateCharacterRequest`, `UpdateCharacterRequest`, `CharacterResponse`다.

먼저 실패시킬 테스트:

- `POST /api/characters`는 캐릭터를 생성하고 생성 결과를 반환한다.
- `GET /api/characters?ownerId={ownerId}`는 ownerId 기준 목록을 반환한다.
- `GET /api/characters/{characterId}`는 단건을 반환한다.
- `PATCH /api/characters/{characterId}`는 부분 수정 결과를 반환한다.
- `DELETE /api/characters/{characterId}`는 성공 시 본문 없는 응답을 반환한다.
- 잘못된 request validation은 4xx 응답이 된다.
- 없는 ID는 공통 에러 응답의 `code`가 `CHARACTER_NOT_FOUND`다.

최소 구현:

- controller는 use case 호출, request 변환, response 변환, HTTP status만 담당한다.
- `CreateCharacterRequest`와 `UpdateCharacterRequest`에는 HTTP 입력 검증을 둔다.
- `CharacterResponse`는 명세의 응답 모양을 따른다.
- `visibility`가 요청에 없으면 MVP 기본값 `PRIVATE`가 적용되도록 한다.

Persona 필드 정책:

- MVP 내부 저장 타입은 JSON 문자열을 감싼 `Persona` 값 객체다.
- HTTP 요청의 `persona`는 명세 예시처럼 구조화 JSON object로 받는다.
- web 또는 application 진입 DTO에서 JSON 값을 문자열로 직렬화한 뒤 domain `Persona`에서 shape를 다시 검증한다.
- `personality`, `speechStyle` 요청/응답은 지원하지 않는다.

리팩터링 기준:

- 도메인 규칙 자체를 controller 테스트에서 반복 검증하지 않는다.
- controller에 repository나 JPA entity를 직접 주입하지 않는다.
- JSON 직렬화 기준은 한 곳에 모아 response/request 변환 코드가 흩어지지 않게 한다.

완료 기준:

- controller 테스트가 HTTP status, request validation, response shape를 검증한다.
- Swagger UI에서 Character API 5개를 수동 확인할 수 있다.

## 최종 확인

마지막에는 좁은 테스트가 아니라 전체 테스트로 닫는다.

```powershell
.\mvnw.cmd test
```

완료 기준:

- 캐릭터 생성, 수정, 단건 조회, ownerId 기준 목록 조회, 삭제 API가 동작한다.
- 모든 Character MVP 규칙이 domain 또는 request validation에서 한 번 이상 검증된다.
- `CharacterController`는 use case 호출과 DTO 변환만 담당한다.
- `CharacterRepository` domain 계약은 Spring/JPA 타입을 노출하지 않는다.
- Swagger UI에서 `POST /api/characters`, `GET /api/characters`, `GET /api/characters/{characterId}`, `PATCH /api/characters/{characterId}`, `DELETE /api/characters/{characterId}`를 확인할 수 있다.
