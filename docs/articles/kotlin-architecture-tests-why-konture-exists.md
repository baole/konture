# Kotlin Architecture Tests: Why Konture Exists

_Kotlin architecture has two sources of truth: the Gradle graph that decides what can link, and the Kotlin source model that decides what the code actually says. Konture exists to test both._

Consider a familiar rule:

> The domain layer must not depend on the data layer.

In a Kotlin project, that rule can fail in two different places.

It can fail in the build graph:

```kotlin
// domain/build.gradle.kts
dependencies {
    implementation(project(":data"))
}
```

It can also fail in source:

```kotlin
package com.acme.domain

import com.acme.data.UserRepositoryImpl
```

Those failures are related, but they are not identical. The first is a physical Gradle module dependency. The second is a source-level reference. A serious architecture-testing tool for Kotlin has to understand both, because large Kotlin systems are governed by both.

That is the reason Konture exists.

## The Incomplete Views

Existing tools are useful. Konture is not trying to replace the compiler, the linter, the test runner, or every architecture-testing library. The issue is that each view of a Kotlin project leaves something out.

| View | What it sees well | What it misses |
| --- | --- | --- |
| JVM bytecode | Compiled classes and class dependencies | Kotlin source intent, source sets, Gradle project boundaries, some compiler-plugin effects |
| Kotlin source scan | Packages, imports, declarations, modifiers, annotations | The real Gradle module graph and project dependency policy |
| Gradle graph inspection | Modules, source sets, declared project dependencies | Source-level imports, public signatures, visibility, and type leakage |
| Linters | Local style and single-file quality | Whole-project structure and architectural ownership |

Kotlin architecture sits across those views.

### Bytecode Is Valuable, But It Is Not the Whole Kotlin Program

[ArchUnit](https://www.archunit.org) is mature and proven for JVM systems. If your architectural rule can be answered from compiled classes, bytecode analysis is often a strong fit.

Modern Kotlin projects often need more context than that.

Kotlin source constructs do not always map neatly to the source-level design a team wants to protect. Top-level functions and extension functions compile into generated holder classes. Inline functions move bodies into call sites. `object`, delegated properties, and compiler plugins can introduce generated structures that are important to the runtime but noisy for a source-level design rule.

That does not make bytecode analysis wrong. It means bytecode is the wrong primary lens for rules such as:

- Does `:feature:checkout:impl` depend on a sibling implementation module?
- Does `commonMain` import an Android API?
- Does a public domain signature expose a persistence type?
- Does an `impl` package remain internal in source?

Those are Kotlin and Gradle architecture questions, not only JVM class questions.

### Source Scanning Is Useful, But Folders Are Not the Build

Kotlin-first source scanners are good at declarations, imports, naming, annotations, and visibility. They can express many rules that bytecode tools make awkward.

But a source directory is not a Gradle project. A package name is not a module dependency. A folder named `api` is not the same thing as a module that other projects depend on through `api` or `implementation`.

That distinction matters in Android and Kotlin Multiplatform projects:

```text
:app
:core:domain
:core:data
:feature:checkout:api
:feature:checkout:impl
:feature:profile:api
:feature:profile:impl
:shared
:androidApp
:iosApp
```

The build already knows which modules exist, which source sets are production source sets, and which project dependencies are declared. Architecture tests should use that information instead of reconstructing it from naming conventions alone.

### Linters Are Not Architecture Testers

`detekt` and `ktlint` are excellent at local checks. They are not designed to answer whole-system questions:

- Does this module depend on a forbidden sibling module?
- Did a feature implementation become visible to another implementation?
- Does the project graph contain a cycle?
- Does a public API expose a type owned by another layer?

Those are not formatting problems. They are ownership and dependency problems.

## Konture's Two-View Model

Konture combines the build view and the source view.

The build view includes:

- Gradle modules.
- Source sets.
- Production Kotlin source directories.
- Declared project dependencies.
- Applied plugin context.

The source view includes:

- Files, packages, and imports.
- Classes, interfaces, functions, and properties.
- Visibility and annotations.
- References between project classes.

```mermaid
flowchart TB
    Start["Kotlin project"]

    subgraph BuildView ["Build graph view"]
        GB["Gradle build"]
        KP["Konture Gradle plugin"]
        LM["Layout metadata<br/>modules, source sets, dependencies"]
        GB --> KP --> LM
    end

    subgraph SourceView ["Source code view"]
        KF["Kotlin source files"]
        PA["PSI-based parser"]
        SM["Source model<br/>packages, imports, declarations"]
        KF --> PA --> SM
    end

    Start --> GB
    Start --> KF

    LM --> AT["Architecture tests<br/>module rules + source rules"]
    SM --> AT
    AT --> TR["Test result<br/>pass or fail the boundary"]

    style Start fill:#f8fafc,stroke:#64748b,stroke-width:2px
    style BuildView fill:#faf5ff,stroke:#a855f7,stroke-width:2px
    style SourceView fill:#f0fdf4,stroke:#22c55e,stroke-width:2px
    style AT fill:#dbeafe,stroke:#2563eb,stroke-width:2px
    style TR fill:#ecfdf5,stroke:#059669,stroke-width:2px
```

That lets a team express a boundary at both levels:

```kotlin
Konture.architecture {
    modules {
        that().haveNamePath(":domain")
        should().notDependOnModule(":data")
    }

    classes {
        that().resideInAPackage("..domain..")
        should().onlyDependOnClassesInAnyPackage(
            "..domain..",
            "kotlin..",
            "java..",
        )
    }
}
```

The module rule catches the physical build dependency. The source rule catches the source-level reference pattern. Used together, they give better coverage than either view alone.

## What Konture Is

Konture is a Kotlin architecture-testing library with two coordinated parts:

- A Gradle plugin that captures project layout, source sets, and module dependencies.
- An assertion library that lets teams write architecture rules as ordinary Kotlin tests.

It does not require a custom test runner. Architecture tests can run under JUnit, Kotest, TestBalloon, or another Kotlin/JVM runner your project already uses.

Konture is also architecture-agnostic. It does not prescribe Clean Architecture, MVVM, hexagonal architecture, feature slicing, or DDD. Those are design choices. Konture's job is to make the chosen design executable.

That distinction matters. An Android team may protect feature API and implementation modules. A backend team may protect ports and adapters. A KMP team may keep shared code free of platform APIs. A library team may prevent implementation types from leaking into public packages.

Konture should encode the team's policy, not invent one.

## The Three Structural Jobs

Architecture tests are most valuable when they protect decisions that are expensive to repair later. In Kotlin systems, those decisions usually cluster into three jobs.

| Job | Threat | Build-level rule | Source-level rule |
| --- | --- | --- | --- |
| Logical isolation | Layers or modules depend in forbidden directions | `:domain` does not depend on `:data`; feature implementations do not depend on sibling implementations | Domain packages do not import data, UI, framework, or platform packages |
| API hermeticity | Implementation detail becomes public contract | API modules stay separate from implementation modules | Public signatures do not expose persistence, transport, or framework types |
| Mechanical hygiene | Structure becomes harder to navigate and build | Module graph has no cycles | Files avoid wildcard imports, class/file mismatch, or uncontrolled generated-code zones |

The point is not to create a large rule set. The point is to protect the small number of structural choices that keep the codebase changeable.

## Gradle Awareness Is Platform Engineering

Kotlin teams often use modules to manage ownership, compile scope, and feature independence. That makes the Gradle graph a platform concern, not merely a build-file detail.

Consider this common policy:

> Feature implementation modules must not depend on other feature implementation modules.

```mermaid
flowchart TD
    App[":app"]

    subgraph CheckoutFeature ["Checkout Feature"]
        CheckoutImpl[":feature:checkout:impl"]
        CheckoutApi[":feature:checkout:api"]
    end

    subgraph ProfileFeature ["Profile Feature"]
        ProfileImpl[":feature:profile:impl"]
        ProfileApi[":feature:profile:api"]
    end

    subgraph Core ["Core Modules"]
        Domain[":core:domain"]
        Network[":core:network"]
    end

    App --> CheckoutImpl
    App --> ProfileImpl
    CheckoutImpl --> CheckoutApi
    CheckoutImpl --> Domain
    CheckoutImpl --> Network
    ProfileImpl --> ProfileApi
    ProfileImpl --> Domain
    CheckoutImpl -.->|"forbidden"| ProfileImpl

    style App fill:#f8fafc,stroke:#64748b,stroke-width:1px
    style CheckoutImpl fill:#f0f9ff,stroke:#0284c7,stroke-width:1px
    style CheckoutApi fill:#e0f2fe,stroke:#0ea5e9,stroke-width:1px
    style ProfileImpl fill:#f0f9ff,stroke:#0284c7,stroke-width:1px
    style ProfileApi fill:#e0f2fe,stroke:#0ea5e9,stroke-width:1px
    style Domain fill:#f0fdf4,stroke:#16a34a,stroke-width:1px
    style Network fill:#f0fdf4,stroke:#16a34a,stroke-width:1px
    linkStyle 7 stroke:#ef4444,stroke-width:2px,stroke-dasharray: 5 5;
```

If `:feature:checkout:impl` adds this dependency:

```kotlin
implementation(project(":feature:profile:impl"))
```

the build may still pass. The immediate feature may even ship faster. But the module graph now says checkout is coupled to profile internals.

That has practical consequences:

- A profile implementation change can force more downstream work than necessary.
- Build cache reuse becomes less effective because internal changes cross feature boundaries.
- Refactoring profile internals becomes harder because another feature can now depend on them.
- Reviewers have to notice build-file drift manually.

A Gradle-aware rule makes the boundary executable:

```kotlin
Konture.modules {
    that().haveNameMatching(":feature:**:impl")
    should().onlyDependOnModules(
        ":feature:**:api",
        ":core:**",
        ":shared",
    )
}
```

This is not only "clean architecture." It is build health and ownership encoded as a test.

## Source Awareness Protects Semantic Boundaries

A clean Gradle graph does not prove clean source semantics.

For example, `:data` may correctly depend on `:domain` so it can implement domain interfaces. But a developer can still leak a persistence model into a domain-facing API:

```kotlin
package com.acme.domain

import com.acme.data.UserEntity

interface UserRepository {
    fun getUser(id: UserId): UserEntity
}
```

The build graph alone may not tell you this is wrong. The source model can.

Architecture tests can inspect imports, packages, declarations, visibility, and signatures:

```kotlin
Konture.classes {
    that().resideInAPackage("..domain..")
    should().onlyDependOnClassesInAnyPackage(
        "..domain..",
        "kotlin..",
        "java..",
    )
}
```

For external frameworks, a custom import predicate can make the policy explicit:

```kotlin
Konture.scopeFromPackage("com.acme.domain")
    .assertTrue("Domain must not import framework or persistence APIs") { cls ->
        cls.imports.none { fqName ->
            fqName.startsWith("android.") ||
                fqName.startsWith("androidx.compose.") ||
                fqName.startsWith("org.springframework.") ||
                fqName.startsWith("jakarta.persistence.")
        }
    }
```

That is the source-level half of architecture governance: not just which modules can link, but which concepts are allowed to appear in which parts of the code.

## Tradeoffs and Failure Modes

Architecture tests deserve the same skepticism as any other production guardrail. Bad rules create drag.

Common failure modes:

- **Overbroad rules**: Banning `kotlinx..` from domain may accidentally block legitimate use of coroutines or serialization.
- **Hidden exceptions**: Excluding large legacy packages can make the rule look stronger than it is.
- **Generated code noise**: Generated sources may need explicit treatment so the rule protects authored code.
- **KMP complexity**: `commonMain`, `androidMain`, and `iosMain` often need different policies.
- **Mixed Java/Kotlin projects**: A Kotlin source rule may not cover Java code unless the project deliberately accounts for it.
- **Rule maintenance**: Architecture evolves. Tests must evolve with deliberate architecture decisions, not block them by accident.

These tradeoffs do not weaken the case for architecture tests. They define the bar for using them responsibly.

Start with stable, high-signal rules. Make exceptions visible. Prove every rule can fail. Treat rule changes as architecture changes, not as a way to get CI green.

## Evidence From the Showcase Suites

The repository includes showcase suites that exercise Konture at different levels of complexity.

The smallest Gradle showcase models a classic `:app`, `:domain`, and `:data` project. Its architecture tests verify the module graph, source package boundaries, repository contracts, use case placement, type-leakage rules, and access rules. It also includes a negative test that asserts a deliberately wrong module rule throws an `AssertionError`, which is a useful pattern for proving a rule can actually fail.

The larger showcases are more representative of staff-level platform concerns:

- The Now in Android suite checks that feature modules do not depend on `:app`, do not bypass repositories to reach database or network modules, keep feature API modules independent from feature implementation modules, prevent feature implementation-to-implementation coupling, and keep ViewModels away from Android framework imports.
- The KotlinConf KMP suite checks that the shared `:core` module stays a leaf dependency, client app modules do not depend on backend implementation, backend code does not depend on frontend client modules, and backend routes do not directly import repositories or database schemas.

Those are not toy style rules. They are executable versions of ownership and platform constraints: feature decoupling, shared-model purity, backend/frontend separation, route-service boundaries, and API surface control.

The lightweight Gradle showcase can be run directly:

```bash
./gradlew -p showcases/sample-gradle :konture-test:test
```

In this repository, that command completes successfully and runs the dedicated architecture-test module after generating Konture's layout and dependency metadata.

## Why This Matters With AI-Assisted Development

AI coding assistants tend to optimize for local progress: import the visible class, add the missing dependency, satisfy the immediate test.

That behavior is useful, but it can cross boundaries the agent does not fully model. Architecture tests give agents the same structural feedback humans get:

- The module dependency is forbidden.
- The import crosses a layer.
- The public signature leaks an implementation type.
- The package visibility is wrong.

This is not autonomous architecture. It is executable feedback. The team still owns the design; the build makes violations concrete enough for humans and tools to repair.

## When Konture Is a Good Fit

Konture is a good fit when the rules you care about span Kotlin source and Gradle structure:

- Gradle module boundaries and acyclic project graphs.
- Feature `:api` and `:impl` separation.
- Domain or shared KMP code staying independent from frameworks and platform APIs.
- Public API signatures avoiding persistence, transport, or UI types.
- Kotlin visibility conventions such as keeping implementation packages `internal`.
- File and package conventions that require project-wide context.

It is less useful for formatting, ordinary style, and checks a standard linter already handles well. Use the cheapest tool that can enforce the rule accurately.

## The Core Idea

Architecture should not rely on memory.

If a boundary matters enough to protect in every review, it is a candidate for an executable test. If breaking it slows builds, leaks APIs, couples teams, or makes AI-assisted changes riskier, the repository should be able to say so.

Konture exists because Kotlin architecture is not only in bytecode, not only in source files, and not only in Gradle build files. It lives in the relationship between them.

---

## Continue the Series

- [Kotlin Architecture Tests: What They Are and Why They Matter](kotlin-architecture-tests-what-and-why.md)
- [Kotlin Architecture Tests with Konture: A Practical Guide](kotlin-architecture-tests-with-konture.md)
