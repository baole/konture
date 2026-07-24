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
    id("io.github.baole.konture") version "0.7.2"
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
    testImplementation("io.github.baole:konture:0.7.2")

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
            <version>0.7.2</version>
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
                <version>0.7.2</version>
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

## 🚀 Next Steps

Now that Konture is installed and configured in your build, learn how to write your first tests and customize the rule engine:

*   **[📐 Usage Guide](usage.md)**: Learn how to define rules using the **Fluent Scope** or **Declarative Rules** DSL, run assertions, and interpret test failures.
*   **[⚙️ Configuration Guide](configuration.md)**: Explore plugin properties, customize task log levels, and master package-matching wildcards.
*   **[🛡️ Architecture Baselines Guide](baseline.md)**: Learn how to record, commit, and manage historical architectural violations in legacy codebases.
