package io.github.baole.konture.sample

import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class DslArchitectureTest {

    @Test
    fun `test source sets can be checked for prohibited Kotlin calls`() {
        // This test itself belongs to the captured Gradle test source set.
        Konture.files(sourceSets = SourceSets.tests()) {
            should().notCall("io.mockk.spyk")
        }
        // A real violation would be: io.mockk.spyk(value)
    }

    @Test
    fun `module and class verification using auto-checking blocks`() {
        // Auto-verifying module block DSL
        Konture.modules {
            that().haveNamePath(":data")
            should().onlyDependOnModules(":domain")
        }

        // Auto-verifying class block DSL
        Konture.classes {
            that().resideInAPackage("..domain..")
            should().onlyDependOnClassesInAnyPackage("..domain..", "kotlin..", "java..")
        }
    }

    @Test
    fun `nested visual layered architecture DSL block`() {
        Konture.layered {
            val domain = layer("domain") definedBy "..domain.."
            val data = layer("data") definedBy "..data.."
            val app = layer("app") definedBy "..app.."

            where(app) {
                mayNotBeAccessedByAnyLayer()
            }
            where(data) {
                mayOnlyBeAccessedByLayers(app)
            }
        }
    }

    @Test
    fun `unified architecture DSL context containing multiple rule types`() {
        Konture.architecture {
            modules {
                that().haveNamePath(":data")
                should().notDependOnModule(":domain")
            }

            classes {
                that().resideInAPackage("..domain..")
                that().haveNameEndingWith("Repository")
                should().beInterfaces()
            }
        }
    }

    @Test
    fun `algebraic scope operations with operator overloading`() {
        val domainScope = Konture.scopeFromPackage("io.github.baole.konture.sample.domain")
        val dataScope = Konture.scopeFromPackage("io.github.baole.konture.sample.data")

        // Merge two scopes using '+' operator
        val combinedScope = domainScope + dataScope

        // Subtract an excluded subpackage using '-' operator
        val publicDomainScope = domainScope - Konture.scopeFromPackage("io.github.baole.konture.sample.domain.exclude")

        // Perform assertions on the derived scopes
        combinedScope.classes().assertTrue("Combined scope classes should have package name") { cls ->
            cls.packageName.startsWith("io.github.baole.konture.sample")
        }
    }

    @Test
    fun `demo logical condition combining rules with anyOf and allOf`() {
        // Classes must satisfy compound logical checks (using the brand new logical DSL)
        Konture.classes {
            // Match any class residing in either domain or data package
            that().anyOf(
                { resideInAPackage("..domain..") },
                { resideInAPackage("..data..") }
            ).should().onlyDependOnClassesInAnyPackage(
                "..domain..", "..data..", "kotlin..", "java..", "android.."
            )
            
            // Match repository interfaces using allOf
            that().allOf(
                { resideInAPackage("..domain..") },
                { haveNameEndingWith("Repository") }
            ).should().beInterfaces()
        }
    }

    @Test
    fun `demo multi-parameter list and vararg rules`() {
        // Showcase the newly added multi-value (vararg/list) rules for Classes, Functions, and Properties
        // Note: Because this is a syntax-only demonstration and this showcase project has no matching source 
        // elements (e.g. classes annotated with "Service" or "Component"), we explicitly use allowEmpty() 
        // to prevent false-positive empty-selection AssertionError failures during verification.
        Konture.architecture {
            classes {
                allowEmpty()
                // Match classes annotated with either of these annotations, or having certain modifiers
                that().haveAnyAnnotationOf("Service", "Component")
                    .and().haveAllModifiers(Modifier.OPEN)
                
                // Assert that they reside in any of the specified visibilities or are assignable
                should().haveAnyVisibility(Visibility.PUBLIC, Visibility.INTERNAL)
            }

            functions {
                allowEmpty()
                // Match functions with suspend modifier and specific parameters
                that().haveAllModifiers(Modifier.SUSPEND)
                    .and().haveReturnType("String", "Int")
                
                // Assert parameters and annotations
                should().haveAnyParameterType("kotlin.String", "kotlin.Long")
                    .andShould().haveAnyAnnotationOf("Transactional", "EventHandler")
            }

            properties {
                allowEmpty()
                // Match properties with specific annotations
                that().haveAnyAnnotationOf("Inject", "Autowired")
                
                // Assert modifiers, visibilities, and types
                should().haveAllModifiers() // empty is always matched, or can specify multiple:
                    .andShould().haveAnyVisibility(Visibility.PRIVATE, Visibility.INTERNAL)
                    .andShould().haveType("kotlin.String", "kotlin.Int")
            }
        }
    }

    @Test
    fun `intentional architectural violations with multiple different location types`() {
        Konture.architecture {
            // 1. Module Violation (at module level)
            modules {
                allowEmpty()
                that().haveNamePath(":data")
                should().notDependOnModule(":domain")
            }

            // 2. Class Violation (at class level)
            classes {
                allowEmpty()
                that().resideInAPackage("..domain..")
                that().haveNameEndingWith("User")
                should().haveNameEndingWith("Dto")
            }

            // 3. File Violation (at file level)
            files {
                allowEmpty()
                that().haveNameEndingWith("UseCase.kt")
                should().haveNameEndingWith("Action.kt")
            }

            // 4. Function Violation (at function level)
            functions {
                allowEmpty()
                that().haveNameEndingWith("getUser")
                should().haveNameEndingWith("fetchUser")
            }

            // 5. Property Violation (at property/line level)
            properties {
                allowEmpty()
                that().haveNameEndingWith("id")
                should().haveType("kotlin.Int")
            }
        }
    }
}
