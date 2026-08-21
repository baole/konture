# Named Rules & Rule Metadata

Konture allows you to attach **stable identifiers**, **descriptions**, **severities**, and **tags** to your architectural rules. Named rules make failure reports cleaner, enable rule-level baseline filtering, and provide clear metadata for team architecture compliance.

---

## 🏷️ Defining a Named Rule

Use the top-level `rule(id)` function to define a named architectural rule:

```kotlin
import io.github.baole.konture.*
import io.github.baole.konture.core.model.Severity
import org.junit.jupiter.api.Test

class RepositoryArchitectureTest {

    @Test
    fun `domain repositories must be interfaces`() {
        val repositoryRule = rule("domain.repositories.must-be-interfaces") {
            description = "Domain repositories must be interfaces to enforce Dependency Inversion Principle"
            severity = Severity.ERROR
            tag("architecture", "domain", "dip")

            classes {
                that().resideInAPackage("..domain.repository..")
                should().beInterfaces()
            }
        }

        repositoryRule.check()
    }
}
```

---

## 🏛️ Named Rules in Batch Architecture Suites

You can also define named rules directly inside `architecture { ... }` blocks:

```kotlin
architecture {
    rule("core.services.naming") {
        description = "Core services must have a Service suffix"
        severity = Severity.WARNING
        tag("naming", "convention")

        classes {
            that().resideInAPackage("..service..")
            should().haveNameEndingWith("Service")
        }
    }

    rule("domain.layer.isolation") {
        description = "Domain layer must not depend on data or presentation"
        severity = Severity.ERROR
        tag("layering", "isolation")

        layered {
            layer("Domain") definedBy "..domain.."
            layer("Data") definedBy "..data.."
            layer("Presentation") definedBy "..presentation.."

            "Domain" shouldNotDependOn "Data"
            "Domain" shouldNotDependOn "Presentation"
        }
    }
}
```

---

## ⚙️ Metadata Properties

| Metadata Property | Description | Default |
| :--- | :--- | :--- |
| `id` | Unique, stable string identifier for the rule (e.g. `domain.repositories.must-be-interfaces`) | Required parameter in `rule(id)` |
| `description` | Human-readable explanation of why the rule exists | `null` |
| `severity` | Violation severity level (`Severity.ERROR`, `Severity.WARNING`, `Severity.INFO`) | `Severity.ERROR` |
| `tags` | Arbitrary category tags defined via `tag("tag1", "tag2")` | Empty set |

---

## 🚦 Configurable Severity & Build Gate Enforcement

Konture provides a configurable build gate threshold via `Konture.failOnSeverity`, allowing teams to progressively enforce architecture guardrails in CI pipelines.

### Severity Hierarchy

1. **`Severity.ERROR`** (Highest): Critical architectural violations (e.g., circular module dependencies, domain boundary leaks).
2. **`Severity.WARNING`**: Non-critical convention drifts or deprecation warnings (e.g., missing class name suffixes, forbidden utility usages).
3. **`Severity.INFO`** (Lowest): Informational findings, statistics, or upcoming rule previews.

### Threshold Evaluation Logic

When rules are evaluated, Konture compares each rule's `severity` against the active `failOnSeverity` threshold:

* **Default (`failOnSeverity = Severity.ERROR`)**: Only `ERROR` violations fail tests with an `AssertionError`. `WARNING` and `INFO` violations are logged to the console as non-blocking diagnostics.
* **`failOnSeverity = Severity.WARNING`**: Both `ERROR` and `WARNING` violations fail the build.
* **`failOnSeverity = Severity.INFO`**: All violations (`ERROR`, `WARNING`, `INFO`) fail the build.
* **Audit / Dry-Run Mode (`failOnSeverity = null`)**: **No** violations fail tests with `AssertionError`. All rule evaluations run, violations are logged as diagnostics, and full statistics are recorded into JSON/SARIF/HTML reports.

### ⚙️ Configuring the Build Gate Threshold

#### 1. Via CI System Property
You can toggle strictness or enable audit mode in CI environments without modifying source code:
```bash
# Strict mode: fail on both ERROR and WARNING
./gradlew test -Dkonture.fail.on.severity=warning

# Audit / Dry-Run mode: log all violations and export reports without failing the build
./gradlew test -Dkonture.fail.on.severity=none -Dkonture.output.format=sarif
```

#### 2. Programmatically (Thread-Isolated)
```kotlin
import io.github.baole.konture.Konture
import io.github.baole.konture.core.model.Severity
import org.junit.jupiter.api.BeforeEach

class ArchitectureAuditTest {
    @BeforeEach
    fun setUp() {
        // Run this test suite in audit mode
        Konture.failOnSeverity = null
    }
}
```

### 📈 Full Accumulation in Reports & Baselines

Sub-threshold violations are **never ignored** in reporting:
* **JSON & SARIF reports**: Contain all violations across all severities with accurate `errorCount`, `warningCount`, and `infoCount`.
* **Architecture Baselines**: In baseline generation mode (`Konture.generateBaseline = true`), violations across all severity levels are captured to `konture-baseline.json`.

---

## 📊 Violation Reporting & Baselines

When a named rule fails, its stable `id` and `severity` are attached to every generated `Violation` object and recorded in JSON baseline files (`konture-baseline.json`):

```json
{
  "version": 1,
  "testClasses": [
    {
      "name": "com.acme.ArchitectureTest",
      "tests": [
        {
          "name": "domainRepositoriesMustBeInterfaces",
          "violations": [
            {
              "message": "Class UserRepositoryImpl must be an interface (at core/UserRepositoryImpl.kt:12)",
              "location": "com.acme.domain.repository.UserRepositoryImpl"
            }
          ]
        }
      ]
    }
  ]
}
```

This ensures that baseline suppressions remain stable even if file line numbers shift over time.
