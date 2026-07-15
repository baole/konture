
# Installation

> **🤖 Build with AI**
> Writing architecture tests with AI is now easier than ever! You can use our official prompts & skills:
> *   **[🤖 Integration Prompt](ai-prompts/integration-prompt.md)**
> *   **[📐 Writing Tests Prompt](ai-prompts/writing-tests-prompt.md)**
> *   **[🤖 See Prompts and Skills Catalog](ai-prompts/README.md)** for a central index on how to load these.
{: .tip }

## 💻 Supported Environments & Platforms

Konture works natively across all Kotlin environments:

* **Android**: Supports **AGP 9.x+** (older versions on a best-effort basis) and automatically handles module variants.
* **Kotlin Multiplatform (KMP)**: Seamlessly queries multiplatform source directories (e.g., `commonMain`, `androidMain`, `iosMain`).
* **Backend**: Fully supports JVM frameworks (like **Spring**, **Ktor**, **Micronaut**, **Quarkus**) without launching heavy DI or server contexts.
* **Test Agnostic**: Runs inside any test runner (**JUnit 4/5/6**, **Kotest**, **TestBalloon**) as a standard library, requiring no extensions or custom boilerplate.

---

## 🛠️ Step-by-Step Setup

### Step 1: Apply the Gradle Plugin

Apply the Konture plugin to your **root project's `build.gradle.kts`**:

```kotlin
plugins {
    id("io.github.baole.konture") version "0.6.9"
}
```

When applied to the root project, the plugin automatically registers a task named `generateArchitectureLayout`. This task serializes all subprojects in the build, gathering their source sets and declared dependency paths into a consolidated, relocatable `layout.json` metadata file.

---

### Step 2: Configure repositories in `settings.gradle.kts`

The Konture Gradle plugin is published to the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.baole.konture), and the assertion library is published to [Maven Central](https://central.sonatype.com/artifact/io.github.baole/konture).

Ensure that `gradlePluginPortal()` and `mavenCentral()` are declared in your repository configurations. If you are also trying out locally compiled versions during development or contributing, you can additionally include `mavenLocal()`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

---

### Step 3: Create the Dedicated Test Module (`:konture-test`)

Create a subfolder named `konture-test/` with a `build.gradle.kts` file:

```kotlin
plugins {
    kotlin("jvm")
    id("io.github.baole.konture") // this is important!
}

repositories {
    mavenCentral()
}

dependencies {
    // The unified assertion library containing all assertion builders and styles
    testImplementation("io.github.baole:konture:0.6.9")

    // Test runner of your choice (JUnit 5 is shown as a standard example,
    // but Kotest, TestBalloon, JUnit 4/6 work natively too!)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
}

tasks.test {
    useJUnitPlatform()
}
```

Don't forget to include `:konture-test` in your root `settings.gradle.kts`:
```kotlin
include(":konture-test")
```

---

## 🏁 Writing Your First Test

You can write architectural tests using any of the major API paradigms supported by Konture. Below are quick examples of our two most popular ergonomics.

Create a test class inside `konture-test/src/test/kotlin/com/acme/konture/ArchitectureTest.kt`:

### Fluent Scope

The Konsist-inspired style uses a fluent scope builder where you retrieve classes from the whole project or a module, filter them using helper functions, and call `assertTrue` with a predicate lambda.

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

### Declarative Rules

The ArchUnit-inspired style uses a declarative rule-building builder DSL. You specify the target of the rule (`classes()` or `modules()`), filter them with `that()`, declare constraints with `should()`, and execute the validation using `check()`.

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

To verify your architectural rules, run the standard test task on your dedicated test project:

```bash
# Run tests specifically in the architecture subproject
./gradlew :konture-test:test

# Run all checks across the multi-project build
./gradlew check
```

When a rule is violated, the build will fail immediately, printing a descriptive trace containing:
1. The exact rule that was violated.
2. The list of offending Kotlin files.
3. The absolute path of the files, allowing you to click them directly in your IDE (Android Studio / IntelliJ) to fix the violation.
