# Implementation Plan: 친구 실시간 위치 전달(WebSocket + Redis Pub/Sub)

**Branch**: `001-realtime-interview-tests` | **Date**: 2026-02-22 | **Spec**: `/Users/changha/Documents/26-1-quarter/weeks2_changha/specs/001-realtime-interview-tests/spec.md`
**Input**: `/Users/changha/Documents/26-1-quarter/weeks2_changha/specs/001-realtime-interview-tests/spec.md`의 기능 명세

## Summary

2장 주제에 맞춰 사용자 위치를 WebSocket으로 수신하고 친구에게 실시간 전파한다.
단일 인스턴스 전달과 다중 인스턴스(Redis Pub/Sub) 전파를 모두 지원하며, 로컬 MacBook에서 반복 가능한 테스트로 지연/성공률/복구 성능을 검증한다.

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 4.x, spring-boot-starter-webmvc, spring-boot-starter-websocket, spring-data-redis, Micrometer
**Storage**: Redis (Pub/Sub + 최신 위치 캐시), 영속 DB는 현재 범위에서 N/A
**Testing**: JUnit 5, Spring Boot Test, WebSocket integration test, Redis integration test, k6 또는 JMeter
**Target Platform**: macOS 로컬 개발 환경
**Project Type**: web-service
**Performance Goals**: 위치 전달 p95 <= 2초, 전달 성공률 >= 99%, 재연결 성공률 >= 95%
**Constraints**: 제한된 로컬 자원(CPU/메모리), 학습 목적 우선, 대규모 트래픽 비대상
**Scale/Scope**: 동시 연결 20/50/100, 사용자당 1~5 update/s, 인스턴스 2개 + Redis 1개

## Constitution Check

*GATE: Phase 0 연구 전 반드시 통과, Phase 1 설계 후 재검증*

- 지연 최소화 우선: PASS
- 친구 관계 기반 접근 제어: PASS
- WebSocket + Redis Pub/Sub 분리 원칙: PASS
- 결과적 일관성과 최신성 규칙: PASS
- 재현 가능한 실험 중심: PASS

Phase 1 산출물 작성 후 재검증: PASS

## Project Structure

### Documentation (this feature)

```text
/Users/changha/Documents/26-1-quarter/weeks2_changha/specs/001-realtime-interview-tests/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── realtime-events.md
└── tasks.md
```

### Source Code (repository root)

```text
/Users/changha/Documents/26-1-quarter/weeks2_changha/
├── src/
│   ├── main/
│   │   ├── java/com/systemdesign/location/
│   │   │   ├── auth/
│   │   │   ├── friend/
│   │   │   ├── location/
│   │   │   ├── websocket/
│   │   │   ├── pubsub/
│   │   │   └── metrics/
│   │   └── resources/
│   └── test/
│       ├── java/com/systemdesign/location/
│       │   ├── unit/
│       │   ├── integration/
│       │   └── contract/
│       └── resources/
└── specs/
```

**Structure Decision**: Spring Boot 단일 서비스 구조에서 위치 수집/친구 전파/인증/메트릭 경계를 패키지로 분리한다.

## Complexity Tracking

현재 헌장 위반 없음.
