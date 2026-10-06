#!/usr/bin/env bash
# Genuine packaged client in a fresh private WSL display/profile.
set -euo pipefail
repo="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
loader="${1:?fabric, forge or neoforge}"
stage="${2:?Fresh owned Linux staging path}"
fixture="${3:?Disposable matching-version world}"
java="${4:?Matching Java executable}"
exec python3 "$repo/scripts/run-packaged-client.py" --repo "$repo" \
  --loader "$loader" --stage "$stage" --fixture "$fixture" --java "$java"
