# Tasks: 친구 실시간 위치 전달(WebSocket + Redis Pub/Sub)

**Input**: `/Users/changha/Documents/26-1-quarter/weeks2_changha/specs/001-realtime-interview-tests/`의 설계 문서  
**Prerequisites**: plan.md (필수), spec.md (필수), research.md, data-model.md, contracts/, quickstart.md

**Tests**: 이 기능은 테스트가 핵심 요구사항이므로 각 사용자 스토리에 테스트 작업을 포함한다.

**Organization**: 사용자 스토리 단위로 그룹화해 독립 구현/검증이 가능하도록 구성한다.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 병렬 수행 가능
- **[Story]**: 연결된 사용자 스토리 (`[US1]`, `[US2]`, `[US3]`)
- 설명에는 정확한 파일 경로를 포함

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 WebSocket/Redis/Actuator 테스트 의존성을 `build.gradle`에 추가
- [ ] T002 위치 도메인 패키지 기본 구조를 `src/main/java/com/systemdesign/location/`에 생성
- [ ] T003 [P] 로컬 실행 설정을 `src/main/resources/application.properties`에 추가
- [ ] T004 [P] 로컬 Redis 실행 구성을 `docker-compose.yml`에 추가

---

## Phase 2: Foundational (Blocking Prerequisites)

- [ ] T005 공통 WebSocket 인증 인터셉터를 `src/main/java/com/systemdesign/location/auth/WsAuthHandshakeInterceptor.java`에 구현
- [ ] T006 [P] 친구 관계 조회 인터페이스와 기본 구현을 `src/main/java/com/systemdesign/location/friend/FriendRelationService.java`와 `src/main/java/com/systemdesign/location/friend/InMemoryFriendRelationService.java`에 구현
- [ ] T007 [P] 사용자 연결 상태 레지스트리를 `src/main/java/com/systemdesign/location/location/UserPresenceRegistry.java`에 구현
- [ ] T008 최신 위치 캐시 저장소를 `src/main/java/com/systemdesign/location/location/LatestLocationCache.java`에 구현
- [ ] T009 위치 이벤트 검증기(크기/속도 제한)를 `src/main/java/com/systemdesign/location/location/LocationUpdateValidator.java`에 구현
- [ ] T010 Redis Pub/Sub 공통 설정을 `src/main/java/com/systemdesign/location/pubsub/RedisPubSubConfig.java`에 구현
- [ ] T011 WebSocket 엔드포인트 설정을 `src/main/java/com/systemdesign/location/websocket/WebSocketConfig.java`에 구현
- [ ] T012 공통 메트릭 수집 컴포넌트를 `src/main/java/com/systemdesign/location/metrics/LocationMetrics.java`에 구현
- [ ] T013 공통 오류 응답/로깅 유틸을 `src/main/java/com/systemdesign/location/websocket/WsErrorResponseFactory.java`에 구현

**Checkpoint**: 이후 사용자 스토리 구현 가능

---

## Phase 3: User Story 1 - 내 위치를 친구에게 실시간 전달 (Priority: P1) 🎯 MVP

**Goal**: 인증된 사용자의 위치를 친구에게만 실시간 전달하고 비친구 누수를 차단  
**Independent Test**: A/B 친구 연결 상태에서 전달 성공, 비친구 C 미수신 검증

### Tests for User Story 1

- [ ] T014 [P] [US1] WebSocket 이벤트 계약 테스트를 `src/test/java/com/systemdesign/location/contract/LocationWebSocketContractTest.java`에 작성
- [ ] T015 [P] [US1] 친구/비친구 전달 통합 테스트를 `src/test/java/com/systemdesign/location/integration/FriendLocationDeliveryIntegrationTest.java`에 작성
- [ ] T016 [P] [US1] 최신 이벤트 우선 규칙 단위 테스트를 `src/test/java/com/systemdesign/location/unit/LocationOrderingRuleTest.java`에 작성

### Implementation for User Story 1

- [ ] T017 [P] [US1] 위치 이벤트 모델을 `src/main/java/com/systemdesign/location/location/model/LocationUpdateEvent.java`에 구현
- [ ] T018 [P] [US1] 친구 전파 대상 계산기를 `src/main/java/com/systemdesign/location/location/FriendFanoutResolver.java`에 구현
- [ ] T019 [US1] 위치 갱신 처리 서비스를 `src/main/java/com/systemdesign/location/location/LocationUpdateService.java`에 구현
- [ ] T020 [US1] WebSocket 핸들러를 `src/main/java/com/systemdesign/location/websocket/LocationWebSocketHandler.java`에 구현
- [ ] T021 [US1] 친구 전파 디스패처를 `src/main/java/com/systemdesign/location/location/FriendLocationDispatcher.java`에 구현
- [ ] T022 [US1] 사용자 연결/해제 이벤트 처리를 `src/main/java/com/systemdesign/location/websocket/ConnectionLifecycleHandler.java`에 구현

---

## Phase 4: User Story 2 - Redis Pub/Sub 기반 다중 인스턴스 전파 (Priority: P2)

**Goal**: 서로 다른 인스턴스에 연결된 친구 간 위치 이벤트 전파 보장  
**Independent Test**: 2인스턴스+Redis 구성에서 교차 인스턴스 전달 및 복구 검증

### Tests for User Story 2

- [ ] T023 [P] [US2] Redis 메시지 계약 테스트를 `src/test/java/com/systemdesign/location/contract/RedisLocationEventContractTest.java`에 작성
- [ ] T024 [P] [US2] 교차 인스턴스 전파 통합 테스트를 `src/test/java/com/systemdesign/location/integration/RedisFanoutIntegrationTest.java`에 작성
- [ ] T025 [P] [US2] Redis 단절/복구 통합 테스트를 `src/test/java/com/systemdesign/location/integration/RedisRecoveryIntegrationTest.java`에 작성

### Implementation for User Story 2

- [ ] T026 [P] [US2] Redis 전파 메시지 DTO를 `src/main/java/com/systemdesign/location/pubsub/LocationPubSubMessage.java`에 구현
- [ ] T027 [US2] Redis 발행기를 `src/main/java/com/systemdesign/location/pubsub/LocationEventPublisher.java`에 구현
- [ ] T028 [US2] Redis 구독기를 `src/main/java/com/systemdesign/location/pubsub/LocationEventSubscriber.java`에 구현
- [ ] T029 [US2] Redis 수신 이벤트 브리지 서비스를 `src/main/java/com/systemdesign/location/pubsub/RedisToWebSocketBridge.java`에 구현
- [ ] T030 [US2] Redis 장애 처리기를 `src/main/java/com/systemdesign/location/pubsub/PubSubFailureHandler.java`에 구현

---

## Phase 5: User Story 3 - 로컬 학습용 신뢰성/성능 테스트 자동화 (Priority: P3)

**Goal**: 로컬에서 반복 가능한 부하/장애 실험과 리포트 자동화  
**Independent Test**: 20/50/100 연결 프로파일 실행 후 지표 리포트 생성 검증

### Tests for User Story 3

- [ ] T031 [P] [US3] 로드 테스트 프로파일 스크립트를 `tests/perf/location-load.js`에 작성
- [ ] T032 [P] [US3] 신뢰성 테스트 스크립트를 `tests/perf/location-recovery.js`에 작성
- [ ] T033 [P] [US3] 보안 테스트 시나리오를 `tests/perf/location-security.js`에 작성

### Implementation for User Story 3

- [ ] T034 [US3] 테스트 리포트 집계기를 `src/main/java/com/systemdesign/location/metrics/TestRunReportService.java`에 구현
- [ ] T035 [US3] 메트릭/리포트 내보내기 설정을 `src/main/resources/application.properties`에 추가
- [ ] T036 [US3] 로컬 테스트 실행 스크립트를 `scripts/run-location-tests.sh`에 작성
- [ ] T037 [US3] 기준 리포트 저장 경로 생성을 `specs/001-realtime-interview-tests/reports/.gitkeep`에 추가
- [ ] T038 [US3] 실행/측정 절차를 `specs/001-realtime-interview-tests/quickstart.md`에 보강

---

## Phase 6: Polish & Cross-Cutting Concerns

- [ ] T039 [P] 문서 가이드를 `HELP.md`에 업데이트
- [ ] T040 무권한 접근 회귀 테스트를 `src/test/java/com/systemdesign/location/integration/WebSocketAuthSecurityIntegrationTest.java`에 추가
- [ ] T041 전체 테스트 실행 파이프라인을 `build.gradle`에 정리
- [ ] T042 성능 기준선 리포트를 `specs/001-realtime-interview-tests/reports/baseline.md`에 기록

---

## Dependencies & Execution Order

### Phase Dependencies

- Phase 1 -> Phase 2 -> Phase 3/4/5 -> Phase 6

### User Story Dependencies

- **US1**: Phase 2 완료 후 바로 착수 가능 (MVP)
- **US2**: Phase 2 + US1 기본 흐름 완료 후 착수 권장
- **US3**: US1/US2 구현 완료 후 착수 권장

### Within Each User Story

- 테스트 작성 -> 모델/서비스 구현 -> 통합/회귀 검증 순서로 진행

### Parallel Opportunities

- Phase 1: T003, T004 병렬 가능
- Phase 2: T006, T007, T012 병렬 가능
- US1: T014, T015, T016, T017, T018 병렬 가능
- US2: T023, T024, T025, T026 병렬 가능
- US3: T031, T032, T033 병렬 가능

---

## Parallel Example: User Story 1

```bash
Task: "Contract test in src/test/java/com/systemdesign/location/contract/LocationWebSocketContractTest.java"
Task: "Integration test in src/test/java/com/systemdesign/location/integration/FriendLocationDeliveryIntegrationTest.java"
Task: "Location event model in src/main/java/com/systemdesign/location/location/model/LocationUpdateEvent.java"
Task: "Fanout resolver in src/main/java/com/systemdesign/location/location/FriendFanoutResolver.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1 완료
2. Phase 2 완료
3. Phase 3 완료
4. US1 독립 검증 완료 후 데모 가능
