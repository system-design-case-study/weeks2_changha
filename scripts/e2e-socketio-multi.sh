#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if ! command -v docker >/dev/null 2>&1; then
  echo "[ERROR] docker not found"
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "[ERROR] docker compose not available"
  exit 1
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo "[ERROR] python3 not found"
  exit 1
fi

python3 - <<'PY'
try:
    import socketio  # noqa: F401
except ModuleNotFoundError:
    raise SystemExit(
        "[ERROR] python-socketio not installed. "
        "Install with: python3 -m pip install --user 'python-socketio[client]'"
    )
PY

cleanup() {
  docker compose down -v --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT

echo "[1/4] Building application jar..."
./gradlew bootJar >/dev/null

if ! ls build/libs/*.jar >/dev/null 2>&1; then
  echo "[ERROR] bootJar output not found under build/libs"
  exit 1
fi

echo "[2/4] Starting redis + 2 app instances..."
docker compose up -d redis app1 app2 >/dev/null

echo "[3/4] Waiting for Socket.IO ports..."
python3 - <<'PY'
import socket
import time

targets = [("127.0.0.1", 9092), ("127.0.0.1", 9093)]
deadline = time.time() + 120

while True:
    all_up = True
    for host, port in targets:
        s = socket.socket()
        s.settimeout(0.4)
        try:
            s.connect((host, port))
        except OSError:
            all_up = False
        finally:
            s.close()
    if all_up:
        print("Ports ready:", targets)
        break
    if time.time() > deadline:
        raise SystemExit("[ERROR] Timeout waiting for Socket.IO ports")
    time.sleep(0.5)
PY

echo "[4/4] Running multi-instance e2e checks..."
python3 - <<'PY'
import time
import socketio

received_b = []
received_c = []

sio_a = socketio.Client(reconnection=False, logger=False, engineio_logger=False)
sio_b = socketio.Client(reconnection=False, logger=False, engineio_logger=False)
sio_c = socketio.Client(reconnection=False, logger=False, engineio_logger=False)
sio_unauthorized = socketio.Client(reconnection=False, logger=False, engineio_logger=False)

def connect_with_retry(client, url, namespace, attempts=30, delay=0.3):
    last_error = None
    for _ in range(attempts):
        try:
            client.connect(url, namespaces=[namespace], transports=["websocket"])
            return
        except Exception as exc:
            last_error = exc
            time.sleep(delay)
    raise last_error

@sio_b.on("location:update", namespace="/location")
def on_b(data):
    received_b.append(data)

@sio_c.on("location:update", namespace="/location")
def on_c(data):
    received_c.append(data)

try:
    connect_with_retry(sio_a, "http://127.0.0.1:9092?userId=user-a", "/location")
    connect_with_retry(sio_b, "http://127.0.0.1:9093?userId=user-b", "/location")
    connect_with_retry(sio_c, "http://127.0.0.1:9093?userId=user-c", "/location")

    time.sleep(0.4)
    started = time.time()
    sio_a.emit(
        "location:update",
        {
            "userId": "forged-user-ignored",
            "lat": 37.5665,
            "lon": 126.9780,
            "sequence": 2001,
            "clientTs": int(time.time() * 1000),
        },
        namespace="/location",
    )

    for _ in range(40):
        if received_b:
            break
        time.sleep(0.1)

    if len(received_b) != 1:
        raise SystemExit(f"[FAIL] friend delivery expected=1 actual={len(received_b)}")

    if len(received_c) != 0:
        raise SystemExit(f"[FAIL] non-friend leakage expected=0 actual={len(received_c)}")

    outbound = received_b[0]
    if outbound.get("userId") != "user-a":
        raise SystemExit(f"[FAIL] sender identity mismatch: {outbound}")

    latency_ms = int((time.time() - started) * 1000)
    print(f"[PASS] friend delivery=1, non-friend leakage=0, cross-instance latency~{latency_ms}ms")

    unauthorized_rejected = False
    try:
        sio_unauthorized.connect("http://127.0.0.1:9092", namespaces=["/location"], transports=["websocket"])
    except Exception:
        unauthorized_rejected = True

    if not unauthorized_rejected:
        raise SystemExit("[FAIL] unauthorized connection should be rejected")

    print("[PASS] unauthorized connection rejected")
finally:
    for client in (sio_a, sio_b, sio_c, sio_unauthorized):
        try:
            client.disconnect()
        except Exception:
            pass
PY

echo "[DONE] e2e passed"
