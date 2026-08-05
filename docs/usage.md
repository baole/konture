# Usage Guide

Once Konture is installed and integrated into your project, you can begin defining and enforcing your architectural rules. This guide covers how to write your first tests using Konture's two main paradigms, how to run them, and how to interpret failure messages.

> **🤖 Streamline with AI**
> Writing architecture tests is even faster with our specialized prompts! Load these in your AI workflow to generate correct, compile-safe rules:
> *   **[✍️ Writing Tests Prompt](ai-prompts/writing-tests-prompt.md)**: Standardized guidelines for crafting expressive constraints.
> {: .tip }

---

## 🗺️ API Overview

Konture provides a expressive DSL across six core scopes: `files {}`, `classes {}`, `functions {}`, `modules {}`, `slices {}`, and `properties {}`. See the full [API Overview Guide](api-overview.md) for detailed documentation and snippets.

| Feature Dimension | `files {}` | `classes {}` | `functions {}` | `modules {}` | `slices {}` | `properties {}` |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **Builder Entry Point** | ✅ `files()` | ✅ `classes()` | ✅ `functions()` | ✅ `modules()` | ✅ `slices()` | ✅ `properties()` |
| **Auto-Checking Block DSL** | ✅ `files { ... }` | ✅ `classes { ... }` | ✅ `functions { ... }` | ✅ `modules { ... }` | ✅ `slices { ... }` | ✅ `properties { ... }` |
| **Batch Context Integration** | ✅ `architecture { ... }` | ✅ `architecture { ... }` | ✅ `architecture { ... }` | ✅ `architecture { ... }` | ✅ `architecture { ... }` | ✅ `architecture { ... }` |
| **Functional Inspection Scope** | ✅ `fileScope` | ✅ `classScope` | ✅ `functionScope` | ✅ `moduleScope` | ✅ `sliceScope(...)` | ✅ `propertyScope` |
| **Module-Scoped Entry** | ✅ `fileScopeFromModule` | ✅ `classScopeFromModule` | ✅ `functionScopeFromModule` | ✅ `moduleScopeFromModule` | ✅ `sliceScopeFromModule` | ✅ `propertyScopeFromModule` |
| **Package Filtering** | ✅ `resideInAPackage` | ✅ `resideInAPackage` | ✅ `resideInAPackage` | ✅ `containPackage` | ✅ `resideInAPackage` | ✅ `resideInAPackage` |
| **Annotation Filtering** | ✅ `haveAnnotationOf` | ✅ `beAnnotatedWith` | ✅ `beAnnotatedWith` | ✅ `containClassesWithAnnotation` | ✅ `containClassesWithAnnotation` | ✅ `beAnnotatedWith` |
| **Call & Reference Prohibitions** | ✅ `notCall` / `notReferenceClass` | ✅ `notCall` / `notReferenceClass` | ✅ `notCall` / `notReferenceClass` | ✅ `notCall` / `notReferenceClass` | ✅ `notCall` / `notReferenceClass` | ✅ `notCall` / `notReferenceClass` |
| **Dependency Assertions** | ✅ `onlyDependOn*` / `notDependOn*` | ✅ `onlyDependOn*` / `notDependOn*` | ➖ | ✅ `onlyDependOnModules` | ✅ `onlyDependOnSlices` | ➖ |
| **Cycle Detection** | ➖ | ✅ `beFreeOfCycles()` | ➖ | ✅ `beFreeOfCycles()` | ✅ `beFreeOfCycles()` | ➖ |

---

## 📐 Writing Your First Test


Create a new Kotlin test class inside your dedicated architecture test module:
`konture-test/src/test/kotlin/com/acme/konture/ArchitectureTest.kt`

Konture supports two distinct, highly ergonomic API paradigms for designing your rules. Choose the one that best fits your team's style:

### 1. Fluent Scope (Konsist-Inspired)

The **Fluent Scope** style is an imperative, lambda-driven builder. You query the global project scope (`classes`, `files`, `functions`, or `properties`), filter elements using helper properties or extensions, and run assertions directly using an `assertTrue` lambda.

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

    @Test
    fun "viewmodel getters should not return Unit"() {
        Konture.functionScope
            .functions
            .withNameStartingWith("get")
            .assertTrue("Getters must return non-Unit types!") { func ->
                func.declaration.returnType != "Unit"
            }
    }

    @Test
    fun "properties in domain models must be read-only"() {
        Konture.propertyScope
            .properties
            .withPackage("..domain..")
            .assertTrue("Domain model properties must be val!") { prop ->
                prop.declaration.isVal
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

## Source-set-scoped usage rules

Konture's existing source rules inspect production source sets by default. Select test or custom source sets explicitly when a rule must inspect test code:

```kotlin
Konture.files(sourceSets = SourceSets.tests()) {
    should().notCall("io.mockk.spyk")
    should().notReferenceClass("io.mockk.MockK")
}

Konture.functions(sourceSets = SourceSets.named("test", "androidTest", "commonTest")) {
    should().notCall("io.mockk.spyk")
}
```

`SourceSets.named(...)` matches exact captured Gradle source-set names, `matchingName("*Test")` uses glob matching, and `SourceSets.of(role = SourceSetRole.TEST, kind = SourceSetKind.ANDROID)` selects a portable category. `notCall` analyzes Kotlin source calls, not runtime behavior; an unused import does not violate it.

## Type-safe type and annotation rules

Where a rule identifies a concrete Kotlin type or annotation, Konture also accepts a `KClass` or a reified type parameter. The existing string overloads remain useful for package patterns, unresolved or generated symbols, and source syntax that cannot be represented by a runtime class.

```kotlin
Konture.classes {
    that().haveAnnotationOf<Inject>()
    and().resideInPackageOf<MarkerClass>()
    and().resideInAModule(":core")
    should().beAssignableTo(Repository::class)
}

Konture.functions {
    that().haveReturnTypeOf<Result<*>>()
    and().resideInPackageOf<MarkerClass>()
    should().notReferenceClass<android.content.Context>()
    andShould().beSuspend()
    andShould().beOperator()
    andShould().haveParameterTypes(String::class, UserId::class)
}

Konture.properties {
    that().resideInPackageOf<MarkerClass>()
    should().notCall("android.content.Context.getString")
    andShould().haveTypeOf<StateFlow<*>>()
}

Konture.files {
    that().resideInPackageOf<MarkerClass>()
    should().notReferenceClass<LegacyClient>()
    andShould().onlyDependOnPackages("com.acme..", "kotlin..")
    andShould().anyOf(
        { resideInAPackage("com.acme.core..") },
        { resideInAPackage("com.acme.feature..") }
    )
}

Konture.slices {
    matching("com.acme.(*)..")
        .should().onlyDependOnSlices("core", "common")
        .andShould().notDependOnSlice("internal")
        .andShould().notCall<LegacyClient>()
}

Konture.modules {
    that().resideInAModule(":feature-*")
        .and().containPackage("com.acme.feature..")
        .should().beFreeOfCycles()
        .andShould().notCall("java.lang.System.exit")
}


```


Typed function and property rules compare the resolved raw declared type. For example, `List::class` matches `List<String>`, while explicit imports and import aliases resolve to their fully qualified types. Ambiguous references, generic arguments, nullability, type aliases, and type parameters still require the existing string or custom assertion APIs.

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

## 🛡️ Key Architectural Guardrails

Konture provides robust pre-built assertions to enforce software layout policies with high-level structural awareness.

### 1. Abstract & Interface Alignment
Kotlin interfaces are abstract by definition. In Konture, both abstract classes and interfaces are evaluated correctly under abstract rules.
```kotlin
// Matches both interfaces and abstract classes
Konture.classes()
    .that().areAbstract()
    .should().bePublic()
    .check()
```

### 2. Inline & Value Classes
Modern Kotlin value classes (`value class` with `@JvmInline`) parse with `Modifier.VALUE` instead of the legacy `Modifier.INLINE`, but they represent the same architectural concept. Konture's inline rules automatically evaluate both transparently.
```kotlin
Konture.classes()
    .that().haveNameEndingWith("Id")
    .should().beInline()
    .check()
```

### 3. Transitive Assignability (Supertypes & Subtypes)
Assertions like `beAssignableTo()` and `beAssignableFrom()` evaluate recursive inheritance trees:

*   **Supertype Check (`beAssignableTo`)**: Verifies that the selected classes implement or extend a specified supertype (inheritance up the tree).
*   **Subtype Check (`beAssignableFrom`)**: Verifies that the selected classes are supertypes of (assignable from) a specified subtype (inheritance down the tree).

Both assertions fully support transitive lookups, type-safe `KClass` parameters, and `reified` type parameters:

```kotlin
// UP: Ensure services extend BaseService
Konture.classes()
    .that().haveNameEndingWith("Service")
    .should().beAssignableTo(BaseService::class)
    .check()

// DOWN: Ensure base controllers are assignable from a specific specialized controller
Konture.classes()
    .that().haveNameEndingWith("Controller")
    .should().beAssignableFrom<SpecializedController>()
    .check()
```

Similarly, you can filter classes during selection using `areAssignableFrom()`:

```kotlin
Konture.classes()
    .that().areAssignableFrom<BaseService>()
    .should().bePublic()
    .check()
```


### 4. Dependency Package Isolation
The `assertOnlyDependOnClassesInAnyPackage()` assertion enforces layer isolation by evaluating both internal dependencies and external libraries, while seamlessly excluding standard platform namespaces (`java.*`, `javax.*`, `kotlin.*`).
```kotlin
// Ensure domain model only depends on the core business layer
Konture.scope
    .classes
    .withPackage("..domain..")
    .assertOnlyDependOnClassesInAnyPackage("..domain..", "..core..")
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
