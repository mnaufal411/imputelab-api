#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"

if [ ! -f .env ]; then
  echo "File .env tidak ditemukan."
  exit 1
fi

set -a
source .env
set +a

if [ -z "${DB_PASSWORD:-}" ]; then
  echo "DB_PASSWORD belum diatur di .env."
  exit 1
fi

cleanup() {
  echo "Menghentikan backend dan frontend..."
  kill "$BACKEND_PID" "$FRONTEND_PID" 2>/dev/null || true
  wait 2>/dev/null || true
}
trap cleanup INT TERM EXIT

(cd api && ./mvnw spring-boot:run) &
BACKEND_PID=$!

(cd frontend && npm run dev -- --host 127.0.0.1) &
FRONTEND_PID=$!

wait -n "$BACKEND_PID" "$FRONTEND_PID"
