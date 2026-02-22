---
description: 입력값 또는 대화 맥락을 사용해 프로젝트 헌장을 생성/갱신하고, 연관 산출물과 동기화합니다.
handoffs:
  - label: Build Specification
    agent: speckit.specify
    prompt: Implement the feature specification based on the updated constitution. I want to build...
---

## User Input

```text
$ARGUMENTS
```

입력이 비어있지 않다면 반드시 고려한 뒤 진행합니다.

## 한국어 작성 규칙

- 사용자에게 보여주는 설명/질문/결과 보고는 한국어로 작성합니다.
- 명령어, 경로, 파일명, 코드 블록, 플레이스홀더 토큰(`[PROJECT_NAME]` 등)은 유지합니다.

## Outline

당신은 `.specify/memory/constitution.md`를 갱신합니다.
이 파일은 대괄호 플레이스홀더를 포함한 템플릿입니다.
목표는 다음 3가지입니다.

1. 플레이스홀더 값을 수집/추론
2. 헌장 본문을 정확히 치환
3. 변경 사항을 관련 문서에 반영

## Execution flow

1. `.specify/memory/constitution.md` 로드
   - `[ALL_CAPS_IDENTIFIER]` 패턴 플레이스홀더 전수 확인
   - 원칙 개수는 템플릿 기본값보다 적거나 많을 수 있으므로 사용자 요구를 우선
2. 값 수집/추론
   - 사용자 입력 우선
   - 없으면 저장소 문맥(README, docs, 기존 문서)에서 추론
   - 날짜 규칙:
     - `RATIFICATION_DATE`: 최초 채택일(모르면 TODO 또는 사용자 확인)
     - `LAST_AMENDED_DATE`: 변경 시 오늘 날짜, 아니면 기존 유지
   - 버전 규칙(`CONSTITUTION_VERSION`, semver):
     - MAJOR: 호환 불가 원칙 변경/삭제
     - MINOR: 원칙/섹션 추가, 가이드의 실질적 확장
     - PATCH: 문구 명확화/오탈자/비의미적 수정
3. 헌장 초안 작성
   - 플레이스홀더를 실제 내용으로 치환
   - 의도적으로 남긴 토큰이 있으면 이유를 명시
4. 연동 문서 동기화
   - 템플릿/프롬프트/체크리스트 등 헌장 의존 산출물에 필요한 반영 수행
5. 결과 보고
   - 변경 요약
   - 버전 변경 사유
   - 후속 권장 작업

## Guardrails

- 의미 없는 포맷 변경은 피하고 의미 변경만 반영합니다.
- 결정이 모호하면 추정 근거를 먼저 제시하고 확정합니다.
- 헌장 자체의 공백/모순이 남지 않게 완결된 문서로 만듭니다.
