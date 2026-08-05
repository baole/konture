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
| **Baseline & Violation Ignoring** | ✅ `ignoreFailuresIn()` | ✅ `ignoreFailuresIn()` | ✅ `ignoreFailuresIn()` | ✅ `ignoreFailuresIn()` | ✅ `ignoreFailuresIn()` | ✅ `ignoreFailuresIn()` |
| **Logical Chaining Operators** (`and`, `or`, `not`) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Name / Path Filtering** (`that()`) | ✅ | ✅ | ✅ | ✅ (`havePath`, `haveName`) | ✅ (`matching`, `haveKey`) | ✅ |
| **Module Filtering** (`resideInAModule`) | ✅ | ✅ | ✅ | ✅ `resideInAModule` | ✅ `resideInModule` | ✅ |
| **Package Filtering** (`resideInAPackage`) | ✅ | ✅ | ✅ | ✅ `containPackage` / `resideInAPackage` | ✅ `resideInAPackage` | ✅ |
| **Annotation Filtering / Assertions** | ✅ (`haveAnnotationOf`) | ✅ (`beAnnotatedWith`) | ✅ (`beAnnotatedWith`) | ✅ (`containClassesWithAnnotation`) | ✅ (`containClassesWithAnnotation`) | ✅ (`beAnnotatedWith`) |
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

## 📚 KDoc API Reference

For detailed KDoc function signatures and type specifications generated directly from source headers, see the [Dokka API Docs](api-docs.md).

