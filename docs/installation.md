
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

You can set up Konture using either **Gradle** or **Maven**. Choose your preferred build tool below:

### 🐘 Gradle Setup

#### Step 1: Apply the Gradle Plugin

Apply the Konture plugin to your **root project's `build.gradle.kts`**:

```kotlin
plugins {
    id("io.github.baole.konture") version "0.6.9"
}
```

When applied to the root project, the plugin automatically registers a task named `generateArchitectureLayout`. This task serializes all subprojects in the build, gathering their source sets and declared dependency paths into a consolidated, relocatable `layout.json` metadata file.

---

#### Step 2: Configure repositories in `settings.gradle.kts`

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

#### Step 3: Create the Dedicated Test Module (`:konture-test`)

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
    // The only required Konture assertion library dependency
    testImplementation("io.github.baole:konture:0.6.9")

    // Plus your standard test runner of choice (JUnit 5, Kotest, TestBalloon, etc.)
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

### 📦 Maven Setup

For projects utilizing Apache Maven, Konture provides a dedicated Maven plugin to serialize your multi-module project layout and dependency graph.

#### Step 1: Create the Dedicated Test Module (`konture-test`)

Create a subdirectory named `konture-test/` with a `pom.xml` file. Ensure that you declare all modules you want to run architecture tests on as test-scoped dependencies inside this `pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.acme</groupId>
        <artifactId>my-parent-project</artifactId>
        <version>1.0.0</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>konture-test</artifactId>

    <dependencies>
        <!-- The only required Konture assertion library dependency -->
        <dependency>
            <groupId>io.github.baole</groupId>
            <artifactId>konture</artifactId>
            <version>0.6.9</version>
            <scope>test</scope>
        </dependency>

        <!-- Plus your standard test runner (e.g., JUnit 5, Kotest) -->
        <!-- Plus the project modules under test that you want to run checks on -->
    </dependencies>

    <build>
        <testSourceDirectory>src/test/kotlin</testSourceDirectory>
        <plugins>
            <!-- Kotlin Compiler Plugin -->
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${kotlin.version}</version>
                <executions>
                    <execution>
                        <id>test-compile</id>
                        <phase>test-compile</phase>
                        <goals>
                            <goal>test-compile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Konture Maven Plugin for generating architecture graph contracts -->
            <plugin>
                <groupId>io.github.baole.konture</groupId>
                <artifactId>konture-maven-plugin</artifactId>
                <version>0.6.9</version>
                <executions>
                    <execution>
                        <phase>process-test-resources</phase>
                        <goals>
                            <goal>generate-layout</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Surefire Plugin for running tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>
        </plugins>
    </build>
</project>
```

#### Step 2: Register the new module in your parent POM

Add the `<module>` declaration to your parent `pom.xml` so Maven recognizes and builds the module:

```xml
<modules>
    <module>domain</module>
    <module>data</module>
    <module>app</module>
    <module>konture-test</module>
</modules>
```

---

## ⚙️ Plugin Configuration

Both the Gradle and Maven plugins share the same configuration structure. Match rules are exactly identical.

### 🐘 Gradle Configuration

The Konture Gradle plugin can be customized using the `konture { ... }` configuration block in your build configuration file (typically in your root `build.gradle.kts` or inside your dedicated `:konture-test` subproject).

```kotlin
konture {
    // Exclude subprojects from analysis
    excludeModules(":legacy-app", ":experimental:*")

    // Exclude specific packages from being parsed
    excludePackages("com.acme.generated..", "..databinding..")

    // Exclude specific classes or patterns
    excludeClasses("ExcludedService", "*Helper")

    // Exclude certain Gradle dependency configurations from being traversed
    excludeConfigurations("test*", "profile")

    // Set the task execution log level
    logLevel("INFO")
}
```

### 📦 Maven Configuration

For the Konture Maven plugin, add configuration options directly inside the `<configuration>` block of `konture-maven-plugin` in your `konture-test/pom.xml`:

```xml
<plugin>
    <groupId>io.github.baole.konture</groupId>
    <artifactId>konture-maven-plugin</artifactId>
    <version>0.6.9</version>
    <executions>
        <execution>
            <phase>process-test-resources</phase>
            <goals>
                <goal>generate-layout</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <!-- Exclude subprojects from analysis -->
        <excludeModules>
            <module>legacy-app</module>
        </excludeModules>

        <!-- Exclude specific packages from being parsed -->
        <excludePackages>
            <package>com.acme.generated..</package>
            <package>..databinding..</package>
        </excludePackages>

        <!-- Exclude specific classes or patterns -->
        <excludeClasses>
            <class>ExcludedService</class>
            <class>*Helper</class>
        </excludeClasses>

        <!-- Exclude certain dependency configurations from being traversed -->
        <excludeConfigurations>
            <configuration>test</configuration>
            <configuration>profile</configuration>
        </excludeConfigurations>

        <!-- Set the task execution log level -->
        <logLevel>INFO</logLevel>
    </configuration>
</plugin>
```

### 📋 Available Settings & Matching Rules

| Property / DSL Method | Default Value | Description & Matching Rules |
| :--- | :--- | :--- |
| **`excludeModules`** | `emptyList()` | Excludes matching Gradle subprojects from the scanned dependency graph.<br>• Supports **Module Glob Patterns**:<br>  - `*` matches exactly one segment (all characters except the colon `:`). E.g. `:feature:*` matches `:feature:api` but **not** `:feature:api:impl`.<br>  - `**` matches zero or more segments. E.g. `:feature:**` matches `:feature:api:impl`. |
| **`excludePackages`** | `emptyList()` | Excludes class files in matching packages from being parsed or analyzed.<br>• Supports **Package Segment Wildcards (`..`)**:<br>  - `..` represents zero-or-more package segments.<br>  - E.g. `com.acme.domain..` matches `com.acme.domain` and any of its subpackages.<br>  - E.g. `..generated..` matches any package containing the segment `generated` anywhere in its hierarchy. |
| **`excludeClasses`** | `emptyList()` | Excludes matching classes from the scope. Both fully qualified names and simple class names are checked.<br>• Supports two matching styles:<br>  - **Package Wildcards (`..`)** (e.g., `com.acme.service.ExcludedClass`).<br>  - **Simple Globs (`*`)** where `*` matches zero or more characters (e.g., `*Helper`, `*Test`). |
| **`excludeConfigurations`** | `listOf("test", "benchmark", "profile", "testedapks")` | Excludes specific dependency configurations from being traversed in the dependency graph.<br>• Supports simple glob matching (`*`). E.g. `test*` matches `testImplementation`. |
| **`logLevel`** | `"INFO"` | Configures logging level of the Konture plugin execution.<br>• Supported levels: `"INFO"`, `"DEBUG"`, `"WARNING"`, `"TRACE"`. |

### 🧩 Wildcard & Pattern Matching in Depth

Konture uses custom, lightweight matching engines optimized for Kotlin package hierarchies and Gradle module structures rather than slow or complex regular expressions.

---

#### 📦 Package Matching (`..`)
Package names are dot-separated (e.g., `com.acme.feature.payment.service`).
*   **Double Dot (`..`)**: Represents **zero or more package segments**.
*   **Single Dot (`.`)**: Separates explicit segments.

##### Examples:
| Pattern | Matches | Does NOT Match | Why? |
| :--- | :--- | :--- | :--- |
| `com.acme.domain..` | `com.acme.domain`<br>`com.acme.domain.repository`<br>`com.acme.domain.repository.impl` | `com.acme.api`<br>`org.acme.domain` | Matches any package starting with `com.acme.domain` followed by zero or more segments. |
| `..generated..` | `com.acme.generated`<br>`com.acme.feature.generated.service`<br>`generated.com.acme` | `com.acme.generate`<br>`com.acme.regenerated` | Matches any package path where `generated` is a standalone segment. |
| `..` | *Matches everything* | None | Matches zero or more segments anywhere. |

---

#### 🛠️ Module Glob Matching (`*` vs `**`)
Gradle subprojects are colon-separated (e.g., `:feature:checkout:impl`).
*   **Single Star (`*`)**: Matches **exactly one module segment** (all characters except the colon `:`).
*   **Double Star (`**`)**: Matches **zero or more segments** (any character sequence).

##### Examples:
| Pattern | Matches | Does NOT Match | Why? |
| :--- | :--- | :--- | :--- |
| `:feature:*` | `:feature:checkout`<br>`:feature:catalog` | `:feature:checkout:impl`<br>`:feature` | `*` matches exactly one level below `:feature`. |
| `:feature:**` | `:feature:checkout`<br>`:feature:checkout:impl`<br>`:feature` | `:core:network` | `**` matches any depth of subprojects under `:feature`. |
| `:*-api` | `:payment-api`<br>`:auth-api` | `:feature:payment-api` | Matches any root-level module ending with `-api`. |

---

#### 🔍 Simple Glob Matching (`*`)
Used for configuration matching (e.g., `excludeConfigurations`) and class name matching.
*   **Single Star (`*`)**: Matches zero or more characters of any kind.

##### Examples:
| Pattern | Matches | Does NOT Match |
| :--- | :--- | :--- |
| `*Helper` | `AuthHelper`<br>`Helper`<br>`com.acme.MyHelper` | `Helpers`<br>`HelperClass` |
| `test*` | `test`<br>`testImplementation`<br>`testRuntimeOnly` | `latest` |

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

### 🐘 Gradle

```bash
# Run tests specifically in the architecture subproject
./gradlew :konture-test:test

# Run all checks across the multi-project build
./gradlew check
```

### 📦 Maven

```bash
# Run tests specifically inside the architecture subproject
mvn test -pl konture-test

# Run all checks across the multi-module build
mvn test
```

When a rule is violated, the build will fail immediately, printing a descriptive trace containing:
1. The exact rule that was violated.
2. The list of offending Kotlin files.
3. The absolute path of the files, allowing you to click them directly in your IDE (Android Studio / IntelliJ) to fix the violation.
