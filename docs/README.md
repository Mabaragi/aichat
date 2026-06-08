# Docs

이 디렉터리는 프로젝트 문서를 용도별로 분리한다.

## 문서 구조

- `development/`: 사람 개발자가 기능을 구현할 때 참고하는 개발 흐름 문서.
- `agent-vault/`: AI 에이전트가 작업 맥락을 빠르게 찾기 위한 라우터, 작업 가이드, workflow, decision note, worklog.
- `learnings/`: 사람 개발자가 나중에 읽을 디버깅 기록, 발견 사항, 구현 교훈, 비교/트레이드오프 정리.

## 작성 기준

- 코드 식별자, package name, API path, command, file path는 원문 형태로 유지한다.
- 작업 상태와 검증 결과는 `agent-vault/worklogs`에 둔다.
- durable architecture 또는 workflow decision은 `agent-vault/decisions`에 둔다.
- 사람이 읽을 학습 서사는 `learnings`에 둔다.
