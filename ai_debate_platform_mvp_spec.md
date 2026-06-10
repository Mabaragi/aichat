# AI 토론 플랫폼 MVP 명세서

## 1. 프로젝트 개요

이 프로젝트는 AI 캐릭터들이 서로 상호작용하며 콘텐츠를 만들어내는 엔터테인먼트 플랫폼을 목표로 한다.

초기 MVP는 **AI 토론 플랫폼**이다. 사용자가 토론 주제와 두 참가자 모델을 지정하면, AI 참가자들이 해당 주제로 토론하고 사용자는 그 과정을 감상한다.

MVP의 핵심은 단순한 챗봇이 아니라, **여러 AI 참가자가 하나의 세션 안에서 순서대로 상호작용하는 콘텐츠 생성 구조**를 만드는 것이다.

---

## 2. MVP 한 줄 정의

**사용자가 설정한 주제와 참가자 모델을 바탕으로, 두 AI 참가자가 정해진 규칙에 따라 토론하는 엔터테인먼트 서비스.**

---

## 3. MVP 목표

MVP에서 검증할 핵심 가설은 다음과 같다.

1. 사용자는 직접 AI와 대화하지 않아도, AI 참가자들끼리 상호작용하는 콘텐츠를 보는 것에서 재미를 느낀다.
2. 사용자가 주제와 참가자 모델을 지정하면, 반복 감상 가능한 토론 콘텐츠가 생성된다.
3. 참가자 모델의 응답 정책 차이가 토론의 재미를 만든다.
4. 토론 세션 구조는 이후 상황극, 소설, 게임 시뮬레이션으로 확장될 수 있다.

---

## 4. MVP 범위

### 4.1 포함 범위

MVP에서 구현할 기능은 다음과 같다.

- 이메일/비밀번호 회원가입
- JWT 로그인, access token 갱신, 로그아웃
- 사용자 소유 리소스 인증/인가
- AI 캐릭터 생성
- AI 캐릭터 목록 조회
- 토론 세션 생성
- 토론 참가자 설정
- 토론 세션 시작
- AI 발화 생성
- 토론 턴 목록 조회
- 토론 세션 조회
- 토론 세션 완료
- 생성된 토론 내용 저장
- 간단한 공유용 공개 링크 생성

### 4.2 제외 범위

MVP에서는 다음 기능을 구현하지 않는다.

- 소셜 로그인
- 관리자 역할 및 권한 체계
- access token blacklist
- 캐릭터 장기 기억
- 캐릭터 간 관계도
- 세계관 설정
- 상황극/소설 생성
- 마피아 게임/보드게임 시뮬레이션
- 실시간 WebSocket 스트리밍
- 복잡한 결제 시스템
- 추천 알고리즘
- 피드/커뮤니티 기능
- 관리자 페이지

---

## 5. 핵심 사용자 시나리오

### 5.1 기본 시나리오

1. 사용자가 AI 캐릭터를 만든다.
2. 사용자가 토론 주제를 입력한다.
3. 사용자가 토론에 참여할 두 참가자 모델을 선택한다.
4. 각 참가자 모델의 발화 순서는 요청 배열 순서로 결정된다.
5. 토론 라운드 수와 형식을 설정한다.
6. 토론 세션을 생성한다.
7. 사용자가 토론 시작 버튼을 누른다.
8. AI 참가자들이 순서대로 발화한다.
9. 사용자는 생성된 토론을 읽는다.
10. 토론이 끝나면 세션이 완료 상태가 된다.
11. 사용자는 마음에 드는 토론을 저장하거나 공유한다.

### 5.2 예시

```text
주제: 부먹 vs 찍먹

참가자 A:
- model: FAST

참가자 B:
- model: QUALITY

토론 형식:
- 찬반 토론
- 최대 5라운드
```

---

## 6. 도메인 모델 개요

MVP의 핵심 도메인은 다음과 같다.

```text
User
 ├─ Character
 └─ DebateSession
     ├─ DebateParticipant
     │   ├─ Character Snapshot
     │   └─ ParticipantModel
     └─ DebateTurn
         └─ DebateParticipant
```

가장 중요한 Aggregate Root는 `DebateSession`이다.

---

## 7. 핵심 도메인 개념

### 7.1 User

서비스 사용자다.

MVP에서는 이메일/비밀번호 회원가입과 Bearer JWT 인증을 사용한다. Access token은
15분, Refresh token은 14일 동안 유효하며 Refresh token은 서버에 SHA-256 해시로
저장하고 갱신할 때마다 회전한다.

주요 책임:

- 캐릭터 소유
- 토론 세션 소유
- 저장/공유 콘텐츠 소유

### 7.2 Character

사용자가 생성한 AI 캐릭터 원본이다.

캐릭터는 여러 토론 세션에서 재사용될 수 있다.

주요 책임:

- 캐릭터 기본 정보 관리
- 성격 설정 보관
- 말투 설정 보관
- 공개 여부 관리

### 7.3 DebateSession

하나의 토론 콘텐츠 생성 단위다.

MVP의 핵심 Aggregate Root다.

주요 책임:

- 토론 주제 관리
- 토론 상태 관리
- 참가자 목록 관리
- 현재 라운드 관리
- 토론 종료 조건 관리

### 7.4 DebateParticipant

특정 토론 세션 안에서 발화할 캐릭터와 생성 모델을 나타낸다.

`DebateParticipant`는 `Character` aggregate를 직접 참조하지 않고, 세션 생성 시점의 캐릭터 정보를 스냅샷으로 보관한다. `sourceCharacterId`는 원본 추적용 식별자이며, 이름, 설명, 성격, 말투는 토론 참가자 안에 복사한다.

이 구조를 사용하는 이유:

- 원본 캐릭터가 수정되거나 삭제돼도 기존 토론의 참가자 설정이 변하지 않는다.
- `debate.domain`이 `character.domain.Character` 타입에 의존하지 않는다.
- 같은 캐릭터를 서로 다른 `ParticipantModel`로 두 번 선택할 수 있다.
- 세션 요청 배열 순서를 `position`으로 고정해 발화 순서를 안정적으로 보존한다.

`CreateDebateSessionUseCase`는 application 계층에서 `CharacterRepository`를 조회하고 접근 권한을 확인한 뒤 `DebateParticipant` 스냅샷을 생성한다.

### 7.5 DebateTurn

토론 중 한 번의 발화다.

주요 책임:

- 누가 말했는지 기록
- 몇 번째 라운드인지 기록
- 몇 번째 턴인지 기록
- 발화 내용 저장
- 생성 당시 프롬프트와 모델 정보 저장
- 토큰 사용량 저장

### 7.6 SavedScene

사용자가 특정 토론 구간을 저장한 것이다.

MVP에서는 최소 필드만 둔다.

### 7.7 SharedContent

공개 공유 링크를 위한 모델이다.

MVP에서는 토론 세션 하나를 공개 링크로 공유하는 정도만 지원한다.

### 7.8 UserIntervention

사용자 개입 기능을 위한 확장 모델이다.

MVP에서는 실제 기능 구현을 미뤄도 되지만, 향후 확장을 위해 도메인 개념으로만 남긴다.

---

## 8. 도메인 엔티티 상세

## 8.1 User

| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | 사용자 ID |
| email | String | 이메일 |
| passwordHash | String | Delegating PasswordEncoder로 인코딩한 비밀번호 |
| nickname | String | 닉네임 |
| createdAt | LocalDateTime | 생성일 |

---

## 8.2 Character

| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | 캐릭터 ID |
| ownerId | Long | 소유자 ID |
| name | String | 캐릭터 이름 |
| description | String | 캐릭터 설명 |
| personality | String 또는 JSON | 성격 설정 |
| speechStyle | String 또는 JSON | 말투 설정 |
| visibility | String | 공개 여부 |
| createdAt | LocalDateTime | 생성일 |
| updatedAt | LocalDateTime | 수정일 |

현재 코드의 `Character` 클래스는 위 목표 필드 중 `personality`를 아직 저장하지 않는다. 생성 팩토리 인자로는 `personality`를 받지만 객체 필드에 반영되지 않으므로, MVP 구현 시 `personality` 저장 필드를 추가하거나 명세에서 제외하는 결정을 해야 한다.

### personality 예시

```json
{
  "rationality": 80,
  "aggressiveness": 30,
  "humor": 40,
  "empathy": 60
}
```

### speechStyle 예시

```json
{
  "tone": "차분함",
  "formality": "높음",
  "sentenceStyle": "논리적이고 간결함",
  "catchphrase": ""
}
```

---

## 8.3 DebateSession

| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | 토론 세션 ID |
| ownerId | Long | 생성자 ID |
| topicTitle | String | 토론 주제 |
| topicDescription | String | 주제 설명 |
| topicCategory | String | 주제 카테고리 |
| status | String | 세션 상태 |
| format | String | 토론 형식 |
| maxRounds | Integer | 최대 라운드 |
| currentRound | Integer | 현재 라운드 |
| maxTurnLength | Integer | 발화 최대 길이 |
| createdAt | LocalDateTime | 생성일 |
| startedAt | LocalDateTime | 시작일 |
| endedAt | LocalDateTime | 종료일 |

### DebateSessionStatus

```java
public enum DebateSessionStatus {
    CREATED,
    READY,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}
```

### DebateFormat

```java
public enum DebateFormat {
    FREE_DISCUSSION,
    PROS_AND_CONS
}
```

---

## 8.4 DebateParticipant

| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | 토론 참가자 ID |
| sourceCharacterId | Long | 원본 캐릭터 추적용 ID |
| position | Integer | 세션 안의 발화 순서, 0 또는 1 |
| model | ParticipantModel | 참가자가 발화 생성에 사용할 모델 선택 |
| name | String | 생성 시점 캐릭터 이름 |
| description | String | 생성 시점 캐릭터 설명 |
| personality | String 또는 JSON | 생성 시점 성격 설정 |
| speechStyle | String 또는 JSON | 생성 시점 말투 설정 |

### ParticipantModel

```java
public enum ParticipantModel {
    MOCK,
    FAST,
    BALANCED,
    QUALITY
}
```

MVP 초기에는 `MOCK`으로 도메인 흐름을 검증하고, 실제 LLM 연동 단계에서 `FAST`, `BALANCED`, `QUALITY`를 모델 선택 정책에 연결한다.

---

## 8.5 DebateTurn

| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | 발화 ID |
| sessionId | Long | 토론 세션 ID |
| participantId | Long | 발화자 ID |
| round | Integer | 라운드 |
| turnIndex | Integer | 전체 턴 순서 |
| type | String | 발화 타입 |
| status | String | 발화 상태 |
| content | Text | 발화 내용 |
| promptSnapshot | Text | 생성 당시 프롬프트 |
| modelName | String | 사용 모델 |
| inputTokens | Integer | 입력 토큰 수 |
| outputTokens | Integer | 출력 토큰 수 |
| createdAt | LocalDateTime | 생성일 |

### TurnType

```java
public enum TurnType {
    OPENING,
    ARGUMENT,
    REBUTTAL,
    SUMMARY,
    USER_INTERVENTION
}
```

### TurnStatus

```java
public enum TurnStatus {
    GENERATING,
    COMPLETED,
    FAILED,
    BLOCKED
}
```

---

## 8.6 SavedScene

| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | 저장 장면 ID |
| ownerId | Long | 소유자 ID |
| sessionId | Long | 토론 세션 ID |
| title | String | 저장 제목 |
| startTurnIndex | Integer | 시작 턴 |
| endTurnIndex | Integer | 종료 턴 |
| createdAt | LocalDateTime | 생성일 |

---

## 8.7 SharedContent

| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | 공유 콘텐츠 ID |
| ownerId | Long | 소유자 ID |
| sessionId | Long | 토론 세션 ID |
| slug | String | 공유 URL 식별자 |
| visibility | String | 공개 여부 |
| createdAt | LocalDateTime | 생성일 |

---

## 9. MVP DB 테이블 초안

SQLite 기준으로 시작한다.

```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    nickname TEXT NOT NULL,
    created_at TEXT NOT NULL
);

CREATE TABLE refresh_tokens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    family_id BLOB NOT NULL,
    session_id TEXT NOT NULL,
    token_hash TEXT NOT NULL UNIQUE,
    expires_at TEXT NOT NULL,
    revoked_at TEXT,
    replaced_by_hash TEXT,
    version INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE characters (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    owner_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    personality TEXT,
    speech_style TEXT,
    visibility TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE debate_sessions (
    id INTEGER PRIMARY KEY,
    owner_id INTEGER NOT NULL,
    topic_title TEXT NOT NULL,
    topic_description TEXT,
    topic_category TEXT,
    status TEXT NOT NULL,
    format TEXT NOT NULL,
    max_rounds INTEGER NOT NULL,
    current_round INTEGER NOT NULL,
    max_turn_length INTEGER,
    created_at TEXT NOT NULL,
    started_at TEXT,
    ended_at TEXT,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE debate_participants (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER NOT NULL,
    source_character_id INTEGER NOT NULL,
    position INTEGER NOT NULL,
    model TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    personality TEXT,
    speech_style TEXT,
    FOREIGN KEY (session_id) REFERENCES debate_sessions(id),
    UNIQUE (session_id, position)
);

CREATE TABLE debate_turns (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER NOT NULL,
    participant_id INTEGER NOT NULL,
    round INTEGER NOT NULL,
    turn_index INTEGER NOT NULL,
    type TEXT NOT NULL,
    status TEXT NOT NULL,
    content TEXT,
    prompt_snapshot TEXT,
    model_name TEXT,
    input_tokens INTEGER,
    output_tokens INTEGER,
    created_at TEXT NOT NULL,
    FOREIGN KEY (session_id) REFERENCES debate_sessions(id),
    FOREIGN KEY (participant_id) REFERENCES debate_participants(id)
);

CREATE TABLE saved_scenes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    owner_id INTEGER NOT NULL,
    session_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    start_turn_index INTEGER NOT NULL,
    end_turn_index INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users(id),
    FOREIGN KEY (session_id) REFERENCES debate_sessions(id)
);

CREATE TABLE shared_contents (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    owner_id INTEGER NOT NULL,
    session_id INTEGER NOT NULL,
    slug TEXT NOT NULL UNIQUE,
    visibility TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users(id),
    FOREIGN KEY (session_id) REFERENCES debate_sessions(id)
);
```

MVP에서는 JPA `ddl-auto=update`로 시작해도 된다.
다만 기능이 안정되면 Flyway를 도입하는 것이 좋다.

---

## 10. 현재 저장소 구현 상태

이 문서는 MVP 목표 명세와 구현 방향을 함께 기록한다. 현재 저장소는 전체 MVP가 완성된 상태는 아니지만 User, Character와 토론 세션 생성 vertical slice는 실제 동작한다.

- 빌드 도구는 Maven이며, 루트에 `pom.xml`, `mvnw`, `mvnw.cmd`가 있다.
- 기준 패키지는 `com.example.aichat`이다.
- 설정 파일은 `src/main/resources/application.yaml`이며, SQLite 데이터베이스 `./data/ai-debate.db`를 사용한다.
- `User` 생성과 JPA 저장, `Character` CRUD와 JPA 저장이 구현되어 있다.
- `POST /api/debate-sessions`는 사용자와 캐릭터를 검증하고 캐릭터 스냅샷 기반 참가자 2명을 포함한 세션을 SQLite에 저장한다.
- `DebateSession`은 participant를 소유하는 aggregate root이며 별도 `DebateParticipantRepository`를 두지 않는다.
- `StartDebateSession`, turn 생성/조회, 세션 조회/완료와 share 기능은 아직 구현 전이다.
- 토론 프롬프트 정책은 `debate.domain.DebateTurnPromptBuilder`가 담당한다. 공용 생성 계약은 `generation.application`의 `TextGenerator`, `GenerationRequest`, `GenerationResult`로 구성되고, `generation.infrastructure.MockTextGenerator`가 deterministic mock 응답을 제공한다.
- `OpenAiTextGenerator`와 `GeminiTextGenerator` provider adapter 및 단위 테스트가 구현되어 있다. 아직 Spring bean 등록, provider 선택 설정, `GenerateNextTurnUseCase` orchestration 연결은 구현 전이다.
- HTTP API는 Bearer JWT 인증을 사용하며 Character와 DebateSession 생성의 소유자는 access token의 `sub`에서 결정한다.
- SQLite MVP에서는 `debate_sessions.id`를 persistence adapter가 현재 최대값 이후로 할당한다. 단일 애플리케이션 인스턴스를 전제로 JVM 내 할당을 직렬화한다.

---

## 11. Spring Boot 백엔드 기술 스택

### 11.1 기본 스택

- Java 21
- Spring Boot 4.x
- Spring Web MVC
- Spring Data JPA
- Validation
- SQLite
- Hibernate Community Dialects
- Lombok
- Maven
- Maven Wrapper
- IntelliJ IDEA

### 11.2 pom.xml 의존성/플러그인 기준

현재 루트 `pom.xml`은 Spring Boot parent와 다음 의존성을 기준으로 한다.

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>3.0.2</version>
    </dependency>
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-community-dialects</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

테스트 의존성은 `spring-boot-starter-data-jpa-test`, `spring-boot-starter-validation-test`, `spring-boot-starter-webmvc-test`를 사용한다.

빌드 플러그인은 `spring-boot-maven-plugin`과 `maven-compiler-plugin`을 사용한다. Lombok annotation processor는 `maven-compiler-plugin`의 `annotationProcessorPaths`로 설정한다.

Windows 기준 실행/검증 명령은 다음과 같다.

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

### 11.3 application.yaml 예시

```yaml
spring:
  application:
    name: aichat

  datasource:
    url: jdbc:sqlite:./data/ai-debate.db
    driver-class-name: org.sqlite.JDBC

  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

server:
  port: 8080
```

---

## 12. 패키지 구조

초기에는 모듈러 모놀리스 구조를 사용한다.

```text
src/main/java/com/example/aichat
 ├─ user
 │   ├─ domain
 │   ├─ application
 │   ├─ infrastructure
 │   └─ web
 │
 ├─ character
 │   ├─ domain
 │   ├─ application
 │   ├─ infrastructure
 │   └─ web
 │
 ├─ debate
 │   ├─ domain
 │   ├─ application
 │   ├─ infrastructure
 │   └─ web
 │
 ├─ generation
 │   ├─ application
 │   └─ infrastructure
 │
 └─ common
     ├─ domain
     ├─ exception
     └─ time
```

### 12.1 각 계층 책임

```text
domain
- 엔티티
- 값 객체
- 도메인 규칙
- 도메인 예외
- Repository 인터페이스

application
- 유스케이스
- 트랜잭션 경계
- 도메인 객체 조합
- 외부 서비스 호출 조율

infrastructure
- JPA Entity
- Spring Data Repository
- 외부 API Client
- DB 구현체

web
- Controller
- Request DTO
- Response DTO
```

---

## 13. 주요 유스케이스

### 13.1 User

- CreateUser
- GetUser

### 13.2 Character

- CreateCharacter
- UpdateCharacter
- GetCharacter
- ListCharacters
- DeleteCharacter

### 13.3 DebateSession

- CreateDebateSession
- StartDebateSession
- GenerateNextTurn
- CompleteDebateSession
- GetDebateSession
- ListDebateSessions

### 13.4 DebateTurn

- ListDebateTurns
- RegenerateTurn

### 13.5 Share

- CreateShareLink
- GetSharedSession
- DeleteShareLink

---

## 14. API 명세 초안

이 섹션은 앞으로 구현할 MVP 목표 API 명세다. 현재 코드에는 컨트롤러 클래스와 베이스 경로만 있으며, 실제 HTTP 메서드, Request DTO 필드, Response DTO 필드는 아직 구현 전이다.

현재 선언된 베이스 경로:

```text
/api/users
/api/auth
/api/characters
/api/debate-sessions
/api/debate-sessions/{sessionId}/turns
```

## 14.1 User API

### 회원가입

```http
POST /api/auth/signup
```

Request:

```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "마바라기"
}
```

Response:

```json
{
  "user": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "마바라기",
    "createdAt": "2026-06-05T12:00:00"
  },
  "accessToken": "<jwt>",
  "refreshToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### 로그인 및 토큰 관리

```text
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET  /api/users/me
```

- `/api/auth/refresh`는 Refresh token을 request body로 받고 Access/Refresh token을 모두 교체한다.
- 회전된 Refresh token이 다시 사용되면 같은 token family를 모두 폐기한다.
- `/api/auth/logout`은 해당 token family를 폐기하고 멱등적으로 `204`를 반환한다.
- Access token은 `Authorization: Bearer <access-token>` header로 전달한다.

---

## 14.2 Character API

### 캐릭터 생성

```http
POST /api/characters
Authorization: Bearer <access-token>
```

Request:

```json
{
  "name": "합리주의 미식가",
  "description": "논리적이고 차분하게 음식 취향을 분석하는 캐릭터",
  "personality": {
    "rationality": 90,
    "aggressiveness": 20,
    "humor": 30,
    "empathy": 50
  },
  "speechStyle": {
    "tone": "차분함",
    "formality": "높음",
    "sentenceStyle": "논리적이고 간결함"
  },
  "visibility": "PRIVATE"
}
```

Response:

```json
{
  "id": 1,
  "ownerId": 1,
  "name": "합리주의 미식가",
  "description": "논리적이고 차분하게 음식 취향을 분석하는 캐릭터",
  "visibility": "PRIVATE",
  "createdAt": "2026-06-05T12:00:00"
}
```

### 캐릭터 목록 조회

```http
GET /api/characters?ownerId=1
```

익명 사용자는 `PUBLIC` 캐릭터만 조회할 수 있다. `PRIVATE` 캐릭터는 소유자 access
token이 있을 때만 조회할 수 있으며, 다른 사용자의 접근에는 존재를 숨기기 위해
`404 CHARACTER_NOT_FOUND`를 반환한다. 생성·수정·삭제의 소유자는 request body가
아니라 access token의 `sub` claim에서 결정한다.

### 캐릭터 단건 조회

```http
GET /api/characters/{characterId}
```

### 캐릭터 수정

```http
PATCH /api/characters/{characterId}
```

### 캐릭터 삭제

```http
DELETE /api/characters/{characterId}
```

---

## 14.3 DebateSession API

### 토론 세션 생성

```http
POST /api/debate-sessions
Authorization: Bearer <access-token>
```

Request:

```json
{
  "topic": {
    "title": "부먹 vs 찍먹",
    "description": "탕수육 소스를 부어 먹는 것과 찍어 먹는 것 중 어느 방식이 더 나은가?",
    "category": "FOOD"
  },
  "format": "PROS_AND_CONS",
  "maxRounds": 5,
  "maxTurnLength": 600,
  "participants": [
    {
      "characterId": 10,
      "model": "FAST"
    },
    {
      "characterId": 20,
      "model": "QUALITY"
    }
  ]
}
```

인증 적용 후 `ownerId`는 request body에서 받지 않는다. Spring Security가 검증한 access token의 `sub` claim을 web 계층에서 `authenticatedUserId`로 변환해 `CreateDebateSessionUseCase`에 명시적으로 전달한다. use case는 이 ID로 사용자 존재 여부와 캐릭터 접근 권한을 검사하고 `DebateSession.ownerId`를 설정한다.

`@RequestBody`는 클라이언트 JSON을 DTO로 변환하고, 인증 principal은 Spring Security filter chain이 `Authorization` header의 JWT를 검증한 뒤 별도로 제공한다. body의 사용자 ID와 JWT 사용자는 자동으로 일치 검증되지 않으므로 protected API request DTO에 `ownerId`를 두지 않는다.

Response:

```json
{
  "id": 1,
  "ownerId": 1,
  "topicTitle": "부먹 vs 찍먹",
  "status": "CREATED",
  "format": "PROS_AND_CONS",
  "maxRounds": 5,
  "currentRound": 0,
  "participants": [
    {
      "id": 1,
      "sourceCharacterId": 10,
      "position": 0,
      "name": "합리주의 미식가",
      "description": "논리적이고 차분하게 음식 취향을 분석하는 캐릭터",
      "personality": {
        "rationality": 90
      },
      "speechStyle": {
        "tone": "차분함"
      },
      "model": "FAST"
    },
    {
      "id": 2,
      "sourceCharacterId": 20,
      "position": 1,
      "name": "직관적인 미식가",
      "description": null,
      "personality": null,
      "speechStyle": null,
      "model": "QUALITY"
    }
  ],
  "createdAt": "2026-06-05T12:00:00"
}
```

### 토론 세션 시작

```http
POST /api/debate-sessions/{sessionId}/start
```

Response:

```json
{
  "id": 1,
  "status": "RUNNING",
  "startedAt": "2026-06-05T12:01:00"
}
```

### 토론 세션 조회

```http
GET /api/debate-sessions/{sessionId}
```

### 내 토론 세션 목록 조회

```http
GET /api/debate-sessions?ownerId=1
```

### 토론 세션 완료

```http
POST /api/debate-sessions/{sessionId}/complete
```

---

## 14.4 DebateTurn API

### 다음 발화 생성

```http
POST /api/debate-sessions/{sessionId}/turns/generate
```

Response:

```json
{
  "id": 1,
  "sessionId": 1,
  "participantId": 1,
  "round": 1,
  "turnIndex": 1,
  "type": "ARGUMENT",
  "status": "COMPLETED",
  "content": "부먹은 소스와 튀김의 조화를 극대화한다는 점에서 더 완성도 높은 방식입니다.",
  "modelName": "mock-model",
  "inputTokens": 0,
  "outputTokens": 0,
  "createdAt": "2026-06-05T12:02:00"
}
```

MVP 초기에는 실제 LLM 호출 대신 mock generator를 사용할 수 있다.

### 토론 발화 목록 조회

```http
GET /api/debate-sessions/{sessionId}/turns
```

Response:

```json
[
  {
    "id": 1,
    "participantId": 1,
    "participantModel": "FAST",
    "round": 1,
    "turnIndex": 1,
    "type": "ARGUMENT",
    "content": "부먹은 소스와 튀김의 조화를 극대화합니다.",
    "createdAt": "2026-06-05T12:02:00"
  },
  {
    "id": 2,
    "participantId": 2,
    "participantModel": "QUALITY",
    "round": 1,
    "turnIndex": 2,
    "type": "REBUTTAL",
    "content": "찍먹은 바삭함과 개인 취향을 보존한다는 점에서 더 합리적입니다.",
    "createdAt": "2026-06-05T12:03:00"
  }
]
```

---

## 14.5 Share API

### 공유 링크 생성

```http
POST /api/debate-sessions/{sessionId}/share
```

Response:

```json
{
  "id": 1,
  "sessionId": 1,
  "slug": "debate-bumuk-jjikmuk-001",
  "visibility": "PUBLIC",
  "url": "/share/debate-bumuk-jjikmuk-001",
  "createdAt": "2026-06-05T12:10:00"
}
```

### 공유 세션 조회

```http
GET /api/shared-contents/{slug}
```

---

## 15. 토론 생성 로직

### 15.1 기본 흐름

```text
1. DebateSession 조회
2. 세션 상태가 RUNNING인지 확인
3. 현재까지 생성된 DebateTurn 목록 조회
4. 다음 발화자 계산
5. 현재 라운드 계산
6. Prompt 생성
7. LLM 또는 Mock Generator 호출
8. DebateTurn 저장
9. 세션 currentRound 갱신
10. 종료 조건 확인
11. maxRounds에 도달하면 COMPLETED 처리
```

### 15.2 다음 발화자 계산

MVP에서는 세션의 `participants` 배열 순서 기준으로 번갈아 말하게 한다.

```text
참가자 A participants[0] model = FAST
참가자 B participants[1] model = QUALITY

turnIndex 1 -> A
turnIndex 2 -> B
turnIndex 3 -> A
turnIndex 4 -> B
```

### 15.3 라운드 계산

참가자가 2명인 MVP 기준:

```text
round = ((turnIndex - 1) / 2) + 1
```

예시:

```text
turnIndex 1 -> round 1
turnIndex 2 -> round 1
turnIndex 3 -> round 2
turnIndex 4 -> round 2
```

### 15.4 종료 조건

```text
maxRounds = 5
participants = 2
최대 turn 수 = 10

turnIndex가 10까지 생성되면 세션 완료
```

---

## 16. 프롬프트 구성

### 16.1 프롬프트에 포함할 정보

- 토론 주제
- 토론 설명
- 토론 형식
- 참가자 모델
- 참가자 이름, 설명, 성격, 말투 스냅샷
- 이전 발화 목록
- 이번 발화 목적
- 최대 길이

### 16.2 프롬프트 템플릿 초안

```text
당신은 AI 토론 플랫폼의 캐릭터입니다.

[토론 주제]
{topicTitle}

[주제 설명]
{topicDescription}

[토론 형식]
{format}

[당신의 참가자 모델]
모델: {participantModel}

[캐릭터]
이름: {participantName}
설명: {participantDescription}
성격: {participantPersonality}
말투: {participantSpeechStyle}

[이전 발화]
{previousTurns}

[지시]
위 정보를 바탕으로 참가자 모델의 응답 정책에 맞게 다음 발화를 작성하세요.
상대의 이전 발화를 참고하되, 단순 반복하지 마세요.
토론 주제에서 벗어나지 마세요.
최대 {maxTurnLength}자 이내로 작성하세요.
```

### 16.3 promptSnapshot 저장

생성된 모든 `DebateTurn`에는 생성 당시의 프롬프트를 저장한다.

이유:

- 생성 품질 디버깅
- 프롬프트 변경 영향 추적
- 모델별 결과 비교
- 비용과 품질 개선

---

## 17. LLM 연동 전략

### 17.1 MVP 1단계: Mock Generator

초기에는 실제 LLM API를 붙이지 않는다.

Mock Generator는 다음 목적을 가진다.

- 도메인 로직 검증
- 세션 상태 전이 검증
- 턴 순서 검증
- API 응답 구조 검증
- 프론트 연동 준비

예시 응답:

```text
{participantModel} 참가자가 '{topicTitle}'에 대해 다음 주장을 생성합니다.
```

### 17.2 MVP 2단계: 실제 LLM API 연동

도메인 로직이 안정되면 외부 LLM API를 붙인다.

권장 구조:

```text
DebateTurnGenerationUseCase
 ├─ DebateSessionRepository
 ├─ DebateTurnRepository
 ├─ DebateTurnPromptBuilder
 └─ TextGenerator
```

`TextGenerator`는 여러 비즈니스 도메인이 재사용할 수 있는 provider-neutral 인터페이스로 둔다. `generation`은 `debate` 타입을 참조하지 않는다.

```java
public interface TextGenerator {
    GenerationResult generate(GenerationRequest request);
}
```

구현체는 나중에 교체 가능하게 한다.

```text
MockTextGenerator
OpenAiTextGenerator
GeminiTextGenerator
LocalTextGenerator
```

토론 주제, 형식, 참가자 모델, 이전 발화, 최대 길이를 어떤 프롬프트로 구성할지는 `debate.domain.DebateTurnPromptBuilder`가 소유한다. provider별 request schema, 인증, timeout, retry, 실제 model ID 매핑은 `generation.infrastructure`가 담당한다.

---

## 18. 상태 전이

### 18.1 DebateSession 상태 전이

```text
CREATED
  -> RUNNING
  -> COMPLETED

CREATED
  -> CANCELLED

RUNNING
  -> PAUSED
  -> RUNNING

RUNNING
  -> FAILED

RUNNING
  -> CANCELLED
```

MVP 필수 상태 전이:

```text
CREATED -> RUNNING -> COMPLETED
```

나머지는 확장용으로 enum에 둘 수 있지만, 실제 API는 나중에 구현해도 된다.

### 18.2 DebateTurn 상태 전이

```text
GENERATING -> COMPLETED
GENERATING -> FAILED
GENERATING -> BLOCKED
```

MVP에서는 동기 생성이면 바로 `COMPLETED`로 저장해도 된다.

---

## 19. 도메인 규칙

### 19.1 DebateSession 규칙

- 토론 세션에는 최소 2명의 참가자가 필요하다.
- MVP에서는 참가자는 정확히 2명으로 제한한다.
- `maxRounds`는 1 이상 10 이하로 제한한다.
- `maxTurnLength`는 100 이상 2000 이하로 제한한다.
- `CREATED` 상태의 세션만 시작할 수 있다.
- `RUNNING` 상태의 세션만 발화를 생성할 수 있다.
- 최대 라운드에 도달하면 세션은 `COMPLETED`가 된다.

### 19.2 Character 규칙

- 캐릭터 이름은 필수다.
- 캐릭터 이름은 1자 이상 50자 이하로 제한한다.
- description은 1000자 이하로 제한한다.
- personality와 speechStyle은 JSON 문자열로 저장한다.
- ownerId는 필수다.

### 19.3 DebateParticipant 규칙

- model은 필수다.
- model은 `MOCK`, `FAST`, `BALANCED`, `QUALITY` 중 하나여야 한다.
- sourceCharacterId와 name은 필수다.
- position은 각각 0과 1이어야 한다.
- MVP에서는 세션 생성 요청의 `participants` 배열 순서가 발화 순서를 결정한다.
- 세션 소유자가 소유한 캐릭터 또는 `PUBLIC` 캐릭터만 참가자로 선택할 수 있다.
- 같은 캐릭터를 두 참가자 위치에 중복 선택할 수 있다.
- 참가자 생성 후 원본 캐릭터 변경은 저장된 스냅샷에 영향을 주지 않는다.

### 19.4 DebateTurn 규칙

- turnIndex는 세션 안에서 유일해야 한다.
- participantId는 해당 세션에 속한 참가자여야 한다.
- content는 `COMPLETED` 상태에서 비어 있으면 안 된다.
- promptSnapshot은 실제 LLM 생성 시 저장한다.

---

## 20. 예외 처리

### 20.1 공통 에러 응답

```json
{
  "code": "DEBATE_SESSION_NOT_FOUND",
  "message": "토론 세션을 찾을 수 없습니다.",
  "timestamp": "2026-06-05T12:00:00"
}
```

### 20.2 주요 에러 코드

```text
USER_NOT_FOUND
EMAIL_ALREADY_EXISTS
INVALID_CREDENTIALS
INVALID_TOKEN
REFRESH_TOKEN_REUSED
UNAUTHORIZED
CHARACTER_NOT_FOUND
DEBATE_SESSION_NOT_FOUND
DEBATE_PARTICIPANT_NOT_FOUND
INVALID_SESSION_STATE
INVALID_DEBATE_RULE
INVALID_PARTICIPANT_COUNT
DUPLICATED_SPEAKING_ORDER
TURN_GENERATION_FAILED
SHARED_CONTENT_NOT_FOUND
```

---

## 21. 테스트 전략

### 21.1 우선 작성할 테스트

#### DebateSession 도메인 테스트

- CREATED 상태의 세션은 시작할 수 있다.
- RUNNING 상태의 세션만 발화를 생성할 수 있다.
- maxRounds에 도달하면 세션은 완료된다.
- 참가자가 2명이 아니면 세션 생성에 실패한다.
- 참가자 position이 0과 1이 아니면 세션 생성에 실패한다.

#### DebateSession 생성 유스케이스 테스트

- 세션 소유자의 `PRIVATE` 캐릭터를 선택할 수 있다.
- 다른 사용자의 `PUBLIC` 캐릭터를 선택할 수 있다.
- 다른 사용자의 `PRIVATE` 캐릭터와 존재하지 않는 캐릭터는 거부한다.
- 같은 캐릭터를 서로 다른 모델로 두 번 선택할 수 있다.
- 원본 캐릭터가 변경돼도 참가자 스냅샷은 유지된다.

#### DebateTurn 생성 유스케이스 테스트

- 첫 번째 발화자는 `participants[0]` 참가자다.
- 두 번째 발화자는 `participants[1]` 참가자다.
- 세 번째 발화자는 다시 `participants[0]` 참가자다.
- turnIndex가 올바르게 증가한다.
- round가 올바르게 계산된다.
- 최대 턴 수에 도달하면 세션이 완료된다.

#### Character 테스트

- 캐릭터 이름이 없으면 생성할 수 없다.
- ownerId가 없으면 생성할 수 없다.
- 캐릭터를 생성하면 ownerId와 연결된다.

### 21.2 테스트 우선순위

```text
1. 순수 도메인 테스트
2. Application Service 테스트
3. Repository 테스트
4. Controller 테스트
```

---

## 22. 구현 순서

### 22.1 1차 구현

```text
1. Maven 기반 Spring Boot 프로젝트 구조 확인
2. SQLite 연결 확인 및 JPA 매핑 구현
3. User Entity/Repository/API 생성
4. Character Entity/Repository/API 생성
5. DebateSession Entity/Repository 생성
6. DebateParticipant Entity/Repository 생성
7. DebateTurn Entity/Repository 생성
```

### 22.2 2차 구현

```text
1. CreateDebateSessionUseCase 구현
2. StartDebateSessionUseCase 구현
3. GenerateNextTurnUseCase 구현
4. MockTextGenerator 구현
5. DebateTurn 목록 조회 구현
6. DebateSession 상세 조회 구현
```

### 22.3 3차 구현

```text
1. DebateTurnPromptBuilder 구현
2. 실제 LLM API 연동
3. promptSnapshot 저장
4. modelName/tokenUsage 저장
5. 생성 실패 처리
```

### 22.4 4차 구현

```text
1. SavedScene 구현
2. SharedContent 구현
3. 공유 링크 조회 API 구현
4. 프론트엔드 연동
```

---

## 23. MVP 완료 기준

MVP 완료 기준은 다음과 같다.

- 사용자가 캐릭터를 만들 수 있다.
- 사용자가 주제와 참가자를 지정해 토론 세션을 만들 수 있다.
- 토론 세션을 시작할 수 있다.
- AI 또는 mock generator가 순서대로 발화를 생성할 수 있다.
- 생성된 발화가 DB에 저장된다.
- 토론 턴 목록을 조회할 수 있다.
- maxRounds에 도달하면 토론이 완료된다.
- 토론 세션을 공개 링크로 공유할 수 있다.
- 전체 흐름이 프론트엔드에서 호출 가능한 REST API로 제공된다.

---

## 24. MVP 이후 계획

MVP 이후에는 토론 플랫폼을 더 넓은 AI 캐릭터 엔터테인먼트 플랫폼으로 확장한다.

첫 번째 확장 방향은 토론 구조 강화다. 캐릭터의 말투, 성격, 입장, 관계성을 더 세밀하게 설정하고, 사용자가 토론 중간에 개입해 흐름을 바꾸거나 특정 캐릭터에게 질문할 수 있게 한다. 마음에 드는 장면을 저장하고 공유하는 기능도 강화한다.

두 번째 확장 방향은 AI 상황극과 소설 생성이다. 여러 AI 캐릭터가 독립된 인물처럼 행동하며, 각자의 성격, 목표, 기억, 관계를 바탕으로 세계관 안에서 대화하고 갈등하며 이야기를 만들어가게 한다.

세 번째 확장 방향은 AI 게임 시뮬레이션이다. 마피아 게임이나 보드게임처럼 AI 캐릭터들이 독립된 플레이어로 참여하고, 거짓말, 추론, 협상, 투표, 배신 같은 상호작용을 통해 재미를 만든다.

궁극적으로는 사용자가 여러 AI 캐릭터를 직접 구성하고, 토론, 상황극, 소설, 게임 등 다양한 활동을 만들 수 있는 멀티 AI 캐릭터 플랫폼을 지향한다.

핵심 방향은 멀티 에이전트 기술을 복잡한 업무 도구가 아니라, 일반 사용자도 쉽게 즐길 수 있는 엔터테인먼트 경험으로 바꾸는 것이다.
