---
description: 기존 tasks를 의존성 순서의 실행 가능한 GitHub 이슈로 변환합니다.
tools: ['github/github-mcp-server/issue_write']
---

## User Input

```text
$ARGUMENTS
```

입력이 비어있지 않다면 반드시 고려한 뒤 진행합니다.

## 한국어 작성 규칙

- 사용자 보고는 한국어로 작성합니다.
- 명령어/경로/URL/리모트 식별값은 원문 그대로 유지합니다.

## Outline

1. 저장소 루트에서 아래 명령 실행:

   ```bash
   .specify/scripts/bash/check-prerequisites.sh --json --require-tasks --include-tasks
   ```

   - JSON에서 `FEATURE_DIR`, `AVAILABLE_DOCS`, `tasks` 경로를 파싱
2. Git remote 확인:

   ```bash
   git config --get remote.origin.url
   ```

3. remote가 **GitHub URL일 때만** 진행
4. tasks 항목 각각에 대해 GitHub MCP를 사용해 이슈 생성

> [!CAUTION]
> REMOTE URL과 다른 저장소에는 절대로 이슈를 생성하지 마세요.
