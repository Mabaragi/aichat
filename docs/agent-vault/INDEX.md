# Agent Vault Index

이 문서는 에이전트가 필요한 컨텍스트만 골라 읽기 위한 라우터다. 작업을 시작할 때 루트 `AGENTS.md`를 읽은 뒤, 여기서 관련 문서만 추가로 연다.

## Project Guides

- `guides/project-map.md`: 프로젝트 목적, 기술 스택, 주요 패키지 경계, 현재 구현 상태를 요약한다. 새 기능을 추가하거나 낯선 도메인을 건드릴 때 읽는다.
- `guides/character-cli.md`: `character` CLI 명령 계약, 입력/출력, exit code, 예제를 정리한다. CLI를 직접 호출하는 에이전트 작업 전에 읽는다.
- `../../development/backend-tdd-guideline.md`: 백엔드 작업에서 TDD를 우선 적용하는 기준과 계층별 테스트 우선순위를 설명한다. 백엔드 기능, 버그 수정, 도메인 규칙을 구현할 때 읽는다.
- `../../development/character-domain-flow.md`: `character` 도메인의 MVP 구현 순서와 완료 기준을 설명한다. 캐릭터 도메인을 확장할 때 읽는다.

## Workflows

- `workflows/verification.md`: 코드 변경, 문서 변경, API 변경별 검증 기준을 정한다. final response 전에 어떤 검증을 실행할지 결정할 때 읽는다.
- `workflows/worklog.md`: 작업 완료 후 worklog를 남기는 규칙과 템플릿을 제공한다. substantial work를 마칠 때 읽는다.

## Decisions

- `decisions/README.md`: decision note를 언제 쓰고 어떤 형식으로 남길지 설명한다. 구조적 판단이나 되돌리기 어려운 결정을 했을 때 읽는다.
- `decisions/2026-06-09-generation-boundary.md`: 토론 프롬프트와 공용 텍스트 생성 capability의 책임 경계 및 의존 방향을 기록한다. prompt 또는 LLM provider 연동을 변경할 때 읽는다.

## Worklogs

- `worklogs/README.md`: 월별 worklog 파일 작성 규칙을 설명한다. 외부 worklog 채널이 없거나 저장소 안에 작업 기록을 남겨야 할 때 읽는다.

## Human Learning Notes

- `../learnings/README.md`: 사람 개발자를 위한 학습 기록 작성 규칙과 템플릿이다. 디버깅 서사, 비교, 구현 교훈을 남길 때 읽는다.
- `../learnings/INDEX.md`: 사람이 읽을 학습 노트의 annotated index다. 기존 학습 기록이 있는지 찾을 때 읽는다.
