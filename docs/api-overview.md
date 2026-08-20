# API Overview

Konture provides a unified, highly expressive DSL for architectural assertions across six core scopes:
- **`files {}`**: Source file imports, package placements, and file-level call/reference prohibitions.
- **`classes {}`**: Class declarations, interfaces, supertypes, modifiers, visibility, annotations, and dependencies.
- **`functions {}`**: Function declarations (top-level and member), return types, parameter signatures, and invocations.
- **`modules {}`**: Gradle module graph paths, module-to-module directional dependencies, cycle freedom, and applied plugins.
- **`slices {}`**: Logical horizontal or vertical package slices derived via capture group patterns (e.g., `com.acme.(*)..`).
- **`properties {}`**: Top-level and class property declarations, type signatures, and access prohibitions.

---

## 🗺️ Feature Parity & API Surface Matrix

The table below outlines all available builder entry points, scoping utilities, filtering conditions, and assertion rules across Konture's DSL surface:

| Feature Dimension | `files {}` | `classes {}` | `functions {}` | `modules {}` | `slices {}` | `properties {}` |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **Declarative Builder Entry Point** (`Konture.*()`) | ✅ `files()` | ✅ `classes()` | ✅ `functions()` | ✅ `modules()` | ✅ `slices()` | ✅ `properties()` |
| **SourceSet Selector Override** | ✅ `files(sourceSets)` | ✅ `classes(sourceSets)` | ✅ `functions(sourceSets)` | ✅ `modules(sourceSets)` | ✅ `slices(sourceSets)` | ✅ `properties(sourceSets)` |
| **Auto-Checking Block DSL** | ✅ `files { ... }` | ✅ `classes { ... }` | ✅ `functions { ... }` | ✅ `modules { ... }` | ✅ `slices { ... }` | ✅ `properties { ... }` |
| **Batch Architecture Context Integration** | ✅ `architecture { files { ... } }` | ✅ `architecture { classes { ... } }` | ✅ `architecture { functions { ... } }` | ✅ `architecture { modules { ... } }` | ✅ `architecture { slices { ... } }` | ✅ `architecture { properties { ... } }` |
| **Functional Inspection Scope** | ✅ `fileScope` | ✅ `classScope` / `scope` | ✅ `functionScope` | ✅ `moduleScope` | ✅ `sliceScope(pattern)` | ✅ `propertyScope` |
| **Module-Scoped Functional Entry** | ✅ `fileScopeFromModule` | ✅ `classScopeFromModule` | ✅ `functionScopeFromModule` | ✅ `moduleScopeFromModule` | ✅ `sliceScopeFromModule` | ✅ `propertyScopeFromModule` |
| **Package-Scoped Functional Entry** | ✅ `fileScopeFromPackage` | ✅ `classScopeFromPackage` | ✅ `functionScopeFromPackage` | ➖ *(N/A)* | ✅ `sliceScopeFromPackage` | ✅ `propertyScopeFromPackage` |
| **Scope Set Algebra Operators** (`+`, `-`) | ✅ `+` / `-` | ✅ `+` / `-` | ✅ `+` / `-` | ✅ `+` / `-` | ✅ `+` / `-` | ✅ `+` / `-` |
| **Allow Empty Selection** (`allowEmpty()`) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Debug Matched Printing** (`printMatched*()`) | ✅ `printMatchedFiles()` | ✅ `printMatchedClasses()` | ✅ `printMatchedFunctions()` | ✅ `printMatchedModules()` | ✅ `printMatchedSlices()` | ✅ `printMatchedProperties()` |
| **Debug All Discovered Printing** (`printAll*()`) | ✅ `printAllFiles()` | ✅ `printAllClasses()` | ✅ `printAllFunctions()` | ✅ `printAllModules()` | ✅ `printAllSlices()` | ✅ `printAllProperties()` |
| **Baseline & Violation Suppression** | ✅ `suppress { ... }` | ✅ `suppress { ... }` | ✅ `suppress { ... }` | ✅ `suppress { ... }` | ✅ `suppress { ... }` | ✅ `suppress { ... }` |
| **Logical Chaining Operators** (`and`, `or`, `not`) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Name / Path Filtering** (`that()`) | ✅ | ✅ | ✅ | ✅ (`havePath`, `haveName`) | ✅ (`matching`, `haveKey`) | ✅ |
| **Module Filtering** (`resideInAModule`) | ✅ | ✅ | ✅ | ✅ `resideInAModule` | ✅ `resideInAModule` | ✅ |
| **Package Filtering** (`resideInAPackage`) | ✅ | ✅ | ✅ | ✅ `resideInAPackage` | ✅ `resideInAPackage` | ✅ |
| **Annotation Filtering / Assertions** | ✅ (`containClassesWithAnnotation`) | ✅ (`beAnnotatedWith`) | ✅ (`beAnnotatedWith`) | ✅ (`containClassesWithAnnotation`) | ✅ (`containClassesWithAnnotation`) | ✅ (`beAnnotatedWith`) |
| **Visibility / Modifier Controls** | ➖ *(N/A)* | ✅ | ✅ | ➖ *(N/A)* | ➖ *(N/A)* | ✅ |
| **Call / Reference Prohibitions** | ✅ `notCall`, `notReferenceClass` | ✅ `notCall`, `notReferenceClass` | ✅ `notCall`, `notReferenceClass` | ✅ `notCall`, `notReferenceClass` | ✅ `notCall`, `notReferenceClass` | ✅ `notCall`, `notReferenceClass` |
| **Dependency Assertions** | ✅ `onlyDependOnPackages`, `notDependOnPackages`, `onlyDependOnModules`, `notDependOnModules` | ✅ `onlyDependOn*`, `notDependOn*` | ➖ | ✅ `onlyDependOnModules`, `notDependOnModules` | ✅ `onlyDependOnSlices`, `notDependOnSlice` | ➖ |
| **Cycle Detection Assertions** | ➖ | ✅ `beFreeOfCycles()` | ➖ | ✅ `beFreeOfCycles()` | ✅ `beFreeOfCycles()` | ➖ |
| **Plugin / Asset Assertions** | ➖ | ➖ | ➖ | ✅ `havePlugin`, `notHavePlugin` | ➖ | ➖ |
| **Type-Safe Overloads** (`KClass`, `reified T`) | ✅ | ✅ | ✅ | ✅ *(Calls/Refs/Annotations)* | ✅ *(Calls/Refs/Annotations)* | ✅ |
| **Composite Predicates** (`anyOf`, `allOf`, `noneOf`) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Custom Predicates & Assertions** (`satisfy`) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

---

## 💡 Quick Code Snippets by Scope

### 1. `files {}` Scope
```kotlin
Konture.files {
    that().resideInAPackage("com.acme.feature..")
    should().onlyDependOnPackages("com.acme.core..", "kotlin..")
    andShould().notUseWildcardImports()
}
```

### 2. `classes {}` Scope
```kotlin
Konture.classes {
    that().haveNameEndingWith("Repository")
    should().beInterfaces()
    andShould().resideInAPackage("com.acme.domain..")
}
```

### 3. `functions {}` Scope
```kotlin
Konture.functions {
    that().haveNameStartingWith("get")
    should().notReferenceClass<android.content.Context>()
}
```

### 4. `modules {}` Scope
```kotlin
Konture.modules {
    that().resideInAModule(":feature-*")
    should().notDependOnModules(":feature-*")
    andShould().beFreeOfCycles()
    andShould().notCall("java.lang.System.exit")
}
```

### 5. `slices {}` Scope
```kotlin
Konture.slices {
    matching("com.acme.(*)..")
    should().onlyDependOnSlices("core", "common")
    andShould().beFreeOfCycles()
}
```

### 6. `properties {}` Scope
```kotlin
Konture.properties {
    that().resideInPackageOf<MarkerClass>()
    should().haveTypeOf<StateFlow<*>>()
}
```

---

## 🚨 Structured Violation Model

When executing declarative rules with `.check()`, Konture returns a structured `ViolationReport`. The underlying core models are fully `@Serializable` using `kotlinx.serialization`, enabling custom test reporters, IDE plugins, or CI quality tools to consume structured diagnostic results:

| Core Model | Description |
| :--- | :--- |
| **`ViolationReport`** | Contains `ruleId`, `violations: List<Violation>`, `severity: Severity`, and computed flags `hasErrors` / `hasWarnings`. |
| **`Violation`** | Details an individual violation including `ruleId`, `subject: Subject`, `target: Subject?`, `sourceLocation: SourceLocation?`, `dependencyPath: List<Subject>`, `message`, and `severity`. |
| **`Subject`** | Sealed hierarchy identifying the target element (`ModuleSubject`, `ClassSubject`, `FunctionSubject`, or `CustomSubject`). |
| **`SourceLocation`** | Source location metadata containing build-root relative `filePath`, 1-based `line`, and optional `column`. |
| **`Severity`** | Enum representing violation importance (`INFO`, `WARNING`, `ERROR`). |

---

## 📚 KDoc API Reference

For detailed KDoc function signatures and type specifications generated directly from source headers, see the [Dokka API Docs](api-docs.md).

