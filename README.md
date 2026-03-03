# Weeks2 Realtime Interview Tests

## 개요
이 프로젝트는 "주변 친구 최신 위치 업데이트" 시나리오를 대상으로, 실시간 전송 경로와 느린 소비자(backpressure) 상황을 로컬에서 재현하고 운영 기준을 정리하기 위한 실험 저장소입니다.

핵심 전송 데이터는 아래 최소 집합을 기준으로 합니다.

- `userId`: 위치 주체 식별자
- `latitude`: 위도 (`-90 ~ 90`)
- `longitude`: 경도 (`-180 ~ 180`)
- `timestamp`: 위치 측정 시각(권장: UTC epoch millis)

블로그 작성 링크 : [시스템 설계2 - 2장](https://changha-dev.github.io/posts/system-architecture-2/)

위 블로그를 읽고 Redis pub/sub 과정에서 client에 보내기 위한 outputBuffer가 있다는 것을 알았습니다.  
이 부분에 대한 buffer limit에서 어떻게 동작하는 지와 로컬에서 테스트했을 때 soft limit, hard limit의 임계점이 어느정도인지 파악해보았습니다.

## 진행 목적
- 친구 관계 기반으로 위치 이벤트가 정확히 전달되는지 검증
- 멀티 인스턴스 환경(Socket.IO + Redis backplane)에서 전파 일관성 확인
- 느린 구독자 상황에서 Redis `client-output-buffer-limit` 보호 동작 확인
- 실서비스에 가까운 payload 크기에서 임계치(언제 disconnect 되는지) 수치화

## 현재 구현 범위
- Spring Boot 기반 실시간 서버
  - Spring WebSocket 경로 + 백프레셔 정책 컴포넌트
  - Socket.IO 서버(netty-socketio) 통합
  - 친구 관계 경계(권한) + 인증 인터셉터
- 로컬 멀티 인스턴스 검증
  - `docker-compose`로 `redis + app1 + app2` 실행
  - cross-instance friend 전파, non-friend 차단, 무권한 연결 차단 E2E
- Redis 내부 출력 버퍼 테스트 자동화
  - 단일 실행: `scripts/redis-output-buffer-test.sh`
  - 배치 + Markdown 보고서 생성: `scripts/redis-output-buffer-batch.sh`

## 왜 `PAYLOAD_BYTES=256`을 realistic 기준으로 썼는가
실제 위치 이벤트(JSON compact 직렬화) 길이를 기준으로 여유 있는 상한을 잡았다.

- `{userId, latitude, longitude, timestamp}`: 약 `82 ~ 120 bytes`
- `accuracy`, `speed` 추가 케이스: 약 `157 bytes`
- 결론: `256 bytes`는 위치 이벤트 실무 payload를 커버하는 보수적 기준

## 주요 실험 결과
아래 결과는 2026-03-03(Asia/Seoul) 로컬 실험 기준이다.

### 1) realistic-location 기본 배치
- 설정:
  - `PROFILE=realistic-location`
  - `PAYLOAD_BYTES=256`
  - `PUBLISH_COUNT=10000`
  - `TEST_BUFFER_LIMIT='normal 0 0 0 replica 256mb 64mb 60 pubsub 32mb 8mb 60'`
  - `RUNS=5`
- 결과:
  - `disconnect=0/5`
  - `peak omem=2,385,720`
- 해석:
  - 실서비스 근사 부하(10k burst)에서는 Redis pubsub output buffer 한계까지 가지 않음
- 보고서:
  - `specs/001-realtime-interview-tests/reports/redis-output-buffer-batch-20260303-161550.md`

### 2) Hard limit 임계치 (`32mb`) 탐색
- 조건:
  - `PAYLOAD_BYTES=256`, `MONITOR_SECONDS=12`
  - `client-output-buffer-limit pubsub 32mb 8mb 60`
- 관측:
  - `112000` publish: 유지(WARN, 5/5)
  - `113000` publish: disconnect(PASS, 5/5)
- 결론:
  - 현재 환경 hard-limit 전이 구간은 `112k ~ 113k` messages

### 3) Soft limit 임계치 (`8mb for 60s`) 탐색
- 조건:
  - `PAYLOAD_BYTES=256`, `MONITOR_SECONDS=70`, `SAMPLE_INTERVAL=1`
  - 동일 limit: `pubsub 32mb 8mb 60`
- 관측:
  - `28000`: `max_omem=7,867,800`, 70초 내 disconnect 없음(WARN)
  - `30000`: `max_omem=8,460,000`, 약 62초에 disconnect(PASS)
  - `32000`: `max_omem=9,052,200`, 약 63초에 disconnect(PASS)
- 결론:
  - soft-limit 전이 구간은 대략 `28k ~ 30k` messages
  - "8MB 초과 상태가 약 60초 유지"되면 disconnect

## `pubsub 32mb 8mb 60` 해석
Redis `client-output-buffer-limit`의 Pub/Sub 클라이언트 규칙이다.

- hard: `32mb`를 한 번이라도 넘으면 즉시 끊김
- soft: `8mb` 초과 상태가 `60초` 연속 유지되면 끊김


## 실행 방법
사전 준비:
- Redis 실행 (`127.0.0.1:6379`)
- Java 21
- Python3
- (멀티 인스턴스 E2E는 Docker 필요)

### 단일 Redis output-buffer 테스트
```bash
PROFILE=realistic-location \
PAYLOAD_BYTES=256 \
PUBLISH_COUNT=30000 \
SAMPLE_INTERVAL=1 \
MONITOR_SECONDS=70 \
./scripts/redis-output-buffer-test.sh
```

### 배치 실행 + 보고서 생성
```bash
PROFILE=realistic-location \
PAYLOAD_BYTES=256 \
PUBLISH_COUNT=10000 \
RUNS=5 \
./scripts/redis-output-buffer-batch.sh
```

보고서 출력 경로:
- `specs/001-realtime-interview-tests/reports/`

