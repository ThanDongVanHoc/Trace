#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANDROID_ROOT="$SCRIPT_DIR/apps/android"

MODE="${1:-check}"
case "$MODE" in
    build|check|install|submission|bundle) ;;
    *)
        echo "Unknown mode: $MODE" >&2
        echo "Usage: $0 [build|check|install|submission|bundle]" >&2
        exit 2
        ;;
esac

is_jdk21() {
    local java_bin="$1"
    [[ -x "$java_bin" ]] || return 1
    "$java_bin" -version 2>&1 | grep -Eq 'version "21(\.|")' || return 1
}

find_jdk21() {
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
        if is_jdk21 "$java_bin"; then
            echo "$candidate"
            return 0
        fi
    done

    if command -v java >/dev/null 2>&1 && is_jdk21 "$(command -v java)"; then
        command -v java | sed 's#/bin/java$##'
        return 0
    fi

    echo "JDK 21 was not found. Install JDK 21 LTS or set JAVA_HOME." >&2
    echo "  sdk install java 21-temurin     (SDKMAN: https://sdkman.io)" >&2
    echo "  apt install temurin-21-jdk      (Debian/Ubuntu via Adoptium APT repo)" >&2
    return 1
}

JDK21="$(find_jdk21)"
export JAVA_HOME="$JDK21"
export PATH="$JDK21/bin:$PATH"
echo "Using JDK 21: $JDK21" >&2

gradle_args=()
case "$MODE" in
    build) gradle_args=(assembleDebug) ;;
    check) gradle_args=(testDebugUnitTest lintDebug assembleDebug) ;;
    install) gradle_args=(testDebugUnitTest installDebug) ;;
    submission) gradle_args=(testDebugUnitTest lintDebug assembleDebug assembleRelease -PtraceSplitApks=true) ;;
    bundle) gradle_args=(testDebugUnitTest lintDebug bundleRelease) ;;
esac

cd "$ANDROID_ROOT"
./gradlew "${gradle_args[@]}"

if [[ "$MODE" != "submission" ]]; then
    exit 0
fi

release_directory="$ANDROID_ROOT/app/build/outputs/apk/release"
source_apk="$(
    find "$release_directory" -maxdepth 1 -type f \
        -name '*arm64-v8a*release*unsigned*.apk' 2>/dev/null \
        | sort | head -n 1
)"
if [[ -z "$source_apk" ]]; then
    echo "Unsigned ARM64 release APK was not found in $release_directory" >&2
    exit 1
fi

android_sdk=""
if [[ -n "${ANDROID_SDK_ROOT:-}" && -d "$ANDROID_SDK_ROOT" ]]; then
    android_sdk="$ANDROID_SDK_ROOT"
elif [[ -n "${ANDROID_HOME:-}" && -d "$ANDROID_HOME" ]]; then
    android_sdk="$ANDROID_HOME"
elif [[ -f "$ANDROID_ROOT/local.properties" ]]; then
    android_sdk="$(sed -n 's/^sdk\.dir=//p' "$ANDROID_ROOT/local.properties" | head -n 1)"
fi
if [[ -z "$android_sdk" || ! -d "$android_sdk" ]]; then
    echo "Android SDK was not found. Set ANDROID_HOME, ANDROID_SDK_ROOT, or apps/android/local.properties" >&2
    exit 1
fi

build_tools_dir="$android_sdk/build-tools"
apksigner="$(
    find "$build_tools_dir" -maxdepth 2 -type f -name apksigner 2>/dev/null \
        | sort -V | tail -n 1
)"
debug_keystore="$HOME/.android/debug.keystore"
if [[ -z "$apksigner" ]]; then
    echo "apksigner was not found under $build_tools_dir" >&2
    exit 1
fi
if [[ ! -f "$debug_keystore" ]]; then
    echo "Android debug keystore was not found at $debug_keystore" >&2
    exit 1
fi

apk_directory="$SCRIPT_DIR/apk"
mkdir -p "$apk_directory"
target_apk="$apk_directory/app-release.apk"

"$apksigner" sign \
    --ks "$debug_keystore" \
    --ks-key-alias androiddebugkey \
    --ks-pass pass:android \
    --key-pass pass:android \
    --out "$target_apk" \
    "$source_apk"

echo "APK: $target_apk"
echo "SHA-256: $(sha256sum "$target_apk" | awk '{print toupper($1)}')"
