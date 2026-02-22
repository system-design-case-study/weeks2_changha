---
description: tasks.md에 정의된 작업을 순서대로 실행해 구현을 완료합니다.
---

## User Input

```text
$ARGUMENTS
```

입력이 비어있지 않다면 반드시 고려한 뒤 진행합니다.

## 한국어 작성 규칙

- 사용자에게 보여주는 진행/결과는 한국어로 작성합니다.
- 명령어, 경로, 체크박스 토큰(`- [ ]`, `- [X]`, `T001`)은 유지합니다.

## Outline

1. `.specify/scripts/bash/check-prerequisites.sh --json --require-tasks --include-tasks` 실행
   - `FEATURE_DIR`, `AVAILABLE_DOCS` 파싱
2. 체크리스트 상태 확인 (`FEATURE_DIR/checklists/`가 있으면)
   - 파일별 총 항목, 완료, 미완료 집계
   - 하나라도 미완료면 표로 보고 후 사용자에게 진행 여부 확인
3. 구현 컨텍스트 로드
   - `tasks.md` (필수)
   - `plan.md`, `spec.md`, `data-model.md`, `contracts/`, `research.md`, `quickstart.md` (있으면 로드)
4. 작업 실행
   - 단계/의존성 순서대로 수행
   - `[P]`는 독립 파일 기준으로 병렬 수행
   - 테스트가 정의되어 있으면 먼저 실행/수정
5. 각 작업 완료 시 `tasks.md`에서 체크박스 업데이트
   - 완료: `- [X]`
6. 검증
   - 테스트/빌드/린트/스모크 등 가능한 검증 수행
   - 실패 시 원인과 다음 조치 보고
7. 종료 보고
   - 완료 작업 수, 남은 작업, 검증 결과, 리스크 요약

## Checklist Gate (if present)

다음 형식으로 상태를 보고합니다.

```text
| Checklist | Total | Completed | Incomplete | Status |
|-----------|-------|-----------|------------|--------|
| ux.md     | 12    | 12        | 0          | PASS   |
| test.md   | 8     | 5         | 3          | FAIL   |
```

- PASS: 모든 체크리스트의 미완료가 0
- FAIL: 하나 이상 미완료 존재

FAIL이면 아래를 질문하고 대기합니다.

`Some checklists are incomplete. Do you want to proceed with implementation anyway? (yes/no)`

## Guardrails

- 작업 파일(`tasks.md`)과 구현 파일을 동시에 추적해 상태 불일치가 없게 유지
- 실패를 숨기지 말고 즉시 보고
- 완료된 항목은 반드시 `[X]`로 반영
