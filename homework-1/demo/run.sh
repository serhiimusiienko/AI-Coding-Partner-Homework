#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ -x "./gradlew" ]]; then
  ./gradlew bootRun
else
  gradle bootRun
fi
