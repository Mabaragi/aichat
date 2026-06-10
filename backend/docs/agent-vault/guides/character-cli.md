# Aichat CLI Guide

## Purpose

`aichat` CLI는 REST API와 별개인 실행 진입점이다. 에이전트는 이 CLI를 통해 캐릭터를 만들고, 사용자를 만들고, 토론 세션을 생성하고, 다음 턴 계산을 확인할 수 있다.

CLI는 `com.example.aichat.*.application` use case와 계산용 `debate` helper만 호출한다. 비즈니스 규칙은 CLI에 두지 않는다.

## Entry Point

- Main class: `com.example.aichat.cli.AichatCliApplication`
- Spring context: `com.example.aichat.AichatApplication` 기반, web server 없이 실행
- Default output: `JSON`

## Global Options

- `--output JSON|TEXT`
  - `JSON`: machine-readable pretty JSON
  - `TEXT`: 사람이 읽기 쉬운 텍스트
- `-h`, `--help`
  - 커맨드 도움말 출력

## Character Commands

### Create

```powershell
aichat --output json character create --owner-id 1 --name "합리주의 미식가" --description "논리적이고 차분하게 음식 취향을 분석하는 캐릭터" --personality "{\"rationality\":90}" --speech-style "{\"tone\":\"차분함\"}" --visibility PRIVATE
```

- Required:
  - `--owner-id`
  - `--name`
- Optional:
  - `--description`
  - `--personality`
  - `--speech-style`
  - `--visibility`
- `personality`와 `speech-style`은 raw JSON 문자열로 받는다.

### Get

```powershell
aichat character get 1
```

- Positional arg:
  - `CHARACTER_ID`

### List

```powershell
aichat character list --owner-id 1
```

- Required:
  - `--owner-id`

### Update

```powershell
aichat character update 1 --name "새 이름" --visibility PUBLIC
```

- Positional arg:
  - `CHARACTER_ID`
- Optional:
  - `--name`
  - `--description`
  - `--personality`
  - `--speech-style`
  - `--visibility`
- 생략된 필드는 기존 값을 유지한다.

### Delete

```powershell
aichat character delete 1
```

- Positional arg:
  - `CHARACTER_ID`

## User Commands

### Create

```powershell
aichat user create --email user@example.com --password password123 --nickname "마바라기"
```

- Required:
  - `--email`
  - `--password`
  - `--nickname`

### Get

```powershell
aichat user get 1
```

- Positional arg:
  - `USER_ID`

## Debate Commands

### Create

```powershell
aichat debate create --owner-id 1 --topic-title "부먹 vs 찍먹" --topic-description "어느 방식이 더 나은가?" --topic-category FOOD --format PROS_AND_CONS --max-rounds 5 --max-turn-length 600 --participant "{\"characterId\":10,\"model\":\"FAST\"}" --participant "{\"characterId\":20,\"model\":\"QUALITY\"}"
```

- Required:
  - `--owner-id`
  - `--topic-title`
  - `--format`
  - `--max-rounds`
  - `--max-turn-length`
  - `--participant` 2회
- Optional:
  - `--topic-description`
  - `--topic-category`
- `--participant`는 raw JSON 문자열이며 `characterId`와 `model`을 포함해야 한다.

### Next Turn

```powershell
aichat debate next-turn --turn-index 3 --participant-count 2 --max-rounds 5
```

- Required:
  - `--turn-index`
  - `--participant-count`
  - `--max-rounds`

## Output Shapes

- Success responses are deterministic and use the same field order every time.
- `JSON` mode returns structured output suitable for agent parsing.
- `TEXT` mode is best for interactive debugging and human inspection.

## Exit Codes

- `0`: success
- `1`: unexpected runtime error
- `2`: validation or business-rule failure

## Error Handling

- `CHARACTER_NOT_FOUND`, `USER_NOT_FOUND`, and `DEBATE_SESSION_NOT_FOUND` are returned when a requested resource does not exist.
- CLI input validation failures use `INVALID_ARGUMENT`.
- Invalid JSON input for `--personality`, `--speech-style`, or `--participant` is rejected before calling the use case.

## Notes For Agents

- Prefer `JSON` output unless you specifically need a human-readable trace.
- Use `character list` before `character get` when you do not already know the ID.
- Keep JSON options small and valid; shell quoting is the common failure point.
- `debate create` requires exactly two participants, so provide the `--participant` option twice.
