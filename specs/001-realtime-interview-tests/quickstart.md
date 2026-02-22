# Quickstart: Friend Location Realtime (Local)

## 1) Prerequisites

- Java 21
- Redis local 실행
- Gradle wrapper (`./gradlew`)
- 부하 테스트 도구(k6 또는 JMeter)

## 2) Redis 실행

```bash
brew services start redis
```

또는

```bash
docker run --name local-redis -p 6379:6379 -d redis:7
```

## 3) 애플리케이션 실행

```bash
./gradlew bootRun
```

멀티 인스턴스 실험:
- 인스턴스1: `SERVER_PORT=8080`
- 인스턴스2: `SERVER_PORT=8081`

## 4) 기능 테스트

1. A/B를 친구로 설정하고 둘 다 연결
2. A 위치 갱신 전송
3. B 수신 확인(지연 측정)
4. 비친구 C는 미수신 확인

## 5) 신뢰성 테스트

- Redis 중단/재시작 후 복구 확인
- 서버 재시작 후 클라이언트 재연결/재동기화 확인

## 6) 성능 테스트 프로파일

- S: 20 connections, 1 update/s
- M: 50 connections, 2 update/s
- L: 100 connections, 5 update/s

지표:
- delivery success rate
- p50/p95/p99 latency
- reconnect success rate
- error counts
