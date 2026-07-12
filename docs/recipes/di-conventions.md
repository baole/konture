# DI Graph Resolution & Wiring Correctness

Dependency Injection (DI) wiring is the backbone of modern apps. If a DI container isn't wired properly, components fail at runtime due to missing dependencies, invalid scopes, or mismatched qualifiers.

We advocate a **Dual-Testing Model** to enforce DI safety:
1. **Dynamic DI Verifiers (Runtime Integration)**: Running container dry-runs (e.g. Koin's `checkModules()`, Spring's `@SpringBootTest`, or Hilt test configurations) to prove runtime wiring correctness.
2. **Static Convention Assertions (Konture)**: Enforcing static class structures and module visibility conventions (e.g., verifying that Koin modules are declared private or that all Dagger/Hilt installation binders follow exact naming standards).

---

## 💡 The Rationale
* **Zero Runtime Crashes**: Verifying DI module structures in your test suite catches missing constructor objects, invalid singleton bindings, and cyclic definitions before shipping code.
* **Encapsulation & Architecture Integrity**: Enforcing that DI configuration classes (like Koin modules or Spring `@Configuration` templates) are marked package-private (`internal` or `private`) prevents outer layers from bypassing API boundaries and calling these files.
* **Unified Code Quality**: Matching DI module files with standard suffix rules (e.g., must end in `Module` or `Module.kt`) keeps multi-developer projects organized.

---

## 🛠️ Implementation with Konture & Container Frameworks

### 1. Koin: Dynamic dry-runs + Konture Static checks

First, use Koin's native verifier to dynamically spin up and dry-run your module definitions:

```kotlin
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class KoinWiringTest : KoinTest {

    @Test
    fun `verify koin modules are structurally correct`() {
        // Dynamic Dry-Run: fails immediately if any required dependencies are missing
        appModule.verify()
    }
}
```

Next, use Konture to statically verify that Koin modules are kept `internal` and located inside designated `di` packages:

```kotlin
import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class KoinStaticConventionTest {

    @Test
    fun `koin module configurations must be internal and kept in di packages`() {
        Konture.classes {
            that().haveNameMatching("..di..")
                .should().beInternal()
        }
    }
}
```

### 2. Spring Boot: Custom Auto-Configuration Boundaries

Assert that Spring `@Configuration` files must only reside inside dedicated infrastructure adapter modules, keeping the core domain completely framework-agnostic:

```kotlin
import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class SpringConfigurationSanityTest {

    @Test
    fun `spring configuration annotations must not exist inside pure domain layers`() {
        Konture.scope.classes()
            .withPackage("..domain..")
            .assertTrue("Domain classes must not declare Spring configuration") { cls ->
                cls.annotations.none { ann -> 
                    ann.name.contains("org.springframework.context.annotation.Configuration") 
                }
            }
    }
}
```

---

## 🚨 Example Failure Output

If a developer mistakenly exposes a heavy Spring `@Configuration` inside a pure Domain module package:

```text
AssertionError: Architecture violation: Domain classes must not declare Spring configuration
Offending classes:
  - Class 'io.github.baole.konture.sample.domain.AppConfiguration' failed assertion.
    (at /path/to/project/showcases/sample-gradle/domain/src/main/kotlin/io.github.baole.konture/sample/domain/AppConfiguration.kt:5)
```

The assertion flags the illegal framework-coupling annotation immediately, pointing to the exact line in your domain code.
