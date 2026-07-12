# Installation & Setup

> [!TIP]
> **🤖 Save Time: Let AI Set It Up & Write Tests!**
> Instead of manually copying and editing build configurations, you can use our official, high-context AI prompts and custom skills to let AI assistants (like Gemini, Claude, Cursor) do it for you instantly:
> *   **[🤖 setup-konture Skill / Prompt](ai-prompts/setup-prompt.md)**: Lets autonomous agents or chat assistants inspect, install, configure, and set up a dedicated test module in any repository with zero manual intervention.
> *   **[📐 konture-architecture-tests Skill / Prompt](ai-prompts/writing-tests-prompt.md)**: A complete, copy-pasteable master prompt to easily design, write, and review custom guardrails using AI with compile-safe DSL API references.
> *   See the full **[🤖 AI Prompts & Custom Skills Catalog](ai-prompts/README.md)** for a central index on how to load these.
> 
> *Copy the prompts first to automate your entire onboarding!*

Konture is designed to integrate into your Kotlin or Gradle project with minimal friction. It is structured into two parts:

1. **The Gradle Plugin** (`io.github.baole.konture`): Extracts your multi-module project layout, applied plugins, and project dependency boundaries at build time, caching it safely inside your build directories.
2. **The Assertion Library** (`io.github.baole:konture`): Provides rich, fluent DSLs (inspired by Konsist, ArchUnit, and modern declarative standards) to run assertions against your codebase.

---

## 💻 Supported Environments & Platforms

Konture works natively across the entire spectrum of Kotlin projects:

* **Backend / Server-Side**: Fully supports frameworks like **Spring Boot**, **Ktor**, **Micronaut**, and **Quarkus**. Runs without starting heavy dependency injection contexts or loading production classloaders.
* **Android**: Baselines against **Android Gradle Plugin (AGP) 9.x** (with prior versions supported on a best-effort basis). It automatically extracts module variants and scopes checks to production source sets.
* **Kotlin Multiplatform (KMP)**: Correctly identifies and queries multiplatform source directories (e.g. `commonMain`, `androidMain`, `iosMain`, `desktopMain`).
* **Test Framework Agnostic**: Runs seamlessly inside **[JUnit 4](https://junit.org/junit4/)**, **[JUnit 5](https://junit.org/junit5/)**, **[JUnit 6](https://junit.org/)**, **[Kotest](https://kotest.io/)**, **[TestBalloon](https://github.com/infix-de/testBalloon)**, or absolutely **any other test runner** of your choice. Because Konture operates as a standalone Kotlin library rather than a customized framework runner, you can invoke your architectural assertions directly inside any test block without any runner extensions or custom boilerplate—the choice of test runner simply doesn't matter.

---

## 🛠️ Step-by-Step Setup

To prevent test framework and AST-parsing dependencies from polluting your production application classpath, we highly recommend creating a **dedicated subproject** (e.g., `:architecture-test` or `:konture-test`) for your architectural rules.

### Step 1: Apply the Gradle Plugin

Apply the Konture plugin to your **root project's `build.gradle.kts`**:

```kotlin
plugins {
    id("io.github.baole.konture") version "0.6.6"
}
```

When applied to the root project, the plugin automatically registers a task named `generateArchitectureLayout`. This task serializes all subprojects in the build, gathering their source sets and declared dependency paths into a consolidated, relocatable `layout.json` metadata file.

---

### Step 2: Configure repositories in `settings.gradle.kts`

If you are trying out locally compiled versions of Konture (e.g., during development or contributing), ensure `mavenLocal()` is enabled at the top of your repository configurations:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

include(":app")
// etc...
include(":konture-test") // Include your architecture test module!
```

---

### Step 3: Create the Dedicated Test Module (`:konture-test`)

Create a subfolder named `konture-test/` with a `build.gradle.kts` file:

```kotlin
plugins {
    kotlin("jvm")
    id("io.github.baole.konture")
}

repositories {
    mavenCentral()
}

dependencies {
    // The unified assertion library containing all assertion builders and styles
    testImplementation("io.github.baole:konture:0.6.6")
    
    // Test runner of your choice (JUnit 5 is shown as a standard example, but Kotest, TestBalloon, JUnit 4/6 work natively too!)
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

Create a test class inside `konture-test/src/test/kotlin/io/github/baole/archtest/ArchitectureTest.kt`:

### Option A: Konsist-Inspired (Fluent Scope)

The Konsist-inspired style uses a fluent scope builder where you retrieve classes from the whole project or a module, filter them using helper functions, and call `assertTrue` with a predicate lambda.

```kotlin
package io.github.baole.archtest

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

### Option B: ArchUnit-Inspired (Declarative Rules)

The ArchUnit-inspired style uses a declarative rule-building builder DSL. You specify the target of the rule (`classes()` or `modules()`), filter them with `that()`, declare constraints with `should()`, and execute the validation using `check()`.

```kotlin
package io.github.baole.archtest

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
