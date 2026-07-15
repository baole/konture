# 🧪 Writing & Extending Architecture Tests

````markdown
You are writing, reviewing, or extending Kotlin architecture tests using Konture
(https://github.com/baole/konture) in this Gradle project. Konture combines the
project's Gradle build graph with AST-based static analysis so architecture
guardrails can run as regular unit tests: dependency direction, module
boundaries, package isolation, type leakage, layer-crossing calls, API surface
rules, naming conventions, and other project-specific policies.

Do the following, in order. Before writing any tests, assess the current
architecture and produce a short plan for suitable architecture guardrails. Stop
to report back if the request is better handled by another tool or if the
project's intended architecture is unclear.

## Phase 1 — Validate the request
1. Decide whether this belongs in Konture. If a compiler, detekt, ktlint, or an
   ordinary unit test would already catch the issue, say so and recommend that
   tool instead.
2. Use Konture when the rule needs whole-project, whole-module, or build-graph
   visibility: layer isolation, module dependency direction, public API leaks,
   cross-layer type usage, DI wiring policy, or project-specific architectural
   conventions.
3. Identify whether the requested rule maps to a known guardrail category
   (dependency direction, module boundary, cross-layer type leakage,
   layer-crossing calls, DI graph policy, API visibility) or a custom local
   convention.

## Phase 2 — Assess the real architecture
1. Look first for explicit project guidance: `docs/architecture.md`, ADRs,
   `CONTRIBUTING.md`, module diagrams, README architecture notes, or existing
   Konture/Konsist/detekt architecture tests.
2. Inspect `settings.gradle.kts`, Gradle module paths, source-set names, package
   conventions, and existing dependencies before writing any rule.
3. Summarize the architecture you found: modules, likely layers, allowed
   dependency direction, important package boundaries, and any conventions that
   appear intentional.
4. Identify architecture risks or gaps that Konture can realistically guard:
   sideways module dependencies, domain impurity, DTO/entity leakage, public API
   exposure, direct calls across layers, cycles, or custom local policies.
5. Reference only real module paths and package names found in this repository.
   Do not ship placeholder rules or imported architecture assumptions from
   another project.
6. If the intended layering cannot be inferred with confidence, ask a short
   targeted question instead of guessing.

## Phase 3 — Plan suitable architecture tests
1. Propose a concise architecture-test plan before coding. For each planned
   guardrail, state the rule objective, the real modules/packages it applies to,
   why Konture is the right tool, and whether the rule encodes an existing
   convention or a new policy.
2. Prefer tests that enforce the project's actual architecture rather than
   generic examples. Do not add a rule just because Konture can express it.
3. If an intended rule would fail against the current codebase, call out the
   existing violation and recommend whether to fix it first, quarantine it with
   an explicit exception, or scope the rule to new code only.

## Phase 4 — Locate the architecture test module
1. Prefer the dedicated Konture test module already present in the repo, commonly
   `:konture-test` or `:architecture-tests`.
2. If no dedicated module exists, report that the setup is missing and recommend
   creating one rather than placing architecture tests inside production modules.
3. Confirm the test module has `testImplementation(project(":..."))`
   dependencies on the production modules the rules need to inspect.

## Phase 5 — Write compile-safe Konture tests
Use the Konture DSL APIs that are present in the installed project version.
Prefer existing local test style, naming, JUnit/Kotest conventions, and package
layout.

Common rule shapes:

```kotlin
import io.github.baole.konture.*

Konture.architecture {
    modules {
        that().haveNamePath(":core:domain")
        should().notDependOnModule(":core:data")
    }

    classes {
        that().resideInAPackage("..domain..")
        should().onlyDependOnClassesInAnyPackage("..domain..", "kotlin..")
    }
}
```

```kotlin
import io.github.baole.konture.*

Konture.layered {
    val presentation = layer("presentation") definedBy "..presentation.."
    val domain = layer("domain") definedBy "..domain.."
    val data = layer("data") definedBy "..data.."

    where(presentation) {
        mayOnlyAccessLayers(domain)
    }
    where(data) {
        mayOnlyAccessLayers(domain)
    }
    where(domain) {
        mayOnlyAccessLayers()
    }
}
```

```kotlin
import io.github.baole.konture.*

Konture.modules()
    .that().haveNamePath(":feature:checkout")
    .should().notDependOnModule(":feature:payment")
    .check()
```

```kotlin
import io.github.baole.konture.*

Konture.classes()
    .that().haveNameEndingWith("Repository")
    .should().beInterfaces()
    .check()
```

Available module filters/assertions include:
- Filters: `haveNamePath(path)`, `haveNameMatching(glob)`
- Assertions: `onlyDependOnModules(vararg paths)`, `notDependOnModule(path)`

Available class filters/assertions include:
- Filters: `resideInAPackage(pkg)`, `haveNameEndingWith(suffix)`,
  `areInterfaces()`, `haveAnnotationOf(fqName)`
- Assertions: `beInterfaces()`, `beInternal()`, `bePublic()`,
  `notHaveSignaturesWithTypesAnnotatedWith(annotation)`,
  `notDependOnClassesInAnyPackage(vararg pkgs)`,
  `onlyDependOnClassesInAnyPackage(vararg pkgs)`,
  `onlyBeAccessedByAnyPackage(vararg pkgs)`

For cycle prevention, use:

```kotlin
import io.github.baole.konture.*

Konture.assertNoCycles()
```

Do not hallucinate Konture APIs. If a rule needs DSL behavior not shown in this
project, inspect the installed library/version before coding.

## Phase 6 — Verify the rule
1. Run the relevant architecture test task, usually
   `./gradlew :konture-test:test` or `./gradlew :architecture-tests:test`.
2. When practical, prove the rule can fail red by temporarily introducing a real
   violation or pointing to an existing violation. Remove temporary violations
   before finishing.
3. If the codebase already violates the intended rule, do not hide the violation
   with broad exclusions. Report the violation and ask whether to fix it,
   quarantine it with an explicit documented exception, or scope the rule to new
   code only.
4. Report the final guardrails added or changed, the module/package names they
   protect, and the command used to run them.

Constraints:
- Keep the diff scoped to architecture-test files and only the build/test-module
  wiring required for those tests.
- Preserve existing project conventions for test framework, source layout,
  naming, imports, and formatting.
- Avoid broad wildcard bans until checking actual imports; for example, banning
  `kotlinx..` may accidentally forbid legitimate coroutines or serialization
  usage.
- For KMP source-set rules, inspect actual source sets (`commonMain`,
  `androidMain`, `iosMain`, `jvmMain`, etc.) before defining what counts as a
  platform-only dependency.
- Flag overlaps with existing Konsist, detekt, or custom architecture checks
  instead of silently duplicating coverage.
````
