# Redis Output Buffer Batch Report

- Generated At: 2026-03-03 16:19:23 +0900
- Runs: 5
- Profile: realistic-location
- Redis: 127.0.0.1:6379 (db=0, version=7.2.6)
- Channel: location:update
- Payload Bytes: 256
- Publish Count: 10000
- Sample Interval (s): 0.1
- Monitor Seconds: 30
- Subscriber SO_RCVBUF: 4096
- Test Buffer Limit: `normal 0 0 0 replica 256mb 64mb 60 pubsub 32mb 8mb 60`

## Summary

- Pass: 0/5 (0.0%)
- Warn: 5, Fail: 0
- Disconnect Observed: 0/5
- Peak omem(max): 2385720
- Peak omem(avg): 2385720
- Peak obl(max): 0
- Peak oll(max): 141
- Avg Duration(ms): 41677

## Run Results

| Run | Status | Exit | Disconnected | Max OBL | Max OLL | Max OMEM | Duration(ms) | Started At | Finished At |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | WARN | 2 | false | 0 | 141 | 2385720 | 41428 | 2026-03-03 16:15:50 +0900 | 2026-03-03 16:16:32 +0900 |
| 2 | WARN | 2 | false | 0 | 141 | 2385720 | 42365 | 2026-03-03 16:16:33 +0900 | 2026-03-03 16:17:15 +0900 |
| 3 | WARN | 2 | false | 0 | 141 | 2385720 | 42992 | 2026-03-03 16:17:16 +0900 | 2026-03-03 16:17:59 +0900 |
| 4 | WARN | 2 | false | 0 | 141 | 2385720 | 40722 | 2026-03-03 16:18:00 +0900 | 2026-03-03 16:18:41 +0900 |
| 5 | WARN | 2 | false | 0 | 141 | 2385720 | 40879 | 2026-03-03 16:18:42 +0900 | 2026-03-03 16:19:23 +0900 |
