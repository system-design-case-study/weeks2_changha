---
description: "기능 구현을 위한 작업 목록 템플릿"
---

# Tasks: [FEATURE NAME]

**Input**: `/specs/[###-feature-name]/`의 설계 문서
**Prerequisites**: plan.md (필수), spec.md (필수), research.md, data-model.md, contracts/

**Tests**: 아래 예시는 테스트 작업을 포함합니다. 테스트는 필수가 아니며, 명시적으로 요청된 경우에만 포함합니다.

**Organization**: 사용자 스토리 단위로 그룹화해 독립 구현/검증이 가능해야 합니다.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 병렬 수행 가능
- **[Story]**: 연결된 사용자 스토리 (US1, US2, US3...)
- 설명에는 정확한 파일 경로를 포함

## Path Conventions

- **Single project**: `src/`, `tests/`
- **Web app**: `backend/src/`, `frontend/src/`
- **Mobile**: `api/src/`, `ios/src/` 또는 `android/src/`

<!--
  중요: 아래 작업은 예시입니다. 생성 시 실제 작업으로 대체해야 합니다.
-->

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 구현 계획에 맞는 프로젝트 구조 생성
- [ ] T002 [language]/[framework] 의존성 초기화
- [ ] T003 [P] 린트/포맷터 설정

---

## Phase 2: Foundational (Blocking Prerequisites)

- [ ] T004 DB 스키마/마이그레이션 기반 설정
- [ ] T005 [P] 인증/인가 프레임워크 구현
- [ ] T006 [P] API 라우팅/미들웨어 뼈대 구성
- [ ] T007 공통 베이스 모델/엔티티 작성
- [ ] T008 공통 오류 처리/로깅 구성
- [ ] T009 환경 설정 관리 구성

**Checkpoint**: 이후 사용자 스토리 구현 가능

---

## Phase 3: User Story 1 - [Title] (Priority: P1) 🎯 MVP

**Goal**: [이 스토리의 전달 가치]
**Independent Test**: [독립 검증 방법]

### Tests for User Story 1 (OPTIONAL)

- [ ] T010 [P] [US1] 계약 테스트 작성
- [ ] T011 [P] [US1] 통합 테스트 작성

### Implementation for User Story 1

- [ ] T012 [P] [US1] 모델 작성
- [ ] T013 [P] [US1] 모델 작성
- [ ] T014 [US1] 서비스 구현
- [ ] T015 [US1] 엔드포인트/기능 구현
- [ ] T016 [US1] 검증/예외 처리 추가
- [ ] T017 [US1] 로깅 추가

---

## Phase 4: User Story 2 - [Title] (Priority: P2)

**Goal**: [이 스토리의 전달 가치]
**Independent Test**: [독립 검증 방법]

### Tests for User Story 2 (OPTIONAL)

- [ ] T018 [P] [US2] 계약 테스트 작성
- [ ] T019 [P] [US2] 통합 테스트 작성

### Implementation for User Story 2

- [ ] T020 [P] [US2] 모델 작성
- [ ] T021 [US2] 서비스 구현
- [ ] T022 [US2] 엔드포인트/기능 구현
- [ ] T023 [US2] 필요 시 US1 컴포넌트 연동

---

## Phase 5: User Story 3 - [Title] (Priority: P3)

**Goal**: [이 스토리의 전달 가치]
**Independent Test**: [독립 검증 방법]

### Tests for User Story 3 (OPTIONAL)

- [ ] T024 [P] [US3] 계약 테스트 작성
- [ ] T025 [P] [US3] 통합 테스트 작성

### Implementation for User Story 3

- [ ] T026 [P] [US3] 모델 작성
- [ ] T027 [US3] 서비스 구현
- [ ] T028 [US3] 엔드포인트/기능 구현

---

## Phase N: Polish & Cross-Cutting Concerns

- [ ] TXXX [P] 문서 업데이트
- [ ] TXXX 코드 정리/리팩터링
- [ ] TXXX 전체 성능 개선
- [ ] TXXX [P] 추가 단위 테스트
- [ ] TXXX 보안 보강
- [ ] TXXX quickstart.md 검증

---

## Dependencies & Execution Order

### Phase Dependencies

- Setup(Phase 1) -> Foundational(Phase 2) -> User Stories(Phase 3+) -> Polish

### User Story Dependencies

- US1: Foundational 완료 후 시작 가능
- US2: Foundational 완료 후 시작 가능 (US1 연동 가능)
- US3: Foundational 완료 후 시작 가능 (US1/US2 연동 가능)

### Within Each User Story

- 테스트(선택) 먼저 작성 후 실패 확인
- 모델 -> 서비스 -> 엔드포인트 -> 통합 순서

### Parallel Opportunities

- [P] 표시는 병렬 실행 가능
- Foundational 완료 후 스토리 병렬 진행 가능

---

## Parallel Example: User Story 1

```bash
Task: "Contract test for [endpoint] in tests/contract/test_[name].py"
Task: "Integration test for [user journey] in tests/integration/test_[name].py"
Task: "Create [Entity1] model in src/models/[entity1].py"
Task: "Create [Entity2] model in src/models/[entity2].py"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1 완료
2. Phase 2 완료
3. Phase 3 완료
4. User Story 1 독립 검증
5. 필요 시 배포/데모
