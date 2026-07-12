---
name: konture-architecture-tests
description: Instructions to classify, scope, and write Kotlin architecture tests
  using Konture (io.github.baole.konture) in a Gradle project — Android, KMP, or
  JVM backend (Spring/Ktor). Covers aligning proposed rules with core architectural
  pillars (dependency direction/layer isolation, module boundary enforcement,
  cross-layer type leakage, layer-crossing call violations, DI graph wiring correctness,
  API surface/visibility enforcement) or custom project-specific guardrails (naming
  conventions, legacy package quarantine, serialization markers), discovering the
  project's real architecture before writing a rule, verifying Konture's DSL, and
  scaffolding a dedicated architecture-test module. Use whenever the user asks to
  add, review, or extend an architecture test, enforce layer isolation, check for
  module boundary violations, or write custom design-standard guardrails.
metadata:
  keywords:
  - konture
  - architecture testing
  - kotlin
  - gradle
  - kmp
  - android
  - layer isolation
  - module boundaries
  - dependency injection
  - api visibility
---

# Konture Architecture Tests

Konture (https://github.com/baole/konture) combines the Gradle build graph with
AST/PSI static analysis to give **whole-project/whole-graph visibility** — the
one thing compilers and linters structurally cannot provide, since they only
ever see a single compile-unit or a single file's AST. Architecture tests run
as fast, ordinary Kotlin unit tests.

## Prerequisites

Before writing any rule, confirm:

- The project is a Kotlin/Gradle project (Android, KMP, or JVM backend).
- Konture is (or can be) added as a dependency — check
  `gradle/libs.versions.toml` or root `build.gradle.kts` for whether it's
  already applied, and what version is resolved.
- There's a real question to answer: "could a compiler or detekt/ktlint already
  catch this?" If yes, this doesn't belong in Konture — redirect to a lint rule
  instead. Konture earns its keep specifically where whole-graph visibility is
  required.

## Workflow to write a new architecture rule

Follow these steps in order, adapting to the task:

- Step 1: Align the request with foundational pillars or define custom guardrails
- Step 2: Discover the project's real architecture (never assume one)
- Step 3: Locate or scaffold the dedicated test module
- Step 4: Verify the DSL, then draft the test
- Step 5: Run it and confirm it actually fails red on a real violation

### Step 1. Align the request with foundational pillars or define custom guardrails

Read [references/six_pillars.md](references/six_pillars.md) for the full taxonomy (the six core foundational pillars, why compiler/linter misses them, and how to formulate project-specific custom guardrails such as naming conventions, legacy quarantine, serialization annotations, or concurrency restrictions).

Determine whether the request maps to one of the six core architectural pillars, or if it represents a custom project-specific guardrail. Be open and flexible: architecture tests depend entirely on each project's unique engineering standards. If a request doesn't cleanly fit one of the core pillars, design it as a custom project-specific guardrail instead of forcing it.

### Step 2. Discover the project's real architecture

Every project's layering, module names, and DI framework are different — never
reuse a layering scheme from a previous project or from any example in this
skill as if it were this project's actual architecture. Do this in order,
stopping as soon as you have enough to proceed confidently:

1. **Look for an explicit architecture doc first** — `docs/architecture.md`,
   an `adr/` or `docs/decisions/` folder (Architecture Decision Records),
   `CONTRIBUTING.md`, a module-graph diagram, or a "modularization strategy"
   doc referenced from the root `README.md`. If one exists, treat it as the
   source of truth for intended layering — it may state rules the codebase
   doesn't fully enforce yet, which is exactly the gap Konture should close.
2. **If no doc exists, infer from the real build graph** — actual module
   names and their `implementation`/`api` dependencies in
   `settings.gradle.kts` and each module's `build.gradle.kts`, actual package
   conventions in use, the DI framework actually present (Koin/Hilt/Spring/
   Dagger), and what layering the existing module graph already implies.
3. **If the intended layering still isn't clear from either** — ask the user
   directly rather than guessing. A wrong guess produces a rule that either
   enforces the wrong policy or has to be quietly loosened later, which
   defeats the point of an architecture guardrail. A short, targeted question
   ("should `feature:checkout` be allowed to depend on `feature:profile` at
   all, or is that the sideways dependency you want blocked?") is better than
   a confident-sounding rule built on an assumed shape.
4. Never write a rule against a placeholder module/package name "for
   illustration" and call it done — every rule ships against real names found
   in this repo.

### Step 3. Locate or scaffold the dedicated test module

Konture's stated best practice is a separate module (commonly `:konture-test`
or `:architecture-tests`), not architecture tests living inside production
modules. If it doesn't exist yet:

1. Create the module and register it in `settings.gradle.kts`.
2. Use [resources/konture-test-module.build.gradle.kts.template](resources/konture-test-module.build.gradle.kts.template)
   as the starting `build.gradle.kts` — replace the placeholder
   `project(":...")` dependencies with the real modules the rules need
   whole-graph visibility into (this also forces those modules to compile
   first).

### Step 4. Verify the DSL, then draft the test

Read [references/dsl_verification.md](references/dsl_verification.md) before
writing real code. Konture's own docs use two different, likely-inconsistent
DSL syntaxes — that file explains how to check which one actually compiles
against the version resolved in this project.

Use [resources/ArchitectureGuardrails.kt.template](resources/ArchitectureGuardrails.kt.template)
as the starting shape for the test class, then:

- Replace every module/package name with real ones from Step 2.
- Name each test after the policy, not the mechanism — e.g.
  `` `repositories inside domain must be declared as interfaces` `` rather than
  `test1`. The test name is the architecture's live documentation.
- Keep one rule per test ("one rule, one reason to fail") — don't bundle
  unrelated assertions into a single test, or a red CI run won't tell you
  which policy actually broke.

### Step 5. Run it and confirm it fails red on a real violation

A rule that's never seen a real violation is unverified. Where possible,
temporarily introduce the violation it's meant to catch (or point to an
existing one, if the codebase already has the smell) to confirm the rule
actually fails red before it's trusted to fail red in CI. Then remove the
temporary violation (or leave it as a tracked, documented exception if the
user chooses that instead of fixing it immediately).

## Common judgment calls to flag to the user, not decide silently

See the "Common judgment calls" section at the end of
[references/six_pillars.md](references/six_pillars.md) — covers overly broad
wildcard bans, KMP `commonMain` platform-leak rule scoping, DI dead-binding
false positives, and how to handle a rule that the codebase already violates.

## Reference files

- [references/six_pillars.md](references/six_pillars.md) — foundational pillars,
  custom guardrails, quick lookup, and judgment calls to flag rather than
  decide silently.
- [references/dsl_verification.md](references/dsl_verification.md) — the
  README-vs-docs DSL discrepancy and how to verify which syntax compiles.
- [resources/konture-test-module.build.gradle.kts.template](resources/konture-test-module.build.gradle.kts.template) —
  starter Gradle module config.
- [resources/ArchitectureGuardrails.kt.template](resources/ArchitectureGuardrails.kt.template) —
  starter test class.
