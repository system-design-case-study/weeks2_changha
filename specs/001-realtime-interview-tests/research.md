# Phase 0 Research: 친구 위치 실시간 전파

## Decision 1: 위치 업데이트 전송은 WebSocket 채널 사용

- Decision: 클라이언트는 위치 업데이트를 WebSocket으로 서버에 전송한다.
- Rationale: 짧은 주기의 양방향 갱신에 적합하고 연결 상태 추적이 쉽다.
- Alternatives considered:
  - HTTP polling: 구현은 단순하지만 지연/비용 증가.
  - SSE: 서버->클라이언트 단방향이어서 위치 업로드에 부적합.

## Decision 2: 인스턴스 간 전파는 Redis Pub/Sub

- Decision: 교차 인스턴스 친구 전파는 Redis Pub/Sub 채널로 처리한다.
- Rationale: 로컬 환경에서 설정이 간단하고 멀티 인스턴스 학습에 충분하다.
- Alternatives considered:
  - Redis Streams/Kafka: 내구성은 높지만 현재 학습 범위 대비 복잡도 과다.

## Decision 3: 결과적 일관성 + 최신 이벤트 우선

- Decision: 동일 사용자 위치 이벤트는 `server_ts` 또는 `sequence` 기준으로 최신 이벤트만 반영한다.
- Rationale: 네트워크 지연/재전송으로 생기는 역순/중복 이벤트를 단순하게 처리 가능.
- Alternatives considered:
  - 강한 순서 보장 큐: 단순 학습 프로젝트에는 과도한 복잡도.

## Decision 4: 친구 관계 검증은 연결 시 + 이벤트 처리 시 이중 검증

- Decision: handshake 인증 후에도 이벤트 처리 시 친구 관계를 재검증한다.
- Rationale: 세션 중 권한 변경/우회 요청을 방어할 수 있다.
- Alternatives considered:
  - 연결 시 1회 검증: 구현 단순하지만 동적 권한 변경에 취약.

## Decision 5: 로컬 성능 검증은 고정 프로파일(20/50/100)

- Decision: 연결 수와 갱신 빈도를 고정 프로파일로 표준화한다.
- Rationale: 회귀 비교가 쉬워지고 15분 이내 테스트 완료 목표를 관리하기 쉽다.
- Alternatives considered:
  - 랜덤 부하: 재현성/비교 가능성 저하.
