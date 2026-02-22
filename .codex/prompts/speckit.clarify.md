---
description: 현재 spec의 모호하거나 누락된 지점을 최대 5개의 질문으로 명확화하고 spec에 반영합니다.
handoffs:
  - label: Build Technical Plan
    agent: speckit.plan
    prompt: Create a plan for the spec. I am building with...
---

## User Input

```text
$ARGUMENTS
```

입력이 비어있지 않다면 반드시 고려한 뒤 진행합니다.

## 한국어 작성 규칙

- 사용자 질문/요약/보고는 한국어로 작성합니다.
- 명령어, 경로, 파일명, 코드 블록, 고정 토큰(`NEEDS CLARIFICATION`, `N/A`)은 유지합니다.

## Goal

활성 feature spec의 모호성과 누락 결정을 줄이고, 결정 내용을 `spec.md`에 직접 반영합니다.

## Execution Steps

1. 저장소 루트에서 1회 실행:

   ```bash
   .specify/scripts/bash/check-prerequisites.sh --json --paths-only
   ```

   - `FEATURE_DIR`, `FEATURE_SPEC` 파싱
   - 실패 시 `/speckit.specify` 재실행 안내
2. `FEATURE_SPEC` 로드 후 진단
   - 상태를 `Clear / Partial / Missing`으로 분류
   - 우선순위: scope > security/privacy > UX > technical detail
3. 질문 생성
   - 최대 5개 (기본 3개 + 필요 시 2개)
   - 사용자가 이미 명시한 내용은 다시 묻지 않음
   - 결정이 구현/범위/테스트 전략에 실질 영향이 있을 때만 질문
4. 사용자 응답 반영
   - `NEEDS CLARIFICATION` 마커를 구체 값으로 교체
   - 충돌되는 문장도 함께 정리
5. 검증
   - 명세가 일관되고 테스트 가능한 문장인지 확인
6. 결과 보고
   - 수정된 파일 경로
   - 해결된 쟁점 수
   - 남은 리스크(있다면)

## Question Design Guide

질문 유형:

- 범위 경계(포함/제외)
- 위험 우선순위(보안/성능/데이터 손실 등)
- 깊이 수준(가벼운 검토 vs 릴리스 게이트)
- 독자/리뷰 맥락(작성자/리뷰어/QA)
- 예외/복구 시나리오 포함 여부

## Guardrails

- 모호한 기본값 남발 금지
- 불필요한 질문 금지
- 합의된 답변은 spec에 즉시 반영
- spec의 다른 섹션과 충돌하지 않게 동기화
