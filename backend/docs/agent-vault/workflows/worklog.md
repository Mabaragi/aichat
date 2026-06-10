# Worklog Workflow

## When To Write

상당한 코드, 문서, 설정, 테스트, 빌드 산출물 변경을 완료하면 worklog를 남긴다.

외부 worklog 채널이 있으면 그 채널에 남긴다. 이 저장소 안에 기록해야 하거나 외부 채널이 없으면 `docs/agent-vault/worklogs/YYYY-MM.md`에 추가한다.

## Entry Format

```md
## YYYY-MM-DD - <짧은 작업명>

- 완료: <작업 요약>
- 아티팩트: <핵심 파일, 문서, PR, 스크린샷 링크>
- 검증: <실행한 검증 또는 미실행 사유>
- 후속: <남은 일 또는 없음>
```

## Final Response Block

worklog 파일을 쓰지 못했거나 외부 채널이 없는 경우 final response에 아래 형식의 worklog-ready block을 포함한다.

```text
완료: <작업 요약>
아티팩트: <핵심 파일/문서/PR 링크>
검증: <실행한 검증 또는 미실행 사유>
```
