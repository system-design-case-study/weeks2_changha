# Implementation Plan: [FEATURE]

**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**: [link]
**Input**: `/specs/[###-feature-name]/spec.md`의 기능 명세

**Note**: 이 템플릿은 `/speckit.plan` 명령이 채웁니다. 실행 흐름은 `.specify/templates/plan-template.md`를 따릅니다.

## Summary

[기능 명세에서 핵심 요구사항과 research 결과 기반 기술 접근 요약]

## Technical Context

<!--
  ACTION REQUIRED: 이 섹션을 실제 기술 맥락으로 교체하세요.
-->

**Language/Version**: [e.g., Python 3.11, Swift 5.9, Rust 1.75 or NEEDS CLARIFICATION]
**Primary Dependencies**: [e.g., FastAPI, UIKit, LLVM or NEEDS CLARIFICATION]
**Storage**: [if applicable, e.g., PostgreSQL, CoreData, files or N/A]
**Testing**: [e.g., pytest, XCTest, cargo test or NEEDS CLARIFICATION]
**Target Platform**: [e.g., Linux server, iOS 15+, WASM or NEEDS CLARIFICATION]
**Project Type**: [e.g., library/cli/web-service/mobile-app/compiler/desktop-app or NEEDS CLARIFICATION]
**Performance Goals**: [도메인 성능 목표 또는 NEEDS CLARIFICATION]
**Constraints**: [도메인 제약 또는 NEEDS CLARIFICATION]
**Scale/Scope**: [규모/범위 또는 NEEDS CLARIFICATION]

## Constitution Check

*GATE: Phase 0 연구 전 반드시 통과, Phase 1 설계 후 재검증*

[헌장 기준으로 산정한 게이트]

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
└── tasks.md
```

### Source Code (repository root)

<!--
  ACTION REQUIRED: 아래 예시 트리를 실제 구조로 교체하세요.
  사용하지 않는 옵션은 제거하세요.
-->

```text
# [REMOVE IF UNUSED] Option 1: Single project (DEFAULT)
src/
├── models/
├── services/
├── cli/
└── lib/

tests/
├── contract/
├── integration/
└── unit/

# [REMOVE IF UNUSED] Option 2: Web application
backend/
├── src/
│   ├── models/
│   ├── services/
│   └── api/
└── tests/

frontend/
├── src/
│   ├── components/
│   ├── pages/
│   └── services/
└── tests/

# [REMOVE IF UNUSED] Option 3: Mobile + API
api/
└── [same as backend above]

ios/ or android/
└── [platform-specific structure]
```

**Structure Decision**: [선택한 구조와 실제 디렉터리 근거]

## Complexity Tracking

> **Constitution 위반을 정당화해야 할 때만 작성**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [예: 4번째 프로젝트] | [필요 사유] | [대안이 부족한 이유] |
| [예: Repository 패턴] | [문제] | [직접 접근이 어려운 이유] |
