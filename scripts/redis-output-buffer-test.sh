#!/usr/bin/env bash

set -euo pipefail

REDIS_HOST="${REDIS_HOST:-127.0.0.1}"
REDIS_PORT="${REDIS_PORT:-6379}"
REDIS_DB="${REDIS_DB:-0}"

PROFILE="${PROFILE:-custom}"

case "$PROFILE" in
  stress)
    DEFAULT_CHANNEL="test:chan"
    DEFAULT_PAYLOAD_BYTES="4096"
    DEFAULT_PUBLISH_COUNT="20000"
    DEFAULT_SAMPLE_INTERVAL="0.5"
    DEFAULT_MONITOR_SECONDS="20"
    DEFAULT_SUB_RCVBUF="4096"
    DEFAULT_TEST_BUFFER_LIMIT="normal 0 0 0 replica 256mb 64mb 60 pubsub 1mb 256kb 10"
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
    DEFAULT_PAYLOAD_BYTES="4096"
    DEFAULT_PUBLISH_COUNT="20000"
    DEFAULT_SAMPLE_INTERVAL="0.5"
    DEFAULT_MONITOR_SECONDS="20"
    DEFAULT_SUB_RCVBUF="4096"
    DEFAULT_TEST_BUFFER_LIMIT="normal 0 0 0 replica 256mb 64mb 60 pubsub 1mb 256kb 10"
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

SUB_PID=""
PUB_PID=""
ORIGINAL_LIMIT=""
SUB_INFO_FILE=""

log() {
  printf '[%s] %s\n' "$(date +%H:%M:%S)" "$*"
}

redis_cli() {
  redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" -n "$REDIS_DB" "$@"
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[ERROR] required command not found: $1" >&2
    exit 1
  fi
}

field_value() {
  local line="$1"
  local key="$2"
  awk -v line="$line" -v key="$key" '
    BEGIN {
      n = split(line, parts, " ");
      for (i = 1; i <= n; i++) {
        split(parts[i], kv, "=");
        if (kv[1] == key) {
          print kv[2];
          exit;
        }
      }
    }
  '
}

cleanup() {
  set +e

  if [[ -n "$PUB_PID" ]] && kill -0 "$PUB_PID" >/dev/null 2>&1; then
    kill "$PUB_PID" >/dev/null 2>&1 || true
    wait "$PUB_PID" >/dev/null 2>&1 || true
  fi

  if [[ -n "$SUB_PID" ]] && kill -0 "$SUB_PID" >/dev/null 2>&1; then
    kill "$SUB_PID" >/dev/null 2>&1 || true
    wait "$SUB_PID" >/dev/null 2>&1 || true
  fi

  if [[ -n "$SUB_INFO_FILE" ]]; then
    rm -f "$SUB_INFO_FILE" >/dev/null 2>&1 || true
  fi

  if [[ -n "$ORIGINAL_LIMIT" ]]; then
    redis_cli CONFIG SET client-output-buffer-limit "$ORIGINAL_LIMIT" >/dev/null 2>&1 || true
  fi
}

trap cleanup EXIT

require_cmd redis-cli
require_cmd python3

if [[ "$(redis_cli PING 2>/dev/null || true)" != "PONG" ]]; then
  echo "[ERROR] Redis is not reachable at ${REDIS_HOST}:${REDIS_PORT}" >&2
  echo "        Start Redis first, then rerun this script." >&2
  exit 1
fi

ORIGINAL_LIMIT="$(redis_cli --raw CONFIG GET client-output-buffer-limit | sed -n '2p')"
if [[ -z "$ORIGINAL_LIMIT" ]]; then
  echo "[ERROR] failed to read current client-output-buffer-limit" >&2
  exit 1
fi

log "Original client-output-buffer-limit: $ORIGINAL_LIMIT"
log "Profile: ${PROFILE}"
log "Applying test limit: $TEST_BUFFER_LIMIT"
redis_cli CONFIG SET client-output-buffer-limit "$TEST_BUFFER_LIMIT" >/dev/null

log "Starting subscriber on channel '$CHANNEL'"
SUB_INFO_FILE="$(mktemp)"
REDIS_HOST="$REDIS_HOST" REDIS_PORT="$REDIS_PORT" CHANNEL="$CHANNEL" SUB_RCVBUF="$SUB_RCVBUF" \
  python3 - <<'PY' >"$SUB_INFO_FILE" 2>/dev/null &
import os
import socket
import time
import sys

host = os.environ["REDIS_HOST"]
port = int(os.environ["REDIS_PORT"])
channel = os.environ["CHANNEL"].encode()
rcvbuf = int(os.environ["SUB_RCVBUF"])

sock = socket.create_connection((host, port), timeout=10)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_RCVBUF, rcvbuf)
sock.settimeout(10)

def bulk(data: bytes) -> bytes:
    return b"$" + str(len(data)).encode() + b"\r\n" + data + b"\r\n"

def send_command(*parts: bytes):
    frame = b"*" + str(len(parts)).encode() + b"\r\n"
    for p in parts:
        frame += bulk(p)
    sock.sendall(frame)

def read_line() -> bytes:
    data = b""
    while not data.endswith(b"\r\n"):
        chunk = sock.recv(1)
        if not chunk:
            raise RuntimeError("socket closed before line was received")
        data += chunk
    return data

send_command(b"CLIENT", b"ID")
client_id_line = read_line()
if not client_id_line.startswith(b":"):
    raise RuntimeError(f"unexpected CLIENT ID response: {client_id_line!r}")

client_id = client_id_line[1:-2].decode()
sys.stdout.write(client_id)
sys.stdout.flush()

send_command(b"SUBSCRIBE", channel)

# Keep the socket open without reading to emulate a slow/non-reading subscriber.
while True:
    time.sleep(1)
PY
SUB_PID="$!"
sleep 1

if ! kill -0 "$SUB_PID" >/dev/null 2>&1; then
  echo "[ERROR] subscriber process failed to start" >&2
  exit 1
fi

SUB_CLIENT_ID=""
for _ in $(seq 1 20); do
  if [[ -s "$SUB_INFO_FILE" ]]; then
    SUB_CLIENT_ID="$(cat "$SUB_INFO_FILE")"
    break
  fi
  sleep 0.2
done

if [[ -z "$SUB_CLIENT_ID" ]]; then
  echo "[ERROR] failed to resolve subscriber client id" >&2
  exit 1
fi

log "Subscriber mapped: pid=${SUB_PID}, client_id=${SUB_CLIENT_ID}"
log "Subscriber is connected with SO_RCVBUF=${SUB_RCVBUF} and no read loop"

log "Publishing burst: count=${PUBLISH_COUNT}, payload_bytes=${PAYLOAD_BYTES}"
{
  CHANNEL="$CHANNEL" PAYLOAD_BYTES="$PAYLOAD_BYTES" PUBLISH_COUNT="$PUBLISH_COUNT" \
    python3 - <<'PY' | redis_cli --pipe >/dev/null
import os
import sys

channel = os.environ["CHANNEL"]
payload = b"x" * int(os.environ["PAYLOAD_BYTES"])
count = int(os.environ["PUBLISH_COUNT"])
channel_bytes = channel.encode()

prefix = (
    b"*3\r\n"
    + b"$7\r\nPUBLISH\r\n"
    + f"${len(channel_bytes)}\r\n".encode()
    + channel_bytes
    + b"\r\n"
    + f"${len(payload)}\r\n".encode()
)
command = prefix + payload + b"\r\n"

for _ in range(count):
    sys.stdout.buffer.write(command)
PY
} &
PUB_PID="$!"

max_obl=0
max_oll=0
max_omem=0
disconnected="false"

samples="$(python3 - <<PY
import math
print(max(1, math.ceil(float("${MONITOR_SECONDS}") / float("${SAMPLE_INTERVAL}"))))
PY
)"

log "Sampling CLIENT LIST TYPE pubsub every ${SAMPLE_INTERVAL}s for up to ${MONITOR_SECONDS}s"
printf '%-8s %-8s %-8s %-10s\n' "sample" "obl" "oll" "omem"

for i in $(seq 1 "$samples"); do
  line="$(redis_cli --raw CLIENT LIST TYPE pubsub | awk -v id="${SUB_CLIENT_ID}" '$0 ~ ("id=" id " ") { print; exit }')"
  if [[ -z "$line" ]]; then
    disconnected="true"
    log "Subscriber disconnected (client id ${SUB_CLIENT_ID})"
    break
  fi

  obl="$(field_value "$line" obl)"
  oll="$(field_value "$line" oll)"
  omem="$(field_value "$line" omem)"

  obl="${obl:-0}"
  oll="${oll:-0}"
  omem="${omem:-0}"

  (( obl > max_obl )) && max_obl="$obl"
  (( oll > max_oll )) && max_oll="$oll"
  (( omem > max_omem )) && max_omem="$omem"

  printf '%-8s %-8s %-8s %-10s\n' "$i" "$obl" "$oll" "$omem"
  sleep "$SAMPLE_INTERVAL"
done

if [[ -n "$PUB_PID" ]] && kill -0 "$PUB_PID" >/dev/null 2>&1; then
  wait "$PUB_PID"
fi

log "Max observed: obl=${max_obl}, oll=${max_oll}, omem=${max_omem}"

if [[ "$disconnected" == "true" ]]; then
  log "[PASS] Redis pubsub output buffer limit triggered disconnect as expected."
  echo "RESULT status=PASS disconnected=true max_obl=${max_obl} max_oll=${max_oll} max_omem=${max_omem}"
  exit 0
fi

log "[WARN] Subscriber remained connected within monitor window."
log "       Increase PUBLISH_COUNT or PAYLOAD_BYTES, or lower TEST_BUFFER_LIMIT."
echo "RESULT status=WARN disconnected=false max_obl=${max_obl} max_oll=${max_oll} max_omem=${max_omem}"
exit 2
