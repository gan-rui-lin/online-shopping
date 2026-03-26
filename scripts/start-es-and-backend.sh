#!/usr/bin/env bash
set -euo pipefail

ES_URL="http://127.0.0.1:9200"
START_ES_WITH_DOCKER=false
START_ES_LOCAL=false
ES_BIN_PATH="/mnt/d/download/elasticsearch-9.3.2/bin/elasticsearch"
ES_CONTAINER_NAME="online-shopping-es"
MAVEN_COMMAND=""
ES_WAIT_SECONDS=180
SKIP_DB_CREDENTIAL_CHECK=false
SKIP_ES_HEALTH_CHECK=false

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

print_usage() {
  cat <<EOF
Usage: $(basename "$0") [options]

Options:
  --es-url URL                     Elasticsearch URL (default: ${ES_URL})
  --start-es-with-docker           Start Elasticsearch with Docker
  --start-es-local                 Start local Elasticsearch binary
  --es-bin-path PATH               Local Elasticsearch executable path
                                   (default: ${ES_BIN_PATH})
  --es-container-name NAME         Docker container name
                                   (default: ${ES_CONTAINER_NAME})
  --maven-command CMD              Maven command to run backend
                                   (default: ./mvnw)
  --es-wait-seconds N              Wait seconds for ES startup
                                   (default: ${ES_WAIT_SECONDS})
  --skip-db-credential-check       Skip DB_PASSWORD empty check
  --skip-es-health-check           Skip Elasticsearch health check
  -h, --help                       Show this help

Examples:
  ./scripts/start-es-and-backend.sh --start-es-with-docker
  ./scripts/start-es-and-backend.sh --start-es-local --es-bin-path /opt/elasticsearch/bin/elasticsearch
  DB_PASSWORD=yourpass ./scripts/start-es-and-backend.sh --start-es-local
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --es-url)
      ES_URL="$2"
      shift 2
      ;;
    --start-es-with-docker)
      START_ES_WITH_DOCKER=true
      shift
      ;;
    --start-es-local)
      START_ES_LOCAL=true
      shift
      ;;
    --es-bin-path)
      ES_BIN_PATH="$2"
      shift 2
      ;;
    --es-container-name)
      ES_CONTAINER_NAME="$2"
      shift 2
      ;;
    --maven-command)
      MAVEN_COMMAND="$2"
      shift 2
      ;;
    --es-wait-seconds)
      ES_WAIT_SECONDS="$2"
      shift 2
      ;;
    --skip-db-credential-check)
      SKIP_DB_CREDENTIAL_CHECK=true
      shift
      ;;
    --skip-es-health-check)
      SKIP_ES_HEALTH_CHECK=true
      shift
      ;;
    -h|--help)
      print_usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      print_usage
      exit 1
      ;;
  esac
done

if [[ -z "${MAVEN_COMMAND}" ]]; then
  if [[ -x "${PROJECT_ROOT}/mvnw" ]]; then
    MAVEN_COMMAND="${PROJECT_ROOT}/mvnw"
  else
    MAVEN_COMMAND="mvn"
  fi
fi

test_es_health() {
  local url="$1"
  local body
  if body="$(curl -fsS --max-time 3 "$url" 2>/dev/null)"; then
    if echo "$body" | grep -q '"cluster_name"'; then
      return 0
    fi
  fi
  return 1
}

wait_es_ready() {
  local url="$1"
  local wait_seconds="$2"
  local max_retry=$(( wait_seconds / 2 ))
  if [[ "$max_retry" -lt 1 ]]; then
    max_retry=1
  fi

  for ((i=1; i<=max_retry; i++)); do
    sleep 2
    if test_es_health "$url"; then
      return 0
    fi
  done

  return 1
}

echo "[1/4] Bash version: ${BASH_VERSION}"

if [[ "${START_ES_WITH_DOCKER}" == true ]]; then
  echo "[2/4] Starting Elasticsearch container: ${ES_CONTAINER_NAME}"

  docker rm -f "${ES_CONTAINER_NAME}" >/dev/null 2>&1 || true

  docker run -d \
    --name "${ES_CONTAINER_NAME}" \
    -p 9200:9200 \
    -e "discovery.type=single-node" \
    -e "xpack.security.enabled=false" \
    -e "xpack.security.autoconfiguration.enabled=false" \
    -e "ES_JAVA_OPTS=-Xms1g -Xmx1g" \
    docker.elastic.co/elasticsearch/elasticsearch:9.3.2 >/dev/null

  if ! wait_es_ready "${ES_URL}" "${ES_WAIT_SECONDS}"; then
    echo "Elasticsearch did not become ready in time." >&2
    echo "Check: docker logs ${ES_CONTAINER_NAME}" >&2
    exit 1
  fi
fi

if [[ "${START_ES_LOCAL}" == true ]]; then
  if test_es_health "${ES_URL}"; then
    echo "[2/4] Elasticsearch already running at ${ES_URL}, skip local startup."
  else
    echo "[2/4] Starting local Elasticsearch: ${ES_BIN_PATH}"

    if [[ ! -f "${ES_BIN_PATH}" ]]; then
      echo "Local ES startup file not found: ${ES_BIN_PATH}" >&2
      exit 1
    fi

    chmod +x "${ES_BIN_PATH}" || true

    ES_HOME="$(cd "$(dirname "${ES_BIN_PATH}")/.." && pwd)"
    ES_LOG="${ES_HOME}/logs/startup-from-script.log"

    mkdir -p "${ES_HOME}/logs"

    (
      cd "${ES_HOME}"
      nohup "${ES_BIN_PATH}" > "${ES_LOG}" 2>&1 &
      echo $! > "${ES_HOME}/logs/elasticsearch.pid"
    )

    if ! wait_es_ready "${ES_URL}" "${ES_WAIT_SECONDS}"; then
      echo "Local Elasticsearch did not become ready in time." >&2
      echo "Check logs: ${ES_LOG}" >&2
      exit 1
    fi
  fi
fi

if [[ "${SKIP_ES_HEALTH_CHECK}" != true ]]; then
  echo "[3/4] Checking Elasticsearch: ${ES_URL}"
  if ! test_es_health "${ES_URL}"; then
    echo "Cannot connect to Elasticsearch at ${ES_URL}." >&2
    echo "Start ES first (or use --start-es-local / --start-es-with-docker)." >&2
    exit 1
  fi
fi

export APP_SEARCH_ES_ENABLED=true
export ES_URIS="${ES_URL}"

if [[ -z "${DB_USERNAME:-}" ]]; then
  export DB_USERNAME="root"
fi

if [[ "${SKIP_DB_CREDENTIAL_CHECK}" != true && -z "${DB_PASSWORD:-}" ]]; then
  echo 'DB_PASSWORD is empty. Set it before startup, e.g. export DB_PASSWORD="your_mysql_password"' >&2
  echo 'Or use --skip-db-credential-check if your DB password is intentionally empty.' >&2
  exit 1
fi

echo "[4/4] Starting backend with ES enabled..."
echo "APP_SEARCH_ES_ENABLED=${APP_SEARCH_ES_ENABLED}"
echo "ES_URIS=${ES_URIS}"
echo "DB_USERNAME=${DB_USERNAME}"

if [[ -z "${DB_PASSWORD:-}" ]]; then
  echo "DB_PASSWORD=(empty)"
else
  echo "DB_PASSWORD=(set)"
fi

cd "${PROJECT_ROOT}"
exec ${MAVEN_COMMAND} spring-boot:run