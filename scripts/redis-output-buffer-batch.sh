#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

RUNS="${RUNS:-5}"
SLEEP_BETWEEN="${SLEEP_BETWEEN:-1}"

REDIS_HOST="${REDIS_HOST:-127.0.0.1}"
REDIS_PORT="${REDIS_PORT:-6379}"
REDIS_DB="${REDIS_DB:-0}"

PROFILE="${PROFILE:-custom}"

case "$PROFILE" in
  stress)
    DEFAULT_CHANNEL="test:chan"
    DEFAULT_PAYLOAD_BYTES="2048"
    DEFAULT_PUBLISH_COUNT="10000"
    DEFAULT_SAMPLE_INTERVAL="0.1"
    DEFAULT_MONITOR_SECONDS="30"
    DEFAULT_SUB_RCVBUF="4096"
    DEFAULT_TEST_BUFFER_LIMIT="normal 0 0 0 replica 256mb 64mb 60 pubsub 8mb 2mb 10"
    ;;
  realistic-location)
    DEFAULT_CHANNEL="location:update"
    DEFAULT_PAYLOAD_BYTES="128"
    DEFAULT_PUBLISH_COUNT="10000"
    DEFAULT_SAMPLE_INTERVAL="0.1"
    DEFAULT_MONITOR_SECONDS="30"
    DEFAULT_SUB_RCVBUF="4096"
    DEFAULT_TEST_BUFFER_LIMIT="normal 0 0 0 replica 256mb 64mb 60 pubsub 32mb 8mb 60"
    ;;
  custom)
    DEFAULT_CHANNEL="test:chan"
    DEFAULT_PAYLOAD_BYTES="2048"
    DEFAULT_PUBLISH_COUNT="10000"
    DEFAULT_SAMPLE_INTERVAL="0.1"
    DEFAULT_MONITOR_SECONDS="30"
    DEFAULT_SUB_RCVBUF="4096"
    DEFAULT_TEST_BUFFER_LIMIT="normal 0 0 0 replica 256mb 64mb 60 pubsub 8mb 2mb 10"
    ;;
  *)
    echo "[ERROR] unsupported PROFILE: ${PROFILE}" >&2
    echo "        Supported profiles: stress, realistic-location, custom" >&2
    exit 1
    ;;
esac

CHANNEL="${CHANNEL:-$DEFAULT_CHANNEL}"
PAYLOAD_BYTES="${PAYLOAD_BYTES:-$DEFAULT_PAYLOAD_BYTES}"
PUBLISH_COUNT="${PUBLISH_COUNT:-$DEFAULT_PUBLISH_COUNT}"
SAMPLE_INTERVAL="${SAMPLE_INTERVAL:-$DEFAULT_SAMPLE_INTERVAL}"
MONITOR_SECONDS="${MONITOR_SECONDS:-$DEFAULT_MONITOR_SECONDS}"
SUB_RCVBUF="${SUB_RCVBUF:-$DEFAULT_SUB_RCVBUF}"
TEST_BUFFER_LIMIT="${TEST_BUFFER_LIMIT:-$DEFAULT_TEST_BUFFER_LIMIT}"

REPORT_DIR="${REPORT_DIR:-specs/001-realtime-interview-tests/reports}"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[ERROR] required command not found: $1" >&2
    exit 1
  fi
}

require_cmd redis-cli
require_cmd python3
require_cmd awk
require_cmd date

if [[ "$(redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" -n "$REDIS_DB" PING 2>/dev/null || true)" != "PONG" ]]; then
  echo "[ERROR] Redis is not reachable at ${REDIS_HOST}:${REDIS_PORT}" >&2
  exit 1
fi

mkdir -p "$REPORT_DIR"

timestamp="$(date '+%Y%m%d-%H%M%S')"
report_file="${REPORT_DIR}/redis-output-buffer-batch-${timestamp}.md"
tmp_csv="$(mktemp)"
trap 'rm -f "$tmp_csv"' EXIT

echo "run,status,exit_code,disconnected,max_obl,max_oll,max_omem,started_at,finished_at,duration_ms" >"$tmp_csv"

echo "[INFO] Running ${RUNS} iterations..."
echo "[INFO] Profile: ${PROFILE}"
for run in $(seq 1 "$RUNS"); do
  started_at="$(date '+%Y-%m-%d %H:%M:%S %z')"
  start_epoch_ms="$(python3 - <<'PY'
import time
print(int(time.time() * 1000))
PY
)"

  run_log="$(mktemp)"
  set +e
  REDIS_HOST="$REDIS_HOST" \
  REDIS_PORT="$REDIS_PORT" \
  REDIS_DB="$REDIS_DB" \
  CHANNEL="$CHANNEL" \
  PROFILE="$PROFILE" \
  PAYLOAD_BYTES="$PAYLOAD_BYTES" \
  PUBLISH_COUNT="$PUBLISH_COUNT" \
  SAMPLE_INTERVAL="$SAMPLE_INTERVAL" \
  MONITOR_SECONDS="$MONITOR_SECONDS" \
  SUB_RCVBUF="$SUB_RCVBUF" \
  TEST_BUFFER_LIMIT="$TEST_BUFFER_LIMIT" \
  ./scripts/redis-output-buffer-test.sh >"$run_log" 2>&1
  exit_code=$?
  set -e

  finish_epoch_ms="$(python3 - <<'PY'
import time
print(int(time.time() * 1000))
PY
)"
  finished_at="$(date '+%Y-%m-%d %H:%M:%S %z')"
  duration_ms=$((finish_epoch_ms - start_epoch_ms))

  result_line="$(grep '^RESULT ' "$run_log" || true)"

  status="FAIL"
  disconnected="false"
  max_obl="0"
  max_oll="0"
  max_omem="0"

  if [[ -n "$result_line" ]]; then
    status="$(echo "$result_line" | awk '{for(i=1;i<=NF;i++){if($i ~ /^status=/){split($i,a,"="); print a[2]}}}')"
    disconnected="$(echo "$result_line" | awk '{for(i=1;i<=NF;i++){if($i ~ /^disconnected=/){split($i,a,"="); print a[2]}}}')"
    max_obl="$(echo "$result_line" | awk '{for(i=1;i<=NF;i++){if($i ~ /^max_obl=/){split($i,a,"="); print a[2]}}}')"
    max_oll="$(echo "$result_line" | awk '{for(i=1;i<=NF;i++){if($i ~ /^max_oll=/){split($i,a,"="); print a[2]}}}')"
    max_omem="$(echo "$result_line" | awk '{for(i=1;i<=NF;i++){if($i ~ /^max_omem=/){split($i,a,"="); print a[2]}}}')"
  elif grep -q '\[PASS\]' "$run_log"; then
    status="PASS"
  elif grep -q '\[WARN\]' "$run_log"; then
    status="WARN"
  fi

  echo "${run},${status},${exit_code},${disconnected},${max_obl},${max_oll},${max_omem},${started_at},${finished_at},${duration_ms}" >>"$tmp_csv"

  echo "[RUN ${run}/${RUNS}] status=${status} exit=${exit_code} disconnected=${disconnected} max_omem=${max_omem}"

  rm -f "$run_log"
  if [[ "$run" -lt "$RUNS" ]]; then
    sleep "$SLEEP_BETWEEN"
  fi
done

summary="$(python3 - "$tmp_csv" <<'PY'
import csv
import statistics
import sys

path = sys.argv[1]
rows = []
with open(path, newline="") as f:
    reader = csv.DictReader(f)
    rows = list(reader)

total = len(rows)
pass_count = sum(1 for r in rows if r["status"] == "PASS")
warn_count = sum(1 for r in rows if r["status"] == "WARN")
fail_count = sum(1 for r in rows if r["status"] == "FAIL")
disconnect_count = sum(1 for r in rows if r["disconnected"] == "true")

omem_values = [int(r["max_omem"]) for r in rows]
obl_values = [int(r["max_obl"]) for r in rows]
oll_values = [int(r["max_oll"]) for r in rows]
duration_values = [int(r["duration_ms"]) for r in rows]

def safe_mean(values):
    return int(statistics.mean(values)) if values else 0

print(f"total={total}")
print(f"pass_count={pass_count}")
print(f"warn_count={warn_count}")
print(f"fail_count={fail_count}")
print(f"disconnect_count={disconnect_count}")
print(f"pass_rate={pass_count / total * 100:.1f}" if total else "pass_rate=0.0")
print(f"max_omem_peak={max(omem_values) if omem_values else 0}")
print(f"avg_omem_peak={safe_mean(omem_values)}")
print(f"max_obl_peak={max(obl_values) if obl_values else 0}")
print(f"max_oll_peak={max(oll_values) if oll_values else 0}")
print(f"avg_duration_ms={safe_mean(duration_values)}")
PY
)"

eval "$summary"

redis_version="$(redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" -n "$REDIS_DB" INFO server \
  | awk -F: '/^redis_version:/{gsub(/\r/,"",$2); print $2; exit}')"

{
  echo "# Redis Output Buffer Batch Report"
  echo
  echo "- Generated At: $(date '+%Y-%m-%d %H:%M:%S %z')"
  echo "- Runs: ${RUNS}"
  echo "- Profile: ${PROFILE}"
  echo "- Redis: ${REDIS_HOST}:${REDIS_PORT} (db=${REDIS_DB}, version=${redis_version})"
  echo "- Channel: ${CHANNEL}"
  echo "- Payload Bytes: ${PAYLOAD_BYTES}"
  echo "- Publish Count: ${PUBLISH_COUNT}"
  echo "- Sample Interval (s): ${SAMPLE_INTERVAL}"
  echo "- Monitor Seconds: ${MONITOR_SECONDS}"
  echo "- Subscriber SO_RCVBUF: ${SUB_RCVBUF}"
  echo "- Test Buffer Limit: \`${TEST_BUFFER_LIMIT}\`"
  echo
  echo "## Summary"
  echo
  echo "- Pass: ${pass_count}/${total} (${pass_rate}%)"
  echo "- Warn: ${warn_count}, Fail: ${fail_count}"
  echo "- Disconnect Observed: ${disconnect_count}/${total}"
  echo "- Peak omem(max): ${max_omem_peak}"
  echo "- Peak omem(avg): ${avg_omem_peak}"
  echo "- Peak obl(max): ${max_obl_peak}"
  echo "- Peak oll(max): ${max_oll_peak}"
  echo "- Avg Duration(ms): ${avg_duration_ms}"
  echo
  echo "## Run Results"
  echo
  echo "| Run | Status | Exit | Disconnected | Max OBL | Max OLL | Max OMEM | Duration(ms) | Started At | Finished At |"
  echo "| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |"
  tail -n +2 "$tmp_csv" \
    | awk -F',' '{printf "| %s | %s | %s | %s | %s | %s | %s | %s | %s | %s |\n", $1,$2,$3,$4,$5,$6,$7,$10,$8,$9}'
} >"$report_file"

echo "[DONE] Batch report generated: ${report_file}"
echo "[SUMMARY] pass=${pass_count}/${total}, disconnect=${disconnect_count}/${total}, max_omem=${max_omem_peak}"
