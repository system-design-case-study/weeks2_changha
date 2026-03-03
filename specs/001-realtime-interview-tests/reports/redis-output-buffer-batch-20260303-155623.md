# Redis Output Buffer Batch Report

- Generated At: 2026-03-03 15:56:30 +0900
- Runs: 3
- Redis: 127.0.0.1:6379 (db=0, version=7.2.6)
- Channel: test:chan
- Payload Bytes: 2048
- Publish Count: 10000
- Sample Interval (s): 0.1
- Monitor Seconds: 30
- Subscriber SO_RCVBUF: 4096
- Test Buffer Limit: `normal 0 0 0 replica 256mb 64mb 60 pubsub 8mb 2mb 10`

## Summary

- Pass: 3/3 (100.0%)
- Warn: 0, Fail: 0
- Disconnect Observed: 3/3
- Peak omem(max): 6700320
- Peak omem(avg): 6000960
- Peak obl(max): 1024
- Peak oll(max): 396
- Avg Duration(ms): 1320

## Run Results

| Run | Status | Exit | Disconnected | Max OBL | Max OLL | Max OMEM | Duration(ms) | Started At | Finished At |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | PASS | 0 | true | 1024 | 357 | 6040440 | 1372 | 2026-03-03 15:56:23 +0900 | 2026-03-03 15:56:25 +0900 |
| 2 | PASS | 0 | true | 0 | 311 | 5262120 | 1280 | 2026-03-03 15:56:26 +0900 | 2026-03-03 15:56:27 +0900 |
| 3 | PASS | 0 | true | 1024 | 396 | 6700320 | 1308 | 2026-03-03 15:56:28 +0900 | 2026-03-03 15:56:30 +0900 |
