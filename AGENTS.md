# AGENTS.md

This repository contains **Konture**, a stack- and build-tool agnostic Kotlin architecture testing library for Android, Kotlin Multiplatform (KMP), and JVM backend projects.

---

## 🧭 Mandatory Orchestrator Mode: Phased Sub-Agent Pipeline

The primary agent operates as an **Orchestrator** and **automatically delegates** domain tasks to specialized sub-agents defined in [`.agents/agents/`](.agents/agents/):

| Sub-Agent | Domain / Focus | Key Responsibilities |
| :--- | :--- | :--- |
| **[`konture-dsl-architect`](.agents/agents/konture-dsl-architect.md)** | Architecture & DSL API Design | Fluent architecture testing APIs (`classes()`, `modules()`, `files()`, `slices()`, `layeredArchitecture()`, `functions()`, `properties()`), `@KontureDsl` scoping, and Kotlin Binary Compatibility (`checkKotlinAbi`). |
| **[`konture-engine-engineer`](.agents/agents/konture-engine-engineer.md)** | AST/PSI Engine & Cache | Kotlin compiler embeddable PSI traversal (`PsiEnvironment`), incremental/persistent AST caching, symbol/type resolution (`TypeResolver`), cycle detection, and memory/daemon safety. |
| **[`konture-plugin-engineer`](.agents/agents/konture-plugin-engineer.md)** | Build Tool Plugins (Gradle & Maven) | Maintains `plugin-gradle` and `plugin-maven`, enforces Gradle Configuration Cache and Project Isolation compatibility (via `archLayoutElements`), AGP/KGP inspectors, and layout tasks. |
| **[`konture-test-engineer`](.agents/agents/konture-test-engineer.md)** | Test Lifecycle & Dogfooding | Authors unit tests in `:core` and `:library`, integration tests in `:plugin-gradle`, negative violation assertions (`assertFailsWith`), and dogfooding architecture tests in `konture-test`. |
| **[`konture-i18n-specialist`](.agents/agents/konture-i18n-specialist.md)** | 8-Language Localization (i18n) | Manages resource bundles across 8 supported locales (`en`, `fr`, `es`, `it`, `vi`, `zh`, `zh_CN`, `zh_TW`), `MessageFormat` apostrophe escaping (`''`), and locale-neutral baseline persistence. |
| **[`konture-docs-specialist`](.agents/agents/konture-docs-specialist.md)** | Documentation, Jekyll & Dokka | Manages Jekyll docs in `docs/` adhering to the zero-front-matter rule, registers metadata in `script/add_front_matter.py`, generates Dokka API docs (`dokkaHtmlMultiModule`), and authors architecture recipes. |
| **[`konture-quality-verifier`](.agents/agents/konture-quality-verifier.md)** | Code Review, ABI & Quality Gates | Conducts 3-stage semantic code reviews (Architecture, Craft/I/O/Memory, Tests), JaCoCo 80% coverage enforcement, file size limits (>300 lines), and runs the local pipeline (`./script/check.sh`). |
| **[`konture-showcase-runner`](.agents/agents/konture-showcase-runner.md)** | Showcases & Benchmarks | Validates real-world showcases (`nowinandroid`, `sample-gradle`, `sample-maven`, `ktor-arrow-example`, `kotlinconf-app`) and benchmarks large repository performance (`LargeRepositoryPerformanceBenchmarkTest`). |

### ⚠️ Serialization Rule: No Concurrent Gradle Operations
To avoid Gradle daemon lock contention and corrupted cache states, the orchestrator **MUST serialize execution into ordered phases**. Never run Gradle tasks concurrently across multiple subagents.

### Phased Pipeline vs. Fast-Path Protocol

1. **Full Feature & Architectural Refactoring (Phased Pipeline)**:
   When authoring new DSL rules, changing AST/PSI engine traversal, altering Gradle/Maven plugins, or implementing multi-module features, the orchestrator **MUST serialize execution** across specialists into ordered phases:
   - **Phase 1 (API & Engine Design)**: `konture-dsl-architect` (fluent grammar, ABI compatibility) and/or `konture-engine-engineer` (PSI traversal, AST caches).
   - **Phase 2 (Plugins & Multi-Locale Sync)**: `konture-plugin-engineer` (task isolation, layout elements) and/or `konture-i18n-specialist` (syncing 8-language message bundles).
   - **Phase 3 (Automated Testing & Dogfooding)**: `konture-test-engineer` (authoring positive/negative unit tests, dogfooding in `konture-test`).
   - **Phase 4 (Docs & Quality Gates)**: `konture-docs-specialist` (Jekyll doc mapping, Dokka) and `konture-quality-verifier` (running `./script/preflight.sh`, `./script/check.sh`, ABI audits).

2. **Micro-Tasks & Hotfixes (Fast-Path Protocol)**:
   For localized micro-tasks—such as fixing a single typo or message key, patching a deterministic line of logic, adjusting a single test fixture, or answering architectural queries—the primary orchestrator is **permitted to apply the change directly** without incurring multi-agent delegation overhead. The orchestrator must run `./script/preflight.sh` immediately after code changes to ensure all invariants and tests remain green.

### Compound User Prompts
When a user prompt combines disciplines (e.g., *"Design a new rule, write its documentation, and verify it on nowinandroid"*), the orchestrator **MUST automatically sequence the corresponding specialists** (`konture-dsl-architect` $\rightarrow$ `konture-docs-specialist` $\rightarrow$ `konture-showcase-runner`) rather than executing everything monolithically.

---

## 🧰 External Skills & Domain Whitelisting

- **General Rule**: Generic, unvetted skills (such as generic Android, web, or mocking skills) **MUST NOT** be loaded or used unless explicitly authorized. General Android or web skills often inject incompatible patterns (e.g., direct Android SDK dependencies in `:core`, MockK mocks, or manual fakes) that violate Konture's architectural invariants.
- **Authorized Domain Whitelist**:
  - **Autonomous Feature Shipping**: [`ship-feature`](.agents/skills/ship-feature/SKILL.md) is authorized when delivering complete features or bugfixes end-to-end.
  - **GitHub Issue-to-PR Lifecycle**: [`github-ticket-workflow`](.agents/skills/github-ticket-workflow/SKILL.md) is authorized when picking up, triaging, implementing in an isolated worktree, and PR-reviewing GitHub tickets.
- **Precedence**: If any whitelisted or external skill conflicts with this `AGENTS.md`, this `AGENTS.md` is **STRICTLY AUTHORITATIVE**.

---

## 🛠️ Build and Test Commands

Always use `-q`/`--quiet` when running `./gradlew` commands to avoid noisy output. Use fine-grained instructions rather than project-wide commands.

- **Build everything**: `./gradlew -q assemble`
- **Run core tests**: `./gradlew -q :core:test`
- **Run library tests**: `./gradlew -q :library:test`
- **Run Gradle plugin tests**: `./gradlew -q :plugin-gradle:test`
- **Run dogfooding architecture tests**: `./gradlew -q :runKontureTest -Dkonture.applyPlugin=true`
- **Check binary compatibility (ABI)**: `./gradlew -q checkKotlinAbi`
- **Update binary compatibility dumps**: `./gradlew -q updateKotlinAbi`
- **Apply Kotlin code formatting**: `./gradlew -q spotlessApply`
- **Generate API documentation (Dokka)**: `./gradlew -q dokkaHtmlMultiModule`
- **Fast Agent Pre-Flight Quality Gate (<5s)**: `./script/preflight.sh`
- **Audit 8-Language i18n Parity & Syntax**: `python3 script/audit_i18n.py`
- **Audit Architecture Invariants & File Sizes**: `python3 script/audit_architecture.py --changed-only`
- **In-Memory AST Rule Prototyping Harness**: `SnippetAssertionHarness` in `:library:test`
- **Run full local validation pipeline**: `./script/check.sh`
- **Quick code format**: `./script/format.sh`

---

## 🏛️ Core Architectural Invariants & Boundary Rules

1. **Strict Layer Decoupling**:
   - `:core` has **zero project dependencies** and contains pure data models, AST abstractions, SARIF & JSON reporting, rule metadata, and violation models.
   - `:library` and `:plugin-gradle` depend **only on `:core`**.
   - These boundaries are continuously enforced by Konture itself in `konture-test/src/test/kotlin/io/github/baole/konture/tests/ArchitectureTest.kt`.
2. **No Compiler Classpath Leaks**:
   - The Kotlin compiler embeddable dependencies used by `PsiParser` and `PsiEnvironment` must remain isolated inside the test runner or plugin process and never leak into downstream consumer project compilation classpaths.
3. **Gradle Configuration Cache & Project Isolation Compatibility**:
   - All Gradle tasks must be fully compatible with Gradle's Configuration Cache.
   - Project Isolation is strictly respected: tasks never access parent or sibling project models directly (`parent.subprojects` is strictly forbidden). Artifacts are published and consumed via Gradle's Consuming/Publishing Artifacts API (`archLayoutElements`) using attribute matching.
4. **AST / PSI Caching Lifecycle**:
   - Syntax trees and symbol tables are cached incrementally via `IncrementalAstCache` and `PersistentAstCache` with SHA-256 source hashing (`SourceHasher`).
   - Caches must release AST and PSI references to avoid OutOfMemory errors in persistent Gradle daemons and test runners.
5. **Binary Compatibility (ABI)**:
   - Any public API modification must maintain backward binary compatibility. ABI validation (`checkKotlinAbi`) is enforced in CI.
6. **Code Coverage**:
   - JaCoCo requires a minimum of **80% bundle instruction coverage** across subprojects.

---

## 📝 Documentation Rules (GitHub Pages & Jekyll)

Our documentation site is built using **Jekyll** on GitHub Pages. However, we keep our source markdown files in `docs/` as clean, standard GitHub Flavored Markdown (GFM).

### 🚨 Crucial Rule: Do NOT Add Jekyll Front Matter Directly
* **Never add YAML front matter blocks** (the `--- ... ---` header) directly to any `.md` file inside the `docs/` directory.
* Doing so clutters the raw Markdown view on GitHub and is automatically prepended by our build pipeline.

### ⚙️ How GitHub Pages Build Works (And What You Must Do)
We use a Python post-processing script, [add_front_matter.py](script/add_front_matter.py), which runs in our CI/CD pipeline right before building the Jekyll site.

1. **When Modifying Existing Docs**: Just modify the Markdown content as normal. Do not touch or add front matter lines.
2. **When Creating a New Markdown File** (e.g., `docs/my-new-file.md`):
   - Create the file as a clean, standard Markdown file with no front matter.
   - **MANDATORY**: You **must** open [add_front_matter.py](script/add_front_matter.py) and add a metadata mapping entry for your new file inside the `FRONT_MATTER_MAP` dictionary.
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

## 🎨 Code Style & Quality Guidelines

- **Kotlin Format**: Spotless + Ktlint are configured. Always run `./gradlew -q spotlessApply` (or `./script/format.sh`) before pushing or committing changes.
- **Java/Kotlin Source Headers**: File modification tools should maintain the copyright/license headers at the top of Kotlin files (automatically handled by spotless).
- **Detekt static analysis**: Checked during compilation and tests. Ensure no new warnings are introduced.
- **File Size Standards**:
  - Keep source files under **300 lines** of code (hard review trigger: **600 lines**). Split large files into focused extension traits or separate assertion interfaces.
  - Keep test files under **450 lines** of code (hard review trigger: **500 lines**). Split large test classes into thematic suites.

---

## 🌐 Internationalization (i18n)

Konture supports **8 languages**: English (`en`), French (`fr`), Spanish (`es`), Italian (`it`), Vietnamese (`vi`), Chinese (`zh`), Chinese Simplified (`zh_CN`), and Chinese Traditional (`zh_TW`).

- **Always consider i18n** for user-visible strings, including errors, warnings, logs, assertion messages, and baseline parsing.
- **Do not add or depend on hard-coded English message formats** when equivalent message keys or localization utilities exist.
- **Preserve locale-neutral behavior** for persisted data such as baselines: normalize and parse localized messages through the existing i18n-aware utilities (`BaselineNormalizer`).
- **Java `MessageFormat` Apostrophe Rule**: Literal single quotes **MUST be doubled** (`''`) in `messages*.properties` (e.g., `doesn''t`). An unescaped `'` disables placeholder `{0}` expansion!
- **Test relevant locale variants** whenever changing user-visible messages, message parsing, or baseline matching.

---

## 🔍 Code Review & Engineering Preferences

When reviewing or self-auditing code:
1. **Three-Stage Review Pipeline**:
   - **Stage 1 (Architecture)**: Enforce layer decoupling, zero compiler classpath leaks, and configuration cache safety.
   - **Stage 2 (Craft & Resources)**: Ensure file streams are closed with `.use {}`, no daemon memory leaks, DRY matchers, and binary compatibility.
   - **Stage 3 (Tests & Coverage)**: Verify JaCoCo coverage $\ge 80\%$, author both positive and negative tests, and dogfood in `konture-test`.
2. **Review Plan Before Implementation**:
   - For non-trivial changes, outline the affected modules, API signature impacts (ABI), and test plan before modifying files.
3. **Engineering Preferences**:
   - Prefer explicit typing over ambiguous inference for public APIs (`explicitApiWarning`).
   - Treat edge cases (empty source sets, missing packages, comments in imports) with equal priority to happy paths.
   - Prefer deterministic, high-speed unit tests with mock AST fixtures over heavy end-to-end builds whenever possible.
