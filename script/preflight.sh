#!/usr/bin/env bash
# ==============================================================================
# Konture Fast Agent Pre-Flight Quality Gate (< 5 seconds)
#
# Runs fast static checks:
# 1. Jekyll documentation metadata mapping (add_front_matter.py --validate)
# 2. 8-Language i18n parity and MessageFormat apostrophe syntax (audit_i18n.py)
# 3. Architectural invariants & changed file size limits (audit_architecture.py)
# 4. Spotless / Ktlint formatting check (spotlessCheck)
# 5. Kotlin Binary Compatibility check (checkKotlinAbi)
# ==============================================================================

set -eo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$REPO_ROOT"

echo "======================================================================="
echo "               Konture Agent Pre-Flight Quality Check                  "
echo "======================================================================="

echo "[1/5] Validating documentation metadata..."
python3 script/add_front_matter.py --validate
echo "✓ Documentation metadata valid."

echo "[2/5] Auditing 8-language i18n bundles..."
python3 script/audit_i18n.py
echo "✓ i18n resource bundles synchronized."

echo "[3/5] Auditing architectural invariants & changed files..."
python3 script/audit_architecture.py --changed-only
echo "✓ Architectural invariants satisfied."

echo "[4/5] Checking Kotlin code formatting (Spotless & Ktlint)..."
./gradlew -q spotlessCheck ktlintCheck
echo "✓ Formatting and style checks passed."

echo "[5/5] Checking Kotlin Binary Compatibility (ABI)..."
./gradlew -q checkKotlinAbi
echo "✓ ABI compatibility verified."

echo "======================================================================="
echo "             All Agent Pre-Flight Checks Passed Cleanly!               "
echo "======================================================================="
