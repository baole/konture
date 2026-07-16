<p align="center">
  <img src="docs/assets/images/logo.png" width="160" alt="Konture Logo">
</p>

<h1 align="center">🧬 Konture: Kotlin Architecture Testing Guardrails</h1>

<p align="center">
  <a href="https://baole.github.io/konture/"><img src="https://img.shields.io/badge/docs-GitHub%20Pages-blue?style=for-the-badge&logo=github" alt="GitHub Pages"></a>
  <a href="https://plugins.gradle.org/plugin/io.github.baole.konture"><img src="https://img.shields.io/badge/gradle%20plugin-portal-blue?style=for-the-badge&logo=gradle" alt="Gradle Plugin Portal"></a>
  <a href="https://central.sonatype.com/artifact/io.github.baole/konture"><img src="https://img.shields.io/maven-central/v/io.github.baole/konture-core?style=for-the-badge&logo=apache-maven" alt="Maven Central"></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/kotlin-2.4.0-purple?style=for-the-badge&logo=kotlin" alt="Kotlin"></a>
  <a href="https://gradle.org/"><img src="https://img.shields.io/badge/gradle-9.6.1-blue?style=for-the-badge&logo=gradle" alt="Gradle"></a>
</p>

**Konture** is a stack- and build-tool agnostic Kotlin architecture testing library for Android, Kotlin Multiplatform (KMP), and JVM backend projects. It combines real project structure (captured directly from your project's build graph) with AST-based static analysis and a premium, architecture-agnostic **Fluent Lambda DSL** to enforce boundaries on any test framework.

---

## 🛡️ The problem

In multi-module, multi-layer projects, architecture erodes through small shortcuts. For example, a feature module might declare a "sideways" dependency on a sibling feature.

```mermaid
graph TD
    app[":app"] --> checkout[":feature:checkout"]
    app --> profile[":feature:profile"]
    checkout --> core[":core:network"]
    profile --> core
    checkout -.->|"PROHIBITED: Sideways Dependency"| profile

    style app fill:#f1f5f9,stroke:#94a3b8,stroke-width:2px
    style checkout fill:#e0f2fe,stroke:#0284c7,stroke-width:2px
    style profile fill:#e0f2fe,stroke:#0284c7,stroke-width:2px
    style core fill:#ecfdf5,stroke:#10b981,stroke-width:2px
    linkStyle 4 stroke:#ef4444,stroke-width:2px,stroke-dasharray: 5 5;
```

Konture helps developers analyze project structure and enforce architectural rules and boundaries directly inside the test suite.

---

## 🔑 Key Capabilities

*   **📦 Platform & Stack Agnostic**: Works seamlessly across Android, Kotlin Multiplatform (KMP), and Kotlin backend projects (Spring Boot, Ktor, etc.).
*   **📐 Architecture Agnostic**: Set constraints for any design pattern (Clean, Layered, MVVM, Hexagonal, DDD) without layout restrictions.
*   **🛠️ Build Tool Agnostic**: Engineered to support multiple build systems, starting with deep Gradle integration and designed for Maven and other build tools.
*   **🧪 Test Framework Agnostic**: Runs as a pure JVM library, compatible with [JUnit 4](https://junit.org/junit4/), [JUnit 5](https://junit.org/junit5/), [JUnit 6](https://junit.org/), [Kotest](https://kotest.io/), [TestBalloon](https://github.com/infix-de/testBalloon), or any other runner.
*   **✍️ Fluent Lambda DSL**: Write expressive, readable assertions for module dependencies, package isolation, interface adherence, and naming conventions.
*   **🤖 AI-Agent Friendly**: Includes dedicated prompts and custom skills for autonomous integration and code generation:
    *   **[🤖 Installation](docs/ai-prompts/integration-prompt.md)**: Automated Gradle project setup.
    *   **[✍️ AI Test Writing Guide](docs/ai-prompts/writing-tests-prompt.md)**: Context-rich master prompt for writing compile-safe DSL tests.

---

## 🚀 Getting Started

> [!TIP]
> **🤖 Automated Setup**: Save time by using our [🤖 integrate-konture prompt/skill](docs/ai-prompts/integration-prompt.md) to let an AI assistant automatically configure your Gradle project and generate tests. See the [AI Prompts Catalog](docs/ai-prompts/README.md) for details.

### Installation

#### Gradle

1. Apply the plugin to your root `build.gradle.kts`:
```kotlin
plugins {
    id("io.github.baole.konture") version "0.6.9" apply true
}
```

2. Add the dependency to your test module's `build.gradle.kts`:
```kotlin
dependencies {
    testImplementation("io.github.baole:konture:0.6.9")
}
```

> [!IMPORTANT]
> We recommend running your architectural guards in a dedicated test module, such as `:konture-test` (see our [sample module](/konture-test/)).

> [!NOTE]
> For alternative Gradle plugin setup formats, refer to the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.baole.konture).

#### Maven (Coming Soon)

Support for Maven is currently in development.

### Write Your First Guardrail

Create a unit test to enforce architectural boundaries and conventions. Since Konture is completely architecture-agnostic, you can configure guardrails that match your codebase's custom patterns:


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
        }
    }

    @Test
    fun `repositories inside domain must be declared as interfaces`() {
        architecture {
            // 🎯 Select classes inside domain package
            classes {
                that().resideInAPackage("..domain..")
                that().haveNameEndingWith("Repository")
                should().beInterfaces()
            }
        }
    }
}
```


## 📖 Documentation

Visit our official **[GitHub Pages Documentation Site](https://baole.github.io/konture/)** or explore the resources directly:

*   **[🚀 Installation](docs/installation.md)**: Manual integration guide.
*   **[🤖 AI Prompts & Skills Catalog](docs/ai-prompts/README.md)**: Dedicated prompts for [autonomous setup](docs/ai-prompts/integration-prompt.md) and [test generation](docs/ai-prompts/writing-tests-prompt.md).
*   **[🧩 Core Concepts](docs/architecture_test.md)**: Static-analysis engine details.
*   **[📜 Recipes](docs/recipes/)**: Templates for common guardrails (layer isolation, interface conventions, etc.).
*   **[🏢 Showcases](docs/showcases.md)**: Real-world configurations (Now in Android, KotlinConf, Ktor, etc.).

## 🤝 Contributing

We welcome all contributions! Please check our **[Contribution Guidelines](docs/contributing.md)** (or the online **[Contributing Guide](https://baole.github.io/konture/contributing/)**) for local setup, build commands, and PR workflows.

## 📄 License

This project is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).
