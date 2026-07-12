# 🚀 Konture Showcases

This directory contains a curated collection of showcase projects demonstrating how to integrate and utilize **Konture** for structural and architecture testing in both **Gradle** and **Maven** environments.

These showcases demonstrate how Konture protects project boundaries, enforces design conventions, and catches architecture regressions at build time.

---

## 🏛️ Showcases (Ordered by Learning Priority)

### 1. 📦 [sample-gradle](sample-gradle) & [sample-maven](sample-maven)
The fundamental starting point showcases. These lightweight, multi-module setups demonstrate the complete capabilities of Konture's DSL and AST scope checks across both major JVM build systems:

*   **Build System support**:
    *   `sample-gradle` showcases Gradle-based integration and configuration.
    *   `sample-maven` showcases Maven-based integration via `konture-maven-plugin`.
*   **Integrations & Rules demonstrated**:
    *   **Layer Isolation**: Chained layered architecture builders preventing bypass shortcuts.
    *   **Cross-Layer Type Leakage**: Custom functional `KontureScope` assertions ensuring domain UseCase signatures do not leak lower-level technical data/app coordinates.
    *   **Call Violations**: Enforcing that domain UseCases are only called by authorized presentation controllers using `onlyBeAccessedByAnyPackage(...)`.
    *   **Visibility boundaries**: Verifying public and internal modifier distributions on domain and data models.
*   **How to Run (Gradle)**:
    ```bash
    ./gradlew test -p showcases/sample-gradle
    ```
*   **How to Run (Maven)**:
    ```bash
    cd showcases/sample-maven
    mvn test
    ```

---

### 2. 🤖 [nowinandroid](nowinandroid)
Google's official, highly-modularized Android reference application. 

*   **Significance**: Serves as the premier industry reference layout for modern Android development. It models layered clean architecture, feature-by-feature modularization, and unidirectional data flow in extremely complex, large-scale codebases.
*   **Aignment Focus**: Audited to provide a structural model of feature-feature decoupling and dependency-inversion boundaries within an enterprise Android workspace.

---

### 3. 📱 [kotlinconf-app](kotlinconf-app)
The official companion client application for KotlinConf.

*   **Significance**: Serves as a prime showcase for Kotlin Multiplatform (KMP) mobile and desktop architecture.
*   **Alignment Focus**: Audited to demonstrate how multi-layered shared business logic and presentation state can be protected across shared multiplatform boundaries.

---

### 4. 🎛️ [ktor-arrow-example](ktor-arrow-example)
An advanced, production-grade backend implementation of the RealWorld (Conduit) App built on top of Ktor, Arrow, and SQLDelight.

*   **Integration Applied**:
    *   Configured Gradle version catalog (`gradle/libs.versions.toml`) with Konture plugins and dependencies.
    *   Applied the `io.github.baole.konture` plugin to the test modules and integrated `mavenLocal()` resolution.
    *   Implemented [ArchitectureTest.kt](ktor-arrow-example/src/test/kotlin/io/github/nomisrev/ArchitectureTest.kt) asserting:
        *   **Strict Layer Isolation**: Ensuring the HTTP presentation layer never references DB persistence adapters directly.
        *   **Schema Non-Leakage**: Guaranteeing routes and public services do not expose raw SQLDelight query descriptors or database tables in their public signatures.
        *   **Visibility Boundaries**: Demonstrating how to annotate architectural rules as `@Disabled` with detailed commentary when existing project code deliberately violates a design standard (e.g., public persistence classes instead of `internal` ones).
*   **How to Run**:
    ```bash
    cd showcases/ktor-arrow-example
    ./gradlew test --tests "io.github.nomisrev.ArchitectureTest"
    ```
