---
description: 계획 템플릿을 기반으로 구현 계획 워크플로를 실행해 설계 산출물을 생성합니다.
handoffs:
  - label: Create Tasks
    agent: speckit.tasks
    prompt: Break the plan into tasks
    send: true
  - label: Create Checklist
    agent: speckit.checklist
    prompt: Create a checklist for the following domain...
---

## User Input

```text
$ARGUMENTS
```

입력이 비어있지 않다면 반드시 고려한 뒤 진행합니다.

## 한국어 작성 규칙

- 사용자에게 보여주는 설명/질문/보고는 한국어로 작성합니다.
- 명령어, 파일 경로, 파일명, 코드 블록, 고정 토큰(`NEEDS CLARIFICATION`, `N/A`)은 그대로 유지합니다.

## Outline

1. **Setup**
   - 저장소 루트에서 `.specify/scripts/bash/setup-plan.sh --json` 실행
   - JSON에서 `FEATURE_SPEC`, `IMPL_PLAN`, `SPECS_DIR`, `BRANCH` 파싱
2. **Load context**
   - `FEATURE_SPEC`, `.specify/memory/constitution.md`, `IMPL_PLAN` 템플릿 읽기
3. **Execute plan workflow**
   - `Technical Context` 채우기 (`NEEDS CLARIFICATION` 표기 허용)
   - 헌장 기반 `Constitution Check` 채우기
   - 게이트 위반 시 즉시 `ERROR`
   - Phase 0: `research.md` 작성 (`NEEDS CLARIFICATION` 모두 해소)
   - Phase 1: `data-model.md`, `contracts/`, `quickstart.md` 생성
   - Phase 1: `.specify/scripts/bash/update-agent-context.sh codex` 실행
   - 설계 후 `Constitution Check` 재평가
4. **Stop and report**
   - Phase 2 계획 완료 시 종료
   - 브랜치, `IMPL_PLAN` 경로, 생성 산출물 보고

## Phases

### Phase 0: Outline & Research

1. Technical Context에서 미해결 항목 추출
   - `NEEDS CLARIFICATION` -> 조사 과제
   - 의존성 -> 모범 사례 조사
   - 통합 지점 -> 패턴 조사
2. 조사 결과를 `research.md`에 기록
   - Decision
   - Rationale
   - Alternatives considered

**Output**: 모든 `NEEDS CLARIFICATION`이 해소된 `research.md`

### Phase 1: Design & Contracts

**Prerequisites:** `research.md` 완료

1. 엔티티/필드/관계/검증 규칙을 `data-model.md`에 정리
2. 외부 인터페이스가 있으면 `/contracts/`에 계약 문서화
   - 라이브러리 API, CLI command schema, 웹 서비스 endpoint 등
3. 에이전트 컨텍스트 업데이트
   - `.specify/scripts/bash/update-agent-context.sh codex`
   - 현재 계획에서 새로 추가된 기술만 반영
   - 수동 추가 구간은 보존

**Output**: `data-model.md`, `contracts/*`, `quickstart.md`, 에이전트 컨텍스트 파일

## Key rules

- 절대 경로를 사용합니다.
- 게이트 실패 또는 미해결 항목이 남아 있으면 `ERROR`로 중단합니다.
