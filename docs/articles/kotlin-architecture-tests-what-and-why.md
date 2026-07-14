# Kotlin Architecture Tests: What They Are and Why They Matter

_Your Kotlin project can compile, pass every unit test, satisfy every linter, and still violate the architecture your team depends on, especially when humans and AI agents are both moving fast._

![Architecture tests as an early quality gate](../assets/images/architecture-tests-quality-gate.svg)

Most teams already have several quality gates.

The compiler checks whether the code is valid Kotlin. The linter checks style and local code smells. Unit tests check expected behavior.

Architecture tests add a different gate:

> Does this change still respect the structure of the system?

That question matters because architecture violations often look like normal code. A human developer or AI agent can make a change that compiles, passes tests, and still crosses a boundary the team depends on.

For example, a Kotlin use case might import a database implementation.

```kotlin
package com.acme.checkout.domain

import com.acme.checkout.data.SqlUserRepository

class GetUserUseCase(
    private val repository: SqlUserRepository,
)
```

The code compiles. The unit tests may still pass. `ktlint` probably has nothing to say.

And yet, something important just broke: the domain layer now knows about a data-layer implementation. A boundary that was supposed to protect the business logic has become a suggestion.

That is the space architecture tests are meant to cover.

They are not a replacement for the compiler, unit tests, integration tests, or linters. They answer a different question:

> Does this code still respect the structure we said the system should have?

For Kotlin teams, that question matters more every year. Projects are larger. Android and Kotlin Multiplatform applications are more modular. Backend systems are often split into domain, application, infrastructure, and adapter modules. AI coding assistants can generate working code quickly, but they sometimes choose the shortest compiling path: import the available class, add the missing Gradle dependency, or reuse a nearby implementation detail. Those choices can be locally correct and architecturally wrong.

Architecture tests turn those boundaries into executable checks.

## The Green Build Illusion

A green build is necessary. It is not sufficient.

The compiler checks whether the code is legal Kotlin. It checks syntax, type safety, visibility rules, and whether referenced symbols are available on the classpath.

Linters check local code quality. They catch formatting issues, naming conventions, complexity thresholds, unused imports, and many single-file smells.

Unit tests check behavior. They tell you whether a function, class, or use case does what a test expects it to do.

None of those tools knows your architectural intent.

The compiler does not know that `:domain` should not depend on `:data`. If the dependency exists in Gradle, the compiler accepts the import.

A linter does not know that a network DTO is leaking into the UI layer. It mostly sees files and syntax, not the ownership rules behind your types.

A unit test does not fail because a feature module depends on a sibling feature module. It fails only if the behavior under test breaks.

Architecture violations are often valid code. That is why they slip through.

## Architecture Erodes Through Small Changes

Most architecture problems do not arrive as a big redesign.

They arrive as tiny shortcuts:

- A controller calls a repository directly because it was faster than adding a service method.
- A domain model starts referencing a network DTO.
- A feature implementation module imports another feature implementation module.
- An `impl` class is left `public` and becomes someone else's dependency.
- A UI state exposes a network response DTO instead of a mapped presentation model.
- An AI agent adds a forbidden module dependency because it makes the current task compile.
- A legacy package gains one more consumer because the migration is already messy.

Each change is easy to justify locally. The code works. The feature ships.

The cost appears later.

Refactoring becomes harder because internal details have become public contracts. Build times get worse because modules depend on each other in unnecessary directions. Tests become heavier because domain code now knows about frameworks. AI-generated patches become risky when the repository has no executable way to say, "this boundary is real."

Architecture tests are a way to make those rules visible to the build early, before a shortcut becomes a pattern other code starts to copy.

## What Architecture Tests Check

Good architecture tests usually protect six kinds of decisions.

### 1. Dependency Direction and Layer Isolation

In Clean Architecture, layered architecture, or ports and adapters, outer layers can depend inward. The domain or core layer should not depend outward on UI, database, network, framework, or infrastructure details.

```mermaid
flowchart TB
    Outer["Outer layers<br/>UI, controllers, data adapters,<br/>network clients, frameworks"]
    Domain["Core domain<br/>entities, use cases, contracts"]
    Details["Concrete details<br/>Compose, Room, Retrofit,<br/>SQLUserRepository"]

    Outer -->|"allowed: depend inward"| Domain
    Domain -.->|"forbidden: know outward"| Details

    style Outer fill:#f8fafc,stroke:#64748b,stroke-width:2px
    style Domain fill:#dbeafe,stroke:#2563eb,stroke-width:2px
    style Details fill:#fee2e2,stroke:#dc2626,stroke-width:2px
    linkStyle 1 stroke:#dc2626,stroke-width:2px,stroke-dasharray: 6 6;
```

Typical policies:

- Domain must not depend on data.
- Domain must not import Spring, Android, Compose, Room, SQLDelight, or Ktor server APIs.
- Application services may depend on domain, but not directly on web controllers.

The compiler cannot infer "domain purity." If the type is visible, the compiler allows it.

### 2. Module Boundary Enforcement

In a multi-module Kotlin project, Gradle modules are part of the architecture.

```mermaid
graph TD
    app[":app"] --> checkout[":feature:checkout"]
    app --> profile[":feature:profile"]
    checkout --> core[":core:network"]
    profile --> core
    checkout -.->|"PROHIBITED: Sideways Dependency"| profile

    style app fill:#f1f5f9,stroke:#94a3b8,stroke-width:2px
    style checkout fill:#e0f2fe,stroke:#0284c7,stroke-width:2px
    style profile fill:#e0f2fe,stroke:#0284c7,stroke-width:2px
    style core fill:#ecfdf5,stroke:#10b981,stroke-width:2px
    linkStyle 4 stroke:#ef4444,stroke-width:2px,stroke-dasharray: 5 5;
```

Typical policies:

- `:feature:checkout` must not depend on `:feature:profile`.
- `:core:domain` must not depend on `:app`.
- Implementation modules must stay behind API modules.
- The build graph must remain acyclic.

Gradle can enforce missing dependencies. It cannot stop someone from adding a new dependency that violates the intended design.

### 3. Cross-Layer Type Leakage

Types from one layer should not leak into another layer's public API.

Typical policies:

- Database entities should not be returned from REST controllers.
- Room entities should not appear in Composable or ViewModel signatures.
- Network DTOs should not appear in domain use cases.
- Transport, persistence, or UI-specific types should not appear in domain APIs.

The compiler is happy to pass valid types around. It does not know which types represent persistence, transport, UI, or domain concepts.

### 4. Layer-Crossing Calls

Architecture often says that components should move through layers in order.

Typical policies:

- Controllers call services, not repositories directly.
- Composables call ViewModels, not Retrofit services.
- Route handlers call application services, not SQL adapters.
- UI modules do not call infrastructure modules.

The compiler sees a callable function. It does not know the call skipped the layer where validation, authorization, transactions, or logging live.

### 5. Dependency Injection and Wiring Conventions

Some structural failures appear only at runtime.

Typical policies:

- Feature modules should not override core DI bindings.
- Modules should not register unused or duplicate bindings.
- DI modules should live in approved packages.
- Adapter implementations should be bound to domain interfaces, not consumed directly.

Some of these checks belong in DI-specific integration tests. Some can be expressed as source or module rules. The important point is that wiring is architectural, not only behavioral.

### 6. API Surface and Visibility

Kotlin's language features change how we design public contracts. By default, Kotlin classes and members are `public`. In multi-module environments, this makes accidental API exposure remarkably easy. 

Furthermore, Kotlin projects rely heavily on idiomatic features that Java-centric tools struggle to analyze:
- **Top-level functions and extension functions** (which compile to synthetic classes);
- **Kotlin `object` and `companion object` declarations** (which govern singleton patterns);
- **Visibility controls** like `internal` (which are easily bypassed in compiled bytecode if not carefully verified).

Typical policies:
- Classes in `..impl..` packages must be explicitly marked `internal`.
- Public API packages must contain documented interfaces, sealed classes, or clean DTOs only.
- Implementation classes must not leak across module boundaries.
- Top-level extension utilities must reside in designated utility packages.

Once another module imports an accidental public helper or direct implementation class, refactoring it later becomes a breaking change.

## Architecture Tests Are Living Documentation

Architecture diagrams are useful. README files are useful. Onboarding docs are useful.

But none of them fails CI.

An architecture test gives a rule an executable form:

```kotlin
Konture.architecture {
    // 1. Module-level structural rules
    modules {
        that().haveNamePath(":domain")
        should().notDependOnModule(":data", ":app")
    }

    // 2. Class-level source conventions
    classes {
        that().resideInAPackage("..domain..")
        that().haveNameEndingWith("Repository")
        should().beInterfaces()
    }
}
```

The test name becomes documentation:

```kotlin
@Test
fun `domain must not depend on data or app modules`() {
    // rule here
}
```

When the test fails, the architecture is no longer an opinion in a code review. It is a broken contract.

That changes the conversation.

Instead of saying:

> Please do not import data from domain.

The repository says:

> This import violates the domain boundary. Here is the file that crossed it.

That is better feedback for humans. It is also much better feedback for AI coding agents.

## Why This Matters More with AI-Generated Code

AI coding tools are good at local completion. They can find an available class, import it, make a test pass, and move on.

Architecture is often global context.

An agent may not know that a repository implementation is forbidden in the domain layer. It may not know that feature modules should communicate through API modules. It may not know that a network DTO should be mapped before it reaches a UI state model. Even when the instruction is present somewhere in the repository, the agent may miss it, overfit to the immediate failing test, or make a dependency change that looks harmless in isolation.

Prompt instructions help:

```text
Keep domain independent from data.
Do not add sideways feature dependencies.
Map network DTOs before they reach UI state.
```

But prompt instructions are not quality gates.

An architecture test is.

If AI-generated code crosses a boundary, the same build that checks human-written code catches it early. 

Because architecture tests run as standard unit tests, they produce rich, deterministic stdout logs that pinpoint the exact files, lines, and layers that violated the rule. This transforms architecture tests from a defensive gate into a **self-healing feedback loop**:

1. **Detection**: An AI agent makes an implementation shortcut that violates a boundary.
2. **Failure**: The architecture test fails in local verification or CI, printing exactly which file imported the forbidden dependency.
3. **Self-Correction**: The agent parses the test failure stdout, understands the structural constraint, and automatically refactors its code to use the correct domain interface.

By making structural rules executable, teams can securely leverage high-speed AI generation without worrying about gradual codebase erosion or the cognitive load of reviewing bloated prompt templates.

## What Makes a Good Architecture Test

A good architecture test is specific.

Bad:

```text
The project should follow clean architecture.
```

Good:

```text
Classes in packages matching ..domain.. may depend only on ..domain.., kotlin.., and java...
```

A good architecture test has a clear owner.

If the team cannot explain why a rule exists, it probably should not block CI.

A good architecture test is scoped.

Generated code, legacy migration areas, test fixtures, and intentionally unstable packages may need explicit exclusions. That is fine, as long as the exception is deliberate and visible.

A good architecture test fails for one reason.

Do not bundle ten unrelated policies into one test. A red architecture test should tell the developer which design decision was broken.

## Where Architecture Tests Fit in the Toolchain

Architecture tests sit beside the tools you already use.

```mermaid
flowchart TD
    subgraph QualityGates ["Verification Toolchain Hierarchy"]
        direction BT
        L1["Compiler<br/><b>Is it valid Kotlin?</b>"]
        L2["Linter<br/><b>Local style & basic smells</b>"]
        L3["Unit Tests<br/><b>Isolated component behavior</b>"]
        L4["Integration Tests<br/><b>Collaborative system behavior</b>"]
        L5["Architecture Tests<br/><b>Systemic structure & boundary rules</b>"]

        L1 --> L2
        L2 --> L3
        L3 --> L4
        L4 --> L5
    end

    style QualityGates fill:#fafafa,stroke:#e4e4e7,stroke-width:1px
    style L1 fill:#f1f5f9,stroke:#94a3b8,stroke-width:1px
    style L2 fill:#f1f5f9,stroke:#94a3b8,stroke-width:1px
    style L3 fill:#e0f2fe,stroke:#38bdf8,stroke-width:1px
    style L4 fill:#e0f2fe,stroke:#38bdf8,stroke-width:1px
    style L5 fill:#ecfdf5,stroke:#10b981,stroke-width:2px
```

| Tool | Main question |
| --- | --- |
| Compiler | Is this valid Kotlin? |
| Linter | Does this file follow local code style and simple quality rules? |
| Unit test | Does this unit behave correctly? |
| Integration test | Do these components work together? |
| Architecture test | Does this code still respect the intended structure? |

That final question is the missing one in many Kotlin projects.

## The Practical Payoff

Architecture tests help teams:

- Catch dependency drift early.
- Keep domain logic framework-independent.
- Protect Gradle module boundaries.
- Prevent DTO and entity leakage.
- Keep KMP shared code portable.
- Make public APIs intentional.
- Give AI coding agents executable feedback.
- Turn architectural decisions into CI-enforced contracts.

The point is not to make architecture rigid.

The point is to make architecture explicit.

Once a team has chosen its structure, the build should help protect it.

In the next article, we will look at why existing options fall short for modern Kotlin ecosystems. We will explore the friction of using Java bytecode scanners on native Kotlin constructs, and why Konture was built around a specific philosophy: **true architectural integrity requires testing both the build configuration graph and the Kotlin source AST in a unified, compiler-like test loop.**

---

## Continue the Series

- [Kotlin Architecture Tests: Why Konture Exists](kotlin-architecture-tests-why-konture-exists.md)
- [Kotlin Architecture Tests with Konture: A Practical Guide](kotlin-architecture-tests-with-konture.md)
