# Learnings

이 디렉터리는 사람 개발자가 나중에 읽을 학습 기록을 둔다. 에이전트의 항상 읽는 작업 지침이 아니라, 디버깅 과정에서 얻은 발견, 구현 교훈, 비교/트레이드오프, 반복 참고할 설명을 정리하는 공간이다.

## Structure

- `INDEX.md`: 작성된 학습 노트의 annotated index.
- `notes/`: 날짜 기반 짧은 기록. 특정 디버깅 세션, 발견, 회고에 사용한다.
- `topics/`: 반복해서 참고할 주제별 정리. 시간이 지나도 유효한 설명에 사용한다.

## Boundary

- 작업 상태와 검증 결과는 `docs/agent-vault/worklogs`에 쓴다.
- 항상 따라야 하는 backend 에이전트 규칙은 `AGENTS.md` 또는 `docs/agent-vault`에 쓴다.
- durable architecture/workflow decision은 `docs/agent-vault/decisions`에 쓴다.
- 사람이 읽을 학습 서사와 구현 교훈은 이 디렉터리에 쓴다.

## Note Template

```md
# <제목>

Date: YYYY-MM-DD

## Context

## What We Learned

## Useful Commands Or Links

## Follow-Up
```
