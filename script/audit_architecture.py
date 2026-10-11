#!/usr/bin/env python3
"""
Konture Architectural Invariants & File Standards Auditor.

Audits repository files for:
1. Strict Layer Decoupling:
   - :core has 0 project dependencies.
   - :core has 0 compiler-embeddable dependencies.
2. Gradle Configuration Cache & Project Isolation:
   - No 'parent.subprojects' or forbidden cross-project calls.
3. Source & Test File Standards:
   - Audits line counts against AGENTS.md thresholds:
     - Source files: target <= 300 lines (hard review trigger: 600 lines).
     - Test files: target <= 450 lines (hard review trigger: 500 lines).
4. Supports '--changed-only' to check only git uncommitted/staged files.
"""

import os
import re
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent

# Thresholds defined in AGENTS.md
SRC_TARGET_LINES = 300
SRC_HARD_TRIGGER = 600
TEST_TARGET_LINES = 450
TEST_HARD_TRIGGER = 500


def get_changed_files():
    """Returns set of relative paths changed in working tree or staged."""
    cmd = ["git", "status", "--porcelain"]
    try:
        res = subprocess.run(cmd, cwd=REPO_ROOT, capture_output=True, text=True, check=True)
        files = set()
        for line in res.stdout.splitlines():
            if len(line) > 3:
                filepath = line[3:].strip()
                # Handle rename syntax 'old -> new'
                if " -> " in filepath:
                    filepath = filepath.split(" -> ")[1].strip()
                files.add(filepath)
        return files
    except Exception:
        return set()


def check_core_layer_isolation():
    """Checks that :core has zero project dependencies and no compiler leaks."""
    core_build = REPO_ROOT / "core" / "build.gradle.kts"
    if not core_build.exists():
        return []

    errors = []
    with open(core_build, "r", encoding="utf-8") as f:
        for line_no, line in enumerate(f, 1):
            stripped = line.strip()
            if stripped.startswith("//"):
                continue
            if "project(" in stripped and any(kw in stripped for kw in ["implementation", "api", "compileOnly"]):
                errors.append(f"core/build.gradle.kts:{line_no}: :core must have ZERO project dependencies: '{stripped}'")
            if "kotlin-compiler-embeddable" in stripped:
                errors.append(f"core/build.gradle.kts:{line_no}: :core must not leak compiler dependencies: '{stripped}'")
    return errors


def check_gradle_project_isolation():
    """Checks for forbidden Project Isolation anti-patterns like parent.subprojects."""
    errors = []
    for build_file in REPO_ROOT.glob("**/*.gradle.kts"):
        if "build/" in str(build_file) or ".gradle/" in str(build_file):
            continue
        rel_path = build_file.relative_to(REPO_ROOT)
        with open(build_file, "r", encoding="utf-8") as f:
            for line_no, line in enumerate(f, 1):
                stripped = line.strip()
                if stripped.startswith("//"):
                    continue
                if "parent.subprojects" in stripped:
                    errors.append(f"{rel_path}:{line_no}: Forbidden 'parent.subprojects' violates Project Isolation: '{stripped}'")
    return errors


def audit_file_sizes(changed_only=False):
    """Audits Kotlin source and test file lengths against AGENTS.md thresholds."""
    changed_files = get_changed_files() if changed_only else None
    
    warnings = []
    hard_triggers = []

    for kt_file in REPO_ROOT.glob("**/*.kt"):
        rel_path = kt_file.relative_to(REPO_ROOT)
        rel_str = str(rel_path)

        # Ignore build, gradle, and sample/showcase directories
        if any(ignored in rel_str for ignored in ["build/", ".gradle/", "showcases/"]):
            continue

        if changed_only and rel_str not in changed_files:
            continue

        with open(kt_file, "r", encoding="utf-8") as f:
            lines = sum(1 for _ in f)

        is_test = "/test/" in rel_str

        if is_test:
            if lines > TEST_HARD_TRIGGER:
                hard_triggers.append((lines, TEST_HARD_TRIGGER, rel_str, "Test"))
            elif lines > TEST_TARGET_LINES:
                warnings.append((lines, TEST_TARGET_LINES, rel_str, "Test"))
        else:
            if lines > SRC_HARD_TRIGGER:
                hard_triggers.append((lines, SRC_HARD_TRIGGER, rel_str, "Source"))
            elif lines > SRC_TARGET_LINES:
                warnings.append((lines, SRC_TARGET_LINES, rel_str, "Source"))

    return warnings, hard_triggers


def main():
    changed_only = "--changed-only" in sys.argv
    strict = "--strict" in sys.argv

    mode_label = "CHANGED FILES ONLY" if changed_only else "FULL REPOSITORY"
    print(f"=== Konture Architecture & File Standards Audit ({mode_label}) ===")

    # 1. Architectural Invariant Checks
    core_errors = check_core_layer_isolation()
    isolation_errors = check_gradle_project_isolation()

    all_invariants = core_errors + isolation_errors
    if all_invariants:
        print("\n[ERROR] Architectural Invariant Violations Detected:", file=sys.stderr)
        for err in all_invariants:
            print(f"  ❌ {err}", file=sys.stderr)
        return 1
    else:
        print("[SUCCESS] Core layer decoupling & Gradle Project Isolation invariants verified.")

    # 2. File Size Thresholds Audit
    warnings, hard_triggers = audit_file_sizes(changed_only=changed_only)

    if hard_triggers:
        print(f"\n[ALERT] {len(hard_triggers)} file(s) exceed HARD REVIEW TRIGGERS:")
        for lines, threshold, path, kind in sorted(hard_triggers, reverse=True):
            print(f"  ⚠️  {lines:4d} lines (> {threshold}): [{kind}] {path}")

    if warnings and not changed_only:
        print(f"\n[INFO] {len(warnings)} file(s) exceed recommended soft targets (source > 300, test > 450 lines).")
    elif warnings and changed_only:
        print(f"\n[INFO] {len(warnings)} changed file(s) exceed recommended soft targets:")
        for lines, threshold, path, kind in sorted(warnings, reverse=True):
            print(f"  ℹ️  {lines:4d} lines (> {threshold}): [{kind}] {path}")

    if strict and hard_triggers:
        print(f"\n[FAILED] Strict check failed due to {len(hard_triggers)} hard file size triggers.", file=sys.stderr)
        return 1

    print("\n[SUCCESS] Architectural audit complete!")
    return 0


if __name__ == "__main__":
    sys.exit(main())
