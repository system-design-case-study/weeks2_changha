# Redis Output Buffer Batch Report

- Generated At: 2026-03-03 15:59:45 +0900
- Runs: 5
- Redis: 127.0.0.1:6379 (db=0, version=7.2.6)
- Channel: test:chan
- Payload Bytes: 2048
- Publish Count: 10000
- Sample Interval (s): 0.1
- Monitor Seconds: 30
- Subscriber SO_RCVBUF: 4096
- Test Buffer Limit: `normal 0 0 0 replica 256mb 64mb 60 pubsub 8mb 2mb 10`

## Summary

- Pass: 5/5 (100.0%)
- Warn: 0, Fail: 0
- Disconnect Observed: 5/5
- Peak omem(max): 8256960
- Peak omem(avg): 6145344
- Peak obl(max): 1024
- Peak oll(max): 488
- Avg Duration(ms): 1263

## Run Results

| Run | Status | Exit | Disconnected | Max OBL | Max OLL | Max OMEM | Duration(ms) | Started At | Finished At |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | PASS | 0 | true | 0 | 414 | 7004880 | 1296 | 2026-03-03 15:59:35 +0900 | 2026-03-03 15:59:36 +0900 |
| 2 | PASS | 0 | true | 0 | 0 | 0 | 1168 | 2026-03-03 15:59:37 +0900 | 2026-03-03 15:59:38 +0900 |
| 3 | PASS | 0 | true | 1024 | 449 | 7597080 | 1277 | 2026-03-03 15:59:39 +0900 | 2026-03-03 15:59:41 +0900 |
| 4 | PASS | 0 | true | 1024 | 488 | 8256960 | 1276 | 2026-03-03 15:59:42 +0900 | 2026-03-03 15:59:43 +0900 |
| 5 | PASS | 0 | true | 1024 | 465 | 7867800 | 1299 | 2026-03-03 15:59:44 +0900 | 2026-03-03 15:59:45 +0900 |
