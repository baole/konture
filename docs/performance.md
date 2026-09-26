# Performance & Scale

Konture is engineered for large-scale multi-module Kotlin repositories (Android, Kotlin Multiplatform, and JVM server backends). Architecture testing suites in 100+ module repositories must remain cheap and rapid to prevent developers and CI pipelines from abandoning architectural guardrails.

---

## ⚡ Large-Repository Benchmarks

Konture includes dedicated scalability benchmarks (`LargeRepositoryPerformanceBenchmarkTest`) simulating enterprise architectures with **100+ Gradle modules**, deep dependency hierarchies (`:core`, `:domain`, `:data`, `:feature`, `:app`), and hundreds of Kotlin source files and class declarations.

| Evaluation Scenario | 100+ Module Threshold | Typical Execution Time |
| :--- | :--- | :--- |
| **Cold Run (Clean build, no AST cache)** | `< 5.0 seconds` | ~1.5 – 3.2s |
| **Incremental Run (Cached AST / Fingerprint)** | `< 1.0 second` | ~0.15 – 0.45s |

> [!NOTE]
> Benchmarks are run on commodity hardware and verified in CI. Because Konture relies on an in-memory synthetic or precomputed Gradle metadata model rather than full JVM reflection or bytecode classloading, memory overhead scales linearly with project size rather than exponentially.

---

## 🚀 Parallel Rule Evaluation

By default, Konture evaluates rule suites sequentially. When checking multiple independent rule scopes (e.g. `classes { }`, `files { }`, `modules { }`, and `slices { }`), you can enable **parallel rule evaluation** to execute rule suites concurrently using Kotlin Coroutines (`kotlinx.coroutines`).

### Enabling Parallel Evaluation

#### In Gradle Build Configuration

```kotlin
// build.gradle.kts (inside your architecture test module)
konture {
    analysis {
        parallel = true
        maxWorkers = 4 // Optional: limit worker coroutines (default 0 = unconstrained)
    }
}
```

#### Via Command Line Flags

You can enable parallel evaluation or adjust worker limits dynamically on CI runners:

```bash
./gradlew test -Dkonture.parallel.enabled=true -Dkonture.parallel.maxWorkers=4
```

#### Programmatically in Tests

```kotlin
import io.github.baole.konture.Konture
import io.github.baole.konture.architecture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EnterpriseArchitectureTest {

    @BeforeEach
    fun setUp() {
        Konture.parallel = true
        Konture.parallelMaxWorkers = 4
    }

    @Test
    fun `verify architecture guardrails concurrently`() {
        Konture.architecture {
            classes {
                that().inPackage("..domain..")
                should().notDependOnClassesThat().inPackage("..presentation..")
            }

            files {
                that().nameEndsWith("Repository.kt")
                should().inPackage("..data..")
            }

            modules {
                that().haveNameStartingWith(":feature:")
                should().notDependOn(":app")
            }
        }
    }
}
```

---

## 🛡️ Thread Safety and Determinism

Parallel execution in Konture adheres to strict safety guarantees:

1. **Deterministic Failure Ordering**: Regardless of which rule suite finishes first, violation errors and assertion reports are collected, sorted, and presented in the exact order the rules were declared in code.
2. **State Isolation**: Each worker coroutine receives an isolated replica of the parent `KontureRuntimeState` via ThreadLocal propagation, preserving metadata tags, severities, and baseline configurations across worker threads.
3. **Synchronized PSI & Baseline Access**: Shared PSI environment creation and baseline cache lookups are guarded by internal synchronizations, preventing race conditions during concurrent AST analysis.

---

## 💡 Best Practices for 100+ Module Monorepos

1. **Combine Incremental Analysis and Persistent Caching**:
   Enable both `incremental = true` and `cache = true` in your Gradle `analysis { }` block. Unchanged files skip AST parsing entirely across test runs.
2. **Dedicated Architecture Test Module**:
   Host your architecture tests in a dedicated Gradle module (e.g., `:architecture-test` or `:lint:architecture`). This isolates test dependencies from production modules and leverages Gradle's build cache.
3. **Tune Worker Limits in CI**:
   In containerized CI environments (such as Docker or Kubernetes executors with constrained CPU quotas), set `maxWorkers` matching the allotted CPU cores (`-Dkonture.parallel.maxWorkers=2` or `4`) to prevent CPU throttling.
4. **Prefer Scoped Module Matchers**:
   Scope rules to target packages or module globs (e.g., `that().inPackage("..domain..")` or `that().haveNameStartingWith(":feature:")`) rather than analyzing all declarations for every rule.
