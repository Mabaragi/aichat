# Java Records And Value Objects

Date: 2026-06-09

## Context

이 프로젝트는 `CreateCharacterRequest`, `CreateCharacterCommand`, `CharacterView` 같은 경계 데이터와 `Personality`, `SpeechStyle` 같은 작은 value object에 Java `record`를 사용한다.

반대로 `Character`처럼 identity, lifecycle, 상태 변경 메서드가 있는 도메인 entity는 `class`로 둔다.

## What `record` Provides

Java `record`는 값 전달 객체에 필요한 기본 코드를 자동 생성한다.

- component는 `private final` 필드가 된다.
- canonical constructor가 생긴다.
- accessor가 생긴다. 예: `value()`, `ownerId()`, `name()`.
- `equals()`와 `hashCode()`가 component 값 기준으로 생성된다.
- `toString()`이 생성된다.

따라서 아래 두 객체는 같은 값으로 비교된다.

```java
new Personality("calm").equals(new Personality("calm")) // true
```

## Self Validation

`record`도 compact constructor를 사용해 자기 검증과 정규화를 할 수 있다.

```java
public record Personality(String value) {

    public Personality {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("personality is required");
        }

        value = value.trim();
    }
}
```

compact constructor에서 component parameter를 바꾸면 최종 필드에 그 값이 들어간다. 그래서 아래 비교도 true가 된다.

```java
new Personality(" calm ").equals(new Personality("calm")) // true
```

## Why Keep `of`

현재 `Personality.of("calm")`은 `new Personality("calm")`과 거의 같다.

```java
public static Personality of(String value) {
    return new Personality(value);
}
```

그래도 `of`를 두면 호출부가 도메인 생성 의도를 더 명확히 표현하고, 나중에 생성 정책이 바뀌어도 호출부 변경을 줄일 수 있다.

예를 들어 이후에 `null` 처리, JSON 정규화, 기본값, 캐싱 같은 정책이 생기면 `of` 안으로 모을 수 있다.

```java
public static Personality of(String value) {
    return value == null ? null : new Personality(value);
}
```

단, Java `record`의 canonical constructor는 public이므로 `new Personality(...)` 사용 자체를 막을 수는 없다. 팀이 단순함을 더 중시하면 `of` 없이 constructor 호출로 통일해도 된다.

## Project Guideline

- Request/response DTO, command, result, view는 `record`를 우선 고려한다.
- 작은 value object는 `record`를 우선 고려하되 compact constructor에서 검증과 정규화를 수행한다.
- Aggregate root, entity, JPA entity, 상태 변경이 있는 도메인 모델은 `class`를 사용한다.
- `of` factory는 생성 정책이 커질 가능성이 있거나 호출부의 도메인 의도를 드러내고 싶을 때 둔다.
