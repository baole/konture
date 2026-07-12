#!/usr/bin/env python3
import os
import sys

FRONT_MATTER_MAP = {
    "README.md": {
        "layout": "default",
        "title": "Overview",
        "nav_order": 1,
        "permalink": "/"
    },
    "architecture.md": {
        "layout": "default",
        "title": "How it Works",
        "nav_order": 7,
        "permalink": "/architecture.html"
    },
    "architecture_test.md": {
        "layout": "default",
        "title": "Why Architecture Testing?",
        "nav_order": 2,
        "permalink": "/architecture_test.html"
    },
    "contributing.md": {
        "layout": "default",
        "title": "Contributing",
        "nav_order": 8,
        "permalink": "/contributing.html"
    },
    "installation.md": {
        "layout": "default",
        "title": "Installation",
        "nav_order": 3,
        "permalink": "/installation.html"
    },
    "showcases.md": {
        "layout": "default",
        "title": "Showcases",
        "nav_order": 6,
        "permalink": "/showcases.html"
    },
    "recipes/README.md": {
        "layout": "default",
        "title": "Recipes",
        "has_children": True,
        "nav_order": 5,
        "permalink": "/recipes/"
    },
    "recipes/layer-isolation.md": {
        "layout": "default",
        "title": "Layer Isolation & Dependency Direction",
        "parent": "Recipes",
        "nav_order": 1
    },
    "recipes/module-hygiene.md": {
        "layout": "default",
        "title": "Module Boundary Enforcement",
        "parent": "Recipes",
        "nav_order": 2
    },
    "recipes/type-leakage.md": {
        "layout": "default",
        "title": "Cross-Layer Type Leakage",
        "parent": "Recipes",
        "nav_order": 3
    },
    "recipes/call-violations.md": {
        "layout": "default",
        "title": "Layer-Crossing Call Violations",
        "parent": "Recipes",
        "nav_order": 4
    },
    "recipes/di-conventions.md": {
        "layout": "default",
        "title": "DI Graph Resolution & Wiring Correctness",
        "parent": "Recipes",
        "nav_order": 5
    },
    "recipes/visibility-boundaries.md": {
        "layout": "default",
        "title": "API Surface & Visibility Boundary Enforcement",
        "parent": "Recipes",
        "nav_order": 6
    },
    "recipes/repositories.md": {
        "layout": "default",
        "title": "Repositories Must Be Interfaces",
        "parent": "Recipes",
        "nav_order": 7
    },
    "recipes/naming-conventions.md": {
        "layout": "default",
        "title": "Naming Suffix Conventions",
        "parent": "Recipes",
        "nav_order": 8
    },
    "ai-prompts/README.md": {
        "layout": "default",
        "title": "AI Prompts & Custom Skills",
        "has_children": True,
        "nav_order": 4,
        "permalink": "/ai-prompts/"
    },
    "ai-prompts/setup-prompt.md": {
        "layout": "default",
        "title": "Onboarding & Setup",
        "parent": "AI Prompts & Custom Skills",
        "nav_order": 1
    },
    "ai-prompts/writing-tests-prompt.md": {
        "layout": "default",
        "title": "Writing & Generating Tests",
        "parent": "AI Prompts & Custom Skills",
        "nav_order": 2
    },
    "ai-prompts/konture-architecture-tests.md": {
        "layout": "default",
        "title": "Writing & Extending Tests (The 6 Pillars)",
        "parent": "AI Prompts & Custom Skills",
        "nav_order": 3
    }
}

def find_markdown_files(base_dir):
    md_files = []
    for root, _, files in os.walk(base_dir):
        if any(ignored in root for ignored in [".git", "node_modules"]):
            continue
        for file in files:
            if file.endswith(".md"):
                # Store path relative to base_dir (e.g. README.md or recipes/README.md)
                full_path = os.path.join(root, file)
                rel_path = os.path.relpath(full_path, base_dir)
                md_files.append((full_path, rel_path))
    return md_files

def validate_files(files):
    missing_files = []
    for full_path, rel_path in files:
        if rel_path not in FRONT_MATTER_MAP:
            missing_files.append(rel_path)
    return missing_files

def convert_files(files):
    converted_count = 0
    for full_path, rel_path in files:
        if rel_path in FRONT_MATTER_MAP:
            metadata = FRONT_MATTER_MAP[rel_path]
            with open(full_path, "r", encoding="utf-8") as f:
                content = f.read()

            # Safeguard: if it already starts with front matter ---, skip
            if content.startswith("---\n"):
                print(f"Skipping {rel_path} (already has standard Jekyll front matter).")
                continue

            # Build YAML front matter block
            lines = ["---"]
            for key, val in metadata.items():
                if isinstance(val, bool):
                    lines.append(f"{key}: {str(val).lower()}")
                else:
                    lines.append(f"{key}: {val}")
            lines.append("---")
            lines.append("") # blank line

            new_content = "\n".join(lines) + content.lstrip("\n")
            with open(full_path, "w", encoding="utf-8") as f:
                f.write(new_content)
            print(f"Prepended front matter to {rel_path} successfully.")
            converted_count += 1
        else:
            print(f"Warning: No metadata mapped for {rel_path}.")
    return converted_count

def main():
    base_dir = "docs"
    for arg in sys.argv[1:]:
        if not arg.startswith("--"):
            base_dir = arg
            break

    if not os.path.exists(base_dir):
        print(f"Error: {base_dir} directory does not exist.")
        sys.exit(1)

    validate_only = "--validate" in sys.argv

    files = find_markdown_files(base_dir)
    if not files:
        print("No markdown files found in docs/")
        sys.exit(0)

    if validate_only:
        print(f"Validating {len(files)} markdown files under {base_dir}/...")
        missing = validate_files(files)
        if missing:
            print("\nError: The following markdown files do not have a defined metadata mapping in script/add_front_matter.py:")
            for m_file in missing:
                print(f"  - {m_file}")
            print("\nPlease add the missing file mapping inside the FRONT_MATTER_MAP dictionary in script/add_front_matter.py.\n")
            sys.exit(1)
        print("All files have defined metadata in FRONT_MATTER_MAP!")
        sys.exit(0)
    else:
        print(f"Scanning and prepending front matter to {len(files)} markdown files under {base_dir}/...")
        count = convert_files(files)
        print(f"Done! Successfully converted {count} files in-place.")

if __name__ == "__main__":
    main()
