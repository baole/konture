# AGENTS.md

This repository contains **Konture**, a stack- and build-tool agnostic Kotlin architecture testing library for Android, Kotlin Multiplatform (KMP), and JVM backend projects.

---

## 🛠️ Build and Test Commands

Always use `-q`/`--quiet` when running `./gradlew` commands to avoid noisy output. Use fine-grained instructions rather than project-wide commands.

- **Build everything**: `./gradlew -q assemble`
- **Run core tests**: `./gradlew -q :core:test`
- **Run library tests**: `./gradlew -q :library:test`
- **Run Gradle plugin tests**: `./gradlew -q :plugin-gradle:test`
- **Apply Kotlin code formatting**: `./gradlew -q spotlessApply`
- **Generate API documentation (Dokka)**: `./gradlew -q dokkaHtmlMultiModule`
- **Validate Jekyll documentation metadata**: `python3 script/add_front_matter.py --validate`

---

## 📝 Documentation Rules (GitHub Pages & Jekyll)

Our documentation site is built using **Jekyll** on GitHub Pages. However, we keep our source markdown files in `docs/` as clean, standard GitHub Flavored Markdown (GFM).

### 🚨 Crucial Rule: Do NOT Add Jekyll Front Matter Directly
* **Never add YAML front matter blocks** (the `--- ... ---` header) directly to any `.md` file inside the `docs/` directory.
* Doing so clutters the raw Markdown view on GitHub and is automatically prepended by our build pipeline.

### ⚙️ How GitHub Pages Build Works (And What You Must Do)
We use a Python post-processing script, [add_front_matter.py](file:///Users/baoleduc/workspace/konture/script/add_front_matter.py), which runs in our CI/CD pipeline right before building the Jekyll site.

1. **When Modifying Existing Docs**: Just modify the Markdown content as normal. Do not touch or add front matter lines.
2. **When Creating a New Markdown File** (e.g., `docs/my-new-file.md`):
   - Create the file as a clean, standard Markdown file with no front matter.
   - **MANDATORY**: You **must** open [add_front_matter.py](file:///Users/baoleduc/workspace/konture/script/add_front_matter.py) and add a metadata mapping entry for your new file inside the `FRONT_MATTER_MAP` dictionary.
     *Example mapping entry:*
     ```python
     "my-new-file.md": {
         "layout": "default",
         "title": "My New File Title",
         "nav_order": 58,
         "permalink": "/my-new-file/"
     }
     ```
3. **GFM Alerts**: Use standard GitHub Flavored Markdown alerts (e.g., `> [!NOTE]`, `> [!TIP]`, `> [!WARNING]`). The script automatically converts these to Jekyll-compatible Kramdown blockquotes with appropriate CSS styling during the CI build.
4. **Validation**: Always validate your documentation map after any changes:
   ```bash
   python3 script/add_front_matter.py --validate
   ```

---

## 🎨 Code Style Guidelines

- **Kotlin Format**: Spotless + Ktlint are configured. Always run `./gradlew spotlessApply` before pushing or committing changes.
- **Java/Kotlin Source Headers**: File modification tools should maintain the copyright/license headers at the top of Kotlin files (automatically handled by spotless).
- **Detekt static analysis**: Checked during compilation and tests. Ensure no new warnings are introduced.

## 🌐 Internationalization (i18n)

- **Always consider i18n** for user-visible strings, including errors, warnings, logs, assertion messages, and baseline parsing.
- **Do not add or depend on hard-coded English message formats** when equivalent message keys or localization utilities exist.
- **Preserve locale-neutral behavior** for persisted data such as baselines: normalize and parse localized messages through the existing i18n-aware utilities.
- **Test relevant locale variants** whenever changing user-visible messages, message parsing, or baseline matching.
