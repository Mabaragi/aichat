# Character 도메인 개발 흐름

이 문서는 `src/main/java/com/example/aichat/character` 모듈을 실제 MVP 기능으로 구현할 때 따를 순서를 정리한다. 현재 코드는 스텁 상태이므로, 먼저 도메인 규칙을 고정한 뒤 바깥 계층으로 확장한다.

## 현재 구조

기준 패키지는 `com.example.aichat.character`다.

```text
character
├─ domain
│  ├─ Character
│  └─ CharacterRepository
├─ application
│  ├─ CreateCharacterUseCase
│  ├─ UpdateCharacterUseCase
│  ├─ GetCharacterUseCase
│  ├─ ListCharactersUseCase
│  └─ DeleteCharacterUseCase
├─ infrastructure
└─ web
   ├─ CharacterController
   ├─ CreateCharacterRequest
   ├─ UpdateCharacterRequest
   └─ CharacterResponse
```

`Character`라는 타입명은 `java.lang.Character`와 이름이 겹친다. 다른 패키지에서 사용할 때는 import 충돌을 주의하고, 필요하면 `com.example.aichat.character.domain.Character`처럼 의도를 분명히 한다.

## MVP 규칙

- `ownerId`는 필수다.
- `name`은 필수이며 1자 이상 50자 이하여야 한다.
- `description`은 선택값이며 1000자 이하여야 한다.
- `personality`와 `speechStyle`은 MVP에서는 JSON 문자열로 저장한다.
- `visibility`는 필수이며 초기 기본값은 `PRIVATE`로 둔다.
- 생성 시 `createdAt`, `updatedAt`을 기록하고, 수정 시 `updatedAt`만 갱신한다.
- 캐릭터 삭제는 우선 hard delete로 시작하되, 토론 세션 참조 정책이 정해지면 soft delete 전환 여부를 다시 판단한다.

## 구현 순서

1. `domain`부터 구현한다.
   `Character`에 필드와 생성/수정 메서드를 추가하고, 입력값 검증은 도메인 생성 지점에서 한 번 보장한다. 도메인 테스트는 `CharacterTest`에 먼저 추가한다.

2. `CharacterRepository` 계약을 정한다.
   application 계층이 필요한 동작만 인터페이스에 둔다. 최소 동작은 저장, ID 조회, ownerId 기준 목록 조회, 삭제다.

3. `infrastructure`에 JPA 구현을 둔다.
   SQLite 테이블 초안의 `characters` 구조와 맞춘다. JPA 전용 엔티티와 Spring Data repository를 infrastructure 안에 두고, domain repository 인터페이스를 구현하는 어댑터를 만든다.

4. `application` 유스케이스를 구현한다.
   `CreateCharacterUseCase`, `UpdateCharacterUseCase`, `GetCharacterUseCase`, `ListCharactersUseCase`, `DeleteCharacterUseCase`를 각각 독립된 진입점으로 둔다. 트랜잭션 경계는 application 계층에 둔다.

5. `web` 계층을 마지막에 연결한다.
   `CharacterController`는 DTO 변환과 HTTP status만 담당한다. 비즈니스 규칙은 controller에 넣지 않는다.

## API 흐름

- 생성: `POST /api/characters`
  요청 DTO는 `ownerId`, `name`, `description`, `personality`, `speechStyle`, `visibility`를 받는다.
- 목록 조회: `GET /api/characters?ownerId={ownerId}`
  MVP에서는 ownerId 기준 목록만 지원한다.
- 단건 조회: `GET /api/characters/{characterId}`
  존재하지 않으면 `CHARACTER_NOT_FOUND`로 응답한다.
- 수정: `PATCH /api/characters/{characterId}`
  부분 수정으로 시작하되, `name`이 들어오면 동일한 길이 규칙을 다시 적용한다.
- 삭제: `DELETE /api/characters/{characterId}`
  MVP에서는 성공 시 본문 없는 응답을 기본으로 한다.

## 테스트 우선순위

1. 순수 도메인 테스트
   이름 필수, 이름 길이, description 길이, ownerId 필수, 생성/수정 시간 갱신을 검증한다.

2. Application 테스트
   생성, 수정, 조회, 목록, 삭제 흐름이 repository 계약을 통해 동작하는지 검증한다. 없는 ID 조회와 삭제 실패도 포함한다.

3. Repository 테스트
   SQLite/JPA 설정에서 JSON 문자열 필드와 시간 필드가 저장/조회되는지 확인한다.

4. Controller 테스트
   요청 validation, HTTP status, response shape를 확인한다. 도메인 규칙 자체를 controller 테스트에서 반복 검증하지 않는다.

## 완료 기준

- 캐릭터 생성, 수정, 단건 조회, ownerId 기준 목록 조회, 삭제 API가 동작한다.
- 모든 Character MVP 규칙이 domain 또는 request validation에서 한 번 이상 검증된다.
- `CharacterController`는 유스케이스 호출과 DTO 변환만 담당한다.
- `.\mvnw.cmd test`가 통과한다.
- Swagger UI에서 `POST /api/characters`, `GET /api/characters`, `GET /api/characters/{characterId}`, `PATCH /api/characters/{characterId}`, `DELETE /api/characters/{characterId}`를 확인할 수 있다.
