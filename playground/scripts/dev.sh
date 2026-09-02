#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"
use_trace_jdk21

REPOSITORY_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
PLAYGROUND_ROOT="$REPOSITORY_ROOT/playground"
WRAPPER="$REPOSITORY_ROOT/apps/android/gradlew"
DATA_DIRECTORY="$PLAYGROUND_ROOT/data"
mkdir -p "$DATA_DIRECTORY"

WATCH_OUTPUT="$DATA_DIRECTORY/compile-watch.log"
WATCH_ERROR="$DATA_DIRECTORY/compile-watch-error.log"

echo 'Starting TRACE Kotlin playground...'
echo 'Swagger: http://localhost:8080/docs'
echo 'SQLite:  playground/data/trace-dev.db'
echo 'Compile errors: playground/data/compile-watch-error.log'
echo 'Press Ctrl+C to stop.'

"$WRAPPER" -p "$PLAYGROUND_ROOT" assemble --continuous -x test \
    >"$WATCH_OUTPUT" 2>"$WATCH_ERROR" &
WATCHER_PID=$!

cleanup() {
    if kill -0 "$WATCHER_PID" 2>/dev/null; then
        kill "$WATCHER_PID" 2>/dev/null || true
        wait "$WATCHER_PID" 2>/dev/null || true
    fi
}
trap cleanup EXIT INT TERM

"$WRAPPER" -p "$PLAYGROUND_ROOT" :dev-server:run
exit $?
