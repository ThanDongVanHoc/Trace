#!/usr/bin/env bash
set -euo pipefail

MEMBER="${1:-all}"
case "$MEMBER" in
    all|member1|member2|member3|member4) ;;
    *)
        echo "Usage: test.sh [all|member1|member2|member3|member4]" >&2
        exit 1
        ;;
esac

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"
use_trace_jdk21

REPOSITORY_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
PLAYGROUND_ROOT="$REPOSITORY_ROOT/playground"
WRAPPER="$REPOSITORY_ROOT/apps/android/gradlew"

case "$MEMBER" in
    member1) TASK=':member1-enrollment:test' ;;
    member2) TASK=':member2-recognition:test' ;;
    member3) TASK=':member3-memory:test' ;;
    member4) TASK=':member4-vault:test' ;;
    *)       TASK='test' ;;
esac

"$WRAPPER" -p "$PLAYGROUND_ROOT" "$TASK"
exit $?
