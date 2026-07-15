# Naming Suffix Conventions

In team-based Kotlin engineering, maintaining consistent naming patterns makes codebases highly navigable. If some team members name business handlers `GetProductUseCase` while others name them `RetrieveProductService` or `GetProductHelper`, readability suffers.

Konture lets you enforce strict naming suffixes on classes residing inside specific directories or packages.

---

## 💡 Rationale
* **Code Navigation**: Developer scanning is simplified when suffixes are strictly aligned with their structural roles.
* **Onboarding**: New engineers can immediately locate the purpose of a class simply by reading its suffix.
* **Automation**: Offloads code review naming friction from human maintainers to automatic CI/CD test gates.

---

## 🛠️ Implementation

Below are examples of enforcing suffixes for two common architectures (Domain Use Cases and Android ViewModels):

### 1. Enforcing `UseCase` Suffix on Domain Classes

Ensure that any class residing within packages containing `..usecase..` or `..domain..` matches consistent patterns:

```kotlin
import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class NamingConventionTest {

    @Test
    fun `use cases must be named UseCase`() {
        Konture.classes()
            .that().resideInAPackage("..usecase..")
            .should().haveNameEndingWith("UseCase")
            .check()
    }
}
```

For more complex naming conventions (such as checking matching annotations and class prefixes), you can use the ultra-concise Fluent DSL.

> A `should { }` block must either return a single `Boolean` expression (as a predicate) or perform imperative assertions using the `check(condition, message)` helper (returning `Unit`).
> Multiple loose Boolean expressions inside a block will NOT work as assertions because only the final expression is evaluated. Returning other types (such as `null`, elements, or collections) is not supported and should be avoided.
{: .important }

#### 1. Single Boolean Predicate Expression
```kotlin
import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class NamingConventionTest {

    @Test
    fun `viewmodels must have ViewModel suffix`() {
        Konture.classes()
            .that { resideInAPackage("..presentation..") }
            .should { name.endsWith("ViewModel") }
            .check()
    }
}
```

#### 2. Imperative Assertion Block using `check(...)`
```kotlin
import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class NamingConventionTest {

    @Test
    fun `viewmodels must have ViewModel suffix with check helper`() {
        Konture.classes()
            .that { resideInAPackage("..presentation..") }
            .should {
                check(name.endsWith("ViewModel"), "ViewModel $name must have 'ViewModel' suffix")
            }
            .check()
    }
}
```

---

## 🚨 Example Failure Output

If a developer accidentally creates a class `GetProductAction` in a domain usecase directory:

```text
AssertionError: Architecture violation in rule: classes that reside in a package matching '..usecase..' should have name ending with 'UseCase'
Offending classes:
  - io.github.baole.konture.sample.domain.usecase.GetProductAction (at /path/to/project/showcases/sample-gradle/domain/src/main/kotlin/io.github.baole.konture/sample/domain/usecase/GetProductAction.kt:6)
```
Like all other assertions, this failure output is integrated into your test report and provides links straight to the offending file.
