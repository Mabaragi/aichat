# Character CLI Guide

## Purpose

`character` CLI는 REST API와 별개인 실행 진입점이다. 에이전트는 이 CLI를 통해 캐릭터를 생성, 조회, 목록 조회, 수정, 삭제할 수 있다.

CLI는 `com.example.aichat.character.application` use case만 호출한다. 비즈니스 규칙은 CLI에 두지 않는다.

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

## Output Shapes

- Success responses are deterministic and use the same field order every time.
- `JSON` mode returns structured output suitable for agent parsing.
- `TEXT` mode is best for interactive debugging and human inspection.

## Exit Codes

- `0`: success
- `1`: unexpected runtime error
- `2`: validation or business-rule failure

## Error Handling

- `CHARACTER_NOT_FOUND` is returned when a requested character does not exist.
- CLI input validation failures use `INVALID_ARGUMENT`.
- Invalid JSON input for `--personality` or `--speech-style` is rejected before calling the use case.

## Notes For Agents

- Prefer `JSON` output unless you specifically need a human-readable trace.
- Use `character list` before `character get` or `character update` when you do not already know the ID.
- Keep `personality` and `speech-style` JSON small and valid; shell quoting is the common failure point.
