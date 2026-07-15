---
name: integrate-konture
description: Instructions to integrate the Konture (io.github.baole.konture)
  Kotlin/Gradle architecture-testing library into a project for the first time —
  installing the plugin/dependency, scaffolding a dedicated architecture-test
  module, and deciding how it runs in CI. Use whenever the user asks to "add
  Konture", "integrate Konture into my project", "set up architecture testing",
  "install the Konture Gradle plugin", or "wire Konture into CI". This skill
  covers first-time integration only — for writing or reviewing the actual architecture
  rules/tests once Konture is installed, use the konture-architecture-tests
  skill instead.
metadata:
  keywords:
  - konture
  - gradle plugin integration
  - architecture testing
  - kotlin
  - kmp
  - android
  - ci integration
  - version catalog
---

# Integrate Konture

Konture (https://github.com/baole/konture) is a Kotlin/Gradle
architecture-testing library. This skill covers getting it installed into a
project and running — not writing the actual guardrails. Once integration is done,
hand off to the **konture-architecture-tests** skill for classifying and
authoring rules.

## Prerequisites

- The project is a Kotlin/Gradle build (Android, KMP, or JVM backend).
- You have (or can get) a listing of the project's modules and its current
  Gradle/Kotlin/AGP versions — Step 1 below covers gathering this.

## Workflow

- Step 1: Inspect the project
- Step 2: Install Konture
- Step 3: Scaffold a dedicated test module
- Step 4: Decide how it runs in CI, then wire it in
- Step 5: Report back and hand off to rule-writing

### Step 1. Inspect the project

Gather this before touching any file:

1. **Build system details** — Kotlin DSL vs Groovy, whether
   `gradle/libs.versions.toml` (version catalog) already exists, current
   Gradle/Kotlin/AGP versions.
2. **Module list** — read `settings.gradle.kts` for every module/subproject and
   identify logical layers (e.g. `core:domain`, `core:data`, `feature:*`,
   `app`) so later steps reference real module paths, not placeholders.
3. **Existing architecture-testing tooling** — if the project already uses
   Konsist, detekt architecture rules, or another architecture-testing tool,
   flag the overlap and ask whether to replace it or run alongside it, rather
   than silently duplicating coverage.

### Step 2. Install Konture

Read [references/dependency-integration.md](references/dependency-integration.md) for the
full version-catalog vs. traditional-DSL forms, and for how to check the
actual latest Konture version (never assume a version from a doc is current —
Konture is young and actively developed).

- If the project already uses a version catalog, add Konture there.
- If it doesn't, use the traditional DSL form — **don't introduce a version
  catalog just to install one library**; that's a bigger build-system decision
  than this task. Ask the user first if you think a catalog would genuinely
  help.

### Step 3. Scaffold a dedicated test module

Konture's stated best practice is a separate module (commonly `:konture-test`
or `:architecture-tests`), not architecture tests living inside production
modules.

1. Create the module directory and register it using
   [resources/settings.gradle.kts.snippet](resources/settings.gradle.kts.snippet)
   — adjust the module name to fit this project's naming conventions if
   `:konture-test` doesn't.
2. Use
   [resources/konture-test-module.build.gradle.kts.template](resources/konture-test-module.build.gradle.kts.template)
   as the starting `build.gradle.kts`. Replace every placeholder
   `project(":...")` dependency with the real modules found in Step 1 — this
   also forces those modules to compile before the architecture tests run.
3. Don't write actual architecture rules yet — that's Step 5 / the
   `konture-architecture-tests` skill's job. It's fine to leave the module with
   zero tests at this point, or at most one trivial smoke-test rule to confirm
   the module compiles and Konture resolves correctly.

### Step 4. Decide how it runs in CI, then wire it in

Read [references/ci-integration.md](references/ci-integration.md) before
touching CI config. In short: the new module rides along with
`./gradlew test`/`check` automatically with zero extra config, but that's not
always the right choice — measure the run time first, then pick one of:

- **A.** Leave it in the default lifecycle (simplest).
- **B.** Add it as a separate CI stage — see
  [resources/ci-architecture-tests-job.yml.snippet](resources/ci-architecture-tests-job.yml.snippet)
  for an illustrative example to adapt, not a drop-in replacement.
- **C.** Use a JUnit 5 tagged split within the same module.

State which option you picked and why. Then confirm it's actually invoked
from the existing CI config (`.github/workflows/`, Azure Pipelines YAML, or
equivalent) — don't assume the default lifecycle is already wired into every
CI job just because it works locally with root `./gradlew test`.

### Step 5. Report back and hand off

Report:

- The final module list and where the new test module sits.
- The Konture version installed and how (catalog vs. traditional).
- The CI approach chosen and why.
- How to run the checks locally (e.g. `./gradlew :konture-test:test`).

If the user wants actual architecture rules written now (layer isolation,
module boundaries, DI wiring checks, etc.), that's the
**konture-architecture-tests** skill's job — it covers aligning the request
with foundational pillars or custom project guardrails, discovering the project's
real architecture, verifying the DSL, and drafting the test.

## Constraints

- Don't touch unrelated build logic, dependency versions, or module structure
  beyond what's needed for this integration.
- Keep the diff scoped and reviewable — one logical change per step above,
  rather than one giant commit.

## Reference files

- [references/dependency-integration.md](references/dependency-integration.md) —
  version-catalog vs. traditional dependency integration, and how to check the
  latest version.
- [references/ci-integration.md](references/ci-integration.md) — the
  measure-first CI decision process and the three wiring options.
- [resources/konture-test-module.build.gradle.kts.template](resources/konture-test-module.build.gradle.kts.template) —
  starter Gradle module config.
- [resources/settings.gradle.kts.snippet](resources/settings.gradle.kts.snippet) —
  module registration snippet.
- [resources/ci-architecture-tests-job.yml.snippet](resources/ci-architecture-tests-job.yml.snippet) —
  illustrative separate-CI-stage example.
