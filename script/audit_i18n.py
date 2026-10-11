#!/usr/bin/env python3
"""
Konture 8-Language Internationalization (i18n) Parity & Syntax Linter.

Audits resource bundles in library/src/main/resources/.../i18n/
Ensures:
1. All 8 supported locales (en, es, fr, it, vi, zh, zh_CN, zh_TW) exist.
2. Complete 100% key parity with the base bundle (messages.properties).
3. Java MessageFormat apostrophe escaping rule: Literal single quotes MUST be doubled ('')
   in any string that contains argument placeholders ({0}, {1}, etc.).
4. Placeholder index consistency ({0}, {1}) across all translations.
"""

import os
import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
I18N_DIR = REPO_ROOT / "library" / "src" / "main" / "resources" / "io" / "github" / "baole" / "konture" / "i18n"

REQUIRED_BUNDLES = [
    "messages.properties",       # en (Base)
    "messages_es.properties",    # Spanish
    "messages_fr.properties",    # French
    "messages_it.properties",    # Italian
    "messages_vi.properties",    # Vietnamese
    "messages_zh.properties",    # Chinese (Base)
    "messages_zh_CN.properties", # Chinese (Simplified)
    "messages_zh_TW.properties", # Chinese (Traditional)
]

PLACEHOLDER_REGEX = re.compile(r"\{(\d+)\}")

def parse_properties_file(filepath: Path):
    """
    Parses a Java .properties file while tracking line numbers for accurate reporting.
    Handles line continuations with backslash.
    Returns: dict of key -> (value, line_number)
    """
    entries = {}
    if not filepath.exists():
        return entries

    with open(filepath, "r", encoding="utf-8") as f:
        current_key = None
        current_val_lines = []
        current_line_no = 0

        for line_no, raw_line in enumerate(f, 1):
            line = raw_line.rstrip("\r\n")
            stripped = line.strip()

            # Ignore comments and blank lines if not in continuation
            if not current_key and (not stripped or stripped.startswith("#") or stripped.startswith("!")):
                continue

            if current_key:
                # Continuing previous value
                if line.endswith("\\"):
                    current_val_lines.append(line[:-1].lstrip())
                else:
                    current_val_lines.append(line.lstrip())
                    entries[current_key] = (" ".join(current_val_lines), current_line_no)
                    current_key = None
                    current_val_lines = []
            else:
                # Find separator (= or :)
                match = re.search(r"[=:]", line)
                if match:
                    sep_idx = match.start()
                    key = line[:sep_idx].strip()
                    val = line[sep_idx + 1:].strip()
                    if val.endswith("\\"):
                        current_key = key
                        current_val_lines.append(val[:-1].rstrip())
                        current_line_no = line_no
                    else:
                        entries[key] = (val, line_no)

    return entries


def audit_i18n():
    print(f"Auditing i18n resource bundles under: {I18N_DIR}")
    if not I18N_DIR.exists():
        print(f"[ERROR] Directory does not exist: {I18N_DIR}", file=sys.stderr)
        return 1

    errors = []
    warnings = []

    # Check for missing bundle files
    for bundle_name in REQUIRED_BUNDLES:
        bundle_path = I18N_DIR / bundle_name
        if not bundle_path.exists():
            errors.append(f"Missing required bundle file: {bundle_name}")

    if errors:
        for err in errors:
            print(f"[ERROR] {err}", file=sys.stderr)
        return 1

    base_path = I18N_DIR / "messages.properties"
    base_entries = parse_properties_file(base_path)
    base_keys = set(base_entries.keys())

    print(f"Base bundle (messages.properties) contains {len(base_keys)} keys.")

    for bundle_name in REQUIRED_BUNDLES:
        bundle_path = I18N_DIR / bundle_name
        entries = parse_properties_file(bundle_path)
        keys = set(entries.keys())

        # 1. Key Parity Checks
        missing_keys = base_keys - keys
        if missing_keys:
            for k in sorted(missing_keys):
                errors.append(f"{bundle_name}: Missing key '{k}' (defined in messages.properties)")

        extra_keys = keys - base_keys
        if extra_keys:
            for k in sorted(extra_keys):
                warnings.append(f"{bundle_name}:{entries[k][1]}: Extraneous key '{k}' not found in base messages.properties")

        # 2. MessageFormat Apostrophe & Placeholder Syntax Checks
        for key, (val, line_no) in entries.items():
            base_val_tuple = base_entries.get(key)
            base_val = base_val_tuple[0] if base_val_tuple else ""

            placeholders = PLACEHOLDER_REGEX.findall(val)
            base_placeholders = PLACEHOLDER_REGEX.findall(base_val)

            # Check literal apostrophes in strings with placeholders
            if placeholders:
                # In Java MessageFormat, a single quote (') disables placeholder expansion unless doubled ('')
                # Strip out valid doubled quotes ('') first
                val_without_doubled = val.replace("''", "")
                if "'" in val_without_doubled:
                    errors.append(
                        f"{bundle_name}:{line_no}: Unescaped single quote (') in key '{key}' with placeholders {placeholders}.\n"
                        f"  Value: {val}\n"
                        f"  Fix: Double all single quotes ('') e.g., 'doesn''t' or 'l''architecture'."
                    )

            # Check placeholder parity between base and translation
            if base_placeholders:
                base_set = set(base_placeholders)
                val_set = set(placeholders)
                if base_set != val_set and not (len(base_set) == 0 and len(val_set) == 0):
                    warnings.append(
                        f"{bundle_name}:{line_no}: Placeholder mismatch for key '{key}'. "
                        f"Base expects {sorted(base_set)}, but found {sorted(val_set)}."
                    )

    if warnings:
        print(f"\n[WARNINGS] Found {len(warnings)} non-blocking warning(s):")
        for w in warnings:
            print(f"  [WARN] {w}")

    if errors:
        print(f"\n[ERRORS] Found {len(errors)} blocking i18n error(s):", file=sys.stderr)
        for e in errors:
            print(f"  [ERROR] {e}", file=sys.stderr)
        return 1

    print("\n[SUCCESS] All 8 i18n resource bundles are valid, synchronized, and conform to MessageFormat syntax!")
    return 0


if __name__ == "__main__":
    sys.exit(audit_i18n())
