#!/bin/sh
# ub-mode Dryer gate. Installs unclebob/dryer at a pinned commit into target/dryer/tool
# (Python 3.11+ required) and runs tools/dryer/dryer_gate.py. Usage: tools/dryer/dryer-gate.sh [threshold]
set -eu

DRYER_REPO=https://github.com/unclebob/dryer
DRYER_REF=66ff6d21a42c04afcad89c78a80066176d1294b0
ROOT=$(CDPATH= cd -- "$(dirname "$0")/../.." && pwd)
TOOL="$ROOT/target/dryer/tool"
PY="$TOOL/.venv/bin/python"

if [ ! -x "$PY" ] || [ "$(git -C "$TOOL/src" rev-parse HEAD 2>/dev/null)" != "$DRYER_REF" ]; then
  for candidate in python3.13 python3.12 python3.11 python3; do
    if command -v "$candidate" >/dev/null 2>&1 &&
       "$candidate" -c 'import sys; sys.exit(sys.version_info < (3, 11))'; then
      BOOT=$candidate; break
    fi
  done
  [ -n "${BOOT:-}" ] || { echo "dryer: Python 3.11+ is required" >&2; exit 2; }
  rm -rf "$TOOL"
  git init -q "$TOOL/src"
  git -C "$TOOL/src" fetch -q --depth 1 "$DRYER_REPO" "$DRYER_REF"
  git -C "$TOOL/src" checkout -q FETCH_HEAD
  "$BOOT" -m venv "$TOOL/.venv"
  PIP="$TOOL/.venv/bin/pip --disable-pip-version-check -q"
  $PIP install tree-sitter==0.26.0 tree-sitter-language-pack==1.22.1
  $PIP install --no-deps "$TOOL/src"
fi

exec python3 "$ROOT/tools/dryer/dryer_gate.py" "$PY" "${1:-0.8}"
