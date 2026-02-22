---
description: 설계 산출물을 기반으로 실행 가능한 의존성 순서의 tasks.md를 생성합니다.
handoffs:
  - label: Analyze For Consistency
    agent: speckit.analyze
    prompt: Run a project analysis for consistency
    send: true
  - label: Implement Project
    agent: speckit.implement
    prompt: Start the implementation in phases
    send: true
---

## User Input

```text
$ARGUMENTS
```

입력이 비어있지 않다면 반드시 고려한 뒤 진행합니다.

## 한국어 작성 규칙

- 설명/보고는 한국어로 작성합니다.
- 명령어, 파일 경로, 코드 블록, ID 토큰(`T001`, `[US1]`, `[P]`)은 유지합니다.

## Outline

1. **Setup**
   - `.specify/scripts/bash/check-prerequisites.sh --json` 실행
   - `FEATURE_DIR`, `AVAILABLE_DOCS` 파싱 (절대 경로 사용)
2. **Load design documents** (`FEATURE_DIR`)
   - 필수: `plan.md`, `spec.md`
   - 선택: `data-model.md`, `contracts/`, `research.md`, `quickstart.md`
3. **Task generation workflow**
   - 기술 스택/구조, 사용자 스토리 우선순위(P1/P2/P3) 추출
   - 엔티티/계약/연구 결과를 스토리에 매핑
   - 스토리별 작업 생성
   - 의존성 그래프와 병렬 실행 포인트 생성
   - 각 스토리가 독립 검증 가능하도록 완결성 점검
4. **Generate tasks.md**
   - `.specify/templates/tasks-template.md` 구조를 따름
   - Phase 1: Setup
   - Phase 2: Foundational
   - Phase 3+: 사용자 스토리별 단계(우선순위 순)
   - Final Phase: Polish & Cross-Cutting
5. **Report**
   - 생성 경로, 총 작업 수, 스토리별 작업 수, 병렬 포인트, MVP 범위 보고

## Task Generation Rules

**핵심 원칙**: 작업은 사용자 스토리 중심으로 조직하여 독립 구현/검증 가능해야 합니다.

### Checklist Format (필수)

모든 작업은 아래 형식을 엄격히 따릅니다.

```text
- [ ] [TaskID] [P?] [Story?] Description with file path
```

형식 규칙:

1. 체크박스: 항상 `- [ ]`
2. Task ID: `T001`, `T002` ...
3. `[P]`: 병렬 가능 작업에만 표시
4. `[Story]`: 스토리 단계에서만 필수 (`[US1]`, `[US2]` ...)
5. 설명: 반드시 정확한 파일 경로 포함

예시:

- `- [ ] T001 Create project structure per implementation plan`
- `- [ ] T005 [P] Implement authentication middleware in src/middleware/auth.py`
- `- [ ] T012 [P] [US1] Create User model in src/models/user.py`
- `- [ ] T014 [US1] Implement UserService in src/services/user_service.py`

## Phase Structure

- Phase 1: Setup
- Phase 2: Foundational (모든 스토리의 선행 조건)
- Phase 3+: 사용자 스토리별(우선순위 순)
- Final Phase: Polish

스토리 내부 권장 순서:

- (선택) 테스트 -> 모델 -> 서비스 -> 엔드포인트 -> 통합

## Guardrails

- 애매한 작업명 금지: 파일 단위로 명확히 작성
- 범용 태스크 남발 금지: 스토리 가치에 직접 연결
- 의존성 누락 금지: 선행/후행 관계를 문서화
