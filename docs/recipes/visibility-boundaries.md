# API Surface & Visibility Boundary Enforcement

To keep modular architectures clean, implementation details must remain hidden from the outside. If every helper class, DB connector, or internal utility is declared `public`, outer modules can bind directly to them, rendering modularization and compile-time boundaries meaningless.

Implementation files should be marked with Kotlin's `internal` visibility. Furthermore, any public-facing API surface exposed across project or package boundaries must be cleanly documented using KDocs so developers can integrate them correctly.

```text
               +--------------------------------------+
               |          PUBLIC API INTERFACE        | <--- Exposed to outside
               +--------------------------------------+
                                  |
                                  v (Delegates to)
               +--------------------------------------+
               |     INTERNAL IMPLEMENTATION CLASS    | <--- Hidden (No direct outside access)
               +--------------------------------------+
```

---

## 💡 The Rationale
* **True Encapsulation**: Restricting utility classes or sub-components to `internal` or `private` ensures they can be modified or refactored with zero risk of breaking dependent code modules.
* **Maintainable API Footprint**: Keeping public surfaces minimal minimizes library or component footprints, making it much easier to preserve backwards compatibility.
* **Rich Developer Experience**: Requiring proper KDoc comments on every exposed public class and function results in clean auto-completion popups and reliable code generation.

---

## 🛠️ Implementation with Konture

Because Konture operates directly on your files' AST structures, it can check class, function, and property visibility modifiers as well as KDoc strings.

### 1. Enforcing Internal Visibility on Implementations

You can ensure that implementation classes (e.g. classes residing in a `.internal.` or `.impl.` package) are never declared `public`:

```kotlin
import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class EncapsulationTest {

    @Test
    fun `implementation classes must remain strictly internal`() {
        Konture.classes {
            that().resideInAPackage("..internal..")
                .or().resideInAPackage("..impl..")
                .should().beInternal()
        }
    }
}
```

### 2. Requiring KDocs on Public Surfaces

You can query all `public` interfaces and classes inside your core packages and verify that they contain valid KDoc headers:

```kotlin
import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class ApiDocumentationTest {

    @Test
    fun `all public-facing api classes and interfaces must declare kdocs`() {
        // Enforce KDocs on all public classes inside the primary export package
        Konture.scope.classes
            .withPackage("io.github.baole.konture.api..")
            .filter { it.visibility == Visibility.PUBLIC }
            .assertTrue("Exposed public API must have KDoc comments") { cls ->
                !cls.kdocText.isNullOrBlank()
            }
    }
}
```

---

## 🚨 Example Failure Output

If an internal helper is accidentally declared `public`:

```text
AssertionError: Architecture violation: Implementation classes must remain strictly internal
Offending classes:
  - Class 'io.github.baole.konture.sample.internal.XmlConfigHelper' is public instead of internal
    (at /path/to/project/showcases/sample-gradle/internal/src/main/kotlin/io.github.baole.konture/sample/internal/XmlConfigHelper.kt:6)
```

The error prints the exact line location, allowing you to instantly enforce `internal` visibility.
