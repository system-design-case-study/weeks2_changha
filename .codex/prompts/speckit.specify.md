---
description: 자연어 기능 설명으로부터 feature specification을 생성하거나 갱신합니다.
handoffs:
  - label: Build Technical Plan
    agent: speckit.plan
    prompt: Create a plan for the spec. I am building with...
  - label: Clarify Spec Requirements
    agent: speckit.clarify
    prompt: Clarify specification requirements
    send: true
---

## User Input

```text
$ARGUMENTS
```

입력이 비어있지 않다면 반드시 고려한 뒤 진행합니다.

## 한국어 작성 규칙

- 사용자에게 보여주는 설명/질문/보고는 한국어로 작성합니다.
- 명령어, 경로, 파일명, 코드 블록, 토큰(`FR-001`, `SC-001`, `NEEDS CLARIFICATION`)은 유지합니다.

## Outline

`/speckit.specify` 뒤에 입력된 텍스트가 기능 설명입니다.
비어있지 않으면 다시 물어보지 말고 바로 진행합니다.

1. 브랜치 short name(2~4 단어) 생성
   - 핵심 키워드 기반, 가능한 action-noun 형식
   - 기술 용어/약어(OAuth2, API, JWT 등) 보존
2. 기존 브랜치/스펙 번호 확인 후 신규 번호 계산
   - remote/local/specs 디렉터리에서 최고 번호 탐색
3. 아래 스크립트를 **한 번만** 실행:

   ```bash
   .specify/scripts/bash/create-new-feature.sh --json --number <N> --short-name "<short-name>" "<feature description>"
   ```

   - JSON 출력의 `BRANCH_NAME`, `SPEC_FILE`를 기준으로 후속 작업
4. `.specify/templates/spec-template.md`를 기준으로 명세 작성
5. 품질 검증 체크리스트 생성 및 검증 루프 수행

## Spec Writing Rules

- 사용자 가치와 비즈니스 요구(WHAT/WHY) 중심
- 구현 상세(HOW: 언어/프레임워크/API) 금지
- 모호한 경우 합리적 기본값을 사용하되, 필요 시만 `NEEDS CLARIFICATION` 표기
- `NEEDS CLARIFICATION`은 최대 3개
- 우선순위: scope > security/privacy > UX > technical detail

## Quality Validation

`FEATURE_DIR/checklists/requirements.md`를 생성하고 아래를 점검합니다.

- 구현 상세가 스펙에 유출되지 않았는가
- 모든 필수 섹션이 채워졌는가
- 요구사항이 테스트 가능하고 모호하지 않은가
- 성공 기준이 측정 가능하고 기술 중립적인가
- 시나리오/엣지케이스/가정/의존성이 충분한가

검증 처리:

1. 실패 항목(clarification 제외)이 있으면 spec 수정 후 재검증 (최대 3회)
2. `NEEDS CLARIFICATION`이 남아 있으면 질문 묶음 제시
   - 질문당 A/B/C/Custom 옵션 표
   - 사용자 응답을 받아 spec에 반영
3. 최종 체크리스트 상태 업데이트

## Completion Report

다음을 보고합니다.

- 브랜치명
- `spec.md` 경로
- 체크리스트 결과
- 다음 권장 단계 (`/speckit.clarify` 또는 `/speckit.plan`)

## Guardrails

- `create-new-feature.sh`는 feature당 1회만 실행
- 스펙 섹션 순서/헤더를 유지
- 사용자 입력이 없는 경우에만 오류 반환
