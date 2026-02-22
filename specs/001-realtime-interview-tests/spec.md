# Feature Specification: 친구 실시간 위치 전달(WebSocket + Redis Pub/Sub)

**Feature Branch**: `001-realtime-interview-tests`
**Created**: 2026-02-22
**Status**: Draft
**Input**: 사용자 설명: "2장 주제인 본인 위치와 친구 간 WebSocket 기반 위치 전달을 구현하고 Redis Pub/Sub를 포함해 테스트한다."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 내 위치를 친구에게 실시간 전달 (Priority: P1)

사용자는 자신의 위치를 업데이트하고, 온라인 상태의 친구가 2초 이내에 최신 위치를 수신한다.

**Why this priority**: 2장의 핵심 시나리오이며 제품의 최소 가치다.

**Independent Test**: 사용자 A/B를 친구로 연결 후 A 위치 갱신 이벤트를 발행하고 B 수신 지연/정확도를 검증한다.

**Acceptance Scenarios**:

1. **Given** A와 B가 친구이며 둘 다 연결된 상태, **When** A가 위치를 갱신하면, **Then** B는 2초 이내 A의 최신 위치를 수신한다.
2. **Given** A와 C가 친구가 아닌 상태, **When** A가 위치를 갱신하면, **Then** C는 A 위치를 수신하지 못한다.

---

### User Story 2 - Redis Pub/Sub 기반 다중 인스턴스 전파 (Priority: P2)

A와 B가 서로 다른 서버 인스턴스에 연결되어도 위치 이벤트가 Redis Pub/Sub를 통해 전달된다.

**Why this priority**: 단일 노드 테스트만으로는 확장 경로를 검증할 수 없다.

**Independent Test**: 인스턴스 2개 + Redis 1개 구성에서 교차 인스턴스 전달 성공률과 지연을 측정한다.

**Acceptance Scenarios**:

1. **Given** A는 인스턴스1, B는 인스턴스2에 연결된 상태, **When** A가 위치를 갱신하면, **Then** B가 동일 이벤트를 수신한다.
2. **Given** Redis 연결이 일시 단절된 상태, **When** 위치 갱신이 발생하면, **Then** 서버는 비정상 종료하지 않고 복구 후 처리 재개한다.

---

### User Story 3 - 로컬 학습용 신뢰성/성능 테스트 자동화 (Priority: P3)

MacBook 로컬 환경에서 고정 프로파일 부하와 장애 주입 테스트를 반복 실행해 수치 기반으로 설계를 검증한다.

**Why this priority**: 학습 목표 달성을 위해 재현 가능한 실험 루프가 필요하다.

**Independent Test**: 20/50/100 연결 프로파일 실행 후 성공률/지연/재연결률 리포트 생성 여부를 검증한다.

**Acceptance Scenarios**:

1. **Given** 테스트 프로파일이 정의된 상태, **When** 테스트를 실행하면, **Then** 전달 성공률과 p95 지연이 포함된 리포트가 생성된다.
2. **Given** 서버 재시작 장애가 주입된 상태, **When** 클라이언트가 자동 재연결을 시도하면, **Then** 정해진 시간 내 복구 성공률이 목표를 만족한다.

---

### Edge Cases

- 동일 사용자 위치 이벤트가 중복 또는 역순으로 도착하면 어떤 규칙으로 최신 상태를 확정하는가?
- 클라이언트가 과도한 빈도/대용량 payload를 보내면 어떻게 제한하는가?
- Redis Pub/Sub 유실이 발생했을 때 다음 주기 갱신으로 어떻게 수렴시키는가?
- 친구 관계가 변경(차단/해제)되었을 때 기존 구독 전파를 어떻게 즉시 차단하는가?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST accept user location updates over WebSocket from authenticated users.
- **FR-002**: System MUST deliver a user's latest location only to authorized friend recipients.
- **FR-003**: System MUST prevent location leakage to non-friend or unauthorized users.
- **FR-004**: System MUST propagate location updates across instances using Redis Pub/Sub.
- **FR-005**: System MUST recover from transient Redis or network failures without process crash.
- **FR-006**: System MUST resolve duplicate or out-of-order location events with a deterministic latest-event rule.
- **FR-007**: System MUST enforce payload-size and update-rate limits per connection.
- **FR-008**: System MUST support reconnect and re-subscribe flow for disconnected clients.
- **FR-009**: System MUST provide repeatable local tests for functional, reliability, performance, and security scenarios.
- **FR-010**: System MUST produce measurable reports including delivery success, latency percentiles, reconnect success, and error counts.

### Key Entities *(include if feature involves data)*

- **UserPresence**: 사용자 연결 상태. userId, connectionId, status, lastHeartbeatAt.
- **FriendRelation**: 친구 관계/권한 정보. userId, friendId, relationStatus.
- **LocationUpdate**: 위치 이벤트. userId, lat, lon, clientTs, serverTs, sequence.
- **FanoutDelivery**: 친구별 전달 결과. updateId, targetUserId, status, deliveredAt, errorReason.
- **TestScenario**: 테스트 프로파일. scenarioId, connectionCount, updateRate, duration, failureType.
- **TestRunReport**: 테스트 결과. runId, successRate, latencyP95, reconnectSuccessRate, errorSummary.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 로컬 단일 인스턴스(동시 연결 50, 사용자당 2 update/s)에서 전달 성공률 99% 이상.
- **SC-002**: 로컬 2인스턴스 + Redis 구성에서 위치 전달 p95 지연 2초 이하.
- **SC-003**: Redis 단절/복구 시나리오에서 프로세스 비정상 종료 0건.
- **SC-004**: 무권한 사용자 위치 접근 시도 차단율 100%.
- **SC-005**: 기본 테스트 세트(기능/신뢰성/성능/보안) 15분 이내 완료.
