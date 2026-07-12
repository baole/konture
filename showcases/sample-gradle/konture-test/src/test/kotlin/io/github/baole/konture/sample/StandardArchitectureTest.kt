package io.github.baole.konture.sample

import io.github.baole.konture.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StandardArchitectureTest {

    @Test
    fun `no circular dependencies allowed in the module graph`() {
        Konture.assertNoCycles()
    }

    @Test
    fun `module dependency rules verified via standard archunit style`() {
        // :domain should not depend on any other module
        Konture.modules()
            .that().haveNamePath(":domain")
            .should().notDependOnModule(":data")
            .check()

        Konture.modules()
            .that().haveNamePath(":domain")
            .should().notDependOnModule(":app")
            .check()

        // :data should only depend on :domain
        Konture.modules()
            .that().haveNamePath(":data")
            .should().onlyDependOnModules(":domain")
            .check()
    }

    @Test
    fun `class dependency rules verified via standard archunit style`() {
        // Classes in domain package should only depend on domain, kotlin or java
        Konture.classes()
            .that().resideInAPackage("..domain..")
            .should().onlyDependOnClassesInAnyPackage("..domain..", "kotlin..", "java..")
            .check()

        // Repositories should be interfaces
        Konture.classes()
            .that().resideInAPackage("..domain..")
            .that().haveNameEndingWith("Repository")
            .should().beInterfaces()
            .check()

        // Implementations should be classes
        Konture.classes()
            .that().resideInAPackage("..data..")
            .that().haveNameEndingWith("RepositoryImpl")
            .should().onlyBeAccessedByAnyPackage("..data..", "..app..")
            .check()
    }

    @Test
    fun `class level constraints verified via standard konsist style`() {
        val scope = Konture.scope

        // Repositories must be interfaces
        scope.withNameEndingWith("Repository")
            .assertTrue("Repositories must be interfaces") {
                it.isInterface
            }

        // Classes in domain should not import classes from data
        val domainScope = Konture.scopeFromPackage("io.github.baole.konture.sample.domain")
        domainScope.assertTrue("Domain classes must not access data package") { cls ->
            cls.imports.none { import -> import.contains(".data.") }
        }
    }

    @Test
    fun `violating dependency rule should throw assertion error`() {
        assertThrows<AssertionError> {
            Konture.modules()
                .that().haveNamePath(":data")
                .should().onlyDependOnModules(":app")
                .check()
        }
    }

    @Test
    fun `use cases must reside in domain package and have names ending with UseCase via archunit style`() {
        Konture.classes()
            .that().haveNameEndingWith("UseCase")
            .should().resideInAPackage("..domain..")
            .check()
    }

    @Test
    fun `use cases must reside in domain package and have names ending with UseCase via konsist style`() {
        val scope = Konture.scope
        scope.withNameEndingWith("UseCase")
            .assertTrue("UseCases must reside in the domain package") { cls ->
                cls.packageName.contains(".domain")
            }
    }

    @Test
    fun `scalability module pattern matching using wildcards`() {
        // Assert that any module starting with ':da' (like :data) only depends on modules starting with ':do' (like :domain)
        Konture.modules()
            .that().haveNameMatching(":da*")
            .should().onlyDependOnModules(":do*")
            .check()

        // Assert that domain module is only depended on by modules matching ':da*' (like :data) or ':ap*' (like :app)
        Konture.modules()
            .that().haveNamePath(":domain")
            .should().onlyBeDependedOnBy(":da*", ":ap*")
            .check()

        // Assert that domain doesn't depend on any data module
        Konture.modules()
            .that().haveNamePath(":domain")
            .should().notDependOnModule(":da*")
            .check()
    }

    @Test
    fun `scalability package pattern matching using double dot wildcards`() {
        // Find classes in any subpackage containing '.domain' and verify they reside in the specific domain FQN
        Konture.classes()
            .that().resideInAPackage("..domain..")
            .should().resideInAPackage("io.github.baole.konture.sample.domain..")
            .check()
    }

    @Test
    fun `exclusions are honored in loaded project graph`() {
        val graph = Konture.projectGraph
        val allClasses = graph.getAllModules().flatMap { it.classes }
        
        val serviceNames = allClasses.map { it.name }
        val packageNames = allClasses.map { it.packageName }
        
        org.junit.jupiter.api.Assertions.assertFalse(serviceNames.contains("ExcludedService"), "ExcludedService should have been excluded")
        org.junit.jupiter.api.Assertions.assertFalse(serviceNames.contains("ExcludedClassInExcludedPackage"), "ExcludedClassInExcludedPackage should have been excluded")
        org.junit.jupiter.api.Assertions.assertFalse(packageNames.any { it.startsWith("io.github.baole.konture.sample.domain.exclude") }, "Packages matching exclusion pattern should have been excluded")
    }

    @Test
    fun `layered architecture verified via standard chaining syntax`() {
        Konture.layeredArchitecture()
            .layer("domain").definedBy("..domain..")
            .layer("data").definedBy("..data..")
            .layer("app").definedBy("..app..")
            .whereLayer("app").mayNotBeAccessedByAnyLayer()
            .whereLayer("data").mayOnlyBeAccessedByLayers("app")
            .check()
    }

    @Test
    fun `domain class signatures must not leak implementation-specific packages`() {
        // Enforce Type Leakage check using the Konsist-style functional scope API
        Konture.scope.classes()
            .withNameEndingWith("UseCase")
            .assertTrue("UseCase signature must be technical-agnostic") { cls ->
                val signatureTypes = cls.functions.flatMap { func ->
                    listOf(func.returnType) + func.parameters.map { it.type }
                }
                signatureTypes.none { type -> 
                    type.contains(".data.") || type.contains(".app.") 
                }
            }
    }

    @Test
    fun `use cases must only be accessed by the presentation app layer`() {
        // Enforce strict call boundary check: domain logic entry points must be called only from app or domain itself
        Konture.classes()
            .that().haveNameEndingWith("UseCase")
            .should().onlyBeAccessedByAnyPackage("..app..", "..domain..")
            .check()
    }

    @Test
    fun `domain entities must have public visibility`() {
        // Enforce visibility boundary: domain model objects must be fully public for standard consumer access
        Konture.classes()
            .that().haveNameEndingWith("User")
            .should().bePublic()
            .check()
    }
}
