# 친구 실시간 위치 공유 시스템 헌장 (Constitution)

## Core Principles

### 지연 최소화 우선
친구 위치 공유의 핵심은 빠른 체감 응답이다.
- 목표: `위치 갱신 -> 친구 화면 반영` p95 <= 2초 (로컬 환경)
- 혼잡 시 완전한 최신성보다 빠른 응답을 우선한다.

### 친구 관계 기반 접근 제어
위치 데이터는 친구 관계가 확인된 사용자에게만 전달한다.
- 인증되지 않은 연결은 차단한다.
- 친구 관계가 없는 사용자에게 위치를 전파하지 않는다.

### WebSocket + Redis Pub/Sub 분리 원칙
단일 노드 내 실시간 전달은 WebSocket, 다중 인스턴스 간 전파는 Redis Pub/Sub로 분리한다.
- Pub/Sub는 best-effort(at-most-once)로 간주한다.
- 유실 가능성은 다음 주기 갱신과 재동기화로 복구한다.

### 결과적 일관성과 최신성 규칙
위치 정보는 순간적으로 stale할 수 있음을 허용한다.
- 동일 사용자 이벤트 충돌 시 최신 규칙(`server_ts` 또는 `sequence`)으로 수렴한다.
- 사용자에게는 위치와 마지막 갱신 시각을 함께 제공한다.

### 재현 가능한 실험 중심
학습 목적상 모든 핵심 시나리오는 로컬에서 반복 실행 가능해야 한다.
- 기능/신뢰성/성능/보안 테스트를 자동 또는 반자동으로 수행한다.
- 전달 성공률, 지연 분포, 재연결 성공률, 오류율을 리포트로 남긴다.

## Data & Event Model

### Canonical State
- 사용자별 최신 위치는 캐시(Redis 또는 메모리 캐시)에 저장한다.
- 장기 위치 이력 저장은 현재 범위 밖이다.

### Event Schema
위치 이벤트 최소 필드:
- `userId`
- `lat`, `lon`
- `client_ts`
- `server_ts`
- `sequence` (선택)
- `roomOrChannel` (친구 전파 채널 식별)

### Ordering / Idempotency
- 동일 `userId` 기준으로 더 최신 이벤트만 반영한다.
- 중복 이벤트 재처리는 상태 불일치를 만들면 안 된다.

## Performance & Reliability Guardrails

### Fanout Control
- 위치 변경 시 전파 대상은 활성 친구로 제한한다.
- 필요 시 이동거리 임계치, 최소 전송 간격, 샘플링을 적용한다.

### Failure Handling
- Redis 단절/지연, 서버 재시작, 일시 네트워크 오류에서 프로세스는 생존해야 한다.
- 재연결 후 구독 복구 및 최신 위치 재동기화를 수행한다.

### Safety Limits
- payload 크기 제한과 rate limit을 적용한다.
- 비정상 트래픽은 차단하고 원인을 로그/메트릭으로 남긴다.

## Governance

### Compatibility
- 이벤트 스키마는 하위 호환(필드 추가 허용, 의미 변경/삭제 금지)을 원칙으로 한다.

### Observability Requirements
필수 메트릭:
- WebSocket 연결 수/실패율/재연결 성공률
- 위치 갱신 수신 QPS, 처리 지연 p50/p95/p99
- Redis publish/subscribe 오류율
- 친구 전파 성공률/실패율
- stale 위치 비율(허용 범위 모니터링)

### Test Gate
최소 통과 조건:
- 기능: 친구 간 위치 전달 성공 + 비친구 전파 차단
- 신뢰성: Redis 단절/복구, 서버 재시작 후 재동기화
- 성능: 로컬 프로파일(20/50/100 connections)에서 p95 목표 만족
- 보안: 무인증/무권한 접근 차단

### Release Policy
- 학습 프로젝트라도 변경 전후 회귀 테스트를 수행한다.
- 기준 미달 시 기능 확장보다 원인 분석 및 안정화 작업을 우선한다.

**Version**: 1.1.0 | **Ratified**: 2026-02-22 | **Last Amended**: 2026-02-22
