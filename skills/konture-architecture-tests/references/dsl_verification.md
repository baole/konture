# Verifying Konture's DSL before writing a real test

Konture provides a unified, consistent, and compiling API surface across all documentation (including the README and `docs/architecture_test.md`) using the public rule-builder DSL and optional lambda helpers.

Always verify examples against the literal public Konture API before writing or changing architecture tests.

## Standard Architecture Block Pattern

All guardrails are defined within the `architecture { ... }` block, utilizing nested builder scopes for different target elements (such as `modules`, `classes`, `files`, `functions`, or `properties`).

Within each builder scope:
1. Filter the elements using `that()` rules.
2. Assert expectations on the filtered elements using `should()` and `andShould()` / `orShould()` assertions.

## Starter Test Shape

```kotlin
import io.github.baole.konture.architecture
import org.junit.jupiter.api.Test

class ArchitectureGuardrails {

    @Test
    fun `domain layer should be completely isolated from data and UI layers`() {
        architecture {
            // 🎯 Select modules inside domain
            modules {
                that().haveNamePath(":core:domain")
                should().notDependOnModule(":core:data")
                andShould().notDependOnModule(":feature:checkout")
            }

            // 🎯 Verify class boundary rules
            classes {
                that().resideInAPackage("..domain..")
                that().haveNameEndingWith("Repository")
                should().beInterfaces()
            }
        }
    }
}
```

## Best Practices for Verification

1. Check the actual Konture version applied in this project (`gradle/libs.versions.toml` or root `build.gradle.kts`).
2. Treat the README's Fluent Lambda DSL as the standard, single source of truth for the public API surface.
3. Ensure assertions use compiling visibility checks, such as `should().beInternal()` or checking visibility via `.satisfy { cls, violations -> if (cls.visibility != io.github.baole.konture.Visibility.INTERNAL) { ... } }` if custom logic is required.
