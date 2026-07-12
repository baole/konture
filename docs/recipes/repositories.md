# Repositories Must Be Interfaces

A cornerstone of Clean Architecture is ensuring that your core business logic (residing in your domain layers) remains completely decoupled from concrete infrastructure or database adapters. 

By enforcing that all repository declarations are strictly defined as interfaces, you guarantee that use cases program against abstractions rather than concrete SQL, NoSQL, or network clients.

---

## 💡 Rationale
* **Abstraction**: Keeps the domain layer pure and platform-agnostic.
* **Testability**: Makes mocking or faking dependencies in unit tests incredibly simple.
* **Separation of Concerns**: Prevents data access details (e.g., Hibernate, Room, or Retrofit queries) from leaking into business domain handlers.

---

## 🛠️ Implementation

Below are the two ways to write this rule. Both will scan your entire codebase for classes ending in `Repository` and assert that they are declared as interfaces.

### Option A: Standard Declarative DSL (Highly Readable)

```kotlin
import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class RepositoryArchitectureTest {

    @Test
    fun `repositories must be declared as interfaces`() {
        Konture.classes()
            .that().haveNameEndingWith("Repository")
            .should().beInterfaces()
            .check()
    }
}
```

### Option B: Fluent Lambda DSL (Ultra-Concise, v0.6.1+)

If you want to perform more custom conditions on top of this check, you can leverage our premium extension blocks. 

> [!IMPORTANT]
> A `should { }` block must either return a single `Boolean` expression (as a predicate) or perform imperative assertions using the `check(condition, message)` helper (returning `Unit`). 
> Multiple loose Boolean expressions inside a block will NOT work as assertions because only the final expression is evaluated. Returning other types (such as `null`, elements, or collections) is not supported and should be avoided.

#### 1. Single Boolean Predicate Expression
```kotlin
import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class RepositoryArchitectureTest {

    @Test
    fun `repositories must be interfaces fluent`() {
        Konture.classes()
            .that { name.endsWith("Repository") }
            .should { isInterface }
            .check()
    }
}
```

#### 2. Imperative Assertion Block using `check(...)`
```kotlin
import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class RepositoryArchitectureTest {

    @Test
    fun `repositories must be interfaces with check helper`() {
        Konture.classes()
            .that { name.endsWith("Repository") }
            .should {
                check(isInterface, "Repository $name must be an interface")
            }
            .check()
    }
}
```

---

## 🚨 Example Failure Output

If a developer accidentally checks in a concrete class named `ProductRepository` without declaring it as an interface:

```text
AssertionError: Architecture violation in rule: classes that have name ending with 'Repository' should be interfaces
Offending classes:
  - io.github.baole.konture.sample.data.ProductRepository (at /path/to/project/showcases/sample-gradle/data/src/main/kotlin/io.github.baole.konture/sample/data/ProductRepository.kt:8)
```
You can click directly on the file path in your IDE terminal to navigate straight to the line of code that broke the contract!
