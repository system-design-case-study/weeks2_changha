# Redis Output Buffer Batch Report

- Generated At: 2026-03-03 16:13:56 +0900
- Runs: 5
- Profile: realistic-location
- Redis: 127.0.0.1:6379 (db=0, version=7.2.6)
- Channel: location:update
- Payload Bytes: 128
- Publish Count: 10000
- Sample Interval (s): 0.1
- Monitor Seconds: 30
- Subscriber SO_RCVBUF: 4096
- Test Buffer Limit: `normal 0 0 0 replica 256mb 64mb 60 pubsub 32mb 8mb 60`

## Summary

- Pass: 0/5 (0.0%)
- Warn: 5, Fail: 0
- Disconnect Observed: 0/5
- Peak omem(max): 1099800
- Peak omem(avg): 1099800
- Peak obl(max): 0
- Peak oll(max): 65
- Avg Duration(ms): 40023

## Run Results

| Run | Status | Exit | Disconnected | Max OBL | Max OLL | Max OMEM | Duration(ms) | Started At | Finished At |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | WARN | 2 | false | 0 | 65 | 1099800 | 40255 | 2026-03-03 16:10:31 +0900 | 2026-03-03 16:11:11 +0900 |
| 2 | WARN | 2 | false | 0 | 65 | 1099800 | 41078 | 2026-03-03 16:11:12 +0900 | 2026-03-03 16:11:54 +0900 |
| 3 | WARN | 2 | false | 0 | 65 | 1099800 | 40410 | 2026-03-03 16:11:55 +0900 | 2026-03-03 16:12:35 +0900 |
| 4 | WARN | 2 | false | 0 | 65 | 1099800 | 39252 | 2026-03-03 16:12:36 +0900 | 2026-03-03 16:13:15 +0900 |
| 5 | WARN | 2 | false | 0 | 65 | 1099800 | 39123 | 2026-03-03 16:13:16 +0900 | 2026-03-03 16:13:56 +0900 |
