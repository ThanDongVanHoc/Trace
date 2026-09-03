#!/usr/bin/env bash
set -euo pipefail

TRACE_JDK_VERSION_PATTERN='version "21[.\"]'

trace_is_jdk21() {
    local java_bin="$1"
    if [[ ! -x "$java_bin" ]]; then
        return 1
    fi
    if "$java_bin" -version 2>&1 | grep -E "$TRACE_JDK_VERSION_PATTERN" >/dev/null; then
        return 0
    fi
    return 1
}

use_trace_jdk21() {
    local candidates=()
    if [[ -n "${JAVA_HOME:-}" ]]; then
        candidates+=("$JAVA_HOME")
    fi

    local root dir
    for root in /usr/lib/jvm /opt/java /opt/jdk /usr/local; do
        [[ -d "$root" ]] || continue
        while IFS= read -r dir; do
            candidates+=("$dir")
        done < <(find "$root" -maxdepth 1 -type d \
            \( -iname 'jdk-21*' -o -iname 'java-21*' -o -iname 'temurin-21*' \
               -o -iname 'openjdk-21*' -o -iname 'zulu-21*' -o -iname 'graalvm-21*' \) \
            2>/dev/null)
    done

    local seen="" candidate java_bin
    for candidate in "${candidates[@]}"; do
        candidate="${candidate%/}"
        [[ -n "$candidate" ]] || continue
        case ":$seen:" in
            *":$candidate:"*) continue ;;
        esac
        seen="$seen:$candidate"
        java_bin="$candidate/bin/java"
        if trace_is_jdk21 "$java_bin"; then
            export JAVA_HOME="$candidate"
            export PATH="$candidate/bin:$PATH"
            echo "Using JDK 21: $candidate" >&2
            return 0
        fi
    done

    if command -v java >/dev/null 2>&1 && trace_is_jdk21 "$(command -v java)"; then
        echo "Using JDK 21 from PATH: $(command -v java)" >&2
        return 0
    fi

    cat >&2 <<'EOF'
TRACE playground requires JDK 21 LTS, but it was not found.
Install Temurin 21, then run this command again:
  sdk install java 21-temurin     (SDKMAN: https://sdkman.io)
  apt install temurin-21-jdk      (Debian/Ubuntu via Adoptium APT repo)
https://adoptium.net/temurin/releases/?version=21
EOF
    return 1
}
