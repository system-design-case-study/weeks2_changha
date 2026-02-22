# Data Model: Friend Location Realtime

## 1) UserPresence

- Purpose: 사용자 연결 및 활성 상태 추적
- Fields:
  - `userId` (string)
  - `connectionId` (string)
  - `status` (CONNECTED, DISCONNECTED, RECONNECTING)
  - `lastHeartbeatAt` (timestamp)

## 2) FriendRelation

- Purpose: 위치 전파 권한의 기준
- Fields:
  - `userId` (string)
  - `friendId` (string)
  - `relationStatus` (ACTIVE, BLOCKED, REMOVED)

## 3) LocationUpdate

- Purpose: 사용자 위치 갱신 이벤트
- Fields:
  - `updateId` (string)
  - `userId` (string)
  - `lat` (double)
  - `lon` (double)
  - `clientTs` (timestamp)
  - `serverTs` (timestamp)
  - `sequence` (long, optional)

## 4) FanoutDelivery

- Purpose: 친구별 전달 결과 추적
- Fields:
  - `updateId` (string)
  - `targetUserId` (string)
  - `deliveryStatus` (DELIVERED, FAILED, DROPPED)
  - `deliveredAt` (timestamp)
  - `errorReason` (string, optional)

## 5) TestScenario

- Purpose: 반복 가능한 테스트 조건 정의
- Fields:
  - `scenarioId` (string)
  - `connectionCount` (20, 50, 100)
  - `updateRatePerUser` (1~5)
  - `durationSeconds` (int)
  - `failureInjectionType` (NONE, REDIS_DOWN, SERVER_RESTART, NETWORK_JITTER)

## 6) TestRunReport

- Purpose: 테스트 결과 비교
- Fields:
  - `runId` (string)
  - `scenarioId` (string)
  - `deliverySuccessRate` (decimal)
  - `latencyP50Ms` (long)
  - `latencyP95Ms` (long)
  - `latencyP99Ms` (long)
  - `reconnectSuccessRate` (decimal)
  - `errorCounts` (map<string,int>)
  - `createdAt` (timestamp)
