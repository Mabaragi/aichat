# Decision Notes

이 디렉터리는 되돌리기 어렵거나 다음 작업자가 반드시 알아야 하는 구조적 결정을 기록한다.

## When To Add

- package boundary, persistence strategy, API shape, workflow rule처럼 이후 구현을 제약하는 결정을 했을 때.
- 반복된 논의나 수정을 하나의 기준으로 고정해야 할 때.
- 명세와 구현 사이의 해석을 durable하게 남겨야 할 때.

## File Name

```text
YYYY-MM-DD-short-title.md
```

## Template

```md
# <Decision Title>

Date: YYYY-MM-DD

## Context

## Decision

## Consequences

## Links
```
