---
description: task 생성 이후 spec/plan/tasks 간 일관성과 품질을 읽기 전용으로 분석합니다.
---

## User Input

```text
$ARGUMENTS
```

입력이 비어있지 않다면 반드시 고려한 뒤 진행합니다.

## 한국어 작성 규칙

- 보고서는 한국어로 작성합니다.
- 파일 경로/토큰/명령어는 원문 그대로 유지합니다.

## Goal

`spec.md`, `plan.md`, `tasks.md` 간의 불일치, 중복, 모호성, 누락을 구현 전에 탐지합니다.
이 명령은 `/speckit.tasks`로 `tasks.md`가 생성된 후에만 실행합니다.

## Operating Constraints

- **STRICTLY READ-ONLY**: 어떤 파일도 수정하지 않습니다.
- 헌장(`.specify/memory/constitution.md`) 위반은 자동으로 **CRITICAL**입니다.

## Execution Steps

1. `.specify/scripts/bash/check-prerequisites.sh --json --require-tasks --include-tasks` 실행
   - `FEATURE_DIR`, `AVAILABLE_DOCS` 파싱
   - `SPEC`, `PLAN`, `TASKS` 절대 경로 도출
2. 아티팩트 로드(필요 최소 범위)
   - spec: 요구사항, 유저스토리, 엣지케이스
   - plan: 아키텍처/제약/단계
   - tasks: Task ID/설명/단계/[P]/파일 경로
   - constitution: 원칙 검증
3. 내부 모델 구성
   - 요구사항 인벤토리
   - 유저 액션 인벤토리
   - 작업-요구사항 매핑
   - 헌장 규칙 집합
4. 탐지 패스
   - Duplication
   - Ambiguity
   - Underspecification
   - Constitution Alignment
   - Coverage Gaps
   - Inconsistency
5. 심각도 부여
   - CRITICAL / HIGH / MEDIUM / LOW
6. 보고서 출력 (파일 쓰기 금지)

## Output Format

### Specification Analysis Report

| ID | Category | Severity | Location(s) | Summary | Recommendation |
|----|----------|----------|-------------|---------|----------------|
| A1 | Duplication | HIGH | spec.md | ... | ... |

### Coverage Summary

| Requirement Key | Has Task? | Task IDs | Notes |
|-----------------|-----------|----------|-------|

추가 포함:

- Constitution Alignment Issues
- Unmapped Tasks
- Metrics (요구사항 수, 작업 수, Coverage %, 모호성 수, 중복 수, Critical 수)

## Next Actions

- CRITICAL 존재 시 `/speckit.implement` 진행 전 선해결 권고
- LOW/MEDIUM만 존재 시 진행 가능 + 개선 제안
- 필요 시 수정 권고 명령 제시 (`/speckit.specify`, `/speckit.plan`, 수동 tasks 보강 등)

마지막 질문:

`상위 N개 이슈에 대한 구체 수정안(편집 제안)을 생성할까요?`
