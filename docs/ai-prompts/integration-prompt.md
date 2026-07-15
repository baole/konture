# 🤖 Installation & Add First Test

```markdown
You are integrating Konture (https://github.com/baole/konture), a Kotlin/Gradle
architecture-testing library, into this repository. Konture combines the project's
Gradle build graph with AST-based static analysis to let you write architecture
guardrails as regular unit tests (module boundaries, package isolation, interface
adherence, naming conventions, dependency direction).

Do the following, in order, and stop to report back after each phase if something
doesn't match what's described:

## Phase 1 — Inspect the project
1. Identify the Gradle build system in use: Kotlin DSL vs Groovy, whether
   `gradle/libs.versions.toml` (version catalog) already exists, and the current
   Gradle/Kotlin/AGP versions.
2. List all existing modules/subprojects (from `settings.gradle.kts`) and identify
   logical layers (e.g. `core:domain`, `core:data`, `feature:*`, `app`) so later
   guardrails reference real module paths, not placeholders.
3. Check the latest Konture version by looking at the plugin/library coordinates
   `io.github.baole.konture` and `io.github.baole:konture` on [Maven Central](https://central.sonatype.com/artifact/io.github.baole/konture) / [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.baole.konture) — do not assume the version in the README is current.

## Phase 2 — Install Konture
If the project uses a version catalog (`gradle/libs.versions.toml`), add:
    [versions]
    konture = "<latest-version>"
    [plugins]
    konture = { id = "io.github.baole.konture", version.ref = "konture" }
    [libraries]
    konture = { group = "io.github.baole", name = "konture", version.ref = "konture" }

Then apply the plugin in the root `build.gradle.kts`:
    plugins {
        alias(libs.plugins.konture) apply true
    }

If no version catalog exists, use the traditional DSL form instead (plugin id
+ version directly in root `build.gradle.kts`, dependency coordinate directly
in the test module). Ask before introducing a version catalog if the project
doesn't already use one — that's a build-system decision beyond this task's scope.

## Phase 3 — Create a dedicated test module
Follow Konture's recommended best practice: don't add architecture tests inside
production modules. Instead:
1. Create a new module, e.g. `:konture-test` (or `:architecture-tests` if that
   naming fits the project's conventions better — check existing module naming
   patterns first).
2. Register it in `settings.gradle.kts`.
3. Configure its `build.gradle.kts` with:
   - `kotlin("jvm")` plugin
   - `testImplementation` on the Konture library
   - JUnit 5 (`junit-jupiter-api` + `junit-jupiter-engine`) unless the project
     already standardizes on JUnit 4 or Kotest — match existing conventions
   - `testImplementation(project(":..."))` for every production module that
     architecture rules need to inspect (this forces those modules to compile
     first)
   - `tasks.test { useJUnitPlatform() }`

## Phase 4 — Write initial guardrails
Using Konture's Fluent Lambda DSL (`io.github.baole.konture.architecture`),
write a starter `ArchitectureGuardrails.kt` test class with rules tailored to
this project's actual module structure. At minimum, cover:
1. **Layer isolation** — domain/core modules must not depend on data or feature
   modules (adjust module names to match Phase 1's findings).
2. **Interface conventions** — e.g. repository classes ending in `Repository`
   must be interfaces, if that convention exists in the codebase.
3. Any other convention you find already informally followed in the codebase
   (naming suffixes, package-per-layer structure, etc.) that would benefit from
   being enforced mechanically.

Do not invent rules for conventions the codebase doesn't actually follow —
inspect real code first, then encode what's already true as an enforced rule,
rather than imposing new architecture.

## Phase 5 — Wire into CI
1. Confirm the `konture-test` module's test task runs as part of the existing
   CI pipeline (check `.github/workflows/`, Azure Pipelines YAML, or equivalent)
   — add it to the relevant Gradle test invocation if it's not automatically
   picked up.
2. Report the final module list, the guardrails written, and how to run them
   locally (e.g. `./gradlew :konture-test:test`).

Constraints:
- Don't touch unrelated build logic, dependency versions, or module structure
  beyond what's needed for this integration.
- If the project already uses Konsist, detekt architecture rules, or another
  architecture-testing tool, flag the overlap and ask whether to replace or
  run alongside it rather than silently duplicating coverage.
- Keep the diff scoped and reviewable — one logical change per phase.
```
