# Usage Guide

Once Konture is installed and integrated into your project, you can begin defining and enforcing your architectural rules. This guide covers how to write your first tests using Konture's two main paradigms, how to run them, and how to interpret failure messages.

> **🤖 Streamline with AI**
> Writing architecture tests is even faster with our specialized prompts! Load these in your AI workflow to generate correct, compile-safe rules:
> *   **[✍️ Writing Tests Prompt](ai-prompts/writing-tests-prompt.md)**: Standardized guidelines for crafting expressive constraints.
> {: .tip }

---

## 📐 Writing Your First Test

Create a new Kotlin test class inside your dedicated architecture test module:
`konture-test/src/test/kotlin/com/acme/konture/ArchitectureTest.kt`

Konture supports two distinct, highly ergonomic API paradigms for designing your rules. Choose the one that best fits your team's style:

### 1. Fluent Scope (Konsist-Inspired)

The **Fluent Scope** style is an imperative, lambda-driven builder. You query the global project scope, filter classes using helper properties or extensions, and run assertions directly using an `assertTrue` lambda.

This style is highly expressive, extremely flexible, and perfect for team-wide code conventions.

```kotlin
package com.acme.konture

import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class FluentArchitectureTest {

    @Test
    fun "repositories should be interfaces"() {
        Konture.scope
            .classes
            .withNameEndingWith("Repository")
            .assertTrue("Repositories must be declared as interfaces!") { classDecl ->
                classDecl.isInterface
            }
    }
}
```

---

### 2. Declarative Rules (ArchUnit-Inspired)

The **Declarative Rules** style utilizes a structured, fluent rule builder. You specify the subject (`classes()` or `modules()`), filter the set using `that()`, declare constraints with `should()`, and execute the evaluation with `.check()`.

This style is highly readable, structured, and ideal for describing high-level architecture designs (like layered boundaries or clean-architecture isolation).

```kotlin
package com.acme.konture

import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class DeclarativeArchitectureTest {

    @Test
    fun "core modules dependency isolation"() {
        Konture.modules()
            .that().haveNameMatching(":core:*")
            .should().notDependOnModule(":app")
            .check()
    }
}
```

---

## 🏃 Running the Tests

Because Konture compiled layouts run as standard unit tests on the JVM, executing them is fast and seamless. Run the tests using your build system or trigger them directly from your IDE gutter.

### 🐘 Gradle

Execute the standard test task on your dedicated test subproject:

```bash
# Run tests specifically in the architecture subproject
./gradlew :konture-test:test

# Run all checks across the entire multi-project build
./gradlew check
```

---

### 📦 Maven

Execute the surefire test lifecycle:

```bash
# Run tests specifically inside the architecture subproject
mvn test -pl konture-test

# Run all checks across the entire multi-module build
mvn test
```

---

## 🔍 Interpreting Failure Traces

When an architectural constraint is violated, Konture throws a detailed, well-formatted `AssertionError` to abort your build. The trace is designed to make debugging effortless.

### Example Failure Output

```text
java.lang.AssertionError: Architecture validation failed!
Rule: "Classes with name ending with 'Repository' should be interfaces."

Violations found in 1 class:
  • com.acme.database.UserRepository (at file:///Users/acme/project/core/database/src/main/kotlin/com/acme/database/UserRepository.kt:12)
    Reason: Class must be declared as an interface.
```

> **IDE Clickable Links**: Notice that Konture output includes absolute `file://` URLs. In modern IDEs like Android Studio and IntelliJ IDEA, these links are automatically highlighted. You can click them directly in the test console to jump straight to the offending line of code and fix the violation instantly!
{: .important }
