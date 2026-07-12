# Foundational Pillars & Custom Guardrails for Konture testing

Source: https://github.com/baole/konture/blob/main/docs/architecture_test.md

When someone describes a problem ("I don't want X depending on Y", "make sure nobody..."), align it with one of the six foundational pillars or frame it as a custom project-specific guardrail. Architecture rules are open-ended and adapt to each unique project's engineering standards.

| # | Foundational Pillar | What it guards | Why compiler/linter misses it | Concrete request patterns |
|---|--------|-----------------|-------------------------------|----------------------------|
| 1 | **Dependency Direction & Layer Isolation** | Domain/core stays pure; outer layers depend inward only | If it's on the classpath, the compiler allows the import — it can't reason about "domain purity" | "domain shouldn't import Spring/Room/Compose/Android", "core must not depend on data or presentation" |
| 2 | **Module Boundary Enforcement** | No sideways dependencies between sibling feature modules; `commonMain` never declares platform-only artifacts | Gradle only enforces *declared* dependencies — nothing stops someone editing `build.gradle.kts` to add a shortcut | "feature:checkout must not depend on feature:profile", "commonMain shouldn't pull in an androidMain-only lib" |
| 3 | **Cross-Layer Type Leakage** | DTOs/entities from one layer never appear in another layer's public signatures | Compiler happily passes types around — it has no concept of "this leaks your DB schema" | "a Room/JPA entity shouldn't be a Composable/controller parameter", "domain models must not reference network DTOs" |
| 4 | **Layer-Crossing Call Violations** | Outer layers call only their immediate neighbor — no skipping the service/use-case layer | A visible method is a callable method as far as the compiler's concerned | "Activity/Composable must not call Retrofit service directly", "controller must not call JpaRepository directly, only the service layer" |
| 5 | **DI Graph Wiring Correctness** | DI bindings resolve, aren't duplicated/overridden unintentionally, aren't dead | DI frameworks (Spring, Koin, Hilt) resolve at runtime/lazily — compiles fine, crashes on boot | "no unused DI bindings", "feature module shouldn't silently override a core bean", "every @Provides is actually injected somewhere" |
| 6 | **API Surface & Visibility Enforcement** | Implementation details across module boundaries stay `internal`, not accidentally `public` | Kotlin classes are public by default; compiler won't flag a forgotten `internal` | "everything in an `..impl..` package must be internal", "don't let a module's internal helper become a public contract other modules can start relying on" |

### 🚀 Custom Project-Specific Guardrails
Beyond the core architectural pillars, Konture is fully extensible and supports arbitrary custom guardrails tailored to your project's specific conventions:
* **Naming Conventions**: e.g., classes residing in `..repository..` must have name suffixing with `Repository`.
* **Legacy Code Quarantine**: e.g., code outside `..legacy..` package must not depend on legacy utility classes.
* **Serialization Constraints**: e.g., all classes in a `..payload..` package must be annotated with `@Serializable`.
* **Experimental API Restrictions**: e.g., only explicitly allowed packages can import a specific experimental library.
* **Concurrency Contracts**: e.g., blocking I/O calls must be confined to designated components.

If a request doesn't cleanly fit one of the six foundational pillars, design a custom project-specific guardrail using Konture's rich DSL. Do not restrict requests to the six core pillars.

## Quick reference: request → pillar → question to ask first

- "X shouldn't know about Y's framework" → Pillar 1 → confirm the actual package
  prefixes in use.
- "stop people importing across sibling features" → Pillar 2 → confirm the full
  module list and intended dependency graph.
- "our DB model shows up somewhere it shouldn't" → Pillar 3 → confirm which
  layer's public signatures should be scanned.
- "someone skipped the service layer" → Pillar 4 → confirm which layers are
  adjacent vs. non-adjacent in this project's convention.
- "DI crashed at runtime / has dead code" → Pillar 5 → confirm DI framework and
  whether dead-binding detection is even supported by the installed version.
- "internal stuff leaked into another module's public API" → Pillar 6 → confirm
  which packages are meant to be `impl`/internal-only by convention.

## Common judgment calls to flag to the user, not decide silently

- **Scope of `hasZeroDependenciesOn`-style rules**: banning `android..` from
  domain is usually uncontroversial; banning a broad prefix like `kotlinx..`
  might be too aggressive and catch legitimate `kotlinx.coroutines` or
  `kotlinx.serialization` usage. Always check what's actually imported in the
  target package before writing a broad wildcard ban.
- **KMP `commonMain` platform-leak rules** are more delicate than single-platform
  rules — confirm which source sets exist (`androidMain`, `iosMain`, `jvmMain`)
  before writing "commonMain must not depend on platform-only artifacts", since
  the definition of "platform-only" depends on the project's actual target list.
- **DI dead-binding rules** can have false positives for bindings intentionally
  reserved for future use or consumed reflectively — confirm with the user
  before making this a hard CI failure vs. a warning.
- **New vs. retrofitted rules**: if the codebase already violates the rule
  being proposed, don't silently write a passing test around the existing
  violation (e.g. excluding the offending class). Flag the existing violation
  to the user and ask whether to fix it now, quarantine it with an explicit
  documented exception, or scope the rule to new code only.
