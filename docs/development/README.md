# 개발 가이드

이 디렉터리는 사람 개발자가 기능을 구현할 때 참고하는 개발 흐름 문서를 둔다. 저장소의 실제 기준 명세는 루트의 `ai_debate_platform_mvp_spec.md`이며, 이 문서는 명세를 구현 순서로 풀어 쓰는 보조 문서다.

## 문서 목록

- [Character TDD 전체 개발 흐름](./character-tdd-flow.md): 실제 구현자가 먼저 읽을 문서. `character` 모듈을 domain부터 web까지 TDD로 완성하는 단계별 기준.
- [Character 도메인 개발 흐름](./character-domain-flow.md): `character` 모듈을 스텁 상태에서 MVP 구현으로 확장할 때 따를 순서와 완료 기준.

## 작성 기준

- 코드 식별자, 패키지명, API path는 원문 형태로 유지한다.
- 상세 구현 코드는 문서에 길게 넣지 않고, 구현 순서와 판단 기준을 중심으로 적는다.
- 실제 코드 위치가 바뀌면 관련 개발 가이드의 경로도 함께 갱신한다.
