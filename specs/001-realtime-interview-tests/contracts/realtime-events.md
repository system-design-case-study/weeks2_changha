# Contract: Friend Location Realtime Events

## WebSocket Endpoint

- Endpoint: `ws://localhost:8080/ws/location`
- Auth: `Authorization: Bearer <token>`

## Client -> Server

### location_update

```json
{
  "type": "location_update",
  "updateId": "upd-001",
  "userId": "user-1",
  "lat": 37.1234,
  "lon": 127.5678,
  "clientTs": "2026-02-22T15:10:00Z",
  "sequence": 42
}
```

Validation:
- 인증된 사용자만 허용
- payload 크기 제한 적용
- rate limit 적용

### heartbeat

```json
{
  "type": "heartbeat",
  "clientTs": "2026-02-22T15:10:05Z"
}
```

## Server -> Client

### friend_location

```json
{
  "type": "friend_location",
  "updateId": "upd-001",
  "userId": "user-1",
  "lat": 37.1234,
  "lon": 127.5678,
  "serverTs": "2026-02-22T15:10:00Z"
}
```

### error

```json
{
  "type": "error",
  "code": "FORBIDDEN",
  "message": "Not allowed to receive this location"
}
```

## Redis Pub/Sub Channel

- Channel: `location.friend.update`

Message body:

```json
{
  "updateId": "upd-001",
  "userId": "user-1",
  "lat": 37.1234,
  "lon": 127.5678,
  "serverTs": "2026-02-22T15:10:00Z",
  "sequence": 42
}
```

Rules:
- 동일 `updateId` 재수신 시 idempotent 처리
- 최신 이벤트 우선 규칙 적용
