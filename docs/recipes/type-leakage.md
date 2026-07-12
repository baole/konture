# Cross-Layer Type Leakage

Type leakage occurs when technical implementation models (such as database entities annotated with `@Entity`, Room/Realm records, or network wire DTO schemas) are exposed directly in the public function signatures or property declarations of other layers (like UseCases, business Services, or UI components).

Each layer must map technical objects to domain-clean, decoupled representations before crossing boundaries.

```text
[Network DTO] ---> (MAPPED) ---> [Domain Model] ---> [UI Component]
      |                                                    ^
      +----------------- PROHIBITED LEAKAGE ---------------+
```

---

## 💡 The Rationale
* **Encapsulation & Decoupling**: If your views or domain logic rely directly on database schema models or network JSON contracts, any API change or database migration will instantly cascade through your entire codebase, breaking compilation.
* **Maintainability**: Clear separation of types guarantees that your database schema can evolve independently from your UI presentation state or business rules.
* **Separation of Concerns**: DTO annotations (like Jackson, kotlinx.serialization, or Moshi) and database descriptors (like Hibernate, JPA, or Room) remain contained inside the specific infrastructure adapters where they belong.

---

## 🛠️ Implementation with Konture

Because Konture constructs a full Abstract Syntax Tree (AST) representation, you can inspect the parameter types, return types, and class annotations of your code using the highly flexible **Konsist-style functional `KontureScope` API**.

### 1. Blocking DTOs & Entities in Domain Signatures

You can query all classes in the domain package ending with `UseCase` or `Service` and verify that neither their function parameter types nor return types reference DTO or Entity package coordinates:

```kotlin
import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class TypeLeakageTest {

    @Test
    fun `domain class signatures must not contain technical entities or DTOs`() {
        // 1. Retrieve a scope representing the entire project
        val projectScope = Konture.scope

        // 2. Query use cases and assert no leaked technical types exist
        projectScope.classes
            .withNameEndingWith("UseCase")
            .assertTrue("UseCase signature must be technical-agnostic") { cls ->
                // Gather all types referenced in return types and parameters
                val signatureTypes = cls.functions.flatMap { func ->
                    listOf(func.returnType) + func.parameters.map { it.type }
                }

                // Verify that no signature type is defined in .dto. or .entity. packages
                signatureTypes.none { type -> 
                    type.contains(".dto.") || type.contains(".entity.") 
                }
            }
    }
}
```

### 2. Blocking JPA Entities in Controller Responses

Similarly, you can enforce that REST controllers do not return database entities directly, forcing the use of separate response schemas:

```kotlin
import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class ControllerResponseTest {

    @Test
    fun `controllers must not leak database entity signatures`() {
        Konture.scope.classes
            .withNameEndingWith("Controller")
            .assertTrue("Controller return types must not be persistence entities") { cls ->
                val returnTypes = cls.functions.map { it.returnType }
                
                // Assert no return type references a persistence entity
                returnTypes.none { type -> type.contains(".entity.") }
            }
    }
}
```

---

## 🚨 Example Failure Output

If a developer exposes a concrete JPA model `UserEntity` inside a domain use case signature:

```text
AssertionError: Architecture violation: UseCase signature must be technical-agnostic
Offending classes:
  - Class 'io.github.baole.konture.sample.domain.usecase.GetUserUseCase' failed assertion.
    (at /path/to/project/showcases/sample-gradle/domain/src/main/kotlin/io.github.baole.konture/sample/domain/usecase/GetUserUseCase.kt:14)
```

The error informs you of the failure, points straight to the offending UseCase declaration, and supplies a clickable absolute path to open the file instantly.
