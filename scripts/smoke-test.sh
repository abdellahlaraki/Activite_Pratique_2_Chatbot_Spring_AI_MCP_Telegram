#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

JAVA_BIN="${JAVA_BIN:-java}"
SMOKE_LOG_DIR="$(mktemp -d)"
PIDS=()

cleanup() {
  for pid in "${PIDS[@]}"; do
    kill "$pid" 2>/dev/null || true
  done
  wait 2>/dev/null || true
  rm -rf "$SMOKE_LOG_DIR"
}
trap cleanup EXIT INT TERM

wait_for_url() {
  local name="$1"
  local url="$2"
  for _ in {1..40}; do
    if curl --fail --silent "$url" >/dev/null; then
      echo "[OK] $name"
      return 0
    fi
    sleep 1
  done
  echo "[ERREUR] $name ne répond pas : $url"
  if [[ -f "$SMOKE_LOG_DIR/$name.log" ]]; then
    tail -n 80 "$SMOKE_LOG_DIR/$name.log"
  fi
  return 1
}

start_service() {
  local name="$1"
  shift
  "$JAVA_BIN" -Xms32m -Xmx192m "$@" >"$SMOKE_LOG_DIR/$name.log" 2>&1 &
  PIDS+=("$!")
}

start_service discovery -jar discovery-service/target/discovery-service-1.0.0.jar
wait_for_url discovery-service http://localhost:8761/actuator/health

start_service customer -jar customer-service/target/customer-service-1.0.0.jar
start_service inventory -jar inventory-service/target/inventory-service-1.0.0.jar
wait_for_url customer-service http://localhost:8081/actuator/health
wait_for_url inventory-service http://localhost:8082/actuator/health

start_service mcp -jar mcp-business-server/target/mcp-business-server-1.0.0.jar
wait_for_url mcp-business-server http://localhost:8989/actuator/health

OPENAI_API_KEY=test-key RAG_INDEX_ON_STARTUP=false TELEGRAM_BOT_ENABLED=false \
  "$JAVA_BIN" -Xms32m -Xmx256m -jar chatbot-agent/target/chatbot-agent-1.0.0.jar \
  >"$SMOKE_LOG_DIR/chatbot-agent.log" 2>&1 &
PIDS+=("$!")
wait_for_url chatbot-agent http://localhost:8087/actuator/health

start_service gateway -jar gateway-service/target/gateway-service-1.0.0.jar
wait_for_url gateway-service http://localhost:8888/actuator/health
wait_for_url gateway-customer-route http://localhost:8888/api/customers

curl --fail --silent http://localhost:8082/api/products/low-stock?threshold=5 >/dev/null
echo "[OK] API stock faible"
echo "Tous les contrôles de démarrage sont réussis."
