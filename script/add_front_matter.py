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
    "architecture_test.md": {
        "layout": "default",
        "title": "Why Architecture Testing?",
        "nav_order": 20,
        "permalink": "/architecture_test/"
    },
    "how_it_works.md": {
        "layout": "default",
        "title": "How it Works",
        "nav_order": 30,
        "permalink": "/how_it_works/"
    },
    "contributing.md": {
        "layout": "default",
        "title": "Contributing",
        "nav_order": 100,
        "permalink": "/contributing/"
    },
    "installation.md": {
        "layout": "default",
        "title": "Installation",
        "nav_order": 50,
        "permalink": "/installation/"
    },
    "usage.md": {
        "layout": "default",
        "title": "Usage",
        "nav_order": 52,
        "permalink": "/usage/"
    },
    "configuration.md": {
        "layout": "default",
        "title": "Configuration",
        "nav_order": 54,
        "permalink": "/configuration/"
    },
    "baseline.md": {
        "layout": "default",
        "title": "Architecture Baselines",
        "nav_order": 56,
        "permalink": "/baseline/"
    },
    "showcases.md": {
        "layout": "default",
        "title": "Showcases",
        "nav_order": 70,
        "permalink": "/showcases/"
    },
    "recipes/README.md": {
        "layout": "default",
        "title": "Recipes",
        "has_children": True,
        "has_toc": False,
        "nav_order": 60,
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
        "title": "AI Prompts & Skills",
        "has_children": True,
        "has_toc": False,
        "nav_order": 40,
        "permalink": "/ai-prompts/"
    },
    "ai-prompts/integration-prompt.md": {
        "layout": "default",
        "title": "Installation",
        "parent": "AI Prompts & Skills",
        "nav_order": 1
    },
    "ai-prompts/writing-tests-prompt.md": {
        "layout": "default",
        "title": "Writing Tests",
        "parent": "AI Prompts & Skills",
        "nav_order": 2
    },
    "ai-prompts/konture-architecture-tests.md": {
        "layout": "default",
        "title": "Writing & Extending Tests (The 6 Pillars)",
        "parent": "AI Prompts & Skills",
        "nav_order": 3
    },
    "articles/README.md": {
        "layout": "default",
        "title": "Articles",
        "has_children": True,
        "has_toc": False,
        "nav_order": 80,
        "permalink": "/articles/"
    },
    "articles/kotlin-architecture-tests-what-and-why.md": {
        "layout": "default",
        "title": "Kotlin Architecture Tests: What and Why",
        "description": "Why green Kotlin builds do not prove architectural boundaries are intact.",
        "parent": "Articles",
        "nav_order": 1,
        "permalink": "/articles/kotlin-architecture-tests-what-and-why/"
    },
    "articles/kotlin-architecture-tests-why-konture-exists.md": {
        "layout": "default",
        "title": "Kotlin Architecture Tests: Why Konture Exists",
        "description": "Why Kotlin architecture tests need both Gradle graph and source-model awareness.",
        "parent": "Articles",
        "nav_order": 2,
        "permalink": "/articles/kotlin-architecture-tests-why-konture-exists/"
    },
    "articles/kotlin-architecture-tests-with-konture.md": {
        "layout": "default",
        "title": "Kotlin Architecture Tests with Konture",
        "description": "How to set up Konture architecture tests with rule-design and rollout guidance.",
        "parent": "Articles",
        "nav_order": 3,
        "permalink": "/articles/kotlin-architecture-tests-with-konture/"
    },
    "api-docs.md": {
        "layout": "default",
        "title": "API Docs",
        "nav_order": 90,
        "permalink": "/api-docs/"
    }
}

def find_markdown_files(base_dir):
    md_files = []
    for root, _, files in os.walk(base_dir):
        if any(ignored in root for ignored in [".git", "node_modules", "_site", ".jekyll-cache"]):
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
    import re
    # Matches a blockquote starting with GFM alert and all subsequent lines in that same blockquote
    alert_pattern = r'^[ \t]*>[ \t]*\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\][ \t]*\n((?:[ \t]*>[ \t]*.*\n?)*)'

    def replace_alert(match):
        alert_type = match.group(1).lower()
        blockquote_body = match.group(2)
        if not blockquote_body.endswith("\n"):
            blockquote_body += "\n"
        # We append the kramdown attribute block immediately after the blockquote
        return f"{blockquote_body}{{: .{alert_type} }}\n"

    for full_path, rel_path in files:
        with open(full_path, "r", encoding="utf-8") as f:
            content = f.read()

        # Perform the GFM alert conversion
        modified_content = re.sub(alert_pattern, replace_alert, content, flags=re.M | re.I)

        if rel_path in FRONT_MATTER_MAP:
            metadata = FRONT_MATTER_MAP[rel_path]

            # Safeguard: if it already starts with front matter ---, check if alert was converted
            if content.startswith("---\n"):
                if modified_content != content:
                    with open(full_path, "w", encoding="utf-8") as f:
                        f.write(modified_content)
                    print(f"Converted GFM alerts in {rel_path} (already has front matter).")
                    converted_count += 1
                else:
                    print(f"Skipping {rel_path} (already has front matter and no GFM alerts to convert).")
                continue

            # Build YAML front matter block
            lines = ["---"]
            for key, val in metadata.items():
                if isinstance(val, bool):
                    lines.append(f"{key}: {str(val).lower()}")
                elif isinstance(val, int):
                    lines.append(f"{key}: {val}")
                else:
                    # Escape double quotes and enclose in double quotes
                    escaped_val = str(val).replace('"', '\\"')
                    lines.append(f'{key}: "{escaped_val}"')
            lines.append("---")
            lines.append("") # blank line

            new_content = "\n".join(lines) + modified_content.lstrip("\n")
            with open(full_path, "w", encoding="utf-8") as f:
                f.write(new_content)
            print(f"Prepended front matter and converted GFM alerts in {rel_path} successfully.")
            converted_count += 1
        else:
            # Not in FRONT_MATTER_MAP but we can still convert alerts if present!
            if modified_content != content:
                with open(full_path, "w", encoding="utf-8") as f:
                    f.write(modified_content)
                print(f"Converted GFM alerts in unmapped file {rel_path} successfully.")
                converted_count += 1
            else:
                print(f"Warning: No metadata mapped for {rel_path} and no GFM alerts found.")
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
