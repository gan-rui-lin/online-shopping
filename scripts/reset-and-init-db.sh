#!/usr/bin/env bash
set -euo pipefail

DB_USER="root"
DB_PASSWORD="${DB_PASSWORD:-}"
DB_NAME="online_shopping"
MYSQL_EXE="mysql"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --db-user)
      DB_USER="$2"
      shift 2
      ;;
    --db-password)
      DB_PASSWORD="$2"
      shift 2
      ;;
    --db-name)
      DB_NAME="$2"
      shift 2
      ;;
    --mysql-exe)
      MYSQL_EXE="$2"
      shift 2
      ;;
    -h|--help)
      cat <<EOF
Usage:
  ./reset-db.sh [--db-user USER] [--db-password PASSWORD] [--db-name NAME] [--mysql-exe PATH]

Options:
  --db-user        MySQL username, default: root
  --db-password    MySQL password, or use environment variable DB_PASSWORD
  --db-name        Database name, default: online_shopping
  --mysql-exe      MySQL client executable, default: mysql
EOF
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      exit 1
      ;;
  esac
done

if [[ -z "$DB_PASSWORD" ]]; then
  echo "DbPassword is required. Pass --db-password or set DB_PASSWORD environment variable." >&2
  exit 1
fi

if ! command -v "$MYSQL_EXE" >/dev/null 2>&1; then
  echo "MySQL client not found: $MYSQL_EXE" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(dirname "$SCRIPT_DIR")"

SCHEMA_PATH="$REPO_ROOT/src/main/resources/schema.sql"
INIT_DATA_PATH="$REPO_ROOT/src/main/resources/init-data.sql"

if [[ ! -f "$SCHEMA_PATH" ]]; then
  echo "schema.sql not found: $SCHEMA_PATH" >&2
  exit 1
fi

if [[ ! -f "$INIT_DATA_PATH" ]]; then
  echo "init-data.sql not found: $INIT_DATA_PATH" >&2
  exit 1
fi

echo "[1/4] Drop and recreate database: $DB_NAME"
RESET_SQL="DROP DATABASE IF EXISTS \`$DB_NAME\`; CREATE DATABASE \`$DB_NAME\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
"$MYSQL_EXE" -u"$DB_USER" -p"$DB_PASSWORD" -e "$RESET_SQL"

echo "[2/4] Apply schema.sql"
"$MYSQL_EXE" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$SCHEMA_PATH"

echo "[3/4] Apply init-data.sql"
"$MYSQL_EXE" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$INIT_DATA_PATH"

echo "[4/4] Verify seeded products"
"$MYSQL_EXE" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "
SELECT COUNT(*) AS spu_count
FROM product_spu
WHERE id BETWEEN 1000 AND 1011;

SELECT COUNT(*) AS image_count
FROM product_image
WHERE id BETWEEN 3000 AND 3011;
"

echo "Database reset and seed completed."