#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$ROOT_DIR/ai-service"

if [ -n "${AI_TEST_PYTHON:-}" ]; then
  PYTHON_BIN="$AI_TEST_PYTHON"
elif [ -x ".venv/bin/python" ]; then
  PYTHON_BIN=".venv/bin/python"
else
  PYTHON_BIN="python3"
fi

PYTHONPATH="${PYTHONPATH:-}:."
export PYTHONPATH

"$PYTHON_BIN" -m pytest --cov=app --cov-report=xml --cov-report=term
